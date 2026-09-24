/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.wikimachine.decompression;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a source {@link InputStream} on a separate thread and hands its bytes to the consumer
 * through a bounded queue of chunks. Wrapped around a decompressing stream, this lets the
 * decompression run while the consumer parses the data it already received, instead of both
 * taking turns on the same thread.
 * <p>
 * The bytes returned are exactly the bytes of the source, in the same order. A failure of the
 * source is not turned into an end of stream: it is rethrown to the consumer as an
 * {@link IOException} once all data read before the failure has been consumed. The source is
 * closed by the reading thread when it is exhausted, when it fails, or when this stream is
 * closed. The reading thread is a daemon thread, so an abandoned stream cannot keep the JVM
 * alive.
 * <p>
 * Instances are meant to be used by a single consumer thread.
 */
public final class ReadAheadInputStream
    extends InputStream
{
    private static final Logger LOG = LoggerFactory.getLogger(ReadAheadInputStream.class);

    /**
     * The default size of a single chunk handed over between the threads, in bytes.
     */
    public static final int DEFAULT_CHUNK_SIZE = 1 << 16;

    /**
     * The default number of chunks that may be read ahead of the consumer.
     */
    public static final int DEFAULT_CAPACITY = 16;

    /**
     * How long {@link #close()} waits for the reading thread to terminate.
     */
    private static final long CLOSE_TIMEOUT_MILLIS = 5000;

    /**
     * An element of the queue: either data, the end of the source, or the failure of the source.
     */
    private record Chunk(byte[] data, int length, Throwable failure)
    {
        private static final Chunk END = new Chunk(new byte[0], -1, null);

        boolean isEnd()
        {
            return length < 0;
        }
    }

    private final String name;
    private final BlockingQueue<Chunk> queue;
    private final Thread reader;
    private volatile boolean closed;

    /** The chunk currently consumed; {@code null} if the next one has to be taken. */
    private Chunk current;
    private int position;

    /**
     * Wraps {@code source} with {@link #DEFAULT_CHUNK_SIZE} and {@link #DEFAULT_CAPACITY}.
     *
     * @param source The stream to read ahead. Must not be {@code null}.
     * @param name   A name for the stream, used to name the reading thread.
     */
    public ReadAheadInputStream(InputStream source, String name)
    {
        this(source, name, DEFAULT_CHUNK_SIZE, DEFAULT_CAPACITY);
    }

    /**
     * Wraps {@code source} and immediately starts reading it on a separate thread.
     *
     * @param source    The stream to read ahead. Must not be {@code null}.
     * @param name      A name for the stream, used to name the reading thread.
     * @param chunkSize The size of a single chunk in bytes. Must be positive.
     * @param capacity  The number of chunks that may be read ahead. Must be positive.
     * @throws IllegalArgumentException Thrown if {@code chunkSize} or {@code capacity} were not
     *                                  positive.
     */
    public ReadAheadInputStream(InputStream source, String name, int chunkSize, int capacity)
    {
        Objects.requireNonNull(source, "source");
        if (chunkSize <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("Chunk size and capacity must be positive.");
        }
        this.name = name;
        queue = new ArrayBlockingQueue<>(capacity);
        reader = new Thread(() -> readSource(source, chunkSize), "jwpl-read-ahead-" + name);
        reader.setDaemon(true);
        reader.start();
    }

    private void readSource(InputStream source, int chunkSize)
    {
        Chunk terminal = Chunk.END;
        try (source) {
            boolean end = false;
            while (!end && !closed) {
                byte[] data = new byte[chunkSize];
                int length = 0;
                try {
                    while (length < chunkSize) {
                        int n = source.read(data, length, chunkSize - length);
                        if (n < 0) {
                            end = true;
                            break;
                        }
                        length += n;
                    }
                }
                finally {
                    // Hand over what was read before a failure, so the consumer sees every
                    // byte the source delivered before the failure is reported.
                    if (length > 0) {
                        queue.put(new Chunk(data, length, null));
                    }
                }
            }
        }
        catch (InterruptedException e) {
            // Only close() interrupts this thread: nobody is waiting for further chunks.
            return;
        }
        catch (Throwable t) {
            terminal = new Chunk(null, -1, t);
        }
        if (closed) {
            return;
        }
        try {
            queue.put(terminal);
        }
        catch (InterruptedException e) {
            // Closed while waiting for room: the terminal chunk is no longer of interest.
        }
    }

    /**
     * @return The chunk to read from, or {@code null} if the end of the source was reached.
     * @throws IOException Thrown if the source failed or this stream was closed.
     */
    private Chunk fill() throws IOException
    {
        if (closed) {
            throw new IOException("Stream closed");
        }
        while (current == null || (!current.isEnd() && position >= current.length())) {
            try {
                current = queue.take();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException("Interrupted while waiting for data.");
            }
            position = 0;
        }
        if (current.isEnd()) {
            // Keep the terminal chunk as the current one: every further read reports the same.
            if (current.failure() != null) {
                throw new IOException("Reading ahead of '" + name
                        + "' failed: " + current.failure().getMessage(), current.failure());
            }
            return null;
        }
        return current;
    }

    @Override
    public int read() throws IOException
    {
        Chunk chunk = fill();
        if (chunk == null) {
            return -1;
        }
        return chunk.data()[position++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        Chunk chunk = fill();
        if (chunk == null) {
            return -1;
        }
        int n = Math.min(len, chunk.length() - position);
        System.arraycopy(chunk.data(), position, b, off, n);
        position += n;
        return n;
    }

    @Override
    public int available() throws IOException
    {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (current == null || current.isEnd()) {
            return 0;
        }
        return current.length() - position;
    }

    /**
     * Stops the reading thread, which closes the source, and discards all data read ahead.
     * Waits a bounded time for the thread to terminate.
     */
    @Override
    public void close() throws IOException
    {
        if (closed) {
            return;
        }
        closed = true;
        reader.interrupt();
        // Make room in case the reading thread is blocked on a full queue.
        queue.clear();
        current = null;
        try {
            reader.join(CLOSE_TIMEOUT_MILLIS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (reader.isAlive()) {
            LOG.warn("Thread '{}' did not terminate within {} ms after close.", reader.getName(),
                    CLOSE_TIMEOUT_MILLIS);
        }
    }
}
