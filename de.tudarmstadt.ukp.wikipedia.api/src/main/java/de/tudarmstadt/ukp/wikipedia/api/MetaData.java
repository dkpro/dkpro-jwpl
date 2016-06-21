/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * Provides access to meta data about a certain instance of Wikipedia.
 *
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
