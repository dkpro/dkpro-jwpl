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
package org.dkpro.jwpl.util.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks the queries behind the {@code get*ContainingTemplate*} and
 * {@code count*ContainingTemplate*} page methods of {@link WikipediaTemplateInfo} against an
 * in-memory database holding the page template index.
 */
class WikipediaTemplateInfoPageIndexQueryTest
{
    private static final List<Integer> ALL_INDEXED_PAGES = List.of(10, 20, 30, 40);

    private TemplateIndexTestDatabase database;

    @BeforeEach
    void setUp() throws SQLException
    {
        database = TemplateIndexTestDatabase.open("tplpageindex");
        // page 40: infobox_city; page 30: stub; page 20: cite_web;
        // page 10: infobox_city, infobox_person and cite_web.
        // Page 50 has no templates and is therefore missing in the index.
        database.createIndex(GeneratorConstants.TABLE_TPLID_PAGEID, "pageId",
                new int[] { 1, 40 }, new int[] { 4, 30 }, new int[] { 3, 20 },
                new int[] { 3, 10 }, new int[] { 2, 10 }, new int[] { 1, 10 });
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        database.close();
    }

    static Stream<Arguments> queries()
    {
        return Stream.of(
                arguments("whitelist names, each page once and in order",
                        List.of("infobox_city", "cite_web"), false, true, List.of(10, 20, 40)),
                // Page 10 contains two templates starting with "infobox"
                arguments("whitelist fragment matching two templates of a page",
                        List.of("Infobox"), true, true, List.of(10, 40)),
                arguments("whitelist fragments", List.of("infobox", "cite"), true, true,
                        List.of(10, 20, 40)),
                arguments("whitelist names are normalized", List.of(" Infobox Person "), false,
                        true, List.of(10)),
                arguments("whitelist names are not matched as prefix", List.of("infobox"), false,
                        true, List.of()),
                // Page 10 contains infobox_city next to other templates and must not be returned
                arguments("blacklist name", List.of("infobox_city"), false, false,
                        List.of(20, 30)),
                arguments("blacklist names", List.of("cite_web", "stub"), false, false,
                        List.of(40)),
                arguments("blacklist fragment", List.of("infobox"), true, false,
                        List.of(20, 30)),
                arguments("blacklist fragments are normalized", List.of("Cite"), true, false,
                        List.of(30, 40)),
                // Page 50 has no templates and is not part of the index
                arguments("blacklist of unknown template", List.of("unknown"), false, false,
                        ALL_INDEXED_PAGES),
                arguments("empty blacklist of names", List.of(), false, false,
                        ALL_INDEXED_PAGES),
                arguments("empty blacklist of fragments", List.of(), true, false,
                        ALL_INDEXED_PAGES));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("queries")
    void testIdAndCountQuery(String description, List<String> names, boolean prefix,
            boolean whitelist, List<Integer> expectedPageIds)
        throws SQLException
    {
        assertEquals(expectedPageIds, database.queryInts(
                WikipediaTemplateInfo.buildIndexedPageIdQuery(names.size(), prefix, whitelist),
                names, prefix));
        assertEquals(List.of(expectedPageIds.size()), database.queryInts(
                WikipediaTemplateInfo.buildIndexedPageCountQuery(names.size(), prefix, whitelist),
                names, prefix));
    }

    @Test
    void testEmptyWhitelistIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedPageIdQuery(0, false, true));
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedPageCountQuery(0, true, true));
    }

    @Test
    void testNullTemplateListIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.checkTemplateNames(null));
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.checkTemplateNames(Arrays.asList("stub", null)));
    }
}
