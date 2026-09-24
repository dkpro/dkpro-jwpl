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
 * Checks the queries behind the {@code get*ContainingTemplate*} and
 * {@code count*ContainingTemplate*} page methods of {@link WikipediaTemplateInfo} against an
 * in-memory database holding the page template index.
 */
class WikipediaTemplateInfoPageIndexQueryTest
{
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:tplpageindex", "sa", "");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " (templateId INTEGER NOT NULL PRIMARY KEY,"
                    + " templateName VARCHAR(255) NOT NULL)");
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_PAGEID
                    + " (templateId INTEGER NOT NULL, pageId INTEGER NOT NULL,"
                    + " UNIQUE (templateId, pageId))");

            st.execute("INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " VALUES (1, 'infobox_city'), (2, 'infobox_person'), (3, 'cite_web'),"
                    + " (4, 'stub')");

            // page 40: infobox_city; page 30: stub; page 20: cite_web;
            // page 10: infobox_city, infobox_person and cite_web.
            // Page 50 has no templates and is therefore missing in the index.
            st.execute("INSERT INTO " + GeneratorConstants.TABLE_TPLID_PAGEID
                    + " VALUES (1, 40), (4, 30), (3, 20), (3, 10), (2, 10), (1, 10)");
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

    private List<Integer> pageIds(List<String> names, boolean prefix, boolean whitelist)
        throws SQLException
    {
        List<Integer> pageIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                WikipediaTemplateInfo.buildIndexedPageIdQuery(names.size(), prefix, whitelist))) {
            WikipediaTemplateInfo.bindTemplateNames(statement, names, prefix);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    pageIds.add(result.getInt(1));
                }
            }
        }
        return pageIds;
    }

    private int count(List<String> names, boolean prefix, boolean whitelist) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement(WikipediaTemplateInfo
                .buildIndexedPageCountQuery(names.size(), prefix, whitelist))) {
            WikipediaTemplateInfo.bindTemplateNames(statement, names, prefix);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    @Test
    void testWhitelistNamesReturnEachPageOnceInOrder() throws SQLException
    {
        List<String> names = List.of("infobox_city", "cite_web");
        assertEquals(List.of(10, 20, 40), pageIds(names, false, true));
        assertEquals(3, count(names, false, true));
    }

    @Test
    void testWhitelistFragmentsReturnEachPageOnce() throws SQLException
    {
        // Page 10 contains two templates starting with "infobox"
        List<String> fragments = List.of("Infobox");
        assertEquals(List.of(10, 40), pageIds(fragments, true, true));
        assertEquals(2, count(fragments, true, true));

        fragments = List.of("infobox", "cite");
        assertEquals(List.of(10, 20, 40), pageIds(fragments, true, true));
        assertEquals(3, count(fragments, true, true));
    }

    @Test
    void testWhitelistNamesAreNormalizedAndNotMatchedAsPrefix() throws SQLException
    {
        assertEquals(List.of(10), pageIds(List.of(" Infobox Person "), false, true));
        assertEquals(List.of(), pageIds(List.of("infobox"), false, true));
        assertEquals(0, count(List.of("infobox"), false, true));
    }

    @Test
    void testBlacklistNamesExcludePagesContainingAnyOfTheTemplates() throws SQLException
    {
        // Page 10 contains infobox_city next to other templates and must not be returned
        List<String> names = List.of("infobox_city");
        assertEquals(List.of(20, 30), pageIds(names, false, false));
        assertEquals(2, count(names, false, false));

        names = List.of("cite_web", "stub");
        assertEquals(List.of(40), pageIds(names, false, false));
        assertEquals(1, count(names, false, false));
    }

    @Test
    void testBlacklistFragmentsExcludePagesContainingAnyOfTheTemplates() throws SQLException
    {
        List<String> fragments = List.of("infobox");
        assertEquals(List.of(20, 30), pageIds(fragments, true, false));
        assertEquals(2, count(fragments, true, false));

        fragments = List.of("Cite");
        assertEquals(List.of(30, 40), pageIds(fragments, true, false));
        assertEquals(2, count(fragments, true, false));
    }

    @Test
    void testBlacklistOfUnknownTemplateReturnsEveryIndexedPageOnce() throws SQLException
    {
        // Page 50 has no templates and is not part of the index
        assertEquals(List.of(10, 20, 30, 40), pageIds(List.of("unknown"), false, false));
        assertEquals(4, count(List.of("unknown"), false, false));
    }

    @Test
    void testEmptyBlacklistReturnsEveryIndexedPageOnce() throws SQLException
    {
        assertEquals(List.of(10, 20, 30, 40), pageIds(List.of(), false, false));
        assertEquals(4, count(List.of(), true, false));
    }

    @Test
    void testEmptyWhitelistIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedPageIdQuery(0, false, true));
        assertThrows(IllegalArgumentException.class,
                () -> WikipediaTemplateInfo.buildIndexedPageCountQuery(0, true, true));
    }
}
