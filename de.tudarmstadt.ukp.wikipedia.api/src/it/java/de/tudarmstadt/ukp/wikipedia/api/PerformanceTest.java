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
package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.util.GraphUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Encapsulates the integration test code that stresses a Wikipedia backend to check the performance of it.
 */
class PerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Wikipedia wiki;

    private final Set<Integer> pageIDs;
    private List<List<Integer>> randomIdList;
    private List<List<String>> randomTitleList;

    // determines how many sample pageCycles are run for averaging results
    private int maxiCycles;
    private int pageCycles;

    PerformanceTest(Wikipedia pWiki, int maxiCycles, int pageCycles) throws WikiApiException {
        this.wiki = pWiki;
        this.maxiCycles = maxiCycles;
        this.pageCycles = pageCycles;
        pageIDs = wiki.__getPages();
        initializeLists(pageIDs);
    }

    private void initializeLists(Set<Integer> allPageIDs) throws WikiApiException {
        randomIdList = new ArrayList<>();
        randomTitleList = new ArrayList<>();

        for (int j=0; j<maxiCycles; j++) {
            Set<Integer> randomPageIds = GraphUtilities.getRandomPageSubset(allPageIDs, pageCycles);
            List<Integer> randomPageIdList = new ArrayList<>( randomPageIds );
            randomIdList.add(randomPageIdList);

            List<String> randomPageTitles = new ArrayList<>();
            for (int id : randomPageIds) {
                Page p = wiki.getPage(id);
                randomPageTitles.add(p.getTitle().getWikiStyleTitle());
            }
            randomTitleList.add(randomPageTitles);
        }

    }

    void loadPagesTest(String mode) throws WikiApiException {
        double averageThroughput = 0;
        for (int j=0; j<maxiCycles; j++) {
            double averageTime = 0;
            for (int i=0; i<pageCycles; i++) {

                long id = wiki.__getPageHibernateId( randomIdList.get(j).get(i) );
                String title = randomTitleList.get(j).get(i);

                double startTime = System.currentTimeMillis();
                if (mode.equals("intern")) {
                    loadPage(id);
                }
                else {
                    loadPage(title);
                }
                double elapsedTime = (System.currentTimeMillis() - startTime);
                averageTime += elapsedTime;
            }
            double throughput = pageCycles / averageTime;
            averageThroughput += throughput;
            logger.trace("throughput: {} pages/ms", throughput);
        }
        averageThroughput /= maxiCycles;

        logger.debug("-----------------");
        logger.debug("average throughput: {} pages/ms", averageThroughput);
        logger.debug("average throughput: {} pages/s", averageThroughput*1000);
        logger.debug("-----------------");
    }

    void loadPagesAndAccessFieldsTest(String mode) throws WikiApiException {
        double averageThroughput = 0;
        for (int j=0; j<maxiCycles; j++) {
            double averageTime = 0;
            for (int i=0; i<pageCycles; i++) {

                Set<Integer> page = GraphUtilities.getRandomPageSubset(pageIDs, 1);
                Iterator it = page.iterator();
                int pageID = (Integer) it.next();
                long id = wiki.__getPageHibernateId(pageID);

                double startTime = System.currentTimeMillis();
                if (mode.equals("intern")) {
                    loadPageAndAccessFields_intern(id);
                }
                else {
                    loadPageAndAccessFields_extern(id);
                }
                double elapsedTime = (System.currentTimeMillis() - startTime);
                averageTime += elapsedTime;
            }
            double throughput = pageCycles / averageTime;
            averageThroughput += throughput;
            logger.trace("throughput: {} pages/ms", throughput);
        }
        averageThroughput /= maxiCycles;

        logger.debug("-----------------");
        logger.debug("average throughput: {} pages/ms", averageThroughput);
        logger.debug("average throughput: {} pages/s", averageThroughput*1000);        logger.debug("-----------------");
    }

    private void loadPage(long id) throws WikiApiException {
        Page page = new Page(this.wiki, id);
        assertNotNull(page);
    }

    private void loadPage(String title) throws WikiApiException {
        Page page = wiki.getPage(title);
        assertNotNull(page);
    }

    private void loadPageAndAccessFields_intern(long id) throws WikiApiException {
        Page page = new Page(this.wiki, id);
        Set<Integer> inLinks = page.getInlinkIDs();
        assertNotNull(inLinks);
        Set<Integer> outLinks = page.getOutlinkIDs();
        assertNotNull(outLinks);
        String text = page.getText();
        assertNotNull(text);
    }

    private void loadPageAndAccessFields_extern(long id) throws WikiApiException {
        Page page = new Page(this.wiki, id);
        Set<Page> inLinks = page.getInlinks();
        assertNotNull(inLinks);
        Set<Page> outLinks = page.getOutlinks();
        assertNotNull(outLinks);
        String text = page.getText();
        assertNotNull(text);
    }

    /**
     * This is a test class for the version of PageIterator, that buffers a
     * certain number of pages in order to gain efficiency.
     * We get the same number of pages from a Wikipedia using
     * different buffer sizes and return the performance.
     *
     * For an unbuffered iterator set bufferSize to 1.
     */
    void loadPageAndIterate(int numberOfPages, int bufferSize, Wikipedia wiki) {
        long from = System.currentTimeMillis();
        Iterator<Page> pages = wiki.getPages(bufferSize).iterator();
        int counter = 0;
        while (counter < numberOfPages && pages.hasNext()) {
            pages.next();
            counter++;
        }
        long to = System.currentTimeMillis();
        logger.debug("RetrievedPages  : " + counter);
        logger.debug("Used Buffer Size: " + bufferSize);
        logger.debug("Time            : " + (to - from) + "ms");
        logger.debug("------------------------------");
    }
}
