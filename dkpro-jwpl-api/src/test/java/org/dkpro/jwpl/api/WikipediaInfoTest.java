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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WikipediaInfoTest
    extends BaseJWPLTest
{

    private static WikipediaInfo wikipediaInfo;

    /**
     * Made this static so that following tests don't run if assumption fails. (With AT_Before,
     * tests also would not be executed but marked as passed) This could be changed back as soon as
     * JUnit ignored tests after failed assumptions
     */
    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        try {
            wiki = new Wikipedia(db);
            wikipediaInfo = new WikipediaInfo(wiki);
        }
        catch (Exception e) {
            fail("WikipediaInfo could not be initialized: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetAverageFanOut() {
        double average = wikipediaInfo.getAverageFanOut();
        assertTrue(average > 0);
        assertEquals(1.1176470588235294d, average);
        //call it twice
        average = wikipediaInfo.getAverageFanOut();
        assertEquals(1.1176470588235294d, average);
    }

    @Test
    public void testGetNumberOfPagesMatchesIteration()
    {
        int iterated = 0;
        for (Page ignored : wiki.getPages()) {
            iterated++;
        }
        assertTrue(iterated > 0);
        assertEquals(iterated, wikipediaInfo.getNumberOfPages());
    }

    @Test
    public void testGetAverageFanOutMatchesLoadedOutlinks()
    {
        double sum = 0;
        for (Page page : wiki.getPages()) {
            sum += page.getOutlinks().size();
        }
        assertEquals(sum / wikipediaInfo.getNumberOfPages(), wikipediaInfo.getAverageFanOut());
    }

    @Test
    public void testGetAverageFanOutForSubset() throws WikiApiException
    {
        List<Page> subset = new ArrayList<>();
        for (Page page : wiki.getPages()) {
            if (subset.size() % 2 == 0 || page.getNumberOfOutlinks() > 0) {
                subset.add(page);
            }
            if (subset.size() == 10) {
                break;
            }
        }

        double sum = 0;
        for (Page page : subset) {
            sum += page.getOutlinks().size();
        }

        WikipediaInfo subsetInfo = new WikipediaInfo(subset, wiki);
        assertEquals(subset.size(), subsetInfo.getNumberOfPages());
        assertTrue(subsetInfo.getAverageFanOut() > 0);
        assertEquals(sum / subset.size(), subsetInfo.getAverageFanOut());
    }

    @Test
    public void testGetArticlesWithOverlappingCategoriesMatchesPairwise() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki);
        Map<Integer, Set<Integer>> categoryArticleMap = getCategoryArticleMap(catGraph);

        // previous pairwise implementation as oracle
        Object[] nodeArray = categoryArticleMap.keySet().toArray();
        Arrays.sort(nodeArray);
        Set<Integer> overlappingArticles = new HashSet<>();
        for (int i = 0; i < nodeArray.length; i++) {
            Set<Integer> outerPages = categoryArticleMap.get((Integer) nodeArray[i]);
            for (int j = i + 1; j < nodeArray.length; j++) {
                Set<Integer> innerPages = categoryArticleMap.get((Integer) nodeArray[j]);
                for (int outerPage : outerPages) {
                    if (innerPages.contains(outerPage)) {
                        overlappingArticles.add(outerPage);
                    }
                }
            }
        }

        assertFalse(overlappingArticles.isEmpty());
        assertEquals(overlappingArticles.size(),
                wikipediaInfo.getArticlesWithOverlappingCategories(wiki, catGraph));
    }

    @Test
    public void testCategoryStatisticsAreComputedLazily() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki);
        Map<Integer, Set<Integer>> categoryArticleMap = getCategoryArticleMap(catGraph);

        Set<Integer> categorizedArticles = new HashSet<>();
        Map<Integer, Integer> distribution = new HashMap<>();
        for (Set<Integer> articles : categoryArticleMap.values()) {
            categorizedArticles.addAll(articles);
            distribution.merge(articles.size(), 1, Integer::sum);
        }

        WikipediaInfo info = new WikipediaInfo(wiki);
        int numberOfCategorizedArticles = info.getNumberOfCategorizedArticles(wiki, catGraph);
        assertTrue(numberOfCategorizedArticles > 0);
        assertEquals(categorizedArticles.size(), numberOfCategorizedArticles);

        Map<Integer, Integer> actualDistribution = info.getDistributionOfArticlesByCategory(wiki,
                catGraph);
        assertFalse(actualDistribution.isEmpty());
        assertEquals(distribution, actualDistribution);
    }

    private static Map<Integer, Set<Integer>> getCategoryArticleMap(CategoryGraph catGraph)
    {
        Map<Integer, Set<Integer>> categoryArticleMap = new HashMap<>();
        for (int node : catGraph.getGraph().vertexSet()) {
            Category cat = wiki.getCategory(node);
            if (cat != null) {
                categoryArticleMap.put(node, new HashSet<>(cat.__getPages()));
            }
        }
        return categoryArticleMap;
    }
}
