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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

public class PageIteratorTest extends BaseJWPLTest {

	/**
	 * Made this static so that following tests don't run if assumption fails.
	 * (With AT_Before, tests also would not be executed but marked as passed)
	 * This could be changed back as soon as JUnit ignored tests after failed
	 * assumptions
	 */
	@BeforeClass
	public static void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			fail("Wikipedia could not be initialized: "+e.getLocalizedMessage());
		}
	}


	/**
	 * The test wikipedia contains 29 articles + 2 disambiguation + 1 discussion pages.
	 */
	@Test
	public void test_pageIteratorTest() {
		int nrOfPages = 0;
		int nrOfArticles = 0;

		Iterator<Page> pageIter = wiki.getPages().iterator();
        Iterator<Page> articleIter = wiki.getArticles().iterator();

		while (pageIter.hasNext()) {
			Page p = pageIter.next();
			assertNotNull(p);
			nrOfPages++;
		}
		assertEquals("Number of pages == 33", 33, nrOfPages);

		while (articleIter.hasNext()) {
			Page p = articleIter.next();
			assertNotNull(p);
			nrOfArticles++;
		}
		
		// Assuming 32 is the correct number now
		assertEquals("Number of articles == 33", 33, nrOfArticles);

	}

	/**
	 * The test wikipedia contains 30 articles + 2 disambiguation + 1 discussion pages.
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
			assertEquals("Number of pages == 33", 33, nrOfPages);
		}
	}
}
