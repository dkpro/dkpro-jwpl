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

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

public class PageIteratorTest extends BaseJWPLTest {

	@Before
	public void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		
		try {
			wiki = new Wikipedia(db);
		} catch (WikiInitializationException e) {
			fail("Wikipedia could not be initialized.");
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
