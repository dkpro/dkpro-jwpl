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

import static org.dkpro.jwpl.revisionmachine.api.ChronoTestData.FIRST_PK;
import static org.dkpro.jwpl.revisionmachine.api.ChronoTestData.LAST_PK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Iterates in chronological order over a copy of the stripped test data set. In the copy, the
 * second article of the article index points to the revisions of the page 'Car' as well, so that
 * the iteration covers two articles without a chronological mapping.
 */
public class ChronoRevisionIteratorTest
{

    // Number of revisions of 'Car' covered by the article with a chronological mapping: all of
    // them
    private static final int MAPPED = ChronoTestData.REVISIONS;

    // Chrono storage space that is too small to keep all of these revisions
    private static final long STORAGE_SPACE = 20_000;

    // Revision ID of the first added revision, above those of the test data set
    private static final int FIRST_ADDED_REVISION_ID = 2_000_000;

    // Buffer size with which every article is fetched in a batch of its own
    private static final int SINGLE_ARTICLE_BATCH = 1;

    // Buffer size with which all articles are fetched in a single batch
    private static final int LARGE_BATCH = 1000;

    // Articles appended after 'Car' (3442): one with two revisions between articles with one
    private static final List<AddedArticle> ARTICLES_WITH_ONE_AND_TWO_REVISIONS = List.of(
            new AddedArticle(3443, 1), new AddedArticle(3444, 2), new AddedArticle(3445, 1),
            new AddedArticle(3446, 1));

    @TempDir
    static Path tempDir;

    private static String url;

    private static RevisionAPIConfiguration config;

    @BeforeAll
    public static void setUpDatabase() throws Exception
    {
        url = copyDatabase(tempDir);
        config = configuration(url);
    }

    /**
     * Copies the test data set into the given directory and lets the second article of the
     * article index point to the revisions of the page 'Car'.
     *
     * @return the JDBC URL of the copy
     */
    private static String copyDatabase(final Path dir) throws Exception
    {
        String url = ChronoTestData.copyDatabase(dir);
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE index_articleID_rc_ts SET FullRevisionPKs = '" + FIRST_PK
                    + "', RevisionCounter = '1 " + ChronoTestData.REVISIONS
                    + "' WHERE ArticleID = 3443");
        }
        return url;
    }

    private static RevisionAPIConfiguration configuration(final String url)
    {
        RevisionAPIConfiguration config = ChronoTestData.configuration(url);
        // One article per batch, so that the article index is paged as well
        config.setBufferSize(1);
        return config;
    }

    private static List<String> revisionsOfCar(final String url) throws Exception
    {
        List<String> revisionsOfCar = new ArrayList<>();
        for (Revision revision : ChronoTestData.revisionsOfCar(config, url)) {
            revisionsOfCar.add(describe(revision));
        }
        return revisionsOfCar;
    }

    @Test
    public void testIterationProbesSchemaOnceAndReusesStatements() throws Exception
    {
        List<String> revisionsOfCar = revisionsOfCar(url);

        List<String> expected = new ArrayList<>(revisionsOfCar);
        expected.addAll(revisionsOfCar);

        AtomicInteger namespaceProbes = new AtomicInteger();
        AtomicInteger articleStatements = new AtomicInteger();
        AtomicInteger mappingStatements = new AtomicInteger();
        Connection connection = countingConnection(DriverManager.getConnection(url, "sa", ""),
                namespaceProbes, articleStatements, mappingStatements, new AtomicInteger(),
                new AtomicInteger());

        List<String> actual = iterateChronologically(config, connection);

        assertEquals(expected, actual);
        assertEquals(1, namespaceProbes.get());
        assertEquals(1, articleStatements.get());
        assertEquals(1, mappingStatements.get());
        assertTrue(connection.isClosed());
    }

    @Test
    public void testIterationWithMappingReusesRangeStatement(@TempDir final Path dir)
        throws Exception
    {
        String url = copyDatabase(dir);
        RevisionAPIConfiguration config = configuration(url);
        // Small enough to evict reconstructed revisions, so that they have to be fetched again
        config.setChronoStorageSpace(STORAGE_SPACE);

        List<String> revisionsOfCar = revisionsOfCar(url);

        // The second article covers the first MAPPED revisions of 'Car' and delivers them in
        // reverse order
        String mapping = ChronoTestData.reversedMapping(MAPPED, MAPPED);
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE index_articleID_rc_ts SET RevisionCounter = '1 " + MAPPED
                    + "' WHERE ArticleID = 3443");
            statement.execute("INSERT INTO index_chronological VALUES(3443, '" + mapping + "', '"
                    + mapping + "')");
        }

        List<String> expected = new ArrayList<>(revisionsOfCar);
        expected.addAll(ChronoTestData.reversedTail(revisionsOfCar.subList(0, MAPPED), MAPPED));

        AtomicInteger rangeStatements = new AtomicInteger();
        AtomicInteger rangeQueries = new AtomicInteger();
        Connection connection = countingConnection(DriverManager.getConnection(url, "sa", ""),
                new AtomicInteger(), new AtomicInteger(), new AtomicInteger(), rangeStatements,
                rangeQueries);

        List<String> actual = iterateChronologically(config, connection);

        assertEquals(expected, actual);
        assertEquals(1, rangeStatements.get());
        assertTrue(rangeQueries.get() > 1, "Expected evicted revisions to be fetched again");
        assertTrue(connection.isClosed());
    }

    /**
     * Iterates over articles with one and two revisions and compares the result with the
     * {@link RevisionApi}. The helpers close their connections and the iteration closes its
     * connection, JUnit deletes the copy of the database in {@code dir} afterwards.
     */
    @ParameterizedTest
    @ValueSource(ints = { SINGLE_ARTICLE_BATCH, LARGE_BATCH })
    public void testIterationOverArticlesWithOneAndTwoRevisions(final int bufferSize,
            @TempDir final Path dir)
        throws Exception
    {
        String url = prepareDatabaseWithArticles(dir, ARTICLES_WITH_ONE_AND_TWO_REVISIONS);
        RevisionAPIConfiguration config = configuration(url);
        config.setBufferSize(bufferSize);

        List<Integer> revisionIDs = revisionIDsInStorageOrder(url);
        List<String> actual = iterateChronologically(config,
                DriverManager.getConnection(url, "sa", ""));

        assertAllRevisionsInOrder(revisionIDs, actual);
        assertMatchesRevisionApi(config, revisionIDs, actual);
    }

    /**
     * An article appended to the test data set.
     *
     * @param articleID
     *            ID of the article
     * @param revisions
     *            number of revisions of the article, at most that of 'Car'
     */
    private record AddedArticle(int articleID, int revisions)
    {
    }

    /**
     * Copies the test data set into the given directory and appends the given articles after the
     * revisions of 'Car'.
     *
     * @return the JDBC URL of the copy
     */
    private static String prepareDatabaseWithArticles(final Path dir,
            final List<AddedArticle> articles)
        throws Exception
    {
        String url = ChronoTestData.copyDatabase(dir);
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                PreparedStatement deleteIndexEntry = connection.prepareStatement(
                        "DELETE FROM index_articleID_rc_ts WHERE ArticleID = ?");
                PreparedStatement insertRevision = connection.prepareStatement(
                        "INSERT INTO revisions SELECT CAST(? AS BIGINT), CAST(? AS BIGINT),"
                                + " CAST(? AS BIGINT), CAST(? AS BIGINT), CAST(? AS BIGINT),"
                                + " Timestamp, Revision, Comment, Minor, ContributorName,"
                                + " ContributorIsRegistered, ContributorId"
                                + " FROM revisions WHERE PrimaryKey = ?");
                PreparedStatement insertRevisionIndexEntry = connection
                        .prepareStatement("INSERT INTO index_revisionID VALUES(?, ?, ?)");
                PreparedStatement insertArticleIndexEntry = connection.prepareStatement(
                        "INSERT INTO index_articleID_rc_ts VALUES(?, ?, ?, 0, 0)")) {
            int pk = LAST_PK + 1;
            int revisionID = FIRST_ADDED_REVISION_ID;
            for (AddedArticle article : articles) {
                // The test data set indexes some of these articles without their revisions
                deleteIndexEntry.setInt(1, article.articleID());
                deleteIndexEntry.executeUpdate();

                int firstPK = pk;
                int fullRevisionID = revisionID;
                for (int revisionCounter = 1; revisionCounter <= article.revisions();
                        revisionCounter++) {
                    // Copies the text of the revision of 'Car' with the same revision counter,
                    // a full revision followed by diffs
                    insertRevision.setInt(1, pk);
                    insertRevision.setInt(2, fullRevisionID);
                    insertRevision.setInt(3, revisionCounter);
                    insertRevision.setInt(4, revisionID);
                    insertRevision.setInt(5, article.articleID());
                    insertRevision.setInt(6, FIRST_PK + revisionCounter - 1);
                    insertRevision.executeUpdate();

                    insertRevisionIndexEntry.setInt(1, revisionID);
                    insertRevisionIndexEntry.setInt(2, pk);
                    insertRevisionIndexEntry.setInt(3, firstPK);
                    insertRevisionIndexEntry.executeUpdate();

                    pk++;
                    revisionID++;
                }

                insertArticleIndexEntry.setInt(1, article.articleID());
                insertArticleIndexEntry.setString(2, String.valueOf(firstPK));
                insertArticleIndexEntry.setString(3, "1 " + article.revisions());
                insertArticleIndexEntry.executeUpdate();
            }
        }
        return url;
    }

    /**
     * Returns the IDs of all revisions in the order of their primary keys, which is the
     * chronological order within each article.
     */
    private static List<Integer> revisionIDsInStorageOrder(final String url) throws Exception
    {
        List<Integer> revisionIDs = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet result = statement
                        .executeQuery("SELECT RevisionID FROM revisions ORDER BY PrimaryKey")) {
            while (result.next()) {
                revisionIDs.add(result.getInt(1));
            }
        }
        return revisionIDs;
    }

    /**
     * Asserts that the iteration returned the revisions of 'Car' and of all added articles.
     */
    private static void assertAllRevisionsInOrder(final List<Integer> revisionIDs,
            final List<String> actual)
    {
        int addedRevisions = ARTICLES_WITH_ONE_AND_TWO_REVISIONS.stream()
                .mapToInt(AddedArticle::revisions).sum();
        assertEquals(ChronoTestData.REVISIONS + addedRevisions, revisionIDs.size());
        assertEquals(revisionIDs.size(), actual.size());
    }

    /**
     * Asserts that the iteration returned, in the given order, the revisions that the
     * {@link RevisionApi} returns for the given IDs.
     */
    private static void assertMatchesRevisionApi(final RevisionAPIConfiguration config,
            final List<Integer> revisionIDs, final List<String> actual)
        throws Exception
    {
        List<String> expected = new ArrayList<>();
        try (RevisionApi revisionApi = new RevisionApi(config)) {
            for (int revisionID : revisionIDs) {
                expected.add(describe(revisionApi.getRevision(revisionID)));
            }
        }
        assertEquals(expected, actual);
    }

    /**
     * Iterates in chronological order over all revisions and closes the iterator afterwards.
     *
     * @return the descriptions of the revisions in the order of the iteration
     */
    private static List<String> iterateChronologically(final RevisionAPIConfiguration config,
            final Connection connection)
        throws Exception
    {
        List<String> revisions = new ArrayList<>();
        ChronoRevisionIterator iterator = new ChronoRevisionIterator(config, connection);
        try {
            while (iterator.hasNext()) {
                revisions.add(describe(iterator.next()));
            }
        }
        finally {
            iterator.close();
        }
        return revisions;
    }

    private static String describe(final Revision revision)
    {
        return revision.getRevisionID() + "/" + revision.getArticleID() + "/"
                + revision.getRevisionCounter() + "/" + revision.getRevisionText().hashCode();
    }

    /**
     * Wraps the connection to count the probes for the Namespace column and the statements
     * prepared for the article batches, the mapping lookup and the revision ranges, as well as
     * the executed revision range queries.
     */
    private static Connection countingConnection(final Connection connection,
            final AtomicInteger namespaceProbes, final AtomicInteger articleStatements,
            final AtomicInteger mappingStatements, final AtomicInteger rangeStatements,
            final AtomicInteger rangeQueries)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("prepareStatement")) {
                if (args[0].toString().contains("index_articleID_rc_ts")) {
                    articleStatements.incrementAndGet();
                }
                else if (args[0].toString().contains("index_chronological")) {
                    mappingStatements.incrementAndGet();
                }
                else if (args[0].toString().contains("FROM revisions WHERE PrimaryKey >= ?")) {
                    rangeStatements.incrementAndGet();
                    PreparedStatement statement = (PreparedStatement) invoke(connection, method,
                            args);
                    return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                            new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
                                if (m.getName().equals("executeQuery") && a == null) {
                                    rangeQueries.incrementAndGet();
                                }
                                return invoke(statement, m, a);
                            });
                }
            }
            Object result = invoke(connection, method, args);
            if (method.getName().equals("createStatement")) {
                Statement statement = (Statement) result;
                return Proxy.newProxyInstance(Statement.class.getClassLoader(),
                        new Class<?>[] { Statement.class }, (p, m, a) -> {
                            if (m.getName().equals("executeQuery")
                                    && a[0].toString().contains("WHERE 1 = 0")) {
                                namespaceProbes.incrementAndGet();
                            }
                            return invoke(statement, m, a);
                        });
            }
            return result;
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class }, handler);
    }

    private static Object invoke(final Object target, final Method method, final Object[] args)
        throws Throwable
    {
        try {
            return method.invoke(target, args);
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
