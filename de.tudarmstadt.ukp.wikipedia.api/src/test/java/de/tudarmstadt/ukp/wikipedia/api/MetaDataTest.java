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

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


public class MetaDataTest extends BaseJWPLTest {

    // The object under test
    private MetaData metaData;

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

    @Before
    public void setup() {
        metaData = new MetaData(wiki);
    }

    @After
    public void tearDown() {
        metaData = null;
    }

    @Test
    public void testGetNumberOfCategories() {
        long numberOfCategories = metaData.getNumberOfCategories();
        assertTrue(numberOfCategories > 0);
        assertEquals(17, numberOfCategories);
    }

    @Test
    public void testGetNumberOfPages() {
        long numberOfPages = metaData.getNumberOfPages();
        assertTrue(numberOfPages > 0);
        assertEquals(36, numberOfPages);
    }

    @Test
    public void testGetNumberOfDisambiguationPages() {
        long numberOfDisambiguationPages = metaData.getNumberOfDisambiguationPages();
        assertTrue(numberOfDisambiguationPages > 0);
        assertEquals(2, numberOfDisambiguationPages);

    }

    @Test
    public void testGetNumberOfRedirectPages() {
        long numberOfRedirectPages = metaData.getNumberOfRedirectPages();
        assertTrue(numberOfRedirectPages > 0);
        assertEquals(6, numberOfRedirectPages);
    }

    @Test
    public void testGetMainCategory() {
        try {
            Category c = metaData.getMainCategory();
            assertNotNull(c);
            assertEquals(1, c.getPageId());
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the main category: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetDisambiguationCategory() {
        try {
            Category c = metaData.getDisambiguationCategory();
            assertNotNull(c);
            assertEquals(200, c.getPageId());
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the disambiguation category: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testGetLanguage() {
        WikiConstants.Language language = metaData.getLanguage();
        assertNotNull(language);
        assertEquals(WikiConstants.Language._test, language);
    }

    @Test
    public void testGetVersion() {
        try {
            String version = metaData.getVersion();
            assertNotNull(version);
            assertFalse(version.isEmpty());
            assertEquals("1.0", version);
        } catch (WikiApiException e) {
            fail("A WikiApiException occurred while getting the disambiguation category: " + e.getLocalizedMessage());
        }
    }
}
