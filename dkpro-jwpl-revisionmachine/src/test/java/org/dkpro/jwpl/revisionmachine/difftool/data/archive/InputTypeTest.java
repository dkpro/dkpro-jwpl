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
package org.dkpro.jwpl.revisionmachine.difftool.data.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link InputType}, most of all the type a dump file name implies (see issue #105).
 */
class InputTypeTest
{

    @ParameterizedTest
    @CsvSource({ //
            "dewiki-20250201-pages-meta-history.xml, XML", //
            "dewiki-20250201-pages-meta-history.xml.bz2, BZIP2", //
            "dewiki-20250201-pages-meta-history.xml.7z, SEVENZIP", //
            "/data/dumps/DEWIKI.XML.BZ2, BZIP2", //
            "  spaced.7z  , SEVENZIP" })
    void derivesTheTypeFromTheExtension(String path, InputType expected)
    {
        assertEquals(expected, InputType.fromPath(path));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "dump", "dump.gz", "dump.zip", "dump.xml.gz", ".xmlish" })
    void reportsNoTypeForAnExtensionItDoesNotKnow(String path)
    {
        assertNull(InputType.fromPath(path));
    }

    @Test
    void parsesTheStringRepresentation()
    {
        assertEquals(InputType.XML, InputType.parse("xml"));
        assertEquals(InputType.BZIP2, InputType.parse("BZIP2"));
        assertEquals(InputType.SEVENZIP, InputType.parse("SevenZip"));
        assertThrows(IllegalArgumentException.class, () -> InputType.parse("gzip"));
    }
}
