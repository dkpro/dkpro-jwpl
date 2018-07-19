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
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;

/**
 * Tutorial 4
 *
 * Wikipedia categories are used as a kind of semantic tag for pages.
 * They are organized in a thesaurus like structure.
 *
 *
 */
public class T4_Categories implements WikiConstants {

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

        // Get the category "Säugetiere" (mammals)
        String title = "Säugetiere";
        Category cat;
        try {
            cat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }

        StringBuilder sb = new StringBuilder();

        // the title of the category
        sb.append("Title : " + cat.getTitle() + LF);
        sb.append(LF);

        // the number of links pointing to this page (number of superordinate categories)
        sb.append("# super categories : " + cat.getParents().size() + LF);
        for (Category parent : cat.getParents()) {
            sb.append("  " + parent.getTitle() + LF);
        }
        sb.append(LF);

        // the number of links in this page pointing to other pages (number of subordinate categories)
        sb.append("# sub categories : " + cat.getChildren().size() + LF);
        for (Category child : cat.getChildren()) {
            sb.append("  " + child.getTitle() + LF);
        }
        sb.append(LF);

        // the number of pages that are categorized under this category
        sb.append("# pages : " + cat.getArticles().size() + LF);
        for (Page page : cat.getArticles()) {
            sb.append("  " + page.getTitle() + LF);
        }

        // extract only the pageIDs of pages that are categorized under this category
        sb.append("# pageIDs : " + cat.getArticleIds().size() + LF);
        for (int pageID : cat.getArticleIds()) {
            sb.append("  " + pageID + LF);
        }

        System.out.println(sb);
    }
}
