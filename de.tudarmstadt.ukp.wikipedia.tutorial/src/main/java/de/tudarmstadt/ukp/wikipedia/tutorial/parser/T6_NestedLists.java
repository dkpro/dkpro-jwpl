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

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.NestedList;
import de.tudarmstadt.ukp.wikipedia.parser.NestedListContainer;
import de.tudarmstadt.ukp.wikipedia.parser.NestedListElement;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * Displays all nested lists of a page.
 *
 */

public class T6_NestedLists {

	public static void main(String[] args) throws WikiApiException {

		//db connection settings
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
	    dbConfig.setDatabase("DATABASE");
	    dbConfig.setHost("HOST");
	    dbConfig.setUser("USER");
	    dbConfig.setPassword("PASSWORD");
	    dbConfig.setLanguage(Language.english);

		//initialize a wiki
		Wikipedia wiki = new Wikipedia(dbConfig);

        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        MediaWikiParser parser = pf.createParser();

		//get the page 'House_(disambiguation)'
		ParsedPage pp = parser.parse(wiki.getPage("House_(disambiguation)").getText());

		int i = 1;
		// print out all nested lists of the page
		for(NestedList nl : pp.getNestedLists()){
			System.out.println(i + ": \n" + outputNestedList(nl,0));
			i++;
		}
	}

	/**
	 * Returns String with all elements of a NestedList
	 * @param nl NestedList
	 * @param depth Current depth of the Nestedlist
	 * @return
	 */
	public static String outputNestedList(NestedList nl, int depth){
		String result = "";
		if(nl == null)
		 {
			return result; // If null return empty string
		}

		for(int i = 0; i<depth; i++)
		 {
			result += " ";		// insert indentation according to depth
		}

		if(nl.getClass() == NestedListElement.class){ // If it is a NestedListElement,
													  // we reached a leaf, return its contents
			result += nl.getText();
		}else{
			result += "---";	// If it is not a NestedListElement, it is a NestedListContainer
								// print out all its childs, increment depth
			for(NestedList nl2 : ((NestedListContainer)nl).getNestedLists()) {
				result += "\n"+outputNestedList(nl2, depth+1);
			}
		}

		return result;
	}
}
