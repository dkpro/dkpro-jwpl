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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil.SessionFactoryKey;
import org.junit.jupiter.api.Test;

/**
 * Tests the derivation of the session factory cache key. No database is required here as only the
 * key itself is under test.
 */
public class WikiHibernateUtilKeyTest
{

    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String URL = "jdbc:hsqldb:mem:wikiapi_key_test";
    private static final String SECRET = "s3cr3t-p4ssw0rd";

    private static DatabaseConfiguration config()
    {
        return new DatabaseConfiguration(DRIVER, URL, "localhost", "wikiapi_test", "sa", SECRET,
                Language._test);
    }

    @Test
    public void testEqualValuesYieldEqualKeys()
    {
        SessionFactoryKey a = WikiHibernateUtil.keyOf(config());
        SessionFactoryKey b = WikiHibernateUtil.keyOf(config());

        assertNotNull(a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHostAndDatabaseBoundaryDoesNotCollide()
    {
        DatabaseConfiguration a = config();
        a.setHost("hostdb");
        a.setDatabase("x");

        DatabaseConfiguration b = config();
        b.setHost("host");
        b.setDatabase("dbx");

        assertNotEquals(WikiHibernateUtil.keyOf(a), WikiHibernateUtil.keyOf(b));
    }

    @Test
    public void testDifferingJdbcUrlYieldsDifferentKey()
    {
        DatabaseConfiguration other = config();
        other.setJdbcURL("jdbc:hsqldb:hsql://localhost:9001/wikiapi_test");

        assertNotEquals(WikiHibernateUtil.keyOf(config()), WikiHibernateUtil.keyOf(other));
    }

    @Test
    public void testDifferingDatabaseDriverYieldsDifferentKey()
    {
        DatabaseConfiguration other = config();
        other.setDatabaseDriver("org.mariadb.jdbc.Driver");

        assertNotEquals(WikiHibernateUtil.keyOf(config()), WikiHibernateUtil.keyOf(other));
    }

    @Test
    public void testDifferingUserYieldsDifferentKey()
    {
        DatabaseConfiguration other = config();
        other.setUser("someone-else");

        assertNotEquals(WikiHibernateUtil.keyOf(config()), WikiHibernateUtil.keyOf(other));
    }

    @Test
    public void testDifferingPasswordYieldsDifferentKey()
    {
        DatabaseConfiguration other = config();
        other.setPassword("another-password");

        assertNotEquals(WikiHibernateUtil.keyOf(config()), WikiHibernateUtil.keyOf(other));
    }

    @Test
    public void testDifferingLanguageYieldsDifferentKey()
    {
        DatabaseConfiguration other = config();
        other.setLanguage(Language.english);

        assertNotEquals(WikiHibernateUtil.keyOf(config()), WikiHibernateUtil.keyOf(other));
    }

    @Test
    public void testNullPasswordIsToleratedAndDistinctFromEmptyPassword()
    {
        DatabaseConfiguration nullPassword = config();
        nullPassword.setPassword(null);

        DatabaseConfiguration emptyPassword = config();
        emptyPassword.setPassword("");

        SessionFactoryKey nullKey = WikiHibernateUtil.keyOf(nullPassword);
        assertNull(nullKey.passwordDigest());
        assertNotEquals(nullKey, WikiHibernateUtil.keyOf(emptyPassword));
    }

    @Test
    public void testPasswordIsNeverExposedByTheKey()
    {
        SessionFactoryKey key = WikiHibernateUtil.keyOf(config());

        assertNotEquals(SECRET, key.passwordDigest());
        assertFalse(key.passwordDigest().contains(SECRET));
        assertFalse(key.toString().contains(SECRET));
        assertFalse(key.toString().contains(key.passwordDigest()));
    }
}
