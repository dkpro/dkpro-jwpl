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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the paths from each category to the root of a {@link CategoryGraph} built from the test
 * database (see issue #570). The root of the test database is the category 1.
 */
public class CategoryGraphRootPathMapTest
    extends BaseJWPLTest
{

    private static final int ROOT = 1;

    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }
    }

    /**
     * The root path map is written to (and read from) a file in the working directory, which must
     * not leak from one test into another.
     */
    @BeforeEach
    @AfterEach
    public void deleteSavedRootPathMap()
    {
        new File(wiki.getWikipediaId() + "_rootPathMap").delete();
    }

    /**
     * @return The root paths of the category graph of the test database.
     */
    private static Map<Integer, List<Integer>> expectedRootPathMap()
    {
        Map<Integer, List<Integer>> expected = new HashMap<>();
        expected.put(1, List.of(1));
        expected.put(2, List.of(2, 1));
        expected.put(3, List.of(3, 1));
        expected.put(4, List.of(4, 1));
        expected.put(5, List.of(5, 1));
        expected.put(6, List.of(6, 3, 1));
        expected.put(7, List.of(7, 6, 3, 1));
        expected.put(8, List.of(8, 5, 1));
        expected.put(9, List.of(9, 4, 1));
        expected.put(10, List.of(10, 7, 6, 3, 1));
        expected.put(11, List.of(11, 7, 6, 3, 1));
        expected.put(12, List.of(12, 8, 5, 1));
        expected.put(13, List.of(13, 8, 5, 1));
        expected.put(14, List.of(14, 8, 5, 1));
        expected.put(15, List.of(15, 8, 5, 1));
        expected.put(200, List.of(200, 1));
        // the unconnected category has no path to the root
        expected.put(30, List.of());
        return expected;
    }

    @Test
    public void testRootPathMap() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki);

        Map<Integer, List<Integer>> rootPathMap = catGraph.getRootPathMap();

        assertEquals(expectedRootPathMap(), rootPathMap);
        CategoryGraphRootPathTest.assertAreShortestPathsToTheRoot(catGraph.getGraph(), ROOT,
                rootPathMap);
        assertEquals(4, catGraph.getDepth(), 0.00001);
    }

    @Test
    public void testRootPathMapEqualsThatOfThePreviousAlgorithm() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki);

        assertEquals(CategoryGraphRootPathTest.legacyRootPathMap(catGraph.getGraph(), ROOT),
                catGraph.getRootPathMap());
    }

    @Test
    public void testSavedRootPathMapIsLoaded() throws WikiApiException
    {
        new CategoryGraph(wiki).createRootPathMap();

        assertEquals(expectedRootPathMap(), new CategoryGraph(wiki).getRootPathMap());
    }

    @Test
    public void testLCS() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki);

        assertEquals(7, catGraph.getLCSId(10, 11));
        assertEquals(6, catGraph.getLCSId(6, 11));
        assertEquals(1, catGraph.getLCSId(10, 8));
        assertEquals(-1, catGraph.getLCSId(10, 30));
        assertEquals("Projects of UKP", catGraph.getLCS(10, 11).getTitle().getPlainTitle());
        assertNull(catGraph.getLCS(10, 30));
    }

    /**
     * The categories of the wiki that are not part of the graph have no path to the root. The
     * previous algorithm failed on them with an {@link IllegalArgumentException}.
     */
    @Test
    public void testRootPathMapOfAGraphOfASubsetOfTheCategories() throws WikiApiException
    {
        List<Category> categories = new ArrayList<>();
        for (int pageId : new int[] { 1, 3, 6, 7, 10, 11 }) {
            categories.add(wiki.getCategory(pageId));
        }
        CategoryGraph catGraph = new CategoryGraph(wiki, categories);

        Map<Integer, List<Integer>> rootPathMap = catGraph.getRootPathMap();

        Map<Integer, List<Integer>> expected = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : expectedRootPathMap().entrySet()) {
            List<Integer> path = catGraph.getGraph().containsVertex(entry.getKey())
                    ? entry.getValue()
                    : List.of();
            expected.put(entry.getKey(), path);
        }
        assertEquals(expected, rootPathMap);
    }

    /**
     * A graph that is passed to the constructor may still contain cycles. The previous algorithm
     * did not terminate on a cycle it entered before it had found the root.
     */
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void testRootPathMapOfAGraphWithACycle() throws WikiApiException
    {
        DefaultDirectedGraph<Integer, DefaultEdge> fixture = new CategoryGraph(wiki).getGraph();
        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);
        for (int node : fixture.vertexSet()) {
            graph.addVertex(node);
        }
        // closes the cycle 3-6-7-10-3; the edge is added first, so that it is the first parent of 3
        graph.addEdge(10, 3);
        for (DefaultEdge edge : fixture.edgeSet()) {
            graph.addEdge(fixture.getEdgeSource(edge), fixture.getEdgeTarget(edge));
        }
        CategoryGraph catGraph = new CategoryGraph(wiki, graph);

        assertEquals(expectedRootPathMap(), catGraph.getRootPathMap());
    }
}
