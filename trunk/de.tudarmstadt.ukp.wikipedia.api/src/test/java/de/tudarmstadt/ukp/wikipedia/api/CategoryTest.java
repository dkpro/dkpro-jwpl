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
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class CategoryTest {

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

	@Test
	public void testCategoryTitle(){
		//we test the category 'People of UKP'
		//get the category
		Category cat = null;
		try {
			cat = wiki.getCategory("People of UKP");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category 'People of UKP'");
		}
		//test the title
		try {
			assertEquals("testing the title","People of UKP",cat.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			e.printStackTrace();
			fail("A WikiTitleParsingException occured while testing the title of the cateogry 'People of UKP'");
		}

	}

	@Test
	public void testCategoryPageId(){
		//we test the category 'People of UKP'
		//get the category
		Category cat = null;
		try {
			cat = wiki.getCategory("People of UKP");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category 'People of UKP'");
		}
		//test the pageId
		assertEquals("testing the pageId",8,cat.getPageId());
	}

	@Test
	public void testCategoryParents(){
		//we test the category 'People of UKP'
		//get the category
		Category cat = null;
		try {
			cat = wiki.getCategory("People of UKP");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category 'People of UKP'");
		}
		//test the parents
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		expectedPageIds.add(5);
        expectedPageIds.add(6);

        List<Integer> isIds = new ArrayList<Integer>();
		for(Category parent : cat.getParents()) {
            isIds.add(parent.getPageId());
        }
		Collections.sort(expectedPageIds);
		Collections.sort(isIds);
		assertEquals("parents", expectedPageIds, isIds);
	}

    @Test
    public void testCategoryDescendants(){
        Category cat = null;
        try {
            cat = wiki.getCategory("UKP");
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occured while getting the category 'UKP'");
        }

        //test the descendants
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

    @Test
	public void testCategoryChildren(){
		//we test the category 'People of UKP'
		//get the category
		Category cat = null;
		try {
			cat = wiki.getCategory("People of UKP");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category 'People of UKP'");
		}
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		List<Integer> isIds = new ArrayList<Integer>();
		//test the children
		expectedPageIds.clear();
		expectedPageIds.add(13);expectedPageIds.add(12);expectedPageIds.add(15);
		expectedPageIds.add(14);
		for(Category child : cat.getChildren()) {
			isIds.add(child.getPageId());
		}
		Collections.sort(expectedPageIds);
		Collections.sort(isIds);
		assertEquals("children",expectedPageIds,isIds);
	}

	@Test
	public void testCategoryPages(){
		//we test the category 'People of UKP'
		//get the category
		Category cat = null;
		try {
			cat = wiki.getCategory("People of UKP");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category 'People of UKP'");
		}
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		List<Integer> isIds = new ArrayList<Integer>();
		try {
			for(Page p : cat.getPages()) {
				isIds.add(p.getPageId());
			}
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the pages of the category 'People of UKP' for testing.");
		}
		Collections.sort(expectedPageIds);
		Collections.sort(isIds);
		assertEquals("page",expectedPageIds,isIds);
	}

}
