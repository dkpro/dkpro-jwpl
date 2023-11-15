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

import java.util.HashSet;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.hibernate.CategoryDAO;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

/**
 * Represents a category as conceptually defined by Wikipedia.
 * Each category can group several {@link Page pages}.
 * <p>
 * It can be subdivided further, that is, every category can have descendents or siblings.
 * Structurally, Wikipedia defined categories to be represented as a graph. Consequently,
 * a category can have multiple parent categories.
 *
 * @see Page
 */
public class Category
    implements WikiConstants
{

    private final CategoryDAO catDAO;
    private org.dkpro.jwpl.api.hibernate.Category hibernateCategory;
    private final Wikipedia wiki;

    /**
     * Creates a category object.
     *
     * @param wiki
     *            The wikipedia object.
     * @param id
     *            The hibernate id of the category.
     * @throws WikiPageNotFoundException
     *             If the category does not exist.
     */
    protected Category(Wikipedia wiki, long id) throws WikiPageNotFoundException
    {
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        createCategory(id);
    }

    /**
     * Creates a category object.
     *
     * @param wiki
     *            The wikipedia object.
     * @param pageID
     *            The pageID of the category.
     * @throws WikiPageNotFoundException
     *             If the category does not exist.
     */
    protected Category(Wikipedia wiki, int pageID) throws WikiPageNotFoundException
    {
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        createCategory(pageID);
    }

    /**
     * Creates a category object.
     *
     * @param wiki
     *            The wikipedia object.
     * @param pName
     *            The name of the category.
     * @throws WikiPageNotFoundException
     *             If the category does not exist.
     */
    public Category(Wikipedia wiki, String pName) throws WikiApiException
    {
        if (pName == null || pName.isEmpty()) {
            throw new WikiPageNotFoundException();
        }
        this.wiki = wiki;
        catDAO = new CategoryDAO(wiki);
        Title catTitle = new Title(pName);
        createCategory(catTitle);
    }

    /**
     * @see Category#Category(Wikipedia, long)
     */
    private void createCategory(long id) throws WikiPageNotFoundException
    {
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();
            hibernateCategory = catDAO.findById(id);
            session.getTransaction().commit();

            if (hibernateCategory == null) {
                throw new WikiPageNotFoundException("No category with id " + id + " was found.");
            }
        }
    }

    /**
     * @see Category#Category(Wikipedia, int)
     */
    private void createCategory(int pageID) throws WikiPageNotFoundException
    {
        createCategory(wiki.__getCategoryHibernateId(pageID));
    }

    /**
     * @see Category#Category(Wikipedia, String)
     */
    private void createCategory(Title title) throws WikiPageNotFoundException
    {
        String name = title.getWikiStyleTitle();
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();

            Integer returnValue;

            String query = "select cat.pageId from Category as cat where cat.name = :name";
            if (wiki.getDatabaseConfiguration().supportsCollation()) {
                query += Wikipedia.SQL_COLLATION;
            }
            returnValue = session.createNativeQuery(query, Integer.class)
                    .setParameter("name", name, StandardBasicTypes.STRING).uniqueResult();
            session.getTransaction().commit();

            // if there is no category with this name, the hibernateCategory is null
            if (returnValue == null) {
                hibernateCategory = null;
                throw new WikiPageNotFoundException("No category with name " + name + " was found.");
            }
            else {
                int pageID = returnValue;
                createCategory(pageID);
            }
        }
    }

    /**
     * This returns the internal id. Do not confuse this with the pageId.
     *
     * @return Returns the internal id.
     */
    /*
     * Note well: Access is limited to package-private here intentionally, as the database ID is
     * considered framework-internal use.
     */
    long __getId()
    {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        long id = hibernateCategory.getId();
        session.getTransaction().commit();
        return id;
    }

    /**
     * @return A unique page id.
     */
    public int getPageId()
    {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        int pageID = hibernateCategory.getPageId();
        session.getTransaction().commit();
        return pageID;
    }

    /**
     * @return A set containing parents (super categories) of this category.
     */
    public Set<Category> getParents()
    {
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();
            session.lock(hibernateCategory, LockMode.NONE);
            Set<Integer> tmpSet = new HashSet<>(hibernateCategory.getInLinks());
            session.getTransaction().commit();

            Set<Category> categories = new HashSet<>();
            for (int pageID : tmpSet) {
                categories.add(this.wiki.getCategory(pageID));
            }
            return categories;
        }
    }

    /**
     * This is a more efficient shortcut for writing "getParents().size()", as that would require to
     * load all the parents first.
     *
     * @return The number of parents of this category.
     */
    public int getNumberOfParents()
    {
        int nrOfInlinks = 0;

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        String sql = "select count(inLinks) from category_inlinks where id = :id";
        Long returnValue = session.createNativeQuery(sql, Long.class)
                .setParameter("id", id, StandardBasicTypes.LONG).uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfInlinks = returnValue.intValue();
        }
        return nrOfInlinks;
    }

    /**
     * @return A set containing the IDs of the parents of this category.
     */
    public Set<Integer> getParentIDs()
    {
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();
            session.lock(hibernateCategory, LockMode.NONE);
            Set<Integer> tmpSet = new HashSet<>(hibernateCategory.getInLinks());
            session.getTransaction().commit();
            return tmpSet;
        }
    }

    /**
     * @return A set containing the children (subcategories) of this category.
     */
    public Set<Category> getChildren()
    {
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();
            session.lock(hibernateCategory, LockMode.NONE);
            Set<Integer> tmpSet = new HashSet<>(hibernateCategory.getOutLinks());
            session.getTransaction().commit();

            Set<Category> categories = new HashSet<>();
            for (int pageID : tmpSet) {
                categories.add(this.wiki.getCategory(pageID));
            }
            return categories;
        }
    }

    /**
     * This is a more efficient shortcut for writing "getChildren().size()", as that would require
     * to load all the children first.
     *
     * @return The number of children of this category.
     */
    public int getNumberOfChildren()
    {
        int nrOfOutlinks = 0;

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        String sql = "select count(outLinks) from category_outlinks where id = :id";
        Long returnValue = session.createNativeQuery(sql, Long.class)
                .setParameter("id", id, StandardBasicTypes.LONG).uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfOutlinks = returnValue.intValue();
        }
        return nrOfOutlinks;
    }

    /**
     * @return A set containing the IDs of the children of this category.
     */
    public Set<Integer> getChildrenIDs()
    {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        Set<Integer> tmpSet = new HashSet<>(hibernateCategory.getOutLinks());
        session.getTransaction().commit();
        return tmpSet;
    }

    /**
     * @return The title of the category.
     * @throws WikiTitleParsingException
     *             Thrown if errors occurred.
     */
    public Title getTitle() throws WikiTitleParsingException
    {
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        session.lock(hibernateCategory, LockMode.NONE);
        String name = hibernateCategory.getName();
        session.getTransaction().commit();
        return new Title(name);
    }

    /**
     * @return The set of articles that are categorized under this category.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    public Set<Page> getArticles() throws WikiApiException
    {
        Set<Integer> tmpSet = getArticleIds();
        Set<Page> pages = new HashSet<>();
        for (int pageID : tmpSet) {
            pages.add(this.wiki.getPage(pageID));
        }
        return pages;
    }

    /**
     * @return The set of article ids that are categorized under this category.
     */
    public Set<Integer> getArticleIds()
    {
        try (Session session = this.wiki.__getHibernateSession()) {
            session.beginTransaction();
            session.lock(hibernateCategory, LockMode.NONE);
            Set<Integer> tmpSet = new HashSet<>(hibernateCategory.getPages());
            session.getTransaction().commit();

            return tmpSet;
        }
    }

    /**
     * This is a more efficient shortcut for writing "getPages().size()", as that would require to
     * load all the pages first.
     *
     * @return The number of pages.
     */
    public int getNumberOfPages()
    {
        int nrOfPages = 0;

        long id = this.__getId();
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        String sql = "select count(pages) from category_pages where id = :id";
        Long returnValue = session.createNativeQuery(sql, Long.class)
                .setParameter("id", id, StandardBasicTypes.LONG).uniqueResult();
        session.getTransaction().commit();

        if (returnValue != null) {
            nrOfPages = returnValue.intValue();
        }
        return nrOfPages;
    }

    /**
     * This method exposes implementation details and should not be made public. It is used for
     * performance tuning.
     *
     * @return The set of pages that are categorized under this category.
     */
    /*
     * Note well: Access is limited to package-private here intentionally, as it is API-internal use
     * only.
     */
    Set<Integer> __getPages()
    {
        return getArticleIds();
    }

    /**
     * Returns *all* recursively collected descendants (=subcategories) of this category.
     *
     * @return An iterable of all descendants (=subcategories) of this category.
     */
    public Iterable<Category> getDescendants()
    {
        return new CategoryDescendantsIterable(wiki, this);
    }

    /**
     * Returns *all* recursively collected descendants (=subcategories) of this category.
     *
     * @param bufferSize The size of the page buffer. With {@code bufferSize = 1}, a database connection is needed for
     *                   retrieving a single article. Higher {@code bufferSize} values gives better performance,
     *                   but require more memory. Must not be less or equal to {@code 0}.
     *
     * @return An iterable of all descendants (=subcategories) of this category.
     */
    protected Iterable<Category> getDescendants(int bufferSize)
    {
        return new CategoryDescendantsIterable(wiki, bufferSize, this);
    }

    /**
     * Returns the siblings of this category.
     *
     * @return Returns the siblings of this category or {@code null}, if there are none.
     */
    public Set<Category> getSiblings()
    {
        Set<Category> siblings = new HashSet<>();

        // add siblings
        for (Category parent : this.getParents()) {
            siblings.addAll(parent.getChildren());
        }

        // remove this category from list
        siblings.remove(this);

        return siblings;
    }

    /**
     * @return A string with information about a {@link Category}.
     * @throws WikiApiException
     *             Thrown if errors occurred.
     */
    protected String getCategoryInfo() throws WikiApiException
    {
        StringBuilder sb = new StringBuilder(1000);

        sb.append("ID             : ").append(__getId()).append(LF);
        sb.append("PageID         : ").append(getPageId()).append(LF);
        sb.append("Name           : ").append(getTitle()).append(LF);
        sb.append("In-Links").append(LF);
        for (Category parent : getParents()) {
            sb.append("  ").append(parent.getTitle()).append(LF);
        }
        sb.append("Out-Links").append(LF);
        for (Category child : getChildren()) {
            sb.append("  ").append(child.getTitle()).append(LF);
        }
        sb.append("Pages").append(LF);
        for (Page page : getArticles()) {
            sb.append("  ").append(page.getTitle()).append(LF);
        }
        return sb.toString();
    }

}
