/*******************************************************************************
 * Copyright 2018
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

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;

import static org.junit.Assert.assertTrue;

public class WikiConfigTest {

    @Test
    public void testGetWikiConf() {
        WikiConfig portugueseConf = WikiConstants.Language.portuguese.getWikiconfig();
        WikiConfig englishConf = WikiConstants.Language.english.getWikiconfig();
        WikiConfig testConf = WikiConstants.Language._test.getWikiconfig();
        WikiConfig frenchConf = WikiConstants.Language.french.getWikiconfig();
        // assertion block
        assertTrue(portugueseConf.getContentLanguage() == "pt");
        assertTrue(englishConf.getContentLanguage() == "en");
        assertTrue(testConf.getContentLanguage() == "en");
        assertTrue(frenchConf.getContentLanguage() == "fr");
    }
}
