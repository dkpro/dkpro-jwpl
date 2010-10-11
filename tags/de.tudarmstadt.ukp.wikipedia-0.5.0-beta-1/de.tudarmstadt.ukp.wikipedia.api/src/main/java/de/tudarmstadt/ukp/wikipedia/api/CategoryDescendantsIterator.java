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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An iterator over category objects retrieved by Category.getDescendants()
 * @author zesch
 *
 */
public class CategoryDescendantsIterator implements Iterator<Category> {

	private final Log logger = LogFactory.getLog(getClass());

    private Wikipedia wiki;

    private CategoryBuffer buffer;

    /** Contains all category ids that have not been expanded, yet. */
    private Set<Integer> notExpandedCategories;

    /** As we do not inspect the whole graph at once now, we need a way to check whether a node was already expanded, to avoid infinite loops. */
    private Set<Integer> expandedCategoryIds;

    public CategoryDescendantsIterator(Wikipedia wiki, int bufferSize, Category startCategory) {
        this.wiki = wiki;
        buffer = new CategoryBuffer(bufferSize);
        notExpandedCategories = new HashSet<Integer>();
        // initialize with children of start category
        for (Category catItem : startCategory.getChildren()) {
            notExpandedCategories.add(catItem.getPageId());
        }

        expandedCategoryIds = new HashSet<Integer>();
    }

    public boolean hasNext(){
        return buffer.hasNext();
    }

    public Category next(){
        return buffer.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Buffers categories in a list.
     *
     * @author zesch
     *
     */
    class CategoryBuffer{

        private List<Category> buffer;
        private int maxBufferSize;  // the number of pages to be buffered after a query to the database.
        private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
        private int bufferOffset;   // the offset in the buffer
        private int dataOffset;     // the overall offset in the data

        public CategoryBuffer(int bufferSize){
            this.maxBufferSize = bufferSize;
            this.buffer = new ArrayList<Category>();
            this.bufferFillSize = 0;
            this.bufferOffset = 0;
            this.dataOffset = 0;

//TODO test whether this works when zero pages are retrieved
// we can test this here using a unit test that retrieves no descendants!
        }

        /**
         * If there are elements in the buffer left, then return true.
         * If the end of the filled buffer is reached, then try to load new buffer.
         * @return True, if there are pages left. False otherwise.
         */
        public boolean hasNext(){
            if (bufferOffset < bufferFillSize) {
                return true;
            }
            else {
                return this.fillBuffer();
            }
        }

        /**
         *
         * @return The next Category or null if no more categories are available.
         */
        public Category next(){
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

        private Category getBufferElement() {
            Category cat = buffer.get(bufferOffset);
            bufferOffset++;
            dataOffset++;
            return cat;
        }

        private boolean fillBuffer() {

            // clear the old buffer and all variables regarding the state of the buffer
            buffer.clear();
            bufferOffset = 0;
            bufferFillSize = 0;

            List<Integer> queue = new LinkedList<Integer>();

            // add not expanded categories to queue
            queue.addAll(notExpandedCategories);

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

            if (buffer.size() > 0) {
                bufferFillSize = buffer.size();
                return true;
            }
            else {
                return false;
            }
        }
    }
}
