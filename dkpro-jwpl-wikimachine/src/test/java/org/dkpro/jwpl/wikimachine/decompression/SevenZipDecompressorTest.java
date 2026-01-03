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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SevenZipDecompressorTest extends AbstractDecompressorTest {

    // SUT
    private IDecompressor decomp;

    @BeforeEach
    public void setUp() {
        decomp = new SevenZipDecompressor();
    }

    @Override
    protected IDecompressor getDecompressor() {
        return decomp;
    }

    @ParameterizedTest
    @ValueSource(strings = {"archive.txt.7z", "src/test/resources/archive.txt.7z"})
    void testGetInputStream(String input) throws IOException {
        getAndCheck(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"empty.txt.7z", "src/test/resources/empty.txt.7z"})
    void testGetInputStreamWithEmptyArchive(String input) {
        assertThrows(EOFException.class, () -> getDecompressor().getInputStream(input));
    }

    @Test
    void testGetInputStreamWithRandomResourceName() throws IOException {
        final InputStream in = getDecompressor().getInputStream(UUID.randomUUID().toString());
        assertNull(in);
    }
}
