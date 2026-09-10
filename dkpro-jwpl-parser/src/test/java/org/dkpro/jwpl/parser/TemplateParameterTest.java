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

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.junit.jupiter.api.Test;

/**
 * Tests that the parameters of a template are separated on the pipes that actually separate them.
 * A pipe inside a link, a nested template or a table belongs to that markup, and splitting on it
 * cuts the parameter it occurs in into pieces (see issue #111).
 */
class TemplateParameterTest
{

    private static List<String> parametersOf(String text)
    {
        MediaWikiParserFactory factory = new MediaWikiParserFactory(Language.english);
        factory.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        MediaWikiParser parser = factory.createParser();

        List<Template> templates = parser.parse(text).getTemplates();
        assertEquals(1, templates.size(), "expected exactly one template in: " + text);
        return templates.get(0).getParameters();
    }

    @Test
    void keepsAParameterHoldingALinkWithAnAnchorTogether()
    {
        assertEquals(
                of("region1name=[[Houston/Downtown|Downtown]]", "region1color=#d56d76"),
                parametersOf("""
                        {{Regionlist
                        | region1name=[[Houston/Downtown|Downtown]]
                        | region1color=#d56d76
                        }}"""));
    }

    @Test
    void keepsAParameterHoldingAnImageWithItsOptionsTogether()
    {
        assertEquals(of("image=[[File:X.jpg|thumb|left|A caption]]", "caption=Plain"),
                parametersOf("{{Infobox|image=[[File:X.jpg|thumb|left|A caption]]|caption=Plain}}"));
    }

    @Test
    void keepsAParameterHoldingATableTogether()
    {
        assertEquals(of("body={| class=\"wikitable\"\n! a !! b\n|-\n| 1 || 2\n|}", "footer=x"),
                parametersOf("""
                        {{Wrapper|body={| class="wikitable"
                        ! a !! b
                        |-
                        | 1 || 2
                        |}|footer=x}}"""));
    }

    @Test
    void separatesTheParametersOfAPlainTemplate()
    {
        assertEquals(of("first", "second=2", "third"),
                parametersOf("{{Simple|first|second=2|third}}"));
    }
}
