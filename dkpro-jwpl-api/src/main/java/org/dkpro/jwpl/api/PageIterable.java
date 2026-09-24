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
 * An {@link Iterable} of {@link Page} objects.
 */
public class PageIterable
    implements Iterable<Page>
{

    private final Wikipedia wiki;

    /*
     * Whether only articles are retrieved (or also disambiguation pages)
     */
    private final boolean onlyArticles;

    /*
     * The size of the page buffer. With bufferSize = 1, a database connection is needed for
     * retrieving a single article. Higher bufferSize gives better performance, but needs memory.
     * Initialize it with 500.
     */
    private int bufferSize = 500;

    /*
     * Whether the text of the pages is loaded together with them.
     */
    private final boolean withText;

    /**
     * Initializes a {@link PageIterable} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param onlyArticles {@code True} if only full article pages shall be processed, yet no disambiguation pages.
     *                     {@code False} if disambiguation pages shall be considered as well.
     */
    public PageIterable(Wikipedia wiki, boolean onlyArticles)
    {
        this(wiki, onlyArticles, true);
    }

    /**
     * Initializes a {@link PageIterable} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param onlyArticles {@code True} if only full article pages shall be processed, yet no disambiguation pages.
     *                     {@code False} if disambiguation pages shall be considered as well.
     * @param withText {@code True} if the text of the pages shall be loaded together with them.
     *                 {@code False} if the pages shall be loaded without their text, which suits
     *                 callers that need their metadata only. The text of such a page is queried
     *                 on the first call of its {@link Page#getText()}.
     * @see PageIterator#PageIterator(Wikipedia, boolean, int, boolean)
     */
    public PageIterable(Wikipedia wiki, boolean onlyArticles, boolean withText)
    {
        this.wiki = wiki;
        this.onlyArticles = onlyArticles;
        this.withText = withText;
    }

    /**
     * Initializes a {@link PageIterable} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param onlyArticles {@code True} if only full article pages shall be processed, yet no disambiguation pages.
     *                     {@code False} if disambiguation pages shall be considered as well.
     * @param bufferSize The number of pages to be buffered after a query to the database.
     *                   Higher bufferSize gives better performance, but require more memory.
     */
    protected PageIterable(Wikipedia wiki, boolean onlyArticles, int bufferSize)
    {
        this.wiki = wiki;
        this.onlyArticles = onlyArticles;
        this.bufferSize = bufferSize;
        this.withText = true;
    }

    @Override
    public Iterator<Page> iterator()
    {
        return new PageIterator(wiki, onlyArticles, bufferSize, withText);
    }
}
