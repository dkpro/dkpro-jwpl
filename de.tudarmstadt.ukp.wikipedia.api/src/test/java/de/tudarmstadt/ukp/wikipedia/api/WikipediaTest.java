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


import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class WikipediaTest extends BaseJWPLTest{

	private final Log logger = LogFactory.getLog(getClass());

	private static Wikipedia wiki;

	private static final String A_FAMOUS_PAGE = "Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval";
	private static final String A_FAMOUS_PAGE_CLEAN = "Exploring the Potential of Semantic Relatedness in Information Retrieval";
	private static final int A_FAMOUS_PAGE_ID = 1017;

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

	/*
	 * We test the returned pages with testing their pageId and their title.
     * We also expect a WikiApiException to be thrown when trying to get non existing page.
	 */
	@Test
	public void testGetPageByTitleVariations() {
		getExistingPage(A_FAMOUS_PAGE_CLEAN, A_FAMOUS_PAGE_ID);
		getExistingPage("Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", A_FAMOUS_PAGE_CLEAN, A_FAMOUS_PAGE_ID);
      	getExistingPage("exploring the Potential of Semantic Relatedness in Information Retrieval", A_FAMOUS_PAGE_CLEAN, A_FAMOUS_PAGE_ID);
      	getExistingPage("exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", A_FAMOUS_PAGE_CLEAN, A_FAMOUS_PAGE_ID);
		getExistingPage("TK2", 105);
		getNotExistingPage("TK2 ");
		getNotExistingPage(" TK2");
		getNotExistingPage("TK4");
		getNotExistingPage("");
		getExistingPage("UKP", 1041);
		/*
		 * TODO the following pages should NOT be found. They are found due to case insensitive querying
		 */
//		getNotExistingPage("Ukp");
//		getNotExistingPage("UkP");
//		getNotExistingPage("uKP");
	}

	@Test
	public void testGetPageByID() {
		checkPage(A_FAMOUS_PAGE_ID, null);
	}

	@Test
	public void testGetPageByTitle() {
		checkPage(null, A_FAMOUS_PAGE);
	}

	@Test
	public void testGetPageByExactTitle() {
		try {
			checkGetPageByExactTitle("UKP");
		} catch (WikiPageNotFoundException nfe) {
			fail("Encountered WikiPageNotFoundException: " + nfe.getLocalizedMessage());
		} catch (WikiApiException ae) {
			fail("Encountered WikiApiException: " + ae.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageByExactTitleNull() {
		try {
			checkGetPageByExactTitle(null);
		} catch (WikiPageNotFoundException nfe) {
			// this is expected here
		} catch (WikiApiException ae) {
			fail("Expected a WikiPageNotFoundException, yet encountered WikiApiException: "
					+ ae.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageByExactTitleEmpty() {
		try {
			checkGetPageByExactTitle("");
		} catch (WikiPageNotFoundException nfe) {
			// this is expected here
		} catch (WikiApiException ae) {
			fail("Expected a WikiPageNotFoundException, yet encountered WikiApiException: "
					+ ae.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPagesByTitle() {
		try {
			Set<Page> pages = wiki.getPages(A_FAMOUS_PAGE);
			assertNotNull(pages);
			assertEquals(1, pages.size());
			assertEquals(A_FAMOUS_PAGE_ID, pages.iterator().next().getPageId());
		} catch (WikiApiException e) {
			logger.error(e.getLocalizedMessage(), e);
			fail("Encountered WikiApiException: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageIdsByTitle() {
		try {
			List<Integer> pageIDs = wiki.getPageIds(A_FAMOUS_PAGE);
			assertNotNull(pageIDs);
			assertEquals(1, pageIDs.size());
			assertTrue(pageIDs.contains(A_FAMOUS_PAGE_ID));
		} catch (WikiApiException e) {
			logger.error(e.getLocalizedMessage(), e);
			fail("Encountered WikiApiException: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageIdsByTitleInvalid() {
		try {
			wiki.getPageIds(UUID.randomUUID().toString());
		} catch (WikiPageNotFoundException wpnfe) {
			// this is expected here
		} catch (WikiApiException wae) {
			fail("Expected a WikiPageNotFoundException, yet encountered WikiApiException: "
					+ wae.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageIdsCaseInsensitive() {
		try {
			List<Integer> pageIDs = wiki.getPageIdsCaseInsensitive(A_FAMOUS_PAGE);
			assertNotNull(pageIDs);
			assertEquals(1, pageIDs.size());
			assertTrue(pageIDs.contains(A_FAMOUS_PAGE_ID));
		} catch (WikiApiException e) {
			logger.error(e.getLocalizedMessage(), e);
			fail("Encountered WikiApiException: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageIdsCaseInsensitiveInvalid() {
		try {
			wiki.getPageIdsCaseInsensitive(UUID.randomUUID().toString());
		} catch (WikiPageNotFoundException wpnfe) {
			// this is expected here
		} catch (WikiApiException wae) {
			fail("Expected a WikiPageNotFoundException, yet encountered WikiApiException: "
					+ wae.getLocalizedMessage());
		}
	}

	@Test
	public void testExistsPageByTitle() {
		assertTrue(wiki.existsPage(A_FAMOUS_PAGE));
	}

	@Test
	public void testExistsPageByTitleInvalid() {
		assertFalse(wiki.existsPage(A_FAMOUS_PAGE+"_"));
		assertFalse(wiki.existsPage(A_FAMOUS_PAGE+" (X)"));
		assertFalse(wiki.existsPage(" (X)"));
	}

	@Test
	public void testExistsPageByTitleNullOrEmpty() {
		assertFalse(wiki.existsPage(null));
		assertFalse(wiki.existsPage(""));
	}

	@Test
	public void testExistsPageByID() {
		assertTrue(wiki.existsPage(A_FAMOUS_PAGE_ID));
	}

	@Test
	public void testExistsPageByIDInvalid1() {
		assertFalse(wiki.existsPage(-42));
	}

	@Test
	public void testExistsPageByIDInvalid2() {
		assertFalse(wiki.existsPage(Integer.MAX_VALUE));
	}

	@Test
	public void testGetTitleByID() {
		try {
			Title title = wiki.getTitle(A_FAMOUS_PAGE_ID);
			assertNotNull(title);
			assertEquals(A_FAMOUS_PAGE, title.getRawTitleText());
			assertEquals(A_FAMOUS_PAGE_CLEAN, title.getPlainTitle());
		} catch (WikiPageNotFoundException nfe) {
			fail("Encountered WikiPageNotFoundException: " + nfe.getLocalizedMessage());
		} catch (WikiApiException ae) {
			fail("Encountered WikiApiException: " + ae.getLocalizedMessage());
		}
	}

	@Test
	public void testGetTitleByIDInvalid() {
		try {
			wiki.getTitle(-42);
		} catch (WikiPageNotFoundException wpnfe) {
			// this is expected here
		} catch (WikiApiException e) {
			fail("Expected a WikiApiException, yet encountered WikiApiException: "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageIds() {
		Iterable<Integer> iterable = wiki.getPageIds();
		assertNotNull(iterable);
	}

	@Test
	public void testGetPagesByPageQuery() {
		try {
			PageQuery query = new PageQuery();
			// expected: ONE match
			query.setTitlePattern(A_FAMOUS_PAGE+"%");
			Iterable<Page> iterable = wiki.getPages(query);
			assertNotNull(iterable);
			Page page = iterable.iterator().next();
			assertEquals(A_FAMOUS_PAGE_ID, page.getPageId());

			// expected: ONE match
			query.setOnlyArticlePages(true);
			iterable = wiki.getPages(query);
			assertNotNull(iterable);
			page = iterable.iterator().next();
			assertEquals(A_FAMOUS_PAGE_ID, page.getPageId());

			// expected: NO match
			query.setOnlyArticlePages(false);
			query.setOnlyDisambiguationPages(true);
			iterable = wiki.getPages(query);
			assertNotNull(iterable);
			assertFalse(iterable.iterator().hasNext());

			// expected: NO match
			query.setTitlePattern(A_FAMOUS_PAGE+"_");
			iterable = wiki.getPages(query);
			assertNotNull(iterable);
			assertFalse(iterable.iterator().hasNext());
		} catch (WikiApiException e) {
			fail("Encountered WikiApiException: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageHibernateId() {
		long objectID = wiki.__getPageHibernateId(A_FAMOUS_PAGE_ID);
		assertTrue(objectID > 0);

		// query a 2nd time to validate caching of IDs
		assertEquals(objectID, wiki.__getPageHibernateId(A_FAMOUS_PAGE_ID));
	}

	@Test
	public void testGetPageHibernateIdInvalid1() {
		long objectID = wiki.__getPageHibernateId(-42);
		assertEquals(-1, objectID);
	}

	@Test
	public void testGetPageHibernateIdInvalid2() {
		long objectID = wiki.__getPageHibernateId(Integer.MAX_VALUE);
		assertEquals(-1, objectID);
	}

	@Test
	public void testGetCategoryInvalid1() {
		assertNull(wiki.getCategory(-42));
	}

	@Test
	public void testGetCategoryInvalid2() {
		assertNull(wiki.getCategory(Integer.MAX_VALUE));
	}

	@Test
	public void testGetCategoriesByPageTitle() {
		int expectedCategoryPageId = 9;
		String expectedCategoryTitle = "Publications of UKP";
		try {
			Set<Category> categories = wiki.getCategories(A_FAMOUS_PAGE);
			assertNotNull(categories);
			assertFalse(categories.isEmpty());
			assertEquals(1, categories.size());
			Category c = categories.iterator().next();
			assertNotNull(c);
			assertEquals(expectedCategoryPageId, c.getPageId());
			assertEquals(expectedCategoryTitle, c.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while getting the categories of a page by its title");
		} catch (WikiPageNotFoundException e) {
			fail("A WikiPageNotFoundException occurred while getting the categories of a page by its title");
		}
	}

	@Test
	public void testGetCategoriesByPageTitleInvalid1() {
		try {
			wiki.getCategories("");
		} catch (WikiPageNotFoundException wpnfe) {
			// this is expected here
		} catch (RuntimeException re) {
			fail("Expected a WikiPageNotFoundException, yet encountered RuntimeException: " + re.getLocalizedMessage());
		}
	}

	@Test
	public void testGetCategoriesByPageTitleInvalid2() {
		try {
			wiki.getCategories(null);
		} catch (WikiPageNotFoundException wpnfe) {
			// this is expected here
		} catch (RuntimeException re) {
			fail("Expected a WikiPageNotFoundException, yet encountered RuntimeException: " + re.getLocalizedMessage());
		}
	}


	@Test
	public void testGetLanguage() {
		assertNotNull(wiki.getLanguage());
	}

	/* INTERNAL TEST HELPER METHODS */

	private void getNotExistingPage(String title) {
		boolean exceptionThrown = false;
		try {
			wiki.getPage(title);
		} catch (WikiApiException e) {
			exceptionThrown = true;
		}
		assertTrue("Testing the WikiApiException for non existing page: " + title, exceptionThrown);
	}

    private void getExistingPage(String title, int pageId) {
        getExistingPage(title, title, pageId);
    }

    private void getExistingPage(String keyword, String title, int pageId) {
		Page p = null;
		try {
			p = wiki.getPage(keyword);
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the page: '" + keyword + "'");
		}

		assertEquals("testing the pageId of '" + title + "'", pageId, p.getPageId());

		try {
			assertEquals("testing the title of '" + title + "'", title.trim(), p.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while getting the title of " + title);
		}
	}

	private void checkPage(Integer pageID, String pageTitle) {
		Page page;
		try {
			if(pageID != null) {
				page = wiki.getPage(pageID);
			} else if(pageTitle!=null) {
				page = wiki.getPage(pageTitle);
			} else {
				throw new WikiApiException("Neither pageId nor pageTitle were used to get a Page?!");
			}
			assertNotNull(page);
			assertEquals(A_FAMOUS_PAGE_ID, page.getPageId());
			Title title = page.getTitle();
			assertNotNull(title);
			assertEquals(A_FAMOUS_PAGE, title.getRawTitleText());
			assertEquals(A_FAMOUS_PAGE_CLEAN, title.getPlainTitle());
		} catch (WikiPageNotFoundException nfe) {
			fail("Encountered WikiPageNotFoundException: " + nfe.getLocalizedMessage());
		} catch (WikiApiException ae) {
			fail("Encountered WikiApiException: " + ae.getLocalizedMessage());
		}
	}

	private void checkGetPageByExactTitle(String pageTitle) throws WikiApiException {
		Page page = wiki.getPageByExactTitle(pageTitle);
		assertNotNull(page);
		assertEquals(1041, page.getPageId());
		Title title = page.getTitle();
		assertNotNull(title);
		assertEquals("UKP", title.getRawTitleText());
		assertEquals("UKP", title.getPlainTitle());
	}
}
