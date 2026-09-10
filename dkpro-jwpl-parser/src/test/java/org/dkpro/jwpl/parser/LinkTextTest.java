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
 * Tests {@link Link#getText()} for links that carry no caption of their own. The text behind the
 * pipe of a category link is its sort key, and a sort key of a single space used to leave the
 * caption empty (see issue #90).
 */
class LinkTextTest
{

    private static MediaWikiParser parser()
    {
        return new MediaWikiParserFactory(Language.english).createParser();
    }

    @Test
    void returnsTheTargetOfACategoryLinkWhoseSortKeyIsEmpty()
    {
        String text = """
                Anarchism is a political philosophy.

                [[Category:Anarchism| ]]
                [[Category:Political culture]]
                """;

        List<Link> categories = parser().parse(text).getCategories();

        assertEquals(2, categories.size());
        assertEquals("Category:Anarchism", categories.get(0).getText());
        assertEquals("Category:Political culture", categories.get(1).getText());
    }

    @Test
    void returnsTheTargetOfACategoryLinkWithASortKey()
    {
        List<Link> categories = parser().parse("[[Category:Anarchism|Anarchism, political]]")
                .getCategories();

        assertEquals(1, categories.size());
        // a sort key that is not empty is still reported as the text of the link
        assertEquals("Anarchism, political", categories.get(0).getText());
    }

    @Test
    void keepsTheCaptionOfALinkThatHasOne()
    {
        List<Link> links = parser().parse("An [[anchor link|internal link]] here.").getLinks();

        assertEquals(1, links.size());
        assertEquals("internal link", links.get(0).getText());
    }

    @Test
    void returnsTheTargetOfALinkWithoutACaption()
    {
        List<Link> links = parser().parse("An [[internal link]] here.").getLinks();

        assertEquals(1, links.size());
        assertEquals("internal link", links.get(0).getText());
    }
}
