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

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Home object for class Page
 * @see de.tudarmstadt.ukp.wikipedia.api.Page
 * @author Hibernate Tools
 */
public class PageDAO {

	private final Log logger = LogFactory.getLog(getClass());

    private static final String PAGE_CLASS = "de.tudarmstadt.ukp.wikipedia.api.hibernate.Page";

    private final SessionFactory sessionFactory;

    private Wikipedia wiki;

    public PageDAO(Wikipedia pWiki) {
        this.wiki = pWiki;
        sessionFactory = getSessionFactory();
    }

    protected SessionFactory getSessionFactory() {
        try {
            return WikiHibernateUtil.getSessionFactory(wiki.getDatabaseConfiguration());
        } catch (Exception e) {
            logger.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }

    public void persist(Page transientInstance) {
        logger.debug("persisting Page instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            logger.debug("persist successful");
        } catch (RuntimeException re) {
            logger.error("persist failed", re);
            throw re;
        }
    }

    public void attachDirty(Page instance) {
        logger.debug("attaching dirty Page instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void attachClean(Page instance) {
        logger.debug("attaching clean Page instance");
        try {
//            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            sessionFactory.getCurrentSession().buildLockRequest(LockOptions.NONE).lock(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void delete(Page persistentInstance) {
        logger.debug("deleting Page instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    public Page merge(Page detachedInstance) {
        logger.debug("merging Page instance");
        try {
            Page result = (Page) sessionFactory.getCurrentSession().merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    public Page findById(java.lang.Long id) {
        logger.debug("getting Page instance with id: " + id);
        try {
            Page instance = (Page) sessionFactory.getCurrentSession().get(PAGE_CLASS, id);
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
