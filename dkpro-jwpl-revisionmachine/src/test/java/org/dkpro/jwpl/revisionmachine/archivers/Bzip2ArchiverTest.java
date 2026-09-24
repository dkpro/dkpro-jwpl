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
package org.dkpro.jwpl.revisionmachine.archivers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link Bzip2Archiver}, most of all that it writes standard bzip2 files (see issue #709).
 */
class Bzip2ArchiverTest
{

    private static final String CONTENT = "INSERT INTO revisions VALUES (1, 'Über');\r\n";

    @TempDir
    Path dir;

    @Test
    void writesAStandardBzip2Stream() throws IOException
    {
        Path file = dir.resolve("out.sql.bz2");
        try (OutputStream out = new Bzip2Archiver().getCompressionStream(file.toString())) {
            out.write(CONTENT.getBytes(StandardCharsets.UTF_8));
        }

        byte[] bytes = Files.readAllBytes(file);
        assertArrayEquals(new byte[] { 'B', 'Z', 'h' }, Arrays.copyOf(bytes, 3));
        try (InputStream in = new BZip2CompressorInputStream(Files.newInputStream(file))) {
            assertEquals(CONTENT, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void readsItsOwnOutput() throws IOException
    {
        Path file = dir.resolve("out.sql.bz2");
        Bzip2Archiver archiver = new Bzip2Archiver();
        try (OutputStream out = archiver.getCompressionStream(file.toString())) {
            out.write(CONTENT.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(CONTENT, read(archiver, file));
    }

    @Test
    void readsAStandardBzip2File() throws IOException
    {
        Path file = dir.resolve("standard.xml.bz2");
        Files.write(file, compress(CONTENT));

        assertEquals(CONTENT, read(new Bzip2Archiver(), file));
    }

    @Test
    void readsAFileWithTheLegacyExtraPrefix() throws IOException
    {
        // Earlier versions wrote a superfluous "BZ" in front of the stream header ("BZBZh...")
        Path file = dir.resolve("legacy.sql.bz2");
        byte[] standard = compress(CONTENT);
        byte[] legacy = new byte[standard.length + 2];
        legacy[0] = 'B';
        legacy[1] = 'Z';
        System.arraycopy(standard, 0, legacy, 2, standard.length);
        Files.write(file, legacy);

        assertEquals(CONTENT, read(new Bzip2Archiver(), file));
    }

    private static String read(Bzip2Archiver archiver, Path file) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try (Reader reader = archiver.getDecompressionStream(file.toString(), "UTF-8")) {
            char[] buf = new char[1024];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        }
        return sb.toString();
    }

    private static byte[] compress(String content) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStream out = new BZip2CompressorOutputStream(bytes)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }
}
