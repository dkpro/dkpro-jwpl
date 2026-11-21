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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.util.ApiUtilities;
import org.dkpro.jwpl.api.util.StringUtils;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An iterable over {@link Page} objects selected by a query.
 */
public class PageQueryIterable
    implements Iterable<Page>
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final Wikipedia wiki;
    private final List<Integer> pageIdList;

    /**
     * Instantiates a new {@link PageQueryIterable} via a {@link PageQuery}.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param q The {@link PageQuery} to process. Must not be {@code null}.
     *
     * @throws WikiApiException Thrown if errors occurred.
     */
    public PageQueryIterable(Wikipedia wiki, PageQuery q) throws WikiApiException
    {

        this.wiki = wiki;
        this.pageIdList = new ArrayList<>();

        normalizeRanges(q);
        final boolean checkRedirects = isConstrained(q.getMinRedirects(), q.getMaxRedirects());
        final boolean checkCategories = isConstrained(q.getMinCategories(),
                q.getMaxCategories());
        final boolean checkTokens = isConstrained(q.getMinTokens(), q.getMaxTokens());

        // get a list with all pageIDs of the pages conforming with the constraints that can be
        // evaluated in the database
        final String sql = "select p.pageId " + buildFromAndWhereClause(q);
        List<Integer> idList = wiki.__inTransaction(session -> {
            NativeQuery<Integer> query = session.createNativeQuery(sql, Integer.class);
            bindParameters(query, q);
            return query.list();
        });

        // shortcut to fasten queries that do not have constraints that require loading the pages
        if (!checkRedirects && !checkCategories && !checkTokens) {
            pageIdList.addAll(idList);
            logger.info("Query selected {} pages.", pageIdList.size());
            return;
        }

        final String progressMessage = "searching " + idList.size() + " pages ... ";
        int progress = 0;
        for (Integer pageID : idList) {
            progress++;
            ApiUtilities.printProgressInfo(progress, idList.size(), 100,
                    ApiUtilities.ProgressInfoMode.TEXT, progressMessage);

            Page page;
            try {
                page = wiki.getPage(pageID);
            }
            catch (WikiPageNotFoundException e) {
                logger.warn("Page with pageID {} could not be found, skipping.", pageID, e);
                continue;
            }

            if (checkRedirects && !isInRange(page.getRedirects().size(), q.getMinRedirects(),
                    q.getMaxRedirects())) {
                continue;
            }

            if (checkCategories && !isInRange(page.getCategories().size(),
                    q.getMinCategories(), q.getMaxCategories())) {
                continue;
            }

            // parsing the page is the most expensive check, so it is done last
            if (checkTokens && !isInRange(page.getPlainText().split(" ").length,
                    q.getMinTokens(), q.getMaxTokens())) {
                continue;
            }

            // if still here, add page
            pageIdList.add(pageID);
        } // for
        logger.info("Query selected {} pages.", pageIdList.size());
    }

    /**
     * Counts the pages matching a {@link PageQuery}. If the query only has constraints that can be
     * evaluated in the database, the pages are counted there without selecting or loading them.
     * Otherwise, the query is evaluated as in {@link #PageQueryIterable(Wikipedia, PageQuery)}.
     *
     * @param wiki A valid, full initialized {@link Wikipedia} instance. Must not be {@code null}.
     * @param q The {@link PageQuery} to process. Must not be {@code null}.
     * @return The number of pages that match the query.
     * @throws WikiApiException Thrown if errors occurred.
     */
    static int countPages(Wikipedia wiki, PageQuery q) throws WikiApiException
    {
        normalizeRanges(q);
        if (isConstrained(q.getMinRedirects(), q.getMaxRedirects())
                || isConstrained(q.getMinCategories(), q.getMaxCategories())
                || isConstrained(q.getMinTokens(), q.getMaxTokens())) {
            return new PageQueryIterable(wiki, q).size();
        }

        final String sql = "select count(p.pageId) " + buildFromAndWhereClause(q);
        Long count = wiki.__inTransaction(session -> {
            NativeQuery<Long> query = session.createNativeQuery(sql, Long.class);
            bindParameters(query, q);
            return query.uniqueResult();
        });
        return count == null ? 0 : count.intValue();
    }

    /**
     * Builds the part of the SQL query that selects the pages conforming with the constraints of
     * the query that can be evaluated in the database. The in- and outlinks are counted in the
     * same way as in {@link Page#getNumberOfInlinks()} and {@link Page#getNumberOfOutlinks()}.
     */
    private static String buildFromAndWhereClause(PageQuery q)
    {
        List<String> conditions = new ArrayList<>();
        if (q.onlyDisambiguationPages()) {
            conditions.add("p.isDisambiguation = true");
        }
        if (q.onlyArticlePages()) {
            conditions.add("p.isDisambiguation = false");
        }
        if (hasTitlePattern(q)) {
            conditions.add("p.name like :name");
        }

        String inlinks = "(select count(pi.inLinks) from page_inlinks pi where pi.id = p.id)";
        if (q.getMinIndegree() > 0) {
            conditions.add(inlinks + " >= :minIndegree");
        }
        if (q.getMaxIndegree() < Integer.MAX_VALUE) {
            conditions.add(inlinks + " <= :maxIndegree");
        }

        String outlinks = "(select count(po.outLinks) from page_outlinks po where po.id = p.id)";
        if (q.getMinOutdegree() > 0) {
            conditions.add(outlinks + " >= :minOutdegree");
        }
        if (q.getMaxOutdegree() < Integer.MAX_VALUE) {
            conditions.add(outlinks + " <= :maxOutdegree");
        }

        String clause = "from Page p";
        if (!conditions.isEmpty()) {
            clause += " where " + StringUtils.join(conditions, " and ");
        }
        return clause;
    }

    private static void bindParameters(NativeQuery<?> query, PageQuery q)
    {
        if (hasTitlePattern(q)) {
            query.setParameter("name", q.getTitlePattern());
        }
        if (q.getMinIndegree() > 0) {
            query.setParameter("minIndegree", q.getMinIndegree());
        }
        if (q.getMaxIndegree() < Integer.MAX_VALUE) {
            query.setParameter("maxIndegree", q.getMaxIndegree());
        }
        if (q.getMinOutdegree() > 0) {
            query.setParameter("minOutdegree", q.getMinOutdegree());
        }
        if (q.getMaxOutdegree() < Integer.MAX_VALUE) {
            query.setParameter("maxOutdegree", q.getMaxOutdegree());
        }
    }

    private static boolean hasTitlePattern(PageQuery q)
    {
        return q.getTitlePattern() != null && !q.getTitlePattern().isBlank();
    }

    /**
     * Resets every invalid range of the query to its unconstrained default.
     */
    private static void normalizeRanges(PageQuery q)
    {
        if (!isValidRange(q.getMinIndegree(), q.getMaxIndegree())) {
            q.setMinIndegree(0);
            q.setMaxIndegree(Integer.MAX_VALUE);
        }

        if (!isValidRange(q.getMinOutdegree(), q.getMaxOutdegree())) {
            q.setMinOutdegree(0);
            q.setMaxOutdegree(Integer.MAX_VALUE);
        }

        if (!isValidRange(q.getMinRedirects(), q.getMaxRedirects())) {
            q.setMinRedirects(0);
            q.setMaxRedirects(Integer.MAX_VALUE);
        }

        if (!isValidRange(q.getMinCategories(), q.getMaxCategories())) {
            q.setMinCategories(0);
            q.setMaxCategories(Integer.MAX_VALUE);
        }

        if (!isValidRange(q.getMinTokens(), q.getMaxTokens())) {
            q.setMinTokens(0);
            q.setMaxTokens(Integer.MAX_VALUE);
        }
    }

    private static boolean isValidRange(int min, int max)
    {
        return min >= 0 && max >= 0 && min <= max;
    }

    private static boolean isConstrained(int min, int max)
    {
        return min > 0 || max < Integer.MAX_VALUE;
    }

    private static boolean isInRange(int value, int min, int max)
    {
        return value >= min && value <= max;
    }

    @Override
    public Iterator<Page> iterator()
    {
        return new PageQueryIterator(wiki, pageIdList);
    }

    /**
     * The pages matching the query are selected when this object is created, so their number is
     * known without iterating over them.
     *
     * @return The number of pages that match the query.
     */
    public int size()
    {
        return pageIdList.size();
    }
}
