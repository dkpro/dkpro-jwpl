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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.hibernate.CategoryDAO;
import org.hibernate.JDBCException;

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

    /**
     * An immutable snapshot of the plain columns of a category row. Unlike a Hibernate entity it
     * is never associated with a session, so one instance can safely be shared between threads and
     * {@link Category} objects, e.g. via the {@link CategoryCache} of a {@link Wikipedia}.
     *
     * @param id
     *            The internal (hibernate) id of the category.
     * @param pageId
     *            The page id of the category.
     * @param name
     *            The name of the category.
     */
    record Row(long id, int pageId, String name)
    {

        /**
         * @param entity
         *            A loaded category entity, or {@code null}.
         * @return A snapshot of the plain columns of {@code entity}, or {@code null} if
         *         {@code entity} is {@code null}.
         */
        static Row of(org.dkpro.jwpl.api.hibernate.Category entity)
        {
            return entity == null ? null
                    : new Row(entity.getId(), entity.getPageId(), entity.getName());
        }
    }

    /**
     * Upper bound for the number of parent ids bound into a single {@code in (..)} clause of
     * {@link #getSiblings()}, as for the batches of {@link Wikipedia#getTitles(java.util.Collection)}.
     */
    private static final int SIBLING_BATCH_SIZE = 500;

    /**
     * The native query {@link #createCategory(Title)} runs to find a category, taking its name as
     * parameter {@code name}. It yields the category entities found. The comparison is done in the
     * collation of the column, which keeps the index on the column usable, so the caller has to
     * pick the exact match. Among several categories of that name, the one with the lowest id
     * wins, so the rows are ordered by id.
     *
     * @see Wikipedia#PAGE_NAMES_BY_NAME_QUERY
     */
    static final String CATEGORY_BY_NAME_QUERY = "select * from Category where name = :name order by id";

    private final CategoryDAO catDAO;
    private Row row;
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
        catDAO = wiki.getCategoryDAO();
        createCategory(id);
    }

    /**
     * Creates a category object from an already retrieved row, without querying the database. The
     * row is not added to the category cache; callers that want it cached add it themselves.
     *
     * @param wiki
     *            The wikipedia object.
     * @param row
     *            The {@link Row} of a category entity that has already been retrieved.
     * @return A category object backed by {@code row}.
     */
    static Category fromRow(Wikipedia wiki, Row row)
    {
        return new Category(wiki, row);
    }

    // private, so that it does not make public calls like new Category(wiki, null) ambiguous
    private Category(Wikipedia wiki, Row row)
    {
        this.wiki = wiki;
        this.catDAO = wiki.getCategoryDAO();
        this.row = row;
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
        catDAO = wiki.getCategoryDAO();
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
        catDAO = wiki.getCategoryDAO();
        Title catTitle = new Title(pName);
        createCategory(catTitle);
    }

    /**
     * @see Category#Category(Wikipedia, long)
     */
    private void createCategory(long id) throws WikiPageNotFoundException
    {
        // Not added to the category cache: this constructor is used when iterating over all
        // categories, which would only evict the entries that are actually looked up repeatedly.
        row = Row.of(wiki.__inTransaction(session -> catDAO.findById(id)));

        if (row == null) {
            throw new WikiPageNotFoundException("No category with id " + id + " was found.");
        }
    }

    /**
     * @see Category#Category(Wikipedia, int)
     */
    private void createCategory(int pageID) throws WikiPageNotFoundException
    {
        CategoryCache cache = wiki.__getCategoryCache();
        row = cache.get(pageID);
        if (row != null) {
            return;
        }

        row = Row.of(wiki.__inTransaction(session -> session
                .createQuery("from Category where pageId = :pageId",
                        org.dkpro.jwpl.api.hibernate.Category.class)
                .setParameter("pageId", pageID, Integer.class).uniqueResult()));

        // misses are not cached, so a lookup of an unknown page id queries the database every time
        if (row == null) {
            throw new WikiPageNotFoundException(
                    "No category with page id " + pageID + " was found.");
        }
        cache.put(row);
    }

    /**
     * @see Category#Category(Wikipedia, String)
     */
    private void createCategory(Title title) throws WikiPageNotFoundException
    {
        String name = title.getWikiStyleTitle();

        List<org.dkpro.jwpl.api.hibernate.Category> candidates;
        try {
            candidates = wiki.__inTransaction(session -> session
                    .createNativeQuery(CATEGORY_BY_NAME_QUERY,
                            org.dkpro.jwpl.api.hibernate.Category.class)
                    .setParameter("name", name, String.class).list());
        }
        catch (JDBCException e) {
            if (!Wikipedia.isCollationMismatch(e)) {
                throw e;
            }
            // the column cannot hold the name, so no category can have it
            candidates = List.of();
        }

        // the query compares in the collation of the column, so keep the exact match only
        for (org.dkpro.jwpl.api.hibernate.Category candidate : candidates) {
            if (name.equals(candidate.getName())) {
                row = Row.of(candidate);
                break;
            }
        }

        // if there is no category with this name, the row is null
        if (row == null) {
            throw new WikiPageNotFoundException("No category with name " + name + " was found.");
        }
        // The cache is keyed by page id, so it cannot serve lookups by name. Filling it here lets
        // later lookups of this category by page id, e.g. in getParents(), skip the query.
        wiki.__getCategoryCache().put(row);
    }

    /**
     * Loads the elements of one of the link collections of this category. The collection is read
     * by the id of this category instead of via a reattached entity, so no entity is ever shared
     * between sessions.
     *
     * @param collection
     *            The name of the collection property: {@code inLinks}, {@code outLinks} or
     *            {@code pages}.
     * @return A new, modifiable set containing the elements of the collection.
     */
    private Set<Integer> loadCollection(String collection)
    {
        final String hql = "select l from Category c join c." + collection
                + " l where c.id = :id";
        List<Integer> ids = wiki.__inTransaction(session -> session
                .createQuery(hql, Integer.class).setParameter("id", row.id()).list());
        return new HashSet<>(ids);
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
        return row.id();
    }

    /**
     * @return A unique page id.
     */
    public int getPageId()
    {
        return row.pageId();
    }

    /**
     * Returns the parents of this category. The categories are loaded in batches rather than one
     * by one, and those held by the category cache are not queried again. Parents that do not
     * exist as a category are left out.
     *
     * @return A set containing parents (super categories) of this category.
     */
    public Set<Category> getParents()
    {
        Set<Integer> tmpSet = loadCollection("inLinks");

        return wiki.__getCategoriesByPageIds(tmpSet);
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
        String sql = "select count(inLinks) from category_inlinks where id = :id";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("id", id, Long.class).uniqueResult());

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
        return loadCollection("inLinks");
    }

    /**
     * Returns the children of this category. The categories are loaded in batches rather than one
     * by one, and those held by the category cache are not queried again. Children that do not
     * exist as a category are left out.
     *
     * @return A set containing the children (subcategories) of this category.
     */
    public Set<Category> getChildren()
    {
        Set<Integer> tmpSet = loadCollection("outLinks");

        return wiki.__getCategoriesByPageIds(tmpSet);
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
        String sql = "select count(outLinks) from category_outlinks where id = :id";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("id", id, Long.class).uniqueResult());

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
        return loadCollection("outLinks");
    }

    /**
     * @return The title of the category.
     * @throws WikiTitleParsingException
     *             Thrown if errors occurred.
     */
    public Title getTitle() throws WikiTitleParsingException
    {
        String name = row.name();
        return new Title(name);
    }

    /**
     * Returns the articles that are categorized under this category.
     * <p>
     * The pages are loaded without their text, in batches rather than one by one. The text of a
     * returned page is queried on the first call of its {@link Page#getText()}, which therefore
     * requires the {@link Wikipedia} this category belongs to to still be usable.
     *
     * @return The set of articles that are categorized under this category.
     * @throws WikiApiException
     *             Thrown if errors occurred, e.g. if an article of this category does not exist.
     */
    public Set<Page> getArticles() throws WikiApiException
    {
        Set<Integer> tmpSet = getArticleIds();
        Set<Page> pages = wiki.__getPagesWithoutText(tmpSet);
        if (pages.size() < tmpSet.size()) {
            // As before, when every article was loaded by its own getPage(int) call.
            throw new WikiPageNotFoundException("Not all of the " + tmpSet.size()
                    + " articles of the category with page id " + getPageId() + " were found.");
        }
        return pages;
    }

    /**
     * @return The set of article ids that are categorized under this category.
     */
    public Set<Integer> getArticleIds()
    {
        return loadCollection("pages");
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
        String sql = "select count(pages) from category_pages where id = :id";
        Long returnValue = wiki.__inTransaction(session -> session
                .createNativeQuery(sql, Long.class)
                .setParameter("id", id, Long.class).uniqueResult());

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
     * Returns the siblings of this category, i.e. the children of its parents. The child ids of
     * the parents are read with one query per {@value #SIBLING_BATCH_SIZE} parents and the
     * categories are loaded in batches, without loading the parents themselves.
     * <p>
     * This category is one of the children of its parents, so it is part of the result, too. That
     * was already the case when the siblings were collected via {@link #getParents()} and
     * {@link #getChildren()}: removing {@code this} from that set had no effect, as
     * {@link Category} does not override {@link Object#equals(Object)}.
     *
     * @return Returns the siblings of this category, or an empty set if there are none.
     */
    public Set<Category> getSiblings()
    {
        List<Integer> parentIds = new ArrayList<>(getParentIDs());
        Set<Integer> siblingIds = new HashSet<>();
        for (int from = 0; from < parentIds.size(); from += SIBLING_BATCH_SIZE) {
            List<Integer> batch = parentIds.subList(from,
                    Math.min(from + SIBLING_BATCH_SIZE, parentIds.size()));
            siblingIds.addAll(wiki.__inTransaction(session -> session
                    .createQuery("select o from Category c join c.outLinks o"
                            + " where c.pageId in (:ids)", Integer.class)
                    .setParameterList("ids", batch).list()));
        }

        return wiki.__getCategoriesByPageIds(siblingIds);
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
