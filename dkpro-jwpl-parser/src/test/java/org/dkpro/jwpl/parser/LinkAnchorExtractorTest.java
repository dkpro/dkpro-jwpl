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

import static java.util.Set.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests the anchor texts {@link LinkAnchorExtractor} reads out of the outgoing links of a page,
 * with and without the anchors that repeat the title of the page they point to (see issue #79).
 */
class LinkAnchorExtractorTest
{

    private static final String TEXT = """
            The [[Wikipedia]] is written by volunteers, and the [[Wikipedia|free encyclopedia]]
            is what [[Wikipedia|it]] calls itself. See also the [[Nupedia]].
            """;

    private static final MediaWikiParser PARSER = new MediaWikiParserFactory(Language.english)
            .createParser();

    private static final LinkAnchorExtractor EXTRACTOR = new LinkAnchorExtractor(PARSER);

    private static Map<String, Set<String>> anchorsOf(boolean includeAnchorsEqualToTitle)
        throws WikiTitleParsingException
    {
        return EXTRACTOR.getOutlinkAnchors(PARSER.parse(TEXT), includeAnchorsEqualToTitle);
    }

    @Test
    void returnsEveryAnchorIncludingTheOnesEqualToTheTitle() throws Exception
    {
        Map<String, Set<String>> anchors = anchorsOf(true);

        assertEquals(of("Wikipedia", "free encyclopedia", "it"), anchors.get("Wikipedia"));
        assertEquals(of("Nupedia"), anchors.get("Nupedia"));
    }

    @Test
    void dropsTheAnchorsEqualToTheTitle() throws Exception
    {
        Map<String, Set<String>> anchors = anchorsOf(false);

        assertEquals(of("free encyclopedia", "it"), anchors.get("Wikipedia"));
        // 'Nupedia' is linked by its title only, so it has no anchor left at all
        assertTrue(anchors.get("Nupedia") == null || anchors.get("Nupedia").isEmpty());
    }
}
