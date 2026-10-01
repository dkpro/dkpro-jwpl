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

import org.dkpro.jwpl.api.util.StringUtils;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the template id lookup of {@link WikipediaTemplateInfo#checkTemplateId(String)} against
 * an in-memory HSQLDB database, whose {@code templateName} column is case-sensitive.
 */
class WikipediaTemplateInfoCheckTemplateIdTest
{
    private TemplateIndexTestDatabase database;

    @BeforeEach
    void setUp() throws SQLException
    {
        database = TemplateIndexTestDatabase.open("checkTemplateId");
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        database.close();
    }

    @Test
    void testStoredNamesMatch() throws SQLException
    {
        assertEquals(1, database.queryTemplateId("infobox_city"));
        assertEquals(4, database.queryTemplateId("stub"));
    }

    @Test
    void testMixedCaseNamesMatch() throws SQLException
    {
        assertEquals(2, database.queryTemplateId("Infobox person"));
        assertEquals(3, database.queryTemplateId(" CITE_Web "));
        assertEquals(4, database.queryTemplateId("STUB"));
    }

    @Test
    void testMixedCaseNamesMatchLikeTemplateIds() throws Exception
    {
        WikipediaTemplateInfo.TemplateIds ids = WikipediaTemplateInfo
                .loadTemplateIds(database.connection());
        for (String name : new String[] { "Infobox person", "Cite_Web", "STUB", "infobox_city" }) {
            assertEquals(ids.getTemplateId(name), database.queryTemplateId(name), name);
        }
    }

    @Test
    void testEscapedMixedCaseNamesMatch() throws SQLException
    {
        database.execute("INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                + " VALUES (5, 'o''neil')");

        assertEquals(5, database.queryTemplateId(StringUtils.sqlEscape("O'Neil")));
    }

    @Test
    void testUnknownNamesReturnMinusOne() throws SQLException
    {
        assertEquals(-1, database.queryTemplateId("Infobox"));
    }

    @Test
    void testNamesAreLowerCasedAfterUnescaping()
    {
        assertEquals("sub\u001achar",
                WikipediaTemplateInfo.toStoredTemplateName(StringUtils.sqlEscape("Sub\u001aChar")));
        assertEquals("infobox_person",
                WikipediaTemplateInfo.toStoredTemplateName(" Infobox Person "));
    }
}
