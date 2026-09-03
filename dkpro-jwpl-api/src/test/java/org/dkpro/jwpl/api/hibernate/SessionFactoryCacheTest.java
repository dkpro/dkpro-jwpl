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
package org.dkpro.jwpl.api.hibernate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that the JVM-wide session factory cache honours the derived key: configurations carrying
 * equal values share one {@link SessionFactory}, configurations differing in a connection-affecting
 * field do not, and concurrent first-time access builds exactly one {@link SessionFactory}.
 * <p>
 * Dedicated in-memory databases are used here, so that neither the shared test fixture nor its
 * cached {@link SessionFactory} are affected. HSQLDB is deliberately hardcoded rather than taken
 * from {@code JwplTestDatabase}: it is a test scoped dependency of every module and so is present
 * whichever backend the surrounding suite runs against, and none of the assertions below read a
 * single row - an empty schema is all these factories ever need.
 */
public class SessionFactoryCacheTest
{

    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String DB = "jwpl_cache";
    private static final String URL = "jdbc:hsqldb:mem:" + DB;
    /** Same logical database, reached via a second JDBC url - the very case issue #494 reports. */
    private static final String ALTERNATE_URL = "jdbc:hsqldb:mem:jwpl_cache_alt";
    private static final String RACE_DB = "jwpl_cache_race";

    private static final String SECOND_USER = "jwpl";
    private static final String SECOND_PASSWORD = "jwpl";

    private static final int THREADS = 8;

    @BeforeAll
    public static void createSecondUser() throws SQLException
    {
        // A second set of credentials valid against the very same database, so that the credential
        // dimension of the key can be exercised with two factories that both actually build.
        try (Connection connection = DriverManager.getConnection(URL, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE USER \"" + SECOND_USER + "\" PASSWORD '" + SECOND_PASSWORD
                    + "' ADMIN");
        }
    }

    private static DatabaseConfiguration config()
    {
        return new DatabaseConfiguration(DRIVER, URL, "localhost", DB, "sa", "", Language._test);
    }

    @Test
    public void testDistinctButEqualConfigurationsShareOneSessionFactory()
    {
        SessionFactory a = WikiHibernateUtil.getSessionFactory(config());
        SessionFactory b = WikiHibernateUtil.getSessionFactory(config());

        assertNotNull(a);
        assertSame(a, b);
    }

    @Test
    public void testDifferingJdbcUrlYieldsDistinctSessionFactories()
    {
        DatabaseConfiguration other = config();
        other.setJdbcURL(ALTERNATE_URL);

        assertNotSame(WikiHibernateUtil.getSessionFactory(config()),
                WikiHibernateUtil.getSessionFactory(other));
    }

    @Test
    public void testDifferingCredentialsYieldDistinctSessionFactories()
    {
        DatabaseConfiguration other = config();
        other.setUser(SECOND_USER);
        other.setPassword(SECOND_PASSWORD);

        assertNotSame(WikiHibernateUtil.getSessionFactory(config()),
                WikiHibernateUtil.getSessionFactory(other));
    }

    @Test
    public void testMutatingAConfigurationBackRestoresItsOriginalSessionFactory()
    {
        DatabaseConfiguration config = config();
        SessionFactory original = WikiHibernateUtil.getSessionFactory(config);

        config.setJdbcURL(ALTERNATE_URL);
        assertNotSame(original, WikiHibernateUtil.getSessionFactory(config));

        config.setJdbcURL(URL);
        assertSame(original, WikiHibernateUtil.getSessionFactory(config));
    }

    @Test
    public void testMetaDataEntityStaysInLockstepWithItsSessionFactory()
    {
        DatabaseConfiguration config = config();

        // The entity is resolved from the very same cache entry as the factory, so asking for it
        // must neither return null nor build a second factory.
        SessionFactory sessionFactory = WikiHibernateUtil.getSessionFactory(config);
        assertSame(MetaData.class, WikiHibernateUtil.getMetaDataEntityClass(config));
        assertSame(sessionFactory, WikiHibernateUtil.getSessionFactory(config));
    }

    @Test
    public void testConcurrentFirstTimeAccessBuildsOneSessionFactory() throws Exception
    {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREADS);
        List<SessionFactory> obtained = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                try {
                    startSignal.await();
                    obtained.add(WikiHibernateUtil.getSessionFactory(raceConfig()));
                }
                catch (Throwable e) {
                    failures.add(e);
                }
                finally {
                    doneSignal.countDown();
                }
            });
            t.setDaemon(true);
            t.start();
        }

        startSignal.countDown();
        assertTrue(doneSignal.await(2, TimeUnit.MINUTES), "Threads did not finish in time.");
        assertTrue(failures.isEmpty(), "Obtaining a SessionFactory failed: " + failures);

        SessionFactory expected = WikiHibernateUtil.getSessionFactory(raceConfig());
        for (SessionFactory sessionFactory : obtained) {
            assertSame(expected, sessionFactory);
        }
    }

    private static DatabaseConfiguration raceConfig()
    {
        return new DatabaseConfiguration(DRIVER, "jdbc:hsqldb:mem:" + RACE_DB, "localhost", RACE_DB,
                "sa", "", Language._test);
    }
}
