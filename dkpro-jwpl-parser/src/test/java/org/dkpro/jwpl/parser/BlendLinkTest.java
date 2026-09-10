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

import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests the anchor text of blend links, that is, of links whose ending stands outside the brackets:
 * MediaWiki renders {@code [[Wiki]]pedia} as the single link "Wikipedia", and that ending belongs
 * to the anchor text of the link (see issue #32).
 */
class BlendLinkTest
{

    private static MediaWikiParser parser()
    {
        return new MediaWikiParserFactory(Language.english).createParser();
    }

    private static List<Link> linksOf(String text)
    {
        return parser().parse(text).getLinks();
    }

    @Test
    void includesTheEndingOfABlendLinkInTheAnchorText()
    {
        List<Link> links = linksOf("The [[Wiki]]pedia and the [[cat]]s are here.");

        assertEquals(2, links.size());
        assertEquals("Wiki", links.get(0).getTarget());
        assertEquals("Wikipedia", links.get(0).getText());
        assertEquals("cat", links.get(1).getTarget());
        assertEquals("cats", links.get(1).getText());
    }

    @Test
    void includesTheEndingOfALinkThatCarriesACaption()
    {
        List<Link> links = linksOf("A [[dog|puppy]]like animal.");

        assertEquals(1, links.size());
        assertEquals("dog", links.get(0).getTarget());
        assertEquals("puppylike", links.get(0).getText());
    }

    @Test
    void stopsAtACharacterThatIsNotALowerCaseLetter()
    {
        List<Link> links = linksOf("[[Wiki]]Pedia, [[dog]]'s, [[cat]]-like and [[fish]] alone.");

        assertEquals(4, links.size());
        assertEquals("Wiki", links.get(0).getText());
        assertEquals("dog", links.get(1).getText());
        assertEquals("cat", links.get(2).getText());
        assertEquals("fish", links.get(3).getText());
    }

    @Test
    void leavesTheRenderedTextUnchanged()
    {
        assertEquals("The Wikipedia and the cats are here.",
                parser().parse("The [[Wiki]]pedia and the [[cat]]s are here.").getText());
    }
}
