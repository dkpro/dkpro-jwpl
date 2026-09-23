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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UniversalDecompressorTest extends AbstractDecompressorTest {

    @TempDir
    private Path tmpDir;

    // SUT
    private UniversalDecompressor udc;

    @BeforeEach
    public void setup() {
        udc = new UniversalDecompressor();
        assertNotNull(udc);
    }

    @Override
    protected IDecompressor getDecompressor() {
        return udc;
    }

    @ParameterizedTest
    @ValueSource(strings = {"decompressor-ar.xml"})
    void testInitializeWithExternalConfig(String input) throws IOException {
        Path defaultTestConfig = Path.of("src/test/resources/" + input);
        Path externalConfig = tmpDir.resolve(input);
        /* Copy project local XML config file to external tmp path */
        Files.copy(defaultTestConfig, externalConfig, StandardCopyOption.REPLACE_EXISTING);
        UniversalDecompressor udc = new UniversalDecompressor(externalConfig);
        assertNotNull(udc);
        assertTrue(udc.isSupported("archive.txt.ar"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"archive.txt.gz", "archive.txt.bz2", "archive.txt.7z",
            "src/test/resources/archive.txt.gz"})
    void testIsSupported(String input)
    {
        assertTrue(udc.isSupported(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"archive.txt.gz", "archive.txt.bz2", "archive.txt.7z",
            "src/test/resources/archive.txt.gz", "src/test/resources/archive.txt.bz2",
            "src/test/resources/archive.txt.7z"})
    void testGetInputStream(String input) throws IOException {
        getAndCheck(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/uncompressed.txt"})
    void testGetInputStreamWithDefault(String input) throws IOException {
        assertNotNull(udc);
        try (InputStream in = udc.getInputStream(Path.of(input))) {
            assertNotNull(in);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/archive.txt.ar"})
    @DisabledOnOs(OS.WINDOWS)
    void testGetInputStreamWithExternalConfig(String input) throws IOException {
        Path arConfig = Path.of("src/test/resources/decompressor-ar.xml");
        UniversalDecompressor udc = new UniversalDecompressor(arConfig);
        assertNotNull(udc);
        assertTrue(udc.isSupported(input));

        final Path p = Path.of(input);
        try (InputStream in = new BufferedInputStream(udc.getInputStream(p))) {
          assertNotNull(in);
          String content = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
          assertNotNull(content);
          assertEquals(EXPECTED_CONTENT, content);
        }
    }

    @Test
    void testGetInputStreamSequenceDispatchesBz2() throws IOException {
        final String a = "alpha\n";
        final String b = "beta\n";
        final Path partA = writeBz2(tmpDir.resolve("dump.xml-p1p10.bz2"), a);
        final Path partB = writeBz2(tmpDir.resolve("dump.xml-p11p20.bz2"), b);

        try (InputStream in = udc.getInputStreamSequence(List.of(partA, partB))) {
            assertNotNull(in);
            assertEquals(a + b, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGetInputStreamSequenceDispatchesGz() throws IOException {
        final String a = "alpha\n";
        final String b = "beta\n";
        final Path partA = writeGz(tmpDir.resolve("dump.xml-p1p10.gz"), a);
        final Path partB = writeGz(tmpDir.resolve("dump.xml-p11p20.gz"), b);

        try (InputStream in = udc.getInputStreamSequence(List.of(partA, partB))) {
            assertNotNull(in);
            assertEquals(a + b, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGetInputStreamSequenceRejects7z() throws IOException {
        // Create an empty placeholder so checkPath passes (no file-existence check).
        final Path part = Files.createFile(tmpDir.resolve("dump.xml-p1p10.7z"));
        assertThrows(IOException.class, () -> udc.getInputStreamSequence(List.of(part)));
    }

    @Test
    void testGetInputStreamSequenceRejectsUnsupportedExtension() throws IOException {
        final Path part = Files.createFile(tmpDir.resolve("dump.xml-p1p10.rar"));
        assertThrows(IOException.class, () -> udc.getInputStreamSequence(List.of(part)));
    }

    @Test
    void testGetInputStreamSequenceRejectsMixedExtensions() throws IOException {
        final Path bz2 = writeBz2(tmpDir.resolve("dump.xml-p1p10.bz2"), "x\n");
        final Path gz = writeGz(tmpDir.resolve("dump.xml-p11p20.gz"), "y\n");
        assertThrows(IOException.class, () -> udc.getInputStreamSequence(List.of(bz2, gz)));
    }

    @Test
    void testExternalToolUnavailableFallsBackToInternal() throws IOException {
        final String content = "fallback content\n";
        final Path bz2 = writeBz2(tmpDir.resolve("dump.xml.bz2"), content);
        final Path gz = writeGz(tmpDir.resolve("dump.sql.gz"), content);
        final UniversalDecompressor external = new UniversalDecompressor(writeConfig(
                "bz2", "jwpl-no-such-decompressor-589 -dc %f",
                "gz", "jwpl-no-such-decompressor-589 -dc %f"));

        for (Path p : List.of(bz2, gz)) {
            try (InputStream in = external.getInputStream(p)) {
                assertEquals(content, new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    void testExternalToolUnavailableWithoutInternalSupportFails() throws IOException {
        final Path rar = Files.createFile(tmpDir.resolve("dump.xml.rar"));
        final UniversalDecompressor external = new UniversalDecompressor(
                writeConfig("rar", "jwpl-no-such-decompressor-589 p %f"));
        assertThrows(IOException.class, () -> external.getInputStream(rar));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testExternalToolFailureIsReported() throws IOException {
        assumeTrue(isOnPath("false"));
        final Path bz2 = writeBz2(tmpDir.resolve("dump.xml.bz2"), "content\n");
        final UniversalDecompressor external = new UniversalDecompressor(
                writeConfig("bz2", "false %f"));

        try (InputStream in = external.getInputStream(bz2)) {
            assertThrows(IOException.class, in::readAllBytes);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"bzip2", "lbzip2"})
    @DisabledOnOs(OS.WINDOWS)
    void testExternalBz2MatchesInternal(String tool) throws IOException {
        assumeTrue(isOnPath(tool));
        final Path bz2 = writeBz2(tmpDir.resolve("with space").resolve("dump.xml.bz2"), sample());
        assertExternalMatchesInternal(writeConfig("bz2", tool + " -dc %f"), bz2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"gzip", "pigz"})
    @DisabledOnOs(OS.WINDOWS)
    void testExternalGzMatchesInternal(String tool) throws IOException {
        assumeTrue(isOnPath(tool));
        final Path gz = writeGz(tmpDir.resolve("with space").resolve("dump.sql.gz"), sample());
        assertExternalMatchesInternal(writeConfig("gz", tool + " -dc %f"), gz);
    }

    private void assertExternalMatchesInternal(Path config, Path archive) throws IOException {
        final byte[] expected;
        try (InputStream in = udc.getInputStream(archive)) {
            expected = in.readAllBytes();
        }
        try (InputStream in = new UniversalDecompressor(config).getInputStream(archive)) {
            assertArrayEquals(expected, in.readAllBytes());
        }
    }

    private static String sample() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50_000; i++) {
            sb.append("<page><id>").append(i).append("</id><title>Page ").append(i)
                    .append("</title></page>\n");
        }
        return sb.toString();
    }

    private Path writeConfig(String... entries) throws IOException {
        final Properties properties = new Properties();
        for (int i = 0; i < entries.length; i += 2) {
            properties.setProperty(entries[i], entries[i + 1]);
        }
        final Path config = Files.createTempFile(tmpDir, "decompressor", ".xml");
        try (OutputStream os = Files.newOutputStream(config)) {
            properties.storeToXML(os, null);
        }
        return config;
    }

    private static boolean isOnPath(String executable) {
        final String path = System.getenv("PATH");
        if (path == null) {
            return false;
        }
        for (String dir : path.split(File.pathSeparator)) {
            if (!dir.isEmpty() && Files.isExecutable(Path.of(dir, executable))) {
                return true;
            }
        }
        return false;
    }

    private static Path writeBz2(Path out, String content) throws IOException {
        Files.createDirectories(out.getParent());
        try (OutputStream os = new BZip2CompressorOutputStream(Files.newOutputStream(out))) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return out;
    }

    private static Path writeGz(Path out, String content) throws IOException {
        Files.createDirectories(out.getParent());
        try (OutputStream os = new GZIPOutputStream(Files.newOutputStream(out))) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return out;
    }
}
