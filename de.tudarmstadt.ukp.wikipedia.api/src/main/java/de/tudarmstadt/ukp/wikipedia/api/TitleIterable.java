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
 * An iterable over all titles.
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



