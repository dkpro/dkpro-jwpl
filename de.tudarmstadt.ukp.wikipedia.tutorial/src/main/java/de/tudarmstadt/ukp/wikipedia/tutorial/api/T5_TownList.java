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

import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;


/**
 * Tutorial 5
 *
 * Wikipedia categories are used as a kind of semantic tag for pages.
 * They are organized in a thesaurus like structure.
 *
 * If we get all pages assigned to categories in the sub-tree under the category for "Towns in Germany",
 *   we can get a quite long list of towns in Germany.
 *
 *
 */
public class T5_TownList implements WikiConstants {

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

        // Get the category "Towns in Germany"
        String title = "Towns in Germany";
        Category topCat;
        try {
            topCat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }

        // Add the pages categorized under "Towns in Germany".
        Set<String> towns = new TreeSet<String>();
        for (Page p : topCat.getArticles()) {
            towns.add(p.getTitle().getPlainTitle());
        }

        // Get the pages categorized under each subcategory of "Towns in Germany".
        for (Category townCategory : topCat.getDescendants()) {
            for (Page p : townCategory.getArticles()) {
                towns.add(p.getTitle().getPlainTitle());
            }
            System.out.println("Number of towns: " + towns.size());
        }

        // Output the pages
        for (String town : towns) {
            System.out.println(town);
        }

    }
}
