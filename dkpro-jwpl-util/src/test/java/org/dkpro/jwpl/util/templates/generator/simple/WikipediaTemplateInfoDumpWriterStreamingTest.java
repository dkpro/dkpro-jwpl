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
package org.dkpro.jwpl.util.templates.generator.simple;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Tests that a {@link WikipediaTemplateInfoDumpWriter} streams its dump without changing it and
 * splits the rows of a template over several statements to honour {@code maxAllowedPacket} (see
 * issue #560).
 */
class WikipediaTemplateInfoDumpWriterStreamingTest
{
    /** The default of {@code maxAllowedPackets} in {@link TemplateInfoGeneratorStarter}. */
    private static final long DEFAULT_MAX_ALLOWED_PACKET = 16 * 1024 * 1023;

    private static final Pattern TUPLE_ID = Pattern.compile(", (\\d+)\\)");

    /**
     * The dump written for {@link #goldenMode()} before the dump was streamed, i.e. by the writer
     * that built the whole dump as one String.
     */
    private static final String GOLDEN_DUMP = String.join("\r\n",
            "CREATE TABLE IF NOT EXISTS templates (templateId INTEGER NOT NULL "
                    + "AUTO_INCREMENT,templateName MEDIUMTEXT NOT NULL, PRIMARY KEY(templateId)); ",
            "CREATE INDEX tplIdx ON templates(templateId);",
            "CREATE TABLE IF NOT EXISTS templateId_pageId (templateId INTEGER UNSIGNED NOT NULL,"
                    + "pageId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, pageId));",
            "INSERT INTO templates (templateName) VALUES ('infobox');",
            "REPLACE INTO templateId_pageId VALUES (LAST_INSERT_ID(), 1),(LAST_INSERT_ID(), 2),"
                    + "(LAST_INSERT_ID(), 3);",
            "REPLACE INTO templateId_pageId VALUES (7, 5);",
            "INSERT INTO templates (templateName) VALUES ('müller');",
            "REPLACE INTO templateId_pageId VALUES (LAST_INSERT_ID(), 10);",
            "CREATE INDEX pageIdx ON templateId_pageId(pageId);",
            "CREATE TABLE IF NOT EXISTS templateId_revisionId (templateId INTEGER UNSIGNED NOT "
                    + "NULL,revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId));",
            "REPLACE INTO templateId_revisionId VALUES ((SELECT templateId FROM "
                    + "templates WHERE templateName = 'infobox' LIMIT 1), 100),"
                    + "((SELECT templateId FROM templates WHERE templateName = "
                    + "'infobox' LIMIT 1), 101);",
            "INSERT INTO templates (templateName) VALUES ('onlyrev');",
            "REPLACE INTO templateId_revisionId VALUES (LAST_INSERT_ID(), 200);",
            "CREATE INDEX revisionIdx ON templateId_revisionId(revisionID);", "");

    @TempDir
    Path outputDir;

    private static Map<String, int[]> ids(Object... nameThenIds)
    {
        Map<String, int[]> ids = new LinkedHashMap<>();
        for (int i = 0; i < nameThenIds.length; i += 2) {
            ids.put((String) nameThenIds[i], (int[]) nameThenIds[i + 1]);
        }
        return ids;
    }

    private static GeneratorMode modeOf(Map<String, int[]> pages, Map<String, int[]> revisions)
    {
        GeneratorMode mode = new GeneratorMode();
        mode.active_for_pages = true;
        mode.active_for_revisions = true;
        mode.templateNameToPageId = pages;
        mode.templateNameToRevId = revisions;
        return mode;
    }

    private static GeneratorMode goldenMode()
    {
        return modeOf(
                ids("infobox", new int[] { 1, 2, 3 }, "known", new int[] { 5 }, "müller",
                        new int[] { 10 }),
                ids("infobox", new int[] { 100, 101 }, "onlyrev", new int[] { 200 }));
    }

    private String writeSql(WikipediaTemplateInfoDumpWriter writer, GeneratorMode mode)
        throws Exception
    {
        writer.writeSQL(false, false, mode);
        return Files.readString(outputDir.resolve("templateInfo.sql"), UTF_8);
    }

    private WikipediaTemplateInfoDumpWriter writer(long maxAllowedPacket)
    {
        return new WikipediaTemplateInfoDumpWriter(
                outputDir.resolve("templateInfo.sql").toString(), UTF_8.name(),
                Map.of("known", 7), false, maxAllowedPacket);
    }

    @Test
    void writesTheSameDumpAsBeforeWithoutPacketLimit() throws Exception
    {
        WikipediaTemplateInfoDumpWriter writer = new WikipediaTemplateInfoDumpWriter(
                outputDir.resolve("templateInfo.sql").toString(), UTF_8.name(),
                Map.of("known", 7), false);

        assertEquals(GOLDEN_DUMP, writeSql(writer, goldenMode()));
    }

    @Test
    void writesTheSameDumpAsBeforeWhenEveryTemplateFitsIntoOnePacket() throws Exception
    {
        assertEquals(GOLDEN_DUMP, writeSql(writer(DEFAULT_MAX_ALLOWED_PACKET), goldenMode()));
    }

    @Test
    void splitsTheRowsOfATemplateToHonourMaxAllowedPacket() throws Exception
    {
        long maxAllowedPacket = 64 * 1024;
        int[] pageIds = IntStream.range(0, 200_000).map(i -> i * 3).toArray();
        int[] revisionIds = IntStream.range(0, 20_000).map(i -> 1_000_000 + i).toArray();
        // the name is inserted for the page index, so the revision index looks up its id with a
        // subselect holding characters that take more than one byte in UTF-8
        String sql = writeSql(writer(maxAllowedPacket), modeOf(
                ids("müller", pageIds, "known", new int[] { 1, 2 }),
                ids("müller", revisionIds)));

        List<String> statements = List.of(sql.split("\r\n"));
        for (String statement : statements) {
            assertTrue(statement.getBytes(UTF_8).length <= maxAllowedPacket,
                    "statement exceeds maxAllowedPacket: " + statement.length());
        }
        assertEquals(1, statements.stream()
                .filter(s -> s.equals(
                        "INSERT INTO templates (templateName) VALUES ('müller');"))
                .count());

        assertArrayEquals(pageIds, idsOf(statements, "REPLACE INTO templateId_pageId VALUES "
                + "(LAST_INSERT_ID(), "));
        assertArrayEquals(revisionIds, idsOf(statements,
                "REPLACE INTO templateId_revisionId VALUES ((SELECT templateId FROM "
                        + "templates WHERE templateName = 'müller' LIMIT 1), "));
        assertArrayEquals(new int[] { 1, 2 },
                idsOf(statements, "REPLACE INTO templateId_pageId VALUES (7, "));
        assertTrue(statements.stream()
                .filter(s -> s.startsWith("REPLACE INTO templateId_pageId VALUES (LAST_INSERT_ID()"))
                .count() > 1, "the rows were not split");
    }

    @Test
    void writesOneRowPerStatementIfThePacketIsTooSmallForTwo() throws Exception
    {
        String sql = writeSql(writer(1), modeOf(ids("known", new int[] { 1, 2 }), ids()));

        assertTrue(sql.contains("REPLACE INTO templateId_pageId VALUES (7, 1);\r\n"
                + "REPLACE INTO templateId_pageId VALUES (7, 2);\r\n"), sql);
    }

    @Test
    void movesTheCollectedIdsIntoSortedArrays()
    {
        Map<String, IntSet> collected = new LinkedHashMap<>();
        collected.put("b", new IntOpenHashSet(new int[] { 70_000, 3, 1_000_000, 42 }));
        collected.put("a", new IntOpenHashSet(new int[] { 1 }));

        Map<String, int[]> sorted = WikipediaTemplateInfoGenerator.toSortedIdArrays(collected);

        assertTrue(collected.isEmpty());
        assertEquals(List.of("b", "a"), new ArrayList<>(sorted.keySet()));
        assertArrayEquals(new int[] { 3, 42, 70_000, 1_000_000 }, sorted.get("b"));
        assertArrayEquals(new int[] { 1 }, sorted.get("a"));
    }

    /**
     * @return the ids of all rows written by the statements starting with {@code prefix}, in the
     *         order they are written
     */
    private static int[] idsOf(List<String> statements, String prefix)
    {
        IntStream.Builder ids = IntStream.builder();
        for (String statement : statements) {
            if (statement.startsWith(prefix)) {
                Matcher m = TUPLE_ID.matcher(statement);
                while (m.find()) {
                    ids.add(Integer.parseInt(m.group(1)));
                }
            }
        }
        return ids.build().toArray();
    }
}
