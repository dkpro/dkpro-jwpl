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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikipediaTest {

	private static Wikipedia wiki;

	/**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests also would not be executed but marked as passed)
     * This could be changed back as soon as JUnit ignored tests after failed
     * assumptions
	 */
	@BeforeClass
	public static void setupWikipedia() {
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_test");
		db.setHost("bender.ukp.informatik.tu-darmstadt.de");
		db.setUser("student");
		db.setPassword("student");
		db.setLanguage(Language._test);
		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			Assume.assumeNoException(e);
			//fail("Wikipedia could not be initialized.");
		}
	}

	/*
	 * We test the returned pages with testing their pageId and their title.
     * We also expect a WikiApiException to be thrown when trying to get non existing page.
	 */
	@Test
	public void testGetPage() {
		getExistingPage("Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
		getExistingPage("Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
      	getExistingPage("exploring the Potential of Semantic Relatedness in Information Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
      	getExistingPage("exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
		getExistingPage("TK2", 105);
		getNotExistingPage("TK2 ");
		getNotExistingPage(" TK2");
		getNotExistingPage("TK4");
		getNotExistingPage("");
		getExistingPage("UKP", 1041);
/*
 * TODO the following pages should NOT be found. They are found due to case insensitive querying
 */
//		getNotExistingPage("Ukp");
//		getNotExistingPage("UkP");
//		getNotExistingPage("uKP");
	}

	private void getNotExistingPage(String title) {
		boolean exceptionThrown = false;
		try {
			wiki.getPage(title);
		} catch (WikiApiException e) {
			exceptionThrown = true;
		}
		assertTrue("Testing the WikiApiException for non existing page: " + title, exceptionThrown);
	}

    private void getExistingPage(String title, int pageId) {
        getExistingPage(title, title, pageId);
    }

    private void getExistingPage(String keyword, String title, int pageId) {
		Page p = null;
		try {
			p = wiki.getPage(keyword);
		} catch (WikiApiException e) {
			fail("A WikiApiException occured while getting the page: '" + keyword + "'");
		}

		assertEquals("testing the pageId of '" + title + "'", pageId, p.getPageId());

		try {
			assertEquals("testing the title of '" + title + "'", title.trim(), p.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occured while getting the title of " + title);
		}
	}
}
