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
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An iterator over category objects.
 *
 */
public class CategoryIterator implements Iterator<Category> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CategoryBuffer buffer;

    public CategoryIterator(Wikipedia wiki, int bufferSize) {
        buffer = new CategoryBuffer(bufferSize, wiki);
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
     *
     */
    class CategoryBuffer{

        private final Wikipedia wiki;

        private final List<Category> buffer;
        private final int maxBufferSize;  // the number of pages to be buffered after a query to the database.
        private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
        private int bufferOffset;   // the offset in the buffer
        private int dataOffset;     // the overall offset in the data

        public CategoryBuffer(int bufferSize, Wikipedia wiki){
            this.maxBufferSize = bufferSize;
            this.wiki = wiki;
            this.buffer = new ArrayList<>();
            this.bufferFillSize = 0;
            this.bufferOffset = 0;
            this.dataOffset = 0;
            //TODO test whether this works when zero pages are retrieved
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

            Session session = this.wiki.__getHibernateSession();
            session.beginTransaction();
            final String sql = "SELECT c FROM Category c";
            List<org.dkpro.jwpl.api.hibernate.Category> returnValues =
                    session.createQuery(sql, org.dkpro.jwpl.api.hibernate.Category.class)
                .setFirstResult(dataOffset)
                .setMaxResults(maxBufferSize)
                .setFetchSize(maxBufferSize)
                .list();
            session.getTransaction().commit();

            // clear the old buffer and all variables regarding the state of the buffer
            buffer.clear();
            bufferOffset = 0;
            bufferFillSize = 0;

            Category apiCategory;
            for(org.dkpro.jwpl.api.hibernate.Category o : returnValues){
                if(o==null) {
                    return false;
                } else {
                    long id = o.getId();
                    try {
                        apiCategory= new Category(this.wiki, id);
                        buffer.add(apiCategory);
                    } catch (WikiApiException e) {
                        logger.error("Page with hibernateID {} not found.", id, e);
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


//    private Wikipedia wiki;
//    private int iterPosition;
//
//    public CategoryIterator(Wikipedia wiki) {
//        this.wiki = wiki;
//        this.iterPosition = 0;
//    }
//
//    public boolean hasNext() {
//        Session session = this.wiki.__getHibernateSession();
//        session.beginTransaction();
//        Object returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Category.class)
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        session.getTransaction().commit();
//
//        if (returnValue == null) {
//            return false;
//        }
//        else {
//            return true;
//        }
//
//    }
//
//    public Category next() {
//        Session session = this.wiki.__getHibernateSession();
//        session.beginTransaction();
//        Object returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Category.class)
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        session.getTransaction().commit();
//
//        Category apiCategory;
//
//        if (returnValue == null) {
//            return null;
//        }
//        else {
//            org.tud.ukp.wikipedia.api.hibernate.Category hibernateCategory = (org.tud.ukp.wikipedia.api.hibernate.Category) returnValue;
//            long id = hibernateCategory.getId();
//            try {
//                apiCategory = new org.tud.ukp.wikipedia.api.Category(this.wiki, id);
//                iterPosition++;
//                return apiCategory;
//            } catch (WikiPageNotFoundException e) {
//                logger.error("Category with hibernateID " + id + " not found.");
//                e.printStackTrace();
//            }
//        }
//        return null;
//
//    }
//
//    public void remove() {
//        throw new UnsupportedOperationException();
//    }
}
