/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
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
import org.hibernate.criterion.Restrictions;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * An iterator over page objects.
 * @author zesch
 *
 */
public class PageIterator implements Iterator<Page> {

	private final Log logger = LogFactory.getLog(getClass());

    private PageBuffer buffer;

	public PageIterator(Wikipedia wiki, boolean onlyArticles, int bufferSize) {
		buffer = new PageBuffer(bufferSize, wiki, onlyArticles);
	}

	public boolean hasNext(){
		return buffer.hasNext();
	}

	public Page next(){
		return buffer.next();
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}

	/**
	 * Buffers pages in a list.
	 *
	 * @author zesch
	 *
	 */
	class PageBuffer{

		private Wikipedia wiki;
		private boolean onlyArticles;

		private List<Page> buffer;
		private int maxBufferSize;	// the number of pages to be buffered after a query to the database.
		private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
		private int bufferOffset; 	// the offset in the buffer
		private int dataOffset;		// the overall offset in the data

		public PageBuffer(int bufferSize, Wikipedia wiki, boolean onlyArticles){
			this.maxBufferSize = bufferSize;
			this.wiki = wiki;
			this.onlyArticles = onlyArticles;
			this.buffer = new ArrayList<Page>();
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
		 * @return The next Page or null if no more pages are available.
		 */
		public Page next(){
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

		private Page getBufferElement() {
			Page page = buffer.get(bufferOffset);
			bufferOffset++;
			dataOffset++;
			return page;
		}

//		private void showBuffer() {
//			for (Page p : buffer) {
//				try {
//					logger.info(p.getTitle().getPlainTitle());
//				} catch (WikiTitleParsingException e) {
//					e.printStackTrace();
//				}
//			}
//		}

		private boolean fillBuffer() {

			Session session = this.wiki.__getHibernateSession();
	        session.beginTransaction();
	        List returnValues = null;
	        if (onlyArticles) {
	            returnValues = session.createCriteria(de.tudarmstadt.ukp.wikipedia.api.hibernate.Page.class)
	            .add(Restrictions.eq("isDisambiguation", false))
	            .setFirstResult(dataOffset)
	            .setMaxResults(maxBufferSize)
	            .list();
	        }
	        else {
	            returnValues = session.createCriteria(de.tudarmstadt.ukp.wikipedia.api.hibernate.Page.class)
	            .setFirstResult(dataOffset)
	            .setMaxResults(maxBufferSize)
	            .list();
	        }
	        session.getTransaction().commit();

	        // clear the old buffer and all variables regarding the state of the buffer
	        buffer.clear();
	        bufferOffset = 0;
	        bufferFillSize = 0;

	        Page apiPage;
	        for(Object o : returnValues){
	        	if(o==null) {
	        		return false;
	        	} else {
	        		de.tudarmstadt.ukp.wikipedia.api.hibernate.Page hibernatePage = (de.tudarmstadt.ukp.wikipedia.api.hibernate.Page) o;
	        		long id = hibernatePage.getId();
	        		try {
		                apiPage = new Page(this.wiki, id);
		                if (this.onlyArticles) {
		                    if (!apiPage.isRedirect()) {
		                        buffer.add(apiPage);
		                    }
		                }
		                else {
		                	buffer.add(apiPage);
		                }
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
		} // fillBuffer

	}
}


//    public PageIterator(Wikipedia wiki, boolean onlyArticles, int bufferSize) {
//        this.wiki = wiki;
//        this.iterPosition = 0;
//        this.onlyArticles = onlyArticles;
//        this.bufferSize = bufferSize;
//    }
//
//    public boolean hasNext() {
//        Session session = this.wiki.__getHibernateSession();
//        session.beginTransaction();
//        Object returnValue = null;
//        if (onlyArticles) {
//            returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Page.class)
//            .add(Restrictions.eq("isDisambiguation", false))
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        }
//        else {
//            returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Page.class)
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        }
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
//    public Page next() {
//        Session session = this.wiki.__getHibernateSession();
//        session.beginTransaction();
//        Object returnValue = null;
//        if (onlyArticles) {
//            returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Page.class)
//            .add(Restrictions.eq("isDisambiguation", false))
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        }
//        else {
//            returnValue = session.createCriteria(org.tud.ukp.wikipedia.api.hibernate.Page.class)
//            .setFirstResult(iterPosition)
//            .setMaxResults(1)
//            .uniqueResult();
//        }
//        session.getTransaction().commit();
//
//        Page apiPage;
//
//        if (returnValue == null) {
//            return null;
//        }
//        else {
//            org.tud.ukp.wikipedia.api.hibernate.Page hibernatePage = (org.tud.ukp.wikipedia.api.hibernate.Page) returnValue;
//            long id = hibernatePage.getId();
//            try {
//                apiPage = new Page(this.wiki, id);
//                if (this.onlyArticles) {
//                    if (!apiPage.isRedirect()) {
//                        iterPosition++;
//                        return apiPage;
//                    }
//                }
//                else {
//                    iterPosition++;
//                    return apiPage;
//                }
//            } catch (WikiApiException e) {
//                logger.error("Page with hibernateID " + id + " not found.");
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
//}
