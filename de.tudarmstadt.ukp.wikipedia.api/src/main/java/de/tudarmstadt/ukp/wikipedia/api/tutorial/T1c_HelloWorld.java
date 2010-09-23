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
import de.tudarmstadt.ukp.wikipedia.api.exception.*;

/**
 * Tutorial 1c
 * 
 * Get the text of a wikipedia article.
 * The text will be formatted with MediaWiki markup.
 * 
 * Handle exceptions.
 * 
 * @author zesch
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
