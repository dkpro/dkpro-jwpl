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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the query behind the {@code get*RevisionIds*ContainingTemplate*} methods of
 * {@link WikipediaTemplateInfo} against an in-memory database holding the revision template
 * index.
 */
class WikipediaTemplateInfoRevisionIndexQueryTest
{
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:tplrevisionindex", "sa", "");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " (templateId INTEGER NOT NULL PRIMARY KEY,"
                    + " templateName VARCHAR(255) NOT NULL)");
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " (templateId INTEGER NOT NULL, revisionId INTEGER NOT NULL,"
                    + " UNIQUE (templateId, revisionId))");

            st.execute("INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " VALUES (1, 'infobox_city'), (2, 'infobox_person'), (3, 'cite_web'),"
                    + " (4, 'stub')");

            // revision 400: infobox_city; revision 300: stub; revision 200: cite_web;
            // revision 100: infobox_city, infobox_person and cite_web.
            // Revision 500 has no templates and is therefore missing in the index.
            st.execute("INSERT INTO " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " VALUES (1, 400), (4, 300), (3, 200), (3, 100), (2, 100), (1, 100)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        try (Statement st = connection.createStatement()) {
            st.execute("SHUTDOWN");
        }
        connection.close();
    }

    private List<Integer> revisionIds(List<String> names, boolean prefix, boolean whitelist)
        throws SQLException
    {
        List<Integer> revisionIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(WikipediaTemplateInfo
                .buildIndexedRevisionIdQuery(names.size(), prefix, whitelist))) {
            WikipediaTemplateInfo.bindTemplateNames(statement, names, prefix);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    revisionIds.add(result.getInt(1));
                }
            }
        }
        return revisionIds;
    }

    @Test
    void testWhitelistNamesReturnEachRevisionOnceInOrder() throws SQLException
    {
        // Revision 100 contains both templates
        assertEquals(List.of(100, 400),
                revisionIds(List.of("infobox_city", "infobox_person"), false, true));
        assertEquals(List.of(100, 200, 400),
                revisionIds(List.of("infobox_city", "cite_web"), false, true));
    }

    @Test
    void testWhitelistFragmentsReturnEachRevisionOnce() throws SQLException
    {
        // Revision 100 contains two templates starting with "infobox"
        assertEquals(List.of(100, 400), revisionIds(List.of("Infobox"), true, true));
        assertEquals(List.of(100, 200, 400), revisionIds(List.of("infobox", "cite"), true, true));
    }

    @Test
    void testWhitelistNamesAreNormalizedAndNotMatchedAsPrefix() throws SQLException
    {
        assertEquals(List.of(100), revisionIds(List.of(" Infobox Person "), false, true));
        assertEquals(List.of(), revisionIds(List.of("infobox"), false, true));
    }

    @Test
    void testBlacklistNamesExcludeRevisionsContainingAnyOfTheTemplates() throws SQLException
    {
        // Revision 100 contains infobox_city next to other templates and must not be returned
        assertEquals(List.of(200, 300), revisionIds(List.of("infobox_city"), false, false));
        assertEquals(List.of(400), revisionIds(List.of("cite_web", "stub"), false, false));
    }

    @Test
    void testBlacklistFragmentsExcludeRevisionsContainingAnyOfTheTemplates() throws SQLException
    {
        assertEquals(List.of(200, 300), revisionIds(List.of("infobox"), true, false));
        assertEquals(List.of(300, 400), revisionIds(List.of("Cite"), true, false));
    }

    @Test
    void testBlacklistOfUnknownTemplateReturnsEveryIndexedRevisionOnce() throws SQLException
    {
        // Revision 500 has no templates and is not part of the index
        assertEquals(List.of(100, 200, 300, 400), revisionIds(List.of("unknown"), false, false));
    }

    @Test
    void testEmptyBlacklistReturnsEveryIndexedRevisionOnce() throws SQLException
    {
        assertEquals(List.of(100, 200, 300, 400), revisionIds(List.of(), false, false));
        assertEquals(List.of(100, 200, 300, 400), revisionIds(List.of(), true, false));
    }

    @Test
    void testEmptyWhitelistIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedRevisionIdQuery(0, false, true));
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedRevisionIdQuery(0, true, true));
    }
}
