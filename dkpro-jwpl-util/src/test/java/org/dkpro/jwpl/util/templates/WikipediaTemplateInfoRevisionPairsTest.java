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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.Template;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.util.templates.RevisionPair.RevisionPairType;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks {@link WikipediaTemplateInfo#getRevisionPairs(int, String, RevisionPairType)} and
 * {@link WikipediaTemplateInfo#getRevisionPairsWithoutIndex(int, String, RevisionPairType)}
 * against the former implementation, which looked up every revision by its timestamp. The
 * revisions come from a copy of the RevisionMachine test database; the template index is built
 * from the parsed revision texts.
 */
class WikipediaTemplateInfoRevisionPairsTest
{

    private static final String DB_NAME = "wikiapi_simple_20090119_stripped";

    private static final Path DB_SCRIPT = Paths.get("..", "dkpro-jwpl-revisionmachine", "src",
            "test", "resources", "db", DB_NAME + ".script");

    private static final int PAGE_ID = 3442;

    @TempDir
    Path tempDir;

    private RevisionApi revApi;

    private Connection connection;

    private WikipediaTemplateInfo info;

    /** Number of revisions per template name found in the parsed revisions */
    private final Map<String, Integer> templateCounts = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception
    {
        Files.copy(DB_SCRIPT, tempDir.resolve(DB_NAME + ".script"));
        DatabaseConfiguration dbConfig = new DatabaseConfiguration("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:file:" + tempDir.resolve(DB_NAME), "localhost", DB_NAME, "sa", "",
                Language.simple_english);
        revApi = new RevisionApi(dbConfig);
        connection = revApi.getConnection();

        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.simple_english);
        pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        MediaWikiParser parser = pf.createParser();
        info = new WikipediaTemplateInfo(connection, revApi, parser);

        buildTemplateIndex(parser);
    }

    @AfterEach
    void tearDown() throws Exception
    {
        try (Statement st = connection.createStatement()) {
            st.execute("SHUTDOWN");
        }
        revApi.close();
    }

    /**
     * Creates the template index for all revisions of the test page. Template names are stored
     * lower-cased, as the template info generator does.
     */
    private void buildTemplateIndex(MediaWikiParser parser) throws Exception
    {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + " (templateId INTEGER NOT NULL PRIMARY KEY,"
                    + " templateName VARCHAR(255) NOT NULL)");
            st.execute("CREATE TABLE " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + " (templateId INTEGER NOT NULL, revisionId INTEGER NOT NULL)");
        }
        Map<String, Integer> templateIds = new LinkedHashMap<>();
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO "
                + GeneratorConstants.TABLE_TPLID_REVISIONID + " VALUES (?, ?)")) {
            for (Revision revision : revApi.getRevisionMetaData(PAGE_ID)) {
                Set<String> names = new HashSet<>();
                for (Template tpl : parser.parse(revision.getRevisionText()).getTemplates()) {
                    names.add(tpl.getName().toLowerCase());
                }
                for (String name : names) {
                    int id = templateIds.computeIfAbsent(name, n -> templateIds.size() + 1);
                    templateCounts.merge(name, 1, Integer::sum);
                    insert.setInt(1, id);
                    insert.setInt(2, revision.getRevisionID());
                    insert.executeUpdate();
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO "
                + GeneratorConstants.TABLE_TPLID_TPLNAME + " VALUES (?, ?)")) {
            for (Entry<String, Integer> e : templateIds.entrySet()) {
                insert.setInt(1, e.getValue());
                insert.setString(2, e.getKey());
                insert.executeUpdate();
            }
        }
    }

    /**
     * Templates that are neither in all nor in no revision, i.e. which are added or removed at
     * some point, in upper case to check that names are compared case-insensitively.
     */
    private List<String> changingTemplates(int max) throws Exception
    {
        int revisionCount = revApi.getRevisionMetaData(PAGE_ID).size();
        List<String> templates = new ArrayList<>();
        templateCounts.entrySet().stream()
                .filter(e -> e.getValue() < revisionCount)
                .sorted(Entry.<String, Integer> comparingByValue().reversed()
                        .thenComparing(Entry.comparingByKey()))
                .limit(max).forEach(e -> templates.add(e.getKey().toUpperCase()));
        assertFalse(templates.isEmpty());
        return templates;
    }

    @Test
    void testGetRevisionPairsMatchesFormerImplementation() throws Exception
    {
        int pairCount = 0;
        for (String template : changingTemplates(Integer.MAX_VALUE)) {
            for (RevisionPairType type : RevisionPairType.values()) {
                List<RevisionPair> expected = formerRevisionPairs(template, type, true);
                List<RevisionPair> actual = info.getRevisionPairs(PAGE_ID, template, type);
                assertEquals(ids(expected), ids(actual), template + " " + type);
                pairCount += actual.size();
            }
        }
        assertTrue(pairCount > 0);
    }

    @Test
    void testGetRevisionPairsWithoutIndexMatchesFormerImplementation() throws Exception
    {
        int pairCount = 0;
        for (String template : changingTemplates(3)) {
            for (RevisionPairType type : RevisionPairType.values()) {
                List<RevisionPair> expected = formerRevisionPairs(template, type, false);
                List<RevisionPair> actual = info.getRevisionPairsWithoutIndex(PAGE_ID, template,
                        type);
                assertEquals(ids(expected), ids(actual), template + " " + type);
                assertEquals(ids(info.getRevisionPairs(PAGE_ID, template, type)), ids(actual));
                pairCount += actual.size();
            }
        }
        assertTrue(pairCount > 0);
    }

    @Test
    void testRevisionIdsAreLookedUpInBatches() throws Exception
    {
        String template = changingTemplates(1).get(0);
        List<Integer> revisionIds = new ArrayList<>();
        Set<Integer> expected = new HashSet<>();
        for (Revision revision : revApi.getRevisionMetaData(PAGE_ID)) {
            revisionIds.add(revision.getRevisionID());
            if (info.revisionContainsTemplateName(revision.getRevisionID(), template)) {
                expected.add(revision.getRevisionID());
            }
        }
        // unknown revision ids, to need more than one batch
        for (int i = 1; i <= 2 * WikipediaTemplateInfo.REVISION_ID_BATCH_SIZE; i++) {
            revisionIds.add(i);
        }
        assertFalse(expected.isEmpty());
        assertEquals(expected, WikipediaTemplateInfo
                .getRevisionIdsContainingTemplateName(connection, template, revisionIds));
        assertEquals(Set.of(), WikipediaTemplateInfo
                .getRevisionIdsContainingTemplateName(connection, template, List.of()));
    }

    @Test
    void testRevisionsWithTheSameTimestampAreNotMerged()
    {
        Timestamp ts = new Timestamp(1_000_000L);
        List<Revision> revisions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Revision revision = new Revision(i);
            revision.setRevisionID(100 + i);
            revision.setTimeStamp(ts);
            revisions.add(revision);
        }
        List<RevisionPair> added = WikipediaTemplateInfo.findRevisionPairs(revisions, Set.of(102),
                "stub", RevisionPairType.addTemplate);
        assertEquals(List.of(List.of(101, 102)), ids(added));
        List<RevisionPair> deleted = WikipediaTemplateInfo.findRevisionPairs(revisions,
                Set.of(102), "stub", RevisionPairType.deleteTemplate);
        assertEquals(List.of(List.of(102, 103)), ids(deleted));
    }

    private static List<List<Integer>> ids(List<RevisionPair> pairs)
    {
        List<List<Integer>> ids = new ArrayList<>();
        for (RevisionPair pair : pairs) {
            ids.add(List.of(pair.getBeforeRevision().getRevisionID(),
                    pair.getAfterRevision().getRevisionID()));
        }
        return ids;
    }

    /**
     * The former implementation of both methods: looks up each revision by its timestamp and
     * checks the template for each revision on its own.
     */
    private List<RevisionPair> formerRevisionPairs(String template, RevisionPairType type,
            boolean useIndex)
        throws Exception
    {
        List<RevisionPair> resultList = new LinkedList<>();
        Map<Timestamp, Boolean> tplIndexMap = new HashMap<>();

        for (Timestamp ts : revisionTimestamps()) {
            int revId = revApi.getRevision(PAGE_ID, ts).getRevisionID();
            tplIndexMap.put(ts, useIndex ? info.revisionContainsTemplateName(revId, template)
                    : info.revisionContainsTemplateNameWithoutIndex(revId, template));
        }

        SortedSet<Entry<Timestamp, Boolean>> entries = new TreeSet<>(Entry.comparingByKey());
        entries.addAll(tplIndexMap.entrySet());

        Entry<Timestamp, Boolean> prev = null;
        for (Entry<Timestamp, Boolean> current : entries) {
            if (prev != null && prev.getValue() != current.getValue()) {
                if (prev.getValue() && !current.getValue()
                        && type == RevisionPairType.deleteTemplate
                        || !prev.getValue() && current.getValue()
                                && type == RevisionPairType.addTemplate) {
                    resultList.add(new RevisionPair(revApi.getRevision(PAGE_ID, prev.getKey()),
                            revApi.getRevision(PAGE_ID, current.getKey()), template, type));
                }
            }
            prev = current;
        }
        return resultList;
    }

    /**
     * The query of {@link RevisionApi#getRevisionTimestamps(int)}, without its index check, which
     * HSQLDB does not support.
     */
    private List<Timestamp> revisionTimestamps() throws SQLException
    {
        List<Timestamp> timestamps = new ArrayList<>();
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT Timestamp FROM revisions WHERE ArticleID=?")) {
            statement.setInt(1, PAGE_ID);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    timestamps.add(new Timestamp(result.getLong(1)));
                }
            }
        }
        return timestamps;
    }
}
