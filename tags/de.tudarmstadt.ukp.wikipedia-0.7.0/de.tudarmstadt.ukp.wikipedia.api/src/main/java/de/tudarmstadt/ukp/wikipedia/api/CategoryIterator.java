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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * An iterator over category objects.
 * @author zesch
 *
 */
public class CategoryIterator implements Iterator<Category> {

	private final Log logger = LogFactory.getLog(getClass());

    private CategoryBuffer buffer;

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
     * @author zesch
     *
     */
    class CategoryBuffer{

        private Wikipedia wiki;

        private List<Category> buffer;
        private int maxBufferSize;  // the number of pages to be buffered after a query to the database.
        private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
        private int bufferOffset;   // the offset in the buffer
        private int dataOffset;     // the overall offset in the data

        public CategoryBuffer(int bufferSize, Wikipedia wiki){
            this.maxBufferSize = bufferSize;
            this.wiki = wiki;
            this.buffer = new ArrayList<Category>();
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
            List returnValues = null;
            returnValues = session.createCriteria(de.tudarmstadt.ukp.wikipedia.api.hibernate.Category.class)
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
            for(Object o : returnValues){
                if(o==null) {
                    return false;
                } else {
                    de.tudarmstadt.ukp.wikipedia.api.hibernate.Category hibernateCategory = (de.tudarmstadt.ukp.wikipedia.api.hibernate.Category) o;
                    long id = hibernateCategory.getId();
                    try {
                        apiCategory= new Category(this.wiki, id);
                        buffer.add(apiCategory);
                    } catch (WikiApiException e) {
                        logger.error("Page with hibernateID " + id + " not found.");
                        e.printStackTrace();
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
