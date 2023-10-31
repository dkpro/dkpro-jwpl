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
package org.dkpro.jwpl.api;

import java.util.Iterator;

/**
 * An {@link Iterable} over {@link Category} objects.
 */
public class CategoryIterable
    implements Iterable<Category>
{

    private final Wikipedia wiki;

    /*
     * The size of the page buffer. With bufferSize = 1, a database connection is needed for
     * retrieving a single article. Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 500.
     */
    private int bufferSize = 500;

    public CategoryIterable(Wikipedia wiki)
    {
        this.wiki = wiki;
    }

    public CategoryIterable(Wikipedia wiki, int bufferSize)
    {
        this.wiki = wiki;
        this.bufferSize = bufferSize;
    }

    @Override
    public Iterator<Category> iterator()
    {
        return new CategoryIterator(wiki, bufferSize);
    }
}
