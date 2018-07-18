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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

/**
 * Data access object for class {@link MetaData}.
 * 
 * @see de.tudarmstadt.ukp.wikipedia.api.MetaData
 * @author Hibernate Tools
 */
public class MetaDataDAO extends GenericDAO<MetaData> implements WikiConstants {

	private final Log logger = LogFactory.getLog(getClass());

    public MetaDataDAO(Wikipedia wiki) {
        super(wiki, MetaData.class);
    }

    @Override
    public void persist(MetaData transientInstance) {
        logger.debug("persisting MetaData instance");
        super.persist(transientInstance);
    }

    @Override
    public void attachDirty(MetaData instance) {
        logger.debug("attaching dirty MetaData instance");
        super.attachDirty(instance);
    }

    @Override
    public void attachClean(MetaData instance) {
        logger.debug("attaching clean MetaData instance");
        super.attachClean(instance);
    }

    @Override
    public void delete(MetaData persistentInstance) {
        logger.debug("deleting MetaData instance");
        super.delete(persistentInstance);
    }

    @Override
    public MetaData merge(MetaData detachedInstance) {
        logger.debug("merging MetaData instance");
        return super.merge(detachedInstance);
    }

    @Override
    public MetaData findById(java.lang.Long id) {
        logger.debug("getting MetaData instance with id: " + id);
        return super.findById(id);
    }
}
