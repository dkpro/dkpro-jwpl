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
package de.tudarmstadt.ukp.wikipedia.api.tutorial;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * Tutorial 1b
 * 
 * Get the text of a wikipedia article.
 * The text will be formatted with MediaWiki markup.
 * 
 * If you do not care about exception handling, but want to avoid crashes on every page that does not exist.
 * 
 * @author zesch
 *
 */
public class T1b_HelloWorld implements WikiConstants {

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
        if (wiki.existsPage(title)) {
            Page page = wiki.getPage(title);
            System.out.println(page.getText());
        }
        else {
            System.out.println("Page " + title + " does not exist");
        }
    }
}
