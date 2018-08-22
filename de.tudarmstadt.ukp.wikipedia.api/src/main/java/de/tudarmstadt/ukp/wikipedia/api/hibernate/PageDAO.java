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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Data access object for class {@link Page}.
 * 
 * @see de.tudarmstadt.ukp.wikipedia.api.Page
 * @author Hibernate Tools
 */
public class PageDAO extends GenericDAO<Page> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
