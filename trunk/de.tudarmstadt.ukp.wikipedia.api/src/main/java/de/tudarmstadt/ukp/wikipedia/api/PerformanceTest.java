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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.util.GraphUtilities;

public class PerformanceTest implements WikiConstants {

	private final Log logger = LogFactory.getLog(getClass());

    private final Wikipedia wiki;
    private final Set<Integer> pageIDs;
    private List<List<Integer>> randomIdList;
    private List<List<String>> randomTitleList;

    // determines how many sample pageCycles are run for averaging results
    int maxiCycles = 5;
    int pageCycles = 50;

    public static void main(String[] args) throws WikiApiException {

//        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
//        dbConfig.setDatabase("wikiapi_de");
//        dbConfig.setHost("homer.tk.informatik.tu-darmstadt.de");
//        dbConfig.setUser("wikiapi");
//        dbConfig.setPassword("wikiapitk");
//        dbConfig.setLanguage(Language.german);

        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDatabase("wikiapi_en");
        dbConfig.setHost("bender.ukp.informatik.tu-darmstadt.de");
        dbConfig.setUser("wikiapi");
        dbConfig.setPassword("wikiapitk");
        dbConfig.setLanguage(Language.english);

        Wikipedia wiki = new Wikipedia(dbConfig);

        PerformanceTest pt = new PerformanceTest(wiki);

    	System.out.println("intern page loading");
    	pt.loadPagesTest("intern");
        System.out.println();

    	System.out.println("extern page loading");
    	pt.loadPagesTest("extern");
        System.out.println();

    	System.out.println("intern page loading and field accessing");
        pt.loadPagesAndAccessFieldsTest("intern");
        System.out.println();

        System.out.println("extern page loading and field accessing");
    	pt.loadPagesAndAccessFieldsTest("extern");
        System.out.println();

// TODO test iteration speed

    }

    public PerformanceTest(Wikipedia pWiki) throws WikiApiException {
        this.wiki = pWiki;
        pageIDs = wiki.__getPages();
        initializeLists(pageIDs);
    }

    private void initializeLists(Set<Integer> allPageIDs) throws WikiApiException {
        randomIdList = new ArrayList<List<Integer>>();
        randomTitleList = new ArrayList<List<String>>();

    	for (int j=0; j<maxiCycles; j++) {
    		Set<Integer> randomPageIds = GraphUtilities.getRandomPageSubset(allPageIDs, pageCycles);
    		List<Integer> randomPageIdList = new ArrayList<Integer>( randomPageIds );
    		randomIdList.add(randomPageIdList);

    		List<String> randomPageTitles = new ArrayList<String>();
    		for (int id : randomPageIds) {
    			Page p = wiki.getPage(id);
    			randomPageTitles.add(p.getTitle().getWikiStyleTitle());
    		}
    		randomTitleList.add(randomPageTitles);
        }

    }

    private void loadPagesTest(String mode) throws WikiApiException {
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
            System.out.println("throughput: " + throughput + " pages/ms");
        }
        averageThroughput /= maxiCycles;

        System.out.println("-----------------");
        System.out.println("average throughput: " + averageThroughput + " pages/ms");
        System.out.println("average throughput: " + averageThroughput*1000 + " pages/s");
        System.out.println("-----------------");
        System.out.println("");
    }

    private void loadPagesAndAccessFieldsTest(String mode) throws WikiApiException {
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
            System.out.println("throughput: " + throughput + " pages/ms");
        }
        averageThroughput /= maxiCycles;

        System.out.println("-----------------");
        System.out.println("average throughput: " + averageThroughput + " pages/ms");
        System.out.println("average throughput: " + averageThroughput*1000 + " pages/s");
        System.out.println("-----------------");
        System.out.println("");
    }

    @SuppressWarnings("unused")
    private void loadPage(long id) throws WikiApiException {
      try {
        Page page = new Page(this.wiki, id);
      } catch (WikiPageNotFoundException e) {}
    }

    @SuppressWarnings("unused")
    private void loadPage(String title) throws WikiApiException {
        try {
          Page page = wiki.getPage(title);
        } catch (WikiPageNotFoundException e) {}
      }

    @SuppressWarnings("unused")
    private void loadPageAndAccessFields_intern(long id) throws WikiApiException {
        try {
            Page page = new Page(this.wiki, id);
            Set<Integer> inLinks = page.getInlinkIDs();
            Set<Integer> outLinks = page.getOutlinkIDs();
            String text = page.getText();
        } catch (WikiPageNotFoundException e) {}
    }

    @SuppressWarnings("unused")
    private void loadPageAndAccessFields_extern(long id) throws WikiApiException {
        try {
            Page page = new Page(this.wiki, id);
            Set<Page> inLinks = page.getInlinks();
            Set<Page> outLinks = page.getOutlinks();
            String text = page.getText();
        } catch (WikiPageNotFoundException e) {}
    }
}
