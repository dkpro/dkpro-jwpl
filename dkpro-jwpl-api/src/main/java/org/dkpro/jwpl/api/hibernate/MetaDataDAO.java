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

import org.dkpro.jwpl.api.WikiConstants;
import org.dkpro.jwpl.api.Wikipedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data access object for class {@link MetaData}.
 *
 * @see org.dkpro.jwpl.api.MetaData
 * @see org.dkpro.jwpl.api.hibernate.MetaData
 */
public class MetaDataDAO
    extends GenericDAO<MetaData>
    implements WikiConstants
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Instantiates a {@link MetaDataDAO}.
     *
     * @param wiki A valid {@link Wikipedia} instance. Must not be {@code null}.
     */
    public MetaDataDAO(Wikipedia wiki)
    {
        super(wiki, MetaData.class);
    }

    @Override
    public void persist(MetaData transientInstance)
    {
        logger.debug("persisting MetaData instance");
        super.persist(transientInstance);
    }

    @Override
    public void attachDirty(MetaData instance)
    {
        logger.debug("attaching dirty MetaData instance");
        super.attachDirty(instance);
    }

    @Override
    public void attachClean(MetaData instance)
    {
        logger.debug("attaching clean MetaData instance");
        super.attachClean(instance);
    }

    @Override
    public void delete(MetaData persistentInstance)
    {
        logger.debug("deleting MetaData instance");
        super.delete(persistentInstance);
    }

    @Override
    public MetaData merge(MetaData detachedInstance)
    {
        logger.debug("merging MetaData instance");
        return super.merge(detachedInstance);
    }

    @Override
    public MetaData findById(java.lang.Long id)
    {
        logger.debug("getting MetaData instance with id: {}", id);
        return super.findById(id);
    }
}
