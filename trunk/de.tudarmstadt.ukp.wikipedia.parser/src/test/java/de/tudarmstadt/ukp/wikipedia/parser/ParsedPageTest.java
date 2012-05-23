package de.tudarmstadt.ukp.wikipedia.parser;
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

public class ParsedPageTest {

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


        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        MediaWikiParser parser = pf.createParser();

		//get the page 'House_(disambiguation)'
		ParsedPage pp = parser.parse(p.getText());


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
