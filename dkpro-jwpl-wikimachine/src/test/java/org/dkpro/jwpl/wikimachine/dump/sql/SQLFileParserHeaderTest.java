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
import java.io.StreamTokenizer;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@code CREATE TABLE} header capturing of {@link SQLFileParser} - the mechanism that
 * lets the link parsers probe the layout of the dump at hand instead of assuming a fixed column
 * order (see issue #491).
 */
class SQLFileParserHeaderTest
{

    /** Minimal concrete subclass exposing the protected header API to the test. */
    private static final class TestParser
        extends SQLFileParser
    {
        TestParser(String sql) throws IOException
        {
            init(SqlFixtures.stream(sql));
        }

        @Override
        boolean next()
        {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void capturesNormalisedCategorylinksHeader() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_NORMALISED)) {
            assertTrue(parser.hasHeader());
            assertEquals("categorylinks", parser.getTableName());
            assertEquals(Map.of("cl_from", 0, "cl_sortkey", 1, "cl_timestamp", 2,
                    "cl_sortkey_prefix", 3, "cl_type", 4, "cl_collation_id", 5, "cl_target_id", 6),
                    parser.getColumnIndex());
            assertEquals(List.of("cl_from", "cl_sortkey", "cl_timestamp", "cl_sortkey_prefix",
                    "cl_type", "cl_collation_id", "cl_target_id"),
                    List.copyOf(parser.getColumnIndex().keySet()));
            assertEquals(Map.of("cl_from", "int", "cl_sortkey", "varbinary", "cl_timestamp",
                    "timestamp", "cl_sortkey_prefix", "varbinary", "cl_type", "enum",
                    "cl_collation_id", "smallint", "cl_target_id", "bigint"),
                    parser.getColumnTypes());
        }
    }

    @Test
    void capturesNormalisedPagelinksHeader() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.PAGELINKS_NORMALISED)) {
            assertTrue(parser.hasHeader());
            assertEquals("pagelinks", parser.getTableName());
            assertEquals(Map.of("pl_from", 0, "pl_from_namespace", 1, "pl_target_id", 2),
                    parser.getColumnIndex());
        }
    }

    @Test
    void capturesLegacyCategorylinksHeader() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_LEGACY)) {
            assertTrue(parser.hasHeader());
            assertEquals(Map.of("cl_from", 0, "cl_to", 1, "cl_sortkey", 2, "cl_timestamp", 3,
                    "cl_sortkey_prefix", 4, "cl_collation", 5, "cl_type", 6),
                    parser.getColumnIndex());
        }
    }

    @Test
    void resumesOnTheFirstTupleAfterTheHeader() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_NORMALISED)) {
            assertEquals('(', parser.st.nextToken());
            assertEquals(StreamTokenizer.TT_NUMBER, parser.st.nextToken());
            assertEquals(1341.0, parser.st.nval);
        }
    }

    @Test
    void degradesToNoHeaderForInsertOnlyInput() throws Exception
    {
        final String sql = "INSERT INTO `categorylinks` VALUES (1341,'Articles','A','x');\n";
        try (TestParser parser = new TestParser(sql)) {
            assertFalse(parser.hasHeader());
            assertTrue(parser.getColumnIndex().isEmpty());
            // the tokenizer still resumes on the '(' that opens the first tuple
            assertEquals('(', parser.st.nextToken());
            assertEquals(StreamTokenizer.TT_NUMBER, parser.st.nextToken());
            assertEquals(1341.0, parser.st.nval);
        }
    }

    @Test
    void keepsTheColumnMapAcrossSeveralInsertStatements() throws Exception
    {
        final String sql = """
                CREATE TABLE `t` (
                  `a` int(8) NOT NULL,
                  `b` varbinary(255) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `t` VALUES (1,'x');
                INSERT INTO `t` VALUES (2,'y');
                """;
        try (TestParser parser = new TestParser(sql)) {
            assertEquals(Map.of("a", 0, "b", 1), parser.getColumnIndex());
            parser.skipStatements();
            assertEquals(Map.of("a", 0, "b", 1), parser.getColumnIndex());
            assertTrue(parser.hasHeader());
        }
    }

    @Test
    void replacesTheColumnMapOnASecondCreateTable() throws Exception
    {
        final String sql = """
                CREATE TABLE `first` (
                  `a` int(8) NOT NULL
                ) ENGINE=InnoDB;
                CREATE TABLE `second` (
                  `x` int(8) NOT NULL,
                  `y` int(8) NOT NULL
                ) ENGINE=InnoDB;
                INSERT INTO `second` VALUES (1,2);
                """;
        try (TestParser parser = new TestParser(sql)) {
            assertEquals("second", parser.getTableName());
            assertEquals(Map.of("x", 0, "y", 1), parser.getColumnIndex());
        }
    }

    @Test
    void acceptsUnquotedIdentifiersAndIfNotExists() throws Exception
    {
        final String sql = """
                CREATE TABLE IF NOT EXISTS categorylinks (
                  cl_from int(8) unsigned NOT NULL,
                  cl_to varbinary(255) NOT NULL,
                  PRIMARY KEY (cl_from,cl_to)
                ) ENGINE=InnoDB;
                INSERT INTO categorylinks VALUES (1,'A');
                """;
        try (TestParser parser = new TestParser(sql)) {
            assertEquals("categorylinks", parser.getTableName());
            assertEquals(Map.of("cl_from", 0, "cl_to", 1), parser.getColumnIndex());
        }
    }

    @Test
    void dropTableDoesNotEnterHeaderMode() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_LEGACY)) {
            assertEquals("categorylinks", parser.getTableName());
            assertEquals(7, parser.getColumnIndex().size());
        }
    }

    @Test
    void indexOfAndHasColumnsAreCaseInsensitive() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_NORMALISED)) {
            assertEquals(6, parser.indexOf("CL_TARGET_ID"));
            assertEquals(-1, parser.indexOf("cl_to"));
            assertTrue(parser.hasColumns("cl_from", "cl_type"));
            assertFalse(parser.hasColumns("cl_from", "cl_to"));
        }
    }

    @Test
    void requireColumnReportsTheMissingColumnAndTheColumnsFound() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.CATEGORYLINKS_NORMALISED)) {
            final IOException e = assertThrows(IOException.class,
                    () -> parser.requireColumn("categorylinks", "cl_to"));
            final String message = e.getMessage();
            assertTrue(message.contains("cl_to"), message);
            assertTrue(message.contains("categorylinks"), message);
            assertTrue(message.contains("cl_target_id bigint"), message);
            assertTrue(message.contains("line "), message);
        }
    }

    @Test
    void describeColumnsRendersNamesAndTypesInDeclarationOrder() throws Exception
    {
        try (TestParser parser = new TestParser(SqlFixtures.PAGELINKS_NORMALISED)) {
            assertEquals("[pl_from int, pl_from_namespace int, pl_target_id bigint]",
                    parser.describeColumns());
        }
    }

}
