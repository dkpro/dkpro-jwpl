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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiInitializationException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.dkpro.jwpl.api.util.distance.LevenshteinStringDistance;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.engine.config.WikiConfig;

/**
 * Provides access to Wikipedia articles and categories.
 */
// TODO better JavaDocs!
public class Wikipedia
        implements WikiConstants {

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The native query {@link #existsPage(String)} runs to find the entries of a title, taking the
     * title as parameter {@code pName}. It yields the names of the entries found.
     * <p>
     * Whether the comparison is case-sensitive depends on the collation of the column, and MySQL
     * and MariaDB default to case-insensitive ones. An explicit binary collation in the query,
     * though, keeps these databases from using the index on the column, which turns every lookup
     * into a full scan. So the query compares in the collation of the column, and the caller keeps
     * only the entries whose name equals the title exactly. These are few: the case variants of
     * one title at most.
     */
    static final String PAGE_NAMES_BY_NAME_QUERY = "select p.name from PageMapLine as p "
            + "where p.name = :pName";

    /**
     * The MySQL and MariaDB error codes for comparing strings whose collations cannot be
     * reconciled: {@code ER_CANT_AGGREGATE_2COLLATIONS}, {@code ER_CANT_AGGREGATE_3COLLATIONS} and
     * {@code ER_CANT_AGGREGATE_NCOLLATIONS}. A comparison of a column with a parameter fails this
     * way when the column charset, e.g. {@code utf8mb3} or {@code latin1}, cannot represent a
     * character of the parameter.
     */
    private static final Set<Integer> COLLATION_MISMATCH_ERRORS = Set.of(1267, 1270, 1271);

    /**
     * Upper bound for the number of page ids bound into a single {@code in (..)} clause of
     * {@link Wikipedia#getTitles(Collection)} and {@link Wikipedia#getPages(List)}. Pages with
     * several thousand outgoing links exist, and neither the JDBC drivers nor the query planners
     * deal well with parameter lists of that size.
     */
    private static final int ID_BATCH_SIZE = 500;

    /**
     * Fetch size that makes MySQL Connector/J stream a forward-only, read-only result set row by
     * row. Any other value, positive ones included, makes it read the complete result set into
     * memory unless the connection property {@code useCursorFetch=true} is set. While the result
     * set is open, no other query may be issued on the connection.
     *
     * @see <a href=
     *      "https://dev.mysql.com/doc/connector-j/en/connector-j-reference-implementation-notes.html">
     *      MySQL Connector/J: JDBC API Implementation Notes, ResultSet</a>
     */
    static final int MYSQL_STREAMING_FETCH_SIZE = Integer.MIN_VALUE;

    /**
     * Number of rows MariaDB Connector/J keeps in memory while streaming a result set. It rejects a
     * negative fetch size with an {@link java.sql.SQLException}, so the MySQL Connector/J value
     * {@link Integer#MIN_VALUE} must not be passed to it. A query issued on the same connection
     * before the result set is read completely makes it read all remaining rows into memory.
     *
     * @see <a href=
     *      "https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j">
     *      About MariaDB Connector/J, Streaming Result Sets</a>
     */
    static final int MARIADB_STREAMING_FETCH_SIZE = 1000;

    /**
     * Number of rows pgJDBC fetches per round trip from a server-side cursor. The driver uses a
     * cursor only if the fetch size is positive, the connection is not in autocommit mode, the
     * result set is {@link java.sql.ResultSet#TYPE_FORWARD_ONLY} and the query is a single
     * statement; otherwise it silently reads the complete result set.
     *
     * @see <a href="https://jdbc.postgresql.org/documentation/query/">pgJDBC: Issuing a Query and
     *      Processing the Result, Getting results based on a cursor</a>
     */
    static final int POSTGRESQL_STREAMING_FETCH_SIZE = 1000;

    /**
     * Fetch size passed to HSQLDB and to any driver not handled explicitly. The JDBC specification
     * defines it as a hint that must be {@code >= 0}; HSQLDB uses it as such and may process more
     * or fewer rows.
     *
     * @see <a href=
     *      "https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/Statement.html#setFetchSize(int)">
     *      java.sql.Statement#setFetchSize(int)</a>
     * @see <a href=
     *      "https://hsqldb.org/doc/2.0/apidocs/org.hsqldb/org/hsqldb/jdbc/JDBCStatement.html#setFetchSize(int)">
     *      HSQLDB: JDBCStatement#setFetchSize(int)</a>
     */
    static final int DEFAULT_STREAMING_FETCH_SIZE = 1000;

    /**
     * Selects the metadata columns of a page, i.e. every column but its text, in the order
     * {@link #toPageWithoutText(Object[])} expects them. The alias of the page is {@code p}.
     */
    static final String PAGE_METADATA_SELECT =
            "select p.id, p.pageId, p.name, p.isDisambiguation from Page as p";

    private final Language language;
    private final DatabaseConfiguration dbConfig;

    /*
     * The data access objects shared by all Page and Category objects of this instance. Created on
     * first use, they pin the SessionFactory the configuration maps to at that time.
     */
    private volatile org.dkpro.jwpl.api.hibernate.PageDAO pageDAO;
    private volatile org.dkpro.jwpl.api.hibernate.CategoryDAO categoryDAO;

    private final MetaData metaData;

    // Loaded category rows by page id, see DatabaseConfiguration#setCategoryCacheSize(int).
    private final CategoryCache categoryCache;

    // Note: This should only be accessed internally. Built lazily, see getWikConfig().
    private volatile WikiConfig wikiConfig;

    private final Object wikiConfigLock = new Object();

    /*
     * How the backend lower-cases PageMapLine.name, see getNameLowering(). Null until determined.
     */
    private volatile Optional<CaseVariantPrefixes.Lowering> nameLowering;

    /**
     * Creates a new {@link Wikipedia} object accessing the database indicated by the dbConfig
     * parameter.
     *
     * @param dbConfig A {@link DatabaseConfiguration} object telling the {@link Wikipedia} object where
     *                 the data is stored and how it can be accessed.
     * @throws WikiInitializationException Thrown if errors occurred while bootstrapping the {@link Wikipedia} instance.
     */
    public Wikipedia(DatabaseConfiguration dbConfig) throws WikiInitializationException {

        logger.trace("Creating Wikipedia object.");

        this.language = dbConfig.getLanguage();
        this.dbConfig = dbConfig;
        this.categoryCache = new CategoryCache(dbConfig.getCategoryCacheSize());

        this.metaData = new MetaData(this);

        if (dbConfig.supportsCollation()) {
            logger.info("Wikipedia database backend supports character collation features.");
        } else {
            logger.debug(
                    "Wikipedia database backend does NOT support character collation features.");
        }
    }

    /**
     * Returns the Sweble parser configuration for the language of this instance. The configuration
     * is built on first use rather than on construction, as it may have to be fetched from the
     * corresponding Wikipedia edition. The returned instance may be shared and must be treated as
     * read-only.
     *
     * @return The {@link WikiConfig} for the language of this instance.
     */
    WikiConfig getWikConfig() {
        WikiConfig config = wikiConfig;
        if (config == null) {
            synchronized (wikiConfigLock) {
                config = wikiConfig;
                if (config == null) {
                    config = language.getWikiconfig();
                    wikiConfig = config;
                }
            }
        }
        return config;
    }

    /**
     * Gets the page with the given title. If the title is a redirect, the corresponding page is
     * returned.<br>
     * If the title start with a lowercase letter it converts it to an uppercase letter, as each
     * Wikipedia article title starts with an uppercase letter. Spaces in the title are converted to
     * underscores, as this is a convention for Wikipedia article titles.
     * <p>
     * For example, the article "Steam boat" could be queried with - "Steam boat" - "steam boat" -
     * "Steam_boat" - "steam_boat" and additionally all redirects that might point to that article.
     *
     * @param title The title of the page.
     * @return The page object for a given title.
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly
     *                          parsed.
     */
    public Page getPage(String title) throws WikiApiException {
        return new Page(this, title, false);
    }

    /**
     * Gets the page with exactly the given title.<br>
     * <p>
     * Note that when using this method you are responsible for converting a normal search string
     * into the right wiki-style.<br>
     * <p>
     * If the title is a redirect, the corresponding page is returned.<br>
     *
     * @param exactTitle The exact title of the page.
     * @return The page object for a given title.
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly
     *                          parsed.
     */
    public Page getPageByExactTitle(String exactTitle) throws WikiApiException {
        return new Page(this, exactTitle, true);
    }

    /**
     * Get all pages which match all lowercase/uppercase version of the given title.<br>
     * If the title is a redirect, the corresponding page is returned.<br>
     * Spaces in the title are converted to underscores, as this is a convention for Wikipedia
     * article titles.
     *
     * @param title The title of the page.
     * @return A set of page objects matching this title.
     * @throws WikiApiException If no page or redirect with this title exists or the title could not be properly
     *                          parsed.
     */
    public Set<Page> getPages(String title) throws WikiApiException {
        Set<Integer> ids = new HashSet<>(getPageIdsCaseInsensitive(title));

        Set<Page> pages = new HashSet<>();
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
     * Loads the pages with the given ids, in one query per {@value Wikipedia#ID_BATCH_SIZE} ids
     * instead of one query per page as {@link Wikipedia#getPage(int)} does.
     * <p>
     * The result is positional: the pages are returned in the order of the given ids, and an id
     * that occurs several times yields a page at each of its positions. This is why the ids are
     * passed as a {@link List}, while {@link Wikipedia#getTitles(Collection)} accepts any
     * {@link Collection}: its result is keyed by page id, so neither order nor duplicates matter
     * there.
     * <p>
     * Ids that have no matching page are skipped; callers that need to detect them have to compare
     * the result against the ids they passed in. Like {@link Wikipedia#getPage(int)}, every page is
     * loaded including its text, so the result of a large number of ids can occupy a lot of
     * memory.
     *
     * @param pageIds The ids of the pages to load. Must not be {@code null} and must not contain
     *                {@code null}.
     * @return The loaded pages, in the order of {@code pageIds}. Never {@code null}.
     * @throws IllegalArgumentException Thrown if {@code pageIds} is {@code null} or contains
     *                                  {@code null}.
     * @throws WikiApiException Declared for consistency with {@link Wikipedia#getPage(int)}, but
     *                          not thrown by this implementation: missing pages are skipped, and a
     *                          failing query surfaces as the unchecked
     *                          {@link org.hibernate.HibernateException} of the underlying
     *                          Hibernate call, as in {@link Wikipedia#getTitles(Collection)}.
     */
    public List<Page> getPages(List<Integer> pageIds) throws WikiApiException {
        if (pageIds == null) {
            throw new IllegalArgumentException("pageIds must not be null");
        }
        // Not List#contains(null): immutable lists such as List.of(..) reject a null argument.
        for (Integer pageId : pageIds) {
            if (pageId == null) {
                throw new IllegalArgumentException("pageIds must not contain null");
            }
        }

        List<Page> pages = new ArrayList<>(pageIds.size());
        Map<Integer, org.dkpro.jwpl.api.hibernate.Page> rowsByPageId = new HashMap<>();
        for (int from = 0; from < pageIds.size(); from += ID_BATCH_SIZE) {
            List<Integer> batch = pageIds.subList(from,
                    Math.min(from + ID_BATCH_SIZE, pageIds.size()));
            List<org.dkpro.jwpl.api.hibernate.Page> rows = __inTransaction(session -> session
                    .createQuery("from Page as p where p.pageId in (:ids)",
                            org.dkpro.jwpl.api.hibernate.Page.class)
                    .setParameterList("ids", batch).list());

            rowsByPageId.clear();
            for (org.dkpro.jwpl.api.hibernate.Page row : rows) {
                rowsByPageId.put(row.getPageId(), row);
            }
            for (Integer pageId : batch) {
                org.dkpro.jwpl.api.hibernate.Page row = rowsByPageId.get(pageId);
                if (row != null) {
                    pages.add(new Page(this, row.getId(), row));
                }
            }
        }
        return pages;
    }

    /**
     * Gets the title for a given pageId.
     *
     * @param pageId The id of the page.
     * @return The title for the given pageId.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Title getTitle(int pageId) throws WikiApiException {
        // Read from Page, not from PageMapLine, the way getTitles(Collection) does. A page id is
        // unique in Page, while PageMapLine holds one entry per title the id can be reached by - the
        // name of the page itself and the name of every redirect pointing to it. Asking PageMapLine
        // for a unique result therefore failed for every page that has a redirect, and it could not
        // tell the name of the page from the name of a redirect to begin with.
        String sql = "select p.name from Page as p where p.pageId = :pId";
        String returnValue = __inTransaction(
                session -> session.createQuery(sql, String.class).setParameter("pId", pageId)
                        .uniqueResult());

        if (returnValue == null) {
            throw new WikiPageNotFoundException();
        }
        return new Title(returnValue);
    }

    /**
     * Resolves the titles of many pages at once, without loading the pages themselves.
     * <p>
     * Obtaining the same titles via {@link Wikipedia#getPage(int)} and {@link Page#getTitle()}
     * costs one full entity load - article text included - per page. This reads nothing but the
     * page names, in one query per {@value Wikipedia#ID_BATCH_SIZE} ids.
     * <p>
     * Ids that have no matching page are absent from the result, as are pages whose name cannot be
     * parsed into a {@link Title}. Callers that need to tell those cases apart have to compare the
     * key set of the result against the ids they passed in.
     *
     * @param pageIds The ids of the pages to resolve titles for. Must not be {@code null}.
     * @return The resolved titles, keyed by page id. Never {@code null}.
     */
    public Map<Integer, Title> getTitles(Collection<Integer> pageIds) {
        Map<Integer, Title> titles = new HashMap<>();
        if (pageIds.isEmpty()) {
            return titles;
        }

        List<Integer> ids = new ArrayList<>(pageIds);
        for (int from = 0; from < ids.size(); from += ID_BATCH_SIZE) {
            List<Integer> batch = ids.subList(from, Math.min(from + ID_BATCH_SIZE, ids.size()));
            // A session is acquired per batch on purpose: the thread-bound session context unbinds
            // and closes the session once its transaction completes, so it must not be reused
            // across batches.
            List<Object[]> rows = __inTransaction(session -> session
                    .createQuery("select p.pageId, p.name from Page as p where p.pageId in (:ids)",
                            Object[].class)
                    .setParameterList("ids", batch).list());

            for (Object[] row : rows) {
                Integer pageId = (Integer) row[0];
                String name = (String) row[1];
                try {
                    titles.put(pageId, new Title(name));
                } catch (WikiTitleParsingException e) {
                    // A single unparsable name must not cost the caller the remaining titles.
                    // This is the only record of the failure, so the exception is logged with it.
                    logger.warn("Could not parse the title '{}' of the page with page id {}", name,
                            pageId, e);
                }
            }
        }
        return titles;
    }

    /**
     * Reads the page ids of the categories of many pages at once, without loading the pages or the
     * categories.
     * <p>
     * Calling {@link Page#getCategoryIDs()} on each page costs one query per page. This reads the
     * category ids of all given pages in one query per {@value Wikipedia#ID_BATCH_SIZE} page ids,
     * so a caller that knows its pages in advance can prefetch their categories and check
     * membership without further queries.
     * <p>
     * A page without categories maps to an empty set. Ids that have no matching page are absent
     * from the result, so callers that need to detect them have to compare the key set of the
     * result against the ids they passed in. As with {@link Page#getCategoryIDs()}, the sets may
     * contain the ids of categories that do not exist.
     *
     * @param pageIds The ids of the pages to read the category ids for. Must not be {@code null}
     *                and must not contain {@code null}.
     * @return The page ids of the categories, keyed by the page id of the page they belong to. The
     *         map and its sets are new and modifiable. Never {@code null}.
     * @throws IllegalArgumentException Thrown if {@code pageIds} is {@code null} or contains
     *                                  {@code null}.
     */
    public Map<Integer, Set<Integer>> getCategoryIDs(Collection<Integer> pageIds) {
        if (pageIds == null) {
            throw new IllegalArgumentException("pageIds must not be null");
        }
        // Copied into a set first, which also drops duplicates. Not Collection#contains(null), as
        // immutable collections such as Set.of(..) reject a null argument.
        Set<Integer> distinct = new HashSet<>();
        for (Integer pageId : pageIds) {
            if (pageId == null) {
                throw new IllegalArgumentException("pageIds must not contain null");
            }
            distinct.add(pageId);
        }

        Map<Integer, Set<Integer>> categoryIds = new HashMap<>();
        List<Integer> ids = new ArrayList<>(distinct);
        for (int from = 0; from < ids.size(); from += ID_BATCH_SIZE) {
            List<Integer> batch = ids.subList(from, Math.min(from + ID_BATCH_SIZE, ids.size()));
            // A session is acquired per batch, as in getTitles(Collection). The left join yields a
            // single null category for a page without categories, so that it maps to an empty set.
            List<Object[]> rows = __inTransaction(session -> session
                    .createQuery("select p.pageId, c from Page p left join p.categories c"
                            + " where p.pageId in (:ids)", Object[].class)
                    .setParameterList("ids", batch).list());

            for (Object[] row : rows) {
                Set<Integer> categories = categoryIds.computeIfAbsent((Integer) row[0],
                        pageId -> new HashSet<>());
                if (row[1] != null) {
                    categories.add((Integer) row[1]);
                }
            }
        }
        return categoryIds;
    }

    /**
     * Loads the pages with the given page ids without their text, in one query per
     * {@value Wikipedia#ID_BATCH_SIZE} ids. The text of each page is queried on the first call
     * of {@link Page#getText()}.
     * <p>
     * Ids that have no matching page are absent from the result.
     *
     * @param pageIds The ids of the pages to load. Must not be {@code null}.
     * @return A new, modifiable set with the loaded pages. Never {@code null}.
     */
    Set<Page> __getPagesWithoutText(Collection<Integer> pageIds) {
        Set<Page> pages = new HashSet<>();
        // Copied first, as the isEmpty() of UnmodifiableArraySet cannot be relied upon.
        List<Integer> ids = new ArrayList<>(pageIds);
        for (int from = 0; from < ids.size(); from += ID_BATCH_SIZE) {
            List<Integer> batch = ids.subList(from, Math.min(from + ID_BATCH_SIZE, ids.size()));
            // A session is acquired per batch, as in getTitles(Collection).
            List<Object[]> rows = __inTransaction(session -> session
                    .createQuery(PAGE_METADATA_SELECT + " where p.pageId in (:ids)",
                            Object[].class)
                    .setParameterList("ids", batch).list());

            for (Object[] row : rows) {
                pages.add(Page.of(this, toPageWithoutText(row)));
            }
        }
        return pages;
    }

    /**
     * @param row A row selected by {@link #PAGE_METADATA_SELECT}.
     * @return A hibernate page that holds the metadata of the row, but no text.
     */
    static org.dkpro.jwpl.api.hibernate.Page toPageWithoutText(Object[] row) {
        return new org.dkpro.jwpl.api.hibernate.Page((Long) row[0], (Integer) row[1],
                (String) row[2], Boolean.TRUE.equals(row[3]));
    }

    /**
     * Loads the categories with the given page ids. Categories held by the category cache of this
     * instance are taken from there, the others are read in one query per
     * {@value Wikipedia#ID_BATCH_SIZE} ids and added to the cache, as
     * {@link #getCategory(int)} does.
     * <p>
     * Ids that have no matching category are absent from the result.
     *
     * @param pageIds The page ids of the categories to load. Must not be {@code null}.
     * @return A new, modifiable set with the loaded categories. Never {@code null}.
     */
    Set<Category> __getCategoriesByPageIds(Collection<Integer> pageIds) {
        Set<Category> categories = new HashSet<>();
        List<Integer> missing = new ArrayList<>();
        // Copied into a set first, so that a duplicate id yields a single category, and iterated
        // rather than checked with isEmpty(), which UnmodifiableArraySet gets wrong.
        for (Integer pageId : new HashSet<>(pageIds)) {
            if (pageId == null) {
                continue;
            }
            Category.Row row = categoryCache.get(pageId);
            if (row != null) {
                categories.add(Category.fromRow(this, row));
            } else {
                missing.add(pageId);
            }
        }

        for (int from = 0; from < missing.size(); from += ID_BATCH_SIZE) {
            List<Integer> batch = missing.subList(from,
                    Math.min(from + ID_BATCH_SIZE, missing.size()));
            // A session is acquired per batch, as in getTitles(Collection).
            List<Object[]> rows = __inTransaction(session -> session
                    .createQuery("select c.id, c.pageId, c.name from Category as c"
                            + " where c.pageId in (:ids)", Object[].class)
                    .setParameterList("ids", batch).list());

            for (Object[] columns : rows) {
                Category.Row row = new Category.Row((Long) columns[0], (Integer) columns[1],
                        (String) columns[2]);
                categoryCache.put(row);
                categories.add(Category.fromRow(this, row));
            }
        }
        return categories;
    }

    /**
     * Gets the page ids for a given title.
     *
     * @param title The title of the page.
     * @return The id for the page with the given title.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<Integer> getPageIds(String title) throws WikiApiException {
        String sql = "select p.pageID from PageMapLine as p where p.name = :pName";
        Iterator<Integer> results = __inTransaction(session -> session
                .createQuery(sql, Integer.class)
                .setParameter("pName", title, String.class).list()).iterator();

        if (!results.hasNext()) {
            throw new WikiPageNotFoundException();
        }
        List<Integer> resultList = new LinkedList<>();
        while (results.hasNext()) {
            resultList.add(results.next());
        }
        return resultList;
    }

    /**
     * Gets the page ids for a given title with case-insensitive matching.<br>
     * <p>
     * On HSQLDB and on MySQL or MariaDB databases whose {@code PageMapLine.name} column has a
     * binary {@code utf8*_bin} collation, as JWPL databases have, the lookup seeks the index on
     * {@code name} for the case variants of a short prefix of the title, see
     * {@link CaseVariantPrefixes}. On any other collation it scans the whole table, since a
     * case- or accent-insensitive collation lets names match that the prefix does not cover.
     *
     * @param title The title of the page.
     * @return The ids of the pages with the given title.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public List<Integer> getPageIdsCaseInsensitive(String title) throws WikiApiException {
        final String normalizedTitle = title.toLowerCase().replaceAll(" ", "_");

        Optional<CaseVariantPrefixes.Lowering> lowering = getNameLowering();
        List<String> prefixPatterns = lowering.isPresent()
                ? CaseVariantPrefixes.of(normalizedTitle, lowering.get())
                : Collections.emptyList();

        List<Integer> resultList = queryPageIdsCaseInsensitive(normalizedTitle, prefixPatterns);
        if (resultList.isEmpty()) {
            throw new WikiPageNotFoundException();
        }
        return resultList;
    }

    /**
     * Selects the page ids whose name lower-cases to the given title.
     *
     * @param normalizedTitle The lower-cased title, with underscores instead of spaces.
     * @param prefixPatterns  Patterns from {@link CaseVariantPrefixes#of} restricting the names to
     *                        index ranges, or an empty list to evaluate the predicate on every row.
     *                        They narrow the rows to look at, yet never change the result.
     * @return The matching page ids, possibly empty.
     */
    List<Integer> queryPageIdsCaseInsensitive(String normalizedTitle, List<String> prefixPatterns) {
        StringBuilder hql = new StringBuilder(
                "select p.pageID from PageMapLine as p where lower(p.name) = :pName");
        if (!prefixPatterns.isEmpty()) {
            hql.append(" and (");
            for (int i = 0; i < prefixPatterns.size(); i++) {
                if (i > 0) {
                    hql.append(" or ");
                }
                hql.append("p.name like :prefix").append(i)
                        .append(" escape '").append(CaseVariantPrefixes.ESCAPE).append('\'');
            }
            hql.append(')');
        }
        return __inTransaction(session -> {
            Query<Integer> query = session.createQuery(hql.toString(), Integer.class)
                    .setParameter("pName", normalizedTitle, String.class);
            for (int i = 0; i < prefixPatterns.size(); i++) {
                query.setParameter("prefix" + i, prefixPatterns.get(i), String.class);
            }
            return new ArrayList<>(query.list());
        });
    }

    /**
     * @return How the backend lower-cases {@code PageMapLine.name} if a case-insensitive lookup
     *         may narrow the names by prefix, else empty. Determined once per instance.
     */
    private Optional<CaseVariantPrefixes.Lowering> getNameLowering() {
        Optional<CaseVariantPrefixes.Lowering> lowering = nameLowering;
        if (lowering == null) {
            lowering = detectNameLowering();
            nameLowering = lowering;
        }
        return lowering;
    }

    private Optional<CaseVariantPrefixes.Lowering> detectNameLowering() {
        String driver = dbConfig.getDatabaseDriver();
        if (driver != null && driver.toLowerCase(Locale.ROOT).contains("hsqldb")) {
            // HSQLDB compares strings by code point, unless a collation is set explicitly.
            return Optional.of(CaseVariantPrefixes.Lowering.CONTEXTUAL);
        }
        if (!dbConfig.supportsCollation()) {
            return Optional.empty();
        }
        String sql = "select c.COLLATION_NAME, @@character_set_connection"
                + " from information_schema.COLUMNS c where c.TABLE_SCHEMA = database()"
                + " and lower(c.TABLE_NAME) = 'pagemapline' and lower(c.COLUMN_NAME) = 'name'";
        try {
            List<Object[]> rows = __inTransaction(
                    session -> session.createNativeQuery(sql, Object[].class).list());
            if (rows.size() == 1 && rows.get(0)[0] != null && rows.get(0)[1] != null) {
                String collation = rows.get(0)[0].toString().toLowerCase(Locale.ROOT);
                String connectionCharset = rows.get(0)[1].toString().toLowerCase(Locale.ROOT);
                // A binary collation compares code points, so LIKE 'Prefix%' matches exactly the
                // names starting with Prefix. The patterns must reach the server unaltered, which
                // any utf8 connection character set guarantees for the characters they consist of.
                if (collation.startsWith("utf8") && collation.endsWith("_bin")
                        && connectionCharset.startsWith("utf8")) {
                    return Optional.of(CaseVariantPrefixes.Lowering.PER_CHARACTER);
                }
                logger.debug("Collation {} of PageMapLine.name (connection character set {}) "
                        + "rules out indexed case-insensitive title lookups.", collation,
                        connectionCharset);
            }
        } catch (RuntimeException e) {
            logger.debug("Could not determine the collation of PageMapLine.name.", e);
        }
        return Optional.empty();
    }

    /**
     * Returns the article page for a given discussion page.
     *
     * @param discussionPage the discussion page object
     * @return The page object of the article associated with the discussion. If the parameter
     * already was an article, it is returned directly.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Page getArticleForDiscussionPage(Page discussionPage) throws WikiApiException {
        if (discussionPage.isDiscussion()) {
            String title = discussionPage.getTitle().getPlainTitle()
                    .replaceAll(WikiConstants.DISCUSSION_PREFIX, "");

            if (title.contains("/")) {
                // If we have a discussion archive
                // TODO This does not support articles that contain slashes-
                // However, the rest of the API cannot cope with that as well, so this should not be
                // any extra trouble
                title = title.split("/")[0];
            }
            return getPage(title);
        } else {
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
        // Retrieve discussion page with article title
        // TODO not the prettiest solution, but currently discussions are only marked in the title
        return getDiscussionPage(getPage(articlePageId));
    }

    /**
     * Gets the discussion page for the page with the given title. The page retrieval works as
     * defined in {@link #getPage(String title)}
     *
     * @param title The title of the page for which the discussions should be retrieved.
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly
     *                          parsed.
     */
    public Page getDiscussionPage(String title) throws WikiApiException {
        return getDiscussionPage(getPage(title));
    }

    /**
     * Gets the discussion page for the given article page The provided page must not be a
     * discussion page
     *
     * @param articlePage the article page for which a discussion page should be retrieved
     * @return The discussion page object for the given article page object
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly
     *                          parsed.
     */
    public Page getDiscussionPage(Page articlePage) throws WikiApiException {
        String articleTitle = articlePage.getTitle().toString();
        if (articleTitle.startsWith(WikiConstants.DISCUSSION_PREFIX)) {
            return articlePage;
        } else {
            return new Page(this, WikiConstants.DISCUSSION_PREFIX + articleTitle);
        }
    }

    /**
     * Returns an iterable containing all archived discussion pages for the page with the given
     * title String. <br>
     * The page retrieval works as defined in {@link #getPage(int)}. <br>
     * The most recent discussion page is NOT included here! It can be obtained with
     * {@link #getDiscussionPage(Page)}.
     *
     * @param articlePageId The id of the page for which to fetch the discussion archives
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly
     *                          parsed.
     */
    public Iterable<Page> getDiscussionArchives(int articlePageId) throws WikiApiException {
        // Retrieve discussion archive pages with page id
        return getDiscussionArchives(getPage(articlePageId));
    }

    /**
     * Returns an iterable containing all archived discussion pages for the page with the given
     * title String. <br>
     * The page retrieval works as defined in {@link #getPage(String title)}.<br>
     * The most recent discussion page is NOT included here! It can be obtained with
     * {@link #getDiscussionPage(Page)}.
     *
     * @param title The title of the page for which the discussions should be retrieved.
     * @return The page object for the discussion page.
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly
     *                          parsed.
     * @deprecated Use {@link #getDiscussionArchives(int)} or {@link #getDiscussionArchives(Page)}
     * instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public Iterable<Page> getDiscussionArchives(String title) throws WikiApiException {
        // Retrieve discussion archive pages with page title
        return getDiscussionArchives(getPage(title));
    }

    /**
     * Return an iterable containing all archived discussion pages for the given article page. The
     * most recent discussion page is not included. The most recent discussion page can be obtained
     * with {@link #getDiscussionPage(Page)}. <br>
     * The provided page Object must not be a discussion page itself! If it is a discussion page, is
     * returned unchanged.
     *
     * @param articlePage the article page for which a discussion archives should be retrieved
     * @return An iterable with the discussion archive page objects for the given article page
     * object
     * @throws WikiApiException If no page or redirect with this title exists or title could not be properly
     *                          parsed.
     */
    public Iterable<Page> getDiscussionArchives(Page articlePage) throws WikiApiException {
        String articleTitle = articlePage.getTitle().getWikiStyleTitle();
        if (!articleTitle.startsWith(WikiConstants.DISCUSSION_PREFIX)) {
            articleTitle = WikiConstants.DISCUSSION_PREFIX + articleTitle;
        }

        List<Page> discussionArchives = new LinkedList<>();

        String sql = "SELECT pageID FROM PageMapLine where name like :name";
        final String namePattern = articleTitle + "/%";
        Iterator<Integer> results = __inTransaction(session -> session
                .createQuery(sql, Integer.class)
                .setParameter("name", namePattern, String.class).list()).iterator();

        while (results.hasNext()) {
            int pageID = results.next();
            discussionArchives.add(getPage(pageID));
        }
        return discussionArchives;
    }

    /**
     * Gets the pages or redirects with a name similar to the pattern. Calling this method is quite
     * costly, as similarity is computed for all names.
     *
     * @param pPattern The pattern.
     * @param pSize    The maximum size of the result list. Only the most similar results will be
     *                 included.
     * @return A map of pages with names similar to the pattern and their distance values. Smaller
     * distances are more similar.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Map<Page, Double> getSimilarPages(String pPattern, int pSize) throws WikiApiException {
        Title title = new Title(pPattern);
        String pattern = title.getWikiStyleTitle();

        // a mapping of the most similar pages and their similarity values
        // It is returned by this method.
        Map<Page, Double> pageMap = new HashMap<>();

        // holds a mapping of the best distance values to page IDs
        final Map<Integer, Double> distanceMap = new HashMap<>();
        // how many entries of distanceMap carry each distance value, the largest one last
        final TreeMap<Double, Integer> distanceCounts = new TreeMap<>();

        final LevenshteinStringDistance lsd = new LevenshteinStringDistance();
        final String query = "select new org.dkpro.jwpl.api.Wikipedia$PageTuple(pml.pageID, pml.name)"
                + " from PageMapLine as pml";
        final int fetchSize = streamingFetchSize(dbConfig.getDatabaseDriver());
        // The rows are streamed rather than materialized, so the heap holds the current top
        // entries only. This keeps the connection open for the duration of the scan; no other
        // query may run on it until the stream is closed (see MYSQL_STREAMING_FETCH_SIZE).
        __inTransaction(session -> {
            try (Stream<PageTuple> rows = session.createQuery(query, PageTuple.class)
                    .setFetchSize(fetchSize).getResultStream()) {
                rows.forEach(o -> {
                    // this returns a similarity - if we want to use it, we have to change the
                    // semantics the ordering of the results
                    double distance = lsd.distance(o.name(), pattern);

                    Double previous = distanceMap.put(o.id(), distance);
                    if (previous != null) {
                        decrementDistanceCount(distanceCounts, previous);
                    }
                    distanceCounts.merge(distance, 1, Integer::sum);

                    // if there are more than "pSize" entries in the map remove the one with the
                    // biggest distance
                    if (distanceMap.size() > pSize) {
                        removeLargestDistance(distanceMap, distanceCounts, o.id(), distance);
                    }
                });
            }
            return null;
        });

        for (int pageID : distanceMap.keySet()) {
            Page page = null;
            try {
                page = this.getPage(pageID);
            } catch (WikiPageNotFoundException e) {
                logger.error("Page with pageID {} could not be found. Fatal error. Terminating.", pageID, e);
            }
            pageMap.put(page, distanceMap.get(pageID));
        }

        return pageMap;
    }

    /**
     * Gets the category for a given title. If the {@link Category} title start with a lowercase
     * letter it converts it to an uppercase letter, as each Wikipedia category title starts with an
     * uppercase letter. Spaces in the title are converted to underscores, as this is a convention
     * for Wikipedia category titles.
     * <p>
     * For example, the (possible) category "Famous steamboats" could be queried with - "Famous
     * steamboats" - "Famous_steamboats" - "famous steamboats" - "famous_steamboats"
     *
     * @param title The title of the category.
     * @return The category object with the given title.
     * @throws WikiApiException If no category with the given title exists.
     */
    public Category getCategory(String title) throws WikiApiException {
        return new Category(this, title);
    }

    /**
     * Gets the category for a given pageId.
     * <p>
     * Repeated lookups of the same category are served from a bounded cache of loaded category
     * rows without querying the database, see {@link DatabaseConfiguration#setCategoryCacheSize(int)}.
     *
     * @param pageId The id of the {@link Category}.
     * @return The category object or {@code null} if no category with this pageId exists.
     */
    public Category getCategory(int pageId) {
        try {
            return new Category(this, pageId);
        } catch (WikiPageNotFoundException e) {
            // The exception is used here as a control-flow signal only: the contract of this
            // method is to return null if no category exists for the given id, so there is
            // nothing to chain or log.
            return null;
        }
    }

    /**
     * This returns an iterable over all {@link Category categories}, as returning all category
     * objects would be much too expensive.
     *
     * @return An iterable over all categories.
     */
    public Iterable<Category> getCategories() {
        return new CategoryIterable(this);
    }

    /**
     * Gets the {@link Category categories} for a given {@link Page} identified by its
     * {@code pageTitle}.
     *
     * @param pageTitle The title of a {@link Page}, not a category.
     * @return The category objects which are associated with the given {@code pageTitle}.
     * @throws WikiPageNotFoundException Thrown if no {@link Page} exists for the given {@code pageTitle}.
     */
    public Set<Category> getCategories(String pageTitle) throws WikiPageNotFoundException {
        if (pageTitle == null || pageTitle.isEmpty()) {
            throw new WikiPageNotFoundException();
        }

        String sql = "select c from Page p left join p.categories c where p.name = :pageTitle";
        // The elements of the collection are the page ids of the categories. The left join yields
        // a single null for a page without categories, which is skipped.
        List<Integer> categoryPageIds = __inTransaction(session -> session
                .createQuery(sql, Integer.class).setParameter("pageTitle", pageTitle).list());

        Set<Category> categorySet = __getCategoriesByPageIds(categoryPageIds);
        long expected = categoryPageIds.stream().filter(Objects::nonNull).distinct().count();
        if (categorySet.size() < expected) {
            logger.warn("Could not load {} of the {} categories of the page '{}'",
                    expected - categorySet.size(), expected, pageTitle);
        }
        return categorySet;
    }

    /**
     * Get all wikipedia {@link Category categories}. Returns only an iterable, as a collection may
     * not fit into memory for a large wikipedia.
     *
     * @param bufferSize The size of the internal page buffer.
     * @return An iterable over all categories.
     */
    protected Iterable<Category> getCategories(int bufferSize) {
        return new CategoryIterable(this, bufferSize);
    }

    /**
     * Protected method that is much faster than the public version, but exposes too much
     * implementation details. Get a set with all category pageIDs. Returning all category objects
     * is much too expensive.
     *
     * @return A set with all category pageIDs
     */
    // TODO this should be replaced with the buffered category iterator, as it might produce an
    // HeapSpace Overflow, if there are too many categories.
    protected Set<Integer> __getCategories() {
        return __getIdSet("select cat.pageId from Category as cat");
    }

    /**
     * Get all wikipedia pages. Does not include redirects, as they are only pointers to real pages.
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     *
     * @return An iterable over all pages.
     */
    public Iterable<Page> getPages() {
        return new PageIterable(this, false);
    }

    /**
     * Get all wikipedia pages. Does not include redirects, as they are only pointers to real pages.
     * Returns only an iterable, as a collection may not fit into memory for a large wikipedia.
     *
     * @param bufferSize The size of the internal page buffer.
     * @return An iterable over all pages.
     */
    protected Iterable<Page> getPages(int bufferSize) {
        return new PageIterable(this, false, bufferSize);
    }

    /**
     * Protected method that is much faster than the public version, but exposes too much
     * implementation details. Get a set with all {@code pageIDs}. Returning all page objects is
     * much too expensive. Does not include redirects, as they are only pointers to real pages.
     * <p>
     * As ids can be useful for several application (e.g. in combination with the RevisionMachine),
     * they have been made publicly available via {@link #getPageIds()}.
     *
     * @return A set with all {@code pageIDs}. Returning all pages is much to expensive.
     */
    protected Set<Integer> __getPages() {
        return __getIdSet("select page.pageId from Page as page");
    }

    /**
     * Runs the given id projection query and collects its rows directly into a {@link HashSet},
     * without materializing an intermediate {@link List} first.
     *
     * @param hql An HQL query selecting a single {@link Integer} column.
     * @return A mutable set with the selected ids.
     */
    private Set<Integer> __getIdSet(String hql) {
        return __inTransaction(session -> {
            try (Stream<Integer> ids = session.createQuery(hql, Integer.class).getResultStream()) {
                return ids.collect(Collectors.toCollection(HashSet::new));
            }
        });
    }

    /**
     * @return an iterable over all {@code pageIDs} (without redirects)
     */
    public Iterable<Integer> getPageIds() {
        return this.__getPages();
    }

    /**
     * Get the pages that match the given query. Does not include redirects, as they are only
     * pointers to real pages. Attention: may be running very slow, depending on the size of the
     * Wikipedia!
     *
     * @param query A query object containing the query conditions.
     * @return A set of pages that match the given query.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public Iterable<Page> getPages(PageQuery query) throws WikiApiException {
        return new PageQueryIterable(this, query);
    }

    /**
     * Get the number of pages that match the given query. The query is evaluated the same way as
     * in {@link #getPages(PageQuery)}, which means the same warning applies: it may be running very
     * slow, depending on the size of the Wikipedia!
     *
     * @param query A query object containing the query conditions.
     * @return The number of pages that match the given query.
     * @throws WikiApiException Thrown if errors occurred.
     */
    public int getNumberOfPages(PageQuery query) throws WikiApiException {
        return new PageQueryIterable(this, query).size();
    }

    /**
     * Get all articles (pages MINUS disambiguationPages MINUS redirects). Returns only an iterable,
     * as a collection may not fit into memory for a large wikipedia.
     *
     * @return An iterable of all article pages.
     */
    public Iterable<Page> getArticles() {
        return new PageIterable(this, true);
    }

    /**
     * Get all titles (including disambiguation pages and redirects). Returns only an iterable, as a
     * collection may not fit into memory for a large Wikipedia instance.
     *
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
     * Tests, whether a page or redirect with the given title exists. Trying to retrieve a page that
     * does not exist in Wikipedia throws an exception. You may catch the exception or use this
     * test, depending on your task.
     *
     * @param title The title of the page.
     * @return {@code True}, if a page or redirect with that title exits, {@code false} otherwise.
     */
    public boolean existsPage(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }

        Title t;
        try {
            t = new Title(title);
        } catch (WikiTitleParsingException e) {
            // The exception is used here as a control-flow signal only: a title that cannot be
            // parsed cannot denote an existing page, so there is nothing to chain or log.
            return false;
        }

        String encodedTitle = t.getWikiStyleTitle();

        // A name is not unique in PageMapLine: case variants of one title map to their own
        // entry, and a database whose charset cannot represent a title stores the substituted
        // characters, which lets unrelated titles collapse onto one name. So all entries found
        // are fetched, and one of them matching the title exactly is all it takes.
        List<String> names;
        try {
            names = __inTransaction(
                    session -> session.createNativeQuery(PAGE_NAMES_BY_NAME_QUERY, String.class)
                            .setParameter("pName", encodedTitle, String.class).list());
        } catch (JDBCException e) {
            if (isCollationMismatch(e)) {
                // the column cannot hold the title, so no entry can have it as its name
                return false;
            }
            throw e;
        }
        return names.contains(encodedTitle);
    }

    /**
     * Tests, whether a page with the given pageID exists. Trying to retrieve a pageID that does not
     * exist in Wikipedia throws an exception.
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

        // PageMapLine holds one entry per title the page id can be reached by, so a page that has
        // redirects carries several of them. One entry answers the question, see existsPage(String)
        // on why a unique result must not be asked for here.
        String sql = "select p.id from PageMapLine as p where p.pageID = :pageId";
        Long returnValue = __inTransaction(session -> session.createNativeQuery(sql, Long.class)
                .setParameter("pageId", pageID, Integer.class).setMaxResults(1)
                .uniqueResult());

        return returnValue != null;
    }

    /**
     * Get the hibernate ID to a given pageID of a page. We need different methods for pages and
     * categories here, as a page and a category can have the same ID.
     *
     * @param pageID A pageID that should be mapped to the corresponding hibernate ID.
     * @return The hibernateID of the page with pageID or -1, if the pageID is not valid
     */
    protected long __getPageHibernateId(int pageID) {
        String sql = "select page.id from Page as page where page.pageId = :pageId";
        Long hibernateID = __inTransaction(session -> session.createQuery(sql, Long.class)
                .setParameter("pageId", pageID, Integer.class).uniqueResult());
        return hibernateID != null ? hibernateID : -1;
    }

    /**
     * Get the hibernate ID to a given pageID of a category. We need different methods for pages and
     * categories here, as a page and a category can have the same ID.
     *
     * @param pageID A pageID that should be mapped to the corresponding hibernate ID.
     * @return The hibernateID of the page with pageID or -1, if the pageID is not valid
     */
    protected long __getCategoryHibernateId(int pageID) {
        String sql = "select cat.id from Category as cat where cat.pageId = :pageId";
        Long hibernateID = __inTransaction(session -> session.createQuery(sql, Long.class)
                .setParameter("pageId", pageID, Integer.class).uniqueResult());
        return hibernateID != null ? hibernateID : -1;
    }

    /**
     * @return A {@link MetaData} object containing all metadata about this instance of Wikipedia.
     */
    public MetaData getMetaData() {
        return this.metaData;
    }

    /**
     * Removes all entries from the cache of loaded categories of this instance, see
     * {@link DatabaseConfiguration#setCategoryCacheSize(int)}. This is only needed if the
     * underlying database is changed while this instance is in use, as the cache would otherwise
     * keep serving the old rows.
     */
    public void clearCategoryCache() {
        categoryCache.clear();
    }

    /**
     * @return The cache of loaded category rows of this instance, never {@code null}.
     */
    /*
     * Note well: Access is limited to package-private here intentionally, as it is API-internal use
     * only.
     */
    CategoryCache __getCategoryCache() {
        return categoryCache;
    }

    /**
     * @return The {@link DatabaseConfiguration} object that was used to create the Wikipedia
     * object.
     */
    public DatabaseConfiguration getDatabaseConfiguration() {
        return this.dbConfig;
    }

    /**
     * Returns the {@link org.dkpro.jwpl.api.hibernate.PageDAO} shared by all {@link Page} objects
     * of this instance. Like any DAO, it keeps using the SessionFactory the configuration mapped to
     * when it was created, even if the configuration is mutated afterwards.
     *
     * @return The shared {@link org.dkpro.jwpl.api.hibernate.PageDAO}, never {@code null}.
     */
    org.dkpro.jwpl.api.hibernate.PageDAO getPageDAO() {
        org.dkpro.jwpl.api.hibernate.PageDAO dao = pageDAO;
        if (dao == null) {
            // A racing thread may create a second, equivalent instance; either one is fine to keep.
            dao = new org.dkpro.jwpl.api.hibernate.PageDAO(this);
            pageDAO = dao;
        }
        return dao;
    }

    /**
     * Returns the {@link org.dkpro.jwpl.api.hibernate.CategoryDAO} shared by all {@link Category}
     * objects of this instance. Like any DAO, it keeps using the SessionFactory the configuration
     * mapped to when it was created, even if the configuration is mutated afterwards.
     *
     * @return The shared {@link org.dkpro.jwpl.api.hibernate.CategoryDAO}, never {@code null}.
     */
    org.dkpro.jwpl.api.hibernate.CategoryDAO getCategoryDAO() {
        org.dkpro.jwpl.api.hibernate.CategoryDAO dao = categoryDAO;
        if (dao == null) {
            // A racing thread may create a second, equivalent instance; either one is fine to keep.
            dao = new org.dkpro.jwpl.api.hibernate.CategoryDAO(this);
            categoryDAO = dao;
        }
        return dao;
    }

    /**
     * @return Shortcut for getting a hibernate session.
     */
    protected Session __getHibernateSession() {
        return WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
    }

    /**
     * Runs a unit of work inside a transaction on the session bound to the current thread.
     * <p>
     * This is the shortcut every database access in the API package goes through. Completing the
     * transaction on the failure paths as well is what keeps a failing query from leaving the
     * thread-bound session - and the JDBC connection it holds - behind for good; see
     * {@link WikiHibernateUtil#inTransaction(Session, Function)} for the full story.
     *
     * @param work The unit of work to run. Must not be {@code null}, and must not call back into an
     *             operation that opens a transaction of its own.
     * @param <T>  The type of the result of {@code work}.
     * @return Whatever {@code work} returned.
     */
    /**
     * Tells whether {@code e} was raised because a name compared in a query holds characters the
     * charset of the column cannot represent. No row can have such a name, so the lookups that
     * promise a not-found answer treat it as one.
     *
     * @param e The exception thrown by a query.
     * @return {@code true} if MySQL or MariaDB refused to compare the collations of the column
     *         and the parameter.
     */
    static boolean isCollationMismatch(JDBCException e) {
        SQLException sqlException = e.getSQLException();
        return sqlException != null
                && COLLATION_MISMATCH_ERRORS.contains(sqlException.getErrorCode());
    }

    protected <T> T __inTransaction(Function<Session, T> work) {
        return WikiHibernateUtil.inTransaction(__getHibernateSession(), work);
    }

    /**
     * The ID consists of the host, the database, and the language. This should be unique in most
     * cases.
     *
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

    /**
     * Removes the entry with the largest distance from {@code distanceMap}. Among several entries
     * with that distance, the first one in the iteration order of {@code distanceMap} is removed,
     * which is the entry a {@link java.util.TreeSet} ordering the entries by descending distance
     * alone would yield first.
     *
     * @param distanceMap    The distances by page ID.
     * @param distanceCounts The number of entries in {@code distanceMap} per distance value.
     * @param latestId       The page ID put into {@code distanceMap} last.
     * @param latestDistance The distance put into {@code distanceMap} last.
     */
    private static void removeLargestDistance(Map<Integer, Double> distanceMap,
            TreeMap<Double, Integer> distanceCounts, int latestId, double latestDistance) {
        Entry<Double, Integer> largest = distanceCounts.lastEntry();
        Integer victim = null;
        if (largest.getValue() == 1 && Double.compare(latestDistance, largest.getKey()) == 0) {
            // the latest entry is the only one with the largest distance
            victim = latestId;
        }
        else {
            for (Entry<Integer, Double> e : distanceMap.entrySet()) {
                if (Double.compare(e.getValue(), largest.getKey()) == 0) {
                    victim = e.getKey();
                    break;
                }
            }
        }
        distanceMap.remove(victim);
        decrementDistanceCount(distanceCounts, largest.getKey());
    }

    private static void decrementDistanceCount(TreeMap<Double, Integer> distanceCounts,
            Double distance) {
        distanceCounts.computeIfPresent(distance, (d, count) -> count == 1 ? null : count - 1);
    }

    /**
     * Selects the fetch size for streaming a result set through
     * {@link Query#getResultStream()}, which Hibernate executes as a
     * {@link java.sql.ResultSet#TYPE_FORWARD_ONLY}, {@link java.sql.ResultSet#CONCUR_READ_ONLY}
     * result set, within the transaction of {@link #__inTransaction(Function)} and thus with
     * autocommit disabled.
     *
     * @param databaseDriver The fully qualified class name of the JDBC driver, may be {@code null}.
     * @return {@link #MYSQL_STREAMING_FETCH_SIZE} for MySQL Connector/J ({@code com.mysql.*}),
     * {@link #MARIADB_STREAMING_FETCH_SIZE} for MariaDB Connector/J ({@code org.mariadb.*}),
     * {@link #POSTGRESQL_STREAMING_FETCH_SIZE} for pgJDBC ({@code org.postgresql.*}), else
     * {@link #DEFAULT_STREAMING_FETCH_SIZE}. The driver decides, not the server: MySQL Connector/J
     * connected to a MariaDB server needs {@link #MYSQL_STREAMING_FETCH_SIZE}.
     */
    static int streamingFetchSize(String databaseDriver) {
        if (databaseDriver == null) {
            return DEFAULT_STREAMING_FETCH_SIZE;
        }
        if (databaseDriver.startsWith("com.mysql.")) {
            return MYSQL_STREAMING_FETCH_SIZE;
        }
        if (databaseDriver.startsWith("org.mariadb.")) {
            return MARIADB_STREAMING_FETCH_SIZE;
        }
        if (databaseDriver.startsWith("org.postgresql.")) {
            return POSTGRESQL_STREAMING_FETCH_SIZE;
        }
        return DEFAULT_STREAMING_FETCH_SIZE;
    }

    private record PageTuple(int id, String name) {

    }
}
