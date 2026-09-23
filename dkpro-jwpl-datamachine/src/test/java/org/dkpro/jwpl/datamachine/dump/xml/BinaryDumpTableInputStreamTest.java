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
package org.dkpro.jwpl.datamachine.dump.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.dkpro.jwpl.wikimachine.decompression.GZipDecompressor;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.dkpro.jwpl.wikimachine.util.UTFDataOutputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reads uncompressed and gzip-compressed {@code text.bin} files back through
 * {@link BinaryDumpTableInputStream} and a {@link TextParser} and checks that every row is
 * returned unchanged.
 */
class BinaryDumpTableInputStreamTest
{

    private static final List<String> TEXTS = new ArrayList<>();

    @TempDir
    Path tmp;

    @BeforeAll
    static void createTexts()
    {
        TEXTS.add("");
        TEXTS.add("Plain ASCII text.");
        TEXTS.add("Mehrbyte: äöüß 日本語 😀");
        StringBuilder large = new StringBuilder();
        for (int i = 0; large.length() < 300_000; i++) {
            large.append("Row ").append(i).append(" é中 ");
        }
        TEXTS.add(large.toString());
        for (int i = 0; i < 1_000; i++) {
            TEXTS.add("short text " + i);
        }
    }

    @Test
    void testReadUncompressed() throws IOException
    {
        Path file = tmp.resolve("text.bin");
        try (OutputStream out = Files.newOutputStream(file)) {
            writeRows(out);
        }
        assertRows(new BufferedInputStream(Files.newInputStream(file)));
    }

    @Test
    void testReadCompressed() throws IOException
    {
        Path file = tmp.resolve("text.bin.gz");
        try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(file))) {
            writeRows(out);
        }
        assertRows(new GZipDecompressor().getInputStream(file));
    }

    @Test
    void testCloseClosesUnderlyingStream() throws IOException
    {
        Path file = tmp.resolve("text.bin");
        try (OutputStream out = Files.newOutputStream(file)) {
            writeRows(out);
        }
        InputStream source = new BufferedInputStream(Files.newInputStream(file));
        BinaryDumpTableInputStream in = new BinaryDumpTableInputStream();
        in.initialize(source, DumpTableEnum.TEXT);
        in.close();
        assertThrows(IOException.class, source::read);
    }

    private static void writeRows(OutputStream out) throws IOException
    {
        UTFDataOutputStream data = new UTFDataOutputStream(out);
        for (int i = 0; i < TEXTS.size(); i++) {
            data.writeInt(i);
            data.writeUTFAsArray(TEXTS.get(i));
        }
        data.flush();
    }

    private static void assertRows(InputStream source) throws IOException
    {
        BinaryDumpTableInputStream in = new BinaryDumpTableInputStream();
        in.initialize(source, DumpTableEnum.TEXT);
        TextParser parser = new TextParser();
        parser.setInputStream(in);
        try {
            for (int i = 0; i < TEXTS.size(); i++) {
                assertTrue(parser.next(), "Missing row " + i);
                assertEquals(i, parser.getOldId());
                assertEquals(TEXTS.get(i), parser.getOldText());
            }
            assertFalse(parser.next());
        }
        finally {
            parser.close();
        }
    }
}
