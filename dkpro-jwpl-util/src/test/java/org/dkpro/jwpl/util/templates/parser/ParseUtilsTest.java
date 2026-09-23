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
package org.dkpro.jwpl.util.templates.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.dkpro.jwpl.util.templates.parser.SectionExtractor.ExtractedSection;
import org.junit.jupiter.api.Test;

class ParseUtilsTest
{
    private static final String TEXT = "Intro text.\n\n== First ==\nSome {{Cite}} text.\n\n"
            + "== Second ==\nMore {{Infobox|a=b}} text.\n";

    @Test
    void testDefaultConfigIsShared()
    {
        assertNotNull(DefaultSwebleConfig.get());
        assertSame(DefaultSwebleConfig.get(), DefaultSwebleConfig.get());
    }

    @Test
    void testGetTemplateNamesIsRepeatable() throws Exception
    {
        List<String> first = ParseUtils.getTemplateNames(TEXT, "Test");
        List<String> second = ParseUtils.getTemplateNames(TEXT, "Test");
        assertEquals(List.of("Cite", "Infobox"), first);
        assertEquals(first, second);
    }

    @Test
    void testGetSectionsIsRepeatable() throws Exception
    {
        List<ExtractedSection> first = ParseUtils.getSections(TEXT, "Test", 1);
        List<ExtractedSection> second = ParseUtils.getSections(TEXT, "Test", 1);
        assertFalse(first.isEmpty());
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getTitle(), second.get(i).getTitle());
            assertEquals(first.get(i).getBody(), second.get(i).getBody());
        }
        assertTrue(first.stream().anyMatch(s -> s.getBody().contains("More")));
    }
}
