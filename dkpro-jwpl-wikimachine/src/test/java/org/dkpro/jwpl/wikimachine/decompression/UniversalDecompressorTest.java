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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UniversalDecompressorTest {

    private static final String EXPECTED_CONTENT = "This file is here to test decompression types.";

    @TempDir
    private Path tmpDir;

    private UniversalDecompressor udc;

    @BeforeEach
    public void setup() {
        udc = new UniversalDecompressor();
        assertNotNull(udc);
    }

    @ParameterizedTest
    @ValueSource(strings = {"decompressor-ar.xml"})
    public void testInitializeWithExternalConfig(String input) throws IOException {
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
    public void testIsSupported(String input)
    {
        assertTrue(udc.isSupported(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"archive.txt.gz", "archive.txt.bz2",
            "src/test/resources/archive.txt.gz", "src/test/resources/archive.txt.bz2"})
    public void testGetInputStream(String input) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(udc.getInputStream(input))) {
            assertNotNull(in);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertNotNull(content);
            assertEquals(EXPECTED_CONTENT, content);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/archive.txt.ar"})
    @DisabledOnOs(OS.WINDOWS)
    public void testGetInputStreamWithExternalConfig(String input) throws IOException {
        Path arConfig = Path.of("src/test/resources/decompressor-ar.xml");
        UniversalDecompressor udc = new UniversalDecompressor(arConfig);
        assertNotNull(udc);
        assertTrue(udc.isSupported(input));

        try (InputStream in = udc.getInputStream(input)) {
          assertNotNull(in);
          String content = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
          assertNotNull(content);
          assertEquals(EXPECTED_CONTENT, content);
        }
    }
}
