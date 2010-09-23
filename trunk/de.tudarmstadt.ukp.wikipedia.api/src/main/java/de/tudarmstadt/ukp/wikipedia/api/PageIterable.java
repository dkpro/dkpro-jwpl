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
 * An iterable of page objects.
 * @author zesch
 *
 */
public class PageIterable implements Iterable<Page> {

    /** The Wikipedia object */
    private Wikipedia wiki;
    
    /** Whether only articles are retrieved (or also disambiguation pages) */
    private boolean onlyArticles;
    
    /** 
     * The size of the page buffer.
     * With bufferSize = 1, a database connection is needed for retrieving a single article.
     * Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 500. 
     */
    private int bufferSize = 500;
    
    public PageIterable(Wikipedia wiki, boolean onlyArticles) {
        this.wiki = wiki;
        this.onlyArticles = onlyArticles;
    }

    protected PageIterable(Wikipedia wiki, boolean onlyArticles, int bufferSize) {
        this.wiki = wiki;
        this.onlyArticles = onlyArticles;
        this.bufferSize = bufferSize;
    }

    public Iterator<Page> iterator() {
        return new PageIterator(wiki, onlyArticles, bufferSize);
    }
}
    

