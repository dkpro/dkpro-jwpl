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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link LinktargetParser} and {@link FastUtilLinkTargetResolver}.
 */
class LinktargetParserTest
{

    @Test
    void readsTheLinktargetTable() throws Exception
    {
        try (LinktargetParser parser = new LinktargetParser(
                SqlFixtures.stream(SqlFixtures.LINKTARGET))) {
            assertTrue(parser.next());
            assertEquals(208, parser.getLtId());
            assertEquals(14, parser.getLtNamespace());
            assertEquals("User_aa-0", parser.getLtTitle());

            assertTrue(parser.next());
            assertEquals(212, parser.getLtId());

            assertTrue(parser.next());
            assertEquals(1779, parser.getLtId());
            assertEquals(10, parser.getLtNamespace());

            assertTrue(parser.next());
            // lt_id is a bigint(20): values beyond Integer.MAX_VALUE must survive
            assertEquals(4294967297L, parser.getLtId());
            assertEquals(0, parser.getLtNamespace());
            assertEquals("Big_Target", parser.getLtTitle());

            assertFalse(parser.next());
        }
    }

    @Test
    void readsAHeaderlessLinktargetDump() throws Exception
    {
        final String sql = "INSERT INTO `linktarget` VALUES (1,0,'Main_Page'),(2,14,'Articles');\n";
        try (LinktargetParser parser = new LinktargetParser(SqlFixtures.stream(sql))) {
            assertTrue(parser.next());
            assertEquals(1, parser.getLtId());
            assertEquals(0, parser.getLtNamespace());
            assertEquals("Main_Page", parser.getLtTitle());
            assertTrue(parser.next());
            assertEquals(2, parser.getLtId());
            assertFalse(parser.next());
        }
    }

    @Test
    void rejectsAnUnknownSchema()
    {
        final String sql = """
                CREATE TABLE `linktarget` (
                  `lt_id` bigint(20) unsigned NOT NULL,
                  `lt_page` int(11) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `linktarget` VALUES (1,0);
                """;
        final IOException e = assertThrows(IOException.class,
                () -> new LinktargetParser(SqlFixtures.stream(sql)));
        final String message = e.getMessage();
        assertTrue(message.contains("lt_id, lt_namespace, lt_title"), message);
        assertTrue(message.contains("lt_page int"), message);
    }

    @Test
    void loadHonoursTheNamespacePredicate() throws Exception
    {
        final FastUtilLinkTargetResolver resolver = FastUtilLinkTargetResolver.load(
                new LinktargetParser(SqlFixtures.stream(SqlFixtures.LINKTARGET)),
                FastUtilLinkTargetResolver.ARTICLE_TALK_AND_CATEGORY);

        assertEquals(3, resolver.size());
        assertEquals("User_aa-0", resolver.getTitle(208));
        assertEquals(14, resolver.getNamespace(208));
        assertEquals("Big_Target", resolver.getTitle(4294967297L));
        assertEquals(0, resolver.getNamespace(4294967297L));

        // the NS_TEMPLATE row was filtered out
        assertNull(resolver.getTitle(1779));
        assertEquals(LinkTargetResolver.NAMESPACE_UNKNOWN, resolver.getNamespace(1779));
        // ... and so is an id that never existed
        assertNull(resolver.getTitle(4711));
        assertEquals(LinkTargetResolver.NAMESPACE_UNKNOWN, resolver.getNamespace(4711));
    }

    @Test
    void loadKeepsEverythingWhenThePredicateAccepts() throws Exception
    {
        final FastUtilLinkTargetResolver resolver = FastUtilLinkTargetResolver.load(
                new LinktargetParser(SqlFixtures.stream(SqlFixtures.LINKTARGET)), ns -> true);
        assertEquals(4, resolver.size());
        assertEquals(10, resolver.getNamespace(1779));
    }
}
