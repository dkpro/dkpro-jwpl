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

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;

/**
 * Tutorial 3
 *
 * Even more things to do with a Wikipedia page.
 *
 *
 */
public class T3_PageDetails implements WikiConstants {

    public static void main(String[] args) throws WikiApiException {

        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("SERVER_URL");
        dbConfig.setDatabase("DATABASE");
        dbConfig.setUser("USER");
        dbConfig.setPassword("PASSWORD");
        dbConfig.setLanguage(Language.german);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);

        String title = "Hello world";
        Page page;
        try {
            page = wiki.getPage(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Page " + title + " does not exist");
        }

        StringBuilder sb = new StringBuilder();

        // the title of the page
        sb.append("Queried string : " + title + LF);
        sb.append("Title          : " + page.getTitle() + LF);
        sb.append(LF);

        // output the page's redirects
        sb.append("Redirects" + LF);
        for (String redirect : page.getRedirects()) {
            sb.append("  " + new Title(redirect).getPlainTitle() + LF);
        }
        sb.append(LF);

        // output the page's categories
        sb.append("Categories" + LF);
        for (Category category : page.getCategories()) {
            sb.append("  " + category.getTitle() + LF);
        }
        sb.append(LF);

        // output the ingoing links
        sb.append("In-Links" + LF);
        for (Page inLinkPage : page.getInlinks()) {
            sb.append("  " + inLinkPage.getTitle() + LF);
        }
        sb.append(LF);

        // output the outgoing links
        sb.append("Out-Links" + LF);
        for (Page outLinkPage : page.getOutlinks()) {
            sb.append("  " + outLinkPage.getTitle() + LF);
        }

        System.out.println(sb);
    }
}
