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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.revisionmachine.BaseJWPLTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that the lazy mode of the {@link RevisionIterator} does not select the {@code Revision}
 * blob, and that metadata and texts are the same as in eager mode and as returned by
 * {@link RevisionApi#getRevision(int)}.
 */
public class RevisionIteratorLazyModeTest
    extends BaseJWPLTest
{

    private static final String DATABASE = "wikiapi_simple_20090119_stripped";

    // In the stripped HSQLDB data set only 382 revisions exist for the page 'Car'
    private static final int GLOBAL_REVISION_COUNT = 382;

    private static final Pattern REVISION_COLUMN = Pattern.compile("\\bRevision\\b");

    @TempDir
    static Path tempDir;

    private static DatabaseConfiguration legacyDb;

    private static DatabaseConfiguration namespaceDb;

    private final List<String> preparedPageQueries = new ArrayList<>();

    @BeforeAll
    public static void setUpDatabases() throws Exception
    {
        legacyDb = obtainHSDLDBConfiguration(DATABASE, Language.simple_english);

        Files.copy(Path.of("src/test/resources/db", DATABASE + ".script"),
                tempDir.resolve(DATABASE + ".script"));
        String url = "jdbc:hsqldb:file:" + tempDir.resolve(DATABASE) + ";shutdown=true";
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE revisions ADD COLUMN Namespace INTEGER");
            statement.execute("UPDATE revisions SET Namespace = 0");
        }
        namespaceDb = new DatabaseConfiguration("org.hsqldb.jdbcDriver", url, "localhost",
                DATABASE, "sa", "", Language.simple_english);
    }

    @Test
    public void testLazyModeWithoutNamespaceColumn() throws Exception
    {
        assertLazyModeMatchesEagerMode(legacyDb);
    }

    @Test
    public void testLazyModeWithNamespaceColumn() throws Exception
    {
        assertLazyModeMatchesEagerMode(namespaceDb);
    }

    @Test
    public void testModeSwitchReplacesPageQuery() throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration(legacyDb);
        config.setBufferSize(50);
        List<Revision> eager = iterate(config, false);

        preparedPageQueries.clear();

        int i = 0;
        try (RevisionIterator iterator = new RevisionIterator(config, 0, Integer.MAX_VALUE,
                recordingConnection(config), null)) {
            while (iterator.hasNext()) {
                if (i == 100) {
                    // takes effect with the next page
                    iterator.setShouldLoadRevisionText(true);
                }
                Revision revision = iterator.next();
                assertNotNull(revision);
                assertEquals(eager.get(i).getRevisionID(), revision.getRevisionID());
                assertEquals(eager.get(i).getRevisionText(), revision.getRevisionText());
                i++;
            }
        }
        assertEquals(GLOBAL_REVISION_COUNT, i);

        assertEquals(2, preparedPageQueries.size());
        assertTrue(REVISION_COLUMN.matcher(preparedPageQueries.get(0)).find());
        assertFalse(REVISION_COLUMN.matcher(preparedPageQueries.get(1)).find());
    }

    private void assertLazyModeMatchesEagerMode(DatabaseConfiguration db) throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration(db);
        List<Revision> eager = iterate(config, false);
        assertEquals(1, preparedPageQueries.size());
        assertTrue(REVISION_COLUMN.matcher(preparedPageQueries.get(0)).find());

        preparedPageQueries.clear();
        List<Revision> lazy = iterate(config, true);
        assertEquals(1, preparedPageQueries.size());
        assertFalse(REVISION_COLUMN.matcher(preparedPageQueries.get(0)).find(),
                preparedPageQueries.get(0));

        assertEquals(GLOBAL_REVISION_COUNT, eager.size());
        assertEquals(eager.size(), lazy.size());

        RevisionApi revisionApi = new RevisionApi(db);
        try {
            for (int i = 0; i < eager.size(); i++) {
                Revision e = eager.get(i);
                Revision l = lazy.get(i);
                assertEquals(e.getPrimaryKey(), l.getPrimaryKey());
                assertEquals(e.getRevisionCounter(), l.getRevisionCounter());
                assertEquals(e.getRevisionID(), l.getRevisionID());
                assertEquals(e.getArticleID(), l.getArticleID());
                assertEquals(e.getTimeStamp(), l.getTimeStamp());
                assertEquals(e.getFullRevisionID(), l.getFullRevisionID());
                assertEquals(e.getContributorName(), l.getContributorName());
                assertEquals(e.getContributorId(), l.getContributorId());
                assertEquals(e.getComment(), l.getComment());
                assertEquals(e.isMinor(), l.isMinor());
                assertEquals(e.contributorIsRegistered(), l.contributorIsRegistered());
                assertEquals(e.getNamespace(), l.getNamespace());

                String text = revisionApi.getRevision(e.getRevisionID()).getRevisionText();
                assertEquals(text, e.getRevisionText());
                assertEquals(text, l.getRevisionText());
            }
        }
        finally {
            revisionApi.close();
        }
    }

    private List<Revision> iterate(RevisionAPIConfiguration config, boolean lazy)
        throws Exception
    {
        List<Revision> revisions = new ArrayList<>();
        try (RevisionIterator iterator = new RevisionIterator(config, 0, Integer.MAX_VALUE,
                recordingConnection(config), null)) {
            iterator.setShouldLoadRevisionText(lazy);
            while (iterator.hasNext()) {
                Revision revision = iterator.next();
                assertNotNull(revision);
                revisions.add(revision);
            }
        }
        return revisions;
    }

    /**
     * @return a connection to the database of the given configuration that records the SQL of
     *         every page query prepared on it
     */
    private Connection recordingConnection(RevisionAPIConfiguration config) throws Exception
    {
        Connection connection = DriverManager.getConnection(config.getJdbcURL(), config.getUser(),
                config.getPassword());
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())
                            && ((String) args[0]).contains("FROM revisions WHERE PrimaryKey >")) {
                        preparedPageQueries.add((String) args[0]);
                    }
                    try {
                        return method.invoke(connection, args);
                    }
                    catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
