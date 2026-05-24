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
package org.dkpro.jwpl.wikimachine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DumpFileDiscoveryTest
{

    private static final Set<String> EXTENSIONS = Set.of("bz2", "gz", "7z");

    // hasPageRange -------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "dewiki-20260101-pages-articles1.xml-p1p297012.bz2",
            "enwiki-20250601-pages-meta-history2.xml-p297013p1262093.bz2",
            "/tmp/enwiki-20250601-pages-articles1.xml-p1p812.bz2",
            "foo-pages-articles27.xml-p0p999999.7z"
    })
    void hasPageRangeTrue(String name)
    {
        assertTrue(DumpFileDiscovery.hasPageRange(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "dewiki-20260101-pages-articles.xml.bz2",
            "pagelinks.sql.gz",
            "random.txt",
            "pages-articles1.xml-p1.bz2"
    })
    void hasPageRangeFalse(String name)
    {
        assertFalse(DumpFileDiscovery.hasPageRange(name));
    }

    @Test
    void hasPageRangeHandlesNull()
    {
        assertFalse(DumpFileDiscovery.hasPageRange((String) null));
        assertFalse(DumpFileDiscovery.hasPageRange((File) null));
    }

    // matchesRole --------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "dewiki-20260101-pages-articles.xml.bz2",
            "enwiki-20250601-pages-articles.xml.gz",
            "dewiki-20260101-pages-articles1.xml-p1p297012.bz2",
            "dewiki-20260101-pages-articles3.xml-p2762094p3376257.bz2"
    })
    void matchesRolePagesArticles(String name)
    {
        assertTrue(DumpFileDiscovery.matchesRole(name, "pages-articles", EXTENSIONS));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "dewiki-20260101-pages-articles-multistream.xml.bz2",
            "dewiki-20260101-pages-articles-multistream-index.txt.bz2",
            "dewiki-20260101-pages-meta-current.xml.bz2",
            "pagelinks.sql.gz",
            "dewiki-20260101-pages-articles.xml.rar"
    })
    void doesNotMatchRolePagesArticles(String name)
    {
        assertFalse(DumpFileDiscovery.matchesRole(name, "pages-articles", EXTENSIONS));
    }

    @Test
    void matchesRoleMetaCurrent()
    {
        assertTrue(DumpFileDiscovery.matchesRole(
                "enwiki-20250601-pages-meta-current.xml.bz2", "pages-meta-current", EXTENSIONS));
        assertTrue(DumpFileDiscovery.matchesRole(
                "enwiki-20250601-pages-meta-current1.xml-p1p100.bz2",
                "pages-meta-current", EXTENSIONS));
    }

    @Test
    void matchesRoleHandlesNullsAndEmptyExtensions()
    {
        assertFalse(DumpFileDiscovery.matchesRole(null, "pages-articles", EXTENSIONS));
        assertFalse(DumpFileDiscovery.matchesRole("x.bz2", null, EXTENSIONS));
        assertFalse(DumpFileDiscovery.matchesRole("x.bz2", "pages-articles", null));
        assertFalse(DumpFileDiscovery.matchesRole(
                "x.bz2", "pages-articles", Collections.emptySet()));
    }

    // orderByPageRange ---------------------------------------------------------

    @Test
    void orderByPageRangeSortsByStart()
    {
        final File a = new File("dewiki-20260101-pages-articles2.xml-p297013p1262093.bz2");
        final File b = new File("dewiki-20260101-pages-articles1.xml-p1p297012.bz2");
        final File c = new File("dewiki-20260101-pages-articles3.xml-p2762094p3376257.bz2");
        final File d = new File("dewiki-20260101-pages-articles3.xml-p1262094p2762093.bz2");

        final List<File> ordered = DumpFileDiscovery.orderByPageRange(Arrays.asList(a, b, c, d));

        assertEquals(Arrays.asList(b, a, d, c), ordered);
    }

    @Test
    void orderByPageRangePutsUnrangedFirstStable()
    {
        final File single = new File("dewiki-20260101-pages-articles.xml.bz2");
        final File p2 = new File("dewiki-20260101-pages-articles2.xml-p297013p1262093.bz2");
        final File p1 = new File("dewiki-20260101-pages-articles1.xml-p1p297012.bz2");

        final List<File> ordered = DumpFileDiscovery.orderByPageRange(Arrays.asList(p2, single, p1));

        assertEquals(Arrays.asList(single, p1, p2), ordered);
    }

    @Test
    void orderByPageRangeReturnsEmptyForEmptyInput()
    {
        assertTrue(DumpFileDiscovery.orderByPageRange(Collections.emptyList()).isEmpty());
    }

    @Test
    void orderByPageRangeRejectsNullInput()
    {
        assertThrows(IllegalArgumentException.class,
                () -> DumpFileDiscovery.orderByPageRange(null));
    }

    @Test
    void orderByPageRangeRejectsNullElement()
    {
        assertThrows(IllegalArgumentException.class,
                () -> DumpFileDiscovery.orderByPageRange(Arrays.asList(
                        new File("pages-articles1.xml-p1p10.bz2"), null)));
    }
}
