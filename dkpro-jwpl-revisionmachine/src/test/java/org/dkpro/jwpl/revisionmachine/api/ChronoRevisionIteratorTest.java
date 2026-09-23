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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Iterates in chronological order over a copy of the stripped test data set. In the copy, the
 * second article of the article index points to the revisions of the page 'Car' as well, so that
 * the iteration covers two articles without a chronological mapping.
 */
public class ChronoRevisionIteratorTest
{

    private static final String DATABASE = "wikiapi_simple_20090119_stripped";

    // Primary keys of the revisions of the page 'Car', the only page in the test data set
    private static final int FIRST_PK = 71500;

    private static final int LAST_PK = 71881;

    // Number of revisions of 'Car' covered by the article with a chronological mapping
    private static final int MAPPED = 40;

    // Chrono storage space that is too small to keep all of these revisions
    private static final long STORAGE_SPACE = 20_000;

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
        Files.copy(Path.of("src/test/resources/db", DATABASE + ".script"),
                dir.resolve(DATABASE + ".script"));
        String url = "jdbc:hsqldb:file:" + dir.resolve(DATABASE) + ";shutdown=true";
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE index_articleID_rc_ts SET FullRevisionPKs = '" + FIRST_PK
                    + "', RevisionCounter = '1 382' WHERE ArticleID = 3443");
        }
        return url;
    }

    private static RevisionAPIConfiguration configuration(final String url)
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration(
                new DatabaseConfiguration("org.hsqldb.jdbcDriver", url, "localhost", DATABASE,
                        "sa", "", Language.simple_english));
        // One article per batch, so that the article index is paged as well
        config.setBufferSize(1);
        return config;
    }

    private static List<String> revisionsOfCar(final String url) throws Exception
    {
        List<String> revisionsOfCar = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            RevisionIterator revisionIterator = new RevisionIterator(config, FIRST_PK, LAST_PK,
                    connection);
            while (revisionIterator.hasNext()) {
                revisionsOfCar.add(describe(revisionIterator.next()));
            }
        }
        assertEquals(LAST_PK - FIRST_PK + 1, revisionsOfCar.size());
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

        List<String> actual = new ArrayList<>();
        ChronoRevisionIterator iterator = new ChronoRevisionIterator(config, connection);
        try {
            while (iterator.hasNext()) {
                actual.add(describe(iterator.next()));
            }
        }
        finally {
            iterator.close();
        }

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

        // The second article covers the first revisions of 'Car' and delivers them in reverse
        // order
        StringBuilder mapping = new StringBuilder();
        for (int revisionCounter = 1; revisionCounter <= MAPPED; revisionCounter++) {
            if (revisionCounter > 1) {
                mapping.append(' ');
            }
            mapping.append(revisionCounter).append(' ').append(MAPPED + 1 - revisionCounter);
        }
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE index_articleID_rc_ts SET RevisionCounter = '1 " + MAPPED
                    + "' WHERE ArticleID = 3443");
            statement.execute("INSERT INTO index_chronological VALUES(3443, '" + mapping + "', '"
                    + mapping + "')");
        }

        List<String> expected = new ArrayList<>(revisionsOfCar);
        for (int i = MAPPED - 1; i >= 0; i--) {
            expected.add(revisionsOfCar.get(i));
        }

        AtomicInteger rangeStatements = new AtomicInteger();
        AtomicInteger rangeQueries = new AtomicInteger();
        Connection connection = countingConnection(DriverManager.getConnection(url, "sa", ""),
                new AtomicInteger(), new AtomicInteger(), new AtomicInteger(), rangeStatements,
                rangeQueries);

        List<String> actual = new ArrayList<>();
        ChronoRevisionIterator iterator = new ChronoRevisionIterator(config, connection);
        try {
            while (iterator.hasNext()) {
                actual.add(describe(iterator.next()));
            }
        }
        finally {
            iterator.close();
        }

        assertEquals(expected, actual);
        assertEquals(1, rangeStatements.get());
        assertTrue(rangeQueries.get() > 1, "Expected evicted revisions to be fetched again");
        assertTrue(connection.isClosed());
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
