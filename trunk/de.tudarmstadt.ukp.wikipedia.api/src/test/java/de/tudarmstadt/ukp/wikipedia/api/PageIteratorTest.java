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

import java.util.Iterator;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class PageIteratorTest {

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


	/**
	 * The test wikipedia contains 28 articles + 2 disambiguation pages.
	 */
	@Test
	public void test_pageIteratorTest() {
		int nrOfPages = 0;
		int nrOfArticles = 0;

		Iterator<Page> pageIter = wiki.getPages().iterator();
        Iterator<Page> articleIter = wiki.getArticles().iterator();

		while (pageIter.hasNext()) {
			@SuppressWarnings("unused")
			Page p = pageIter.next();
			nrOfPages++;
		}
		assertEquals("Number of pages == 30", 30, nrOfPages);

		while (articleIter.hasNext()) {
			@SuppressWarnings("unused")
			Page p = articleIter.next();
			nrOfArticles++;
		}
		assertEquals("Number of articles == 28", 28, nrOfArticles);

	}

	/**
	 * The test wikipedia contains 28 articles + 2 disambiguation pages.
	 */
	@Test
	public void test_pageIteratorTestBufferSize() {

		for (int bufferSize=1;bufferSize<=100;bufferSize+=5) {
			Iterator<Page> pageIter = wiki.getPages(bufferSize).iterator();
			int nrOfPages = 0;
			while (pageIter.hasNext()) {
				@SuppressWarnings("unused")
				Page p = pageIter.next();
				nrOfPages++;
			}
			assertEquals("Number of pages == 30", 30, nrOfPages);
		}
	}
}
