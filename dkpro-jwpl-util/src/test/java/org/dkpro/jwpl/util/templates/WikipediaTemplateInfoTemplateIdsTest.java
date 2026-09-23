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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.dkpro.jwpl.api.util.StringUtils;
import org.dkpro.jwpl.util.templates.WikipediaTemplateInfo.TemplateIds;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests loading all template ids at once via
 * {@link WikipediaTemplateInfo#loadTemplateIds(Connection)}, which the template info generator
 * uses instead of one {@link WikipediaTemplateInfo#checkTemplateId(String)} query per template
 * name.
 */
class WikipediaTemplateInfoTemplateIdsTest
{

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:templateIds", "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " (templateId INTEGER NOT NULL PRIMARY KEY,"
                    + " templateName VARCHAR(255) NOT NULL)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME);
        }
        connection.close();
    }

    private void insert(int id, String name) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO "
                + GeneratorConstants.TABLE_TPLID_TPLNAME + " VALUES (?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    /** Escapes a template name found in a page the way the generator does. */
    private static String generatorName(String parsedName)
    {
        return StringUtils.sqlEscape(parsedName.toLowerCase());
    }

    @Test
    void matchesPlainAndMixedCaseNames() throws Exception
    {
        insert(1, "infobox");
        insert(2, "Citation_Needed");

        TemplateIds ids = WikipediaTemplateInfo.loadTemplateIds(connection);

        assertEquals(2, ids.size());
        assertEquals(1, ids.getTemplateId(generatorName("Infobox")));
        assertEquals(2, ids.getTemplateId(generatorName("citation_needed")));
    }

    @Test
    void matchesNamesContainingCharactersThatAreSqlEscaped() throws Exception
    {
        insert(1, "o'neil");
        insert(2, "back\\slash");

        TemplateIds ids = WikipediaTemplateInfo.loadTemplateIds(connection);

        assertEquals(1, ids.getTemplateId(generatorName("O'Neil")));
        assertEquals(2, ids.getTemplateId(generatorName("back\\slash")));
    }

    @Test
    void matchesNamesWithBlanksAndTrailingSpaces() throws Exception
    {
        insert(1, "cite_web");
        insert(2, "trailing   ");

        TemplateIds ids = WikipediaTemplateInfo.loadTemplateIds(connection);

        assertEquals(1, ids.getTemplateId(generatorName("cite web")));
        assertEquals(2, ids.getTemplateId(generatorName(" trailing")));
    }

    @Test
    void keepsTheSmallestIdOfDuplicateNames() throws Exception
    {
        insert(7, "Infobox");
        insert(3, "infobox");
        insert(5, "infobox ");

        TemplateIds ids = WikipediaTemplateInfo.loadTemplateIds(connection);

        assertEquals(1, ids.size());
        assertEquals(3, ids.getTemplateId("infobox"));
    }

    @Test
    void returnsNoIdsForAnEmptyTable() throws Exception
    {
        TemplateIds ids = WikipediaTemplateInfo.loadTemplateIds(connection);

        assertEquals(0, ids.size());
        assertEquals(-1, ids.getTemplateId("infobox"));
    }

    @Test
    void returnsMinusOneForUnknownNames() throws Exception
    {
        insert(1, "infobox");

        assertEquals(-1, WikipediaTemplateInfo.loadTemplateIds(connection)
                .getTemplateId(generatorName("unknown")));
    }
}
