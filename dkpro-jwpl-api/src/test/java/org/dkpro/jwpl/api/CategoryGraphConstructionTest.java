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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.CategoryGraphData.LoadMode;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks that a {@link CategoryGraph} built from bulk loaded category data is identical to one
 * built the way it was before, from one {@link Category} object per node, and that the bulk
 * loading takes a constant number of queries instead of several per category.
 */
public class CategoryGraphConstructionTest
    extends BaseJWPLTest
{

    private static final int NOT_A_CATEGORY = 1017;

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
    public void clearCategoryCache()
    {
        wiki.clearCategoryCache();
    }

    private static Set<Integer> allCategories()
    {
        return wiki.__getCategories();
    }

    private static Set<Integer> ukpSubset() throws WikiApiException
    {
        // the descendants of 'UKP' plus 'UKP' itself, 'Telecooperation' and an unconnected one
        Set<Integer> pageIds = new HashSet<>();
        for (Category category : wiki.getCategory("UKP").getDescendants()) {
            pageIds.add(category.getPageId());
        }
        pageIds.add(wiki.getCategory("UKP").getPageId());
        pageIds.add(wiki.getCategory("Telecooperation").getPageId());
        pageIds.add(wiki.getCategory("Unconnected category").getPageId());
        return pageIds;
    }

    static Stream<Arguments> builds() throws WikiApiException
    {
        List<String> filter = List.of("People", "SIR", "Unconnected");
        List<Arguments> builds = new ArrayList<>();
        for (LoadMode mode : LoadMode.values()) {
            builds.add(Arguments.of("full", allCategories(), null, mode));
            builds.add(Arguments.of("full, filtered", allCategories(), filter, mode));
            builds.add(Arguments.of("subset", ukpSubset(), null, mode));
            builds.add(Arguments.of("subset, filtered", ukpSubset(), filter, mode));
        }
        return builds.stream();
    }

    @ParameterizedTest(name = "{0} ({3})")
    @MethodSource("builds")
    public void testGraphIsIdenticalToThePerCategoryConstruction(String name, Set<Integer> pageIds,
            List<String> filter, LoadMode mode)
        throws WikiApiException
    {
        DefaultDirectedGraph<Integer, DefaultEdge> expected = buildPerCategory(pageIds, filter);
        CategoryGraphData data = CategoryGraphData.load(wiki, pageIds, filter != null, mode);
        DefaultDirectedGraph<Integer, DefaultEdge> actual = CategoryGraph.buildGraph(wiki, pageIds,
                filter, data);

        assertFalse(expected.edgeSet().isEmpty());
        assertEquals(new ArrayList<>(expected.vertexSet()), new ArrayList<>(actual.vertexSet()));
        assertEquals(edgeList(expected), edgeList(actual));

        // the edge order decides which edges the cycle removal drops, so compare after it as well
        CategoryGraph expectedGraph = new CategoryGraph(wiki, expected);
        new CycleHandler(wiki, expectedGraph).removeCycles();
        CategoryGraph actualGraph = new CategoryGraph(wiki, actual);
        new CycleHandler(wiki, actualGraph).removeCycles();
        assertEquals(edgeList(expectedGraph.getGraph()), edgeList(actualGraph.getGraph()));
    }

    @Test
    public void testConstructorsBuildTheSameGraphAsThePerCategoryConstruction()
        throws WikiApiException
    {
        List<String> filter = List.of("People");
        assertSameAsPerCategory(new CategoryGraph(wiki), allCategories(), null);
        assertSameAsPerCategory(new CategoryGraph(wiki, filter), allCategories(), filter);

        List<Category> subset = new ArrayList<>();
        for (int pageId : ukpSubset()) {
            subset.add(wiki.getCategory(pageId));
        }
        Set<Integer> subsetIds = new HashSet<>();
        for (Category category : subset) {
            subsetIds.add(category.getPageId());
        }
        assertSameAsPerCategory(new CategoryGraph(wiki, subset), subsetIds, null);
        assertSameAsPerCategory(new CategoryGraph(wiki, subset, filter), subsetIds, filter);
    }

    private void assertSameAsPerCategory(CategoryGraph actual, Set<Integer> pageIds,
            List<String> filter)
        throws WikiApiException
    {
        CategoryGraph expected = new CategoryGraph(wiki, buildPerCategory(pageIds, filter));
        new CycleHandler(wiki, expected).removeCycles();

        assertEquals(new ArrayList<>(expected.getGraph().vertexSet()),
                new ArrayList<>(actual.getGraph().vertexSet()));
        assertEquals(edgeList(expected.getGraph()), edgeList(actual.getGraph()));
        assertEquals(expected.getGraph().edgeSet().size(), actual.getNumberOfEdges());
        assertEquals(expected.getGraph().vertexSet().size(), actual.getNumberOfNodes());
    }

    @ParameterizedTest
    @MethodSource("modes")
    public void testLoadRunsAConstantNumberOfStatementsInOneTransaction(LoadMode mode)
    {
        Set<Integer> pageIds = allCategories();

        long statementsBefore = statistics.getPrepareStatementCount();
        long transactionsBefore = statistics.getTransactionCount();
        CategoryGraphData.load(wiki, pageIds, true, mode);

        // one query each for the category rows, the in-links and the out-links
        assertEquals(3, statistics.getPrepareStatementCount() - statementsBefore);
        assertEquals(1, statistics.getTransactionCount() - transactionsBefore);
    }

    static Stream<LoadMode> modes()
    {
        return Stream.of(LoadMode.values());
    }

    @Test
    public void testConstructionIsNotLinearInTheNumberOfCategories() throws WikiApiException
    {
        Set<Integer> pageIds = allCategories();

        long before = statistics.getPrepareStatementCount();
        buildPerCategory(pageIds, List.of("People"));
        long perCategory = statistics.getPrepareStatementCount() - before;

        wiki.clearCategoryCache();
        before = statistics.getPrepareStatementCount();
        CategoryGraph.buildGraph(wiki, pageIds, List.of("People"),
                CategoryGraphData.load(wiki, pageIds, true));
        long bulk = statistics.getPrepareStatementCount() - before;

        // one query per category for its row, two per node for its link sets
        assertTrue(perCategory > 2L * pageIds.size(), "per category: " + perCategory);
        assertEquals(3, bulk);
    }

    @Test
    public void testChunkedLoadRunsOneQueryPerTableAndChunk()
    {
        Set<Integer> pageIds = new HashSet<>(allCategories());
        // page ids without a category: they add to the chunks, but not to the loaded rows
        for (int i = 0; pageIds.size() <= CategoryGraphData.CHUNK_SIZE; i++) {
            pageIds.add(-1 - i);
        }

        long before = statistics.getPrepareStatementCount();
        CategoryGraphData data = CategoryGraphData.load(wiki, pageIds, false, LoadMode.CHUNKED);

        // two chunks of category rows, one chunk of each link table
        assertEquals(4, statistics.getPrepareStatementCount() - before);
        assertEquals(allCategories().size(),
                pageIds.stream().filter(pageId -> data.getId(pageId) != null).count());
    }

    @Test
    public void testLoadOfNoCategoriesRunsNoQuery()
    {
        long before = statistics.getPrepareStatementCount();
        CategoryGraphData data = CategoryGraphData.load(wiki, Set.of(), false);
        assertEquals(0, statistics.getPrepareStatementCount() - before);
        assertNull(data.getId(1));
    }

    @ParameterizedTest
    @MethodSource("modes")
    public void testUnknownPageIdIsRejected(LoadMode mode) throws WikiApiException
    {
        Set<Integer> pageIds = new HashSet<>(allCategories());
        pageIds.add(NOT_A_CATEGORY);

        for (List<String> filter : Arrays.asList(null, List.of("X"))) {
            CategoryGraphData data = CategoryGraphData.load(wiki, pageIds, filter != null, mode);
            WikiApiException e = assertThrows(WikiApiException.class,
                    () -> CategoryGraph.buildGraph(wiki, pageIds, filter, data));
            assertEquals(NOT_A_CATEGORY + " is not a valid pageID", e.getMessage());
        }
        assertThrows(WikiApiException.class,
                () -> new CategoryGraph(wiki, pageIds));
    }

    @Test
    public void testLinkSetsMatchTheCategoryObjects() throws WikiApiException
    {
        Set<Integer> pageIds = allCategories();
        CategoryGraphData data = CategoryGraphData.load(wiki, pageIds, true, LoadMode.FULL_SCAN);
        for (int pageId : pageIds) {
            Category category = new Category(wiki, pageId);
            long id = data.getId(pageId);
            assertEquals(category.__getId(), id);
            assertEquals(category.getTitle().getWikiStyleTitle(),
                    new Title(data.getName(pageId)).getWikiStyleTitle());
            assertEquals(new ArrayList<>(category.getParentIDs()),
                    new ArrayList<>(data.getParentIDs(id)));
            assertEquals(new ArrayList<>(category.getChildrenIDs()),
                    new ArrayList<>(data.getChildrenIDs(id)));
        }
        assertNotEquals(0, pageIds.size());
    }

    /**
     * The construction as it was before the category data was loaded in bulk: one
     * {@link Category} object per node, and its link sets loaded one category at a time.
     */
    private static DefaultDirectedGraph<Integer, DefaultEdge> buildPerCategory(
            Set<Integer> pPageIDs, List<String> filterList)
        throws WikiApiException
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);
        for (int pageID : pPageIDs) {
            if (filterList != null) {
                String title = loadCategory(pageID).getTitle().getPlainTitle();
                if (filterList.stream().anyMatch(title::startsWith)) {
                    continue;
                }
            }
            graph.addVertex(pageID);
        }
        for (int pageID : graph.vertexSet()) {
            Category cat = loadCategory(pageID);
            Set<Integer> inLinks = cat.getParentIDs();
            Set<Integer> outLinks = cat.getChildrenIDs();
            for (int inLink : inLinks) {
                if (graph.vertexSet().contains(inLink) && inLink != pageID) {
                    graph.addEdge(inLink, pageID);
                }
            }
            for (int outLink : outLinks) {
                if (graph.vertexSet().contains(outLink) && outLink != pageID) {
                    graph.addEdge(pageID, outLink);
                }
            }
        }
        return graph;
    }

    private static Category loadCategory(int pageID) throws WikiApiException
    {
        try {
            return new Category(wiki, pageID);
        }
        catch (WikiPageNotFoundException e) {
            throw new WikiApiException(pageID + " is not a valid pageID", e);
        }
    }

    private static List<List<Integer>> edgeList(DefaultDirectedGraph<Integer, DefaultEdge> graph)
    {
        Set<List<Integer>> edges = new LinkedHashSet<>();
        for (DefaultEdge edge : graph.edgeSet()) {
            edges.add(List.of(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)));
        }
        return new ArrayList<>(edges);
    }
}
