package de.tudarmstadt.ukp.wikipedia.parser;
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

public class ParsedPageTest extends BaseJWPLTest{

    private static String LF = "\n";

    /**
     * Made this static so that following tests don't run if assumption fails.
     * (With AT_Before, tests also would not be executed but marked as passed)
     * This could be changed back as soon as JUnit ignored tests after failed
     * assumptions
     */
    @BeforeClass
    public static void setupWikipedia() {
        DatabaseConfiguration db = obtainHSQLDBConfiguration();
        try {
            wiki = new Wikipedia(db);
        } catch (Exception e) {
            fail("Wikipedia could not be initialized: "+e.getLocalizedMessage());
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
            fail("A WikiApiException occurred while getting the page " + title);
        }


        String text = "Wikipedia API ist die wichtigste Software überhaupt." + LF +
        	"Wikipedia API. Nicht zu übertreffen. Unglaublich http://www.ukp.tu-darmstadt.de en:Wikipedia API";


        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        MediaWikiParser parser = pf.createParser();

		ParsedPage pp = parser.parse(p.getText());
        assertNotNull(pp);

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
        String parsedPageText = pp.getText();
        assertNotNull(parsedPageText);
        assertEquals(text, parsedPageText);
	}
}
