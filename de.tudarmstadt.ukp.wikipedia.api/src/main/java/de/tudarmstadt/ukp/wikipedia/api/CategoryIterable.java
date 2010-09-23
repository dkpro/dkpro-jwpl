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
 * An iterable over category objects.
 * @author zesch
 *
 */
public class CategoryIterable implements Iterable<Category> {

    private Wikipedia wiki;
    
    /** 
     * The size of the page buffer.
     * With bufferSize = 1, a database connection is needed for retrieving a single article.
     * Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 500. 
     */
    private int bufferSize = 500;
    
    public CategoryIterable(Wikipedia wiki) {
        this.wiki = wiki;
    }

    public CategoryIterable(Wikipedia wiki, int bufferSize) {
        this.wiki = wiki;
        this.bufferSize = bufferSize;
    }

    public Iterator<Category> iterator() {
        return new CategoryIterator(wiki, bufferSize);
    }
}    



