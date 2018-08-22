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
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import org.junit.*;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PageTest extends BaseJWPLTest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String A_FAMOUS_PAGE = "Wikipedia API";
	private static final int A_FAMOUS_PAGE_ID = 1014;
	// Here: ORMs internal object identifier aka Primary Key.
	private static final long A_FAMOUS_PAGE_OBJECT_ID = 1;

	// The object under test
	private Page page;

	/**
	 * Made this static so that following tests don't run if assumption fails.
	 * (With AT_Before, tests also would not be executed but marked as passed)
	 * This could be changed back as soon as JUnit ignored tests after failed
	 * assumptions
	 */
	@BeforeClass
	public static void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		//DatabaseConfiguration db = obtainMySQLConfiguration();
		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
		}
	}

	@Before
	public void setup() {
		page = fetchPage(A_FAMOUS_PAGE);
		assertNotNull(page);
	}

	@After
	public void tearDown() {
		page = null;
	}

	@Test
	public void testGetTitle() throws Exception {
		Title t = page.getTitle();
		assertNotNull(t);
		assertEquals("testing the title", A_FAMOUS_PAGE, t.getPlainTitle());
		assertEquals("testing the pageId", A_FAMOUS_PAGE_ID, page.getPageId());
	}

	@Test
	public void testGetTitleExact() throws Exception {
		// Query page here by its exact title containing '_'.
		String title = "Wikipedia_API";
		Page p = fetchPage(title);
		assertNotNull(p);
		Title t = p.getTitle();
		assertNotNull(t);
		assertEquals("testing the title", title, p.getTitle().getRawTitleText());
		assertEquals("testing the pageId", A_FAMOUS_PAGE_ID, p.getPageId());
	}

	@Test
	public void testGetText() {
		String expectedMarkupText = "Wikipedia API ist die wichtigste [[Software]] überhaupt.\n" +
				"[[JWPL|Wikipedia API]].\n\n" +
				"*Nicht zu übertreffen.\n\n" +
				"*Unglaublich\n\n" +
				"*[[http://www.ukp.tu-darmstadt.de]]\n\n" +
				"[[en:Wikipedia API]] [[fi:WikipediaAPI]]";
		try {
			String textWithMarkup = page.getText();
			assertNotNull(textWithMarkup);
			assertTrue(textWithMarkup.length() > 0);
			assertEquals(expectedMarkupText, textWithMarkup);
		} catch (RuntimeException e) {
			fail("A RuntimeException occurred while accessing the page for its text (markup): "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPlainText() {
		String expectedPlainText = "Wikipedia API ist die wichtigste Software überhaupt.\n" +
				"Wikipedia API.\n" +
				"Nicht zu übertreffen.\n" +
				"Unglaublich\n" +
				"http://www.ukp.tu-darmstadt.de\n" +
				"en:Wikipedia API fi:WikipediaAPI";
		try {
			assertEquals(expectedPlainText, page.getPlainText());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while parsing the page for its text (plain via Sweble): "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPlainTextWithTable() {
		String title = "Humanbiologie";
		String expectedPlainTextOfTable =
				"Die Attraktivität der Fachrichtungen Humanbiologie beziehungsweise Biomedizin als " +
						"Studienfächer ist in jüngerer Zeit deutlich gestiegen.\n" +
				"\n" +
				"Studiengang|besteht seit|Abschluss|Hochschule\n" +
				"Humanbiologie (Biomedical Science)|1979|Bachelor / Master|Marburg (U)\n" +
				"Molekulare Biomedizin|2014|Bachelor|Rheinische Fachhochschule Köln \n" +
				"Kategorie:Biologie Kategorie:Medizin Kategorie:Humangenetik Kategorie:Studienfach";
		try {
			Page page = fetchPage(title);
			assertNotNull(page);
			assertEquals(6000, page.getPageId());
			assertEquals(expectedPlainTextOfTable, page.getPlainText());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while parsing the page for its text (plain with table via Sweble): "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPlainTextWithXMLEndTag() {
		String title = "Liste_von_Materia_Medica_der_traditionellen_uigurischen_Medizin";
		String expectedPlainText = "Dies ist eine Liste von Materia Medica der traditionellen uigurischen Medizin. " +
				"Die uigurische Medizin entwickelte sich aus der arabischen Medizin, der antiken griechischen Medizin " +
				"und der traditionellen chinesischen Medizin. " +
				"Übersicht" + "\n" +
				"Quellen: [cintcm.com], [tcm-resources.com]";
		try {
			Page page = fetchPage(title);
			assertNotNull(page);
			assertEquals(6001, page.getPageId());
			assertEquals(expectedPlainText, page.getPlainText());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while parsing the page for its text (plain with table via Sweble): "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetNumberOfCategories() {
		int categories = page.getNumberOfCategories();
		assertTrue(categories > 0);
		assertEquals(2, categories);
	}

	@Test
	public void testGetCategories() {
		Set<Category> categories = page.getCategories();
		assertNotNull(categories);
		assertFalse(categories.isEmpty());
		assertEquals(2, categories.size());
		try {
			boolean foundSIR = false;
			boolean foundDisambiguation = false;
			for (Category c : categories) {
				String ct = c.getTitle().getPlainTitle();
				assertNotNull(ct);
				if ("SIR".equals(ct)) {
					foundSIR = true;
				}
				if ("Disambiguation".equals(ct)) {
					foundDisambiguation = true;
				}
			}
			assertTrue(foundSIR);
			assertTrue(foundDisambiguation);
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while accessing the category title of the page: "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetNumberOfInlinks() {
		int inlinks = page.getNumberOfInlinks();
		assertTrue(inlinks > 0);
		assertEquals(3, inlinks);
	}

	@Test
	public void testGetNumberOfInlinksZero() {
		int inlinks = fetchPage("Unconnected_page").getNumberOfInlinks();
		assertEquals(0, inlinks);
	}

	@Test
	public void testGetInlinks() {
		Set<Page> inlinks = page.getInlinks();
		assertNotNull(inlinks);
		assertFalse(inlinks.isEmpty());
		assertEquals(3, inlinks.size());
	}

	@Test
	public void testGetInlinkIDs() {
		Set<Integer> inlinkIDs = page.getInlinkIDs();
		assertNotNull(inlinkIDs);
		assertFalse(inlinkIDs.isEmpty());
		assertEquals(3, inlinkIDs.size());
	}

	@Test
	public void testGetNumberOfOutlinks() {
		int outlinks = page.getNumberOfOutlinks();
		assertTrue(outlinks > 0);
		assertEquals(1, outlinks);
	}

	@Test
	public void testGetNumberOfOutlinksZero() {
		int outlinks = fetchPage("Unconnected_page").getNumberOfOutlinks();
		assertEquals(0, outlinks);
	}

	@Test
	public void testGetOutlinks() {
		Set<Page> outlinks = page.getOutlinks();
		assertNotNull(outlinks);
		assertFalse(outlinks.isEmpty());
		assertEquals(1, outlinks.size());
		Page outlink = outlinks.iterator().next();
		assertNotNull(outlink);
		try {
			assertEquals("Torsten Zesch", outlink.getTitle().getPlainTitle());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while accessing the title of the page: "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetOutlinkIDs() {
		Set<Integer> outlinkIDs = page.getOutlinkIDs();
		assertNotNull(outlinkIDs);
		assertFalse(outlinkIDs.isEmpty());
		assertEquals(1, outlinkIDs.size());
	}

	@Test
	public void testGetRedirects() {
		Set<String> redirects = page.getRedirects();
		assertNotNull(redirects);
		assertTrue(redirects.isEmpty());
	}

	@Test
	public void testIsRedirect() {
		assertFalse(page.isRedirect());
	}

	@Test
	public void testIsRedirectValid() {
		Page p = fetchPage("SIR");
		assertNotNull(p);
		assertTrue(p.isRedirect());
	}

	@Test
	public void testIsDisambiguation() {
		assertFalse(page.isDisambiguation());
	}

	@Test
	public void testIsDiscussion() {
		try {
			assertFalse(page.isDiscussion());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occurred while accessing the page for discussion: "
					+ e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetPageInfo() {
		try {
			String pageInfo = page.getPageInfo();
			assertNotNull(pageInfo);
			assertTrue(pageInfo.length() > 0);
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred while getting the page info: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByPageID() {
		try {
			Page p = new Page(wiki, A_FAMOUS_PAGE_ID);
			assertNotNull(p);
			assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByPageIDInvalid() {
		try {
			new Page(wiki, -42);
		} catch (WikiPageNotFoundException pnfe) {
			// this is expected behavior here, provoked by the test
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByObjectID() {
		try {
			Page p = new Page(wiki, A_FAMOUS_PAGE_OBJECT_ID);
			assertNotNull(p);
			assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
			assertEquals(A_FAMOUS_PAGE_OBJECT_ID, p.__getId());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByObjectIDInvalid() {
		try {
			long invalidObjectID = -42L;
			new Page(wiki, invalidObjectID);
		} catch (WikiPageNotFoundException pnfe) {
			// this is expected behavior here, provoked by the test
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByName() {
		try {
			Page p = new Page(wiki, A_FAMOUS_PAGE);
			assertNotNull(p);
			assertEquals(A_FAMOUS_PAGE, p.getTitle().getPlainTitle());
			assertEquals(A_FAMOUS_PAGE_ID, p.getPageId());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByNameEmpty() {
		try {
			new Page(wiki, "");
		} catch (WikiPageNotFoundException pnfe) {
			// this is expected behavior here, provoked by the test
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByNameNull() {
		try {
			new Page(wiki, null);
		} catch (WikiPageNotFoundException pnfe) {
			// this is expected behavior here, provoked by the test
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCreatePageByNameExactDiscussion() {
		try {
			Page p = new Page(wiki, "Discussion:Wikipedia_API", false);
			assertNotNull(p);
			assertTrue(p.isDiscussion());
			assertEquals(4000, p.getPageId());
		} catch (WikiApiException e) {
			fail("A WikiApiException occurred creating a page: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testPageTitleComparatorEquality() {
		Page page1 = fetchPage(A_FAMOUS_PAGE);
		assertNotNull(page1);
		Page page2 =  fetchPage(A_FAMOUS_PAGE);
		assertNotNull(page2);

		List<Page> pages = new ArrayList<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.sort(new PageTitleComparator());

		assertEquals(page1, pages.get(0));
		assertEquals(page2, pages.get(1));
	}

	@Test
	public void testCategoryTitleComparatorNewOrder() {
		Page page1 = fetchPage(A_FAMOUS_PAGE);
		assertNotNull(page1);
		// this page should be re-ordered before "Wikipedia..."
		Page page2 = fetchPage("Unconnected_page");
		assertNotNull(page2);

		List<Page> pages = new ArrayList<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.sort(new PageTitleComparator());

		assertEquals(page2, pages.get(0));
		assertEquals(page1, pages.get(1));
	}

	private Page fetchPage(final String title) {
		Page page = null;
		try {
			page = wiki.getPage(title);
		} catch (WikiApiException e) {
			logger.error(e.getLocalizedMessage(), e);
			fail("A WikiApiException occurred while getting the page: " + e.getLocalizedMessage());
		}
		return page;
	}
}
