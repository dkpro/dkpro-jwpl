/*******************************************************************************
 * Copyright 2016
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

import static de.tudarmstadt.ukp.wikipedia.api.WikiConstants.LF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class PageTest {

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
			Assume.assumeNoException(e);
			//fail("Wikipedia could not be initialized.");
		}
	}

	@Test
	public void testPageTitle() throws Exception {
		String title = "Wikipedia API";
        Page p = wiki.getPage("Wikipedia API");
		assertEquals("testing the title", title, p.getTitle().getPlainTitle().toString());
	}
	
    @Test
    public void testExactPageTitle() throws Exception {
        String title = "Wikipedia_API";
        Page p = wiki.getPage("Wikipedia_API");
        assertEquals("testing the title", title, p.getTitle().getRawTitleText().toString());
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
	public void testPlainText(){
        String title = "Wikipedia API";
        Page p = null;
        try {
            p = wiki.getPage(title);
        } catch (WikiApiException e) {
            e.printStackTrace();
            fail("A WikiApiException occured while getting the page " + title);
        }

        String text = "Wikipedia API ist die wichtigste Software überhaupt. Wikipedia API.\nNicht zu übertreffen.\nUnglaublich\nhttp://www.ukp.tu-darmstadt.de\nen:Wikipedia API fi:WikipediaAPI";


        try{
            assertEquals(text, p.getPlainText());
        }catch(Exception e){
			Assume.assumeNoException(e);
        }
	}

}
