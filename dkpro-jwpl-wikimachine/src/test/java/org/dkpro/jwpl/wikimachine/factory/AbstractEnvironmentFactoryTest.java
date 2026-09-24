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
package org.dkpro.jwpl.wikimachine.factory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.dkpro.jwpl.wikimachine.decompression.UniversalDecompressor;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractEnvironmentFactoryTest
{

    @TempDir
    private Path tmpDir;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void testCreateDecompressorWithoutConfig(String configLocation)
    {
        UniversalDecompressor udc = AbstractEnvironmentFactory.createDecompressor(configLocation);
        assertNotNull(udc);
        assertTrue(udc.isSupported("archive.txt.bz2"));
        assertFalse(udc.isSupported("archive.txt.ar"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/decompressor-ar.xml"})
    void testCreateDecompressorLoadsConfig(String configLocation)
    {
        UniversalDecompressor udc = AbstractEnvironmentFactory.createDecompressor(configLocation);
        assertNotNull(udc);
        assertTrue(udc.isSupported("archive.txt.bz2"));
        assertTrue(udc.isSupported("archive.txt.ar"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"src/test/resources/decompressor-ar.xml"})
    void testCreateDecompressorWithReadAhead(String configLocation)
    {
        assertTrue(AbstractEnvironmentFactory.createDecompressor(configLocation, true).isReadAhead());
        assertFalse(AbstractEnvironmentFactory.createDecompressor(configLocation, false).isReadAhead());
        assertFalse(AbstractEnvironmentFactory.createDecompressor(configLocation).isReadAhead());
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing-decompressor.xml"})
    void testCreateDecompressorFallsBackIfConfigIsAbsent(String configLocation)
    {
        UniversalDecompressor udc = AbstractEnvironmentFactory.createDecompressor(
                tmpDir.resolve(configLocation).toString());
        assertNotNull(udc);
        assertTrue(udc.isSupported("archive.txt.bz2"));
        assertFalse(udc.isSupported("archive.txt.ar"));
    }
}
