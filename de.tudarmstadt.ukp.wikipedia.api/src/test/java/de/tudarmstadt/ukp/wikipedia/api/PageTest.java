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
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class PageTest {

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

	@Test
	public void testPageTitle(){
		String title = "Wikipedia API";
        Page p = null;
		try {
			p = wiki.getPage("Wikipedia API");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page " + title);
		}
		//test the title
		try {
			assertEquals("testing the title", title, p.getTitle().getPlainTitle().toString());
		} catch (WikiTitleParsingException e) {
			e.printStackTrace();
			fail("A WikiTitleParsingException occured while testing the title of the page " + title);
		}

	}

	@Test
	public void testPageId(){
        String title = "Wikipedia API";
		Page p = null;
		try {
			p = wiki.getPage(title);
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page " + title);
		}
		//test the pageId
		assertEquals("testing the pageId", 1014, p.getPageId());
	}

}
