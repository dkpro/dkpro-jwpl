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
 * An iterable over all titles.
 * @author zesch
 *
 */
public class TitleIterable implements Iterable<Title> {

    private Wikipedia wiki;
    
    /** 
     * The size of the title buffer.
     * With bufferSize = 1, a database connection is needed for retrieving a single title.
     * Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 5000. 
     */
    private int bufferSize = 5000;
    
    public TitleIterable(Wikipedia wiki) {
        this.wiki = wiki;
    }

    public TitleIterable(Wikipedia wiki, int bufferSize) {
        this.wiki = wiki;
        this.bufferSize = bufferSize;
    }

    public Iterator<Title> iterator() {
        return new TitleIterator(wiki, bufferSize);
    }
}    



