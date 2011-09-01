/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Oliver Ferschke - several bugfixes and extensions
 *     Samy Ateia - provided a patch via the JWPL mailing list
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.WikiHibernateUtil;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.util.distance.LevenshteinStringDistance;


/**
 * Provides access to Wikipedia articles and categories.
 * @author zesch
 *
 */
// TODO better JavaDocs!
public class Wikipedia implements WikiConstants {

	private final Log logger = LogFactory.getLog(getClass());
    private final Language language;
    private final DatabaseConfiguration dbConfig;

    private final MediaWikiParser parser;

    /**
     * A mapping from page pageIDs to hibernateIDs.
     * It is a kind of cache. It is only filled, if a pageID was previously accessed.
     * The wikiapi startup time is way too long otherwise. */
    private final Map<Integer, Long> idMapPages;
    /**
     * A mapping from categories pageIDs to hibernateIDs.
     * It is a kind of cache. It is only filled, if a pageID was previously accessed.
     * The wikiapi startup time is way too long otherwise. */
    private final Map<Integer, Long> idMapCategories;

    private final MetaData metaData;

    /**
     * Creates a new Wikipedia object accessing the database indicated by the dbConfig parameter.
     * @param dbConfig A database configuration object telling the Wikipedida object where the data is stored and how it can be accessed.
     * @throws WikiInitializationException
     */
    public Wikipedia(DatabaseConfiguration dbConfig) throws WikiInitializationException {
        logger.info("Creating Wikipedia object.");

        this.language = dbConfig.getLanguage();
        this.dbConfig = dbConfig;

        this.idMapPages      = new HashMap<Integer,Long>();
        this.idMapCategories = new HashMap<Integer,Long>();

//// TODO We have to find a way to check the encoding language independently.
//        if (!checkEncoding()) {
//            throw new WikiApiException("There is an encoding problem within the WikiAPI database.");
//        }

        this.metaData = new MetaData(this);

        MediaWikiParserFactory pf = new MediaWikiParserFactory(this.language);
        this.parser = pf.createParser();


//// Building the maps at startup time was removed because it is way too slow
//// It was replaced by a cache-like mechanism.
//        idMapPages      = HibernateUtilities.getIdMappingPages();
//        idMapCategories = HibernateUtilities.getIdMappingCategories();
    }

    /**
     * Gets the page with the given title.
     * If the title is a redirect, the corresponding page is returned.<br/>
     * If the title start with a lowercase letter it converts it to an uppercase letter, as each Wikipedia article title starts with an uppercase letter.
     * Spaces in the title are converted to underscores, as this is a convention for Wikipedia article titles.
     *
     * For example, the article "Steam boat" could be queried with
     * - "Steam boat"
     * - "steam boat"
     * - "Steam_boat"
     * - "steam_boat"
     * and additionally all redirects that might point to that article.
     *
     * @param title The title of the page.
     * @return The page object for a given title.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Page getPage(String title) throws WikiApiException  {
        Page page = new Page(this, title);
        return page;
    }

    /**
     * Gets the page for a given pageId.
     *
     * @param pageId The id of the page.
     * @return The page object for a given pageId.
     * @throws WikiApiException
     */
    public Page getPage(int pageId) throws WikiApiException {
        Page page = new Page(this, pageId);
        return page;
    }

    /**
     * Gets the title for a given pageId.
     *
     * @param pageId The id of the page.
     * @return The title for the given pageId.
     * @throws WikiApiException
     */
    public Title getTitle(int pageId) throws WikiApiException {
    	Session session = this.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createSQLQuery(
            "select p.name from PageMapLine as p where p.id = ?").setInteger(0, pageId).uniqueResult();
        session.getTransaction().commit();

        String title = (String)returnValue;
        if(title==null){
        	throw new WikiPageNotFoundException();
        }
        return new Title(title);
    }

    /**
     * Gets the page ids for a given title.<br/>
     *
     *
     * @param title The title of the page.
     * @return The id for the page with the given title.
     * @throws WikiApiException
     */
    public List<Integer> getPageIds(String title) throws WikiApiException {
    	Session session = this.__getHibernateSession();
        session.beginTransaction();
        Iterator results = session.createQuery(
        "select p.pageID from PageMapLine as p where p.name = ?").setString(0, title).list().iterator();

        session.getTransaction().commit();

        if(!results.hasNext()){
        	throw new WikiPageNotFoundException();
        }
        List<Integer> resultList = new LinkedList<Integer>();
        while(results.hasNext()){
        	resultList.add((Integer)results.next());
        }
        return resultList;
    }

	/**
	 * Returns the article page for a given discussion page.
	 *
	 * @param discussionPage
	 *            the discussion page object
	 * @return The page object of the article associated with the discussion. If
	 *         the parameter already was an article, it is returned directly.
	 * @throws WikiApiException
	 */
    public Page getArticleForDiscussionPage(Page discussionPage) throws WikiApiException {
    	if(discussionPage.isDiscussion()){
    		String title = discussionPage.getTitle().getPlainTitle().replaceAll(WikiConstants.DISCUSSION_PREFIX, "");

    		if(title.contains("/")){
        		//If we have a discussion archive
        		//TODO This does not support articles that contain slashes-
        		//However, the rest of the API cannot cope with that as well, so this should not be any extra trouble
    			title = title.split("/")[0];
    		}
    		return getPage(title);
    	}else{
    		return discussionPage;
    	}

    }


    /**
     * Gets the discussion page for an article page with the given pageId.
     *
     * @param pageId The id of the page.
     * @return The page object for a given pageId.
     * @throws WikiApiException
     */
    public Page getDiscussionPage(int articlePageId) throws WikiApiException {
        //Retrieve discussion page with article title
    	//TODO not the prettiest solution, but currently discussions are only marked in the title
    	return getDiscussionPage(getPage(articlePageId));
    }

    /**
     * Gets the discussion page for the page with the given title.
     * The page retrieval works as defined in {@link #getPage(String title)}
     *
     * @param title The title of the page for which the discussions should be retrieved.
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Page getDiscussionPage(String title) throws WikiApiException  {
    	return getDiscussionPage(getPage(title));
    }

    /**
     * Gets the discussion page for the given article page
     * The provided page must not be a discussion page
     *
     * @param articlePage the article page for which a discussion page should be retrieved
     * @return The discussion page object for the given article page object
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Page getDiscussionPage(Page articlePage) throws WikiApiException{
    	String articleTitle = articlePage.getTitle().toString();
    	if(articleTitle.startsWith(WikiConstants.DISCUSSION_PREFIX)){
    		return articlePage;
    	}else{
        	return new Page(this, WikiConstants.DISCUSSION_PREFIX+articleTitle);
    	}
    }


    /**
	 * Returns an iterable containing all archived discussion pages for
     * the page with the given title String. <br/>
     * The page retrieval works as defined in {@link #getPage(int)}. <br/>
     * The most recent discussion page is NOT included here!
     * It can be obtained with {@link #getDiscussionPage(Page)}.
     *
     * @param articlePageId The id of the page for which to the the discussion archives
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Iterable<Page> getDiscussionArchives(int articlePageId) throws WikiApiException {
        //Retrieve discussion archive pages with page id
    	return getDiscussionArchives(getPage(articlePageId));
    }

    /**
	 * Returns an iterable containing all archived discussion pages for
     * the page with the given title String. <br/>
     * The page retrieval works as defined in {@link #getPage(String title)}.<br/>
     * The most recent discussion page is NOT included here!
     * It can be obtained with {@link #getDiscussionPage(Page)}.
     *
     * @param title The title of the page for which the discussions should be retrieved.
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Iterable<Page> getDiscussionArchives(String title) throws WikiApiException  {
        //Retrieve discussion archive pages with page title
    	return getDiscussionArchives(getPage(title));
    }

    /**
     * Return an iterable containing all archived discussion pages for
     * the given article page. The most recent discussion page is not included.
     * The most recent discussion page can be obtained with {@link #getDiscussionPage(Page)}.
     * <br/>
     * The provided page Object must not be a discussion page itself! If it is
     * a discussion page, is returned unchanged.
     *
     * @param articlePage the article page for which a discussion archives should be retrieved
     * @return An iterable with the discussion archive page objects for the given article page object
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Iterable<Page> getDiscussionArchives(Page articlePage) throws WikiApiException{
    	String articleTitle = articlePage.getTitle().toString();
    	if(!articleTitle.startsWith(WikiConstants.DISCUSSION_PREFIX)){
    		articleTitle=WikiConstants.DISCUSSION_PREFIX+articleTitle;
    	}

    	Session session = this.__getHibernateSession();
        session.beginTransaction();

        List<Page> discussionArchives = new LinkedList<Page>();

        Query query = session.createQuery("SELECT pageID FROM PageMapLine where name like ?");
        query.setString(0, articleTitle+"/%");
        Iterator results = query.list().iterator();

        session.getTransaction().commit();

        while (results.hasNext()) {
            int pageID = (Integer) results.next();
            discussionArchives.add(getPage(pageID));
        }

        return discussionArchives;

    }

//// I do not want to make this public at the moment (TZ, March, 2007)
    /**
     * Gets the pages or redirects with a name similar to the pattern.
     * Calling this method is quite costly, as similarity is computed for all names.
     * @param pPattern The pattern.
     * @param pSize The maximum size of the result list. Only the most similar results will be included.
     * @return A map of pages with names similar to the pattern and their distance values. Smaller distances are more similar.
     * @throws WikiApiException
     */
    protected Map<Page, Double> getSimilarPages(String pPattern, int pSize) throws WikiApiException {
        Title title = new Title(pPattern);
        String pattern = title.getWikiStyleTitle();

        // a mapping of the most similar pages and their similarity values
        // It is returned by this method.
        Map<Page, Double> pageMap = new HashMap<Page, Double>();

        // holds a mapping of the best distance values to page IDs
        Map<Integer, Double> distanceMap = new HashMap<Integer, Double>();

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        Iterator results = session.createQuery(
                "select pml.pageID, pml.name from PageMapLine as pml")
                .list()
                .iterator();
        while (results.hasNext()) {
            Object[] row = (Object[]) results.next();
            int pageID = (Integer) row[0];
            String pageName = (String) row[1];


//// this returns a similarity - if we want to use it, we have to change the semantics the ordering of the results
//            double distance = new Levenshtein().getSimilarity(pageName, pPattern);
            double distance = new LevenshteinStringDistance().distance(pageName, pattern);

            distanceMap.put(pageID, distance);

            // if there are more than "pSize" entries in the map remove the last one (it has the biggest distance)
            if (distanceMap.size() > pSize) {
                Set <Map.Entry<Integer,Double>> valueSortedSet = new TreeSet <Map.Entry<Integer,Double>> (new ValueComparator());
                valueSortedSet.addAll(distanceMap.entrySet());
                Iterator it = valueSortedSet.iterator();
                // remove the first element
                if  (it.hasNext() ) {
                    // get the id of this entry and remove it in the distanceMap
                    Map.Entry entry = (Map.Entry)it.next();
                    distanceMap.remove(entry.getKey());
                }
            }

        }
        session.getTransaction().commit();

        for (int pageID : distanceMap.keySet()) {
            Page page = null;
            try {
                page = this.getPage(pageID);
            } catch (WikiPageNotFoundException e) {
                logger.error("Page with pageID " + pageID + " could not be found. Fatal error. Terminating.");
                e.printStackTrace();
                System.exit(1);
            }
            pageMap.put(page, distanceMap.get(pageID));
        }

        return pageMap;
    }

    /**
     * Gets the category for a given title.
     * If the category title start with a lowercase letter it converts it to an uppercase letter, as each Wikipedia category title starts with an uppercase letter.
     * Spaces in the title are converted to underscores, as this is a convention for Wikipedia category titles.
     *
     * For example, the (possible) category "Famous steamboats" could be queried with
     * - "Famous steamboats"
     * - "Famous_steamboats"
     * - "famous steamboats"
     * - "famous_steamboats"
     * @param title The title of the category.
     * @return The category object with the given title.
     * @throws WikiApiException If no category with the given title exists.
     */
    public Category getCategory(String title) throws WikiApiException   {
        Category cat = new Category(this, title);
        return cat;
    }

    /**
     * Gets the category for a given pageId.
     * @param pageId The id of the category.
     * @return The category object or null if no category with this pageId exists.
     */
    public Category getCategory(int pageId) {
        long hibernateId = __getCategoryHibernateId(pageId);
        if (hibernateId == -1) {
            return null;
        }

        try {
            Category cat = new Category(this, hibernateId);
            return cat;
        } catch (WikiPageNotFoundException e) {
            return null;
        }
    }

    /**
     * This returns an iterable over all categories, as returning all category objects would be much too expensive.
     * @return An iterable over all categories.
     */
    public Iterable<Category> getCategories() {
        return new CategoryIterable(this);
    }

    /**
     * Get all wikipedia categories.
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     * @param bufferSize The size of the internal page buffer.
     * @return An iterable over all categories.
     */
    protected Iterable<Category> getCategories(int bufferSize) {
        return new CategoryIterable(this, bufferSize);
    }


    /**
     * Protected method that is much faster than the public version, but exposes too much implementation details.
     * Get a set with all category pageIDs. Returning all category objects is much too expensive.
     * @return A set with all category pageIDs
     */
    protected Set<Integer> __getCategories() {
// TODO this should be replaced with the buffered category iterator, as it might produce an HeapSpace Overflow, if there are too many categories.

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List<Integer> idList = session.createQuery(
            "select cat.pageId from Category as cat")
            .list();
        Set<Integer> categorySet = new HashSet<Integer>(idList);
        session.getTransaction().commit();

        return categorySet;
    }

    /**
     * Get all wikipedia pages.
     * Does not include redirects, as they are only pointers to real pages.
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     * @return An iterable over all pages.
     */
    public Iterable<Page> getPages() {
        return new PageIterable(this, false);
    }

    /**
     * Get all wikipedia pages.
     * Does not include redirects, as they are only pointers to real pages.
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     * @param bufferSize The size of the internal page buffer.
     * @return An iterable over all pages.
     */
    protected Iterable<Page> getPages(int bufferSize) {
        return new PageIterable(this, false, bufferSize);
    }

    /**
     * Protected method that is much faster than the public version, but exposes too much implementation details.
     * Get a set with all pageIDs. Returning all page objects is much too expensive.
     * Does not include redirects, as they are only pointers to real pages.
     *
     * As ids can be useful for several application (e.g. in combination with
     * the RevisionMachine, they have been made publically available via
     * {@link getPageIds()}.
     *
     * @return A set with all pageIDs. Returning all pages is much to expensive.
     */
    protected Set<Integer> __getPages() {
        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List<Integer> idList = session.createQuery(
            "select page.pageId from Page as page")
            .list();
        Set<Integer> pageSet = new HashSet<Integer>(idList);
        session.getTransaction().commit();

        return pageSet;
    }

    /**
     * @return an iterable over all pageids (without redirects)
     */
    public Iterable<Integer> getPageIds(){
    	return this.__getPages();
    }

    /**
     * Get the pages that match the given query.
     * Does not include redirects, as they are only pointers to real pages.
     * Attention: may be running very slow, depending on the size of the Wikipedia!
     * @param query A query object containing the query conditions.
     * @return A set of pages that match the given query.
     * @throws WikiApiException
     */
    public Iterable<Page> getPages(PageQuery query) throws WikiApiException {
        return new PageQueryIterable(this, query);
    }


    /**
     * Get all articles (pages MINUS disambiguationPages MINUS redirects).
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     * @return An iterable of all article pages.
     */
    public Iterable<Page> getArticles() {
        return new PageIterable(this, true);
    }

    /**
     * Get all titles including disambiguation pages and redirects).
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     * @return An iterable of all article pages.
     */
    public Iterable<Title> getTitles() {
        return new TitleIterable(this);
    }

    /**
     * Returns the language of this Wikipedia.
     * @return The language of this Wikipedia.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Tests, whether a page or redirect with the given title exists.
     * Trying to retrieve a page that does not exist in Wikipedia throws an exception.
     * You may catch the exception or use this test, depending on your task.
     * @param title The title of the page.
     * @return True, if a page or redirect with that title exits, false otherwise.
     */
    public boolean existsPage(String title) {

        if (title == null || title.length() == 0) {
            return false;
        }

        Title t;
        try {
			t = new Title(title);
		} catch (WikiTitleParsingException e) {
			return false;
		}
		String encodedTitle = t.getWikiStyleTitle();

    	Session session = this.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createSQLQuery(
            "select p.id from PageMapLine as p where p.name = ? COLLATE utf8_bin")
            .setString(0, encodedTitle)
            .uniqueResult();
        session.getTransaction().commit();

        if (returnValue == null) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Tests, whether a page with the given pageID exists.
     * Trying to retrieve a pageID that does not exist in Wikipedia throws an exception.
     *
     * @param pageID A pageID.
     * @return True, if a page with that pageID exits, false otherwise.
     */
    public boolean existsPage(int pageID) {

        // TODO carefully, this is a hack to provide a much quicker way to test whether a page exists.
        // Encoding the title in this way surpasses the normal way of creating a title first.
        // This should get a unit test to make sure the encoding function is in line with the title object.
        // Anyway, I do not like this hack :-|

        if (pageID < 0) {
            return false;
        }

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List returnList = session.createSQLQuery(
            "select p.id from PageMapLine as p where p.pageID = ?")
            .setInteger(0, pageID)
            .list();
        session.getTransaction().commit();

        if (returnList.size() == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Get the hibernate ID to a given pageID of a page.
     * We need different methods for pages and categories here, as a page and a category can have the same ID.
     *
     * @param pageID A pageID that should be mapped to the corresponding hibernate ID.
     * @return The hibernateID of the page with pageID or -1, if the pageID is not valid
     */
    protected long __getPageHibernateId(int pageID) {
        long hibernateID = -1;

        // first look in the id mapping cache
        if (idMapPages.containsKey(pageID)) {
            return idMapPages.get(pageID);
        }

        // The id was not found in the id mapping cache.
        // It may not be in the cahe or may not exist at all.
        Session session = this.__getHibernateSession();
        session.beginTransaction();
        Object retObjectPage = session.createQuery(
                "select page.id from Page as page where page.pageId = ?")
                .setInteger(0, pageID)
                .uniqueResult();
        session.getTransaction().commit();
        if (retObjectPage != null) {
            hibernateID = (Long) retObjectPage;
            // add it to the cache
            idMapPages.put(pageID, hibernateID);
            return hibernateID;
        }

        return hibernateID;
    }

    /**
     * Get the hibernate ID to a given pageID of a category.
     * We need different methods for pages and categories here, as a page and a category can have the same ID.
     *
     * @param pageID A pageID that should be mapped to the corresponding hibernate ID.
     * @return The hibernateID of the page with pageID or -1, if the pageID is not valid
     */
    protected long __getCategoryHibernateId(int pageID) {
        long hibernateID = -1;

        // first look in the id mapping cache
        if (idMapCategories.containsKey(pageID)) {
            return idMapCategories.get(pageID);
        }

        // The id was not found in the id mapping cache.
        // It may not be in the cahe or may not exist at all.
        Session session = this.__getHibernateSession();
        session.beginTransaction();
        Object retObjectPage = session.createQuery(
                "select cat.id from Category as cat where cat.pageId = ?")
                .setInteger(0, pageID)
                .uniqueResult();
        session.getTransaction().commit();
        if (retObjectPage != null) {
            hibernateID = (Long) retObjectPage;
            // add it to the cache
            idMapCategories.put(pageID, hibernateID);
        }

        return hibernateID;
    }

    /**
     * Returns a MetaData object containing all meta data about this instance of Wikipedia.
     * @return A MetaData object containing all meta data about this instance of Wikipedia.
     */
    public MetaData getMetaData() {
        return this.metaData;
    }

    /**
     * Returns the DatabaseConfiguration object that was used to create the Wikipedia object.
     * @return The DatabaseConfiguration object that was used to create the Wikipedia object.
     */
    public DatabaseConfiguration getDatabaseConfiguration() {
        return this.dbConfig;
    }

    /**
     * Shortcut for getting a hibernate session.
     * @return
     */
    protected Session __getHibernateSession() {
        return WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
    }

    protected MediaWikiParser getParser() {
        return parser;
    }

    /**
     * The ID consists of the host, the database, and the language.
     * This should be unique in most cases.
     * @return Returns a unique ID for this Wikipedia object.
     */
    public String getWikipediaId() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getDatabaseConfiguration().getHost());
        sb.append("_");
        sb.append(this.getDatabaseConfiguration().getDatabase());
        sb.append("_");
        sb.append(this.getDatabaseConfiguration().getLanguage());
        return sb.toString();
    }

//// Fast methods for CM. Methods consume a _lot_ of memory. I do not know why.
//    public Map<String,Integer> getTitleIdMap() {
//        Map<String,Integer> titleIdMap = new HashMap<String,Integer>();
//
//        Session session = this.__getHibernateSession();
//        session.beginTransaction();
//        Iterator results = session.createQuery(
//                "select pml.name, pml.pageID from PageMapLine as pml")
//                .list()
//                .iterator();
//        while (results.hasNext()) {
//            Object[] row = (Object[]) results.next();
//            titleIdMap.put((String) row[0], (Integer) row[1]);
//        }
//        return titleIdMap;
//    }
//
//    public Map<Integer,String> getIdTitleMap() {
//        Map<Integer,String> idTitleMap = new HashMap<Integer,String>();
//
//        Session session = this.__getHibernateSession();
//        session.beginTransaction();
//        Iterator results = session.createQuery(
//                "select pml.pageID, pml.name from PageMapLine as pml")
//                .list()
//                .iterator();
//        while (results.hasNext()) {
//            Object[] row = (Object[]) results.next();
//            idTitleMap.put((Integer) row[0], (String) row[1]);
//        }
//        return idTitleMap;
//
//    }

    //    public void close() {
//        this.dbConfig = null;
//        this.idMap.clear();
//        this.language = null;
//        this.metaData = null;
//    }


//    /**
//     * @return Returns the page idMap.
//     */
//    public Map<Integer, Long> getIdMapPages() {
//        return idMapPages;
//    }
//
//    /**
//     * @return Returns the category idMap.
//     */
//    public Map<Integer, Long> getIdMapCategories() {
//        return idMapPages;
//    }

//    /**
//     * Check, whether the Wikiapi tables have the correct encoding.
//     * The test case in this method are language specific!
//     * @return True if the encoding is correct, false otherwise
//     */
//    private boolean checkEncoding() {
//        String catStr = "Begriffskl\u00e4rung";
//        Category cat = this.getCategory(catStr);
//        if (cat == null) {
//            return false;
//        }
//        else {
//            return true;
//        }
//    }

//// this is no longer used, as users can test for redirects by asking page.isRedirect()
//// we should not inflate the API with this method
//  /**
//  * Gets the page for a given name.
//  * It only finds the page with *exactly* the given name.
//  * @param pName The name of the page.
//  * @param searchRedirects If true, redirects are searched and the corresponding page is retrieved. (Note: redirects and pages are handled transparently, thus you probably want to include redirects in your search.)
//  * @return The page object for a given name.
//  * @throws WikiPageNotFoundException If no page (or redirect - depending on the flag) with this name exists.
//  */
// public Page getPage(String pName, Boolean searchRedirects) throws WikiPageNotFoundException {
//     Page page = new Page(this, pName, searchRedirects);
//     return page;
// }

//// this was only used for the relatedness cache. I replaced the call with getPage(title).getPageId().
//// If it is too slow, I can tweak it anyway.
//  /**
//  * Convenience method that returns the pageID for the given title or -1 if there is no page with this title.
//  * @param title A page name.
//  * @return The pageID for the given token or -1 if there is no page with this title.
//  */
// public int getPageIdByTitle(String title) {
//     Page page;
//     try {
//         page = getPage(title, true);
//     } catch (WikiPageNotFoundException e) {
//         return -1;
//     }
//     int pageID = page.getPageId();
//     return pageID;
// }


//// No longer used.
//    /**
//     * Convenience method that return the pageID for the given title or -1 if there is no category with this title.
//     * @param title A category name.
//     * @return The pageID for the given token or -1 if there is no category with this title.
//     * @throws WikiApiException
//     */
//    public int getCategoryIdByTitle(String title) throws WikiApiException {
//        Category cat = getCategory(title);
//        // if no category with this title exists, return -1
//        if (cat == null) {
//            return -1;
//        }
//        int catID = cat.getPageId();
//        return catID;
//    }


////number of pages was moved to the MetaData table
//  //
//      private void setNumberOfPages() {
//          Session session = WikiHibernateUtil.getSessionFactory().getCurrentSession();
//          session.beginTransaction();
//          Number numberOfPages_number = (Number) session.createQuery(
//                  "select count(*) from Page as page")
//                  .uniqueResult();
//
//          numberOfPages = numberOfPages_number.intValue();
//          session.getTransaction().commit();
//      }

//// I have moved that to a method returning only an iterable that fetches the objects at run-time. The whole list simply does not fit in memory.
//  /**
//  * Get a set with all pageIDs of article pages (articles = pages MINUS disambiguation pages MINUS redirects).
//  * ATTENTION: This is a run-time optimized function that exposes implementation details. It may change in subsequent releases without notice.
//  * @return A set with all article pageIds.  Returning all page objects would be much too expensive.
//  */
// public Set<Integer> getArticles() {
//     Session session = WikiHibernateUtil.getSessionFactory(this.language).getCurrentSession();
//     session.beginTransaction();
//
//     int notDisambiguation = 0;
//
//     List<Integer> idList = session.createQuery(
//     "select p.pageId from Page as p where p.isDisambiguation = ?")
//     .setInteger(0, notDisambiguation)
//     .list();
//
//     Set<Integer> pageIdList = new HashSet<Integer>(idList);
//
//     session.getTransaction().commit();
//
//     return pageIdList;
// }

//// This functionality is covered by the query interface.
//    /**
//     * Gets the categories that match the regular expression string.
//     * Only MySQL style regular Expressions are allowed, that means:
//     *   % for any number of arbitrary characters
//     *   _ for a single arbitrary character
//     * @param pRegEx The regular expression.
//     * @return A set with the category objects that match the regular expression.
//     * @throws WikiApiException
//     */
//    public Set<Category> getCategoriesByPattern(String pRegEx) throws WikiApiException {
//        Title title = new Title(pRegEx);
//        String regEx = title.getWikiStyleTitle();
//        Set<Category> pageSet = new HashSet<Category>();
//        Session session = WikiHibernateUtil.getSessionFactory(this.language).getCurrentSession();
//        session.beginTransaction();
//        Iterator results = session.createSQLQuery(
//                "select cat.id from Category as cat where cat.name like ? COLLATE utf8_bin")
//                .setString(0, regEx)
//                .list()
//                .iterator();
//        session.getTransaction().commit();
//        while (results.hasNext()) {
//            Long id = (Long) results.next();
//            try {
//                Category cat = new Category(this, id);
//                pageSet.add(cat);
//            } catch (WikiPageNotFoundException e) {}
//        }
//
//        return pageSet;
//    }

//// Removed, because it exposes implementation details.
//// If we need a convenience method like that it should be at least protected.
//    /**
//     * Get the page IDs of a set of categories.
//     * @param categories The set of categories.
//     * @return A set of corresponding category pageIDs.
//     */
//    public Set<Integer> getCategoryIDs(Set<Category> categories) {
//        Set<Integer> catPageIDs = new HashSet<Integer>();
//        for (Category cat : categories) {
//            catPageIDs.add(cat.getPageId());
//        }
//        return catPageIDs;
//    }

////Removed, because it exposes implementation details.
////If we need a convenience method like that it should be at least protected.
//    /**
//     * Get the page IDs of a set of article pages
//     * @param pages The set of pages.
//     * @return A set of corresponding page IDs.
//     */
//    protected Set<Integer> getPageIDs(Set<Page> pages) {
//        Set<Integer> pageIDs = new HashSet<Integer>();
//        for (Page page : pages) {
//            pageIDs.add(page.getPageId());
//        }
//        return pageIDs;
//    }

//// this should be accessed via MetaData
//  /** Gets the root of the category tree.
//  * @return The category object or null if no category with this name exists.
//  * @throws WikiApiException
//  */
// public Category getRootCategory() throws WikiApiException {
//     return getCategory(this.getMainCategoryName());
// }

////This functionality is covered by the query interface.
//    /**
//     * Gets the pages that match the regular expression string.
//     * Only MySQL style regular Expressions are allowed, that means:
//     *   % for any number of arbitrary characters
//     *   _ for a single arbitrary character
//     * @param pRegEx The regular expression.
//     * @return A set with the page objects that match the regular expression.
//     * @throws WikiApiException
//     */
//    public Set<Page> getPagesByPattern(String pRegEx) throws WikiApiException {
//        Title title = new Title(pRegEx);
//        pRegEx = title.getWikiStyleTitle();
//        Set<Page> pageSet = new HashSet<Page>();
//        Session session = WikiHibernateUtil.getSessionFactory(this.language).getCurrentSession();
//        session.beginTransaction();
//
//        Iterator results = session.createSQLQuery(
//        "select pml.pageID from PageMapLine as pml where pml.name like ? COLLATE utf8_bin")
//        .setString(0, pRegEx)
//        .list()
//        .iterator();
//
//        session.getTransaction().commit();
//
//        while (results.hasNext()) {
//            int pageID = (Integer) results.next();
//            Page page = null;
//            try {
//                page = this.__getPage(pageID);
//            } catch (WikiPageNotFoundException e) {
//                logger.error("Page with pageID " + pageID + " could not be found. Fatal error. Terminating.");
//                e.printStackTrace();
//                System.exit(1);
//            }
//            // as redirects point to a regular page, we might try to add a page multiple times here
//            // The set ensures that it is only present once afterwards. Good sets :)
//            pageSet.add(page);
//        }
//
//        return pageSet;
//    }

}

class ValueComparator implements Comparator<Map.Entry<Integer,Double>> {

    @Override
	public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {

        double c1 = e1.getValue();
        double c2 = e2.getValue();

        if (c1 < c2) {
            return 1;
        }
        else if (c1 > c2) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
