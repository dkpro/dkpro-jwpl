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

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the page id query of {@link WikipediaTemplateInfo} against an in-memory database that
 * holds the template index and the RevisionMachine tables it joins with.
 */
class WikipediaTemplateInfoQueryTest
{
    private TemplateIndexTestDatabase database;

    @BeforeEach
    void setUp() throws SQLException
    {
        database = TemplateIndexTestDatabase.open("tplinfo");
        database.createIndex(GeneratorConstants.TABLE_TPLID_REVISIONID, "revisionId",
                // infobox_city in both revisions of page 10
                new int[] { 1, 100 }, new int[] { 1, 101 },
                // infobox_person in page 20, cite_web in pages 20 and 30
                new int[] { 2, 201 }, new int[] { 3, 200 }, new int[] { 3, 300 },
                // stub in page 30 and in revision 999 which is missing in the revision tables
                new int[] { 4, 300 }, new int[] { 4, 999 });
        database.execute(
                "CREATE TABLE index_revisionID (RevisionID INTEGER PRIMARY KEY, "
                        + "RevisionPK INTEGER)",
                "CREATE TABLE revisions (PrimaryKey INTEGER PRIMARY KEY, "
                        + "RevisionID INTEGER, ArticleID INTEGER)",
                // page 10: revisions 100, 101; page 20: revisions 200, 201; page 30: revision 300
                "INSERT INTO revisions VALUES (1, 100, 10), (2, 101, 10), (3, 200, 20), "
                        + "(4, 201, 20), (5, 300, 30)",
                "INSERT INTO index_revisionID VALUES (100, 1), (101, 2), (200, 3), "
                        + "(201, 4), (300, 5)");
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        database.close();
    }

    private List<Integer> queryPageIds(List<String> names, boolean prefix) throws SQLException
    {
        return database.queryInts(WikipediaTemplateInfo.buildPageIdQuery(names.size(), prefix),
                names, prefix);
    }

    private static Set<Integer> set(List<Integer> ids)
    {
        return new TreeSet<>(ids);
    }

    @Test
    void testExactNamesReturnDistinctPageIds() throws SQLException
    {
        List<Integer> pageIds = queryPageIds(List.of("infobox_city"), false);
        assertEquals(List.of(10), pageIds);

        pageIds = queryPageIds(List.of("infobox_person", "cite_web"), false);
        assertEquals(2, pageIds.size());
        assertEquals(Set.of(20, 30), set(pageIds));
    }

    @Test
    void testExactNamesAreNormalized() throws SQLException
    {
        assertEquals(Set.of(20), set(queryPageIds(List.of(" Infobox Person "), false)));
    }

    @Test
    void testExactNamesDoNotMatchPrefixes() throws SQLException
    {
        assertEquals(List.of(), queryPageIds(List.of("infobox"), false));
    }

    @Test
    void testFragmentsMatchPrefixes() throws SQLException
    {
        List<Integer> pageIds = queryPageIds(List.of("Infobox"), true);
        assertEquals(2, pageIds.size());
        assertEquals(Set.of(10, 20), set(pageIds));

        assertEquals(Set.of(10, 20, 30), set(queryPageIds(List.of("infobox", "cite"), true)));
    }

    @Test
    void testRevisionsMissingInRevisionTablesAreIgnored() throws SQLException
    {
        assertEquals(List.of(30), queryPageIds(List.of("stub"), false));
    }

    @Test
    void testTemplateNameCondition()
    {
        assertEquals("(tpl.templateName = ?)",
                WikipediaTemplateInfo.buildTemplateNameCondition(1, false));
        assertEquals("(tpl.templateName LIKE ? OR tpl.templateName LIKE ?)",
                WikipediaTemplateInfo.buildTemplateNameCondition(2, true));
    }
}
