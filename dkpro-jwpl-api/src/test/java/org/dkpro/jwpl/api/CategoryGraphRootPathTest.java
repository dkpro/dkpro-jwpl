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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the computation of the paths from each node to the root of a {@link CategoryGraph} on
 * graphs that are built in memory, and compares it with the depth-first search that was used
 * before (see issue #570).
 */
class CategoryGraphRootPathTest
{

    /**
     * Builds a graph over the given edges, where an edge points from a category to one of its
     * children. The edges are added in the given order, which is the order in which the parents of
     * a node are visited.
     */
    private static DefaultDirectedGraph<Integer, DefaultEdge> graphOf(int[][] edges)
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);
        for (int[] edge : edges) {
            graph.addVertex(edge[0]);
            graph.addVertex(edge[1]);
            graph.addEdge(edge[0], edge[1]);
        }
        return graph;
    }

    @Test
    void mapsEachNodeToItsShortestPathToTheRoot()
    {
        // 1 is the root; 5 can be reached over 1-2-4-5 and over the shorter 1-3-5
        DefaultDirectedGraph<Integer, DefaultEdge> graph = graphOf(
                new int[][] { { 1, 2 }, { 2, 4 }, { 4, 5 }, { 1, 3 }, { 3, 5 }, { 6, 7 } });

        Map<Integer, List<Integer>> rootPathMap = new CategoryGraph(null, graph)
                .__computeRootPathMap(1);

        assertEquals(List.of(1), rootPathMap.get(1));
        assertEquals(List.of(2, 1), rootPathMap.get(2));
        assertEquals(List.of(3, 1), rootPathMap.get(3));
        assertEquals(List.of(4, 2, 1), rootPathMap.get(4));
        assertEquals(List.of(5, 3, 1), rootPathMap.get(5));
        // 6 and 7 are not connected to the root
        assertEquals(List.of(), rootPathMap.get(6));
        assertEquals(List.of(), rootPathMap.get(7));
        assertEquals(graph.vertexSet().size(), rootPathMap.size());
    }

    @Test
    void takesTheFirstParentOnAShortestPathIfThereIsMoreThanOne()
    {
        // 4 has the two shortest paths 4-3-1 and 4-2-1; the edge from 3 is added first
        DefaultDirectedGraph<Integer, DefaultEdge> graph = graphOf(
                new int[][] { { 1, 2 }, { 1, 3 }, { 3, 4 }, { 2, 4 }, { 4, 5 } });

        Map<Integer, List<Integer>> rootPathMap = new CategoryGraph(null, graph)
                .__computeRootPathMap(1);

        assertEquals(List.of(4, 3, 1), rootPathMap.get(4));
        assertEquals(List.of(5, 4, 3, 1), rootPathMap.get(5));
        assertEquals(legacyRootPathMap(graph, 1), rootPathMap);
    }

    @Test
    void mapsAllNodesToAnEmptyPathIfTheRootIsNotPartOfTheGraph()
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = graphOf(new int[][] { { 1, 2 } });

        Map<Integer, List<Integer>> rootPathMap = new CategoryGraph(null, graph)
                .__computeRootPathMap(42);

        assertEquals(Map.of(1, List.of(), 2, List.of()), rootPathMap);
    }

    /**
     * The depth-first search that was used before did not terminate on a graph with a cycle that
     * it enters before it has found the root.
     */
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void terminatesOnAGraphWithACycle()
    {
        // 2, 3 and 4 form a cycle, and the edge from 4 to 2 comes before the edge from the root
        DefaultDirectedGraph<Integer, DefaultEdge> graph = graphOf(
                new int[][] { { 4, 2 }, { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 } });

        Map<Integer, List<Integer>> rootPathMap = new CategoryGraph(null, graph)
                .__computeRootPathMap(1);

        assertEquals(List.of(2, 1), rootPathMap.get(2));
        assertEquals(List.of(3, 2, 1), rootPathMap.get(3));
        assertEquals(List.of(4, 3, 2, 1), rootPathMap.get(4));
        assertEquals(List.of(5, 4, 3, 2, 1), rootPathMap.get(5));
    }

    /**
     * On random acyclic graphs with many nodes that have more than one shortest path to the root,
     * the breadth-first search yields the same paths as the depth-first search it replaces.
     */
    @Test
    void yieldsTheSamePathsAsThePreviousAlgorithmOnRandomAcyclicGraphs()
    {
        Random random = new Random(570);
        for (int i = 0; i < 300; i++) {
            DefaultDirectedGraph<Integer, DefaultEdge> graph = randomAcyclicGraph(random);
            Map<Integer, List<Integer>> rootPathMap = new CategoryGraph(null, graph)
                    .__computeRootPathMap(0);

            assertEquals(legacyRootPathMap(graph, 0), rootPathMap);
            assertAreShortestPathsToTheRoot(graph, 0, rootPathMap);
        }
    }

    /**
     * Builds a random acyclic graph with the root 0, in which an edge always points from a lower to
     * a higher node, and whose edges are added in a random order.
     */
    private static DefaultDirectedGraph<Integer, DefaultEdge> randomAcyclicGraph(Random random)
    {
        int numberOfNodes = 2 + random.nextInt(30);
        double edgeProbability = 0.05 + random.nextDouble() * 0.3;
        List<int[]> edges = new ArrayList<>();
        for (int target = 1; target < numberOfNodes; target++) {
            for (int source = 0; source < target; source++) {
                if (random.nextDouble() < edgeProbability) {
                    edges.add(new int[] { source, target });
                }
            }
        }
        Collections.shuffle(edges, random);

        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);
        for (int node = 0; node < numberOfNodes; node++) {
            graph.addVertex(node);
        }
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        return graph;
    }

    /**
     * Asserts that each non-empty path starts with its node, ends with the root, follows the edges
     * of the graph and is a shortest path, and that each node without a path is not reachable from
     * the root.
     */
    static void assertAreShortestPathsToTheRoot(DefaultDirectedGraph<Integer, DefaultEdge> graph,
            int root, Map<Integer, List<Integer>> rootPathMap)
    {
        BFSShortestPath<Integer, DefaultEdge> shortestPaths = new BFSShortestPath<>(graph);
        for (int node : graph.vertexSet()) {
            List<Integer> path = rootPathMap.get(node);
            GraphPath<Integer, DefaultEdge> shortestPath = shortestPaths.getPath(root, node);
            if (shortestPath == null) {
                assertEquals(List.of(), path, "path of " + node);
                continue;
            }
            assertEquals(node, path.get(0).intValue(), "path of " + node);
            assertEquals(root, path.get(path.size() - 1).intValue(), "path of " + node);
            for (int k = 0; k + 1 < path.size(); k++) {
                assertTrue(graph.containsEdge(path.get(k + 1), path.get(k)), "path of " + node);
            }
            assertEquals(shortestPath.getLength(), path.size() - 1, "path of " + node);
        }
    }

    /**
     * The computation of the root paths as it was done before issue #570: a depth-first search from
     * each node up to the root, starting with the leaf nodes, where the nodes on a path found get
     * the rest of the path as their path. It is kept here to compare the results with.
     */
    static Map<Integer, List<Integer>> legacyRootPathMap(
            DefaultDirectedGraph<Integer, DefaultEdge> graph, int root)
    {
        Map<Integer, List<Integer>> rootPathMap = new HashMap<>();
        Deque<Integer> queue = new ArrayDeque<>();
        for (int node : graph.vertexSet()) {
            if (graph.outDegreeOf(node) == 0) {
                queue.add(node);
            }
        }
        legacyFillRootPathMap(graph, root, queue, rootPathMap);
        queue.addAll(graph.vertexSet());
        legacyFillRootPathMap(graph, root, queue, rootPathMap);
        return rootPathMap;
    }

    private static void legacyFillRootPathMap(DefaultDirectedGraph<Integer, DefaultEdge> graph,
            int root, Deque<Integer> queue, Map<Integer, List<Integer>> rootPathMap)
    {
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            if (rootPathMap.containsKey(currentNode)) {
                continue;
            }
            List<Integer> shortestPath = new ArrayList<>();
            legacyExpandPath(graph, root, currentNode, new LinkedList<>(), shortestPath);
            if (shortestPath.isEmpty()) {
                rootPathMap.put(currentNode, new ArrayList<>());
                continue;
            }
            int i = 0;
            for (int nodeOnPath : shortestPath) {
                if (rootPathMap.containsKey(nodeOnPath)) {
                    continue;
                }
                else {
                    rootPathMap.put(nodeOnPath,
                            new ArrayList<>(shortestPath.subList(i, shortestPath.size())));
                }
                i++;
            }
        }
    }

    private static void legacyExpandPath(DefaultDirectedGraph<Integer, DefaultEdge> graph,
            int root, int currentNode, List<Integer> currentPath, List<Integer> shortestPath)
    {
        currentPath.add(currentNode);
        if (currentNode == root) {
            if (shortestPath.isEmpty() || currentPath.size() < shortestPath.size()) {
                shortestPath.clear();
                shortestPath.addAll(currentPath);
            }
        }
        if (!shortestPath.isEmpty() && currentPath.size() >= shortestPath.size()) {
            return;
        }
        for (DefaultEdge incomingEdge : graph.incomingEdgesOf(currentNode)) {
            List<Integer> savedPath = new LinkedList<>(currentPath);
            legacyExpandPath(graph, root, graph.getEdgeSource(incomingEdge), currentPath,
                    shortestPath);
            currentPath.clear();
            currentPath.addAll(savedPath);
        }
    }
}
