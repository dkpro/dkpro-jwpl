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

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for handling cycles in the category graph.
 *
 * @see CategoryGraph
 */
public class CycleHandler
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    final Wikipedia wiki;
    final CategoryGraph categoryGraph;

    private enum Color
    {
        white, grey, black
    }

    /**
     * Creates a cycle handler object.
     *
     * @param wiki
     *            The {@link Wikipedia} object to use.
     * @param categoryGraph
     *            The category graph in which cycles should be handled.
     */
    public CycleHandler(Wikipedia wiki, CategoryGraph categoryGraph)
    {
        this.wiki = wiki;
        this.categoryGraph = categoryGraph;
    }

    /**
     * The JGraphT cycle detection seems not to find all cycles. Thus, I wrote my own cycle
     * detection. It is a colored DFS and should find all (vicious) cycles.
     *
     * @return True, if the graph contains a cycle.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public boolean containsCycle() throws WikiApiException
    {
        List<int[]> backEdges = findBackEdges(false);
        if (!backEdges.isEmpty()) {
            if (logger.isInfoEnabled()) {
                int[] edge = backEdges.get(0);
                logger.info("Cycle: {} - {}", wiki.getCategory(edge[0]).getTitle(),
                        wiki.getCategory(edge[1]).getTitle());
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes cycles from the graph that was used to construct the cycle handler.
     * <p>
     * A single colored DFS is performed and every back edge is removed as soon as it is found.
     * This removes exactly the edges that restarting the DFS after each removal would remove,
     * because a restart would repeat the identical traversal prefix and then continue with the
     * next outgoing edge of the same vertex.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public void removeCycles() throws WikiApiException
    {
        List<int[]> removedEdges = findBackEdges(true);
        if (logger.isDebugEnabled()) {
            for (int[] edge : removedEdges) {
                logger.debug("Removing cycle: {} - {}", wiki.getCategory(edge[0]).getTitle(),
                        wiki.getCategory(edge[1]).getTitle());
            }
        }
        logger.info("Removed {} cycle edges.", removedEdges.size());
    }

    /**
     * Performs an iterative colored DFS over the category graph, visiting vertices and outgoing
     * edges in the order provided by the graph.
     *
     * @param remove
     *            If {@code true}, every back edge is removed from the graph when found and the
     *            traversal continues. If {@code false}, the traversal stops at the first back edge
     *            and the graph is left unchanged.
     * @return The back edges found, as {@code (source, target)} pairs of page ids.
     */
    private List<int[]> findBackEdges(boolean remove)
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph = categoryGraph.getGraph();
        Map<Integer, Color> colorMap = new HashMap<>();
        // initialize all nodes with white
        for (Integer node : graph.vertexSet()) {
            colorMap.put(node, Color.white);
        }

        List<int[]> backEdges = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();
        for (Integer root : graph.vertexSet()) {
            if (colorMap.get(root) != Color.white) {
                continue;
            }
            colorMap.put(root, Color.grey);
            stack.push(new Frame(root, new ArrayList<>(graph.outgoingEdgesOf(root))));
            while (!stack.isEmpty()) {
                Frame frame = stack.peek();
                if (frame.pos == frame.edges.size()) {
                    // all children are finished
                    colorMap.put(frame.node, Color.black);
                    stack.pop();
                    continue;
                }
                DefaultEdge edge = frame.edges.get(frame.pos++);
                Integer target = graph.getEdgeTarget(edge);
                Color targetColor = colorMap.get(target);
                if (targetColor == Color.grey) {
                    backEdges.add(new int[] { frame.node, target });
                    if (!remove) {
                        return backEdges;
                    }
                    graph.removeEdge(edge);
                }
                else if (targetColor == Color.white) {
                    colorMap.put(target, Color.grey);
                    stack.push(new Frame(target, new ArrayList<>(graph.outgoingEdgesOf(target))));
                }
            }
        }
        return backEdges;
    }

    /**
     * A vertex on the DFS stack together with a snapshot of its outgoing edges and the position
     * of the next edge to examine.
     */
    private static final class Frame
    {
        private final int node;
        private final List<DefaultEdge> edges;
        private int pos;

        private Frame(int node, List<DefaultEdge> edges)
        {
            this.node = node;
            this.edges = edges;
        }
    }
}
