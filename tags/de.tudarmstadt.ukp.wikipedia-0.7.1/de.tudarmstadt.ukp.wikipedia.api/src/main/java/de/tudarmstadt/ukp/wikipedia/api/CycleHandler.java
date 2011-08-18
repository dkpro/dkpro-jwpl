/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * Methods for handling cycles in the category graph.
 * @author zesch
 *
 */
public class CycleHandler {

	private final Log logger = LogFactory.getLog(getClass());

    Wikipedia wiki;
    CategoryGraph categoryGraph;

    private enum Color {white, grey, black};
    private Map<Integer, Color> colorMap;

    /**
     * Creates a cycle handler object.
     * @param wiki The Wikipedia object to use.
     * @param categoryGraph The category graph in which cycles should be handeled.
     */
    public CycleHandler(Wikipedia wiki, CategoryGraph categoryGraph) {
        this.wiki = wiki;
        this.categoryGraph = categoryGraph;
    }


//    /**
//     * If there are cylces in the graph, they are resolved.
//     * The JGraphT cycle detector seems not to detect direct cycles (n1 -> n2 -> n1) - so we also call breakDirectCycles.
//     * For each node in a cycle, we determine the minimum path length to the root.
//     * We remove the edge that runs from this deepest node to any other node in the cycle.
//     * If all nodes are of equal depth, we choose an arbitrary node an remove all outgoing edges to nodes in the cycle.
//     * @throws WikiApiException
//     */
//    @Deprecated
//    private void breakCycles() throws WikiApiException {
//        logger.info("Breaking cycles.");
//
//        // get root node
//        Category root = wiki.getMetaData().getMainCategory();
//        int rootID = root.getPageId();
//
//        CycleDetector cycleDetector = new CycleDetector(categoryGraph.getGraph());
//
//        Map<Integer, Integer> pathLengthToRoot = new HashMap<Integer,Integer>();
//
//        while (hasCycles()) {
//            Set<Integer> cycleNodes = cycleDetector.findCycles();
//            logger.info("Number of nodes in cycles: " + cycleNodes.size());
//            Iterator<Integer> it = cycleNodes.iterator();
//            int currentNode = it.next();
//            Set<Integer> currentCycleNodes = cycleDetector.findCyclesContainingVertex(currentNode);
//            // find the node with the highest minimum path length to the root ( this is the deepest node of the cycle)
//            int maxLength = -1;
//            int maxNode = -1;
//            for (int cycleNode : currentCycleNodes) {
//                // get the path length
//                Category cat = wiki.__getCategory(cycleNode);
//                int pathLength = -1;
//                if (!pathLengthToRoot.containsKey(cycleNode)) {
////                    pathLength = categoryGraph.getPathLengthInNodes(rootID, cat.getPageId());
//                    pathLengthToRoot.put(cycleNode, pathLength);
//                }
//                else {
//                    pathLength = pathLengthToRoot.get(cycleNode);
//                }
//
//                // set the maximum
//                if (pathLength >= maxLength) {
//                    maxNode = cycleNode;
//                    maxLength = pathLength;
//                }
//            }
//
//            // maxCat is the deepest category of a cycle
//            // get all outlinks and remove all edges from the graph that point to nodes in the cycle
//            Set<DefaultEdge> outgoingEdges = categoryGraph.getGraph().outgoingEdgesOf(maxNode);
//            Set<DefaultEdge> edgesToRemove = new HashSet<DefaultEdge>();
//            for (DefaultEdge edge : outgoingEdges) {
//                if ((categoryGraph.getGraph().getEdgeSource(edge) == maxNode) && (currentCycleNodes.contains(categoryGraph.getGraph().getEdgeTarget(edge)))) {
//                    edgesToRemove.add(edge);
//
////// I removed this, because it is must always be possible to reconstruct the real data from the category and article object.
////// A category graph is an abstraction from that. It may contain fewer nodes. It may have cycles broken etc.
////// TODO Some algorithms work on the category structure itself instead of the graph. Maybe that should be changed.
////                    Category cat = wiki.getCategory(maxNode);
////                    Set<Integer> outlinks = cat.getOutLinks();
////                    outlinks.remove(categoryGraph.getGraph().getEdgeTarget(edge));
////                    cat.setOutLinks(outlinks);
//                }
//            }
//            categoryGraph.getGraph().removeAllEdges(edgesToRemove);
//        }
//        breakDirectCycles();
//    }

//    /**
//     *  The JGraphT cycle detector seems not to detect direct cycles (n1 -> n2 -> n1).
//     *  Break such direct cycles.
//     */
//    private void breakDirectCycles() {
//        Set<Integer> nodes = categoryGraph.getGraph().vertexSet();
//        Set<DefaultEdge> edgesToRemove = new HashSet<DefaultEdge>();
//        for (int node : nodes) {
//            Set<DefaultEdge> outgoingEdges = categoryGraph.getGraph().outgoingEdgesOf(node);
//            for (DefaultEdge edge : outgoingEdges) {
//                int outNode = categoryGraph.getGraph().getEdgeTarget(edge);
//                if (outNode == node) {
//                    logger.error("Graph contains self edge.");
//                }
//                Set<DefaultEdge> outgoingEdges2 = categoryGraph.getGraph().outgoingEdgesOf(outNode);
//                for (DefaultEdge edge2 : outgoingEdges2) {
//                    if (categoryGraph.getGraph().getEdgeTarget(edge2) == node) {
//                        logger.error("Direct cycle found.");
//                        edgesToRemove.add(edge2);
//                    }
//                }
//            }
//        }
//        categoryGraph.getGraph().removeAllEdges(edgesToRemove);
//    }


//    /**
//     * Detects whether there is a cyle in the graph.
//     */
//    @Deprecated
//    protected boolean hasCycles() {
//        CycleDetector cycleDetector = new CycleDetector(categoryGraph.getGraph());
//        return cycleDetector.detectCycles();
//    }


//// accessed the JGraphT cycle detector, that I have found to be buggy.
//    private void showCycles() throws WikiApiException {
//        CycleDetector cycleDetector = new CycleDetector(categoryGraph.getGraph());
//        Set<Integer> cycleNodes = cycleDetector.findCycles();
//
//        while (!cycleNodes.isEmpty()) {
//            Iterator<Integer> it = cycleNodes.iterator();
//            int currentNode = it.next();
//            Set<Integer> currentCycleNodes = cycleDetector.findCyclesContainingVertex(currentNode);
//
//            for (int cycleNode : currentCycleNodes) {
//                Category cat = wiki.__getCategory(cycleNode);
//                logger.info(cat.getTitle());
//            }
//            logger.info("");
//
//            // remove these nodes from the set of cycle nodes
//            cycleNodes.removeAll(currentCycleNodes);
//        }
//    }


//    /**
//     * The JGraphT cycle detection seems not to find all cycles. Thus, I wrote my own cycle detection.
//     * It is a colored DFS and should find all (viscious :) cycles.
//     * @return
//     */
//    public boolean containsCycle() {
//        colorMap = new HashMap<Integer, Color>();
//        // initialize all nodes with white
//        for (int node : categoryGraph.getGraph().vertexSet()) {
//            colorMap.put(node, Color.white);
//        }
//
//        for (int node : categoryGraph.getGraph().vertexSet()) {
//            if (colorMap.get(node).equals(Color.white)) {
//                if (visit(node)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean visit(int node) {
//        colorMap.put(node, Color.grey);
//        Set<DefaultEdge> outgoingEdges = categoryGraph.getGraph().outgoingEdgesOf(node);
//        for (DefaultEdge edge : outgoingEdges) {
//            int outNode = categoryGraph.getGraph().getEdgeTarget(edge);
//            if (colorMap.get(outNode).equals(Color.grey)) {
////                Category sourceCat = wiki.getCategory(node);
////                Category targetCat = wiki.getCategory(outNode);
////
////                logger.info(sourceCat.getName() + " - " + targetCat.getName());
//
//                return true;
//            }
//            else if (colorMap.get(outNode).equals(Color.white)) {
//                if (visit(outNode)) {
//                    return true;
//                }
//            }
//        }
//        colorMap.put(node, Color.black);
//        return false;
//    }

    /**
     * The JGraphT cycle detection seems not to find all cycles. Thus, I wrote my own cycle detection.
     * It is a colored DFS and should find all (viscious :) cycles.
     * @return True, if the graph contains a cycle.
     * @throws WikiApiException
     */
    public boolean containsCycle() throws WikiApiException  {
        DefaultEdge edge = findCycle();
        if (edge != null) {
            Category sourceCat = wiki.getCategory(categoryGraph.getGraph().getEdgeSource(edge));
            Category targetCat = wiki.getCategory(categoryGraph.getGraph().getEdgeTarget(edge));

            logger.info("Cycle: " + sourceCat.getTitle() + " - " + targetCat.getTitle());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes cycles from the graph that was used to construct the cycle handler.
     * @throws WikiApiException
     */
    public void removeCycles() throws WikiApiException {
        DefaultEdge edge = null;
        while ((edge = findCycle()) != null) {
            Category sourceCat = wiki.getCategory(categoryGraph.getGraph().getEdgeSource(edge));
            Category targetCat = wiki.getCategory(categoryGraph.getGraph().getEdgeTarget(edge));

            logger.info("Removing cycle: " + sourceCat.getTitle() + " - " + targetCat.getTitle());

            categoryGraph.getGraph().removeEdge(edge);
        }
    }

    private DefaultEdge findCycle() {
        colorMap = new HashMap<Integer, Color>();
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

    private DefaultEdge visit(int node) {
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
