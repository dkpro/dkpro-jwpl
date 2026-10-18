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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.util.ApiUtilities;
import org.dkpro.jwpl.api.util.CommonUtilities;
import org.dkpro.jwpl.api.util.GraphSerialization;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The category graph is constructed from the links connecting Wikipedia categories. It provides
 * various accessors and graph algorithms.
 */
public class CategoryGraph
    implements WikiConstants, Serializable
{

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long serialVersionUID = 1L;

    /**
     * The wikipedia object
     */
    private Wikipedia wiki;

    /**
     * The category graph, directed.
     */
    private DefaultDirectedGraph<Integer, DefaultEdge> graph;

    /**
     * The category graph, undirected.
     */
    private AsUndirectedGraph<Integer, DefaultEdge> undirectedGraph;

    /**
     * A map holding the degree distribution of the graph
     */
    private Map<Integer, Integer> degreeDistribution;

    /**
     * Number of nodes in the graph
     */
    private int numberOfNodes;

    /**
     * Number of edges in the graph
     */
    private int numberOfEdges;

    /**
     * A map holding the (recursive) number of hyponyms for each node.
     * Recursive means that the hyponyms of hyponyms are also taken into account.
     */
    private Map<Integer, Integer> hyponymCountMap = null;

    /**
     * A mapping from all nodes to a list of nodes on the path to the root
     */
    private Map<Integer, List<Integer>> rootPathMap = null;

    /**
     * The average shortest path length. Initially: {@link Double#NEGATIVE_INFINITY}.
     */
    private double averageShortestPathLength = Double.NEGATIVE_INFINITY;

    /**
     * The diameter of a category graph. Initially: {@link Double#NEGATIVE_INFINITY}.
     */
    private double diameter = Double.NEGATIVE_INFINITY;

    /**
     * The average degree of a category graph. Initially: {@link Double#NEGATIVE_INFINITY}.
     */
    private double averageDegree = Double.NEGATIVE_INFINITY;

    /**
     * The cluster coefficient of a category graph. Initially: {@link Double#NEGATIVE_INFINITY}.
     */
    private double clusterCoefficient = Double.NEGATIVE_INFINITY;

    /**
     * The depth of a category graph. Initially: {@link Double#NEGATIVE_INFINITY}.
     */
    private double depth = Double.NEGATIVE_INFINITY;

    /**
     * Whether the graph parameters (average shortest path length, diameter, average degree,
     * cluster coefficient and degree distribution) have already been computed.
     */
    private boolean graphParametersComputed = false;

    /**
     * Creates an {@link CategoryGraph} using a serialized DirectedGraph object.
     *
     * @param pWiki
     *            A {@link Wikipedia} object.
     * @param location
     *            The location of the serialized graph
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph(Wikipedia pWiki, File location) throws WikiApiException
    {
        try {
            constructCategoryGraph(pWiki, GraphSerialization.loadGraph(location));
        }
        catch (IOException | ClassNotFoundException e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Creates a {@link CategoryGraph} object using all categories of the given Wikipedia.
     *
     * @param pWiki
     *            A {@link Wikipedia} object.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph(Wikipedia pWiki) throws WikiApiException
    {
        constructCategoryGraph(pWiki, pWiki.__getCategories(), null);
    }

    /**
     * Creates a CategoryGraph object using all categories, but filters all categories starting with
     * strings contained in the filterList.
     *
     * @param pWiki
     *            The Wikipedia object.
     * @param filterList
     *            A list of strings. All categories starting with or matching such a string are not
     *            added to the category graph.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph(Wikipedia pWiki, List<String> filterList) throws WikiApiException
    {
        constructCategoryGraph(pWiki, pWiki.__getCategories(), filterList);
    }

    /**
     * Creates a CategoryGraph object using the categories given by the iterable
     *
     * @param pWiki
     *            The Wikipedia object.
     * @param categories
     *            An iterable of the categories to use for construction of the category graph.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph(Wikipedia pWiki, Iterable<Category> categories) throws WikiApiException
    {
        Set<Integer> pageIDs = new HashSet<>();
        for (Category category : categories) {
            pageIDs.add(category.getPageId());
        }
        constructCategoryGraph(pWiki, pageIDs, null);
    }

    /**
     * Creates a CategoryGraph object using the categories given by the iterable, but filters all
     * categories starting with strings contained in the filterList
     *
     * @param pWiki
     *            The Wikipedia object.
     * @param categories
     *            An iterable of the categories to use for construction of the category graph.
     * @param filterList
     *            A list of strings. All categories starting with or matching such a string are not
     *            added to the category graph.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph(Wikipedia pWiki, Iterable<Category> categories, List<String> filterList)
        throws WikiApiException
    {
        Set<Integer> pageIDs = new HashSet<>();
        for (Category category : categories) {
            pageIDs.add(category.getPageId());
        }
        constructCategoryGraph(pWiki, pageIDs, filterList);
    }

    /**
     * Creates a category graph using a subset (that may also be the full set) of the categories.
     *
     * @param pWiki
     *            The wiki object.
     * @param pPageIDs
     *            A set of pageIDs of the category pages that should be used to build the category
     *            graph.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected CategoryGraph(Wikipedia pWiki, Set<Integer> pPageIDs) throws WikiApiException
    {
        constructCategoryGraph(pWiki, pPageIDs, null);
    }

    /**
     * Creates a category graph using a subset (that may also be the full set) of the categories.
     *
     * @param pWiki
     *            The wiki object.
     * @param pGraph A valid {@link DefaultDirectedGraph} representation.
     */
    public CategoryGraph(Wikipedia pWiki, DefaultDirectedGraph<Integer, DefaultEdge> pGraph)
    {
        constructCategoryGraph(pWiki, pGraph);
    }

    private void constructCategoryGraph(Wikipedia pWiki,
            DefaultDirectedGraph<Integer, DefaultEdge> pGraph)
    {
        this.wiki = pWiki;
        this.graph = pGraph;
        this.numberOfNodes = this.graph.vertexSet().size();
        this.numberOfEdges = this.graph.edgeSet().size();
        this.undirectedGraph = new AsUndirectedGraph<>(this.graph);
    }

    private void constructCategoryGraph(Wikipedia pWiki, Set<Integer> pPageIDs,
            List<String> filterList)
        throws WikiApiException
    {
        wiki = pWiki;

        // create the graph as a directed Graph
        // algorithms that need to be called on an undirected graph or should ignore direction
        // can be called on an AsUndirectedGraph view of the directed graph
        graph = buildGraph(pWiki, pPageIDs, filterList,
                CategoryGraphData.load(pWiki, pPageIDs, filterList != null));
        numberOfNodes = graph.vertexSet().size();
        numberOfEdges = graph.edgeSet().size();

        logger.info("Added {} nodes.", this.getNumberOfNodes());
        logger.info("Added {} edges.", this.getNumberOfEdges());

        CycleHandler cycleHandler = new CycleHandler(wiki, this);
        logger.info("Graph contains cycles: {}", cycleHandler.containsCycle());
        cycleHandler.removeCycles();
        logger.info("Graph contains cycles: {}", cycleHandler.containsCycle());

        this.numberOfEdges = this.graph.edgeSet().size();
        this.undirectedGraph = new AsUndirectedGraph<>(this.graph);

    }

    /**
     * Builds the category graph over the given categories, before any cycles are removed.
     *
     * @param pWiki
     *            The wiki object, only used to look up titles for debug logging.
     * @param pPageIDs
     *            The page ids of the categories to add as nodes.
     * @param filterList
     *            A list of strings. All categories starting with such a string are not added to
     *            the graph. May be {@code null}.
     * @param data
     *            The category rows and links of (at least) the given categories. The names must
     *            have been loaded if {@code filterList} is not {@code null}.
     * @return The graph, with its nodes and edges in the order the per-category loading produced.
     * @throws WikiApiException
     *             Thrown if a page id is not the page id of a category, or if errors occurred.
     */
    static DefaultDirectedGraph<Integer, DefaultEdge> buildGraph(Wikipedia pWiki,
            Set<Integer> pPageIDs, List<String> filterList, CategoryGraphData data)
        throws WikiApiException
    {
        DefaultDirectedGraph<Integer, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(
                DefaultEdge.class);

        for (int pageID : pPageIDs) {
            if (filterList != null) {
                requireId(data, pageID);
                if (matchesFilter(new Title(data.getName(pageID)), filterList)) {
                    continue;
                }
            }

            directedGraph.addVertex(pageID);
        }

        // add edges
        int progress = 0;
        for (int pageID : directedGraph.vertexSet()) {
            progress++;
            ApiUtilities.printProgressInfo(progress, pPageIDs.size(), 10,
                    ApiUtilities.ProgressInfoMode.TEXT, "Adding edges");

            long id = requireId(data, pageID);

            // get parents and children
            // if the corresponding nodes are in the graph (it could be a subset) => add them to the
            // graph
            Set<Integer> inLinks = data.getParentIDs(id);
            Set<Integer> outLinks = data.getChildrenIDs(id);

            // add edges
            // If an edge already exits, it is silently ignored by JGraphT. So we do not have to
            // check this.
            for (int inLink : inLinks) {
                if (directedGraph.vertexSet().contains(inLink)) {
                    if (inLink == pageID) {
                        logSelfLoop(pWiki, data, pageID);
                    }
                    else {
                        directedGraph.addEdge(inLink, pageID);
                    }
                }
            }
            for (int outLink : outLinks) {
                if (directedGraph.vertexSet().contains(outLink)) {
                    if (outLink == pageID) {
                        logSelfLoop(pWiki, data, pageID);
                    }
                    else {
                        directedGraph.addEdge(pageID, outLink);
                    }
                }
            }
        }
        return directedGraph;
    }

    /**
     * @return The hibernate id of the category with the given page id.
     * @throws WikiApiException
     *             Thrown if there is no category with the given page id.
     */
    private static long requireId(CategoryGraphData data, int pageID) throws WikiApiException
    {
        Long id = data.getId(pageID);
        if (id == null) {
            throw new WikiApiException(pageID + " is not a valid pageID");
        }
        return id;
    }

    private static void logSelfLoop(Wikipedia pWiki, CategoryGraphData data, int pageID)
        throws WikiApiException
    {
        if (logger.isDebugEnabled()) {
            String name = data.getName(pageID);
            Title title = name != null ? new Title(name) : pWiki.getCategory(pageID).getTitle();
            logger.debug("Self-loop for node {} ({})", pageID, title);
        }
    }

    /**
     * Checks whether the category title matches the filter (a filter matches a string, if the
     * string starts with the filter expression).
     *
     * @param title
     *            The title of a category.
     * @param filterList
     *            A list of filter strings.
     * @return True, if the category title starts with or is equal to a string in the filter list.
     *         False, otherwise.
     * @throws WikiTitleParsingException
     *             Thrown if errors occurred.
     */
    private static boolean matchesFilter(Title title, List<String> filterList)
        throws WikiTitleParsingException
    {
        String categoryTitle = title.getPlainTitle();
        for (String filter : filterList) {
            if (categoryTitle.startsWith(filter)) {
                logger.info("{} starts with {} => removing", categoryTitle, filter);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the lowest common subsumer (LCS) of two nodes. The LCS of two nodes is first node on the
     * path to the root, that has both nodes as sons. Nodes that are not in the same connected
     * component as the root node are defined to have no LCS.
     *
     * @param category1
     *            The first category node.
     * @param category2
     *            The second category node.
     * @return The lowest common subsumer of the two nodes, or {@code null} if there is no LCS.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Category getLCS(Category category1, Category category2) throws WikiApiException
    {
        return getLCS(category1.getPageId(), category2.getPageId());
    }

    /**
     * Gets the lowest common subsumer (LCS) of two nodes. The LCS of two nodes is first node on the
     * path to the root, that has both nodes as sons. Nodes that are not in the same connected
     * component as the root node are defined to have no LCS.
     *
     * @param categoryPageId1
     *            The pageid of the first category node.
     * @param categoryPageId2
     *            The pageid of the second category node.
     * @return The pageId of the lowest common subsumer of the two nodes, or {@code null} if there is no
     *         LCS.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public int getLCSId(int categoryPageId1, int categoryPageId2) throws WikiApiException
    {

        // TODO here might be a problem concerning multiple inheritence in the category graph, if
        // there is more than one path of equal length to the root, the method will only find one,
        // but the other (not found) LCS may have a higher information content
        // TODO is the lcs between the same node really defined or should this be handled in the
        // measures (i.e. SR(n1,n1) = 1 per definitionem??)
        if (categoryPageId1 == categoryPageId2) {
            return categoryPageId1;
        }

        List<Integer> nodeList1 = getRootPathMap().get(categoryPageId1);
        List<Integer> nodeList2 = getRootPathMap().get(categoryPageId2);

        // if one of the paths is null => return -1
        if (nodeList1 == null || nodeList2 == null || nodeList1.isEmpty() || nodeList2.isEmpty()) {
            logger.debug("One of the node lists is null or empty!");
            return -1;
        }

        logger.debug(nodeList1.toString());
        logger.debug(nodeList2.toString());

        // node 1 subsumes node 2 ?
        for (int tmpNode2 : nodeList2) {
            if (tmpNode2 == categoryPageId1) {
                return categoryPageId1;
            }
        }

        // node 2 subsumes node 1 ?
        for (int tmpNode1 : nodeList1) {
            if (tmpNode1 == categoryPageId2) {
                return categoryPageId2;
            }
        }
        // they have a lcs ?
        for (int tmpNode1 : nodeList1) {
            for (int tmpNode2 : nodeList2) {
                if (tmpNode1 == tmpNode2) {
                    return tmpNode1;
                }
            }
        }

        logger.debug("No lcs found.");

        return -1;
    }

    /**
     * Gets the lowest common subsumer (LCS) of two nodes. The LCS of two nodes is first node on the
     * path to the root, that has both nodes as sons. Nodes that are not in the same connected
     * component as the root node are defined to have no LCS.
     *
     * @param categoryPageId1
     *            The pageid of the first category node.
     * @param categoryPageId2
     *            The pageid of the second category node.
     * @return The lowest common subsumer of the two nodes, or {@code null} if there is no LCS.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Category getLCS(int categoryPageId1, int categoryPageId2) throws WikiApiException
    {
        int lcsid = getLCSId(categoryPageId1, categoryPageId2);
        return lcsid > -1 ? wiki.getCategory(lcsid) : null;
    }

    /**
     * Gets the path length between two category nodes - measured in "edges".
     *
     * @param node1
     *            The first category node.
     * @param node2
     *            The second category node.
     * @return The number of edges of the path between node1 and node2. 0, if the nodes are
     *         identical. -1, if no path exists.
     */
    public int getPathLengthInEdges(Category node1, Category node2)
    {
        if (this.graph.containsVertex(node1.getPageId())
                && this.graph.containsVertex(node2.getPageId())) {
            if (node1.getPageId() == node2.getPageId()) {
                return 0;
            }

            // get the path from root node to node 1
            GraphPath<Integer, DefaultEdge> edgeList = DijkstraShortestPath
                    .findPathBetween(undirectedGraph, node1.getPageId(), node2.getPageId());
            if (edgeList == null) {
                return -1;
            }
            else {
                return edgeList.getLength();
            }
        }
        // if the given nodes are not in the category graph, return -1
        else {
            return -1;
        }
    }

    /**
     * Computing the path length in very large graphs like the Wikipedia category graph is very time
     * consuming. However, we know that the graph is almost a taxonomy (it contains some cycles that
     * can be removed). The path from each category to the root is stored in the rootPathMap. We can
     * use this information to speed up computation dramatically. However, we might miss some
     * shortest path to a node if there are multiple paths to the root.
     * <p>
     * It is very similar to finding the LCS. If there is no LCS, then there also is no path. If one
     * of the nodes is on the path to the root, then we already know the distance. Otherwise, the
     * distance can be computed as the sum of the distance of node1 to the LCS + the distance of
     * node2 to the LCS.
     *
     * @param cat1
     *            The first category.
     * @param cat2
     *            The second category.
     * @return The number of edges of the path between node1 and node2. 0, if the nodes are
     *         identical. -1, if no path exists.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public int getTaxonomicallyBoundPathLengthInEdges(Category cat1, Category cat2)
        throws WikiApiException
    {
        int node1 = cat1.getPageId();
        int node2 = cat2.getPageId();

        // if the given nodes are not in the category graph, return -1
        if (!this.graph.containsVertex(node1) || !this.graph.containsVertex(node2)) {
            return -1;
        }

        if (node1 == node2) {
            return 0;
        }

        List<Integer> nodeList1 = getRootPathMap().get(node1);
        List<Integer> nodeList2 = getRootPathMap().get(node2);

        // if one of the paths is null => return null
        if (nodeList1 == null || nodeList2 == null || nodeList1.isEmpty()
                || nodeList2.isEmpty()) {
            logger.debug("One of the node lists is null or empty!");
            return -1;
        }

        logger.debug(nodeList1.toString());
        logger.debug(nodeList2.toString());

        // node1 is on path of node2 to the root
        int distance1 = 0;
        for (int tmpNode2 : nodeList2) {
            if (tmpNode2 == node1) {
                return distance1;
            }
            distance1++;
        }

        // node2 is on path of node1 to the root
        int distance2 = 0;
        for (int tmpNode1 : nodeList1) {
            if (tmpNode1 == node2) {
                return distance2;
            }
            distance2++;
        }

        // they have a lcs ?
        distance1 = 0;
        for (int tmpNode1 : nodeList1) {
            distance2 = 0;
            for (int tmpNode2 : nodeList2) {
                if (tmpNode1 == tmpNode2) {
                    return distance1 + distance2;
                }
                distance2++;
            }
            distance1++;
        }

        return -1;
    }

    /**
     * Gets the path length between two category nodes - measured in "nodes".
     *
     * @param node1
     *            The first node.
     * @param node2
     *            The second node.
     * @return The number of nodes of the path between node1 and node2. 0, if the nodes are
     *         identical or neighbors. -1, if no path exists.
     *         
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public int getPathLengthInNodes(Category node1, Category node2) throws WikiApiException
    {

        int retValue = getPathLengthInEdges(node1, node2);

        if (retValue == 0) {
            return 0;
        }
        else if (retValue > 0) {
            return (--retValue);
        }
        else if (retValue == -1) {
            return -1;
        }
        else {
            throw new WikiApiException("Unknown return value.");
        }
    }

    /**
     * Creates the hyponym map, that maps from nodes to their (recursive) number of hyponyms for
     * each node. "recursive" means that the hyponyms of hyponyms are also taken into account.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private void createHyponymCountMap() throws WikiApiException
    {
        // do only create hyponymMap, if it was not already computed
        if (hyponymCountMap != null) {
            return;
        }

        String hyponymCountMapFilename = "hypoCountMap";
        File hyponymCountMapSerializedFile = new File(
                wiki.getWikipediaId() + "_" + hyponymCountMapFilename);

        if (hyponymCountMapSerializedFile.exists()) {
            logger.info("Loading saved hyponymyCountMap ...");
            Map<Integer, Integer> savedMap = (Map<Integer, Integer>) this
                    .deserializeMap(hyponymCountMapSerializedFile);
            // Maps written before the counting was capped may hold negative counts, which are the
            // result of an overflow of the summation (see issue #94). Such a map is unusable, so
            // it is discarded and computed anew rather than propagated into the measures.
            if (containsNegativeCount(savedMap)) {
                logger.warn(
                        "Discarding the saved hyponymyCountMap in {}: it holds negative hyponym "
                                + "counts, which an earlier version of JWPL could produce. It is "
                                + "computed again and overwritten.",
                        hyponymCountMapSerializedFile);
            }
            else {
                hyponymCountMap = savedMap;
                logger.info("Done loading saved hyponymyCountMap");
                return;
            }
        }

        hyponymCountMap = __computeHyponymCountMap();

        if (hyponymCountMap.size() != graph.vertexSet().size()) {
            throw new WikiApiException(
                    "HyponymCountMap does not contain an entry for each node in the graph."
                            + hyponymCountMap.size() + "/" + graph.vertexSet().size());
        }

        logger.info("Computed hyponymCountMap");
        serializeMap(hyponymCountMap, hyponymCountMapSerializedFile);
        logger.info("Serialized hyponymCountMap");
    }

    /**
     * Computes the (recursive) number of hyponyms of each node of the graph, working upwards from
     * the leaf nodes so that the count of a node is available once all of its children have one.
     *
     * @return A map from each node of the graph to its hyponym count.
     * @throws WikiApiException
     *             Thrown if not every node of the graph could be visited, which is the case if the
     *             graph contains a cycle.
     */
    Map<Integer, Integer> __computeHyponymCountMap() throws WikiApiException
    {
        Map<Integer, Integer> result = new HashMap<>();

        // In the category graph a node may have more than one father. A node is therefore only
        // queued once all of its children have a count, i.e. once its number of pending children
        // drops to zero, so that each node is processed exactly once.
        Map<Integer, Integer> pendingChildren = new HashMap<>();

        // a queue holding the nodes to process, initialized with all leaf nodes
        Deque<Integer> queue = new ArrayDeque<>();
        for (int node : graph.vertexSet()) {
            int numberOfChildren = __getChildren(node).size();
            if (numberOfChildren == 0) {
                queue.add(node);
            }
            else {
                pendingChildren.put(node, numberOfChildren);
            }
        }

        logger.info("{} leaf nodes.", queue.size());

        // while the queue is not empty
        while (!queue.isEmpty()) {
            // remove first element from queue
            int currNode = queue.poll();

            Set<Integer> children = __getChildren(currNode);

            // The counts are summed in a long: a node that is reachable over several paths is
            // counted once per path, so the sum can grow far beyond the number of nodes in the
            // graph - in a Wikipedia sized graph well beyond Integer.MAX_VALUE (see issue #94).
            long sumChildHyponyms = 0;
            for (int child : children) {
                sumChildHyponyms += result.get(child);
            }

            // number of hyponyms of current node is the number of its own hyponomies and the sum
            // of the hyponomies of its children.
            long currNodeHyponymCount = children.size() + sumChildHyponyms;
            result.put(currNode, capHyponymCount(currNodeHyponymCount));

            // queue the parents of the current node whose children now all have a count
            for (int parent : __getParents(currNode)) {
                if (pendingChildren.merge(parent, -1, Integer::sum) == 0) {
                    queue.add(parent);
                }
            }

        } // while queue not empty

        // nodes on a cycle never get all of their children counted and are thus never visited
        logger.info("{} nodes visited", result.size());
        if (result.size() != graph.vertexSet().size()) {
            throw new WikiApiException("Visited only " + result.size() + " out of "
                    + graph.vertexSet().size() + " nodes.");
        }

        return result;
    }

    /**
     * As the categoryGraph is a graph rather than a tree, the hyponym count of a node can come out
     * greater than the number of nodes in the graph. This is due to the multiple counting of nodes
     * having more than one parent. Counts are therefore capped to fall in [0,NumberOfNodes], which
     * also keeps the summation of the counts of the parents of such a node away from an overflow.
     *
     * @param count
     *            The counted number of hyponyms of a node.
     * @return The count, capped at the number of nodes in the graph.
     */
    private int capHyponymCount(long count)
    {
        int numberOfNodes = graph.vertexSet().size();
        // TODO capping is not optimal (to say the least :)
        return count > numberOfNodes ? numberOfNodes - 1 : (int) count;
    }

    /**
     * @param countMap
     *            A map from nodes to hyponym counts.
     * @return {@code true} if any node is mapped to a negative count.
     */
    private static boolean containsNegativeCount(Map<Integer, Integer> countMap)
    {
        return countMap.values().stream().anyMatch(count -> count < 0);
    }

    /**
     * @return The leaf nodes of the graph, i.e. nodes with outdegree = 0.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected Set<Integer> __getLeafNodes() throws WikiApiException
    {
        Set<Integer> leafNodes = new HashSet<>();
        for (int node : graph.vertexSet()) {
            if (getOutDegree(node) == 0) {
                leafNodes.add(node);
            }
        }
        return leafNodes;
    }

    //// The method did not consider that IC has to monotonically decrease from leaves to root node
    // /**
    // * Intrinsic information content (Seco Etal. 2004) allows to compute information content from
    //// the structure of the taxonomy (no corpus needed).
    // * IC(n) = 1 - log( hypo(n) + 1) / log(#cat)
    // * hypo(n) is the number of hyponyms of a node n
    // * #cat is the number of categories in the graph
    // * @param numberOfHyponyms
    // * @param numberOfCategories
    // * @return The intrinsic information content.
    // */
    // private double computeIntrinsicInformationContent(int numberOfHyponyms, int numberOfCategories) {
    // return (1 - (Math.log(numberOfHyponyms + 1) / Math.log(numberOfCategories)) );
    // }

    /**
     * Intrinsic information content (Seco Etal. 2004) allows to compute information content from
     * the structure of the taxonomy (no corpus needed). IC(n) = 1 - log( hypo(n) + 1) / log(#cat)
     * hypo(n) is the (recursive) number of hyponyms of a node n. Recursive means that the hyponyms
     * of hyponyms are also taken into account #cat is the number of categories in the graph
     *
     * @param category
     *            The category node for which the intrinsic information content should be returned.
     * @return The intrinsic information content for this category node.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public double getIntrinsicInformationContent(Category category) throws WikiApiException
    {
        int node = category.getPageId();

        int hyponymCount = getHyponymCountMap().get(node);
        int numberOfNodes = this.getNumberOfNodes();

        if (hyponymCount > numberOfNodes) {
            throw new WikiApiException("Something is wrong with the hyponymCountMap. "
                    + hyponymCount + " hyponyms, but only " + numberOfNodes + " nodes.");
        }

        logger.debug("{} has # hyponyms: {}", category.getTitle().getPlainTitle(), hyponymCount);

        double intrinsicIC = -1;
        if (hyponymCount >= 0) {
            intrinsicIC = (1 - (Math.log(hyponymCount + 1) / Math.log(numberOfNodes)));
        }
        return intrinsicIC;
    }

    /**
     * Computes the shortest path from each category node to the root. The paths are computed with
     * a single breadth-first search that starts at the root and follows the edges from the parent
     * to the child categories. A category that is not reachable from the root is mapped to an
     * empty path.
     *
     * @throws WikiApiException
     *             Thrown if errors occurred.
     * @see #getRootPathMap()
     */
    public void createRootPathMap() throws WikiApiException
    {

        // do only create rootPathMap, if it was not already computed
        if (rootPathMap != null) {
            return;
        }

        String rootPathMapFilename = "rootPathMap";
        File rootPathFile = new File(wiki.getWikipediaId() + "_" + rootPathMapFilename);

        // try to load rootPathMap from precomputed file
        if (rootPathFile.exists()) {
            logger.info("Loading saved rootPathMap ...");
            rootPathMap = (Map<Integer, List<Integer>>) deserializeMap(rootPathFile);
            logger.info("Done loading saved rootPathMap");
            return;
        }

        logger.info("Computing rootPathMap");
        int root = wiki.getMetaData().getMainCategory().getPageId();
        rootPathMap = __computeRootPathMap(root);

        // categories that are not part of this graph have no path to the root either
        for (Category cat : wiki.getCategories()) {
            rootPathMap.putIfAbsent(cat.getPageId(), new ArrayList<>());
        }

        int withoutPath = 0;
        for (List<Integer> path : rootPathMap.values()) {
            if (path.isEmpty()) {
                withoutPath++;
            }
        }
        logger.info("{} of {} categories have no path to the root.", withoutPath,
                rootPathMap.size());

        // from the root path map, we can very easily get the depth
        this.depth = getDepthFromRootPathMap();

        logger.info("Setting depth of category graph: {}", this.depth);

        logger.info("Serializing rootPathMap");
        this.serializeMap(rootPathMap, rootPathFile);
    }

    /**
     * Computes the shortest path from each node of the graph to the given root, using a single
     * breadth-first search that starts at the root and follows the outgoing edges, i.e. the edges
     * from a parent to its children. This takes time linear in the size of the graph and also
     * terminates on graphs that still contain cycles.
     * <p>
     * If a node has more than one shortest path to the root, the path is chosen in the same way as
     * the depth-first search that was used before: at each node, the first parent (in the order of
     * the incoming edges of the node) that lies on a shortest path is taken. As a consequence, each
     * suffix of a path is the path of the node it starts with.
     *
     * @param root
     *            The pageId of the root node.
     * @return A map from each node of the graph to the list of nodes on its shortest path to the
     *         root, starting with the node itself and ending with the root. Nodes that are not
     *         reachable from the root are mapped to an empty list.
     */
    Map<Integer, List<Integer>> __computeRootPathMap(int root)
    {
        Map<Integer, List<Integer>> result = new HashMap<>();
        if (!graph.containsVertex(root)) {
            logger.warn("The root node {} is not part of this graph.", root);
            for (int node : graph.vertexSet()) {
                result.put(node, new ArrayList<>());
            }
            return result;
        }

        // breadth-first search from the root: the distance of each reachable node to the root,
        // and the reachable nodes in the order of increasing distance
        Map<Integer, Integer> distances = new HashMap<>();
        List<Integer> nodesByDistance = new ArrayList<>();
        Deque<Integer> queue = new ArrayDeque<>();
        distances.put(root, 0);
        queue.add(root);
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            nodesByDistance.add(currentNode);
            int childDistance = distances.get(currentNode) + 1;
            for (DefaultEdge outgoingEdge : graph.outgoingEdgesOf(currentNode)) {
                int child = graph.getEdgeTarget(outgoingEdge);
                if (!distances.containsKey(child)) {
                    distances.put(child, childDistance);
                    queue.add(child);
                }
            }
        }

        // the path of a node is the node itself followed by the path of its first parent that is
        // one step closer to the root; as the nodes are processed in the order of increasing
        // distance, the path of that parent is always known already
        for (int node : nodesByDistance) {
            if (node == root) {
                List<Integer> path = new ArrayList<>(1);
                path.add(root);
                result.put(root, path);
                continue;
            }
            int parentDistance = distances.get(node) - 1;
            for (DefaultEdge incomingEdge : graph.incomingEdgesOf(node)) {
                int parent = graph.getEdgeSource(incomingEdge);
                Integer distance = distances.get(parent);
                if (distance != null && distance == parentDistance) {
                    List<Integer> parentPath = result.get(parent);
                    List<Integer> path = new ArrayList<>(parentPath.size() + 1);
                    path.add(node);
                    path.addAll(parentPath);
                    result.put(node, path);
                    break;
                }
            }
        }

        // nodes that are not reachable from the root have no path
        for (int node : graph.vertexSet()) {
            result.putIfAbsent(node, new ArrayList<>());
        }
        return result;
    }

    /**
     * @param pageID
     *            The pageID of the category.
     * @return The indegree of the given category.
     */
    protected int getInDegree(int pageID)
    {
        return graph.inDegreeOf(pageID);
    }

    /**
     * @param pageID
     *            The pageID of the category.
     * @return The outdegree of the given category.
     */
    protected int getOutDegree(int pageID)
    {
        return graph.outDegreeOf(pageID);
    }

    /**
     * @param pageID
     *            The pageID of the category.
     * @return A set of child nodes of the given category.
     */
    protected Set<Integer> __getChildren(int pageID)
    {
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(pageID);
        Set<Integer> outLinks = new HashSet<>();
        for (DefaultEdge edge : outgoingEdges) {
            outLinks.add(graph.getEdgeTarget(edge));
        }
        return outLinks;
    }

    /**
     * @param pageID
     *            The pageID of the category.
     * @return A set of parent nodes of the given category.
     */
    protected Set<Integer> __getParents(int pageID)
    {
        Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(pageID);
        Set<Integer> inLinks = new HashSet<>();
        for (DefaultEdge edge : incomingEdges) {
            inLinks.add(graph.getEdgeSource(edge));
        }
        return inLinks;
    }

    /**
     * @return Returns the largest connected component as a new graph. If the base graph already is
     *         connected, it simply returns the whole graph.
     *         
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public CategoryGraph getLargestConnectedComponent() throws WikiApiException
    {
        ConnectivityInspector<Integer, DefaultEdge> connectInspect = new ConnectivityInspector<>(graph);

        // if the graph is connected, simply return the whole graph
        if (connectInspect.isConnected()) {
            return this;
        }

        // else, get the largest connected component
        List<Set<Integer>> connectedComponentList = connectInspect.connectedSets();

        logger.info("{} connected components.", connectedComponentList.size());

        int i = 0;
        int maxSize = 0;
        Set<Integer> largestComponent = new HashSet<>();
        for (Set<Integer> connectedComponent : connectedComponentList) {
            i++;
            if (connectedComponent.size() > maxSize) {
                maxSize = connectedComponent.size();
                largestComponent = connectedComponent;
            }
        }

        double largestComponentRatio = largestComponent.size() * 100 / this.getNumberOfNodes();
        logger.info("Largest component contains {}% ({}/{}) of the nodes in the graph.",
              largestComponentRatio, largestComponent.size(), this.getNumberOfNodes());

        return CategoryGraphManager.getCategoryGraph(wiki, largestComponent);
    }

    /**
     * Get the number of nodes in the graph.
     *
     * @return The number of nodes in the graph.
     */
    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }

    /**
     * Get the number of edges in the graph.
     *
     * @return The number of edges in the graph.
     */
    public int getNumberOfEdges()
    {
        return numberOfEdges;
    }

    /**
     * Computes the average of the path length between all pairs of nodes. The graph is treated as
     * an undirected graph. Computing graph parameters requires touching all node pairs. Therefore,
     * if one is called the others are computed as well and stored for later retrieval.
     *
     * @return The average of the shortest path lengths between all pairs of nodes.
     */
    public double getAverageShortestPathLength()
    {
        if (!graphParametersComputed) {
            logger.debug("Calling setGraphParameters");
            setGraphParameters();
        }
        return averageShortestPathLength;
    }

    /**
     * Computes the diameter of the graph (the maximum of the shortest path length between all pairs
     * of nodes) The graph is treated as an undirected graph. Computing graph parameters requires
     * touching all node pairs. Therefore, if one is called the others are computed as well and
     * stored for later retrieval.
     *
     * @return The diameter of the graph.
     */
    public double getDiameter()
    {
        if (!graphParametersComputed) {
            logger.debug("Calling setGraphParameters");
            setGraphParameters();
        }
        return diameter;
    }

    /**
     * Computes the average degree. The degree of a node is the number of edges that it is
     * connected with. The graph is treated as an undirected graph. Computing graph parameters
     * requires touching all node pairs. Therefore, if one is called the others are computed as well
     * and stored for later retrieval.
     *
     * @return The average degree of the graph.
     */
    public double getAverageDegree()
    {
        if (!graphParametersComputed) {
            logger.debug("Calling setGraphParameters");
            setGraphParameters();
        }
        return averageDegree;
    }

    /**
     * Compute the cluster coefficient of the graph (after Watts and Strogatz 1998) Cluster
     * coefficient C is defined as the average of C_v over all edges. C_v is the fraction of the
     * connections that exist between the neighbor nodes (k_v) of a vertex v and all allowable
     * connections between the neighbors (k_v(k_v -1)/2). C_v = 2 * number of connections between /
     * k_v*(k_v -1)
     *
     * @return The cluster coefficient.
     */
    public double getClusterCoefficient()
    {
        if (!graphParametersComputed) {
            logger.debug("Calling setGraphParameters");
            setGraphParameters();
        }
        return clusterCoefficient;
    }

    /**
     * Computes the degree distribution. The degree of a node is the number of edges that it is
     * connected with. The graph is treated as an undirected graph. Computing graph parameters
     * requires touching all node pairs. Therefore, if one is called the others are computed as well
     * and stored for later retrieval.
     *
     * @return A map with the degree distribution of the graph.
     */
    public Map<Integer, Integer> getDegreeDistribution()
    {
        if (!graphParametersComputed) {
            logger.debug("Calling setGraphParameters");
            setGraphParameters();
        }
        return degreeDistribution;
    }

    /**
     * Get the number of connections that exist between the neighbors of a node.
     *
     * @param node
     *            The node under consideration.
     * @return The number of connections that exist between the neighbors of node.
     */
    private int getNumberOfNeighborConnections(int node)
    {
        int numberOfConnections = 0;

        // get the set of neighbors
        Set<Integer> neighbors = getNeighbors(node);

        if (!neighbors.isEmpty()) {
            // for each pair of neighbors, test if there is a connection
            // the order of the nodes does not matter, as each pair i < j is visited exactly once
            Integer[] nodeArray = neighbors.toArray(new Integer[0]);

            for (int i = 0; i < nodeArray.length; i++) {
                int outerNode = nodeArray[i];
                for (int j = i + 1; j < nodeArray.length; j++) {
                    int innerNode = nodeArray[j];
                    // in case of a connection - increade connection counter
                    // order of the nodes doesn't matter for undirected graphs
                    if (undirectedGraph.containsEdge(innerNode, outerNode)) {
                        numberOfConnections++;
                    }
                }
            }
        }

        // logger.info(neighbors.size() + " - " + numberOfConnections);

        return numberOfConnections;
    }

    /**
     * Get the neighbors of a given node. The category graph is treated as an undirected graph.
     *
     * @param node
     *            the reference node.
     * @return The set of category nodes that are neighbors of this category.
     */
    protected Set<Integer> getNeighbors(int node)
    {

        Set<Integer> neighbors = new HashSet<>();
        Set<DefaultEdge> edges = undirectedGraph.edgesOf(node);
        for (DefaultEdge edge : edges) {
            if (undirectedGraph.getEdgeSource(edge) != node) {
                neighbors.add(undirectedGraph.getEdgeSource(edge));
            }
            if (undirectedGraph.getEdgeTarget(edge) != node) {
                neighbors.add(undirectedGraph.getEdgeTarget(edge));
            }
        }
        return neighbors;
    }

    /**
     * Computes and sets the diameter, the average degree and the average shortest path length of
     * the graph. Do not call this in the constructor. May run a while. It is called in the getters,
     * if parameters are not yet initialized when retrieved.
     */
    private void setGraphParameters()
    {
        if (graphParametersComputed) {
            return;
        }

        // Diameter is the maximum of all shortest path lengths
        // Average shortest path length is (as the name says) the average of the shortest path
        // length between all node pairs

        double maxPathLength = 0.0;
        double shortestPathLengthSum = 0.0;
        double degreeSum = 0.0;
        double clusterCoefficientSum = 0.0;
        Map<Integer, Integer> degreeCounts = new HashMap<>();

        // iterate over all node pairs
        Set<Integer> nodes = undirectedGraph.vertexSet();

        // a hashset of the nodes which have been the start node of the computation process
        // for such nodes all path lengths have beeen already computed
        Set<Integer> wasSource = new HashSet<>();

        int progress = 0;
        for (int node : nodes) {

            progress++;
            ApiUtilities.printProgressInfo(progress, nodes.size(), 100,
                    ApiUtilities.ProgressInfoMode.TEXT, "Getting graph parameters");

            int nodeDegree = undirectedGraph.degreeOf(node);
            degreeSum += nodeDegree;
            degreeCounts.merge(nodeDegree, 1, Integer::sum);

            // cluster coefficient of a node is C_v is the fraction of the connections that exist
            // between the neighbor nodes (k_v) of this node and all allowable connections between
            // the neighbors (k_v(k_v -1)/2)
            // for degrees 0 or 1 there is no cluster coefficient, as there can be no connections
            // between neighbors
            if (undirectedGraph.degreeOf(node) > 1) {
                double numberOfNeighborConnections = getNumberOfNeighborConnections(node);
                clusterCoefficientSum += (numberOfNeighborConnections
                        / ((double) nodeDegree * (nodeDegree - 1)));
            }

            // Returns the new shortestPathLengthSum and the new maxPathLength.
            // They are returned as a double array for performance reasons.
            // I do not want to create an object, as this function is called *very* often
            double[] returnValues = computeShortestPathLenghts(node, shortestPathLengthSum,
                    maxPathLength, wasSource);
            shortestPathLengthSum = returnValues[0];
            maxPathLength = returnValues[1];

            // save the info that the node was already used as the source of path computation
            wasSource.add(node);
        }

        if (nodes.size() > 1) {
            // sum of path lengths / (number of node pairs), computed in long arithmetic to avoid
            // an int overflow for graphs with more than 46341 nodes
            long numberOfNodePairs = (long) nodes.size() * (nodes.size() - 1) / 2;
            this.averageShortestPathLength = shortestPathLengthSum / numberOfNodePairs;
        }
        else {
            this.averageShortestPathLength = 0; // there is only one node
        }
        this.diameter = maxPathLength;
        this.averageDegree = degreeSum / nodes.size();
        this.clusterCoefficient = clusterCoefficientSum / nodes.size();
        this.degreeDistribution = degreeCounts;
        this.graphParametersComputed = true;
    }

    /**
     * Computes the shortest path from node to all other nodes. Paths to nodes that have already
     * been the source of the shortest path computation are omitted (the path was already added to
     * the path sum). Updates the sum of shortest path lengths and the diameter of the graph. As the
     * JGraphT BreadthFirstIterator does not provide information about the distance to the start
     * node in each step, we will use our own BFS implementation.
     *
     * @param pStartNode
     *            The start node of the search.
     * @param pShortestPathLengthSum
     *            The sum of the shortest path lengths.
     * @param pMaxPathLength
     *            The maximum path length found so far.
     * @param pWasSource
     *            A set of nodes which have been the start node of the computation process. For such
     *            nodes all path lengths have been already computed.
     * @return An array of double values. The first value is the shortestPathLengthSum and the
     *         second value is the maxPathLength. They are returned as a double array for
     *         performance reasons. I do not want to create an object, as this function is called
     *         *very* often.
     */
    private double[] computeShortestPathLenghts(int pStartNode, double pShortestPathLengthSum,
            double pMaxPathLength, Set<Integer> pWasSource)
    {

        // a set of nodes that have already been discovered. Nodes are marked on enqueue, so that
        // each node enters the queue at most once and is assigned its (minimal) BFS distance
        Set<Integer> alreadyDiscovered = new HashSet<>();

        // two parallel queues holding the newly discovered nodes and their distance to the start
        // node
        Deque<Integer> nodeQueue = new ArrayDeque<>();
        Deque<Integer> distanceQueue = new ArrayDeque<>();

        // initialize queue with start node
        alreadyDiscovered.add(pStartNode);
        nodeQueue.add(pStartNode);
        distanceQueue.add(0);

        // while the queue is not empty
        while (!nodeQueue.isEmpty()) {
            // remove first element from queue
            int currentNode = nodeQueue.poll();
            int distance = distanceQueue.poll();

            // if the node was a source node in a previous run, we already have added this path
            if (!pWasSource.contains(currentNode)) {
                // add the distance of this node to shortestPathLengthSum
                // check if maxPathLength must be updated
                pShortestPathLengthSum += distance;
                if (distance > pMaxPathLength) {
                    pMaxPathLength = distance;
                }
            }
            // even if the node was a source node in a previous run there can be a path to other
            // nodes over this node, so go on

            // iterate over all neighbors of the queue element
            for (DefaultEdge edge : undirectedGraph.edgesOf(currentNode)) {
                int neighbor = Graphs.getOppositeVertex(undirectedGraph, edge, currentNode);
                // add the node to the queue if it was not discovered yet,
                // increase node distance by one
                if (alreadyDiscovered.add(neighbor)) {
                    nodeQueue.add(neighbor);
                    distanceQueue.add(distance + 1);
                }
            }
        }
      return new double[]{ pShortestPathLengthSum, pMaxPathLength };
    }

    /**
     * This parameter is already set in the constructor as it is needed for computation of
     * relatedness values. Therefore, its computation does not trigger setGraphParameters (it is too
     * slow), even if the depth is implicitly determined there, too.
     *
     * @return The depth of the category graph, i.e. the maximum path length starting with the root
     *         node.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public double getDepth() throws WikiApiException
    {
        if (depth < 0) { // has not been initialized
            if (rootPathMap != null) {
                this.depth = getDepthFromRootPathMap();
                logger.info("Getting depth from RootPathMap: {}", this.depth);

            }
            else {
                depth = computeDepth();
                logger.info("Computing depth of the hierarchy: {}", this.depth);
            }
        }
        return depth;
    }

    /**
     * This parameter is already set in the constructor as it is needed for computation of
     * relatedness values. Therefore, its computation does not trigger setGraphParameters (it is too
     * slow), even if the depth is implicitly determined there, too.
     *
     * @return The depth of the category graph, i.e. the maximum path length starting with the root
     *         node.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private double getDepthFromRootPathMap() throws WikiApiException
    {
        int max = 0;
        for (List<Integer> path : getRootPathMap().values()) {
            if (path.size() > max) {
                max = path.size();
            }
        }

        max = max - 1; // depth is measured in nodes, not edges

        if (max < 0) {
            return 0;
        }
        else {
            return max;
        }
    }

    /**
     * Computes the depth of the category graph, i.e. the maximum path length starting with the root
     * node.
     *
     * @return The depth of the hierarchy.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    private double computeDepth() throws WikiApiException
    {
        Category root = wiki.getMetaData().getMainCategory();
        if (root == null) {
            logger.error("There is no root node for this wiki. " +
                    "Check the parameter that provides the name of the root node.");
            return 0.0;
        }
        // test whether the root category is in this graph
        if (!graph.containsVertex(root.getPageId())) {
            logger.error("The root node is not part of this graph. " +
                            "Cannot compute depth of this graph. Setting depth to 0.0");
            return 0.0;
        }
        double maxPathLength = 0.0;
        double[] returnValues = computeShortestPathLenghts(root.getPageId(), 0.0, maxPathLength,
                new HashSet<>());
        maxPathLength = returnValues[1];
        return maxPathLength;
    }

    /**
     * @return Creates and returns a graph properties information string.
     */
    public String getGraphInfo()
    {
        StringBuffer sb = new StringBuffer(1000);
        Map<Integer, Integer> degreeDistribution = getDegreeDistribution();

        sb.append("Number of Nodes:     ").append(getNumberOfNodes()).append(LF);
        sb.append("Number of Edges:     ").append(getNumberOfEdges()).append(LF);
        sb.append("Avg. path length:    ").append(getAverageShortestPathLength()).append(LF);
        sb.append("Diameter:            ").append(getDiameter()).append(LF);
        sb.append("Average degree:      ").append(getAverageDegree()).append(LF);
        sb.append("Cluster coefficient: ").append(getClusterCoefficient()).append(LF);
        sb.append("Degree distribution: ").append(CommonUtilities.getMapContents(degreeDistribution)).append(LF);

        return sb.toString();
    }

    /**
     * @return Returns the {@link DefaultDirectedGraph directed graph}.
     */
    public DefaultDirectedGraph<Integer, DefaultEdge> getGraph()
    {
        return graph;
    }

    /**
     * @return Returns the {@link AsUndirectedGraph undirected graph}.
     */
    public AsUndirectedGraph<Integer, DefaultEdge> getUndirectedGraph()
    {
        return undirectedGraph;
    }

    /**
     * @return Retrieves a map for the frequencies (value) of hyponyms (key).
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Map<Integer, Integer> getHyponymCountMap() throws WikiApiException
    {
        if (hyponymCountMap == null) {
            createHyponymCountMap();
        }
        return this.hyponymCountMap;
    }

    /**
     * @return Retrieves a map of root paths.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Map<Integer, List<Integer>> getRootPathMap() throws WikiApiException
    {
        if (rootPathMap == null) {
            createRootPathMap();
        }
        return this.rootPathMap;
    }

    /**
     * Serializes the specified {@link Map map} to a {@code file}.
     *
     * @param map
     *            The map to serialize. Must not be {@code null}.
     * @param file
     *            The file for saving the map. Must not be {@code null}.
     */
    private void serializeMap(Map<?, ?> map, File file)
    {
        try (ObjectOutputStream os = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            os.writeObject(map);
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Deserializes a {@link Map map} from the specified {@code file}.
     *
     * @param file
     *            The file with the map. Must not be {@code null}.
     * @return The reconstructed {@link Map} or {@code null} if errors occurred.
     */
    private Map<?, ?> deserializeMap(File file)
    {
        Map<?, ?> map;
        try (ObjectInputStream is = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            map = (Map<?, ?>) is.readObject();
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
        return map;
    }

    /**
     * Serializes the graph to the given destination.
     *
     * @param destination
     *            The destination to which should be saved.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public void saveGraph(String destination) throws WikiApiException
    {
        try {
            GraphSerialization.saveGraph(graph, destination);
        }
        catch (IOException e) {
            throw new WikiApiException(e);
        }
    }

}
