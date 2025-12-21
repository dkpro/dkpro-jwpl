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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.Title;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;

public class LinkAnchorExtractor
{

    private final MediaWikiParser parser;

    public LinkAnchorExtractor()
    {
        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        parser = pf.createParser();
    }

    public LinkAnchorExtractor(Language lang)
    {
        MediaWikiParserFactory pf = new MediaWikiParserFactory(lang);
        parser = pf.createParser();
    }

    public LinkAnchorExtractor(MediaWikiParser parser)
    {
        this.parser = parser;
    }

    /**
     * Note that this method only returns the anchors that are not equal to the page's title.
     * Anchors might contain references to sections in an article in the form of "Page#Section". If
     * you need the plain title, e.g. for checking whether the page exists in Wikipedia, the Title
     * object can be used.
     *
     * @return A set of strings used as anchor texts in links pointing to that page.
     * @throws WikiTitleParsingException
     */
    public Set<String> getInlinkAnchors(Page page) throws WikiTitleParsingException
    {
        Set<String> inAnchors = new HashSet<>();
        for (Page p : page.getInlinks()) {
            ParsedPage pp = parser.parse(p.getText());
            if (pp == null) {
                return inAnchors;
            }
            for (Link l : pp.getLinks()) {
                String pageTitle = page.getTitle().getPlainTitle();

                String anchorText = l.getText();
                if (l.getTarget().equals(pageTitle) && !anchorText.equals(pageTitle)) {
                    inAnchors.add(anchorText);
                }
            }
        }
        return inAnchors;
    }

    /**
     * Note that this method only returns the anchors that are not equal to the title of the page
     * they are pointing to. Anchors might contain references to sections in an article in the form
     * of "Page#Section". If you need the plain title, e.g. for checking whether the page exists in
     * Wikipedia, the Title object can be used.
     *
     * @return A mapping from the page titles of links in that page to the anchor texts used in the
     *         links.
     * @throws WikiTitleParsingException
     */
    public Map<String, Set<String>> getOutlinkAnchors(Page page) throws WikiTitleParsingException
    {
        Map<String, Set<String>> outAnchors = new HashMap<>();
        ParsedPage pp = parser.parse(page.getText());
        if (pp == null) {
            return outAnchors;
        }
        for (Link l : pp.getLinks()) {
            if (l.getTarget().length() == 0) {
                continue;
            }

            String targetTitle = new Title(l.getTarget()).getPlainTitle();
            if (!l.getType().equals(Link.type.EXTERNAL) && !l.getType().equals(Link.type.IMAGE)
                    && !l.getType().equals(Link.type.AUDIO) && !l.getType().equals(Link.type.VIDEO)
                    && !targetTitle.contains(":")) // Wikipedia titles only contain colons if they
            // are categories or other metadata
            {
                String anchorText = l.getText();
                if (!anchorText.equals(targetTitle)) {
                    Set<String> anchors;
                    if (outAnchors.containsKey(targetTitle)) {
                        anchors = outAnchors.get(targetTitle);
                    }
                    else {
                        anchors = new HashSet<>();
                    }
                    anchors.add(anchorText);
                    outAnchors.put(targetTitle, anchors);
                }
            }
        }
        return outAnchors;
    }
}
