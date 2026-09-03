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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link PagelinksParser} on the pre July 2014 layout, the post July 2014 layout, the
 * normalised layout of MediaWiki 1.43+ and the headerless fallback (see issue #491).
 */
class PagelinksParserTest
{

    private static StubLinkTargetResolver resolver()
    {
        return new StubLinkTargetResolver().add(208, 14, "User_aa-0").add(212, 0, "Main_Page");
    }

    private static void assertLegacyRows(PagelinksParser parser) throws IOException
    {
        assertTrue(parser.next());
        assertEquals(1341, parser.getPlFrom());
        assertEquals(0, parser.getPlNamespace());
        assertEquals("Main_Page", parser.getPlTo());

        assertTrue(parser.next());
        assertEquals(1343, parser.getPlFrom());
        assertEquals(14, parser.getPlNamespace());
        assertEquals("User_aa", parser.getPlTo());

        assertTrue(parser.next());
        assertEquals(1506, parser.getPlFrom());
        assertEquals(0, parser.getPlNamespace());
        assertEquals("Sj", parser.getPlTo());

        assertFalse(parser.next());
    }

    @Test
    void readsThePre2014Layout() throws Exception
    {
        try (PagelinksParser parser = new PagelinksParser(
                SqlFixtures.stream(SqlFixtures.PAGELINKS_PRE_2014))) {
            assertLegacyRows(parser);
        }
    }

    @Test
    void readsThePost2014LayoutIdentically() throws Exception
    {
        try (PagelinksParser parser = new PagelinksParser(
                SqlFixtures.stream(SqlFixtures.PAGELINKS_POST_2014))) {
            assertLegacyRows(parser);
        }
    }

    @Test
    void staysInSyncOnAnEmptyPlTo() throws Exception
    {
        // Regression for the removed 'st.toString().charAt(7)' heuristic: an empty quoted string
        // stringifies to "Token[], line 1", whose 8th character is a comma, which used to make
        // the parser skip a column and desynchronise the rest of the INSERT statement.
        final String sql = """
                CREATE TABLE `pagelinks` (
                  `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
                  `pl_namespace` int(11) NOT NULL DEFAULT 0,
                  `pl_to` varbinary(255) NOT NULL DEFAULT ''
                ) ENGINE=InnoDB;
                INSERT INTO `pagelinks` VALUES (1,0,''),(2,0,'Main_Page'),(3,14,'User_aa');
                """;
        try (PagelinksParser parser = new PagelinksParser(SqlFixtures.stream(sql))) {
            assertTrue(parser.next());
            assertEquals(1, parser.getPlFrom());
            assertEquals("", parser.getPlTo());

            assertTrue(parser.next());
            assertEquals(2, parser.getPlFrom());
            assertEquals("Main_Page", parser.getPlTo());

            assertTrue(parser.next());
            assertEquals(3, parser.getPlFrom());
            assertEquals(14, parser.getPlNamespace());
            assertEquals("User_aa", parser.getPlTo());

            assertFalse(parser.next());
        }
    }

    @Test
    void resolvesTargetsOfTheNormalisedLayout() throws Exception
    {
        try (PagelinksParser parser = new PagelinksParser(
                SqlFixtures.stream(SqlFixtures.PAGELINKS_NORMALISED), resolver())) {
            assertTrue(parser.next());
            assertEquals(1426, parser.getPlFrom());
            assertEquals("User_aa-0", parser.getPlTo());
            // the TARGET namespace, not the source namespace pl_from_namespace
            assertEquals(14, parser.getPlNamespace());
            assertEquals(208, parser.getPlTargetId());

            assertTrue(parser.next());
            assertEquals(1271, parser.getPlFrom());
            assertEquals("Main_Page", parser.getPlTo());
            assertEquals(0, parser.getPlNamespace());

            // the row referencing lt_id 999 is unresolvable and therefore skipped
            assertFalse(parser.next());
            assertEquals(3, parser.getRowCount());
            assertEquals(2, parser.getResolvedCount());
            assertEquals(1, parser.getUnresolvedCount());
            parser.checkPostConditions();
        }
    }

    @Test
    void fallsBackToPositionalReadingWithoutAHeader() throws Exception
    {
        // the fourth column of a post July 2014 dump is simply read and discarded
        final String sql = "INSERT INTO `pagelinks` VALUES (1341,0,'Main_Page',0),(1343,14,'User_aa',2);\n";
        try (PagelinksParser parser = new PagelinksParser(SqlFixtures.stream(sql))) {
            assertTrue(parser.next());
            assertEquals(1341, parser.getPlFrom());
            assertEquals(0, parser.getPlNamespace());
            assertEquals("Main_Page", parser.getPlTo());
            assertTrue(parser.next());
            assertEquals(1343, parser.getPlFrom());
            assertEquals(14, parser.getPlNamespace());
            assertEquals("User_aa", parser.getPlTo());
            assertFalse(parser.next());
        }
    }

    @Test
    void rejectsAnUnknownSchema()
    {
        final String sql = """
                CREATE TABLE `pagelinks` (
                  `pl_from` int(8) unsigned NOT NULL,
                  `pl_from_namespace` int(11) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `pagelinks` VALUES (1,0);
                """;
        final IOException e = assertThrows(IOException.class,
                () -> new PagelinksParser(SqlFixtures.stream(sql)));
        final String message = e.getMessage();
        assertTrue(message.contains("pagelinks"), message);
        assertTrue(message.contains("pl_target_id"), message);
        assertTrue(message.contains("pl_from int, pl_from_namespace int"), message);
    }

    @Test
    void rejectsANormalisedDumpWithoutALinkTargetResolver()
    {
        final IOException e = assertThrows(IOException.class,
                () -> new PagelinksParser(SqlFixtures.stream(SqlFixtures.PAGELINKS_NORMALISED)));
        final String message = e.getMessage();
        assertTrue(message.contains("*-linktarget.sql.gz"), message);
        assertTrue(message.contains("linkTargetFile"), message);
    }

    @Test
    void failsLoudlyWhenNothingResolvesOnANormalisedDump() throws Exception
    {
        try (PagelinksParser parser = new PagelinksParser(
                SqlFixtures.stream(SqlFixtures.PAGELINKS_NORMALISED),
                new StubLinkTargetResolver())) {
            assertFalse(parser.next());
            final IOException e = assertThrows(IOException.class, parser::checkPostConditions);
            assertTrue(e.getMessage().contains("issue #491"), e.getMessage());
        }
    }
}
