/*******************************************************************************
 * Copyright 2017
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

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Data access object for class {@link Page}.
 * 
 * @see de.tudarmstadt.ukp.wikipedia.api.Page
 * @author Hibernate Tools
 */
public class PageDAO extends GenericDAO<Page> {

	private final Log logger = LogFactory.getLog(getClass());

    public PageDAO(Wikipedia pWiki) {
        super(pWiki, Page.class);
    }

    @Override
    public void persist(Page transientInstance) {
        logger.debug("persisting Page instance");
        super.persist(transientInstance);
    }

    @Override
    public void attachDirty(Page instance) {
        logger.debug("attaching dirty Page instance");
        super.attachDirty(instance);
    }

    @Override
    public void attachClean(Page instance) {
        logger.debug("attaching clean Page instance");
        super.attachClean(instance);
    }

    @Override
    public void delete(Page persistentInstance) {
        logger.debug("deleting Page instance");
        super.delete(persistentInstance);
    }

    @Override
    public Page merge(Page detachedInstance) {
        logger.debug("merging Page instance");
        return super.merge(detachedInstance);
    }

    @Override
    public Page findById(java.lang.Long id) {
        logger.debug("getting Page instance with id: " + id);
        return super.findById(id);
    }
}
