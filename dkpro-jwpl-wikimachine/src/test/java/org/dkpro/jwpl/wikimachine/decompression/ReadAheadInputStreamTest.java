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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReadAheadInputStreamTest
{
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @ParameterizedTest
    @CsvSource({"0,16,4", "1,16,4", "15,16,4", "16,16,4", "17,16,4", "1000003,1024,3",
            "1000003,65536,16"})
    void testDataIsIdentical(int size, int chunkSize, int capacity)
    {
        final byte[] expected = random(size);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            try (InputStream in = new ReadAheadInputStream(new ByteArrayInputStream(expected),
                    "test", chunkSize, capacity)) {
                assertArrayEquals(expected, in.readAllBytes());
                assertEquals(-1, in.read());
                assertEquals(-1, in.read(new byte[8], 0, 8));
            }
        });
    }

    @Test
    void testDataIsIdenticalWithSingleByteAndOddSizedReads()
    {
        final byte[] expected = random(100_000);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            final ByteArrayOutputStream actual = new ByteArrayOutputStream();
            try (InputStream in = new ReadAheadInputStream(
                    new TrickleInputStream(new ByteArrayInputStream(expected)), "test", 1000, 2)) {
                final byte[] buffer = new byte[777];
                int b;
                while ((b = in.read()) >= 0) {
                    actual.write(b);
                    int n = in.read(buffer, 3, 700);
                    if (n < 0) {
                        break;
                    }
                    actual.write(buffer, 3, n);
                }
            }
            assertArrayEquals(expected, actual.toByteArray());
        });
    }

    @Test
    void testSourceFailureIsPropagatedAfterPrecedingData()
    {
        final byte[] prefix = random(10_000);
        final IOException failure = new IOException("corrupt block");
        assertTimeoutPreemptively(TIMEOUT, () -> {
            try (InputStream in = new ReadAheadInputStream(
                    new FailingInputStream(prefix, failure), "test", 1024, 2)) {
                final byte[] actual = in.readNBytes(prefix.length);
                assertArrayEquals(prefix, actual);
                final IOException thrown = assertThrows(IOException.class, in::read);
                assertSame(failure, thrown.getCause());
                // The failure is sticky: it is never followed by a regular end of stream.
                assertThrows(IOException.class, in::read);
                assertThrows(IOException.class, () -> in.read(new byte[4], 0, 4));
            }
        });
    }

    @Test
    void testRuntimeFailureIsPropagatedAsIOException()
    {
        final RuntimeException failure = new IllegalStateException("decoder bug");
        assertTimeoutPreemptively(TIMEOUT, () -> {
            try (InputStream in = new ReadAheadInputStream(new InputStream()
            {
                @Override
                public int read()
                {
                    throw failure;
                }
            }, "test")) {
                final IOException thrown = assertThrows(IOException.class, in::readAllBytes);
                assertSame(failure, thrown.getCause());
            }
        });
    }

    @Test
    void testEarlyCloseStopsReaderAndClosesSource()
    {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            final EndlessInputStream source = new EndlessInputStream();
            final InputStream in = new ReadAheadInputStream(source, "test", 16, 2);
            assertEquals(16, in.readNBytes(16).length);
            // The queue is full by now and the reading thread is blocked handing over a chunk.
            assertTrue(source.read.await(10, TimeUnit.SECONDS));
            in.close();
            assertTrue(source.closed.get());
            assertThrows(IOException.class, in::read);
            // Closing twice is harmless.
            in.close();
        });
    }

    @Test
    void testCloseWithoutReadingDoesNotHang()
    {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            final EndlessInputStream source = new EndlessInputStream();
            new ReadAheadInputStream(source, "test", 16, 1).close();
            assertTrue(source.closed.get());
        });
    }

    @Test
    void testSourceIsClosedAtEnd()
    {
        assertTimeoutPreemptively(TIMEOUT, () -> {
            final AtomicBoolean closed = new AtomicBoolean();
            try (InputStream in = new ReadAheadInputStream(new ByteArrayInputStream(random(100))
            {
                @Override
                public void close()
                {
                    closed.set(true);
                }
            }, "test")) {
                in.readAllBytes();
            }
            assertTrue(closed.get());
        });
    }

    @Test
    void testInvalidArguments()
    {
        final InputStream source = InputStream.nullInputStream();
        assertThrows(NullPointerException.class, () -> new ReadAheadInputStream(null, "test"));
        assertThrows(IllegalArgumentException.class,
                () -> new ReadAheadInputStream(source, "test", 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ReadAheadInputStream(source, "test", 1, 0));
    }

    private static byte[] random(int size)
    {
        final byte[] data = new byte[size];
        new Random(size).nextBytes(data);
        return data;
    }

    /**
     * Returns at most a few bytes per call, like a decompressor at block boundaries.
     */
    private static final class TrickleInputStream
        extends InputStream
    {
        private final InputStream delegate;

        TrickleInputStream(InputStream delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException
        {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return delegate.read(b, off, Math.min(len, 13));
        }
    }

    /**
     * Returns {@code prefix}, then fails with {@code failure}.
     */
    private static final class FailingInputStream
        extends InputStream
    {
        private final InputStream prefix;
        private final IOException failure;

        FailingInputStream(byte[] prefix, IOException failure)
        {
            this.prefix = new ByteArrayInputStream(prefix);
            this.failure = failure;
        }

        @Override
        public int read() throws IOException
        {
            int b = prefix.read();
            if (b < 0) {
                throw failure;
            }
            return b;
        }
    }

    /**
     * Never ends; records whether it was read from and closed.
     */
    private static final class EndlessInputStream
        extends InputStream
    {
        private final CountDownLatch read = new CountDownLatch(1);
        private final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public int read()
        {
            read.countDown();
            return 'x';
        }

        @Override
        public void close()
        {
            closed.set(true);
        }
    }
}
