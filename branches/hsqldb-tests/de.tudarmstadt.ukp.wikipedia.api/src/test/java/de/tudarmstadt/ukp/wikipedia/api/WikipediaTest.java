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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikipediaTest extends BaseJWPLTest{


	@Before
	public void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		try {
			wiki = new Wikipedia(db);
		} catch (WikiInitializationException e) {
			fail("Wikipedia could not be initialized.");
		}
	}
	
	/*
	 * We test the returned pages with testing their pageId and their title.
     * We also expect a WikiApiException to be thrown when trying to get non existing page.
	 */
	@Test
	public void testGetPage() {
		getExistingPage("Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
        getExistingPage("Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
        getExistingPage("exploring the Potential of Semantic Relatedness in Information Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
        getExistingPage("exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval", "Exploring the Potential of Semantic Relatedness in Information Retrieval", 1017);
		getExistingPage("TK2", 105);
        getNotExistingPage("TK2 ");
        getNotExistingPage(" TK2");
		getNotExistingPage("TK4");
		getNotExistingPage("");
	}

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
			fail("A WikiApiException occured while getting the page: '" + keyword + "'");
		}

		assertEquals("testing the pageId of '" + title + "'", pageId, p.getPageId());

		try {
			assertEquals("testing the title of '" + title + "'", title.trim(), p.getTitle().toString());
		} catch (WikiTitleParsingException e) {
			fail("A WikiTitleParsingException occured while getting the title of " + title);
		}
	}

	/**
	 * Test for existsPage(String)
	 */
	@Test
	public void testExistsPage_Title() {
		assertTrue(wiki.existsPage("TK2"));
        assertTrue(wiki.existsPage("Wikipedia API"));
		assertTrue(wiki.existsPage("Wikipedia_API"));
        assertTrue(wiki.existsPage("wikipedia API"));
        assertTrue(wiki.existsPage("wikipedia_API"));
		assertTrue(wiki.existsPage("NCS"));
		assertFalse(wiki.existsPage("Tk2"));
		assertFalse(wiki.existsPage("TK2 "));
        assertFalse(wiki.existsPage("wikipedia_api"));
        assertFalse(wiki.existsPage(" TK2"));
		assertFalse(wiki.existsPage("Telecooperation"));
		assertFalse(wiki.existsPage(""));
	}

    /**
     * Test for existsPage(String)
     */
    @Test
    public void testExistsPage_PageID() {
        assertTrue(wiki.existsPage(103));
        assertTrue(wiki.existsPage(1022));
        assertFalse(wiki.existsPage(-1));
        assertFalse(wiki.existsPage(1619745));
    }

    /**
	 * Test for getCategory(String)
	 */
	@Test public void testGetCategory(){
        getExistingCategory("People of UKP",8);
        getExistingCategory("People_of_UKP","People of UKP",8);
        getExistingCategory("People_of UKP","People of UKP",8);
        getExistingCategory("people of UKP","People of UKP",8);
        getExistingCategory("people_of_UKP","People of UKP",8);
		getExistingCategory("AQUA",11);
        getExistingCategory("AQUA",11);
		getNotExistingCategory("");
        getNotExistingCategory("Wikipedia_API");
	}
	
    private void getExistingCategory(String title,int pageId) {
        getExistingCategory(title, title, pageId);
    }

    private void getExistingCategory(String keyword, String title,int pageId){
		Category cat = null;
		try{
			cat = wiki.getCategory(keyword);
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting the category: "+ keyword);
		}
		assertEquals("testing the pageId of "+ title, pageId, cat.getPageId());
		try{
			assertEquals("testing the title of "+ title, title, cat.getTitle().toString());
		}catch(WikiTitleParsingException e){
			e.printStackTrace();
			fail("A WikiTitleParsingException occured while getting the title of "+ title);
		}
	}
	
	private void getNotExistingCategory(String title){
		boolean exceptionThrown = false;
		try{
			wiki.getCategory(title);
		}catch(WikiApiException e){
			exceptionThrown = true;
		}
		assertTrue("testing the WikiApiException for non existing category: " + title, exceptionThrown);
	}
	
	/**
	 * Test for getPages(PageQuery)
	 */
	@Test public void testGetPagesWithPageQuery(){
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		expectedPageIds.add(103);
		expectedPageIds.add(105);
		expectedPageIds.add(108);

		PageQuery pageQuery = new PageQuery();
		pageQuery.setTitlePattern("%");
		pageQuery.setMaxCategories(2);
		pageQuery.setMaxIndegree(2);
		pageQuery.setMinIndegree(1);
		pageQuery.setMaxOutdegree(2);
		pageQuery.setMinOutdegree(1);
		pageQuery.setMaxRedirects(1);
		pageQuery.setMinRedirects(1);
		getPagesForPageQuery(pageQuery,expectedPageIds);
		
		expectedPageIds.clear();
		expectedPageIds.add(107);expectedPageIds.add(1022);
		expectedPageIds.add(105);expectedPageIds.add(103);
		
		pageQuery = new PageQuery();
		pageQuery.setTitlePattern("T%");
		getPagesForPageQuery(pageQuery,expectedPageIds);
	}


	private void getPagesForPageQuery(PageQuery pageQuery, List<Integer> expectedPageIds){
		Iterable<Page> pages = null;
		try {
			pages = wiki.getPages(pageQuery);
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting pages for a PageQuery.");
		}
		//Get a Set with the page ids of the retrieved pages 
		List<Integer> retrievedPageIds = new ArrayList<Integer>();
		for(Page page : pages){
			retrievedPageIds.add(page.getPageId());
		}
		//Compare with the expected page ids
		Collections.sort(retrievedPageIds);
		Collections.sort(expectedPageIds);
		assertEquals("testing result for page query",expectedPageIds,retrievedPageIds);
	}

	@Test
	public void testPageTitle(){
		Page p = null;
		//Get the page
		try{
			p = wiki.getPage("Semantic information retrieval (computer science)");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting a test page.");
		}
		//test the title
		try {
			assertEquals("Semantic information retrieval (computer science)", p.getTitle().toString());
			assertEquals("Semantic information retrieval (computer science)", p.getTitle().getPlainTitle());
			assertEquals("Semantic_information_retrieval_(computer_science)", p.getTitle().getWikiStyleTitle());
			assertEquals("Semantic information retrieval", p.getTitle().getEntity());
			assertEquals("computer science", p.getTitle().getDisambiguationText());
		} catch (WikiTitleParsingException e) {
			e.printStackTrace();
			fail("A WikiTitleParsingException occured while getting the title of a test page.");
		}
	}
	
	@Test
	public void testPageId(){
		Page p = null;
		//Get the page
		try{
			p = wiki.getPage("Wikipedia API");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page 'SIR' for test.");
		}
		//test the pageId
		assertEquals("testing the pageId",1014,p.getPageId());
	}
	
	@Test
	public void testPageInlinks(){
		Page p = null;
		//Get the page
		try{
			p = wiki.getPage("Wikipedia API");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page 'SIR' for test.");
		}
		//test the inlinks(compare the pageIds with an expected list
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		expectedPageIds.add(1028);expectedPageIds.add(1043);
		expectedPageIds.add(1022);
		List<Integer> isPageIds = new ArrayList<Integer>();
		for(Page inlink : p.getInlinks()){
			isPageIds.add(inlink.getPageId());
		}
		Collections.sort(expectedPageIds);
		Collections.sort(isPageIds);
		assertEquals("inlinks",expectedPageIds,isPageIds);
	}
	
	@Test
	public void testPageOutlinks(){
		Page p = null;
		//Get the page
		try{
			p = wiki.getPage("Wikipedia API");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page 'SIR' for test.");
		}
		//test the outlinks
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		expectedPageIds.add(1022);
		List<Integer> isPageIds = new ArrayList<Integer>();
		for(Page outlink : p.getOutlinks()){
			isPageIds.add(outlink.getPageId());
		}
		Collections.sort(expectedPageIds);
		Collections.sort(isPageIds);
		assertEquals("outlinks",expectedPageIds,isPageIds);
	}
	
	@Test
	public void testPageRedirects(){
		Page p = null;
		try{
			p = wiki.getPage("Semantic Information Retrieval");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting a test page.");
		}

		Set<String> redirects = new HashSet<String>();
		redirects.add("SIR");
		
		assertEquals("testing the redirects", redirects, p.getRedirects());
	}
	
	@Test
	public void testPageCategories(){
		Page p = null;
		//Get the page
		try{
			p = wiki.getPage("Wikipedia API");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page 'SIR' for test.");
		}
		//test the categories
		List<Integer> expectedPageIds = new ArrayList<Integer>();
		expectedPageIds.add(10);
		expectedPageIds.add(200);
		List<Integer> isPageIds = new ArrayList<Integer>();
		for(Category cat : p.getCategories()){
			isPageIds.add(cat.getPageId());
		}
		Collections.sort(expectedPageIds);
		Collections.sort(isPageIds);
		assertEquals("categories",expectedPageIds,isPageIds);
	}
	
	@Test
	public void testIsRedirect(){
		Page p1 = null;
		Page p2 = null;

		try{
			p1 = wiki.getPage("Wikipedia API");
			p2 = wiki.getPage("SIR");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting a test page.");
		}
		
		assertFalse("testing isRedirect", p1.isRedirect());
		assertTrue("testing isRedirect", p2.isRedirect());
	}
	
	@Test
	public void testIsDisambiguation(){
		Page p1 = null;
		Page p2 = null;
		
		try{
			p1 = wiki.getPage("Wikipedia API");
			p2 = wiki.getPage("TK3");
		}catch(WikiApiException e){
			e.printStackTrace();
			fail("A WikiApiException occured while getting a test page.");
		}

		assertTrue("testing isDisambiguation", p1.isDisambiguation());
		assertFalse("testing isDisambiguation", p2.isDisambiguation());
	}
}
