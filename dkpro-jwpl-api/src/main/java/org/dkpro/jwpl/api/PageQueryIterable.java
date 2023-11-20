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
import org.hibernate.Session;
import org.hibernate.query.Query;
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

    public PageQueryIterable(Wikipedia wiki, PageQuery q) throws WikiApiException
    {

        this.wiki = wiki;
        this.pageIdList = new ArrayList<>();
        boolean hasTitlePattern = false;

        // get a list with all pageIDs of the pages conforming with the query
        String hql = "select p.pageId from Page as p ";
        List<String> conditions = new ArrayList<>();
        if (q.onlyDisambiguationPages()) {
            conditions.add("p.isDisambiguation = 1");
        }
        if (q.onlyArticlePages()) {
            conditions.add("p.isDisambiguation = 0");
        }
        if (q.getTitlePattern() != null && !q.getTitlePattern().isBlank()) {
            conditions.add("p.name like :name");
            hasTitlePattern = true;
        }

        String conditionString = StringUtils.join(conditions, " AND ");
        if (!conditionString.isEmpty()) {
            hql += "where " + conditionString;
        }

        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        Query<Integer> query = session.createQuery(hql, Integer.class);
        if (hasTitlePattern) {
            query.setParameter("name", q.getTitlePattern());
        }
        List<Integer> idList = query.list();
        session.getTransaction().commit();

        int progress = 0;
        for (Integer pageID : idList) {
            progress++;
            ApiUtilities.printProgressInfo(progress, idList.size(), 100,
                    ApiUtilities.ProgressInfoMode.TEXT,
                    "searching " + idList.size() + " pages ... ");

            // shortcut to fasten queries that do not have such constraints
            if (q.getMaxCategories() == Integer.MAX_VALUE && q.getMaxIndegree() == Integer.MAX_VALUE
                    && q.getMaxOutdegree() == Integer.MAX_VALUE
                    && q.getMaxRedirects() == Integer.MAX_VALUE
                    && q.getMaxTokens() == Integer.MAX_VALUE && q.getMinCategories() == 0
                    && q.getMinIndegree() == 0 && q.getMinOutdegree() == 0
                    && q.getMinRedirects() == 0 && q.getMinTokens() == 0) {
                pageIdList.add(pageID);
                continue;
            }

            Page page = null;
            try {
                page = wiki.getPage(pageID);
            }
            catch (WikiPageNotFoundException e) {
                logger.warn("Page with pageID {} could not be found. Fatal error. Terminating.",
                        pageID);
            }

            if (!(q.getMinIndegree() >= 0 && q.getMaxIndegree() >= 0
                    && q.getMinIndegree() <= q.getMaxIndegree())) {
                q.setMinIndegree(0);
                q.setMaxIndegree(Integer.MAX_VALUE);
            }

            if (!(q.getMinOutdegree() >= 0 && q.getMaxOutdegree() >= 0
                    && q.getMinOutdegree() <= q.getMaxOutdegree())) {
                q.setMinOutdegree(0);
                q.setMaxOutdegree(Integer.MAX_VALUE);
            }

            if (!(q.getMinRedirects() >= 0 && q.getMaxRedirects() >= 0
                    && q.getMinRedirects() <= q.getMaxRedirects())) {
                q.setMinRedirects(0);
                q.setMaxRedirects(Integer.MAX_VALUE);
            }

            if (!(q.getMinCategories() >= 0 && q.getMaxCategories() >= 0
                    && q.getMinCategories() <= q.getMaxCategories())) {
                q.setMinCategories(0);
                q.setMaxCategories(Integer.MAX_VALUE);
            }

            if (!(q.getMinCategories() >= 0 && q.getMaxCategories() >= 0
                    && q.getMinCategories() <= q.getMaxCategories())) {
                q.setMinCategories(0);
                q.setMaxCategories(Integer.MAX_VALUE);
            }

            if (!(q.getMinTokens() >= 0 && q.getMaxTokens() >= 0
                    && q.getMinTokens() <= q.getMaxTokens())) {
                q.setMinTokens(0);
                q.setMaxTokens(Integer.MAX_VALUE);
            }

            int inlinkSize = page.getNumberOfInlinks();
            if (inlinkSize < q.getMinIndegree() || inlinkSize > q.getMaxIndegree()) {
                continue;
            }

            int outlinkSize = page.getNumberOfOutlinks();
            if (outlinkSize < q.getMinOutdegree() || outlinkSize > q.getMaxOutdegree()) {
                continue;
            }
            if (page.getRedirects().size() < q.getMinRedirects()
                    || page.getRedirects().size() > q.getMaxRedirects()) {
                continue;
            }

            int categoriesSize = page.getCategories().size();
            if (categoriesSize < q.getMinCategories() || categoriesSize > q.getMaxCategories()) {
                continue;
            }

            String[] tokens = page.getPlainText().split(" ");
            if (tokens.length < q.getMinTokens() || tokens.length > q.getMaxTokens()) {
                continue;
            }

            // if still here, add page
            pageIdList.add(pageID);
        } // for
        logger.info("Query selected {} pages.", pageIdList.size());
    }

    @Override
    public Iterator<Page> iterator()
    {
        return new PageQueryIterator(wiki, pageIdList);
    }
}
