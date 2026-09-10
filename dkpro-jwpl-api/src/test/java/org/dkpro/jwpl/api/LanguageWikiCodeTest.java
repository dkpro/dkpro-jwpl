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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests that each {@link Language} carries the code of its Wikipedia edition (see issue #53).
 */
class LanguageWikiCodeTest
{

    @ParameterizedTest
    @CsvSource({ //
            "english, en", //
            "german, de", //
            "cebuano, ceb", //
            "simple_english, simple", //
            "low_saxon, nds", //
            "dutch_low_saxon, nds-nl", //
            "belarusian_tarashkevitsa, be-tarask", //
            "classical_chinese, zh-classical", //
            "min_nan, zh-min-nan", //
            "cantonese, zh-yue", //
            "tokipona, tok", //
            "_test, test" })
    void mapsALanguageToTheCodeOfItsEdition(Language language, String wikiCode)
    {
        assertEquals(wikiCode, language.getWikiCode());
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void findsEachLanguageByItsCode(Language language)
    {
        assertSame(language, Language.fromWikiCode(language.getWikiCode()));
    }

    @Test
    void assignsEachCodeToOneLanguageOnly()
    {
        Set<String> codes = new HashSet<>();
        for (Language language : Language.values()) {
            String code = language.getWikiCode();
            assertTrue(code.matches("[a-z]+(-[a-z]+)*"), () -> "malformed code of " + language);
            assertTrue(codes.add(code), () -> "code of " + language + " is assigned twice");
        }
    }

    @Test
    void ignoresTheCaseAndSurroundingBlanksOfACode()
    {
        assertSame(Language.english, Language.fromWikiCode(" EN "));
        assertSame(Language.min_nan, Language.fromWikiCode("ZH-MIN-NAN"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "xx", "english", "zh_min_nan", "enwiki" })
    void reportsNoLanguageForAnUnknownCode(String wikiCode)
    {
        assertNull(Language.fromWikiCode(wikiCode));
    }
}
