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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.parser.Template;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the name comparison of the {@code revisionContainsTemplate*} methods of
 * {@link WikipediaTemplateInfo} against the template names stored in an in-memory HSQLDB
 * database and against names parsed from a revision text.
 */
class WikipediaTemplateInfoRevisionContainsTemplateTest
{
    private static final int PERSON_REVISION = 201;

    private static final int CITY_REVISION = 100;

    private TemplateIndexTestDatabase database;

    @BeforeEach
    void setUp() throws SQLException
    {
        database = TemplateIndexTestDatabase.open("revisionContainsTemplate");
        database.createIndex(GeneratorConstants.TABLE_TPLID_REVISIONID, "revisionId",
                // infobox_city in revision 100, infobox_person and cite_web in revision 201
                new int[] { 1, CITY_REVISION }, new int[] { 2, PERSON_REVISION },
                new int[] { 3, PERSON_REVISION });
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        database.close();
    }

    /**
     * Loads the template names of a revision via
     * {@link WikipediaTemplateInfo#REVISION_TEMPLATE_NAMES_QUERY}, as
     * {@link WikipediaTemplateInfo#getTemplateNamesFromRevision(int)} does.
     */
    private List<String> storedNames(int revisionId) throws SQLException
    {
        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = database.connection()
                .prepareStatement(WikipediaTemplateInfo.REVISION_TEMPLATE_NAMES_QUERY)) {
            statement.setInt(1, revisionId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    names.add(result.getString(1).toLowerCase());
                }
            }
        }
        return names;
    }

    private boolean containsName(int revisionId, String name) throws SQLException
    {
        return WikipediaTemplateInfo.containsTemplateName(storedNames(revisionId), List.of(name));
    }

    private boolean containsFragment(int revisionId, String fragment) throws SQLException
    {
        return WikipediaTemplateInfo.containsTemplateFragment(storedNames(revisionId), fragment);
    }

    private boolean indexContains(int revisionId, String name, boolean prefix) throws SQLException
    {
        return database.queryInts(
                WikipediaTemplateInfo.buildIndexedRevisionIdQuery(1, prefix, true),
                List.of(name), prefix).contains(revisionId);
    }

    @Test
    void testStoredNamesMatch() throws SQLException
    {
        assertTrue(containsName(PERSON_REVISION, "infobox_person"));
        assertTrue(containsName(PERSON_REVISION, "Cite_Web"));
        assertFalse(containsName(CITY_REVISION, "infobox_person"));
    }

    @Test
    void testNamesWithBlanksMatch() throws SQLException
    {
        assertTrue(containsName(PERSON_REVISION, "Infobox person"));
        assertTrue(containsName(PERSON_REVISION, " Infobox Person "));
        assertTrue(containsName(PERSON_REVISION, "CITE WEB"));
        assertTrue(WikipediaTemplateInfo.containsTemplateName(storedNames(PERSON_REVISION),
                List.of("stub", "Infobox person")));
    }

    @Test
    void testNamesDoNotMatchPrefixesOrOtherTemplates() throws SQLException
    {
        assertFalse(containsName(PERSON_REVISION, "Infobox"));
        assertFalse(containsName(PERSON_REVISION, "Infobox city"));
        assertFalse(WikipediaTemplateInfo.containsTemplateName(storedNames(PERSON_REVISION),
                List.of()));
    }

    @Test
    void testFragmentsWithBlanksMatch() throws SQLException
    {
        assertTrue(containsFragment(PERSON_REVISION, "Infobox pers"));
        assertTrue(containsFragment(PERSON_REVISION, " CITE W"));
        assertTrue(containsFragment(CITY_REVISION, "infobox "));
        assertFalse(containsFragment(CITY_REVISION, "Infobox pers"));
    }

    @Test
    void testNamesMatchLikeIndexLookup() throws SQLException
    {
        for (String name : new String[] { "Infobox person", " infobox_City ", "Cite web", "Infobox",
                "stub" }) {
            for (int revisionId : new int[] { CITY_REVISION, PERSON_REVISION }) {
                assertEquals(indexContains(revisionId, name, false), containsName(revisionId, name),
                        name + " in " + revisionId);
                assertEquals(indexContains(revisionId, name, true),
                        containsFragment(revisionId, name), name + "* in " + revisionId);
            }
        }
    }

    @Test
    void testParsedNamesMatch()
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory();
        factory.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        MediaWikiParser parser = factory.createParser();
        List<String> names = new ArrayList<>();
        for (Template template : parser
                .parseTemplatesOnly("{{Infobox person|name=Ada}} Text {{cite web|url=x}}")) {
            names.add(template.getName());
        }

        assertTrue(WikipediaTemplateInfo.containsTemplateName(names, List.of("Infobox person")));
        assertTrue(WikipediaTemplateInfo.containsTemplateName(names, List.of("Cite_Web")));
        assertTrue(WikipediaTemplateInfo.containsTemplateFragment(names, "infobox pers"));
        assertFalse(WikipediaTemplateInfo.containsTemplateName(names, List.of("Infobox")));
    }

    @Test
    void testNamesAreNormalizedLikeTheIndex()
    {
        assertEquals("infobox_person",
                WikipediaTemplateInfo.normalizeTemplateName(" Infobox Person "));
    }
}
