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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which provides access to underlying Hibernate session factories.
 */
public class WikiHibernateUtil
    implements WikiConstants
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final String METADATA_TABLE = "MetaData";
    private static final String VERSION_COLUMN = "version";

    /**
     * Identifier casing differs per backend — HSQLDB folds unquoted identifiers to upper case,
     * MariaDB on Linux preserves the case a table was created with. The variants are probed in
     * this order; the first one that yields any column decides.
     */
    private static final String[] TABLE_NAME_VARIANTS = { "MetaData", "METADATA", "metadata" };

    /** The remedy quoted to users whose database predates the {@code version} column. */
    private static final String ADD_VERSION_COLUMN_DDL = "ALTER TABLE MetaData ADD COLUMN version "
            + "VARCHAR(255) DEFAULT NULL;";

    private static final Map<String, SessionFactory> sessionFactoryMap = new HashMap<>();

    /**
     * The {@code MetaData} entity bound for a given session factory, keyed exactly like
     * {@link #sessionFactoryMap} so that both stay in lockstep.
     */
    private static final Map<String, Class<? extends AbstractMetaData>> metaDataEntityMap
            = new HashMap<>();

    /**
     * Retrieves (and creates) a {@link SessionFactory} for a specified {@link DatabaseConfiguration}.
     *
     * @param config The {@link DatabaseConfiguration} to obtain the factory for. Must not be {@code null}.
     * @return A fully initialized {@link SessionFactory} instance.
     * 
     * @throws ExceptionInInitializerError Thrown if the {@code config} instance was incorrect or incomplete.
     */
    public static SessionFactory getSessionFactory(DatabaseConfiguration config)
    {

        if (config.getLanguage() == null) {
            throw new ExceptionInInitializerError(
                    "Database configuration error. 'Language' is empty.");
        }
        else if (config.getHost() == null) {
            throw new ExceptionInInitializerError("Database configuration error. 'Host' is empty.");
        }
        else if (config.getDatabase() == null) {
            throw new ExceptionInInitializerError(
                    "Database configuration error. 'Database' is empty.");
        }
        else if (config.getJdbcURL() == null) {
            throw new ExceptionInInitializerError(
                    "Database configuration error. 'JdbcURL' is empty.");
        }

        // Note: the key intentionally ignores the JDBC URL, the driver and the credentials.
        // It now also binds the probed 'MetaData' entity choice - see metaDataEntityMap.
        String uniqueSessionKey = config.getLanguage().toString() + config.getHost()
                + config.getDatabase();
        if (!sessionFactoryMap.containsKey(uniqueSessionKey)) {
            Class<? extends AbstractMetaData> metaDataEntity = hasMetaDataVersionColumn(config)
                    ? MetaData.class
                    : LegacyMetaData.class;
            Configuration configuration = getConfiguration(config, metaDataEntity);
            StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            SessionFactory sessionFactory = configuration.buildSessionFactory(ssrb.build());
            sessionFactoryMap.put(uniqueSessionKey, sessionFactory);
            metaDataEntityMap.put(uniqueSessionKey, metaDataEntity);
        }
        return sessionFactoryMap.get(uniqueSessionKey);
    }

    /**
     * Retrieves the {@code MetaData} entity bound for a specified {@link DatabaseConfiguration},
     * i.e. either {@link MetaData} (current schema) or {@link LegacyMetaData} (schema predating the
     * {@code version} column). The underlying schema probe runs only once, when the
     * {@link SessionFactory} for {@code config} is built.
     * <p>
     * Note well: this is internal API for {@link org.dkpro.jwpl.api.MetaData}. It is only public
     * because that class resides in a different package.
     *
     * @param config The {@link DatabaseConfiguration} to look up. Must not be {@code null}.
     * @return The bound entity class, never {@code null}.
     */
    public static Class<? extends AbstractMetaData> getMetaDataEntityClass(
            DatabaseConfiguration config)
    {
        // Ensures the probe has run and the map is populated; idempotent and cached.
        getSessionFactory(config);
        String uniqueSessionKey = config.getLanguage().toString() + config.getHost()
                + config.getDatabase();
        return metaDataEntityMap.getOrDefault(uniqueSessionKey, MetaData.class);
    }

    /**
     * Probes the live database for the presence of the {@code MetaData.version} column. This is a
     * single JDBC metadata lookup performed once per {@link SessionFactory}, never per query.
     * <p>
     * If the probe cannot be carried out - for instance because the JDBC driver is unavailable or
     * the account lacks the privileges to read schema metadata - the current schema is assumed,
     * which is exactly the behaviour of JWPL versions predating this probe.
     *
     * @param config The {@link DatabaseConfiguration} to probe.
     * @return {@code false} only if the {@code MetaData} table was found <i>and</i> demonstrably
     *         carries no {@code version} column; {@code true} otherwise.
     */
    private static boolean hasMetaDataVersionColumn(DatabaseConfiguration config)
    {
        try {
            Class.forName(config.getDatabaseDriver());
            try (Connection connection = DriverManager.getConnection(config.getJdbcURL(),
                    config.getUser(), config.getPassword())) {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                // The catalog reported by the connection first, to avoid picking up a same-named
                // table from another schema; then 'null' for drivers whose metadata calls do not
                // match the reported catalog.
                String[] catalogs = { connection.getCatalog(), null };
                for (String catalog : catalogs) {
                    Boolean seen = probeCatalog(databaseMetaData, catalog);
                    if (seen != null) {
                        if (!seen) {
                            logger.warn("The column '{}' is missing in table '{}'. Falling back to "
                                    + "the legacy mapping - MetaData#getVersion() will return null."
                                    + " To use the current schema, run: {}", VERSION_COLUMN,
                                    METADATA_TABLE, ADD_VERSION_COLUMN_DDL);
                        }
                        return seen;
                    }
                }
            }
        }
        catch (SQLException | ClassNotFoundException | RuntimeException e) {
            logger.debug("Could not probe the MetaData schema; assuming the current layout.", e);
        }
        // The table was not visible at all - not a legacy signal.
        return true;
    }

    /**
     * Probes a single catalog for the {@code MetaData} table.
     *
     * @param databaseMetaData The {@link DatabaseMetaData} to query.
     * @param catalog          The catalog to inspect; may be {@code null}.
     * @return {@code Boolean.TRUE} if the table was found and carries a {@code version} column,
     *         {@code Boolean.FALSE} if it was found without one, and {@code null} if the table was
     *         not found under any name variant.
     */
    private static Boolean probeCatalog(DatabaseMetaData databaseMetaData, String catalog)
        throws SQLException
    {
        for (String variant : TABLE_NAME_VARIANTS) {
            boolean anyColumn = false;
            boolean versionColumn = false;
            try (ResultSet columns = databaseMetaData.getColumns(catalog, null, variant, null)) {
                while (columns.next()) {
                    anyColumn = true;
                    if (VERSION_COLUMN.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                        versionColumn = true;
                    }
                }
            }
            if (anyColumn) {
                return versionColumn;
            }
        }
        return null;
    }

    private static Properties getProperties(DatabaseConfiguration config)
    {
        String user = config.getUser();
        String password = config.getPassword();

        /*
         * Ensures explicit DBMS type specific configuration for hsqldb from junit tests context
         */
        String jdbcURL = config.getJdbcURL();
        String databaseDriverClass = config.getDatabaseDriver();

        Properties p = new Properties();
        boolean useMySQL = false;
        boolean useMariaDB = false;
        boolean useHSQL = false;
        // XXX other backends might be interesting here as well...
        if (jdbcURL.toLowerCase().contains("mysql")) {
            useMySQL = true;
        }
        else if (jdbcURL.toLowerCase().contains("mariadb")) {
            useMariaDB = true;
        }
        else if (jdbcURL.toLowerCase().contains("hsql")) {
            useHSQL = true;
        }

        // Database connection settings
        p.setProperty("hibernate.connection.driver_class", databaseDriverClass);
        p.setProperty("hibernate.connection.url", jdbcURL);
        /*
         * Needed to ensure working hsqldb queries - don't remove it...!
         */
        p.setProperty("hibernate.connection.useUnicode", "true");
        p.setProperty("hibernate.connection.characterEncoding", "UTF-8");

        p.setProperty("hibernate.connection.username", user);
        p.setProperty("hibernate.connection.password", password);

        // JDBC connection pool (use the built-in) -->
        p.setProperty("hibernate.connection.pool_size", "5");

        // Enable Hibernate's automatic session context management
        p.setProperty("hibernate.current_session_context_class", "thread");

        // Disable the second-level cache
        p.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");

        // Echo all executed SQL to stdout
        p.setProperty("hibernate.show_sql", "false");

        // Do only update schema on changes
        if (useMySQL || useMariaDB) {
            p.setProperty("hibernate.hbm2ddl.auto", "validate");
        }

        if (useHSQL) {
            p.setProperty("hibernate.hbm2ddl.auto", "none");
        }

        // Leave this set 'true' as this is required for dynamic Dialect resolution!
        p.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "true");

        if (useMySQL || useMariaDB) {
            // Set C3P0 Connection Pool in case somebody wants to use it in production settings
            // if no C3P0 is available at runtime, related warnings can be ignored safely as the
            // built-in CP will be used.
            p.setProperty("hibernate.c3p0.acquire_increment", "3");
            p.setProperty("hibernate.c3p0.idle_test_period", "300");
            p.setProperty("hibernate.c3p0.min_size", "3");
            p.setProperty("hibernate.c3p0.max_size", "15");
            p.setProperty("hibernate.c3p0.max_statements", "100");
            p.setProperty("hibernate.c3p0.timeout", "1000");
        }
        return p;
    }

    private static Configuration getConfiguration(DatabaseConfiguration config,
            Class<? extends AbstractMetaData> metaDataEntity)
    {
        Configuration configuration = new Configuration().addAnnotatedClass(Category.class)
                .addAnnotatedClass(metaDataEntity).addAnnotatedClass(Page.class)
                .addAnnotatedClass(PageMapLine.class).addProperties(getProperties(config));

        Properties overrides = config.getHibernateProperties();
        if (overrides != null && !overrides.isEmpty()) {
            // Caller-supplied settings are merged last and therefore override JWPL's defaults.
            configuration.addProperties(overrides);
        }
        return configuration;
    }

}
