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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.util.ApiUtilities;
import de.tudarmstadt.ukp.wikipedia.util.StringUtils;


/**
 * An iterable over page objects selected by a query.
 * @author zesch
 *
 */
public class PageQueryIterable implements Iterable<Page> {

	private final Log logger = LogFactory.getLog(getClass());

    private Wikipedia wiki;
    private List<Integer> pageIdList;

    public PageQueryIterable(Wikipedia wiki, PageQuery query) throws WikiApiException {

        this.wiki = wiki;
        this.pageIdList = new ArrayList<Integer>();

        // get a list with all pageIDs of the pages conforming with the query
        //TODO change this to a hibernate criteria query
        String hql = "select p.pageId from Page as p ";
        List<String> conditions = new ArrayList<String>();
        if (query.onlyDisambiguationPages()) {
            conditions.add("p.isDisambiguation = 1");
        }
        if (query.onlyArticlePages()) {
            conditions.add("p.isDisambiguation = 0");
        }
        if (query.getTitlePattern() != "") {
            conditions.add("p.name like '" + query.getTitlePattern() + "'");
        }

        String conditionString = StringUtils.join(conditions, " AND ");
        if (conditionString.length() > 0) {
            hql += "where " + conditionString;
        }

        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        List<Integer> idList = session.createQuery(hql).list();
        session.getTransaction().commit();

        int progress = 0;
        for (Integer pageID : idList) {
            progress++;
            ApiUtilities.printProgressInfo(progress, idList.size(), 100, ApiUtilities.ProgressInfoMode.TEXT, "searching " + idList.size() + " pages ... ");

            // shortcut to fasten queries that do not have such constraints
            if (query.getMaxCategories() == Integer.MAX_VALUE &&
                query.getMaxIndegree() == Integer.MAX_VALUE &&
                query.getMaxOutdegree() == Integer.MAX_VALUE &&
                query.getMaxRedirects() == Integer.MAX_VALUE &&
                query.getMaxTokens() == Integer.MAX_VALUE &&
                query.getMinCategories() == 0 &&
                query.getMinIndegree() == 0 &&
                query.getMinOutdegree() == 0 &&
                query.getMinRedirects() == 0 &&
                query.getMinTokens() == 0)
            {
                pageIdList.add(pageID);
                continue;
            }

            Page page = null;
            try {
                page = wiki.getPage(pageID);
            } catch (WikiPageNotFoundException e) {
                logger.error("Page with pageID " + pageID + " could not be found. Fatal error. Terminating.");
                e.printStackTrace();
                System.exit(1);
            }

            String[] tokens = page.getPlainText().split(" ");

            if (!(query.getMinIndegree() >= 0 &&
                query.getMaxIndegree() >= 0 &&
                query.getMinIndegree() <= query.getMaxIndegree())) {

                query.setMinIndegree(0);
                query.setMaxIndegree(Integer.MAX_VALUE);
            }

            if (!(query.getMinOutdegree() >= 0 &&
                    query.getMaxOutdegree() >= 0 &&
                    query.getMinOutdegree() <= query.getMaxOutdegree())) {

                query.setMinOutdegree(0);
                query.setMaxOutdegree(Integer.MAX_VALUE);
            }

            if (!(query.getMinRedirects() >= 0 &&
                    query.getMaxRedirects() >= 0 &&
                    query.getMinRedirects() <= query.getMaxRedirects())) {

                query.setMinRedirects(0);
                query.setMaxRedirects(Integer.MAX_VALUE);
            }

            if (!(query.getMinCategories() >= 0 &&
                    query.getMaxCategories() >= 0 &&
                    query.getMinCategories() <= query.getMaxCategories())) {

                query.setMinCategories(0);
                query.setMaxCategories(Integer.MAX_VALUE);
            }

            if (!(query.getMinCategories() >= 0 &&
                    query.getMaxCategories() >= 0 &&
                    query.getMinCategories() <= query.getMaxCategories())) {

                query.setMinCategories(0);
                query.setMaxCategories(Integer.MAX_VALUE);
            }

            if (!(query.getMinTokens() >= 0 &&
                    query.getMaxTokens() >= 0 &&
                    query.getMinTokens() <= query.getMaxTokens())) {

                query.setMinTokens(0);
                query.setMaxTokens(Integer.MAX_VALUE);
            }


            int inlinkSize = page.getNumberOfInlinks();
            if (inlinkSize < query.getMinIndegree() ||
                inlinkSize > query.getMaxIndegree()) {
                continue;
            }

            int outlinkSize = page.getNumberOfOutlinks();
            if (outlinkSize < query.getMinOutdegree() ||
                outlinkSize > query.getMaxOutdegree()) {
                continue;
            }
            if (page.getRedirects().size() < query.getMinRedirects() ||
                page.getRedirects().size() > query.getMaxRedirects()) {
                continue;
            }

            int categoriesSize = page.getCategories().size();
            if (categoriesSize < query.getMinCategories() ||
                categoriesSize > query.getMaxCategories()) {
                continue;
            }
            if (tokens.length < query.getMinTokens() ||
                tokens.length > query.getMaxTokens()) {
                continue;
            }

            // if still here, add page
            pageIdList.add(pageID);
        } // for
        logger.info("Query selected " + pageIdList.size() + " pages.");
    }

    public Iterator<Page> iterator() {
        return new PageQueryIterator(wiki, pageIdList);
    }
}


