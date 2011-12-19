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
import static org.junit.Assume.assumeNoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class CategoryDescendantsIteratorTest {

	private static Wikipedia wiki;

	/**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests would also not be executed but marked as passed)
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
	 * The category UKP has 9 descendants with pageIds 7-15.
	 */
	@Test
	public void test_categoryIteratorTest() {

        Category cat = null;
        try {
            cat = wiki.getCategory("UKP");
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occured while getting the category 'UKP'");
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
            fail("A WikiApiException occured while getting the category 'UKP'");
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
