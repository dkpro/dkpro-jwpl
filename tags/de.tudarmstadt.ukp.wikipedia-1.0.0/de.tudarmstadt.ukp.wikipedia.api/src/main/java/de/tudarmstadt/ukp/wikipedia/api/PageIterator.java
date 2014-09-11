 /*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Samy Ateia - Improved performance
 *     	see http://groups.google.com/group/jwpl/browse_thread/thread/79393bdd9fb84de9
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * An iterator over page objects.
 *
 * @author zesch
 * @author Oliver Ferschke
 *
 */
public class PageIterator implements Iterator<Page> {

	private final Log logger = LogFactory.getLog(getClass());

    private final PageBuffer buffer;

	public PageIterator(Wikipedia wiki, Set<String> ids, Set<String> titles, int bufferSize) {
		buffer = new PageBuffer(bufferSize, wiki, ids, titles);
	}

    public PageIterator(Wikipedia wiki, boolean onlyArticles, int bufferSize) {
		buffer = new PageBuffer(bufferSize, wiki, onlyArticles);
	}

	@Override
	public boolean hasNext(){
		return buffer.hasNext();
	}

	@Override
	public Page next(){
		return buffer.next();
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException();
	}

	/**
	 * Buffers pages in a list.
	 *
	 * @author zesch
	 * @author Oliver Ferschke
	 *
	 */
	class PageBuffer{

		private final Wikipedia wiki;
		private final boolean onlyArticles;

		private final List<Page> buffer;
		private final int maxBufferSize;	// the number of pages to be buffered after a query to the database.
		private int bufferFillSize; // even a 500 slot buffer can be filled with only 5 elements
		private int bufferOffset; 	// the offset in the buffer
		private long lastPage;// the overall offset in the data

		private List<String> pageIds = new LinkedList<String>(); // a set of ids, if a specific list of articles is supposed to be read
		private List<String> pageTitles = new LinkedList<String>(); // a set of titles, if a specific list of articles is supposed to be read
		boolean loadFromList;

		public PageBuffer(int bufferSize, Wikipedia wiki, boolean onlyArticles){
			this.maxBufferSize = bufferSize;
			this.wiki = wiki;
			this.onlyArticles = onlyArticles;
			this.buffer = new ArrayList<Page>();
			this.bufferFillSize = 0;
			this.bufferOffset = 0;
			this.lastPage = 0;
			this.loadFromList=false;
			//TODO test whether this works when zero pages are retrieved
		}

		public PageBuffer(int bufferSize, Wikipedia wiki, Set<String> ids, Set<String> titles){
			this.maxBufferSize = bufferSize;
			this.wiki = wiki;
			this.buffer = new ArrayList<Page>();
			this.onlyArticles = false;
			this.bufferFillSize = 0;
			this.bufferOffset = 0;
			this.lastPage = 0;
			this.pageIds=new LinkedList<String>(ids);
			this.pageTitles=new LinkedList<String>(titles);
			this.loadFromList=true;
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

			//decide whether to load from list or retrieve all available articles
			if(loadFromList){
		        // clear the old buffer and all variables regarding the state of the buffer
		        buffer.clear();
		        bufferOffset = 0;
		        bufferFillSize = 0;

				//load pages
		        if(pageIds.isEmpty()&&pageTitles.isEmpty()){
		        	return false;
		        }

		        while(bufferFillSize<=maxBufferSize&&!pageIds.isEmpty()){
					String id = pageIds.remove(0);
		        	if(id!=null&&!id.isEmpty()){
						try{
							buffer.add(wiki.getPage(Integer.parseInt(id)));
				        	bufferFillSize++;
						}catch(WikiApiException e){
							logger.warn("Missing article with id "+id);
						}
					}
				}
		        while(bufferFillSize<=maxBufferSize&&!pageTitles.isEmpty()){
		        	String title = pageTitles.remove(0);
					if(title!=null&&!title.isEmpty()){
						try{
							buffer.add(wiki.getPage(title));
				        	bufferFillSize++;
						}catch(WikiApiException e){
							logger.warn("Missing article with title \""+title+"\"");
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
			}else{
				Session session = this.wiki.__getHibernateSession();
		        session.beginTransaction();
		        List returnValues = null;
		        if (onlyArticles) {
		            returnValues = session.createCriteria(de.tudarmstadt.ukp.wikipedia.api.hibernate.Page.class)
		            .add(Restrictions.eq("isDisambiguation", false))
		            .add(Restrictions.gt("id", lastPage))
		            .setMaxResults(maxBufferSize)
		            .list();
		        }
		        else {
		            returnValues = session.createCriteria(de.tudarmstadt.ukp.wikipedia.api.hibernate.Page.class)
		            .add(Restrictions.gt("id", lastPage))
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
		        			apiPage = new Page(this.wiki, id, hibernatePage);
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
			            lastPage = id;
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
		} // fillBuffer

	}
}