/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing (UKP) Lab
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * This class gives access to the additional information created by
 * the TemplateInfoGenerator.
 *
 * @author Oliver Ferschke
 */
public class WikipediaTemplateInfo {

	private final Log logger = LogFactory.getLog(getClass());

    private final Wikipedia wiki;

    /**
     */
    public WikipediaTemplateInfo(Wikipedia pWiki) throws WikiApiException {
        this.wiki = pWiki;
		if (!tableExists(WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID)
				|| !tableExists(WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME)) {
			throw new WikiApiException(
					"Missing tables. Please use the WikipediaTemplateInfoGenerator to generate the template data.");
		}
    }


    /**
     * Returns the number of all pages that contain a template the name
     * of which starts with the given String.
     *
     * @param templateFragment the beginning of the template has to match this String
     * @return the number of pages that contain a template starting with templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Integer countPagesWithTemplateFragment(String templateFragment) throws WikiApiException{
    	templateFragment=templateFragment.toLowerCase();

    	Session session = wiki.__getHibernateSession();
        session.beginTransaction();

        Query query = session.createQuery("SELECT distinct(count(*)) FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" as tpl, "+WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+" AS p WHERE tpl.templateName LIKE ? AND tpl.templateId = p.templateId");
        query.setString(0, templateFragment+"%");

        Integer count = (Integer)query.uniqueResult();

        session.getTransaction().commit();

        return count;

    }

    /**
     * Returns the numbner of all pages that contain a template the name
     * of which equals the given String.
     *
     * @param templateName the beginning of the template has to match this String
     * @return the number of pages that contain a template starting with templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Integer countPagesWithTemplateName(String templateName) throws WikiApiException{
    	templateName=templateName.toLowerCase();

    	Session session = wiki.__getHibernateSession();
        session.beginTransaction();

        Query query = session.createQuery("SELECT distinct(count(*)) FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" as tpl, "+WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+" AS p WHERE tpl.templateName = ? AND tpl.templateId = p.templateId");
        query.setString(0, templateName);

        Integer count = (Integer)query.uniqueResult();

        session.getTransaction().commit();

        return count;

    }


    /**
     * Return an iterable containing all pages that contain template the name
     * of which starts with the given String.
     *
     * @param templateFragment the beginning of the template has to match this String
     * @return An iterable with the page objects that contain templates beginning with the templateFragment
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Iterable<Page> getPagesWithTemplateFragment(String templateFragment) throws WikiApiException{
    	templateFragment=templateFragment.toLowerCase();

    	Session session = wiki.__getHibernateSession();
        session.beginTransaction();

        List<Page> matchedPages = new LinkedList<Page>();

        Query query = session.createQuery("SELECT p.pageId FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" as tpl, "+WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+" AS p WHERE tpl.templateName LIKE ? AND tpl.templateId = p.templateId");
        query.setString(0, templateFragment+"%");
        Iterator results = query.list().iterator();

        session.getTransaction().commit();

        while (results.hasNext()) {
            int pageID = (Integer) results.next();
            matchedPages.add(wiki.getPage(pageID));
        }

        return matchedPages;

    }

    /**
     * Return an iterable containing all pages that contain template the name
     * of which starts with the given String.
     *
     * @param templateName the name of the template that we want to match
     * @return An iterable with the page objects that contain the specified template
     * @throws WikiApiException If there was any error retrieving the page object (most likely if the template templates are corrupted)
     */
    public Iterable<Page> getPagesWithTemplateName(String templateName) throws WikiApiException{
    	templateName=templateName.toLowerCase();

    	Session session = wiki.__getHibernateSession();
        session.beginTransaction();

        List<Page> matchedPages = new LinkedList<Page>();

        Query query = session.createQuery("SELECT p.pageId FROM "+WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME+" as tpl, "+WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID+" AS p WHERE tpl.templateName = ? AND tpl.templateId = p.templateId");
        query.setString(0, templateName);
        Iterator results = query.list().iterator();

        session.getTransaction().commit();

        while (results.hasNext()) {
            int pageID = (Integer) results.next();
            matchedPages.add(wiki.getPage(pageID));
        }

        return matchedPages;

    }


	/**
	 * Checks if a specific table exists
	 *
	 * @param table
	 *            the table to check

	 * @return true, if table exists, false else
	 * @throws SQLException
	 *             if an error occurs connecting to or querying the db
	 */
	private boolean tableExists(String table)
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();

		Iterator results = null;
		Query query = session.createQuery("SHOW TABLES;");
		results = query.list().iterator();
		session.getTransaction().commit();

		if (results == null) {
			return false;
		}
		boolean found = false;
		while (results.hasNext()) {
			String curTable = (String) results.next();
			if (table.equalsIgnoreCase(curTable)) {
				found = true;
			}
		}
		return found;

	}




}
