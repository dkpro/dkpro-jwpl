/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNoException;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class CategoryIteratorTest {

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
			assumeNoException(e);
			//fail("Wikipedia could not be initialized.");
		}
	}


	/**
	 * The test wikipedia contains 17 categories.
	 */
	@Test
	public void test_categoryIteratorTest() {
		int nrOfPages = 0;

		Iterator<Category> catIter = wiki.getCategories().iterator();

		while (catIter.hasNext()) {
			@SuppressWarnings("unused")
			Category c = catIter.next();
			nrOfPages++;
		}
		assertEquals("Number of categories == 17", 17, nrOfPages);

	}

	/**
	 * The test wikipedia contains 17 categories.
	 */
	@Test
	public void test_categoryIteratorTestBufferSize() {

		for (int bufferSize=1;bufferSize<=100;bufferSize+=5) {
			Iterator<Category> catIter = wiki.getCategories(bufferSize).iterator();
			int nrOfPages = 0;
			while (catIter.hasNext()) {
				@SuppressWarnings("unused")
				Category c = catIter.next();
				nrOfPages++;
			}
			assertEquals("Number of categories == 17", 17, nrOfPages);
		}
	}
}
