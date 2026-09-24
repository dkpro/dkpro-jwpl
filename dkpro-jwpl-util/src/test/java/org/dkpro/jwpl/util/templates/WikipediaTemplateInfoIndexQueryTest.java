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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks the queries behind the {@code get*ContainingTemplate*},
 * {@code count*ContainingTemplate*} and {@code get*RevisionIds*ContainingTemplate*} methods of
 * {@link WikipediaTemplateInfo} against an in-memory database holding the page or the revision
 * template index. Both indexes hold the same rows, so every query must select the same ids from
 * either of them.
 */
class WikipediaTemplateInfoIndexQueryTest
{
    private static final List<Integer> ALL_INDEXED_IDS = List.of(10, 20, 30, 40);

    /** The template index tables and the queries {@link WikipediaTemplateInfo} builds for them. */
    enum Index
    {
        PAGES(GeneratorConstants.TABLE_TPLID_PAGEID, "pageId") {
            @Override
            String idQuery(int nameCount, boolean prefix, boolean whitelist)
            {
                return WikipediaTemplateInfo.buildIndexedPageIdQuery(nameCount, prefix,
                        whitelist);
            }

            @Override
            String countQuery(int nameCount, boolean prefix, boolean whitelist)
            {
                return WikipediaTemplateInfo.buildIndexedPageCountQuery(nameCount, prefix,
                        whitelist);
            }
        },

        REVISIONS(GeneratorConstants.TABLE_TPLID_REVISIONID, "revisionId") {
            @Override
            String idQuery(int nameCount, boolean prefix, boolean whitelist)
            {
                return WikipediaTemplateInfo.buildIndexedRevisionIdQuery(nameCount, prefix,
                        whitelist);
            }
        };

        private final String table;
        private final String idColumn;

        Index(String table, String idColumn)
        {
            this.table = table;
            this.idColumn = idColumn;
        }

        abstract String idQuery(int nameCount, boolean prefix, boolean whitelist);

        /** Returns the count query, or {@code null} as there is none for revisions. */
        String countQuery(int nameCount, boolean prefix, boolean whitelist)
        {
            return null;
        }
    }

    private TemplateIndexTestDatabase database;

    @BeforeEach
    void setUp() throws SQLException
    {
        database = TemplateIndexTestDatabase.open("tplindex");
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        database.close();
    }

    private void createIndex(Index index) throws SQLException
    {
        // 40: infobox_city; 30: stub; 20: cite_web;
        // 10: infobox_city, infobox_person and cite_web.
        // 50 has no templates and is therefore missing in the index.
        database.createIndex(index.table, index.idColumn,
                new int[] { 1, 40 }, new int[] { 4, 30 }, new int[] { 3, 20 },
                new int[] { 3, 10 }, new int[] { 2, 10 }, new int[] { 1, 10 });
    }

    static Stream<Arguments> queries()
    {
        return Stream.of(Index.values()).flatMap(index -> Stream.of(
                // 10 contains both templates
                arguments(index, "whitelist names matching two templates of one id",
                        List.of("infobox_city", "infobox_person"), false, true, List.of(10, 40)),
                arguments(index, "whitelist names, each id once and in order",
                        List.of("infobox_city", "cite_web"), false, true, List.of(10, 20, 40)),
                // 10 contains two templates starting with "infobox"
                arguments(index, "whitelist fragment matching two templates of one id",
                        List.of("Infobox"), true, true, List.of(10, 40)),
                arguments(index, "whitelist fragments", List.of("infobox", "cite"), true, true,
                        List.of(10, 20, 40)),
                arguments(index, "whitelist names are normalized", List.of(" Infobox Person "),
                        false, true, List.of(10)),
                arguments(index, "whitelist names are not matched as prefix",
                        List.of("infobox"), false, true, List.of()),
                // 10 contains infobox_city next to other templates and must not be selected
                arguments(index, "blacklist name", List.of("infobox_city"), false, false,
                        List.of(20, 30)),
                arguments(index, "blacklist names", List.of("cite_web", "stub"), false, false,
                        List.of(40)),
                arguments(index, "blacklist fragment", List.of("infobox"), true, false,
                        List.of(20, 30)),
                arguments(index, "blacklist fragments are normalized", List.of("Cite"), true,
                        false, List.of(30, 40)),
                // 50 has no templates and is not part of the index
                arguments(index, "blacklist of unknown template", List.of("unknown"), false,
                        false, ALL_INDEXED_IDS),
                arguments(index, "empty blacklist of names", List.of(), false, false,
                        ALL_INDEXED_IDS),
                arguments(index, "empty blacklist of fragments", List.of(), true, false,
                        ALL_INDEXED_IDS)));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("queries")
    void testIdAndCountQuery(Index index, String description, List<String> names,
            boolean prefix, boolean whitelist, List<Integer> expectedIds)
        throws SQLException
    {
        createIndex(index);
        assertEquals(expectedIds,
                database.queryInts(index.idQuery(names.size(), prefix, whitelist), names, prefix));

        String countQuery = index.countQuery(names.size(), prefix, whitelist);
        if (countQuery != null) {
            assertEquals(List.of(expectedIds.size()),
                    database.queryInts(countQuery, names, prefix));
        }
    }

    @ParameterizedTest
    @EnumSource(Index.class)
    void testEmptyWhitelistIsRejected(Index index)
    {
        assertThrows(IllegalArgumentException.class, () -> index.idQuery(0, false, true));
        assertThrows(IllegalArgumentException.class, () -> index.idQuery(0, true, true));
    }

    @Test
    void testEmptyWhitelistCountIsRejected()
    {
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
