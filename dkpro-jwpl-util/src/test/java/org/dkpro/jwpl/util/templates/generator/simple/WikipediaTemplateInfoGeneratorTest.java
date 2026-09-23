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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.util.StringUtils;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests resolving the ids of templates that already exist in the database, which the
 * {@link WikipediaTemplateInfoGenerator} does before writing the dump, and reading the
 * revisions of a page sequentially.
 */
class WikipediaTemplateInfoGeneratorTest
{
    private static final String DB_NAME = "wikiapi_simple_20090119_stripped";

    // Page 'Car', the only page with revisions in the stripped test database
    private static final int PAGE_ID = 3442;

    @TempDir
    Path tempDir;

    @Test
    void testForEachRevisionOfPageMatchesFullRebuild() throws Exception
    {
        RevisionAPIConfiguration revConfig = new RevisionAPIConfiguration(createDbConfig());

        try (RevisionApi revApi = new RevisionApi(revConfig)) {
            List<Timestamp> timestamps = new ArrayList<>();
            List<Integer> expectedIds = new ArrayList<>();
            try (PreparedStatement statement = revApi.getConnection().prepareStatement(
                    "SELECT Timestamp, RevisionID FROM revisions WHERE ArticleID=? "
                            + "ORDER BY PrimaryKey")) {
                statement.setInt(1, PAGE_ID);
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    timestamps.add(new Timestamp(result.getLong(1)));
                    expectedIds.add(result.getInt(2));
                }
            }
            assertEquals(382, timestamps.size());

            List<Revision> revisions = new ArrayList<>();
            WikipediaTemplateInfoGenerator.forEachRevisionOfPage(revApi, revConfig, PAGE_ID,
                    timestamps, revisions::add);

            assertEquals(expectedIds.size(), revisions.size());
            for (int i = 0; i < revisions.size(); i++) {
                Revision revision = revisions.get(i);
                assertEquals(PAGE_ID, revision.getArticleID());
                assertEquals(i + 1, revision.getRevisionCounter());
                assertEquals(expectedIds.get(i), revision.getRevisionID());

                // Compare with the text rebuilt from the preceding full revision
                Revision rebuilt = revApi.getRevision(revision.getRevisionID());
                assertEquals(rebuilt.getRevisionText(), revision.getRevisionText());
            }
        }
    }

    @Test
    void testForEachRevisionOfPageWithoutRevisions() throws Exception
    {
        RevisionAPIConfiguration revConfig = new RevisionAPIConfiguration(createDbConfig());

        try (RevisionApi revApi = new RevisionApi(revConfig)) {
            List<Revision> revisions = new ArrayList<>();
            WikipediaTemplateInfoGenerator.forEachRevisionOfPage(revApi, revConfig, PAGE_ID,
                    new ArrayList<>(), revisions::add);
            assertTrue(revisions.isEmpty());
        }
    }

    @Test
    void resolvesExistingIdsUnderTheOriginalTemplateNames()
    {
        String quoted = StringUtils.sqlEscape("o'neil");
        Map<String, Integer> existing = Map.of("infobox", 1, "cite web", 2, quoted, 3);
        Map<String, Integer> resolved = new HashMap<>();

        WikipediaTemplateInfoGenerator.resolveTemplateIds(name -> existing.getOrDefault(name, -1),
                Set.of("infobox", "cite web", quoted, "unknown"), resolved);

        assertEquals(Map.of("infobox", 1, "cite web", 2, quoted, 3), resolved);
    }

    @Test
    void keepsIdsAlreadyResolvedForAnotherIndex()
    {
        Map<String, Integer> existing = Map.of("infobox", 1, "stub", 2);
        ToIntFunction<String> existingIds = name -> existing.getOrDefault(name, -1);
        Map<String, Integer> resolved = new HashMap<>();

        WikipediaTemplateInfoGenerator.resolveTemplateIds(existingIds, Set.of("infobox"),
                resolved);
        resolved.put("infobox", 42);
        WikipediaTemplateInfoGenerator.resolveTemplateIds(existingIds, Set.of("infobox", "stub"),
                resolved);

        assertEquals(Map.of("infobox", 42, "stub", 2), resolved);
    }

    private DatabaseConfiguration createDbConfig() throws Exception
    {
        Path script = tempDir.resolve(DB_NAME + ".script");
        try (InputStream in = getClass().getResourceAsStream("/db/" + DB_NAME + ".script")) {
            Files.copy(in, script);
        }
        return new DatabaseConfiguration("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:file:" + tempDir.resolve(DB_NAME), "localhost", DB_NAME, "sa", "",
                Language.simple_english);
    }
}
