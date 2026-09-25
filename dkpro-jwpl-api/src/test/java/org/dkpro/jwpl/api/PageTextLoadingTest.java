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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that navigating links, category members and the text-free page iterator loads the pages
 * without their text, in batches, and that {@link Page#getText()} of such a page still returns the
 * same text as a page loaded with it.
 * <p>
 * Whether the text was loaded is told by the number of {@code Page} entities Hibernate loaded:
 * the text-free paths select the metadata columns as scalars and load no entity.
 */
public class PageTextLoadingTest
    extends BaseJWPLTest
{

    private static final String A_FAMOUS_PAGE = "Wikipedia API";

    private static final String PAGE_ENTITY = org.dkpro.jwpl.api.hibernate.Page.class.getName();

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
        return statistics.getEntityStatistics(PAGE_ENTITY).getLoadCount();
    }

    private record Cost(long pageLoads, long statements)
    {
    }

    private static <T> T measure(Supplier<T> work, Cost[] cost)
    {
        long loads = pageLoads();
        long statements = statistics.getPrepareStatementCount();
        T result = work.get();
        cost[0] = new Cost(pageLoads() - loads, statistics.getPrepareStatementCount() - statements);
        return result;
    }

    /**
     * Asserts that loading the given pages cost one statement more than reading their ids, and no
     * page entity beyond what reading the ids costs by itself (which reattaches the source entity).
     * The two suppliers must work on different instances of the source page, as reading the ids
     * of an instance may initialize its collection for good.
     */
    private static void assertLoadedInOneBatchWithoutText(Supplier<Set<Integer>> ids,
            Supplier<Set<Page>> pages)
        throws Exception
    {
        Cost[] idCost = new Cost[1];
        Set<Integer> expectedIds = measure(ids, idCost);
        Cost[] pageCost = new Cost[1];
        Set<Page> actual = measure(pages, pageCost);

        assertFalse(actual.isEmpty());
        assertEquals(idCost[0].pageLoads(), pageCost[0].pageLoads(), "page entities loaded");
        assertEquals(idCost[0].statements() + 1, pageCost[0].statements(), "statements");

        assertSameAsFullyLoaded(actual);
        assertEquals(expectedIds.size(), actual.size());
    }

    /**
     * Asserts that the given pages hold the same data and text as the pages loaded one by one with
     * their text, and that the text of a page loaded without it is queried once and then cached.
     */
    private static void assertSameAsFullyLoaded(Iterable<Page> pages) throws Exception
    {
        for (Page page : pages) {
            Page full = wiki.getPage(page.getPageId());
            assertEquals(full.__getId(), page.__getId());
            assertEquals(full.getTitle().getPlainTitle(), page.getTitle().getPlainTitle());
            assertEquals(full.isDisambiguation(), page.isDisambiguation());

            long loads = pageLoads();
            long transactions = statistics.getTransactionCount();
            String text = page.getText();
            assertNotNull(text);
            assertEquals(full.getText(), text);
            assertEquals(loads, pageLoads(), "getText() must not load the page entity");
            assertEquals(transactions + 1, statistics.getTransactionCount());

            transactions = statistics.getTransactionCount();
            assertEquals(text, page.getText());
            assertEquals(transactions, statistics.getTransactionCount(),
                    "the text must be cached after the first call");
        }
    }

    /**
     * @return A new instance of the famous page. Loading it costs one entity load and one
     *         statement, which the callers of {@link #measure(Supplier, Cost[])} pay on both sides.
     */
    private static Page famousPage()
    {
        try {
            return wiki.getPage(A_FAMOUS_PAGE);
        }
        catch (WikiApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testGetInlinksLoadsNoText() throws Exception
    {
        assertLoadedInOneBatchWithoutText(() -> famousPage().getInlinkIDs(),
                () -> famousPage().getInlinks());
    }

    @Test
    public void testGetOutlinksLoadsNoText() throws Exception
    {
        assertLoadedInOneBatchWithoutText(() -> famousPage().getOutlinkIDs(),
                () -> famousPage().getOutlinks());
    }

    @Test
    public void testCategoryGetArticlesLoadsNoText() throws Exception
    {
        Category category = wiki.getCategory("UKP");
        assertLoadedInOneBatchWithoutText(category::getArticleIds, () -> {
            try {
                return category.getArticles();
            }
            catch (WikiApiException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    public void testCollectionsOfPageLoadedWithoutText() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE);
        for (Page link : page.getInlinks()) {
            Page full = wiki.getPage(link.getPageId());
            assertEquals(full.getInlinkIDs(), link.getInlinkIDs());
            assertEquals(full.getOutlinkIDs(), link.getOutlinkIDs());
            assertEquals(full.getRedirects(), link.getRedirects());
            assertEquals(pageIds(full.getCategories()), pageIds(link.getCategories()));
            assertEquals(full.getNumberOfInlinks(), link.getNumberOfInlinks());
            assertEquals(full.getNumberOfOutlinks(), link.getNumberOfOutlinks());
            assertEquals(full.getNumberOfCategories(), link.getNumberOfCategories());
        }
    }

    @Test
    public void testPageIteratorWithoutTextLoadsNoText() throws Exception
    {
        assertIteratorWithoutText(false);
    }

    @Test
    public void testArticleIteratorWithoutTextLoadsNoText() throws Exception
    {
        assertIteratorWithoutText(true);
    }

    private static void assertIteratorWithoutText(boolean onlyArticles) throws Exception
    {
        Map<Integer, String> expected = new TreeMap<>();
        for (Page page : new PageIterable(wiki, onlyArticles, 2)) {
            expected.put(page.getPageId(), page.getTitle() + "|" + page.isDisambiguation());
        }
        assertFalse(expected.isEmpty());

        long loads = pageLoads();
        Map<Integer, String> actual = new TreeMap<>();
        // A buffer smaller than the number of pages, so that several buffers are filled.
        PageIterator iterator = new PageIterator(wiki, onlyArticles, 2, false);
        List<Page> pages = new ArrayList<>();
        while (iterator.hasNext()) {
            Page page = iterator.next();
            pages.add(page);
            actual.put(page.getPageId(), page.getTitle() + "|" + page.isDisambiguation());
        }
        assertEquals(loads, pageLoads(), "page entities loaded");
        assertEquals(expected, actual);

        assertSameAsFullyLoaded(pages);
    }

    @Test
    public void testPageIterableWithTextStillLoadsText() throws Exception
    {
        long loads = pageLoads();
        int count = 0;
        for (Page page : new PageIterable(wiki, false, true)) {
            count++;
            long transactions = statistics.getTransactionCount();
            assertNotNull(page.getText());
            assertEquals(transactions, statistics.getTransactionCount());
        }
        assertEquals(loads + count, pageLoads(), "page entities loaded");
    }

    @Test
    public void testFullyLoadedPageGetTextRunsNoQuery() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE);
        long transactions = statistics.getTransactionCount();
        assertNotNull(page.getText());
        assertEquals(transactions, statistics.getTransactionCount());
    }

    private static Set<Integer> pageIds(Set<Category> categories)
    {
        Set<Integer> ids = new HashSet<>();
        for (Category category : categories) {
            ids.add(category.getPageId());
        }
        return ids;
    }
}
