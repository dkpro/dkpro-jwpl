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

import static org.dkpro.jwpl.api.CaseVariantPrefixes.Lowering.CONTEXTUAL;
import static org.dkpro.jwpl.api.CaseVariantPrefixes.Lowering.PER_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class CaseVariantPrefixesTest
{
    @Test
    void testCoversEveryCaseVariantOfThePrefix()
    {
        List<String> patterns = CaseVariantPrefixes.of("sir", PER_CHARACTER);
        assertTrue(patterns.contains("sir%"));
        assertTrue(patterns.contains("SIR%"));
        assertTrue(patterns.contains("Sir%"));
        assertTrue(patterns.contains("sIr%"));
        // LATIN CAPITAL LETTER I WITH DOT ABOVE lower-cases into i
        assertTrue(patterns.contains("SİR%"));
        assertTrue(patterns.stream().allMatch(p -> p.length() == 4 && p.endsWith("%")));
    }

    @Test
    void testCoversCharactersLowerCasingIntoAsciiLetters()
    {
        // KELVIN SIGN lower-cases into k
        assertTrue(CaseVariantPrefixes.of("k", PER_CHARACTER).contains("K%"));
    }

    @Test
    void testCoversCapitalSigmaForFinalSigma()
    {
        // String.toLowerCase turns a word-final capital sigma into a final sigma
        List<String> patterns = CaseVariantPrefixes.of("ς", CONTEXTUAL);
        assertTrue(patterns.contains("Σ%"));
        assertTrue(patterns.contains("ς%"));
    }

    @Test
    void testCoversCyrillic()
    {
        List<String> patterns = CaseVariantPrefixes.of("мир", PER_CHARACTER);
        assertTrue(patterns.contains("Мир%"));
        assertTrue(patterns.contains("МИР%"));
    }

    @Test
    void testEndsPrefixAfterIForContextualLowering()
    {
        // Under the Turkish rules, I followed by U+0307 lower-cases into a single i.
        List<String> patterns = CaseVariantPrefixes.of("sir", CONTEXTUAL);
        assertTrue(patterns.contains("Si%"));
        assertTrue(patterns.stream().allMatch(p -> p.length() == 3));
    }

    @Test
    void testEndsPrefixAtCombiningMark()
    {
        // The lower case of U+0130 is i followed by U+0307
        String lowerCase = "İx".toLowerCase(Locale.ROOT);
        List<String> patterns = CaseVariantPrefixes.of(lowerCase, PER_CHARACTER);
        assertTrue(patterns.contains("İ%"));
        assertTrue(patterns.stream().allMatch(p -> p.length() == 2));
    }

    @Test
    void testEndsPrefixAtUnsupportedScript()
    {
        assertTrue(CaseVariantPrefixes.of("日本", PER_CHARACTER).isEmpty());
        assertEquals(List.of("a%", "A%"), CaseVariantPrefixes.of("a日", PER_CHARACTER));
        assertTrue(CaseVariantPrefixes.of("", PER_CHARACTER).isEmpty());
    }

    @Test
    void testEscapesWildcards()
    {
        List<String> patterns = CaseVariantPrefixes.of("1_%!", PER_CHARACTER);
        assertEquals(List.of("1!_!%!!%"), patterns);
    }

    @Test
    void testLimitsNumberOfPatterns()
    {
        List<String> patterns = CaseVariantPrefixes.of("abcdefghjklmnop", PER_CHARACTER);
        assertEquals(CaseVariantPrefixes.MAX_PATTERNS, patterns.size());
        assertTrue(patterns.contains("abcdef%"));
        assertTrue(patterns.contains("ABCDEF%"));
    }
}
