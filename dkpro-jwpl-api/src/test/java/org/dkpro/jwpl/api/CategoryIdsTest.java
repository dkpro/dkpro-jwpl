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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link Page#getCategoryIDs()} and {@link Wikipedia#getCategoryIDs(java.util.Collection)}
 * return the page ids of the categories of a page without loading the categories themselves.
 * <p>
 * Queries and transactions are counted via the Hibernate statistics of the shared test session
 * factory. Every test starts with an empty category cache, so a category row that is read from the
 * database shows up in the cache.
 */
public class CategoryIdsTest
    extends BaseJWPLTest
{

    private static final String A_FAMOUS_PAGE = "Wikipedia API";
    private static final String AN_UNCATEGORIZED_PAGE = "Unconnected_page";
    private static final int AN_UNKNOWN_PAGE_ID = 100_000;

    private static final String CATEGORY_ENTITY = org.dkpro.jwpl.api.hibernate.Category.class
            .getName();

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

    @BeforeEach
    public void clearCache()
    {
        wiki.clearCategoryCache();
    }

    /**
     * Runs {@code work} and asserts that it ran exactly {@code expected} statements, each in a
     * transaction of its own, and that it read no category, neither as an entity nor as a row of
     * the category cache.
     */
    private static <T> T assertCost(long expected, Supplier<T> work)
    {
        long statements = statistics.getPrepareStatementCount();
        long transactions = statistics.getTransactionCount();
        long loads = statistics.getEntityStatistics(CATEGORY_ENTITY).getLoadCount();
        T result = work.get();
        assertEquals(expected, statistics.getPrepareStatementCount() - statements, "statements");
        assertEquals(expected, statistics.getTransactionCount() - transactions, "transactions");
        assertEquals(loads, statistics.getEntityStatistics(CATEGORY_ENTITY).getLoadCount(),
                "category entities loaded");
        assertEquals(0, wiki.__getCategoryCache().size(), "category rows read");
        return result;
    }

    private static Set<Integer> pageIdsOf(Set<Category> categories)
    {
        Set<Integer> ids = new HashSet<>();
        for (Category category : categories) {
            ids.add(category.getPageId());
        }
        return ids;
    }

    @Test
    public void testCategoryIdsOfPageNeedOneStatementAndNoCategories() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE);

        Set<Integer> ids = assertCost(1, page::getCategoryIDs);

        assertFalse(ids.isEmpty());
        assertEquals(page.getNumberOfCategories(), ids.size());
        assertEquals(pageIdsOf(page.getCategories()), ids);
    }

    @Test
    public void testCategoryIdsOfPageAreACopy() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE);
        Set<Integer> ids = page.getCategoryIDs();
        Set<Integer> expected = new HashSet<>(ids);

        ids.clear();
        assertEquals(expected, page.getCategoryIDs());
    }

    @Test
    public void testCategoryIdsOfUncategorizedPageAreEmpty() throws Exception
    {
        assertTrue(wiki.getPage(AN_UNCATEGORIZED_PAGE).getCategoryIDs().isEmpty());
    }

    @Test
    public void testCategoryIdsOfManyPagesNeedOneStatementAndNoCategories() throws Exception
    {
        int famous = wiki.getPage(A_FAMOUS_PAGE).getPageId();
        int uncategorized = wiki.getPage(AN_UNCATEGORIZED_PAGE).getPageId();
        // duplicates yield a single entry
        List<Integer> pageIds = Arrays.asList(famous, uncategorized, AN_UNKNOWN_PAGE_ID, famous);

        Map<Integer, Set<Integer>> ids = assertCost(1, () -> wiki.getCategoryIDs(pageIds));

        Map<Integer, Set<Integer>> expected = new HashMap<>();
        expected.put(famous, wiki.getPage(famous).getCategoryIDs());
        expected.put(uncategorized, Set.of());
        assertEquals(expected, ids);
    }

    @Test
    public void testCategoryIdsOfAllPagesAreReadInBatches() throws Exception
    {
        Map<Integer, Set<Integer>> expected = new HashMap<>();
        for (int pageId : wiki.getPageIds()) {
            expected.put(pageId, wiki.getPage(pageId).getCategoryIDs());
        }
        List<Integer> pageIds = new ArrayList<>(expected.keySet());
        // more than two batches of 500 ids, almost all of them unknown
        for (int unknown = AN_UNKNOWN_PAGE_ID; pageIds.size() < 1_200; unknown++) {
            pageIds.add(unknown);
        }

        assertEquals(expected, assertCost(3, () -> wiki.getCategoryIDs(pageIds)));
    }

    @Test
    public void testCategoryIdsOfNoPagesNeedNoStatement()
    {
        assertTrue(assertCost(0, () -> wiki.getCategoryIDs(List.of())).isEmpty());
    }

    @Test
    public void testCategoryIdsOfManyPagesRejectNull()
    {
        assertThrows(IllegalArgumentException.class, () -> wiki.getCategoryIDs(null));
        assertThrows(IllegalArgumentException.class,
                () -> wiki.getCategoryIDs(Arrays.asList(1, null)));
    }
}
