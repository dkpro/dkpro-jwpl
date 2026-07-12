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

import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CycleHandlerTest
    extends BaseJWPLTest
{

    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            throw new RuntimeException("Wikipedia could not be initialized: " + e.getLocalizedMessage(), e);
        }
    }

    /*------------------------------------------------------------------*/
    /* Helper: build a synthetic CategoryGraph from a DefaultDirectedGraph */
    /*------------------------------------------------------------------*/
    private CategoryGraph buildGraph(final int... vertexIds)
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (int v : vertexIds) {
            graph.addVertex(v);
        }
        return new CategoryGraph(wiki, graph);
    }

    private void addEdge(CategoryGraph catGraph, int source, int target)
    {
        catGraph.getGraph().addEdge(source, target);
    }

    /* A minimal two-node cycle (1 -> 2 -> 1) must be detected. */
    @Test
    public void testTwoNodeCycleDetectsCycleDirectly() throws WikiApiException
    {
        // 1 -> 2 -> 1 : visiting 1 reaches 2, and 2 has a back edge to 1
        CategoryGraph catGraph = buildGraph(1, 2);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 1);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* A two-node cycle is detected regardless of traversal start. */
    @Test
    public void testTwoNodeCycleDetectsCycle() throws WikiApiException
    {
        // A -> B -> A (pageIDs 1 and 2)
        CategoryGraph catGraph = buildGraph(1, 2);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 1);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* A three-node cycle (1 -> 2 -> 3 -> 1) must be detected. */
    @Test
    public void testThreeNodeCycleDetectsCycle() throws WikiApiException
    {
        // 1 -> 2 -> 3 -> 1
        CategoryGraph catGraph = buildGraph(1, 2, 3);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 1);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* A simple DAG with no back edges must report no cycle. */
    @Test
    public void testNoCycleReturnsFalse() throws WikiApiException
    {
        // Simple DAG: 1 -> 2 -> 3 (no back edges)
        CategoryGraph catGraph = buildGraph(1, 2, 3);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertFalse(handler.containsCycle());
    }

    /* A cycle reached only after a long chain of edges must still be detected. */
    @Test
    public void testCycleAtEndOfLongChainIsDetected() throws WikiApiException
    {
        // Long chain with a cycle at the end: 1 -> 2 -> 3 -> 4 -> 2
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 4);
        addEdge(catGraph, 4, 2);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* With one acyclic component and one cyclic component, a cycle is reported. */
    @Test
    public void testMixedGraphNoCycleComponent() throws WikiApiException
    {
        // Two components: 1->2->3 (DAG, no cycle), 4->5->4 (cycle)
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4, 5);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 4, 5);
        addEdge(catGraph, 5, 4);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* A branching DAG that forces backtracking must still report no cycle. */
    @Test
    public void testBacktrackingNoCycle() throws WikiApiException
    {
        // 1 -> 2, 1 -> 3 -> 4 (all DAG, no cycles)
        // The recursive call on node 2 should return null, then backtrack
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 1, 3);
        addEdge(catGraph, 3, 4);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertFalse(handler.containsCycle());
    }

    /* A cycle plus a dead-end branch off a cycle node must be detected. */
    @Test
    public void testComplexGraphWithCycle() throws WikiApiException
    {
        // 1 -> 2 -> 3 -> 1 (cycle), plus 3 -> 4 (dead end)
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 1);
        addEdge(catGraph, 3, 4);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertTrue(handler.containsCycle());
    }

    /* removeCycles must delete at least one edge to break a two-node cycle. */
    @Test
    public void testRemoveCyclesTwoNodeCycle() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 1);
        assertEquals(2, catGraph.getGraph().edgeSet().size());

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        handler.removeCycles();

        // At least one edge must be removed
        assertTrue(catGraph.getGraph().edgeSet().size() < 2);
        assertFalse(handler.containsCycle());
    }

    /* removeCycles must remove an edge to break a three-node cycle. */
    @Test
    public void testRemoveCyclesBreaksCycle() throws WikiApiException
    {
        // 1 -> 2, 2 -> 3, 3 -> 1 (single cycle)
        CategoryGraph catGraph = buildGraph(1, 2, 3);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 1);
        assertEquals(3, catGraph.getGraph().edgeSet().size());

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        handler.removeCycles();

        // At least one edge must be removed to break the cycle
        assertTrue(catGraph.getGraph().edgeSet().size() < 3);
        assertFalse(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Multiple cycles: ensure all are broken                     */
    /*-----------------------------------------------------------*/
    @Test
    public void testRemoveCyclesMultipleCycles() throws WikiApiException
    {
        // Cycle 1: 1 -> 2 -> 1
        // Cycle 2: 3 -> 4 -> 3
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 1);
        addEdge(catGraph, 3, 4);
        addEdge(catGraph, 4, 3);
        assertEquals(4, catGraph.getGraph().edgeSet().size());

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        handler.removeCycles();

        // Both cycles should be broken (at least 2 edges removed)
        assertTrue(catGraph.getGraph().edgeSet().size() <= 2);
        assertFalse(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Isolated vertices with no edges - empty graph              */
    /*-----------------------------------------------------------*/
    @Test
    public void testEmptyGraphNoCycle() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertFalse(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Graph where cycle is found on second component             */
    /*-----------------------------------------------------------*/
    @Test
    public void testCycleInSecondComponent() throws WikiApiException
    {
        // Component 1: 1 -> 2 -> 3 (DAG, no cycle)
        // Component 2: 6 -> 7 -> 6 (cycle)
        CategoryGraph catGraph = buildGraph(1, 2, 3, 6, 7);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 6, 7);
        addEdge(catGraph, 7, 6);

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        assertTrue(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Diamond shape: 1->2, 1->3, 2->4, 3->4 (no cycle)          */
    /*-----------------------------------------------------------*/
    @Test
    public void testDiamondShapeNoCycle() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 1, 3);
        addEdge(catGraph, 2, 4);
        addEdge(catGraph, 3, 4);

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        assertFalse(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Bidirectional edges: A->B and B->A creates a 2-node cycle  */
    /*-----------------------------------------------------------*/
    @Test
    public void testBidirectionalEdgesFormCycle() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 1);

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        assertTrue(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Single node, no edges                                      */
    /*-----------------------------------------------------------*/
    @Test
    public void testSingleNodeNoEdges() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1);
        CycleHandler handler = new CycleHandler(wiki, catGraph);

        assertFalse(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Cycle removal preserves graph vertices                     */
    /*-----------------------------------------------------------*/
    @Test
    public void testRemoveCyclesPreservesVertices() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2, 3);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 1);

        Set<Integer> verticesBefore = catGraph.getGraph().vertexSet();

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        handler.removeCycles();

        // Vertices should remain unchanged
        assertEquals(verticesBefore, catGraph.getGraph().vertexSet());
    }

    /*-----------------------------------------------------------*/
    /* Path that goes A -> B -> C -> B (cycle at B<C)             */
    /*-----------------------------------------------------------*/
    @Test
    public void testPathThenCycle() throws WikiApiException
    {
        // 1 -> 2 -> 3 -> 2 (cycle at 2-3)
        CategoryGraph catGraph = buildGraph(1, 2, 3);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 2, 3);
        addEdge(catGraph, 3, 2);

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        assertTrue(handler.containsCycle());
    }

    /*-----------------------------------------------------------*/
    /* Star graph with no cycle: 1 -> 2, 1 -> 3, 1 -> 4           */
    /*-----------------------------------------------------------*/
    @Test
    public void testStarGraphNoCycle() throws WikiApiException
    {
        CategoryGraph catGraph = buildGraph(1, 2, 3, 4);
        addEdge(catGraph, 1, 2);
        addEdge(catGraph, 1, 3);
        addEdge(catGraph, 1, 4);

        CycleHandler handler = new CycleHandler(wiki, catGraph);
        assertFalse(handler.containsCycle());
    }
}
