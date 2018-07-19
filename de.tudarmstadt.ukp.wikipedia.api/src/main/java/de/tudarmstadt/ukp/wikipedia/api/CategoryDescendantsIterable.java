/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.Iterator;


/**
 * An iterable over category objects retrieved by Category.getDescendants()
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
