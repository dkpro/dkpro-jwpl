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
package de.tudarmstadt.ukp.wikipedia.api.hibernate;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * A common base class for DAO classes.
 * 
 * @param <T> The entity type to provide persistence features for.
 */
public abstract class GenericDAO<T> {

    private final Log logger = LogFactory.getLog(getClass());
    
    private final Wikipedia wiki;
    private final SessionFactory sessionFactory;

    private final String entityClass;

    GenericDAO(Wikipedia wiki, Class entityClass) {
        this.wiki = wiki;
        this.entityClass = entityClass.getName();
        this.sessionFactory = initializeSessionFactory();
    }

    private SessionFactory initializeSessionFactory() {
        try {
            return WikiHibernateUtil.getSessionFactory(wiki.getDatabaseConfiguration());
        } catch (Exception e) {
            throw new IllegalStateException("Could not locate SessionFactory in JNDI", e);
        }
    }

    private SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected Session getSession() {
        return getSessionFactory().getCurrentSession();
    }

    public void persist(T transientInstance) {
        try {
            getSession().persist(transientInstance);
            logger.trace("persist successful");
        } catch (RuntimeException re) {
            logger.error("Failed persisting " + entityClass + " instance", re);
            throw re;
        }
    }

    public void delete(T persistentInstance) {
        try {
            getSession().delete(persistentInstance);
            logger.trace("delete successful");
        } catch (RuntimeException re) {
            logger.error("Failed deleting " + entityClass + " instance", re);
            throw re;
        }
    }

    public T merge(T detachedInstance) {
        try {
            T result = (T) getSession().merge(detachedInstance);
            logger.trace("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("Failed merging " + entityClass + " instance", re);
            throw re;
        }
    }

    public void attachClean(T instance) {
        try {
            getSession().buildLockRequest(LockOptions.NONE).lock(instance);
            logger.trace("attach successful");
        } catch (RuntimeException re) {
            logger.error("Failed attaching " + entityClass + " instance", re);
            throw re;
        }
    }

    public void attachDirty(T instance) {
        try {
            getSession().saveOrUpdate(instance);
            logger.trace("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }
    
    public T findById(Long id) {
        try {
            T instance = (T) getSession().get(entityClass, id);
            if (instance == null) {
                logger.trace("get successful, no " + entityClass + " instance found");
            } else {
                logger.trace("get successful, instance found");
            }
            return instance;
        } catch (RuntimeException re) {
            logger.error("Failed finding " + entityClass + " instance by id", re);
            throw re;
        }
    }
}
