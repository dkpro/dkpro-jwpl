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
package de.tudarmstadt.ukp.wikipedia.api;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * Provides access to meta data about a certain instance of Wikipedia.
 */
public class MetaData implements WikiConstants
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
	 * @return The id of the {@link MetaData} object.
	 */
	/*
	 * Note well:
	 * Access is limited to package-private here intentionally, as the database ID is considered framework-internal use.
	 */
	long getId()
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		long id = hibernateMetaData.getId();
		session.getTransaction().commit();
		return id;
	}

	/**
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
	 * @return The number of disambiguation pages in the current Wikipedia.
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
	 * @return The disambiguation {@link Category}.
	 * @throws WikiApiException Thrown if errors occurred fetching the information.
	 */
	public Category getDisambiguationCategory() throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String disambCategoryTitle = hibernateMetaData.getDisambiguationCategory();
		session.getTransaction().commit();
		return wiki.getCategory(disambCategoryTitle);
	}

	/**
	 * @return The name of the main/root {@link Category}.
	 * @throws WikiApiException Thrown if errors occurred fetching the information.
	 */
	public Category getMainCategory() throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String mainCategoryTitle = hibernateMetaData.getMainCategory();
		session.getTransaction().commit();
		return wiki.getCategory(mainCategoryTitle);
	}

	/**
	 * @return The version of the wikipedia data.
	 * @throws WikiApiException Thrown if errors occurred fetching the information.
	 */
	public String getVersion() throws WikiApiException
	{
		Session session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		session.lock(hibernateMetaData, LockMode.NONE);
		String version = hibernateMetaData.getVersion();
		session.getTransaction().commit();
		return version;
	}

	/**
	 * @return The language of this wikipedia.
	 */
	public Language getLanguage()
	{
		return wiki.getLanguage();
	}
}
