/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api.tutorial;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.*;

/**
 * Tutorial 1c
 *
 * Get the text of a wikipedia article.
 * The text will be formatted with MediaWiki markup.
 *
 * Handle exceptions.
 *
 *
 */
public class T1c_HelloWorld implements WikiConstants {

    public static void main(String[] args) {

        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("SERVER_URL");
        dbConfig.setDatabase("DATABASE");
        dbConfig.setUser("USER");
        dbConfig.setPassword("PASSWORD");
        dbConfig.setLanguage(Language.german);

        // Create a new German wikipedia.
        Wikipedia wiki = null;
        try {
            wiki = new Wikipedia(dbConfig);
        } catch (WikiInitializationException e1) {
            System.out.println("Could not initialize Wikipedia.");
            e1.printStackTrace();
            System.exit(1);
        }

        // Get the page with title "Hello world".
        String title = "Hello world";
        try {
            Page page = wiki.getPage(title);
            System.out.println(page.getText());
        } catch (WikiApiException e) {
            System.out.println("Page " + title + " does not exist");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
