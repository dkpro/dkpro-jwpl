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
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a Wikipedia page title.
 *
 * Title parsing regexp fixed with the help of many UKP colleagues and Samy Ateia.
 *
 */
public class Title {

    private String wikiStyleTitle;
    private String plainTitle;
    private String entity;
    private String disambiguationText;
    private String rawTitleText;
    private final String sectionText;

    /**
     * Create a {@link Title} object using a title string.
     * The string gets parsed into an entity part and a disambiguation part.
     * As Wikipedia page names represent spaces as underscores, we create a version with spaces and one without.
     *
     * @param titleText The title string of the page.
     * @throws WikiTitleParsingException Thrown if errors occurred during sanitation of the {@code titleText}.
     */
    public Title(String titleText) throws WikiTitleParsingException  {
        if (titleText.length() == 0) {
            throw new WikiTitleParsingException("Title is empty.");
        }

        /*
         * Do not convert first character to upper case. We perform case insensitive querying
         */
        if (titleText.substring(0, 1).toLowerCase().equals(titleText.substring(0, 1))) {
            this.rawTitleText = titleText.substring(0,1).toUpperCase() + titleText.substring(1,titleText.length());
        }
        else {
            this.rawTitleText = titleText;
        }

        // "Car_(automobile)#Introduction"
        // should be split into:
        // - "Car"
        // - "automobile"
        // - "Introduction"

        String titlePart = null;
        String sectionPart = null;
        if (rawTitleText.contains("#")) {
            titlePart = rawTitleText.substring(0, rawTitleText.lastIndexOf("#"));
            sectionPart = rawTitleText.substring(rawTitleText.lastIndexOf("#")+1,rawTitleText.length());
        }
        else {
            titlePart = rawTitleText;
        }

        this.sectionText = sectionPart;

        String regexFindParts = "(.*?)[ _]\\((.+?)\\)$";

        Pattern patternNamespace = Pattern.compile(regexFindParts);
        Matcher matcherNamespace = patternNamespace.matcher(
        		this.decodeTitleWikistyle(titlePart)
        );

        // group 0 is the whole match
        if (matcherNamespace.find()) {
            this.entity = matcherNamespace.group(1);
            this.disambiguationText = matcherNamespace.group(2);

            String relevantTitleParts = this.entity + " (" + this.disambiguationText + ")";
            this.plainTitle = decodeTitleWikistyle(relevantTitleParts);
            this.wikiStyleTitle = encodeTitleWikistyle(relevantTitleParts);
        }
        else {
        	this.plainTitle = decodeTitleWikistyle(titlePart);
        	this.wikiStyleTitle = encodeTitleWikistyle(titlePart);
            this.entity = this.plainTitle;
            this.disambiguationText = null;
        }

        if (StringUtils.isEmpty(getEntity())) {
            throw new WikiTitleParsingException("Title was not properly initialized.");
        }
    }

    /**
     * Encodes a plain title string to wiki-style.
     *
     * Page titles in Wikipedia are encoded in a way that URLs containing the title are valid.
     * Title strings entered by users normally do not conform to this wiki-style encoding.
     *
     * @param pTitle The string to encode. Must not be {@code null}.
     * @return The wiki-style encoded string.
     */
    private String encodeTitleWikistyle(String pTitle) {
        return pTitle.replace(' ', '_');
    }

    /**
     * Decodes a wiki-style title string to plain text.
     *
     * Page titles in Wikipedia are encoded in a way that URLs containing the title are valid.
     * Title strings entered by users normally do not conform to this wiki-style encoding.
     *
     * @param pTitle The string to decode. Must not be {@code null}.
     * @return The decoded string.
     */
    private String decodeTitleWikistyle(String pTitle) {
        return pTitle.replace('_', ' ');
    }

    /**
     * @return The disambiguation text of a page title (i.e., the part in parentheses following the page's name).
     */
    public String getDisambiguationText() {
        return disambiguationText;
    }

    /**
     * @return The name of the entity (i.e. the page's title *without* disambiguation string).
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @return The plain title, without wikistyle underscores replacing spaces.
     */
    public String getPlainTitle() {
        return plainTitle;
    }

    /**
     * @return Returns the section part of a link "Article (Disambiguation)#Section".
     */
    public String getSectionText() {
        return sectionText;
    }

    /**
     * @return The wikistyle title, with spaces replaced by underscores.
     */
    public String getWikiStyleTitle() {
        return wikiStyleTitle;
    }

    protected String getRawTitleText() {
        return rawTitleText;
    }

    @Override
    public String toString() {
        return getPlainTitle();
    }
}
