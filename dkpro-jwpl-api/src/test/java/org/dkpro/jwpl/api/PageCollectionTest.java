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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that the link, category and redirect collections of a {@link Page} are read with a
 * single query by the id of the page, without loading the page row (and thus its text) again, and
 * that they contain exactly the rows of the underlying collection tables.
 */
public class PageCollectionTest
    extends BaseJWPLTest
{

    private static final int A_FAMOUS_PAGE_ID = 1017;

    /*
     * Hibernate's built-in pool of the shared test configuration holds 5 connections and fails
     * fast once they are all in use, so stay below that.
     */
    private static final int THREADS = 4;
    private static final int ROUNDS = 25;

    private static Statistics statistics;

    @BeforeAll
    public static void setupWikipedia() throws Exception
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        wiki = new Wikipedia(db);
        statistics = WikiHibernateUtil.getSessionFactory(db).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterAll
    public static void tearDownStatistics()
    {
        statistics.setStatisticsEnabled(false);
    }

    private static long pageLoads()
    {
        EntityStatistics pageStatistics = statistics
                .getEntityStatistics(org.dkpro.jwpl.api.hibernate.Page.class.getName());
        return pageStatistics.getLoadCount();
    }

    private static void assertSingleQueryWithoutPageLoad(Supplier<Set<?>> access)
    {
        // twice, as a collection loaded on the entity itself would only be read once
        for (int i = 0; i < 2; i++) {
            long loads = pageLoads();
            long statements = statistics.getPrepareStatementCount();
            access.get();
            assertEquals(loads, pageLoads(), "the page must not be loaded again");
            assertEquals(statements + 1, statistics.getPrepareStatementCount(),
                    "the collection must be read with a single query");
        }
    }

    @Test
    public void testCollectionAccessDoesNotReloadPage() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE_ID);
        assertFalse(page.getInlinkIDs().isEmpty());
        assertFalse(page.getOutlinkIDs().isEmpty());

        assertSingleQueryWithoutPageLoad(page::getInlinkIDs);
        assertSingleQueryWithoutPageLoad(page::getOutlinkIDs);
        assertSingleQueryWithoutPageLoad(page::getRedirects);
    }

    @Test
    public void testCategoriesDoNotReloadPage() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE_ID);
        assertFalse(page.getCategories().isEmpty());

        long loads = pageLoads();
        page.getCategories();
        assertEquals(loads, pageLoads(), "the page must not be loaded again");
    }

    @Test
    public void testCollectionsMatchTables() throws Exception
    {
        int checked = 0;
        for (Page page : wiki.getPages()) {
            assertEquals(expected(page), Snapshot.of(page));
            // a page loaded on its own, rather than by the iterator, must agree as well
            assertEquals(expected(page), Snapshot.of(wiki.getPage(page.getPageId())));
            checked++;
        }
        assertTrue(checked > 0);
    }

    /**
     * Several threads share one {@link Page} instance and read its collections concurrently.
     */
    @Test
    public void testConcurrentAccessToSharedPage() throws Exception
    {
        Page shared = wiki.getPage(A_FAMOUS_PAGE_ID);
        Snapshot expected = expected(shared);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<Void>> results = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                Callable<Void> task = () -> {
                    start.await();
                    for (int round = 0; round < ROUNDS; round++) {
                        assertEquals(expected, Snapshot.of(shared));
                    }
                    return null;
                };
                results.add(executor.submit(task));
            }
            start.countDown();
            for (Future<Void> result : results) {
                // rethrows any assertion error or exception of the worker
                result.get(2, TimeUnit.MINUTES);
            }
        }
        finally {
            executor.shutdownNow();
        }
    }

    private static Set<Integer> pageIds(Set<Category> categories)
    {
        Set<Integer> ids = new HashSet<>();
        for (Category category : categories) {
            ids.add(category.getPageId());
        }
        return ids;
    }

    /**
     * Reads the collections of a page directly from the collection tables.
     */
    private static Snapshot expected(Page page)
    {
        long id = page.__getId();
        return new Snapshot(column("inLinks", "page_inlinks", id, Integer.class),
                column("outLinks", "page_outlinks", id, Integer.class),
                column("pages", "page_categories", id, Integer.class),
                column("redirects", "page_redirects", id, String.class));
    }

    private static <T> Set<T> column(String column, String table, long id, Class<T> type)
    {
        String sql = "select " + column + " from " + table + " where id = :id";
        return new HashSet<>(wiki.__inTransaction(session -> session
                .createNativeQuery(sql, type).setParameter("id", id, Long.class).list()));
    }

    /**
     * What the collection methods of a page return, reduced to comparable values.
     */
    private record Snapshot(Set<Integer> inlinks, Set<Integer> outlinks,
            Set<Integer> categories, Set<String> redirects)
    {
        static Snapshot of(Page page)
        {
            return new Snapshot(page.getInlinkIDs(), page.getOutlinkIDs(),
                    pageIds(page.getCategories()), page.getRedirects());
        }
    }
}
