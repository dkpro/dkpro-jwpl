/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

public class PageTest {

	private Wikipedia wiki;

	@Before
	public void setupWikipedia() {
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_test");
		db.setHost("bender.tk.informatik.tu-darmstadt.de");
		db.setUser("student");
		db.setPassword("student");
		db.setLanguage(Language._test);
		try {
			wiki = new Wikipedia(db);
		} catch (WikiInitializationException e) {
			fail("Wikipedia could not be initialized.");
		}
	}

	@Test
	public void testPageTitle(){
		String title = "Wikipedia API";
        Page p = null;
		try {
			p = wiki.getPage("Wikipedia API");
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page " + title);
		}
		//test the title
		try {
			assertEquals("testing the title", title, p.getTitle().getPlainTitle().toString());
		} catch (WikiTitleParsingException e) {
			e.printStackTrace();
			fail("A WikiTitleParsingException occured while testing the title of the page " + title);
		}

	}

	@Test
	public void testPageId(){
        String title = "Wikipedia API";
		Page p = null;
		try {
			p = wiki.getPage(title);
		} catch (WikiApiException e) {
			e.printStackTrace();
			fail("A WikiApiException occured while getting the page " + title);
		}
		//test the pageId
		assertEquals("testing the pageId", 1014, p.getPageId());
	}

	@Test
	public void testParsedPage(){
        String title = "Wikipedia API";
        Page p = null;
        try {
            p = wiki.getPage(title);
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occured while getting the page " + title);
        }

        String LF = "\n";
        String text = "Wikipedia API ist die wichtigste Software überhaupt." + LF +
        	"Wikipedia API. Nicht zu übertreffen. Unglaublich http://www.ukp.tu-darmstadt.de";
        ParsedPage pp = p.getParsedPage();
        int i=0;
        for (Link link : pp.getSection(0).getLinks()) {
            if (i==0) {
                assertEquals("Software", link.getText());
            }
            else if (i==1) {
                assertEquals("Wikipedia API", link.getText());
                assertEquals("JWPL", link.getTarget());
            }
            i++;
        }
        assertEquals(text, pp.getText());
	}
}
