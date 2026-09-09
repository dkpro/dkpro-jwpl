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

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.hibernate.AbstractMetaData;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.LockMode;
import org.hibernate.Session;

import jakarta.persistence.criteria.CriteriaQuery;

/**
 * Provides access to meta-data about a certain {@link Wikipedia} instance.
 *
 * @see Wikipedia
 */
public class MetaData
    implements WikiConstants
{

    private final Wikipedia wiki;
    private final AbstractMetaData hibernateMetaData;

    /**
     * Instantiates a new {@link MetaData} object.
     *
     * @param wiki A valid {@link Wikipedia} reference. Must not be {@code null}.
     */
    protected MetaData(Wikipedia wiki)
    {
        this.wiki = wiki;
        /*
         * Note well: the entity to load is chosen by the schema probe in WikiHibernateUtil and can
         * be either the current or the legacy 'MetaData' mapping. Their entity names differ, hence
         * a criteria query rather than a literal HQL string.
         */
        Class<? extends AbstractMetaData> entityClass = WikiHibernateUtil
                .getMetaDataEntityClass(wiki.getDatabaseConfiguration());
        hibernateMetaData = wiki.__inTransaction(session -> loadMetaData(session, entityClass));
    }

    private static <T extends AbstractMetaData> T loadMetaData(Session session, Class<T> type)
    {
        CriteriaQuery<T> query = session.getCriteriaBuilder().createQuery(type);
        query.select(query.from(type));
        return session.createQuery(query).uniqueResult();
    }

    /**
     * @return The id of the {@link MetaData} object.
     */
    /*
     * Note well: Access is limited to package-private here intentionally, as the database ID is
     * considered framework-internal use.
     */
    long getId()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getId();
        });
    }

    /**
     * @return The number of categories in the current Wikipedia.
     */
    public long getNumberOfCategories()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getNrofCategories();
        });
    }

    /**
     * @return The number of pages in the current Wikipedia.
     */
    public long getNumberOfPages()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getNrofPages();
        });
    }

    /**
     * @return The number of disambiguation pages in the current Wikipedia.
     */
    public long getNumberOfDisambiguationPages()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getNrofDisambiguationPages();
        });
    }

    /**
     * @return The number of redirects in the current Wikipedia.
     */
    public long getNumberOfRedirectPages()
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getNrofRedirects();
        });
    }

    /**
     * @return The disambiguation {@link Category}.
     * @throws WikiApiException
     *             Thrown if errors occurred fetching the information.
     */
    public Category getDisambiguationCategory() throws WikiApiException
    {
        String disambCategoryTitle = wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getDisambiguationCategory();
        });
        return wiki.getCategory(disambCategoryTitle);
    }

    /**
     * @return The name of the main/root {@link Category}.
     * @throws WikiApiException
     *             Thrown if errors occurred fetching the information.
     */
    public Category getMainCategory() throws WikiApiException
    {
        String mainCategoryTitle = wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getMainCategory();
        });
        return wiki.getCategory(mainCategoryTitle);
    }

    /**
     * @return The version of the wikipedia data, or {@code null} if it is unknown. That is the case
     *         when the underlying database predates the {@code MetaData.version} column, and for
     *         DataMachine generated databases created before the DataMachine started writing it.
     *         See {@code dkpro-jwpl-api/README.md} on how to add the column to an existing
     *         database:
     *         {@code ALTER TABLE MetaData ADD COLUMN version VARCHAR(255) DEFAULT NULL;}
     * @throws WikiApiException
     *             Thrown if errors occurred fetching the information.
     */
    public String getVersion() throws WikiApiException
    {
        return wiki.__inTransaction(session -> {
            session.lock(hibernateMetaData, LockMode.NONE);
            return hibernateMetaData.getVersion();
        });
    }

    /**
     * @return The language of this wikipedia.
     */
    public Language getLanguage()
    {
        return wiki.getLanguage();
    }
}
