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
package org.dkpro.jwpl.api.hibernate;

import java.lang.invoke.MethodHandles;

import org.dkpro.jwpl.api.Wikipedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data access object for class {@link Category}
 *
 * @see org.dkpro.jwpl.api.Category
 * @see org.dkpro.jwpl.api.hibernate.Category
 */
public class CategoryDAO
    extends GenericDAO<Category>
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Instantiates a {@link CategoryDAO}.
     *
     * @param wiki A valid {@link Wikipedia} instance. Must not be {@code null}.
     */
    public CategoryDAO(Wikipedia wiki)
    {
        super(wiki, Category.class);
    }

    @Override
    public void persist(Category transientInstance)
    {
        logger.debug("persisting Category instance");
        super.persist(transientInstance);
    }

    @Override
    public void attachDirty(Category instance)
    {
        logger.debug("attaching dirty Category instance");
        super.attachDirty(instance);
    }

    @Override
    public void attachClean(Category instance)
    {
        logger.debug("attaching clean Category instance");
        super.attachClean(instance);
    }

    @Override
    public void delete(Category persistentInstance)
    {
        logger.debug("deleting Category instance");
        super.delete(persistentInstance);
    }

    @Override
    public Category merge(Category detachedInstance)
    {
        logger.debug("merging Category instance");
        return super.merge(detachedInstance);
    }

    @Override
    public Category findById(java.lang.Long id)
    {
        logger.debug("getting Category instance with id: {}", id);
        return super.findById(id);
    }
}
