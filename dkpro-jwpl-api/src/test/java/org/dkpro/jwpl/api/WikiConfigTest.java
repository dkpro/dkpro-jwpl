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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.sweble.wikitext.engine.config.WikiConfig;

public class WikiConfigTest
{

    @Test
    public void testGetWikiConf()
    {
        WikiConfig portugueseConf = WikiConstants.Language.portuguese.getWikiconfig();
        WikiConfig englishConf = WikiConstants.Language.english.getWikiconfig();
        WikiConfig testConf = WikiConstants.Language._test.getWikiconfig();
        // assertion block
        assertSame("pt", portugueseConf.getContentLanguage());
        assertSame("en", englishConf.getContentLanguage());
        assertSame("en", testConf.getContentLanguage());
    }

    /*
     * Note:
     * This is not working in a GitHub build env due to an HTTP 429 response by french Wikipedia - reason is unclear?!
     */
    @Test
    @DisabledIfEnvironmentVariable(named = "BUILD_ENV", matches = "GitHub")
    public void testGetWikiConfFrench()
    {
        WikiConfig frenchConf = WikiConstants.Language.french.getWikiconfig();
        assertSame("fr", frenchConf.getContentLanguage());
    }
}
