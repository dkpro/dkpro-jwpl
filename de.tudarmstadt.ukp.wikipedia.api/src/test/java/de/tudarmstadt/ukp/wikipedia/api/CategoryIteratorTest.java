/*******************************************************************************
 * Copyright 2017
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
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

public class CategoryIteratorTest extends BaseJWPLTest {

    @BeforeClass
	public void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			fail("Wikipedia could not be initialized.");
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
