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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CategorylinksParser} on the legacy layout, on the normalised layout of
 * MediaWiki 1.43+ and on the headerless fallback (see issue #491).
 */
class CategorylinksParserTest
{

    private static StubLinkTargetResolver resolver()
    {
        return new StubLinkTargetResolver().add(208, 14, "User_aa-0").add(212, 14, "User_es")
                .add(4294967297L, 0, "Big_Target");
    }

    @Test
    void readsTheLegacyLayout() throws Exception
    {
        try (CategorylinksParser parser = new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_LEGACY))) {
            assertTrue(parser.next());
            assertEquals(1341, parser.getClFrom());
            assertEquals("Wikipedians_by_language", parser.getClTo());
            assertEquals(CategoryLinkType.SUBCAT, parser.getClType());

            assertTrue(parser.next());
            assertEquals(1343, parser.getClFrom());
            assertEquals("User_aa", parser.getClTo());
            assertEquals(CategoryLinkType.PAGE, parser.getClType());

            assertTrue(parser.next());
            assertEquals(1506, parser.getClFrom());
            // escapes survive the round trip through the tokenizer unchanged
            assertEquals("D\\'Arcy_\\\\_Co", parser.getClTo());

            assertFalse(parser.next());
            assertEquals(3, parser.getRowCount());
            parser.checkPostConditions();
        }
    }

    @Test
    void reportsUnknownTypeWhenTheDumpHasNoClTypeColumn() throws Exception
    {
        try (CategorylinksParser parser = new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_LEGACY_NO_TYPE))) {
            assertTrue(parser.next());
            assertEquals(7, parser.getClFrom());
            assertEquals("Articles", parser.getClTo());
            assertEquals(CategoryLinkType.UNKNOWN, parser.getClType());
            assertFalse(parser.next());
        }
    }

    @Test
    void resolvesTargetsOfTheNormalisedLayout() throws Exception
    {
        try (CategorylinksParser parser = new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_NORMALISED), resolver())) {
            assertTrue(parser.next());
            assertEquals(1341, parser.getClFrom());
            assertEquals("User_aa-0", parser.getClTo());
            assertEquals(208, parser.getClTargetId());
            assertEquals(CategoryLinkType.PAGE, parser.getClType());

            assertTrue(parser.next());
            assertEquals(1343, parser.getClFrom());
            assertEquals("User_es", parser.getClTo());
            assertEquals(CategoryLinkType.SUBCAT, parser.getClType());

            // the row referencing lt_id 999 is skipped, the following one is returned instead
            assertTrue(parser.next());
            assertEquals(1507, parser.getClFrom());
            assertEquals("Big_Target", parser.getClTo());
            // guards against a (int) truncation of cl_target_id
            assertEquals(4294967297L, parser.getClTargetId());

            assertFalse(parser.next());
            assertEquals(4, parser.getRowCount());
            assertEquals(3, parser.getResolvedCount());
            assertEquals(1, parser.getUnresolvedCount());
            parser.checkPostConditions();
        }
    }

    @Test
    void fallsBackToPositionalReadingWithoutAHeader() throws Exception
    {
        final String sql = "INSERT INTO `categorylinks` VALUES "
                + "(1341,'Articles','A','2009-01-01 00:00:00'),(1343,'Wikipedia','W','x');\n";
        try (CategorylinksParser parser = new CategorylinksParser(SqlFixtures.stream(sql))) {
            assertTrue(parser.next());
            assertEquals(1341, parser.getClFrom());
            assertEquals("Articles", parser.getClTo());
            assertEquals(CategoryLinkType.UNKNOWN, parser.getClType());
            assertTrue(parser.next());
            assertEquals(1343, parser.getClFrom());
            assertEquals("Wikipedia", parser.getClTo());
            assertFalse(parser.next());
        }
    }

    @Test
    void terminatesOnATruncatedDump() throws Exception
    {
        final String sql = "INSERT INTO `categorylinks` VALUES (1341,'Articles','A','x'),(1343,";
        try (CategorylinksParser parser = new CategorylinksParser(SqlFixtures.stream(sql))) {
            assertTrue(parser.next());
            assertFalse(parser.next());
        }
    }

    @Test
    void rejectsAnUnknownSchema()
    {
        final String sql = """
                CREATE TABLE `categorylinks` (
                  `cl_from` int(8) unsigned NOT NULL,
                  `cl_sortkey` varbinary(230) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `categorylinks` VALUES (1,'A');
                """;
        final IOException e = assertThrows(IOException.class,
                () -> new CategorylinksParser(SqlFixtures.stream(sql)));
        final String message = e.getMessage();
        assertTrue(message.contains("categorylinks"), message);
        assertTrue(message.contains("cl_to"), message);
        assertTrue(message.contains("cl_target_id"), message);
        assertTrue(message.contains("cl_from int, cl_sortkey varbinary"), message);
    }

    @Test
    void rejectsANormalisedDumpWithoutALinkTargetResolver()
    {
        final IOException e = assertThrows(IOException.class, () -> new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_NORMALISED)));
        final String message = e.getMessage();
        assertTrue(message.contains("*-linktarget.sql.gz"), message);
        assertTrue(message.contains("linkTargetFile"), message);
    }

    @Test
    void rejectsAHeaderWithoutClFrom()
    {
        final String sql = """
                CREATE TABLE `categorylinks` (
                  `cl_source` int(8) unsigned NOT NULL,
                  `cl_to` varbinary(255) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `categorylinks` VALUES (1,'A');
                """;
        final IOException e = assertThrows(IOException.class,
                () -> new CategorylinksParser(SqlFixtures.stream(sql)));
        assertTrue(e.getMessage().contains("cl_from"), e.getMessage());
    }

    @Test
    void rejectsANonNumericSourceId() throws Exception
    {
        final String sql = """
                CREATE TABLE `categorylinks` (
                  `cl_from` int(8) unsigned NOT NULL,
                  `cl_to` varbinary(255) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `categorylinks` VALUES ('oops','A');
                """;
        try (CategorylinksParser parser = new CategorylinksParser(SqlFixtures.stream(sql))) {
            final IOException e = assertThrows(IOException.class, parser::next);
            assertTrue(e.getMessage().contains("expected a numeric literal"), e.getMessage());
        }
    }

    @Test
    void rejectsATruncatedTuple() throws Exception
    {
        final String sql = """
                CREATE TABLE `categorylinks` (
                  `cl_from` int(8) unsigned NOT NULL,
                  `cl_to` varbinary(255) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `categorylinks` VALUES (1);
                """;
        try (CategorylinksParser parser = new CategorylinksParser(SqlFixtures.stream(sql))) {
            final IOException e = assertThrows(IOException.class, parser::next);
            assertTrue(e.getMessage().contains("Truncated row"), e.getMessage());
            assertTrue(e.getMessage().contains("the tuple has 1 values"), e.getMessage());
        }
    }

    @Test
    void failsLoudlyWhenNothingResolvesOnANormalisedDump() throws Exception
    {
        final StubLinkTargetResolver empty = new StubLinkTargetResolver();
        try (CategorylinksParser parser = new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_NORMALISED), empty)) {
            assertFalse(parser.next());
            final IOException e = assertThrows(IOException.class, parser::checkPostConditions);
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("issue #491"), e.getMessage());
        }
    }

    @Test
    void doesNotFailWhenALegacyDumpResolvesNothing() throws Exception
    {
        try (CategorylinksParser parser = new CategorylinksParser(
                SqlFixtures.stream(SqlFixtures.CATEGORYLINKS_LEGACY))) {
            while (parser.next()) {
                // drain
            }
            parser.checkPostConditions();
        }
    }
}
