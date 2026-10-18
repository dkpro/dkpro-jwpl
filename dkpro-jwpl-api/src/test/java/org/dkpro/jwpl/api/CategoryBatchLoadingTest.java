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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks that the category sets of {@link Page} and {@link Category} are loaded in batches rather
 * than with one query per category, that they fill the category cache, and that they hold the
 * same categories as looking each of them up with {@link Wikipedia#getCategory(int)}.
 * <p>
 * Queries are counted via the Hibernate statistics of the shared test session factory. Every test
 * starts with an empty category cache, so each category that is not yet cached has to be read
 * from the database.
 */
public class CategoryBatchLoadingTest
    extends BaseJWPLTest
{

    private static final String A_FAMOUS_CATEGORY = "People of UKP";
    private static final String A_FAMOUS_PAGE = "Wikipedia API";
    private static final String AN_UNCATEGORIZED_PAGE = "Unconnected_page";

    private static final String CATEGORY_ENTITY = org.dkpro.jwpl.api.hibernate.Category.class
            .getName();

    private static Statistics statistics;

    /** Looks every category up on its own, as the category sets did before. */
    private static Wikipedia reference;

    @BeforeAll
    public static void setupWikipedia() throws Exception
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        wiki = new Wikipedia(db);
        reference = new Wikipedia(db);
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

    private static long statements()
    {
        return statistics.getPrepareStatementCount();
    }

    private static long categoryLoads()
    {
        return statistics.getEntityStatistics(CATEGORY_ENTITY).getLoadCount();
    }

    /**
     * Runs {@code work} and asserts that it ran exactly {@code expectedStatements} statements and
     * loaded no category entity.
     */
    private static <T> T assertCost(long expectedStatements, Supplier<T> work)
    {
        long statements = statements();
        long loads = categoryLoads();
        T result = work.get();
        assertEquals(expectedStatements, statements() - statements, "statements");
        assertEquals(loads, categoryLoads(), "category entities loaded");
        return result;
    }

    /**
     * @return The internal id and title of each of the given categories, keyed by page id. Fails
     *         on duplicate page ids.
     */
    private static Map<Integer, String> titles(Set<Category> categories)
        throws WikiTitleParsingException
    {
        Map<Integer, String> titles = new TreeMap<>();
        for (Category category : categories) {
            assertNotNull(category);
            String previous = titles.put(category.getPageId(), describe(category));
            assertNull(previous, "duplicate page id " + category.getPageId());
        }
        return titles;
    }

    /**
     * @return The internal id and title of the categories with the given page ids, looked up one
     *         by one, keyed by page id. Ids without a category are left out.
     */
    private static Map<Integer, String> titlesOneByOne(Set<Integer> pageIds)
        throws WikiTitleParsingException
    {
        Map<Integer, String> titles = new TreeMap<>();
        for (int pageId : pageIds) {
            Category category = reference.getCategory(pageId);
            if (category != null) {
                titles.put(pageId, describe(category));
            }
        }
        return titles;
    }

    private static String describe(Category category) throws WikiTitleParsingException
    {
        return category.__getId() + ":" + category.getTitle().getPlainTitle();
    }

    private static void assertCached(Set<Category> categories)
    {
        for (Category category : categories) {
            assertNotNull(wiki.__getCategoryCache().get(category.getPageId()),
                    "category " + category.getPageId() + " must be cached");
        }
    }

    @Test
    public void testChildrenAreLoadedInOneBatch() throws Exception
    {
        Category category = wiki.getCategory(A_FAMOUS_CATEGORY);
        Set<Integer> ids = category.getChildrenIDs();
        assertTrue(ids.size() > 1);
        wiki.clearCategoryCache();

        // one statement for the ids and one for the categories
        Set<Category> children = assertCost(2, category::getChildren);
        assertEquals(titlesOneByOne(ids), titles(children));
        assertEquals(ids.size(), children.size());
        assertCached(children);

        // the second call only reads the ids, each child comes from the cache
        assertEquals(titles(children), titles(assertCost(1, category::getChildren)));
    }

    @Test
    public void testParentsAreLoadedInOneBatch() throws Exception
    {
        Category category = wiki.getCategory(A_FAMOUS_CATEGORY);
        Set<Integer> ids = category.getParentIDs();
        assertTrue(ids.size() > 1);
        wiki.clearCategoryCache();

        Set<Category> parents = assertCost(2, category::getParents);
        assertEquals(titlesOneByOne(ids), titles(parents));
        assertEquals(ids.size(), parents.size());
        assertCached(parents);
    }

    @Test
    public void testSiblingsAreLoadedWithoutLoadingTheParents() throws Exception
    {
        Category category = wiki.getCategory(A_FAMOUS_CATEGORY);
        Set<Integer> expectedIds = new HashSet<>();
        for (int parentId : category.getParentIDs()) {
            expectedIds.addAll(reference.getCategory(parentId).getChildrenIDs());
        }
        // the category itself is a child of its parents, and it has always been part of the result
        assertTrue(expectedIds.contains(category.getPageId()));
        wiki.clearCategoryCache();

        // the parent ids, the child ids of all parents, and the categories
        Set<Category> siblings = assertCost(3, category::getSiblings);
        assertEquals(titlesOneByOne(expectedIds), titles(siblings));
        assertCached(siblings);
    }

    @Test
    public void testPageCategoriesAreLoadedInOneBatch() throws Exception
    {
        Set<Category> expected = wiki.getPage(A_FAMOUS_PAGE).getCategories();
        assertFalse(expected.isEmpty());

        // Reading the ids of a page costs the same with a warm and a cold cache, so the difference
        // is what loading the categories costs.
        Page warmPage = wiki.getPage(A_FAMOUS_PAGE);
        long statements = statements();
        Set<Category> warm = warmPage.getCategories();
        long idStatements = statements() - statements;

        wiki.clearCategoryCache();
        Page coldPage = wiki.getPage(A_FAMOUS_PAGE);
        Set<Category> cold = assertCost(idStatements + 1, coldPage::getCategories);

        assertEquals(titles(expected), titles(cold));
        assertEquals(titles(expected), titles(warm));
        Set<Integer> ids = new HashSet<>(titles(cold).keySet());
        assertEquals(titlesOneByOne(ids), titles(cold));
        assertCached(cold);
    }

    @Test
    public void testCategoriesOfPageTitleAreLoadedInOneBatch() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE);
        Set<Category> expected = page.getCategories();
        String title = page.getTitle().getWikiStyleTitle();
        wiki.clearCategoryCache();

        // one statement for the ids and one for the categories
        Set<Category> categories = assertCost(2, () -> getCategories(title));
        assertEquals(titles(expected), titles(categories));
        assertCached(categories);
    }

    @Test
    public void testCategoriesOfUncategorizedPageTitleAreEmpty() throws Exception
    {
        // The left join yields a single null here, which used to fail with a NullPointerException.
        assertTrue(wiki.getCategories(AN_UNCATEGORIZED_PAGE).isEmpty());
    }

    @Test
    public void testIdsAreQueriedInBatchesAndUnknownIdsAreLeftOut() throws Exception
    {
        Set<Integer> known = wiki.__getCategories();
        List<Integer> ids = new ArrayList<>(known);
        // more than two batches of 500 ids, almost all of them unknown
        for (int unknown = 100_000; ids.size() < 1_200; unknown++) {
            ids.add(unknown);
        }
        // duplicates yield a single category
        ids.addAll(known);

        Set<Category> categories = assertCost(3, () -> wiki.__getCategoriesByPageIds(ids));
        assertEquals(titlesOneByOne(known), titles(categories));

        // all of them are cached now, so no statement is needed
        assertEquals(titles(categories),
                titles(assertCost(0, () -> wiki.__getCategoriesByPageIds(known))));
    }

    private static Set<Category> getCategories(String pageTitle)
    {
        try {
            return wiki.getCategories(pageTitle);
        }
        catch (WikiApiException e) {
            throw new IllegalStateException(e);
        }
    }
}
