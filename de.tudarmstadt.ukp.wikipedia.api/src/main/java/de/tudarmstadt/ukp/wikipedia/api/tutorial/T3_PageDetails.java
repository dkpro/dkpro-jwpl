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
 * @author zesch
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
