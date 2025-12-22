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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

abstract class AbstractDecompressorTest {

    protected static final String EXPECTED_CONTENT = "This file is here to test decompression types.";

    protected abstract IDecompressor getDecompressor();

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"", "\t", "\n"})
    void testGetInputStreamThrowsInvalid(String input) {
        assertThrows(IllegalArgumentException.class, () -> getDecompressor().getInputStream(input));
        assertThrows(IllegalArgumentException.class, () -> getDecompressor().getInputStream(Path.of(input)));
    }

    @Test
    void testGetInputStreamThrowsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> getDecompressor().getInputStream((String) null));
        assertThrows(IllegalArgumentException.class, () -> getDecompressor().getInputStream((Path) null));
    }

    @Test
    void testGetInputStreamThrowsWithDirectory(@TempDir Path input) {
        assertThrows(IOException.class, () -> getDecompressor().getInputStream(input));
    }
    
    protected void getAndCheck(String input) throws IOException {
        try (InputStream in = getDecompressor().getInputStream(input)) {
            assertNotNull(in);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            assertNotNull(content);
            assertEquals(EXPECTED_CONTENT, content);
        }
    }
}
