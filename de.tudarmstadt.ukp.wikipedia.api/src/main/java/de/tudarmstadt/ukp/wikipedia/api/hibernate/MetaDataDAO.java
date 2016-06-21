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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Home object for class Page
 * @see de.tudarmstadt.ukp.wikipedia.api.Page
 * @author Hibernate Tools
 */
public class MetaDataDAO implements WikiConstants {

	private final Log logger = LogFactory.getLog(getClass());

    private static final String META_DATA_CLASS = "de.tudarmstadt.ukp.wikipedia.api.hibernate.Category";

    private final SessionFactory sessionFactory;

    private Wikipedia wiki;

    public MetaDataDAO(Wikipedia wiki) {
        this.wiki = wiki;
        sessionFactory = getSessionFactory();
    }

    protected SessionFactory getSessionFactory() {
        try {
            logger.info("Using language: " + wiki.getLanguage());
            return WikiHibernateUtil.getSessionFactory(wiki.getDatabaseConfiguration());
        } catch (Exception e) {
            logger.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI", e);
        }
    }

    public void persist(MetaData transientInstance) {
        logger.debug("persisting MetaData instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            logger.debug("persist successful");
        } catch (RuntimeException re) {
            logger.error("persist failed", re);
            throw re;
        }
    }

    public void attachDirty(MetaData instance) {
        logger.debug("attaching dirty MetaData instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void attachClean(MetaData instance) {
        logger.debug("attaching clean MetaData instance");
        try {
//            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            sessionFactory.getCurrentSession().buildLockRequest(LockOptions.NONE).lock(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void delete(MetaData persistentInstance) {
        logger.debug("deleting MetaData instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    public MetaData merge(MetaData detachedInstance) {
        logger.debug("merging MetaData instance");
        try {
            MetaData result = (MetaData) sessionFactory.getCurrentSession().merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    public MetaData findById(java.lang.Long id) {
        logger.debug("getting MetaData instance with id: " + id);
        try {
            MetaData instance = (MetaData) sessionFactory.getCurrentSession().get(META_DATA_CLASS, id);
            if (instance == null) {
                logger.debug("get successful, no instance found");
            } else {
                logger.debug("get successful, instance found");
            }
            return instance;
        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }
}
