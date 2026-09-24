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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * A single threaded, unsynchronised {@link Reader} that buffers another {@link Reader} in a large
 * {@code char[]}.
 * <p>
 * {@link java.io.StreamTokenizer} pulls its input one {@code char} at a time through
 * {@link Reader#read()}. On a {@link java.io.BufferedReader} every such call acquires the lock of
 * the reader, which is pure overhead for a tokenizer that is only ever used by a single thread.
 * This reader serves {@link #read()} from its buffer without any locking and only calls the
 * wrapped reader to refill the buffer in bulk. The sequence of characters it delivers is exactly
 * the sequence of the wrapped reader.
 * <p>
 * Instances are <em>not</em> thread safe. {@link #mark(int)} and {@link #reset()} are not
 * supported.
 */
final class UnsyncBufferedReader
    extends Reader
{

    /** The default size of the buffer, in {@code char}s. */
    static final int DEFAULT_BUFFER_SIZE = 1 << 16;

    private final Reader in;
    private final char[] buf;
    /** Position of the next {@code char} to deliver from {@link #buf}. */
    private int pos;
    /** Number of valid {@code char}s in {@link #buf}. */
    private int lim;
    /** Whether the wrapped reader has signalled the end of its input. */
    private boolean eof;

    /**
     * @param in The {@link Reader} to buffer. Must not be {@code null}.
     */
    UnsyncBufferedReader(Reader in)
    {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * @param in   The {@link Reader} to buffer. Must not be {@code null}.
     * @param size The size of the buffer in {@code char}s. Must be positive.
     */
    UnsyncBufferedReader(Reader in, int size)
    {
        this.in = Objects.requireNonNull(in, "in");
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive, got " + size);
        }
        this.buf = new char[size];
    }

    /**
     * Refills the buffer from the wrapped reader.
     *
     * @return {@code false} if the end of the input has been reached.
     */
    private boolean fill() throws IOException
    {
        if (eof) {
            return false;
        }
        int n;
        do {
            n = in.read(buf, 0, buf.length);
        }
        while (n == 0);
        if (n < 0) {
            eof = true;
            pos = 0;
            lim = 0;
            return false;
        }
        pos = 0;
        lim = n;
        return true;
    }

    @Override
    public int read() throws IOException
    {
        if (pos >= lim && !fill()) {
            return -1;
        }
        return buf[pos++];
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        if (len == 0) {
            return 0;
        }
        if (pos >= lim && !fill()) {
            return -1;
        }
        final int n = Math.min(len, lim - pos);
        System.arraycopy(buf, pos, cbuf, off, n);
        pos += n;
        return n;
    }

    @Override
    public boolean ready() throws IOException
    {
        return pos < lim || (!eof && in.ready());
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
