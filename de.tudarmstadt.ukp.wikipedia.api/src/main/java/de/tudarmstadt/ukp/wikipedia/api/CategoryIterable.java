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
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.Iterator;


/**
 * An iterable over category objects.
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



