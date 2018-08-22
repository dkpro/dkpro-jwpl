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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;

/**
 * An iterator over {@link Page} objects.
 */
public class PageIterator implements Iterator<Page> {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
	 * Buffers {@link Page pages} in a list.
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
		 * @return The next {@link Page} or {@code null} if no more pages are available.
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
				Query query;
		        if (onlyArticles) {
					query = session.createQuery("SELECT p FROM Page p WHERE p.isDisambiguation = :isDisambiguation AND p.id > :pageId");
					query.setParameter("isDisambiguation", false);
					query.setParameter("pageId", lastPage);
		        }
		        else {
					query = session.createQuery("SELECT p FROM Page p WHERE p.id > :pageId");
					query.setParameter("pageId", lastPage);
		        }
				query.setMaxResults(maxBufferSize);
				returnValues = query.getResultList();
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