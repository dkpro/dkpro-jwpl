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
 * @author zesch
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
