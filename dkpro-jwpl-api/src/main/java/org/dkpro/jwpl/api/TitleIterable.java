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
 * An {@link Iterable} over {@link Title} objects.
 */
public class TitleIterable
    implements Iterable<Title>
{

    private final Wikipedia wiki;

    /*
     * The size of the title buffer. With bufferSize = 1, a database connection is needed for
     * retrieving a single title. Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 5000.
     */
    private int bufferSize = 5000;

    /**
     * Initializes a {@link TitleIterable} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     */
    public TitleIterable(Wikipedia wiki)
    {
        this.wiki = wiki;
    }

    /**
     * Initializes a {@link TitleIterator} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param bufferSize The number of pages to be buffered after a query to the database.
     *                   Higher bufferSize gives better performance, but require more memory.
     */
    public TitleIterable(Wikipedia wiki, int bufferSize)
    {
        this.wiki = wiki;
        this.bufferSize = bufferSize;
    }

    @Override
    public Iterator<Title> iterator()
    {
        return new TitleIterator(wiki, bufferSize);
    }
}
