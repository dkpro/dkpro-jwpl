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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Data access object for class Page
 * @see de.tudarmstadt.ukp.wikipedia.api.Page
 * @author Hibernate Tools
 */
public class CategoryDAO {

	private final Log logger = LogFactory.getLog(getClass());

    private static final String CATEGORY_CLASS = "de.tudarmstadt.ukp.wikipedia.api.hibernate.Category";

    private final SessionFactory sessionFactory;

    private Wikipedia wiki;

    public CategoryDAO(Wikipedia pWiki) {
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

    public void persist(Category transientInstance) {
        logger.debug("persisting Category instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            logger.debug("persist successful");
        } catch (RuntimeException re) {
            logger.error("persist failed", re);
            throw re;
        }
    }

    public void attachDirty(Category instance) {
        logger.debug("attaching dirty Category instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void attachClean(Category instance) {
        logger.debug("attaching clean Category instance");
        try {
//            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            sessionFactory.getCurrentSession().buildLockRequest(LockOptions.NONE).lock(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    public void delete(Category persistentInstance) {
        logger.debug("deleting Category instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    public Category merge(Category detachedInstance) {
        logger.debug("merging Category instance");
        try {
            Category result = (Category) sessionFactory.getCurrentSession().merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    public Category findById(java.lang.Long id) {
        logger.debug("getting Category instance with id: " + id);
        try {
            Category instance = (Category) sessionFactory.getCurrentSession().get(CATEGORY_CLASS, id);
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
