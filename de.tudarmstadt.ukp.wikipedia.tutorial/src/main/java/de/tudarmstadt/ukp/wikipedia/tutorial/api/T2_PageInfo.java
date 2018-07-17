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
package de.tudarmstadt.ukp.wikipedia.tutorial.api;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;


/**
 * Tutorial 2
 *
 * A page provides a number of informative methods.
 *
 *
 */
public class T2_PageInfo implements WikiConstants {

    public static void main(String[] args) throws WikiApiException {

        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("SERVER_URL");
        dbConfig.setDatabase("DATABASE");
        dbConfig.setUser("USER");
        dbConfig.setPassword("PASSWORD");
        dbConfig.setLanguage(Language.german);

        // Create a new German wikipedia
        Wikipedia wiki = new Wikipedia(dbConfig);

        String title = "Hello world";
        Page page;
        try {
            page = wiki.getPage(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Page " + title + " does not exist");
        }

        // the title of the page
        System.out.println("Queried string       : " + title);
        System.out.println("Title                : " + page.getTitle());

        // whether the page is a disambiguation page
        System.out.println("IsDisambiguationPage : " + page.isDisambiguation());

        // whether the page is a redirect
        // If a page is a redirect, we can use it like a normal page.
        // The other infos in this example are transparently served by the page that the redirect points to.
        System.out.println("redirect page query  : " + page.isRedirect());

        // the number of links pointing to this page
        System.out.println("# of ingoing links   : " + page.getNumberOfInlinks());

        // the number of links in this page pointing to other pages
        System.out.println("# of outgoing links  : " + page.getNumberOfOutlinks());

        // the number of categories that are assigned to this page
        System.out.println("# of categories      : " + page.getNumberOfCategories());
    }
}
