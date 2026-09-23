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
package org.dkpro.jwpl.wikimachine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UTFDataInputStreamTest
{

    /** Returns at most {@code chunk} bytes per bulk read, as a pipe may do. */
    private static final class ShortReadInputStream
        extends FilterInputStream
    {
        private final int chunk;

        ShortReadInputStream(InputStream in, int chunk)
        {
            super(in);
            this.chunk = chunk;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return super.read(b, off, Math.min(len, chunk));
        }
    }

    private static byte[] encode(String... values) throws IOException
    {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (UTFDataOutputStream out = new UTFDataOutputStream(bytes)) {
            for (String value : values) {
                out.writeUTFAsArray(value);
            }
        }
        return bytes.toByteArray();
    }

    @Test
    void readUTFAsArrayReadsCompleteValueDespiteShortReads() throws IOException
    {
        final char[] chars = new char[100_000];
        Arrays.fill(chars, 'ä');
        final String large = new String(chars);

        final byte[] encoded = encode(large, "tail");
        try (UTFDataInputStream in = new UTFDataInputStream(
                new ShortReadInputStream(new ByteArrayInputStream(encoded), 1000))) {
            assertEquals(large, in.readUTFAsArray());
            assertEquals("tail", in.readUTFAsArray());
        }
    }

    @Test
    void readUTFAsArrayFailsOnTruncatedValue() throws IOException
    {
        final byte[] encoded = encode("truncated value");
        final byte[] truncated = Arrays.copyOf(encoded, encoded.length - 3);
        try (UTFDataInputStream in = new UTFDataInputStream(
                new ByteArrayInputStream(truncated))) {
            assertThrows(EOFException.class, in::readUTFAsArray);
        }
    }
}
