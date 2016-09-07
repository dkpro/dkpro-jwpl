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

import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

public class T6_HelperMethods {

    public static Set<String> getUniqueArticleTitles() throws WikiInitializationException {
        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("SERVER_URL");
        dbConfig.setDatabase("DATABASE");
        dbConfig.setUser("USER");
        dbConfig.setPassword("PASSWORD");
        dbConfig.setLanguage(Language.german);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);

        Set<String> uniqueArticleTitles = new TreeSet<String>();
        for (Title title : wiki.getTitles()) {
            uniqueArticleTitles.add(title.getPlainTitle());
        }

        return uniqueArticleTitles;
    }

}
