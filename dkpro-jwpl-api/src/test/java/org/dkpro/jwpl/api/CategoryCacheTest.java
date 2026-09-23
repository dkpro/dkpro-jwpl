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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the bounded cache of loaded categories each {@link Wikipedia} instance keeps, see
 * {@link DatabaseConfiguration#setCategoryCacheSize(int)}. Queries are counted via the Hibernate
 * statistics of the shared test session factory.
 */
public class CategoryCacheTest
    extends BaseJWPLTest
{

    private static final String A_FAMOUS_CATEGORY = "People of UKP";
    private static final int A_FAMOUS_PAGE_ID = 8;
    private static final int UKP_PAGE_ID = 6;
    private static final int TELECOOPERATION_PAGE_ID = 1;
    private static final int AN_UNKNOWN_PAGE_ID = 424242;

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

    /**
     * Creates a {@link Wikipedia} for the shared test database with the given cache size, leaving
     * the shared configuration as it was.
     */
    private static Wikipedia wikiWithCacheSize(int size) throws Exception
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        int previous = db.getCategoryCacheSize();
        db.setCategoryCacheSize(size);
        try {
            return new Wikipedia(db);
        }
        finally {
            db.setCategoryCacheSize(previous);
        }
    }

    private static long statements()
    {
        return statistics.getPrepareStatementCount();
    }

    @Test
    public void testDefaultCacheSize()
    {
        assertEquals(1000, DatabaseConfiguration.DEFAULT_CATEGORY_CACHE_SIZE);
        assertEquals(DatabaseConfiguration.DEFAULT_CATEGORY_CACHE_SIZE,
                obtainDbConfiguration().getCategoryCacheSize());
        assertTrue(wiki.__getCategoryCache().isEnabled());
    }

    @Test
    public void testCacheHitRunsNoQuery() throws Exception
    {
        wiki.clearCategoryCache();

        long before = statements();
        Category first = wiki.getCategory(A_FAMOUS_PAGE_ID);
        assertNotNull(first);
        assertTrue(statements() > before, "the first lookup must query the database");

        before = statements();
        Category second = wiki.getCategory(A_FAMOUS_PAGE_ID);
        assertNotNull(second);
        assertEquals(before, statements(), "a cached category must not query the database");

        assertEquals(first.getPageId(), second.getPageId());
        assertEquals(first.__getId(), second.__getId());
        assertEquals(first.getTitle().getPlainTitle(), second.getTitle().getPlainTitle());
        assertEquals(A_FAMOUS_CATEGORY, second.getTitle().getPlainTitle());
    }

    @Test
    public void testLookupByTitleFillsCache() throws Exception
    {
        wiki.clearCategoryCache();
        Category byTitle = wiki.getCategory("UKP");

        long before = statements();
        Category byPageId = wiki.getCategory(UKP_PAGE_ID);
        assertEquals(before, statements());
        assertEquals(byTitle.__getId(), byPageId.__getId());
        assertEquals("UKP", byPageId.getTitle().getPlainTitle());
    }

    @Test
    public void testMissesAreNotCached()
    {
        wiki.clearCategoryCache();

        long before = statements();
        assertNull(wiki.getCategory(AN_UNKNOWN_PAGE_ID));
        assertTrue(statements() > before);

        before = statements();
        assertNull(wiki.getCategory(AN_UNKNOWN_PAGE_ID));
        assertTrue(statements() > before, "a miss must query the database again");
        assertEquals(0, wiki.__getCategoryCache().size());
    }

    @Test
    public void testParentsAreServedFromCache() throws Exception
    {
        wiki.clearCategoryCache();
        Category cat = wiki.getCategory(A_FAMOUS_CATEGORY);
        Set<Category> parents = cat.getParents();
        assertFalse(parents.isEmpty());

        // the second call only reads the link collection, each parent comes from the cache
        long before = statements();
        assertEquals(pageIds(parents), pageIds(cat.getParents()));
        assertEquals(before + 1, statements());
    }

    @Test
    public void testZeroCacheSizeDisablesCache() throws Exception
    {
        Wikipedia uncached = wikiWithCacheSize(0);
        assertFalse(uncached.__getCategoryCache().isEnabled());

        assertNotNull(uncached.getCategory(A_FAMOUS_PAGE_ID));
        uncached.getCategory("UKP");

        long before = statements();
        Category cat = uncached.getCategory(A_FAMOUS_PAGE_ID);
        assertTrue(statements() > before, "a disabled cache must not serve categories");
        assertEquals(A_FAMOUS_CATEGORY, cat.getTitle().getPlainTitle());
        assertEquals(0, uncached.__getCategoryCache().size());
    }

    @Test
    public void testLeastRecentlyUsedCategoryIsEvicted() throws Exception
    {
        Wikipedia small = wikiWithCacheSize(2);
        CategoryCache cache = small.__getCategoryCache();

        small.getCategory(A_FAMOUS_PAGE_ID);
        small.getCategory(UKP_PAGE_ID);
        // touch the older entry, so the other one becomes the least recently used
        small.getCategory(A_FAMOUS_PAGE_ID);
        small.getCategory(TELECOOPERATION_PAGE_ID);

        assertEquals(2, cache.size());
        assertNotNull(cache.get(A_FAMOUS_PAGE_ID));
        assertNotNull(cache.get(TELECOOPERATION_PAGE_ID));
        assertNull(cache.get(UKP_PAGE_ID));

        long before = statements();
        small.getCategory(TELECOOPERATION_PAGE_ID);
        assertEquals(before, statements());

        before = statements();
        assertEquals("UKP", small.getCategory(UKP_PAGE_ID).getTitle().getPlainTitle());
        assertTrue(statements() > before, "an evicted category must be loaded again");
        assertEquals(2, cache.size());
    }

    @Test
    public void testCacheRejectsNegativeSize()
    {
        assertThrows(IllegalArgumentException.class, () -> new CategoryCache(-1));
    }

    @Test
    public void testCachedAndUncachedNavigationAgree() throws Exception
    {
        Wikipedia uncached = wikiWithCacheSize(0);
        Map<Integer, Snapshot> expected = snapshots(uncached);
        assertFalse(expected.isEmpty());

        Wikipedia cached = wikiWithCacheSize(3);
        // twice, so the second pass is served from the (constantly evicting) cache
        assertEquals(expected, snapshots(cached));
        assertEquals(expected, snapshots(cached));
    }

    /**
     * Several threads share one {@link Wikipedia} and one small cache, so entries are evicted and
     * loaded again all the time, and read the link collections of the cached categories.
     */
    @Test
    public void testConcurrentAccess() throws Exception
    {
        Map<Integer, Snapshot> expected = snapshots(wikiWithCacheSize(0));

        for (int size : new int[] { 3, DatabaseConfiguration.DEFAULT_CATEGORY_CACHE_SIZE }) {
            Wikipedia shared = wikiWithCacheSize(size);
            List<Integer> pageIds = new ArrayList<>(expected.keySet());
            CountDownLatch start = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(THREADS);
            try {
                List<Future<Void>> results = new ArrayList<>();
                for (int t = 0; t < THREADS; t++) {
                    List<Integer> order = new ArrayList<>(pageIds);
                    Collections.shuffle(order, new Random(t));
                    Callable<Void> task = () -> {
                        start.await();
                        for (int round = 0; round < ROUNDS; round++) {
                            for (int pageId : order) {
                                assertEquals(expected.get(pageId),
                                        Snapshot.of(shared.getCategory(pageId)));
                            }
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
            assertTrue(shared.__getCategoryCache().size() <= size);
        }
    }

    @Test
    public void testCacheIsPerWikipediaInstance() throws Exception
    {
        Wikipedia other = wikiWithCacheSize(10);
        wiki.clearCategoryCache();
        other.getCategory(A_FAMOUS_PAGE_ID);
        assertEquals(0, wiki.__getCategoryCache().size());
        assertEquals(1, other.__getCategoryCache().size());
        assertSame(other.__getCategoryCache().get(A_FAMOUS_PAGE_ID),
                other.__getCategoryCache().get(A_FAMOUS_PAGE_ID));
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
     * Loads every category of the test database by its page id, together with its relatives.
     */
    private static Map<Integer, Snapshot> snapshots(Wikipedia wikipedia) throws Exception
    {
        List<Integer> pageIds = new ArrayList<>();
        for (Category category : wikipedia.getCategories()) {
            pageIds.add(category.getPageId());
        }
        Map<Integer, Snapshot> snapshots = new HashMap<>();
        for (int pageId : pageIds) {
            snapshots.put(pageId, Snapshot.of(wikipedia.getCategory(pageId)));
        }
        return snapshots;
    }

    /**
     * What a category and its navigation methods return, reduced to comparable values.
     */
    private record Snapshot(long id, int pageId, String title, Set<Integer> parents,
            Set<Integer> children, Set<Integer> pages, Set<Integer> siblings)
    {
        static Snapshot of(Category category) throws Exception
        {
            return new Snapshot(category.__getId(), category.getPageId(),
                    category.getTitle().getPlainTitle(), pageIds(category.getParents()),
                    pageIds(category.getChildren()), category.getArticleIds(),
                    pageIds(category.getSiblings()));
        }
    }
}
