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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.util.ApiUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds numerous information on a given subset (that may also be the whole Wikipedia) of Wikipedia
 * nodes.
 */
public class WikipediaInfo
{

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Counts the distinct outlinks of all pages that point to an existing page, i.e. exactly the
     * links {@link Page#getOutlinks()} resolves.
     */
    private static final String SQL_COUNT_RESOLVABLE_OUTLINKS = "select count(*) from "
            + "(select distinct o.id, o.outLinks from page_outlinks o "
            + "join Page src on src.id = o.id "
            + "join Page tgt on tgt.pageId = o.outLinks) links";

    /**
     * Counts the distinct outlinks of a single page that point to an existing page, i.e. the size
     * of {@link Page#getOutlinks()} for that page.
     */
    private static final String SQL_COUNT_RESOLVABLE_OUTLINKS_OF_PAGE = "select "
            + "count(distinct o.outLinks) from page_outlinks o "
            + "join Page tgt on tgt.pageId = o.outLinks where o.id = :id";

    private final Iterable<Page> pages;
    private final boolean allPages;
    private double averageFanOut;

    private int numberOfPages;

    private Map<Integer, Integer> degreeDistribution;
    private Set<Integer> categorizedArticleSet;

    private final Wikipedia wiki;

    /**
     * Get infos for the whole wikipedia.
     *
     * @param pWiki
     *            The wiki object.
     *            
     * @throws WikiApiException Thrown if errors occurred.
     */
    public WikipediaInfo(Wikipedia pWiki) throws WikiApiException
    {
        this(pWiki.getPages(), pWiki, true);
    }

    /**
     * Get infos only for a subset of articles.
     *
     * @param pPages
     *            A set of pages. Only this subset of wiki pages is used in the info object.
     * @param pWiki
     *            The wiki object.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public WikipediaInfo(Iterable<Page> pPages, Wikipedia pWiki) throws WikiApiException
    {
        this(pPages, pWiki, false);
    }

    private WikipediaInfo(Iterable<Page> pPages, Wikipedia pWiki, boolean pAllPages)
        throws WikiApiException
    {
        if (pPages == null) {
            throw new WikiApiException("The page set has to be initialized.");
        }

        if (pWiki == null) {
            throw new WikiApiException("The wiki instance is not set.");
        }

        wiki = pWiki;
        pages = pPages;
        allPages = pAllPages;
        averageFanOut = -1.0; // lazy initialization => it is computed and stored when it is
                              // accessed

        // lazy initialization => computed on first access, see iterateCategoriesGetArticles
        degreeDistribution = null;
        categorizedArticleSet = null;

        numberOfPages = -1; // lazy initialization => computed on first access
    }

    /**
     * Counts the pages of the page set.
     *
     * @return The number of pages in the page set.
     */
    private int computeNumberOfPages()
    {
        if (allPages) {
            // exactly the rows the page iterable walks, without loading any of them
            return wiki.__inTransaction(session -> session
                    .createQuery("select count(p) from Page p", Long.class).uniqueResult())
                    .intValue();
        }
        if (pages instanceof Collection<?> collection) {
            return collection.size();
        }
        // A caller-supplied iterable can only be counted by walking it, as there is no query
        // that selects exactly its pages.
        int count = 0;
        for (Page page : pages) {
            count++;
        }
        return count;
    }

    /**
     * Computes the average fan out of the page set. Fan out is the number of outgoing links per
     * page.
     *
     * @param pages
     *            The pages in an iterable form.
     * @return The average fan out.
     */
    private double computeAverageFanOut(Iterable<Page> pages)
    {
        // Counts only outlinks to existing pages, as Page#getOutlinks() does, but without loading
        // the link targets.
        double sum = 0;
        if (allPages) {
            sum = wiki.__inTransaction(session -> session
                    .createNativeQuery(SQL_COUNT_RESOLVABLE_OUTLINKS, Long.class).uniqueResult());
        }
        else {
            for (Page page : pages) {
                long id = page.__getId();
                sum += wiki.__inTransaction(session -> session
                        .createNativeQuery(SQL_COUNT_RESOLVABLE_OUTLINKS_OF_PAGE, Long.class)
                        .setParameter("id", id, Long.class).uniqueResult());
            }
        }

        return sum / this.getNumberOfPages();
    }

    /**
     * @return Returns the averageFanOut.
     */
    public double getAverageFanOut()
    {
        if (averageFanOut < 0) { // not yet initialized
            averageFanOut = computeAverageFanOut(this.pages);
        }

        return averageFanOut;
    }

    /**
     * @return Returns the numberOfPages.
     */
    public int getNumberOfPages()
    {
        if (numberOfPages < 0) { // not yet initialized
            numberOfPages = computeNumberOfPages();
        }
        return numberOfPages;
    }

    /**
     * Building a mapping from categories to article sets.
     *
     * @param pWiki
     *            The wikipedia object.
     * @param pNodes
     *            The category nodes that should be used to build the map.
     * @return A mapping from categories to article sets.
     */
    private Map<Integer, Set<Integer>> getCategoryArticleMap(Wikipedia pWiki, Set<Integer> pNodes)
    {
        Map<Integer, Set<Integer>> categoryArticleMap = new HashMap<>();

        int progress = 0;
        for (int node : pNodes) {
            progress++;
            ApiUtilities.printProgressInfo(progress, pNodes.size(), 10,
                    ApiUtilities.ProgressInfoMode.TEXT, "Getting category-article map.");

            Category cat = pWiki.getCategory(node);
            if (cat != null) {
                Set<Integer> pages = new HashSet<>(cat.__getPages());
                categoryArticleMap.put(node, pages);
            }
            else {
                logger.info("{} is not a category.", node);
            }
        }

        return categoryArticleMap;
    }

    /**
     * Get various graph parameters like diameter, average out-degree etc. of the category graph.
     *
     * @param catGraph
     *            The category graph.
     */
    public void getGraphParameters(CategoryGraph catGraph)
    {
        double startTime = System.currentTimeMillis();
        logger.error(catGraph.getGraphInfo());
        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.error("{}s", endTime);
    }

    /**
     * Articles in wikipedia may be tagged with multiple categories. It may be interesting to know
     * how many articles have at least one category in common. Such articles would have a very high
     * semantic relatedness even if they share a quite secondary category.
     *
     * @param pWiki
     *            The wikipedia object.
     * @param catGraph
     *            The category graph.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public void getOverlapping(Wikipedia pWiki, CategoryGraph catGraph) throws WikiApiException
    {
        double startTime = System.currentTimeMillis();

        int articlesWithOverlappingCategories = getArticlesWithOverlappingCategories(pWiki,
                catGraph);
        double overlappingCategoriesRatio = (double) articlesWithOverlappingCategories
                / (double) pWiki.getMetaData().getNumberOfPages();
        logger.info("{} - {} - {}", articlesWithOverlappingCategories,
              pWiki.getMetaData().getNumberOfPages(), overlappingCategoriesRatio);

        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.debug("{} ms", endTime);
    }

    /**
     * Articles in wikipedia may be tagged with multiple categories. It may be interesting to know
     * how many articles have at least one category in common. Such articles would have a very high
     * semantic relatedness even if they share a quite secondary category.
     *
     * @param pWiki
     *            The wikipedia object.
     * @param pGraph
     *            The category graph.
     * @return The number of articles that have at least one category in common.
     */
    int getArticlesWithOverlappingCategories(Wikipedia pWiki, CategoryGraph pGraph)
    {
        Set<Integer> nodes = pGraph.getGraph().vertexSet();

        Map<Integer, Set<Integer>> categoryArticleMap = getCategoryArticleMap(pWiki, nodes);

        // An article overlaps exactly if it is contained in at least two of the categories, so a
        // single pass that counts the categories per article replaces the pairwise comparison.
        Map<Integer, Integer> categoriesPerArticle = new HashMap<>();
        for (Set<Integer> articles : categoryArticleMap.values()) {
            for (int article : articles) {
                categoriesPerArticle.merge(article, 1, Integer::sum);
            }
        }

        int overlappingArticles = 0;
        for (int numberOfCategories : categoriesPerArticle.values()) {
            if (numberOfCategories >= 2) {
                overlappingArticles++;
            }
        }
        return overlappingArticles;
    }

    /**
     * Retrieves categorized articles and prints the result as a summary.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param catGraph A {@link CategoryGraph} to be used for traversal and report generation.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public void getCategorizedArticles(Wikipedia wiki, CategoryGraph catGraph)
        throws WikiApiException
    {
        double startTime = System.currentTimeMillis();

        int numberOfCategorizedArticles = getNumberOfCategorizedArticles(wiki, catGraph);
        double categorizedArticlesRatio = (double) numberOfCategorizedArticles
                / (double) wiki.getMetaData().getNumberOfPages();

        logger.info("Categorized articles: {}", numberOfCategorizedArticles);
        logger.info("All articles:         {}", wiki.getMetaData().getNumberOfPages());
        logger.info("Ratio:                {}", categorizedArticlesRatio);

        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.debug("{}ms", endTime);
    }

    /**
     * Computes the average path length starting from a root page.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param connectedCatGraph A {@link CategoryGraph} to be used for traversal.
     *                          
     * @return The averaged path length computed for {@code connectedCatGraph}.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public double getAveragePathLengthFromRoot(Wikipedia wiki, CategoryGraph connectedCatGraph)
        throws WikiApiException
    {
        // get root node
        Category rootCategory = wiki.getMetaData().getMainCategory();
        int root = rootCategory.getPageId();

        int pathLengthSum = computeShortestPathLenghts(root, connectedCatGraph);

        return (double) pathLengthSum / (connectedCatGraph.getGraph().vertexSet().size() - 1);
    }

    /**
     * If the return value has been already computed, it is returned, else it is computed at
     * retrieval time.
     *
     * @param pWiki
     *            The wikipedia object.
     * @param catGraph
     *            The category graph.
     * @return The number of categorized articles, i.e. articles that have at least one category.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public int getNumberOfCategorizedArticles(Wikipedia pWiki, CategoryGraph catGraph)
        throws WikiApiException
    {
        if (categorizedArticleSet == null) { // has not been initialized yet
            iterateCategoriesGetArticles(pWiki, catGraph);
        }
        return categorizedArticleSet.size();
    }

    /**
     * Computes the distribution of the number of articles per category. If the return value has
     * been already computed, it is returned, else it is computed at retrieval time.
     *
     * @param pWiki
     *            The wikipedia object.
     * @param catGraph
     *            The category graph.
     * @return A map containing the distribution mapping from a degree to the number of times this
     *         degree is found in the category graph.
     * @throws WikiPageNotFoundException Thrown if parts in {@code catGraph} could not be found.
     */
    public Map<Integer, Integer> getDistributionOfArticlesByCategory(Wikipedia pWiki,
            CategoryGraph catGraph)
        throws WikiPageNotFoundException
    {
        if (degreeDistribution == null) { // has not been initialized yet
            iterateCategoriesGetArticles(pWiki, catGraph);
        }
        return degreeDistribution;
    }

    /**
     * Methods computing stuff that have to iterate over all categories and access category articles
     * can plug in here. Recently plugin-in: numberOfCategorizedArticles
     * distributionOfArticlesByCategory
     *
     * @param pWiki
     *            The wikipedia object.
     * @param catGraph
     *            The category graph.
     */
    private void iterateCategoriesGetArticles(Wikipedia pWiki, CategoryGraph catGraph)
    {
        Map<Integer, Integer> localDegreeDistribution = new HashMap<>();
        Set<Integer> localCategorizedArticleSet = new HashSet<>();
        Set<Integer> categoryNodes = catGraph.getGraph().vertexSet();
        // iterate over all categories
        int progress = 0;
        for (int node : categoryNodes) {
            progress++;
            ApiUtilities.printProgressInfo(progress, categoryNodes.size(), 100,
                    ApiUtilities.ProgressInfoMode.TEXT, "iterate over categories");

            // get the category
            Category cat = pWiki.getCategory(node);
            if (cat != null) {
                Set<Integer> pages = new HashSet<>(cat.__getPages());

                // update degree distribution map
                int numberOfArticles = pages.size();
                if (localDegreeDistribution.containsKey(numberOfArticles)) {
                    int count = localDegreeDistribution.get(numberOfArticles);
                    count++;
                    localDegreeDistribution.put(numberOfArticles, count);
                }
                else {
                    localDegreeDistribution.put(numberOfArticles, 1);
                }

                // add the page to the categorized articles set, if it is to already in it
                for (int page : pages) {
                    if (!localCategorizedArticleSet.contains(page)) {
                        localCategorizedArticleSet.add(page);
                    }
                }
            }
            else {
                logger.info("{} is not a category.", node);
            }
        }
        this.degreeDistribution = localDegreeDistribution;
        this.categorizedArticleSet = localCategorizedArticleSet;
    }

    /**
     * Computes the shortest path from node to all other nodes. As the JGraphT BreadthFirstIterator
     * does not provide information about the distance to the start node in each step, we will use
     * our own BFS implementation.
     *
     * @param pStartNode
     *            The start node of the search.
     * @param catGraph
     *            The category graph.
     * @return An array of double values.
     */
    private int computeShortestPathLenghts(int pStartNode, CategoryGraph catGraph)
    {
        int shortestPathLengthSum = 0;

        // a set of nodes that have already been expanded -> algorithm should expand nodes
        // monotonically and not go back
        Set<Integer> alreadyExpanded = new HashSet<>();

        // a queue holding the newly discovered nodes with their and their distance to the start
        // node
        Queue<int[]> queue = new ArrayDeque<>();

        // initialize queue with start node
        int[] innerList = new int[2];
        innerList[0] = pStartNode; // the node
        innerList[1] = 0; // the distance to the start node
        queue.add(innerList);

        // while the queue is not empty
        while (!queue.isEmpty()) {
            // remove first element from queue
            int[] queueElement = queue.poll();
            int currentNode = queueElement[0];
            int distance = queueElement[1];

            // if the node was not already expanded
            if (!alreadyExpanded.contains(currentNode)) {
                // the node gets expanded now
                alreadyExpanded.add(currentNode);

                // add the distance of this node to shortestPathLengthSum
                shortestPathLengthSum += distance;

                // get the neighbors of the queue element
                Set<Integer> neighbors = catGraph.getNeighbors(currentNode);

                // iterate over all neighbors
                for (int neighbor : neighbors) {
                    // if the node was not already expanded
                    if (!alreadyExpanded.contains(neighbor)) {
                        // add the node to the queue, increase node distance by one
                        int[] tmpList = new int[2];
                        tmpList[0] = neighbor;
                        tmpList[1] = (distance + 1);
                        queue.add(tmpList);
                    }
                }
            }
        }
        return shortestPathLengthSum;
    }
}
