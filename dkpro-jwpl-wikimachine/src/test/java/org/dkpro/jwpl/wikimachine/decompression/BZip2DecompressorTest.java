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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BZip2DecompressorTest extends AbstractDecompressorTest {

    // SUT
    private IDecompressor decomp;

    @BeforeEach
    public void setUp() {
        decomp = new BZip2Decompressor();
    }

    @Override
    protected IDecompressor getDecompressor() {
        return decomp;
    }

    @ParameterizedTest
    @ValueSource(strings = {"archive.txt.bz2", "src/test/resources/archive.txt.bz2"})
    void testGetInputStream(String input) throws IOException {
        getAndCheck(input);
    }

    @Test
    void testGetInputStreamSequenceConcatenatesParts(@TempDir Path dir) throws IOException {
        final String contentA = "part-a payload\n";
        final String contentB = "part-b payload\n";
        final Path partA = writeBz2(dir.resolve("dump.xml-p1p10.bz2"), contentA);
        final Path partB = writeBz2(dir.resolve("dump.xml-p11p20.bz2"), contentB);

        try (InputStream in = decomp.getInputStreamSequence(List.of(partA, partB))) {
            assertNotNull(in);
            final String decompressed = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(contentA + contentB, decompressed);
        }
    }

    @Test
    void testGetInputStreamSequenceSinglePartEqualsSingleFile(@TempDir Path dir) throws IOException {
        final String content = "lonely payload\n";
        final Path part = writeBz2(dir.resolve("dump.xml-p1p10.bz2"), content);

        try (InputStream in = decomp.getInputStreamSequence(List.of(part))) {
            assertNotNull(in);
            assertEquals(content, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGetInputStreamReadsAllStreamsOfMultistreamFile(@TempDir Path dir) throws IOException {
        final String contentA = "<page>first stream</page>\n";
        final String contentB = "<page>second stream</page>\n";
        final String contentC = "<page>third stream</page>\n";
        final Path multistream = writeMultistreamBz2(dir.resolve("dump-multistream.xml.bz2"),
                contentA, contentB, contentC);

        try (InputStream in = decomp.getInputStream(multistream)) {
            assertNotNull(in);
            assertEquals(contentA + contentB + contentC,
                    new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGetInputStreamSequenceReadsAllStreamsOfMultistreamParts(@TempDir Path dir)
        throws IOException {
        final Path partA = writeMultistreamBz2(dir.resolve("dump.xml-p1p10.bz2"), "a1\n", "a2\n");
        final Path partB = writeMultistreamBz2(dir.resolve("dump.xml-p11p20.bz2"), "b1\n", "b2\n");

        try (InputStream in = decomp.getInputStreamSequence(List.of(partA, partB))) {
            assertNotNull(in);
            assertEquals("a1\na2\nb1\nb2\n", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Writes each chunk as an independent bzip2 stream and concatenates them, as found in
     * Wikimedia's multistream dumps.
     */
    private static Path writeMultistreamBz2(Path out, String... chunks) throws IOException {
        try (OutputStream os = Files.newOutputStream(out)) {
            for (String chunk : chunks) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try (OutputStream bz2 = new BZip2CompressorOutputStream(stream)) {
                    bz2.write(chunk.getBytes(StandardCharsets.UTF_8));
                }
                stream.writeTo(os);
            }
        }
        return out;
    }

    private static Path writeBz2(Path out, String content) throws IOException {
        try (OutputStream os = new BZip2CompressorOutputStream(Files.newOutputStream(out))) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return out;
    }
}
