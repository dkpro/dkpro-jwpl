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
import static org.junit.Assume.assumeNoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class CategoryDescendantsIteratorTest extends BaseJWPLTest{

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
	 * The category UKP has 9 descendants with pageIds 7-15.
	 */
	@Test
	public void test_categoryIteratorTest() {

        Category cat = null;
        try {
            cat = wiki.getCategory("UKP");
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occurred while getting the category 'UKP'");
        }


        List<Integer> expectedPageIds = new ArrayList<Integer>();
        expectedPageIds.add(7);
        expectedPageIds.add(8);
        expectedPageIds.add(9);
        expectedPageIds.add(10);
        expectedPageIds.add(11);
        expectedPageIds.add(12);
        expectedPageIds.add(13);
        expectedPageIds.add(14);
        expectedPageIds.add(15);

        List<Integer> isIds = new ArrayList<Integer>();
        for(Category descendant : cat.getDescendants()) {
            isIds.add(descendant.getPageId());
        }
        Collections.sort(expectedPageIds);
        Collections.sort(isIds);
        assertEquals("descendants", expectedPageIds, isIds);
	}

    /**
     * The category UKP has 9 descendants with pageIds 7-15.
     */
	@Test
	public void test_categoryIteratorTestBufferSize() {

        Category cat = null;
        try {
            cat = wiki.getCategory("UKP");
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occurred while getting the category 'UKP'");
        }


        List<Integer> expectedPageIds = new ArrayList<Integer>();
        expectedPageIds.add(7);
        expectedPageIds.add(8);
        expectedPageIds.add(9);
        expectedPageIds.add(10);
        expectedPageIds.add(11);
        expectedPageIds.add(12);
        expectedPageIds.add(13);
        expectedPageIds.add(14);
        expectedPageIds.add(15);

		for (int bufferSize=1;bufferSize<=100;bufferSize+=5) {
            List<Integer> isIds = new ArrayList<Integer>();
            for(Category descendant : cat.getDescendants(bufferSize)) {
                isIds.add(descendant.getPageId());
            }
            Collections.sort(expectedPageIds);
            Collections.sort(isIds);
            assertEquals("descendants", expectedPageIds, isIds);

        }
	}
}
