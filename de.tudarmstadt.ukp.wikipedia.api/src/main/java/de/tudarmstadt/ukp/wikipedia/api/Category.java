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

import java.math.BigInteger;
import java.util.*;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.CategoryDAO;

public class Category implements WikiConstants {

    private CategoryDAO catDAO;
    private de.tudarmstadt.ukp.wikipedia.api.hibernate.Category hibernateCategory;
    private Wikipedia wiki;


    /**
     * Creates a category object.
     * @param wiki The wikipedia object.
     * @param id The hibernate id of the category.
     * @throws WikiPageNotFoundException If the category does not exist.
     */
    protected Category (Wikipedia wiki, long id) throws WikiPageNotFoundException {
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        createCategory(id);
    };

    /**
     * Creates a category object.
     * @param wiki The wikipedia object.
     * @param pageID The pageID of the category.
     * @throws WikiPageNotFoundException If the category does not exist.
     */
    protected Category (Wikipedia wiki, int pageID) throws WikiPageNotFoundException {
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        createCategory(pageID);
    };

    /**
     * Creates a category object.
     * @param wiki The wikipedia object.
     * @param pName The name of the category.
     * @throws WikiPageNotFoundException If the category does not exist.
     */
    public Category(Wikipedia wiki, String pName) throws WikiApiException {
        if (pName == null || pName.length() == 0) {
            throw new WikiPageNotFoundException();
        }
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        Title catTitle = new Title(pName);
        createCategory(catTitle);
    }

    /**
     * @see de.tudarmstadt.ukp.wikipedia.api.Category#Category(long)
     */
    private void createCategory(long id) throws WikiPageNotFoundException {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        hibernateCategory = catDAO.findById(id);
        session.getTransaction().commit();

        if (hibernateCategory == null) {
            throw new WikiPageNotFoundException("No category with id " + id + " was found.");
        }
    }

    /**
     * @see de.tudarmstadt.ukp.wikipedia.api.Category#Category(Wikipedia, int)
     */
    private void createCategory(int pageID) throws WikiPageNotFoundException {
        createCategory( wiki.__getCategoryHibernateId(pageID));
    }

    /**
     * @see de.tudarmstadt.ukp.wikipedia.api.Category#Category(Wikipedia, String)
     */
    private void createCategory(Title title) throws WikiPageNotFoundException {
        String name = title.getWikiStyleTitle();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();

        Object returnValue;
        returnValue = session.createSQLQuery(
                "select cat.pageId from Category as cat where cat.name = ? COLLATE utf8_bin")
                .setString(0, name)
                .uniqueResult();
        session.getTransaction().commit();

        // if there is no category with this name, the hibernateCategory is null
        if (returnValue == null) {
            hibernateCategory = null;
            throw new WikiPageNotFoundException("No category with name " + name + " was found.");
        }
        else {
            // now cast it into an integer
            int pageID = (Integer) returnValue;
            createCategory( pageID);
        }
    }

    /**
     * This returns the internal id. Do not confuse this with the pageId.
     * @return Returns the internal id.
     */
    public long __getId() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        long id = hibernateCategory.getId();
        session.getTransaction().commit();
        return id;
    }

    /**
     * Returns a unique page id.
     * @return A unique page id.
     */
    public int getPageId() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        int pageID = hibernateCategory.getPageId();
        session.getTransaction().commit();
        return pageID;
    }

    /**
     * Returns a set containing parents (supercategories) of this category.
     * @return A set containing parents (supercategories) of this category.
     */
    public Set<Category> getParents() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getInLinks());
        session.getTransaction().commit();

        Set<Category> categories = new HashSet<Category>();
        for (int pageID : tmpSet) {
            categories.add(this.wiki.getCategory(pageID));
        }
        return categories;
    }

    /**
     * This is a more efficient shortcut for writing "getParents().size()", as that would require to load all the parents first.
     * @return The number of parents of this category.
     */
    public int getNumberOfParents() {
        BigInteger nrOfInlinks = new BigInteger("0");

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createSQLQuery("select count(inLinks) from category_inlinks where id = ?")
            .setLong(0, id)
            .uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfInlinks = (BigInteger) returnValue;
        }
        return nrOfInlinks.intValue();
    }

    /**
     * @return A set containing the IDs of the parents of this category.
     */
    public Set<Integer> getParentIDs() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getInLinks());
        session.getTransaction().commit();
        return tmpSet;
    }

    /**
     * Returns a set containing the children (subcategories) of this category.
     * @return A set containing the children (subcategories) of this category.
     */
    public Set<Category> getChildren() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getOutLinks());
        session.getTransaction().commit();

        Set<Category> categories = new HashSet<Category>();
        for (int pageID : tmpSet) {
            categories.add(this.wiki.getCategory(pageID));
        }
        return categories;
    }

    /**
     * This is a more efficient shortcut for writing "getChildren().size()", as that would require to load all the children first.
     * @return The number of children of this category.
     */
    public int getNumberOfChildren() {
        BigInteger nrOfOutlinks = new BigInteger("0");

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createSQLQuery("select count(outLinks) from category_outlinks where id = ?")
            .setLong(0, id)
            .uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfOutlinks = (BigInteger) returnValue;
        }
        return nrOfOutlinks.intValue();
    }

    /**
     * @return A set containing the IDs of the children of this category.
     */
    public Set<Integer> getChildrenIDs() {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getOutLinks());
        session.getTransaction().commit();
        return tmpSet;
    }

    /**
     * Returns the title of the category.
     * @return The title of the category.
     * @throws WikiTitleParsingException
     */
    public Title getTitle() throws WikiTitleParsingException  {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        String name = hibernateCategory.getName();
        session.getTransaction().commit();
        Title title = new Title(name);
        return title;
    }


    /**
     * Returns the set of pages that are categorized under this category.
     * @return The set of pages that are categorized under this category.
     * @throws WikiApiException
     * @deprecated Use {@link getArticles()} instead.
     */
    @Deprecated
	public Set<Page> getPages() throws WikiApiException {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getPages());
        session.getTransaction().commit();

        Set<Page> pages = new HashSet<Page>();
        for (int pageID : tmpSet) {
            pages.add(this.wiki.getPage(pageID));
        }
        return pages;
    }

    /**
     * Returns the set of articles that are categorized under this category.
     * @return The set of articles that are categorized under this category.
     * @throws WikiApiException
     */
    public Set<Page> getArticles() throws WikiApiException {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getPages());
        session.getTransaction().commit();

        Set<Page> pages = new HashSet<Page>();
        for (int pageID : tmpSet) {
            pages.add(this.wiki.getPage(pageID));
        }
        return pages;
    }

    /**
     * Returns the set of article ids that are categorized under this category.
     * @return The set of article ids that are categorized under this category.
     * @throws WikiApiException
     */
    public Set<Integer> getArticleIds() throws WikiApiException {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getPages());
        session.getTransaction().commit();

        return tmpSet;
    }

    /**
     * This is a more efficient shortcut for writing "getPages().size()", as that would require to load all the pages first.
     * @return The number of pages.
     */
    public int getNumberOfPages() throws WikiApiException {
        BigInteger nrOfPages = new BigInteger("0");

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        Object returnValue = session.createSQLQuery("select count(pages) from category_pages where id = ?")
            .setLong(0, id)
            .uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfPages = (BigInteger) returnValue;
        }
        return nrOfPages.intValue();
    }

    /**
     * This method exposes implementation details and should not be made public.
     * It is used for performance tuning.
     * @return The set of pages that are categorized under this category.
     * @throws WikiPageNotFoundException
     */
    protected Set<Integer> __getPages() throws WikiPageNotFoundException {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<Integer>(hibernateCategory.getPages());
        session.getTransaction().commit();
        return tmpSet;
    }

//// Selectivity has not proven to be of any use. Thus, it is removed in the release.
////
//    /**
//     * Categories having lots of pages (e.g. more than 80,000 pages are tagged with "Mann"/"man")
//     * are not as informative as are more specific categories.
//     * E.g. "See in Weißrussland" / "Lake in Belarus" has only two members in the wikipedia version used while writing this. Two pages with this categories are apparently *very* similar.
//     * The fewer member pages a category has, the higher is its selectivity.
//     *
//     * Formula: y = 1 - log(pages(cat)+1)/log(pages(wiki)+1)
//     *
//     * The definition is different to selectivity in the DB context where it the fraction (distinct values) / (number of rows) in a DB column.
//     * Is there a better term?
//     * @return
//     */
//    public double getSelectivityLog(Wikipedia wiki) {
//        double wikiPages = wiki.getNumberOfPages();
//        double catPages = getPages().size();
//        double selectivity = 1 - (Math.log(catPages +1)/Math.log(wikiPages +1));
//        return selectivity;
//    }

//// linear selectivity - using log function should be better
//    /**
//     * Categories having lots of pages (e.g. more than 80,000 pages are tagged with "Mann"/"man")
//     * are not as informative as are more specific categories.
//     * E.g. "See in Weißrussland" / "Lake in Belarus" has only two members in the wikipedia version used while writing this. Two pages with this categories are apparently *very* similar.
//     * The fewer member pages a category has, the higher is its selectivity.
//     *
//     * As a category with zero pages has an unknown selectivity, we define that the selectivity for 1/n = 1 and for n = 0, where n is the number of pages in the wikipedia.
//     * The function is linear decreasing between this points.
//     * Formula: y = (x - n) / (1/n - n)
//     *
//     * The definition is different to selectivity in the DB context where it the fraction (distinct values) / (number of rows) in a DB column.
//     * Is there a better term?
//     * @return
//     */
//    public double getSelectivityLinear(Wikipedia wiki) {
//        double n = wiki.getNumberOfPages();
//        double x = getPages().size();
//        return ( (x-n) / ((1/n) - n) );
//    }


    /**
     * Returns *all* recursively collected descendants (=subcategories) of this category.
     * @return An iterable of all descendants (=subcategories) of this category.
     */
    public Iterable <Category> getDescendants() {
        return new CategoryDescendantsIterable(wiki, this);

//        Set<Category> subCategories = new HashSet<Category>();
//
//        List<Category> queue = new LinkedList<Category>();
//
//        // initialize queue
//        queue.addAll(this.getChildren());
//
//        while (!queue.isEmpty()) {
//            // remove first element from queue
//            Category currentCat = queue.get(0);
//            queue.remove(0);
//
//            if (!subCategories.contains(currentCat)) {
//                subCategories.add(currentCat);
//                queue.addAll(currentCat.getChildren());
//            }
//        }
//
//        return subCategories;
    }

    /**
     * Returns *all* recursively collected descendants (=subcategories) of this category.
     * @return An iterable of all descendants (=subcategories) of this category.
     */
    protected Iterable <Category> getDescendants(int bufferSize) {
        return new CategoryDescendantsIterable(wiki, bufferSize, this);
    }

    /**
     * Returns the siblings of this category.
     * @return Returns the siblings of this category or null, if there are none.
     */
    public Set<Category> getSiblings() {
        Set<Category> siblings = new HashSet<Category>();

        // add siblings
        for (Category parent : this.getParents()) {
            siblings.addAll(parent.getChildren());
        }

        // remove this category from list
        siblings.remove(this);

        return siblings;
    }

    /**
     * @return A string with infos about this category object.
     * @throws WikiApiException
     */
    protected String getCategoryInfo() throws WikiApiException {
        StringBuffer sb = new StringBuffer(1000);

        sb.append("ID             : " + __getId() + LF);
        sb.append("PageID         : " + getPageId() + LF);
        sb.append("Name           : " + getTitle() + LF);
        sb.append("In-Links" + LF);
        for (Category parent : getParents()) {
            sb.append("  " + parent.getTitle() + LF);
        }
        sb.append("Out-Links" + LF);
        for (Category child : getChildren()) {
            sb.append("  " + child.getTitle() + LF);
        }
        sb.append("Pages" + LF);
        for (Page page : getPages()) {
            sb.append("  " + page.getTitle() + LF);
        }

        return sb.toString();
    }

}
