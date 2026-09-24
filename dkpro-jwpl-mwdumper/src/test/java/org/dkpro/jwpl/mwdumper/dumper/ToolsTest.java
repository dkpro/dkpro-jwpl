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
package org.dkpro.jwpl.mwdumper.dumper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the bzip2 handling of {@link Tools}, most of all that standard bzip2 files are written
 * and read (see issue #714).
 */
class ToolsTest
{

    private static final String CONTENT = "<mediawiki><page><title>Über</title></page></mediawiki>\n";

    @TempDir
    Path dir;

    @Test
    void writesAStandardBzip2Stream() throws IOException
    {
        Path file = dir.resolve("out.xml.bz2");
        try (OutputStream out = Tools.createBZip2File(file.toString())) {
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
        Path file = dir.resolve("out.xml.bz2");
        try (OutputStream out = Tools.createBZip2File(file.toString())) {
            out.write(CONTENT.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(CONTENT, read(file));
    }

    @Test
    void readsAStandardBzip2File() throws IOException
    {
        Path file = dir.resolve("standard.xml.bz2");
        Files.write(file, compress(CONTENT));

        assertEquals(CONTENT, read(file));
    }

    @Test
    void readsAFileWithTheLegacyExtraPrefix() throws IOException
    {
        // Earlier versions wrote a superfluous "BZ" in front of the stream header ("BZBZh...")
        Path file = dir.resolve("legacy.xml.bz2");
        byte[] standard = compress(CONTENT);
        byte[] legacy = new byte[standard.length + 2];
        legacy[0] = 'B';
        legacy[1] = 'Z';
        System.arraycopy(standard, 0, legacy, 2, standard.length);
        Files.write(file, legacy);

        assertEquals(CONTENT, read(file));
    }

    private static String read(Path file) throws IOException
    {
        try (InputStream in = Tools.openInputFile(file.toString())) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
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
