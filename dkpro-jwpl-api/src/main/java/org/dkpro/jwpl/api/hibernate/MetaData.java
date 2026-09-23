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

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * An object-relational entity which maps a {@link org.dkpro.jwpl.api.MetaData}
 * to data attributes in a database. Those are persisted and retrieved by
 * an OR mapper, such as Hibernate.
 * <p>
 * It is accessed via an equally named class in the {@code api} package
 * to hide session management from the user.
 * <p>
 * This entity represents the <i>current</i> schema, i.e. one that carries the {@code version}
 * column. For databases generated before that column was introduced, {@link WikiHibernateUtil}
 * binds {@link LegacyMetaData} instead.
 */
@Immutable
@Entity
@Table(name = "MetaData")
public class MetaData
    extends AbstractMetaData
{

    @Column(name = "version")
    private String version;

    /**
     * A no argument constructor as required by Hibernate.
     */
    public MetaData()
    {
    }

    /**
     * @return Retrieves the version of a {@link MetaData} instance.
     */
    @Override
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version The version of a {@link MetaData} instance.
     */
    @Override
    public void setVersion(String version)
    {
        this.version = version;
    }
}
