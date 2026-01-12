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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
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

    private Map<Integer, Color> colorMap;

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
        DefaultEdge edge = findCycle();
        if (edge != null) {
            Category sourceCat = wiki.getCategory(categoryGraph.getGraph().getEdgeSource(edge));
            Category targetCat = wiki.getCategory(categoryGraph.getGraph().getEdgeTarget(edge));

            logger.info("Cycle: {} - {}", sourceCat.getTitle(), targetCat.getTitle());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes cycles from the graph that was used to construct the cycle handler.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public void removeCycles() throws WikiApiException
    {
        DefaultEdge edge;
        while ((edge = findCycle()) != null) {
            Category sourceCat = wiki.getCategory(categoryGraph.getGraph().getEdgeSource(edge));
            Category targetCat = wiki.getCategory(categoryGraph.getGraph().getEdgeTarget(edge));

            logger.info("Removing cycle: {} - {}", sourceCat.getTitle(), targetCat.getTitle());

            categoryGraph.getGraph().removeEdge(edge);
        }
    }

    private DefaultEdge findCycle()
    {
        colorMap = new HashMap<>();
        // initialize all nodes with white
        for (int node : categoryGraph.getGraph().vertexSet()) {
            colorMap.put(node, Color.white);
        }

        for (int node : categoryGraph.getGraph().vertexSet()) {
            if (colorMap.get(node).equals(Color.white)) {
                DefaultEdge e = visit(node);
                if (e != null) {
                    return e;
                }
            }
        }
        return null;
    }

    private DefaultEdge visit(int node)
    {
        colorMap.put(node, Color.grey);
        Set<DefaultEdge> outgoingEdges = categoryGraph.getGraph().outgoingEdgesOf(node);
        for (DefaultEdge edge : outgoingEdges) {
            int outNode = categoryGraph.getGraph().getEdgeTarget(edge);
            if (colorMap.get(outNode).equals(Color.grey)) {
                return edge;
            }
            else if (colorMap.get(outNode).equals(Color.white)) {
                DefaultEdge e = visit(outNode);
                if (e != null) {
                    return e;
                }
            }
        }
        colorMap.put(node, Color.black);
        return null;
    }
}
