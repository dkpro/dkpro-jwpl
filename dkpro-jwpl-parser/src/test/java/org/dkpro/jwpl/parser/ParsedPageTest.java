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
package org.dkpro.jwpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.WikiConstants;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ParsedPageTest
    extends BaseJWPLTest
{

    private static final String LF = "\n";

    private static String wikiArticleOnAmericium;


    /*
     * Made this static so that following tests don't run if assumption fails. (With AT_Before,
     * tests also would not be executed but marked as passed) This could be changed back as soon as
     * JUnit ignored tests after failed assumptions
     */
    @BeforeAll
    public static void setupWikipedia() throws Exception
    {

        Path p = getResource("pages/Wiki-Article-Americium.txt");
        wikiArticleOnAmericium = Files.readString(p, StandardCharsets.UTF_8);

        DatabaseConfiguration db = obtainHSQLDBConfiguration();
        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage(), e);
        }
    }

    @Test
    public void testParseWithArticleFromTestDB()
    {
        String title = "Wikipedia API";
        Page p = null;
        try {
            p = wiki.getPage(title);
        }
        catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the page " + title, e);
        }

        String text = "Wikipedia API ist die wichtigste Software überhaupt." + LF
                + "Wikipedia API. Nicht zu übertreffen. Unglaublich http://www.ukp.tu-darmstadt.de en:Wikipedia API";

        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        MediaWikiParser parser = pf.createParser();

        ParsedPage pp = parser.parse(p.getText());
        assertNotNull(pp);

        int i = 0;
        for (Link link : pp.getSection(0).getLinks()) {
            if (i == 0) {
                assertEquals("Software", link.getText());
            }
            else if (i == 1) {
                assertEquals("Wikipedia API", link.getText());
                assertEquals("JWPL", link.getTarget());
            }
            i++;
        }
        String parsedPageText = pp.getText();
        assertNotNull(parsedPageText);
        assertEquals(text, parsedPageText);
    }


    /*
     * This test case verifies JWPL-82.
     * see: https://github.com/dkpro/dkpro-jwpl/issues/82
     */
    @Test
    public void testParseWithArticleFromFile()
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(WikiConstants.Language.german);
        MediaWikiParser parser = factory.createParser();
        ParsedPage pp = parser.parse(wikiArticleOnAmericium);
        assertNotNull(pp);
        String parsedPageText = pp.getText();
        assertNotNull(parsedPageText);
        assertNotNull(pp.getLanguages());
        assertNotNull(pp.getCategories());
    }

    private static Path getResource(String r) throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(r);
        return Path.of(resource.toURI());
    }
}
