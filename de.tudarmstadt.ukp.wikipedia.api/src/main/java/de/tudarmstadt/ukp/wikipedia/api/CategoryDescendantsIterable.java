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
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.Iterator;


/**
 * An iterable over category objects retrieved by Category.getDescendants()
 * @author zesch
 *
 */
public class CategoryDescendantsIterable implements Iterable<Category> {

    private Wikipedia wiki;
    private Category startCategory;
    
    /** 
     * The size of the page buffer.
     * With bufferSize = 1, a database connection is needed for retrieving a single article.
     * Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 25. 
     */
    private int bufferSize = 25;
    
    public CategoryDescendantsIterable(Wikipedia wiki, Category startCategory) {
        this.wiki = wiki;
        this.startCategory = startCategory;
    }

    public CategoryDescendantsIterable(Wikipedia wiki, int bufferSize, Category startCategory) {
        this.wiki = wiki;
        this.bufferSize = bufferSize;
        this.startCategory = startCategory;
    }

    public Iterator<Category> iterator() {
        return new CategoryDescendantsIterator(wiki, bufferSize, startCategory);
    }
}
