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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TxtFileWriterTest
{

    private static final List<Object[]> ROWS = List.of(
            new Object[] { 0, 127 },
            new Object[] { 128, Integer.MAX_VALUE },
            new Object[] { -1, Integer.MIN_VALUE },
            new Object[] { 42, 42, "Käse_(Lebensmittel)" },
            new Object[] { 7, 7, null, "text with\ttab and\nnewline", false, true, false },
            new Object[] { 7, "Title", 7, "NULL", "NULL" },
            new Object[] { "null", "german", "Begriffsklärung", "!Hauptkategorie", 10L, 2L, 1L,
                    3L, "20260101" },
            new Object[] { "🙂", "日本語" },
            new Object[] { "single" },
            new Object[] {});

    @TempDir
    Path tempDir;

    /** Reference encoding as produced by the previous implementation. */
    private static byte[] expected()
    {
        StringBuilder sb = new StringBuilder();
        for (Object[] row : ROWS) {
            sb.append(Strings.join(row, "\t")).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void testOutputIsByteIdenticalToPreviousFormat() throws IOException
    {
        Path file = tempDir.resolve("table.txt");
        TxtFileWriter writer = new TxtFileWriter(file.toString());
        for (Object[] row : ROWS) {
            if (row.length == 2 && row[0] instanceof Integer && row[1] instanceof Integer) {
                // exercise the primitive overload
                writer.addRow((int) row[0], (int) row[1]);
            }
            else {
                writer.addRow(row);
            }
        }
        writer.export();
        assertArrayEquals(expected(), Files.readAllBytes(file));
    }

    @Test
    void testLiteralRowRendering() throws IOException
    {
        Path file = tempDir.resolve("literal.txt");
        try (TxtFileWriter writer = new TxtFileWriter(file.toString())) {
            writer.addRow(1, 2);
            writer.addRow(3, null, "NULL", "ä");
            writer.export();
        }
        assertArrayEquals("1\t2\n3\tnull\tNULL\tä\n".getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(file));
    }

    @Test
    void testRepeatedCloseIsHarmless()
    {
        TxtFileWriter writer = new TxtFileWriter(new ByteArrayOutputStream());
        writer.addRow(1, 2);
        writer.export();
        assertDoesNotThrow(writer::close);
    }

    @Test
    void testExportPropagatesWriteError()
    {
        TxtFileWriter writer = new TxtFileWriter(new FailingOutputStream(false));
        writer.addRow(1, 2);
        assertThrows(UncheckedIOException.class, writer::export);
    }

    @Test
    void testClosePropagatesCloseError()
    {
        TxtFileWriter writer = new TxtFileWriter(new FailingOutputStream(true));
        writer.addRow("a", "b");
        assertThrows(UncheckedIOException.class, writer::close);
    }

    private static final class FailingOutputStream
        extends OutputStream
    {
        private final boolean failOnCloseOnly;

        private FailingOutputStream(boolean failOnCloseOnly)
        {
            this.failOnCloseOnly = failOnCloseOnly;
        }

        @Override
        public void write(int b) throws IOException
        {
            if (!failOnCloseOnly) {
                throw new IOException("disk full");
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            if (!failOnCloseOnly) {
                throw new IOException("disk full");
            }
        }

        @Override
        public void close() throws IOException
        {
            throw new IOException("close failed");
        }
    }
}
