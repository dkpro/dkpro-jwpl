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
package de.tudarmstadt.ukp.wikipedia.parser.tutorial;

import java.io.IOException;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * Displays informations about the inner structure of a page.
 *
 */
public class T1_SimpleParserDemo {

	/**
	 * @param args
	 * @throws WikiApiException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

        // load a sample document (the contents are equal to "DarmstadtWikipediaArticle.txt")
        String documentText = TestFile.getFileText();

        //get a ParsedPage object
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		MediaWikiParser parser = pf.createParser();
		ParsedPage pp = parser.parse(documentText);
		
		//get the sections
		for(Section section : pp.getSections()) {
			System.out.println("section : " + section.getTitle());
			System.out.println(" nr of paragraphs      : " + section.nrOfParagraphs());
			System.out.println(" nr of tables          : " + section.nrOfTables());
			System.out.println(" nr of nested lists    : " + section.nrOfNestedLists());
			System.out.println(" nr of definition lists: " + section.nrOfDefinitionLists());
		}
	}
}
