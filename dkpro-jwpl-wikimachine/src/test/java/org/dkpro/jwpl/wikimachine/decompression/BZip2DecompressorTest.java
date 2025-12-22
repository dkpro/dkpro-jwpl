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

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
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
}
