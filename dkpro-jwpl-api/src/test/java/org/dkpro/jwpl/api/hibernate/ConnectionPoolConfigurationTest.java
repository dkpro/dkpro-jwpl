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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests that the size of Hibernate's built-in connection pool follows
 * {@link DatabaseConfiguration#getConnectionPoolSize()}, that a caller-supplied
 * {@code hibernate.connection.pool_size} still takes precedence, and that the built-in pool fails
 * fast once exhausted - which is the behaviour the documentation warns about.
 * <p>
 * Dedicated in-memory HSQLDB databases are used, so that neither the shared test fixture nor its
 * cached {@link SessionFactory} are affected. None of the tests reads a single row.
 */
public class ConnectionPoolConfigurationTest
{

    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String POOL_SIZE = "hibernate.connection.pool_size";

    private static DatabaseConfiguration config(String database)
    {
        return new DatabaseConfiguration(DRIVER, "jdbc:hsqldb:mem:" + database, "localhost",
                database, "sa", "", Language._test);
    }

    private static String poolSizeOf(DatabaseConfiguration config)
    {
        return WikiHibernateUtil.getConfiguration(config, MetaData.class).getProperties()
                .getProperty(POOL_SIZE);
    }

    @Test
    public void testDefaultPoolSizeIsUnchanged()
    {
        DatabaseConfiguration config = config("jwpl_pool_props");
        assertEquals(DatabaseConfiguration.DEFAULT_CONNECTION_POOL_SIZE,
                config.getConnectionPoolSize());
        assertEquals("5", poolSizeOf(config));
    }

    @Test
    public void testConfiguredPoolSizeIsApplied()
    {
        DatabaseConfiguration config = config("jwpl_pool_props");
        config.setConnectionPoolSize(8);
        assertEquals("8", poolSizeOf(config));
    }

    @Test
    public void testHibernatePropertyOverridesPoolSize()
    {
        DatabaseConfiguration config = config("jwpl_pool_props");
        config.setConnectionPoolSize(8);
        config.setHibernateProperty(POOL_SIZE, "3");
        assertEquals("3", poolSizeOf(config));
    }

    @Test
    public void testConfiguredPoolServesAsManyConcurrentTransactions()
    {
        int size = DatabaseConfiguration.DEFAULT_CONNECTION_POOL_SIZE + 3;
        DatabaseConfiguration config = config("jwpl_pool_large");
        config.setConnectionPoolSize(size);
        SessionFactory sessionFactory = WikiHibernateUtil.getSessionFactory(config);

        List<Session> sessions = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                sessions.add(beginTransaction(sessionFactory));
            }
            assertEquals(size, sessions.size());
        }
        finally {
            closeAll(sessions);
        }
    }

    @Test
    public void testDefaultPoolFailsFastWhenExhausted()
    {
        SessionFactory sessionFactory = WikiHibernateUtil
                .getSessionFactory(config("jwpl_pool_default"));

        List<Session> sessions = new ArrayList<>();
        try {
            for (int i = 0; i < DatabaseConfiguration.DEFAULT_CONNECTION_POOL_SIZE; i++) {
                sessions.add(beginTransaction(sessionFactory));
            }
            Session exceeding = sessionFactory.openSession();
            sessions.add(exceeding);
            assertThrows(HibernateException.class, exceeding::beginTransaction);
        }
        finally {
            closeAll(sessions);
        }
    }

    /**
     * Opens a session and begins a transaction on it, which checks a connection out of the pool
     * until the transaction completes.
     */
    private static Session beginTransaction(SessionFactory sessionFactory)
    {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        return session;
    }

    private static void closeAll(List<Session> sessions)
    {
        for (Session session : sessions) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            session.close();
        }
    }
}
