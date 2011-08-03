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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.util.ApiUtilities;

/** Holds numerous information on a given subset (that may also be
 * the whole Wikipedia) of Wikipedia nodes.
 * @author zesch
 */
public class WikipediaInfo {

	private final Log logger = LogFactory.getLog(getClass());

    private Iterable<Page> pages;
    private double averageFanOut;

    private int numberOfPages;

    private Map<Integer,Integer> degreeDistribution;
    private Set<Integer> categorizedArticleSet;

    private Wikipedia wiki;

    /**
     * Get infos for the whole wikipedia.
     * @param pWiki The wiki object.
     */
    public WikipediaInfo(Wikipedia pWiki) throws WikiApiException {
        this.wiki = pWiki;
        new WikipediaInfo(this.wiki.getPages());

    }


    /**
     * Get infos only for a subset of articles.
     * @param pPages A set of pages. Only this subset of wiki pages is used in the info object.
     */
    public WikipediaInfo(Iterable<Page> pPages) throws WikiApiException {
        if (pPages == null) {
            throw new WikiApiException("The page set has to be initialized.");
        }

        pages = pPages;
        averageFanOut = -1.0;   // lazy initialization => it is computed and stored when it is accessed

        degreeDistribution = new HashMap<Integer,Integer>();
        categorizedArticleSet = new HashSet<Integer>();

        // get number of pages
        numberOfPages = 0;
        while (pages.iterator().hasNext()) {
            numberOfPages++;
            pages.iterator().next();
        }

    }


    /** Computes the average fan out of the page set.
     * Fan out is the number of outgoing links per page.
     * @param pageIDs The IDs of the pages.
     * @return The average fan out.
     */
    private double computeAverageFanOut(Iterable<Page> pages) {

        Set<Integer> pageIDs = new HashSet<Integer>();
        while (pages.iterator().hasNext()) {
            pageIDs.add(pages.iterator().next().getPageId());
        }

        if (pageIDs == null) {
            logger.error("Cannot compute average fan-out of an empty page set.");
            return 0.0;
        }

        int fanOutCounter = 0;

        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        Iterator results = session.createQuery("select page.outLinks, page.pageId from Page as page").list().iterator();
        while (results.hasNext()) {
            Object[] row = (Object[]) results.next();
            Set outLinks = (Set) row[0];
            Integer pageId = (Integer) row[1];

            // if the current page ID is in the desired result set => add outlink value
            if (pageIDs.contains(pageId)) {
                fanOutCounter += outLinks.size();
            }

        }
        session.getTransaction().commit();

        return (double) fanOutCounter / this.getNumberOfPages();
    }

    /**
     * @return Returns the averageFanOut.
     */
    public double getAverageFanOut() {
        if (averageFanOut < 0) {    // not yet initialized
            averageFanOut = computeAverageFanOut(this.pages);
        }

        return averageFanOut;
    }

    /**
     * @return Returns the numberOfPages.
     */
    public int getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Building a mapping from categories to article sets.
     * @parm pWiki The wikipedia object.
     * @param pNodes The category nodes that should be used to build the map.
     * @return A mapping from categories to article sets.
     * @throws WikiPageNotFoundException
     */
    private Map<Integer,Set<Integer>> getCategoryArticleMap(Wikipedia pWiki, Set<Integer> pNodes) throws WikiPageNotFoundException {
        Map<Integer,Set<Integer>> categoryArticleMap = new HashMap<Integer,Set<Integer>>();

        int progress = 0;
        for (int node : pNodes) {
            progress++;
            ApiUtilities.printProgressInfo(progress, pNodes.size(), 10, ApiUtilities.ProgressInfoMode.TEXT, "Getting category-article map.");

            Category cat = pWiki.getCategory(node);
            if (cat != null) {
                Set<Integer> pages = new HashSet<Integer>(cat.__getPages());
                categoryArticleMap.put(node, pages);
            }
            else {
                logger.info(node + " is not a category.");
            }
        }

        return categoryArticleMap;
    }

    /**
     * Get various graph parameters like diameter, average out-degree etc of the categroy graph.
     * @param catGraph The category graph.
     */
    public void getGraphParameters(CategoryGraph catGraph) {
        double startTime = System.currentTimeMillis();
        logger.error(catGraph.getGraphInfo());
        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.error(endTime + "s");
    }

    /**
     * Articles in wikipedia may be tagged with multiple categories.
     * It may be interesting to know how many articles have at least one category in common.
     * Such articles would have a very high semantic relatedness even if they share a quite secondary category.
     * @param pWiki The wikipedia object.
     * @param catGraph The category graph.
     * @throws WikiApiException
     */
    public void getOverlapping(Wikipedia pWiki, CategoryGraph catGraph) throws WikiApiException {
        double startTime = System.currentTimeMillis();

        int articlesWithOverlappingCategories = getArticlesWithOverlappingCategories(pWiki, catGraph);
        double overlappingCategoriesRatio = (double) articlesWithOverlappingCategories / (double) pWiki.getMetaData().getNumberOfPages();
        logger.error(articlesWithOverlappingCategories + " - " + pWiki.getMetaData().getNumberOfPages() + " - " + overlappingCategoriesRatio);

        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.error(endTime + "ms");
    }


    /**
     * Articles in wikipedia may be tagged with multiple categories.
     * It may be interesting to know how many articles have at least one category in common.
     * Such articles would have a very high semantic relatedness even if they share a quite secondary category.
     * @param pWiki The wikipedia object.
     * @param pGraph The category graph.
     * @return The number of articles that have at least one category in common.
     * @throws WikiPageNotFoundException
     */
    private int getArticlesWithOverlappingCategories(Wikipedia pWiki, CategoryGraph pGraph) throws WikiPageNotFoundException {
        Set<Integer> overlappingArticles = new HashSet<Integer>();

        // iterate over all node pairs
        Set<Integer> nodes = pGraph.getGraph().vertexSet();

        Map<Integer,Set<Integer>> categoryArticleMap = getCategoryArticleMap(pWiki, nodes);

        // sort the Array so we can use a simple iteration with two for loops to access all pairs
        Object[] nodeArray = nodes.toArray();
        Arrays.sort(nodeArray);

        int progress = 0;
        for (int i=0; i<nodes.size(); i++) {
            progress++;
            ApiUtilities.printProgressInfo(progress, nodes.size(), 100, ApiUtilities.ProgressInfoMode.TEXT, "");

            int outerNode = (Integer) nodeArray[i];

            for (int j=i+1; j<nodes.size(); j++) {
                int innerNode = (Integer) nodeArray[j];

                // test whether the categories have pages in common
                Set<Integer> outerPages = categoryArticleMap.get(outerNode);
                Set<Integer> innerPages = categoryArticleMap.get(innerNode);

                for (int outerPage : outerPages) {
                    if (innerPages.contains(outerPage)) {
                        if (!overlappingArticles.contains(outerPage)) {
                            overlappingArticles.add(outerPage);
                        }
                    }
                }

            }
        }

        return overlappingArticles.size();
    }

    public void getCategorizedArticles(Wikipedia pWiki, CategoryGraph catGraph) throws WikiApiException {
        double startTime = System.currentTimeMillis();

        int numberOfCategorizedArticles = getNumberOfCategorizedArticles(pWiki, catGraph);
        double categorizedArticlesRatio = (double) numberOfCategorizedArticles / (double) pWiki.getMetaData().getNumberOfPages();

        logger.info("Categorized articles: " + numberOfCategorizedArticles);
        logger.info("All articles:         " + pWiki.getMetaData().getNumberOfPages());
        logger.info("Ratio:                " + categorizedArticlesRatio);

        double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.error(endTime + "ms");
    }

    public double getAveragePathLengthFromRoot(Wikipedia pWiki, CategoryGraph connectedCatGraph) throws WikiApiException {
        // get root node
        Category rootCategory = pWiki.getMetaData().getMainCategory();
        int root = rootCategory.getPageId();

        int pathLengthSum = computeShortestPathLenghts(root, connectedCatGraph);

        return (double) pathLengthSum / (connectedCatGraph.getGraph().vertexSet().size()-1);
    }


    /**
     * If the return value has been already computed, it is returned, else it is computed at retrieval time.
     * @param pWiki The wikipedia object.
     * @param catGraph The category graph.
     * @return The number of categorized articles, i.e. articles that have at least one category.
     */
    public int getNumberOfCategorizedArticles(Wikipedia pWiki, CategoryGraph catGraph) throws WikiApiException{
        if (categorizedArticleSet == null) {    // has not been initialized yet
            iterateCategoriesGetArticles(pWiki, catGraph);
        }
        return categorizedArticleSet.size();
    }

    /**
     * Computes the distribution of the number of articles per category.
     * If the return value has been already computed, it is returned, else it is computed at retrieval time.
     * @param pWiki The wikipedia object.
     * @param catGraph The category graph.
     * @return A map containing the distribution mapping from a degree to the number of times this degree is found in the category graph.
     * @throws WikiPageNotFoundException
     */
    public Map<Integer,Integer> getDistributionOfArticlesByCategory(Wikipedia pWiki, CategoryGraph catGraph) throws WikiPageNotFoundException {
        if (degreeDistribution == null) {   // has not been initialized yet
            iterateCategoriesGetArticles(pWiki, catGraph);
        }
        return degreeDistribution;
    }

    /**
     * Methods computing stuff that have to iterate over all categories and access category articles can plug-in here.
     * Recently plugin-in:
     *      numberOfCategorizedArticles
     *      distributionOfArticlesByCategory
     * @param pWiki The wikipedia object.
     * @param catGraph The category graph.
     * @return
     * @throws WikiPageNotFoundException
     */
    private void iterateCategoriesGetArticles(Wikipedia pWiki, CategoryGraph catGraph) throws WikiPageNotFoundException {
        Map<Integer,Integer> localDegreeDistribution = new HashMap<Integer,Integer>();
        Set<Integer> localCategorizedArticleSet = new HashSet<Integer>();
        Set<Integer> categoryNodes = catGraph.getGraph().vertexSet();
        // iterate over all categories
        int progress = 0;
        for (int node : categoryNodes) {
            progress++;
            ApiUtilities.printProgressInfo(progress, categoryNodes.size(), 100, ApiUtilities.ProgressInfoMode.TEXT, "iterate over categories");

            // get the category
            Category cat = pWiki.getCategory(node);
            if (cat != null) {
                Set<Integer> pages = new HashSet<Integer>(cat.__getPages());

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
                logger.info(node + " is not a category.");
            }
        }
        this.degreeDistribution = localDegreeDistribution;
        this.categorizedArticleSet = localCategorizedArticleSet;
    }

    /**
     * Computes the shortest path from node to all other nodes.
     * As the JGraphT BreadthFirstIterator does not provide information about
     *   the distance to the start node in each step, we will use our own BFS implementation.
     * @param pStartNode The start node of the search.
     * @param catGraph The category graph.
     * @return An array of double values.
     */
    private int computeShortestPathLenghts(int pStartNode, CategoryGraph catGraph) {
        int shortestPathLengthSum = 0;

        // a set of nodes that have already been expanded -> algorithm should expand nodes monotonically and not go back
        Set<Integer> alreadyExpanded = new HashSet<Integer>();

        // a queue holding the newly discovered nodes with their and their distance to the start node
        List<int[]> queue = new ArrayList<int[]>();

        // initialize queue with start node
        int[] innerList = new int[2];
        innerList[0] = pStartNode;   // the node
        innerList[1] = 0;           // the distance to the start node
        queue.add(innerList);

        // while the queue is not empty
        while (!queue.isEmpty()) {
            // remove first element from queue
            int[] queueElement = queue.get(0);
            int currentNode = queueElement[0];
            int distance = queueElement[1];
            queue.remove(0);

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
