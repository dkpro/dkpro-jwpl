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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the SQL a {@link WikipediaTemplateInfoDumpWriter} generates, most of all for a run that
 * creates the page index and the revision index at once: a template used on a page and in a
 * revision must receive one id rather than one per index (see issue #95).
 */
class WikipediaTemplateInfoDumpWriterTest
{

    @TempDir
    Path outputDir;

    private static GeneratorMode modeOf(Map<String, Set<Integer>> pages,
            Map<String, Set<Integer>> revisions)
    {
        GeneratorMode mode = new GeneratorMode();
        mode.active_for_pages = true;
        mode.active_for_revisions = true;
        mode.templateNameToPageId = pages;
        mode.templateNameToRevId = revisions;
        return mode;
    }

    private String writeSql(Map<String, Integer> knownIds, GeneratorMode mode) throws IOException
    {
        Path output = outputDir.resolve("templateInfo.sql");
        new WikipediaTemplateInfoDumpWriter(output.toString(), UTF_8.name(), knownIds, false)
                .writeSQL(false, false, mode);
        return Files.readString(output, UTF_8);
    }

    private static long countOccurrences(String haystack, String needle)
    {
        long count = 0;
        int from = 0;
        int at;
        while ((at = haystack.indexOf(needle, from)) != -1) {
            count++;
            from = at + needle.length();
        }
        return count;
    }

    @Test
    void insertsATemplateUsedByBothIndicesOnlyOnce() throws Exception
    {
        String sql = writeSql(Map.of(),
                modeOf(Map.of("Infobox", Set.of(1, 2)), Map.of("Infobox", Set.of(100))));

        assertEquals(1, countOccurrences(sql, "INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                + " (templateName) VALUES ('Infobox');"));
        // the index written first refers to the row it has just inserted ...
        assertTrue(
                sql.contains("REPLACE INTO " + GeneratorConstants.TABLE_TPLID_PAGEID
                        + " VALUES (LAST_INSERT_ID(), "),
                "the page index does not use the inserted id: " + sql);
        // ... and the one written afterwards looks the id of that same row up
        assertTrue(
                sql.contains("REPLACE INTO " + GeneratorConstants.TABLE_TPLID_REVISIONID
                        + " VALUES ((SELECT templateId FROM "
                        + GeneratorConstants.TABLE_TPLID_TPLNAME
                        + " WHERE templateName = 'Infobox' LIMIT 1), 100);"),
                "the revision index does not look the inserted id up: " + sql);
    }

    @Test
    void insertsEveryTemplateThatIsNotKnownYet() throws Exception
    {
        String sql = writeSql(Map.of(),
                modeOf(Map.of("OnlyOnPages", Set.of(1)), Map.of("OnlyInRevisions", Set.of(100))));

        assertEquals(1, countOccurrences(sql, "VALUES ('OnlyOnPages');"));
        assertEquals(1, countOccurrences(sql, "VALUES ('OnlyInRevisions');"));
    }

    @Test
    void reusesTheIdOfATemplateThatIsAlreadyKnown() throws Exception
    {
        String sql = writeSql(Map.of("Infobox", 42),
                modeOf(Map.of("Infobox", Set.of(1)), Map.of("Infobox", Set.of(100))));

        assertFalse(sql.contains("VALUES ('Infobox');"),
                "a known template must not be inserted again: " + sql);
        assertTrue(sql.contains("REPLACE INTO " + GeneratorConstants.TABLE_TPLID_PAGEID
                + " VALUES (42, 1);"), sql);
        assertTrue(sql.contains("REPLACE INTO " + GeneratorConstants.TABLE_TPLID_REVISIONID
                + " VALUES (42, 100);"), sql);
    }
}
