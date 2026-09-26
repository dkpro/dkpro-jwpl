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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.hibernate.Session;

/**
 * The category rows and category links a {@link CategoryGraph} is built from, loaded with a few
 * bulk queries instead of several single-row queries per category.
 * <p>
 * The link sets handed out by {@link #getParentIDs(long)} and {@link #getChildrenIDs(long)} are
 * built the same way {@link Category#getParentIDs()} and {@link Category#getChildrenIDs()} build
 * them, so a graph built from this data gets its edges in the same order as one built from
 * {@link Category} objects. The edge order matters, as it decides which edges
 * {@link CycleHandler#removeCycles()} drops.
 */
final class CategoryGraphData
{

    /**
     * How the rows are read from the database.
     */
    enum LoadMode
    {
        /**
         * Reads the whole category and category link tables, one query per table.
         */
        FULL_SCAN,

        /**
         * Reads only the rows of the requested categories, with one query per table and chunk of
         * {@link #CHUNK_SIZE} categories.
         */
        CHUNKED
    }

    /**
     * The number of categories whose rows are requested by one query in {@link LoadMode#CHUNKED}.
     */
    static final int CHUNK_SIZE = 1_000;

    /**
     * Up to this number of requested categories, only their rows are read
     * ({@link LoadMode#CHUNKED}). Above it, the whole tables are read ({@link LoadMode#FULL_SCAN}),
     * which keeps the number of queries constant.
     */
    static final int CHUNKED_LOAD_LIMIT = 10 * CHUNK_SIZE;

    /**
     * The number of rows fetched per round trip while streaming the tables.
     */
    private static final int FETCH_SIZE = 10_000;

    private static final String CATEGORY_HQL = "select c.pageId, c.id from Category c";
    private static final String CATEGORY_WITH_NAMES_HQL = "select c.pageId, c.id, c.name"
            + " from Category c";
    private static final String BY_PAGE_IDS = " where c.pageId in (:ids)";

    private static final String INLINKS_SQL = "select id, inLinks from category_inlinks";
    private static final String OUTLINKS_SQL = "select id, outLinks from category_outlinks";
    private static final String BY_IDS = " where id in (:ids)";

    private final Map<Integer, Long> pageIdToId;
    private final Map<Integer, String> names;
    private final Map<Long, List<Integer>> inLinks;
    private final Map<Long, List<Integer>> outLinks;

    private CategoryGraphData(Map<Integer, Long> pageIdToId, Map<Integer, String> names,
            Map<Long, List<Integer>> inLinks, Map<Long, List<Integer>> outLinks)
    {
        this.pageIdToId = pageIdToId;
        this.names = names;
        this.inLinks = inLinks;
        this.outLinks = outLinks;
    }

    /**
     * Loads the data for the given categories, choosing the {@link LoadMode} by the number of
     * categories.
     *
     * @param wiki
     *            The Wikipedia to read from.
     * @param pageIds
     *            The page ids of the categories to load the data of.
     * @param withNames
     *            Whether the names of the categories are needed.
     * @return The loaded data. Page ids without a category are absent from it.
     */
    static CategoryGraphData load(Wikipedia wiki, Set<Integer> pageIds, boolean withNames)
    {
        LoadMode mode = pageIds.size() <= CHUNKED_LOAD_LIMIT ? LoadMode.CHUNKED
                : LoadMode.FULL_SCAN;
        return load(wiki, pageIds, withNames, mode);
    }

    /**
     * Loads the data for the given categories in one transaction.
     *
     * @param wiki
     *            The Wikipedia to read from.
     * @param pageIds
     *            The page ids of the categories to load the data of. In
     *            {@link LoadMode#FULL_SCAN}, the data of all categories is loaded.
     * @param withNames
     *            Whether the names of the categories are needed.
     * @param mode
     *            How to read the rows.
     * @return The loaded data. Page ids without a category are absent from it.
     */
    static CategoryGraphData load(Wikipedia wiki, Set<Integer> pageIds, boolean withNames,
            LoadMode mode)
    {
        return wiki.__inTransaction(session -> {
            Map<Integer, Long> pageIdToId = new HashMap<>();
            Map<Integer, String> names = withNames ? new HashMap<>() : null;
            Map<Long, List<Integer>> in = new HashMap<>();
            Map<Long, List<Integer>> out = new HashMap<>();
            String categoryHql = withNames ? CATEGORY_WITH_NAMES_HQL : CATEGORY_HQL;

            if (mode == LoadMode.FULL_SCAN) {
                readCategories(session, categoryHql, null, pageIdToId, names);
                readLinks(session, INLINKS_SQL, null, in);
                readLinks(session, OUTLINKS_SQL, null, out);
            }
            else {
                for (List<Integer> chunk : chunks(pageIds)) {
                    readCategories(session, categoryHql + BY_PAGE_IDS, chunk, pageIdToId, names);
                }
                for (List<Long> chunk : chunks(pageIdToId.values())) {
                    readLinks(session, INLINKS_SQL + BY_IDS, chunk, in);
                    readLinks(session, OUTLINKS_SQL + BY_IDS, chunk, out);
                }
            }
            return new CategoryGraphData(pageIdToId, names, in, out);
        });
    }

    private static void readCategories(Session session, String hql, List<Integer> pageIds,
            Map<Integer, Long> pageIdToId, Map<Integer, String> names)
    {
        var query = session.createQuery(hql, Object[].class).setReadOnly(true)
                .setFetchSize(FETCH_SIZE);
        if (pageIds != null) {
            query.setParameterList("ids", pageIds);
        }
        try (Stream<Object[]> rows = query.getResultStream()) {
            rows.forEach(row -> {
                int pageId = (Integer) row[0];
                pageIdToId.put(pageId, (Long) row[1]);
                if (names != null) {
                    names.put(pageId, (String) row[2]);
                }
            });
        }
    }

    private static void readLinks(Session session, String sql, List<Long> ids,
            Map<Long, List<Integer>> links)
    {
        var query = session.createNativeQuery(sql, Object[].class).setReadOnly(true)
                .setFetchSize(FETCH_SIZE);
        if (ids != null) {
            query.setParameterList("ids", ids);
        }
        try (Stream<Object[]> rows = query.getResultStream()) {
            rows.forEach(row -> {
                // a NULL link is skipped by the element collection mapping as well
                if (row[1] != null) {
                    links.computeIfAbsent(((Number) row[0]).longValue(), k -> new ArrayList<>())
                            .add(((Number) row[1]).intValue());
                }
            });
        }
    }

    private static <T> List<List<T>> chunks(Collection<T> values)
    {
        List<List<T>> chunks = new ArrayList<>();
        List<T> chunk = new ArrayList<>(CHUNK_SIZE);
        for (T value : values) {
            chunk.add(value);
            if (chunk.size() == CHUNK_SIZE) {
                chunks.add(chunk);
                chunk = new ArrayList<>(CHUNK_SIZE);
            }
        }
        if (!chunk.isEmpty()) {
            chunks.add(chunk);
        }
        return chunks;
    }

    /**
     * @param pageId
     *            The page id of a category.
     * @return The hibernate id of the category, or {@code null} if there is no such category.
     */
    Long getId(int pageId)
    {
        return pageIdToId.get(pageId);
    }

    /**
     * @param pageId
     *            The page id of a category.
     * @return The name of the category, or {@code null} if there is no such category or the names
     *         were not loaded.
     */
    String getName(int pageId)
    {
        return names == null ? null : names.get(pageId);
    }

    /**
     * @param id
     *            The hibernate id of a category.
     * @return A new, modifiable set with the page ids of the parents of the category.
     */
    Set<Integer> getParentIDs(long id)
    {
        return new HashSet<>(inLinks.getOrDefault(id, List.of()));
    }

    /**
     * @param id
     *            The hibernate id of a category.
     * @return A new, modifiable set with the page ids of the children of the category.
     */
    Set<Integer> getChildrenIDs(long id)
    {
        return new HashSet<>(outLinks.getOrDefault(id, List.of()));
    }
}
