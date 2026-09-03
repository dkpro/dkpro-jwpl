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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Covers the additional Hibernate settings bag on {@link DatabaseConfiguration}.
 */
public class DatabaseConfigurationTest
{

    /** A subclass mirroring {@code RevisionAPIConfiguration}, which only calls {@code super()}. */
    private static final class SubclassedConfiguration
        extends DatabaseConfiguration
    {
        // Nothing to add; the point is the implicit super() call.
    }

    @Test
    public void testBagIsNeverNullAndInitiallyEmpty()
    {
        assertBagEmpty(new DatabaseConfiguration());
        assertBagEmpty(new DatabaseConfiguration("localhost", "wikiapi_test", "user", "pass",
                WikiConstants.Language._test));
        assertBagEmpty(new DatabaseConfiguration("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:x",
                "localhost", "wikiapi_test", "sa", "", WikiConstants.Language._test));
        assertBagEmpty(new SubclassedConfiguration());
    }

    @Test
    public void testSetHibernatePropertyPuts()
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        db.setHibernateProperty("hibernate.hbm2ddl.auto", "none");
        db.setHibernateProperty("hibernate.show_sql", "true");

        assertEquals(2, db.getHibernateProperties().size());
        assertEquals("none", db.getHibernateProperties().getProperty("hibernate.hbm2ddl.auto"));
        assertEquals("true", db.getHibernateProperties().getProperty("hibernate.show_sql"));
    }

    @Test
    public void testSetHibernatePropertiesReplacesPriorContent()
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        db.setHibernateProperty("hibernate.show_sql", "true");

        Properties replacement = new Properties();
        replacement.setProperty("hibernate.hbm2ddl.auto", "validate");
        db.setHibernateProperties(replacement);

        assertEquals(1, db.getHibernateProperties().size());
        assertEquals("validate", db.getHibernateProperties().getProperty("hibernate.hbm2ddl.auto"));
    }

    @Test
    public void testSetHibernatePropertiesWithNullClears()
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        db.setHibernateProperty("hibernate.show_sql", "true");
        db.setHibernateProperties(null);

        assertTrue(db.getHibernateProperties().isEmpty());
    }

    @Test
    public void testGetHibernatePropertiesReturnsLiveInstance()
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        Properties bag = db.getHibernateProperties();
        assertSame(bag, db.getHibernateProperties());

        bag.setProperty("hibernate.show_sql", "true");
        assertEquals("true", db.getHibernateProperties().getProperty("hibernate.show_sql"));
    }

    private static void assertBagEmpty(DatabaseConfiguration db)
    {
        assertNotNull(db.getHibernateProperties());
        assertTrue(db.getHibernateProperties().isEmpty());
    }
}
