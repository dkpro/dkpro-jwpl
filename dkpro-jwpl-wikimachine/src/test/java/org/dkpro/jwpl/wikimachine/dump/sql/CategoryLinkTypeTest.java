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
package org.dkpro.jwpl.wikimachine.dump.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link CategoryLinkType#fromDumpValue(String)}.
 */
class CategoryLinkTypeTest
{

    @ParameterizedTest
    @ValueSource(strings = { "page", "PAGE", "Page", "subcat", "SUBCAT", "subCat", "file", "FILE",
        "File", "", "pages", "pag", "sub cat", "unknown", "ſubcat", "İ", "fİle",
        "K", "páge", "PAGE\u0000" })
    void matchesLowerCaseSwitch(String value)
    {
        assertEquals(legacy(value), CategoryLinkType.fromDumpValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = { "page", "Page", "PAGE" })
    void recognisesPage(String value)
    {
        assertEquals(CategoryLinkType.PAGE, CategoryLinkType.fromDumpValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = { "subcat", "SubCat", "SUBCAT" })
    void recognisesSubcat(String value)
    {
        assertEquals(CategoryLinkType.SUBCAT, CategoryLinkType.fromDumpValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file", "File", "FILE" })
    void recognisesFile(String value)
    {
        assertEquals(CategoryLinkType.FILE, CategoryLinkType.fromDumpValue(value));
    }

    @Test
    void mapsNullToUnknown()
    {
        assertEquals(CategoryLinkType.UNKNOWN, CategoryLinkType.fromDumpValue(null));
    }

    /** The implementation used before issue #552, kept as the reference. */
    private static CategoryLinkType legacy(String value)
    {
        switch (value.toLowerCase(Locale.ROOT)) {
        case "page":
            return CategoryLinkType.PAGE;
        case "subcat":
            return CategoryLinkType.SUBCAT;
        case "file":
            return CategoryLinkType.FILE;
        default:
            return CategoryLinkType.UNKNOWN;
        }
    }
}
