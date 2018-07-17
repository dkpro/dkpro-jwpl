/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.WikiHibernateUtil;
import de.tudarmstadt.ukp.wikipedia.util.distance.LevenshteinStringDistance;
import org.sweble.wikitext.engine.config.WikiConfig;


/**
 * Provides access to Wikipedia articles and categories.
 *
 */
// TODO better JavaDocs!
public class Wikipedia implements WikiConstants {

    // Note well: The whitespace at the beginning of this constant is here on purpose. Do NOT remove it!
    static final String SQL_COLLATION = " COLLATE utf8_bin";

	private final Log logger = LogFactory.getLog(getClass());
    private final Language language;
    private final DatabaseConfiguration dbConfig;

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

    public WikiConfig wikiConfig;

    /**
     * Creates a new {@link Wikipedia} object accessing the database indicated by the dbConfig parameter.
     * @param dbConfig A {@link DatabaseConfiguration} object telling the {@link Wikipedia} object where the data is stored and how it can be accessed.
     * @throws WikiInitializationException Thrown if errors occurred while bootstrapping the {@link Wikipedia} instance.
     */
    public Wikipedia(DatabaseConfiguration dbConfig) throws WikiInitializationException {

        logger.debug("Creating Wikipedia object.");

        this.language = dbConfig.getLanguage();
        this.dbConfig = dbConfig;

        this.idMapPages      = new HashMap<Integer,Long>();
        this.idMapCategories = new HashMap<Integer,Long>();

        this.metaData = new MetaData(this);
        this.wikiConfig = this.language.getWikiconfig();

        if(dbConfig.supportsCollation()) {
            logger.info("Wikipedia database backend supports character collation features.");
        } else {
            logger.debug("Wikipedia database backend does not support character collation features.");
        }
	}


    /**
     * Gets the page with the given title.
     * If the title is a redirect, the corresponding page is returned.<br>
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
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly parsed.
     */
    public Page getPage(String title) throws WikiApiException  {
    	return new Page(this, title, false);
    }

    /**
     * Gets the page with the exactly the given title.<br>
     *
     * Note that when using this method you are responsible for converting a normal search string into the right wiki-style.<br>
     *
     * If the title is a redirect, the corresponding page is returned.<br>
     *
     * @param exactTitle The exact title of the page.
     * @return The page object for a given title.
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly parsed.
     */
    public Page getPageByExactTitle(String exactTitle) throws WikiApiException  {
        return new Page(this, exactTitle, true);
    }

    /**
     * Get all pages which match all lowercase/uppercase version of the given title.<br>
     * If the title is a redirect, the corresponding page is returned.<br>
     * Spaces in the title are converted to underscores, as this is a convention for Wikipedia article titles.
     *
     * @param title The title of the page.
     * @return A set of page objects matching this title.
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly parsed.
     */
    public Set<Page> getPages(String title) throws WikiApiException  {
        Set<Integer> ids = new HashSet<Integer>(getPageIdsCaseInsensitive(title));

        Set<Page> pages = new HashSet<Page>();
        for (Integer id : ids) {
            pages.add(new Page(this, id));
        }
        return pages;
    }

    /**
     * Gets the page for a given pageId.
     *
     * @param pageId The id of the page.
     * @return The page object for a given pageId.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Page getPage(int pageId) throws WikiApiException {
        return new Page(this, pageId);
    }

    /**
     * Gets the title for a given pageId.
     *
     * @param pageId The id of the page.
     * @return The title for the given pageId.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Title getTitle(int pageId) throws WikiApiException {
    	Session session = this.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createNativeQuery(
            "select p.name from PageMapLine as p where p.pageId= :pId").setParameter("pId", pageId, IntegerType.INSTANCE).uniqueResult();
        session.getTransaction().commit();

        String title = (String)returnValue;
        if(title==null){
        	throw new WikiPageNotFoundException();
        }
        return new Title(title);
    }

    /**
     * Gets the page ids for a given title.<br>
     *
     *
     * @param title The title of the page.
     * @return The id for the page with the given title.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<Integer> getPageIds(String title) throws WikiApiException {
    	Session session = this.__getHibernateSession();
        session.beginTransaction();
        Iterator results = session.createQuery(
        "select p.pageID from PageMapLine as p where p.name = :pName").setParameter("pName", title, StringType.INSTANCE).list().iterator();

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
     * Gets the page ids for a given title with case insensitive matching.<br>
     *
     *
     * @param title The title of the page.
     * @return The ids of the pages with the given title.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<Integer> getPageIdsCaseInsensitive(String title) throws WikiApiException {
        title = title.toLowerCase();
        title = title.replaceAll(" ", "_");

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        Iterator results = session.createQuery(
        "select p.pageID from PageMapLine as p where lower(p.name) = :pName").setParameter("pName", title, StringType.INSTANCE).list().iterator();

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
	 * @throws WikiApiException Thrown if errors occurred.
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
     * @param articlePageId The id of the page.
     * @return The page object for a given pageId.
     * @throws WikiApiException Thrown if errors occurred.
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
     * the page with the given title String. <br>
     * The page retrieval works as defined in {@link #getPage(int)}. <br>
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
     * the page with the given title String. <br>
     * The page retrieval works as defined in {@link #getPage(String title)}.<br>
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
     * <br>
     * The provided page Object must not be a discussion page itself! If it is
     * a discussion page, is returned unchanged.
     *
     * @param articlePage the article page for which a discussion archives should be retrieved
     * @return An iterable with the discussion archive page objects for the given article page object
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly parsed.
     */
    public Iterable<Page> getDiscussionArchives(Page articlePage) throws WikiApiException{
        String articleTitle = articlePage.getTitle().getWikiStyleTitle();
    	if(!articleTitle.startsWith(WikiConstants.DISCUSSION_PREFIX)){
    		articleTitle=WikiConstants.DISCUSSION_PREFIX+articleTitle;
    	}

    	Session session = this.__getHibernateSession();
        session.beginTransaction();

        List<Page> discussionArchives = new LinkedList<Page>();

        Query query = session.createQuery("SELECT pageID FROM PageMapLine where name like :name");
        query.setParameter("name", articleTitle+"/%", StringType.INSTANCE);
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
     * @throws WikiApiException Thrown if errors occurred.
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
        Iterator results = session.createQuery("select pml.pageID, pml.name from PageMapLine as pml").list().iterator();
        while (results.hasNext()) {
            Object[] row = (Object[]) results.next();
            int pageID = (Integer) row[0];
            String pageName = (String) row[1];


            // this returns a similarity - if we want to use it, we have to change the semantics the ordering of the results
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
     * If the {@link Category} title start with a lowercase letter it converts it to an uppercase letter, as each Wikipedia category title starts with an uppercase letter.
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
     * @param pageId The id of the {@link Category}.
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
     * This returns an iterable over all {@link Category categories}, as returning all category objects would be much too expensive.
     * @return An iterable over all categories.
     */
    public Iterable<Category> getCategories() {
        return new CategoryIterable(this);
    }


    /**
     * Gets the {@link Category categories} for a given {@link Page} identified by its {@code pageTitle}.
     * @param pageTitle The title of a {@link Page}, not a category.
     * @return The category objects which are associated with the given {@code pageTitle}.
     * @throws WikiPageNotFoundException Thrown if no {@link Page} exists for the given {@code pageTitle}.
     */
    public Set<Category> getCategories(String pageTitle) throws WikiPageNotFoundException
    {
        if (pageTitle == null || pageTitle.length() == 0) {
            throw new WikiPageNotFoundException();
        }

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List<Integer> categoryHibernateIds = session.createQuery(
                "select c from Page p left join p.categories c where p.name = :pageTitle", Integer.class)
                .setParameter("pageTitle", pageTitle).list();
        session.getTransaction().commit();

        Set<Category> categorySet = new HashSet<Category>(categoryHibernateIds.size());
        for (int hibernateId : categoryHibernateIds) {
            try {
                categorySet.add(new Category(this, hibernateId));
            } catch (WikiPageNotFoundException e) {
                logger.warn("Could not load Category by it's HibernateId = '"+hibernateId+"'");
            }
        }
        return categorySet;
    }

    /**
     * Get all wikipedia {@link Category categories}.
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
                "select cat.pageId from Category as cat", Integer.class).list();
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
     * Get a set with all {@code pageIDs}. Returning all page objects is much too expensive.
     * Does not include redirects, as they are only pointers to real pages.
     *
     * As ids can be useful for several application (e.g. in combination with
     * the RevisionMachine, they have been made publicly available via
     * {@link #getPageIds()}.
     *
     * @return A set with all {@code pageIDs}. Returning all pages is much to expensive.
     */
    protected Set<Integer> __getPages() {
        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List<Integer> idList = session.createQuery(
            "select page.pageId from Page as page", Integer.class).list();
        Set<Integer> pageSet = new HashSet<Integer>(idList);
        session.getTransaction().commit();

        return pageSet;
    }

    /**
     * @return an iterable over all {@code pageIDs} (without redirects)
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
     * @throws WikiApiException Thrown if errors occurred.
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
     * @return The {@link Language} of this Wikipedia.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Tests, whether a page or redirect with the given title exists.
     * Trying to retrieve a page that does not exist in Wikipedia throws an exception.
     * You may catch the exception or use this test, depending on your task.
     * @param title The title of the page.
     * @return {@code True}, if a page or redirect with that title exits, {@code false} otherwise.
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
        String query = "select p.id from PageMapLine as p where p.name = :pName";
        if(dbConfig.supportsCollation()) {
            query += SQL_COLLATION;
        }
        Object returnValue = session.createNativeQuery(query)
            .setParameter("pName", encodedTitle, StringType.INSTANCE)
            .uniqueResult();
        session.getTransaction().commit();

        return returnValue != null;
    }

    /**
     * Tests, whether a page with the given pageID exists.
     * Trying to retrieve a pageID that does not exist in Wikipedia throws an exception.
     *
     * @param pageID A pageID.
     * @return {@code True}, if a page with that pageID exits, {@code false} otherwise.
     */
    public boolean existsPage(int pageID) {

        // This is a hack to provide a much quicker way to test whether a page exists.
        // Encoding the title in this way surpasses the normal way of creating a title first.
        // Anyway, I do not like this hack :-|

        if (pageID < 0) {
            return false;
        }

        Session session = this.__getHibernateSession();
        session.beginTransaction();
        List returnList = session.createNativeQuery(
            "select p.id from PageMapLine as p where p.pageID = :pageId")
            .setParameter("pageId", pageID, IntegerType.INSTANCE)
            .list();
        session.getTransaction().commit();

        return returnList.size() != 0;
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
                "select page.id from Page as page where page.pageId = :pageId")
                .setParameter("pageId", pageID, IntegerType.INSTANCE)
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
                "select cat.id from Category as cat where cat.pageId = :pageId")
                .setParameter("pageId", pageID, IntegerType.INSTANCE)
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
     * @return A {@link MetaData} object containing all meta data about this instance of Wikipedia.
     */
    public MetaData getMetaData() {
        return this.metaData;
    }

    /**
     * @return The {@link DatabaseConfiguration} object that was used to create the Wikipedia object.
     */
    public DatabaseConfiguration getDatabaseConfiguration() {
        return this.dbConfig;
    }

    /**
     * @return Shortcut for getting a hibernate session.
     */
    protected Session __getHibernateSession() {
        return WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
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
