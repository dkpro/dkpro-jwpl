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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the additional Hibernate settings bag added to
 * {@code DatabaseConfiguration}: the only subclass in the code base must inherit a usable, non
 * {@code null} bag through its implicit {@code super()} call.
 */
public class RevisionAPIConfigurationTest
{

    @Test
    public void testHibernatePropertiesAreInitializedForSubclass()
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();

        assertNotNull(config.getHibernateProperties());
        assertTrue(config.getHibernateProperties().isEmpty());

        config.setHibernateProperty("hibernate.hbm2ddl.auto", "none");
        assertEquals("none", config.getHibernateProperties().getProperty("hibernate.hbm2ddl.auto"));
    }
}
