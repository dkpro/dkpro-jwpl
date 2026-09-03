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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.dkpro.jwpl.api.hibernate.LegacyMetaData;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Verifies that JWPL keeps working against a database that predates the
 * {@code MetaData.version} column, and that caller supplied Hibernate settings reach Hibernate.
 * <p>
 * HSQLDB only: the fixture is provisioned in-memory, so no container runtime is required.
 */
@EnabledIf("isHsqldb")
public class LegacyMetaDataTest
{

    private static final String LEGACY_URL = "jdbc:hsqldb:mem:wikiapi_legacy";
    private static final String LEGACY_DB = "wikiapi_legacy";
    /*
     * Note well: session factories are cached JVM-wide, keyed by the connection affecting values of
     * a configuration - the caller supplied Hibernate settings are deliberately not among them. So
     * every configuration below uses a distinct database name to force a fresh probe.
     */
    private static final String VALIDATE_DB = "wikiapi_legacy_validate";
    private static final String SHOW_SQL_DB = "wikiapi_legacy_showsql";

    static boolean isHsqldb()
    {
        return JwplTestDatabase.selectEngine() == JwplTestDatabase.Engine.HSQLDB;
    }

    @BeforeAll
    public static void setupLegacyDatabase()
    {
        JwplTestDatabase.provision(LEGACY_URL, "sa", "", "db/schema-hsqldb-legacy.sql",
                "db/data-legacy.sql");
    }

    private static DatabaseConfiguration legacyConfiguration(String database)
    {
        DatabaseConfiguration db = new DatabaseConfiguration();
        db.setDatabase(database);
        db.setHost("localhost");
        db.setUser("sa");
        db.setPassword("");
        db.setLanguage(WikiConstants.Language._test);
        db.setJdbcURL(LEGACY_URL);
        db.setDatabaseDriver("org.hsqldb.jdbcDriver");
        return db;
    }

    @Test
    public void testWikipediaStartsAgainstLegacySchema()
    {
        assertDoesNotThrow(() -> new Wikipedia(legacyConfiguration(LEGACY_DB)));
    }

    @Test
    public void testGetVersionIsNullOnLegacySchema() throws Exception
    {
        Wikipedia wiki = new Wikipedia(legacyConfiguration(LEGACY_DB));
        assertNull(wiki.getMetaData().getVersion());
    }

    @Test
    public void testRemainingMetaDataStillReadable() throws Exception
    {
        Wikipedia wiki = new Wikipedia(legacyConfiguration(LEGACY_DB));
        MetaData metaData = wiki.getMetaData();

        assertNotNull(metaData);
        assertEquals(1, metaData.getId());
        assertEquals(36, metaData.getNumberOfPages());
        assertEquals(17, metaData.getNumberOfCategories());
        assertEquals(6, metaData.getNumberOfRedirectPages());
        assertEquals(2, metaData.getNumberOfDisambiguationPages());
        assertEquals(1, metaData.getMainCategory().getPageId());
        assertEquals(200, metaData.getDisambiguationCategory().getPageId());
    }

    @Test
    public void testProbeSelectsLegacyEntity()
    {
        assertSame(LegacyMetaData.class,
                WikiHibernateUtil.getMetaDataEntityClass(legacyConfiguration(LEGACY_DB)));
        assertSame(org.dkpro.jwpl.api.hibernate.MetaData.class, WikiHibernateUtil
                .getMetaDataEntityClass(JwplTestDatabase.instance().configuration()));
    }

    @Test
    public void testHibernatePropertyOverrideIsApplied()
    {
        DatabaseConfiguration db = legacyConfiguration(SHOW_SQL_DB);
        // JWPL's own default for this setting is 'false', so seeing 'true' on the built factory
        // proves the caller supplied bag is merged last.
        db.setHibernateProperty("hibernate.show_sql", "true");

        SessionFactory sessionFactory = WikiHibernateUtil.getSessionFactory(db);
        assertEquals("true", sessionFactory.getProperties().get("hibernate.show_sql"));
    }

    @Test
    public void testSchemaValidationAcceptsLegacyMapping()
    {
        DatabaseConfiguration db = legacyConfiguration(VALIDATE_DB);
        // JWPL hardcodes 'none' for HSQLDB, so validation only runs if the override wins. That
        // it passes shows the legacy eight column mapping matches the legacy table.
        db.setHibernateProperty("hibernate.hbm2ddl.auto", "validate");

        assertDoesNotThrow(() -> new Wikipedia(db));
    }
}
