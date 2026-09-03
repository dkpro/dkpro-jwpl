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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The pre-{@code version} mapping of the {@code MetaData} table: the eight columns contributed by
 * {@link AbstractMetaData} and nothing else.
 * <p>
 * {@link WikiHibernateUtil} binds this entity instead of {@link MetaData} when its schema probe
 * finds no {@code version} column, so that databases generated before that column was introduced
 * keep loading instead of failing with a Hibernate schema validation error or an
 * {@code Unknown column 'version'} SQL error. {@link #getVersion()} then returns {@code null}.
 * <p>
 * The recommended remedy is to add the column, after which the current mapping is used again:
 * <pre>
 * ALTER TABLE MetaData ADD COLUMN version VARCHAR(255) DEFAULT NULL;
 * </pre>
 * See {@code dkpro-jwpl-api/README.md} for details.
 * <p>
 * Note that this entity and {@link MetaData} may both declare {@code @Table(name = "MetaData")}
 * because exactly one of them is ever registered in a given session factory. Their entity names
 * differ, following the default simple-class-name rule.
 */
@Entity
@Table(name = "MetaData")
public class LegacyMetaData
    extends AbstractMetaData
{

    /**
     * A no argument constructor as required by Hibernate.
     */
    public LegacyMetaData()
    {
    }
}
