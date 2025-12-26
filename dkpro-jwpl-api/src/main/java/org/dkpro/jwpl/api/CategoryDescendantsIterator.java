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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link Iterator} over category objects retrieved by {@link Category#getDescendants()}.
 */
public class CategoryDescendantsIterator
    implements Iterator<Category>
{

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Wikipedia wiki;

    private final CategoryBuffer buffer;

    /**
     * Contains all category ids that have not been expanded, yet.
     */
    private final Set<Integer> notExpandedCategories;

    /**
     * As we do not inspect the whole graph at once now, we need a way to check whether a node was
     * already expanded, to avoid infinite loops.
     */
    private final Set<Integer> expandedCategoryIds;

    /**
     * Initializes a {@link CategoryDescendantsIterator} instance.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param bufferSize The number of pages to be buffered after a query to the database.
     *                   Higher bufferSize gives better performance, but require more memory.
     * @param startCategory The Wikipedia category to start descending from. Must not be {@code null}.
     */
    public CategoryDescendantsIterator(Wikipedia wiki, int bufferSize, Category startCategory)
    {
        this.wiki = wiki;
        buffer = new CategoryBuffer(bufferSize);
        notExpandedCategories = new HashSet<>();
        // initialize with children of start category
        for (Category catItem : startCategory.getChildren()) {
            notExpandedCategories.add(catItem.getPageId());
        }

        expandedCategoryIds = new HashSet<>();
    }

    @Override
    public boolean hasNext()
    {
        return buffer.hasNext();
    }

    @Override
    public Category next()
    {
        return buffer.next();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Buffers categories in a list.
     */
    class CategoryBuffer
    {

        private final List<Category> buffer;
        private final int maxBufferSize; // the number of pages to be buffered after a query to the
                                         // database.
        private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
        private int bufferOffset; // the offset in the buffer
        private int dataOffset; // the overall offset in the data

        public CategoryBuffer(int bufferSize)
        {
            this.maxBufferSize = bufferSize;
            this.buffer = new ArrayList<>();
            this.bufferFillSize = 0;
            this.bufferOffset = 0;
            this.dataOffset = 0;

            // TODO test whether this works when zero pages are retrieved
            // we can test this here using a unit test that retrieves no descendants!
        }

        /**
         * If there are elements in the buffer left, then return true. If the end of the filled
         * buffer is reached, then try to load new buffer.
         *
         * @return True, if there are pages left. {@code false} otherwise.
         */
        public boolean hasNext()
        {
            if (bufferOffset < bufferFillSize) {
                return true;
            }
            else {
                return this.fillBuffer();
            }
        }

        /**
         * @return The next Category or {@code null} if no more categories are available.
         */
        public Category next()
        {
            // if there are still elements in the buffer, just retrieve the next one
            if (bufferOffset < bufferFillSize) {
                return this.getBufferElement();
            }
            // if there are no more elements => try to fill a new buffer
            else if (this.fillBuffer()) {
                return this.getBufferElement();
            }
            else {
                // if it cannot be filled => return null
                return null;
            }
        }

        private Category getBufferElement()
        {
            Category cat = buffer.get(bufferOffset);
            bufferOffset++;
            dataOffset++;
            return cat;
        }

        private boolean fillBuffer()
        {

            // clear the old buffer and all variables regarding the state of the buffer
            buffer.clear();
            bufferOffset = 0;
            bufferFillSize = 0;

            // add not expanded categories to queue
            List<Integer> queue = new LinkedList<>(notExpandedCategories);

            // expand until buffer size is reached
            while (!queue.isEmpty() && buffer.size() < maxBufferSize) {
                // remove first element from queue
                Category currentCat = wiki.getCategory(queue.get(0));
                queue.remove(0);

                // if the node was not previously expanded
                if (!expandedCategoryIds.contains(currentCat.getPageId())) {
                    buffer.add(currentCat);
                    notExpandedCategories.remove(currentCat.getPageId());
                    expandedCategoryIds.add(currentCat.getPageId());

                    logger.debug("buf: " + buffer.size());
                    logger.debug("notExp: " + notExpandedCategories);
                    logger.debug("exp: " + expandedCategoryIds);

                    for (Category child : currentCat.getChildren()) {
                        queue.add(child.getPageId());
                        notExpandedCategories.add(child.getPageId());
                    }
                }
            }

            if (!buffer.isEmpty()) {
                bufferFillSize = buffer.size();
                return true;
            }
            else {
                return false;
            }
        }
    }
}
