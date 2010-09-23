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
package de.tudarmstadt.ukp.wikipedia.parser.html;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.tutorial.TestFile;

/**
 * This Class shows how to use the HtmlTools.class...<br/>
 * Mainly, you can create an HtmlFile of a ParsedPage.
 * 
 * @author CJacobi
 */
public class HtmlFileDemo {
	
	public static void main( String[] argv ) throws Exception{
		
        // load a sample document (the contents are equal to "DarmstadtWikipediaArticle.txt")
        String documentText = TestFile.getFileText();
        
		// set up an individually parametrized MediaWikiParser
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		pf.getImageIdentifers().add("Image");
		MediaWikiParser parser = pf.createParser();
		
		ParsedPage pp = parser.parse( documentText );
		
        String outFileName = "htmlFileDemo.html";
		HtmlWriter.writeFile(outFileName, "UTF8", HtmlWriter.parsedPageToHtml(pp));
        
        System.out.println("Writing output to file: " + outFileName);
	}
}
