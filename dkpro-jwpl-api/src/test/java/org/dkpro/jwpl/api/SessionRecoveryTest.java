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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Covers the recovery of the thread-bound Hibernate session after a failing unit of work.
 * <p>
 * JWPL binds a session to the current thread, and such a session is unbound and closed only once
 * its transaction completes. A transaction left open by a failing query therefore used to leak the
 * session <i>and</i> the JDBC connection it holds: every later call on that thread failed with
 * {@code IllegalStateException: Transaction already active}, and a handful of failures exhausted
 * the connection pool for the whole JVM.
 *
 * @see org.dkpro.jwpl.api.hibernate.WikiHibernateUtil#inTransaction
 */
public class SessionRecoveryTest
    extends BaseJWPLTest
{

    private static final int A_FAMOUS_PAGE_ID = 1017;

    /** Fails at execution time on every supported backend. */
    private static final String FAILING_QUERY = "select * from NoSuchTableAtAll";

    /**
     * More failures than either connection pool JWPL configures holds -
     * {@code hibernate.connection.pool_size} is 5, {@code hibernate.c3p0.max_size} is 15.
     */
    private static final int MORE_THAN_THE_POOL_HOLDS = 25;

    @BeforeAll
    public static void setupWikipedia() throws Exception
    {
        wiki = new Wikipedia(obtainDbConfiguration());
    }

    @Test
    public void testFailedQueryClosesTheThreadBoundSession()
    {
        AtomicReference<Session> used = new AtomicReference<>();

        assertThrows(RuntimeException.class, () -> wiki.__inTransaction(session -> {
            used.set(session);
            return session.createNativeQuery(FAILING_QUERY, Object.class).list();
        }));

        assertFalse(used.get().isOpen(),
                "A failed unit of work must not leave its session open - it holds a JDBC "
                        + "connection until the transaction completes.");
        assertNotSame(used.get(), wiki.__getHibernateSession(),
                "The failed session must have been unbound from this thread.");
    }

    @Test
    public void testThreadStaysUsableAfterAFailedQuery()
    {
        assertThrows(RuntimeException.class, () -> wiki
                .__inTransaction(session -> session.createNativeQuery(FAILING_QUERY, Object.class)
                        .list()));

        // Used to throw IllegalStateException: Transaction already active.
        assertTrue(wiki.existsPage(A_FAMOUS_PAGE_ID),
                "An unrelated call after a failed query must still work on the same thread.");
    }

    @Test
    public void testRepeatedFailuresDoNotExhaustTheConnectionPool()
    {
        for (int i = 0; i < MORE_THAN_THE_POOL_HOLDS; i++) {
            assertThrows(RuntimeException.class,
                    () -> wiki.__inTransaction(session -> session
                            .createNativeQuery(FAILING_QUERY, Object.class).list()));
        }

        // Used to fail with "The internal connection pool has reached its maximum size and no
        // connection is currently available" once the pool ran dry.
        assertTrue(wiki.existsPage(A_FAMOUS_PAGE_ID),
                "Failed queries must return their connections to the pool.");
    }
}
