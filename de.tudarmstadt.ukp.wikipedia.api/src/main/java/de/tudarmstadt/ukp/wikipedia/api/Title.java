/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Represents a Wikipedia page title.
 * @author zesch
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
     * Create a title object using a title string.
     * The string gets parsed into an entity part and a disambiguation part.
     * As Wikipedia page names represent spaces as underscores, we create a version with spaces and one without. 
     * @param titleText The title string of the page.
     * @throws WikiTitleParsingException 
     */
    public Title(String titleText) throws WikiTitleParsingException  {
        if (titleText.length() == 0) {
            throw new WikiTitleParsingException("Title is empty.");
        }
        
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
        
        String regexFindParts = "(.*?).\\((.+?)\\)$";

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
        
        if (getEntity() == null) {
            throw new WikiTitleParsingException("Title was not properly initialized.");
        }
    }

    /**
     * Encodes a plain title string to wiki-style.
     *
     * Page titles in Wikipedia are encoded in a way that URLs containing the title are valid.
     * Title strings entered by users normally do not conform to this wiki-style encoding.
     * 
     * @param pTitle The string to encode.
     * @return The wiki-style encoded string.
     */
    private String encodeTitleWikistyle(String pTitle) {
        String encodedTitle = pTitle.replace(' ', '_');
        return encodedTitle;
    }
    
    /**
     * Decodes a wiki-style title string to plain text.
     * 
     * Page titles in Wikipedia are encoded in a way that URLs containing the title are valid.
     * Title strings entered by users normally do not conform to this wiki-style encoding.
     *
     * @param pTitle The string to decode.
     * @return The decoded string. 
     */
    private String decodeTitleWikistyle(String pTitle) {
        String encodedTitle = pTitle.replace('_', ' ');
        return encodedTitle;
    }

    /**
     * Returns the disambigutation text of a page title (i.e., the part in parentheses following the page's name).
     * @return The disambigutation text of a page title (i.e., the part in parentheses following the page's name).
     */
    public String getDisambiguationText() {
        return disambiguationText;
    }

    /**
     * Returns the name of the entity (i.e. the page's title *without* disambiguation string).
     * @return The name of the entity (i.e. the page's title *without* disambiguation string). 
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Returns the plain title, without wikistyle underscores replacing spaces.
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
     * Returns the wikistyle title, with spaces replaced by underscores.
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
