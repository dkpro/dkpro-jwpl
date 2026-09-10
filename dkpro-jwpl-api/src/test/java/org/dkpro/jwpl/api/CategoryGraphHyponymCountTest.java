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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * Tests the hyponym counting of {@link CategoryGraph} on graphs that are built in memory, so that
 * shapes a Wikipedia sized category graph has but the test fixture does not - most of all a node
 * reachable over a great many paths - can be asserted without a database.
 */
class CategoryGraphHyponymCountTest
{

    /**
     * Builds a graph over the given edges, where an edge points from a category to one of its
     * children.
     */
    private static CategoryGraph graphOf(int[][] edges)
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);
        for (int[] edge : edges) {
            graph.addVertex(edge[0]);
            graph.addVertex(edge[1]);
            graph.addEdge(edge[0], edge[1]);
        }
        return new CategoryGraph(null, graph);
    }

    @Test
    void countsTheHyponymsOfEachNodeOnceForEveryPathLeadingToIt() throws WikiApiException
    {
        // 1 is the parent of 2 and 3, which are both parents of the leaf 4
        CategoryGraph catGraph = graphOf(new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        Map<Integer, Integer> hyponymCountMap = catGraph.__computeHyponymCountMap();

        assertEquals(0, hyponymCountMap.get(4));
        assertEquals(1, hyponymCountMap.get(2));
        assertEquals(1, hyponymCountMap.get(3));
        // node 4 is reachable over two paths and is counted once per path, which is what makes the
        // counts of the upper nodes of a real category graph grow so quickly
        assertEquals(4, hyponymCountMap.get(1));
    }

    /**
     * A chain of diamonds, in which the count of a node is {@code 4 + 2 * count(next node)}: it
     * doubles per level, so that a graph of less than a hundred nodes reaches counts that no longer
     * fit into an {@code int}. That is the shape which used to turn the counts negative and every
     * measure built on them into -1 (see issue #94).
     */
    @Test
    void capsTheCountsOfAGraphWhoseCountsExceedTheRangeOfAnInt() throws WikiApiException
    {
        final int levels = 40;
        int[][] edges = new int[4 * levels][];
        for (int level = 0; level < levels; level++) {
            int node = 10 * level;
            int left = node + 1;
            int right = node + 2;
            int next = 10 * (level + 1);
            edges[4 * level] = new int[] { node, left };
            edges[4 * level + 1] = new int[] { node, right };
            edges[4 * level + 2] = new int[] { left, next };
            edges[4 * level + 3] = new int[] { right, next };
        }
        CategoryGraph catGraph = graphOf(edges);
        int numberOfNodes = catGraph.getNumberOfNodes();

        Map<Integer, Integer> hyponymCountMap = catGraph.__computeHyponymCountMap();

        assertEquals(numberOfNodes, hyponymCountMap.size());
        for (Map.Entry<Integer, Integer> entry : hyponymCountMap.entrySet()) {
            assertTrue(entry.getValue() >= 0,
                    "Node " + entry.getKey() + " has a negative hyponym count of "
                            + entry.getValue());
            assertTrue(entry.getValue() <= numberOfNodes,
                    "Node " + entry.getKey() + " has more hyponyms (" + entry.getValue()
                            + ") than the graph has nodes (" + numberOfNodes + ")");
        }
        // the root of the chain reaches every other node, so its count is capped
        assertEquals(numberOfNodes - 1, hyponymCountMap.get(0));
        // the leaf of the chain has none
        assertEquals(0, hyponymCountMap.get(10 * levels));
    }
}
