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

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * Provides access to meta data about a certain instance of Wikipedia.
 *
 * @author zesch
 *
 */
public class MetaData
	implements WikiConstants
{
	// private MetaDataDAO metaDAO;
	private de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData hibernateMetaData;
	private Wikipedia wiki;

	/**
	 * Creates a meta data object.
	 */
	protected MetaData(Wikipedia wiki)
	{
		this.wiki = wiki;
		// this.metaDAO = new MetaDataDAO(wiki);
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		hibernateMetaData = (de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData) session
				.createQuery("from MetaData").uniqueResult();
		session.getTransaction().commit();
	};

	/**
	 * Returns the id of the MetaData object.
	 *
	 * @return The id of the MetaData object.
	 */
	public long getId()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long id = hibernateMetaData.getId();
		session.getTransaction().commit();
		return id;
	}

	/**
	 * Returns the number of categories in the current Wikipedia.
	 *
	 * @return The number of categories in the current Wikipedia.
	 */
	public long getNumberOfCategories()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long nrofCategories = hibernateMetaData.getNrofCategories();
		session.getTransaction().commit();
		return nrofCategories;
	}

	/**
	 * Returns the number of pages in the current Wikipedia.
	 *
	 * @return The number of pages in the current Wikipedia.
	 */
	public long getNumberOfPages()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long nrofPages = hibernateMetaData.getNrofPages();
		session.getTransaction().commit();
		return nrofPages;
	}

	/**
	 * Returns the number of disambituation pages in the current Wikipedia.
	 *
	 * @return The number of disambituation pages in the current Wikipedia.
	 */
	public long getNumberOfDisambiguationPages()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long nrofDisambPages = hibernateMetaData.getNrofDisambiguationPages();
		session.getTransaction().commit();
		return nrofDisambPages;
	}

	/**
	 * Returns the number of redirects in the current Wikipedia.
	 *
	 * @return The number of redirects in the current Wikipedia.
	 */
	public long getNumberOfRedirectPages()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long nrofRedirects = hibernateMetaData.getNrofRedirects();
		session.getTransaction().commit();
		return nrofRedirects;
	}

	/**
	 * Returns the disambiguation category.
	 *
	 * @return The disambiguation category.
	 * @throws WikiApiException
	 */
	public Category getDisambiguationCategory()
		throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String disambCategoryTitle = hibernateMetaData.getDisambiguationCategory();
		session.getTransaction().commit();
		Category disambCategory = wiki.getCategory(disambCategoryTitle);
		return disambCategory;
	}

	/**
	 * Returns the name of the main/root category.
	 *
	 * @return The name of the main/root category.
	 * @throws WikiApiException
	 */
	public Category getMainCategory()
		throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String mainCategoryTitle = hibernateMetaData.getMainCategory();
		session.getTransaction().commit();
		Category mainCategory = wiki.getCategory(mainCategoryTitle);
		return mainCategory;
	}

	/**
	 * Returns the version of the wikipedia data.
	 *
	 * @return The version of the wikipedia data.
	 * @throws WikiApiException
	 */
	public String getVersion()
		throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String version = hibernateMetaData.getVersion();
		session.getTransaction().commit();
		return version;
	}

	/**
	 * Returns the language of this wikipedia.
	 *
	 * @return The language of this wikipedia.
	 */
	public Language getLanguage()
	{
		return wiki.getLanguage();
	}
}
