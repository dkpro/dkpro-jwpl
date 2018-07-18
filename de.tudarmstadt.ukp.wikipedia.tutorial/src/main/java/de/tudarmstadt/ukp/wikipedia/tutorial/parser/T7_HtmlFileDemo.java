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
package de.tudarmstadt.ukp.wikipedia.tutorial.parser;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.html.HtmlWriter;

/**
 * This class shows how to use the HtmlTools.class...<br>
 * Mainly, you can create an HtmlFile of a {@link ParsedPage}.
 *
 */
public class T7_HtmlFileDemo {
	
	public static void main( String[] argv ) {
		
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
