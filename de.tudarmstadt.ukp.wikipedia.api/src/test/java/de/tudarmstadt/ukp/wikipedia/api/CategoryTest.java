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

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class CategoryTest extends BaseJWPLTest {

    private static final String A_FAMOUS_CATEGORY = "People of UKP";
    private static final int A_FAMOUS_PAGE_ID = 8;
    // Here: ORMs internal object identifier aka Primary Key.
    private static final long A_FAMOUS_PAGE_OBJECT_ID = 4;

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
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCategoryTitle(){
		Category cat;
		try {
			cat = wiki.getCategory(A_FAMOUS_CATEGORY);
			assertNotNull(cat);
			assertEquals("testing the title","People of UKP", cat.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while testing the title of the category 'People of UKP': " + e.getLocalizedMessage());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCategoryPageId(){
		Category cat;
		try {
			cat = wiki.getCategory(A_FAMOUS_CATEGORY);
			assertNotNull(cat);
		    assertEquals("testing the pageId",8,cat.getPageId());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
		}
		//test the pageId
	}

	@Test
	public void testCategoryParents(){
		Category cat;
		try {
			cat = wiki.getCategory(A_FAMOUS_CATEGORY);
			assertNotNull(cat);
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
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
		}
	}

    @Test
    public void testNumberOfCategoryParents(){
        Category cat;
        try {
            cat = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat);
            int numberOfParents = cat.getNumberOfParents();
            // expecting IDs: 5 and 6 to make up for 2 parent categories
            assertEquals(2, numberOfParents);
            
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCategoryDescendants(){
        Category cat;
        try {
            cat = wiki.getCategory("UKP");
			assertNotNull(cat);
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
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the category 'UKP': " + e.getLocalizedMessage());
        }
    }

    @Test
	public void testCategoryChildren(){
		Category cat;
		try {
			cat = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat);
            List<Integer> expectedPageIds = new ArrayList<Integer>();
            List<Integer> isIds = new ArrayList<Integer>();
            //test the children
            expectedPageIds.clear();
            expectedPageIds.add(13);
            expectedPageIds.add(12);
            expectedPageIds.add(15);
            expectedPageIds.add(14);
            for(Category child : cat.getChildren()) {
                isIds.add(child.getPageId());
            }
            Collections.sort(expectedPageIds);
            Collections.sort(isIds);
            assertEquals("children",expectedPageIds,isIds);
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
		}
	}

    @Test
    public void testNumberOfCategoryChildren(){
        Category cat;
        try {
            cat = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat);
            // expecting IDs: 12, 13, 14 and 15 to make up for 4 child categories
            int expectedNumberOfChildren = cat.getNumberOfChildren();
            assertEquals(4, expectedNumberOfChildren);

        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }

	@Test
	public void testCategoryPages(){
		Category cat;
		try {
			cat = wiki.getCategory("UKP");
            assertNotNull(cat);
            List<Integer> expectedPageIds = new ArrayList<Integer>();
            expectedPageIds.add(1010);
            expectedPageIds.add(1041);
            List<Integer> isIds = new ArrayList<Integer>();
            try {
                Set<Page> pages = cat.getArticles();
                assertNotNull(pages);
                assertFalse(pages.isEmpty());
                for(Page p : pages) {
                    isIds.add(p.getPageId());
                }
            } catch (WikiApiException e) {
                fail("A WikiApiException occurred while getting the pages of the category 'People of UKP' for testing: " + e.getLocalizedMessage());
            }
            Collections.sort(expectedPageIds);
            Collections.sort(isIds);
            assertEquals("page",expectedPageIds,isIds);
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
		}
	}

    @Test
    public void testNumberOfCategoryPages(){
        Category cat;
        try {
            cat = wiki.getCategory("UKP");
            assertNotNull(cat);
            int numberOfPages = cat.getNumberOfPages();
            assertTrue(numberOfPages > 0);
            assertEquals(2, numberOfPages);
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateCategoryByPageID() {
        try {
            Category p = new Category(wiki, A_FAMOUS_PAGE_ID);
            assertNotNull(p);
            assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateCategoryByPageIDInvalid() {
        try {
            new Category(wiki, -42);
        } catch (WikiPageNotFoundException pnfe) {
            // this is expected behavior here, provoked by the test
        }
    }

    @Test
    public void testCreateCategoryByObjectID() {
        try {
            Category p = new Category(wiki, A_FAMOUS_PAGE_OBJECT_ID);
            assertNotNull(p);
            assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
            assertEquals(A_FAMOUS_PAGE_OBJECT_ID, p.__getId());
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateCategoryByObjectIDInvalid() {
        try {
            long invalidObjectID = -42L;
            new Category(wiki, invalidObjectID);
        } catch (WikiPageNotFoundException pnfe) {
            // this is expected behavior here, provoked by the test
        }
    }

    @Test
    public void testCreateCategoryByName() {
        try {
            Category p = new Category(wiki, A_FAMOUS_CATEGORY);
            assertNotNull(p);
            assertEquals(A_FAMOUS_CATEGORY, p.getTitle().getPlainTitle());
            assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateCategoryByNameRandom() {
        try {
            new Category(wiki, UUID.randomUUID().toString());
        } catch (WikiApiException e) {
            // this is expected, as a random page title should not be found
        }
    }

    @Test
    public void testCreateCategoryByNameEmpty() {
        try {
            new Category(wiki, "");
        } catch (WikiPageNotFoundException pnfe) {
            // this is expected behavior here, provoked by the test
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCreateCategoryByNameNull() {
        try {
            new Category(wiki, null);
        } catch (WikiPageNotFoundException pnfe) {
            // this is expected behavior here, provoked by the test
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetCategoryInfo() {
        try {
            Category p = new Category(wiki, "UKP");
            assertNotNull(p);
            String categoryInfo = p.getCategoryInfo();
            assertNotNull(categoryInfo);
            assertTrue(categoryInfo.length() > 0);
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the page info: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCategorySiblings(){
        Category cat;
        try {
            cat = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat);
            Set<Integer> expectedPageIds = new HashSet<Integer>();
            Set<Integer> isIds = new HashSet<Integer>();
            //test the children
            expectedPageIds.clear();
            expectedPageIds.add(7);
            expectedPageIds.add(8);
            expectedPageIds.add(9);
            Set<Category> siblings = cat.getSiblings();
            assertNotNull(siblings);
            assertTrue(siblings.size() > 0);
            for(Category sibling : siblings) {
                isIds.add(sibling.getPageId());
            }
            assertEquals("siblings",expectedPageIds,isIds);
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCategoryTitleComparatorEquality() {
        Category cat1;
        Category cat2;
        try {
            cat1 = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat1);
            cat2 = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat2);

            List<Category> categories = new ArrayList<Category>();
            categories.add(cat1);
            categories.add(cat2);
            categories.sort(new CategoryTitleComparator());

            assertEquals(cat1, categories.get(0));
            assertEquals(cat2, categories.get(1));

        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while comparing the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testCategoryTitleComparatorNewOrder() {
        Category cat1;
        Category cat2;
        try {
            cat1 = wiki.getCategory("UKP");
            assertNotNull(cat1);
            // this category (People of U..) should be re-ordered before UKP
            cat2 = wiki.getCategory(A_FAMOUS_CATEGORY);
            assertNotNull(cat2);

            List<Category> categories = new ArrayList<Category>();
            categories.add(cat1);
            categories.add(cat2);
            categories.sort(new CategoryTitleComparator());

            assertEquals(cat2, categories.get(0));
            assertEquals(cat1, categories.get(1));

        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while comparing the category 'People of UKP': " + e.getLocalizedMessage());
        }
    }
}
