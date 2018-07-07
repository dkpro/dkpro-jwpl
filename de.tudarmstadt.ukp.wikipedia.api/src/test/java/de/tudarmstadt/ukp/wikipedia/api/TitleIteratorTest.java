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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

public class TitleIteratorTest extends BaseJWPLTest{

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


	@Test
	public void test_titleIteratorTest() {

        int nrOfTitles = 0;
		Iterable<Title> iterable = wiki.getTitles();
		assertNotNull(iterable);
		Iterator<Title> titleIter = iterable.iterator();
		while (titleIter.hasNext()) {
			Title t = titleIter.next();
			assertNotNull(t);
			nrOfTitles++;
		}
		assertEquals("Number of titles == 37", 37, nrOfTitles);

	}
}
