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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.WikiConstants.Language.WikiConfigGenerator;
import org.junit.jupiter.api.Test;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

/**
 * Tests {@link Language#getWikiconfig()} without fetching the configuration from Wikipedia, whose
 * API rate limits the build servers.
 */
public class WikiConfigTest
{

    private final WikiConfig generated = DefaultConfigEnWp.generate();

    private final List<String> requestedLangCodes = new ArrayList<>();

    private final WikiConfigGenerator recordingGenerator = langCode -> {
        requestedLangCodes.add(langCode);
        return generated;
    };

    @Test
    public void testGetWikiConf()
    {
        assertSame(generated, Language.portuguese.getWikiconfig(recordingGenerator));
        assertSame(generated, Language.english.getWikiconfig(recordingGenerator));
        assertSame(generated, Language.french.getWikiconfig(recordingGenerator));
        assertEquals(List.of("pt", "en", "fr"), requestedLangCodes);
    }

    @Test
    public void testGetWikiConfForTestLanguageUsesDefault()
    {
        WikiConfig testConf = Language._test.getWikiconfig(recordingGenerator);

        assertEquals("en", testConf.getContentLanguage());
        assertTrue(requestedLangCodes.isEmpty());
    }

    @Test
    public void testGetWikiConfFallsBackToDefaultIfGenerationFails()
    {
        WikiConfig conf = Language.portuguese.getWikiconfig(langCode -> {
            throw new IOException("Server returned HTTP response code: 429");
        });

        assertEquals("en", conf.getContentLanguage());
    }
}
