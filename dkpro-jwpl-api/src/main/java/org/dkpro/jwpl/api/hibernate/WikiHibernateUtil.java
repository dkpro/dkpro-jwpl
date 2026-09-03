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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * The JVM-wide session factory cache. Both the {@link SessionFactory} and the {@code MetaData}
     * entity probed for it live in a single {@link CachedSessionFactory} value, so that the two can
     * not drift apart.
     */
    private static final Map<SessionFactoryKey, CachedSessionFactory> sessionFactoryMap
            = new ConcurrentHashMap<>();

    /**
     * The immutable, value derived cache key of a {@link DatabaseConfiguration}. Every field which
     * affects the connection a {@link SessionFactory} ends up using is a component here, so that two
     * configurations differing in any of them cannot share a cached factory. Being a record, equality
     * is component wise - unlike a concatenated string it therefore cannot suffer boundary collisions
     * such as {@code host="hostdb", database="x"} versus {@code host="host", database="dbx"}.
     * <p>
     * The password is held as a digest only, never in the clear.
     *
     * @param language        The wiki {@link Language}; {@code null} is tolerated by the key itself.
     * @param host            The database host.
     * @param database        The database name.
     * @param jdbcURL         The JDBC url the connection is actually opened against.
     * @param databaseDriver  The JDBC driver class name; may be {@code null}.
     * @param user            The database user; may be {@code null}.
     * @param passwordDigest  The SHA-256 hex digest of the password, or {@code null} if the password
     *                        itself was {@code null}.
     */
    record SessionFactoryKey(Language language, String host, String database, String jdbcURL,
            String databaseDriver, String user, String passwordDigest)
    {

        /**
         * Renders every component but the password, which is elided entirely - not even its digest is
         * shown. The generated record {@code toString()} would print all components, and this
         * representation is what can end up in a log statement or an exception message.
         */
        @Override
        public String toString()
        {
            return "SessionFactoryKey[language=" + language + ", host=" + host + ", database="
                    + database + ", jdbcURL=" + jdbcURL + ", databaseDriver=" + databaseDriver
                    + ", user=" + user + ", password=***]";
        }
    }

    /**
     * A lazily built cache entry pairing a {@link SessionFactory} with the {@code MetaData} entity
     * that was bound into it. Keeping both in one value makes the lockstep of the two structural
     * rather than a convention.
     * <p>
     * The entry object itself is cheap to create, which is what allows it to be installed via
     * {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)}: the expensive
     * schema probe and the Hibernate bootstrap happen afterwards, under this entry's own lock, and so
     * neither block unrelated keys nor re-enter the map from within a mapping function.
     */
    private static final class CachedSessionFactory
    {

        private volatile SessionFactory sessionFactory;

        private volatile Class<? extends AbstractMetaData> metaDataEntity;

        /**
         * Builds the {@link SessionFactory} and probes the bound entity, exactly once. A failed
         * attempt leaves this entry uninitialized, so that a later call may retry.
         *
         * @param config The {@link DatabaseConfiguration} this entry was keyed for.
         */
        private void initialize(DatabaseConfiguration config)
        {
            if (sessionFactory != null) {
                return;
            }
            synchronized (this) {
                if (sessionFactory != null) {
                    return;
                }
                Class<? extends AbstractMetaData> entity = hasMetaDataVersionColumn(config)
                        ? MetaData.class
                        : LegacyMetaData.class;
                Configuration configuration = getConfiguration(config, entity);
                StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties());
                SessionFactory built = configuration.buildSessionFactory(ssrb.build());
                metaDataEntity = entity;
                // Published last: a non-null 'sessionFactory' signals that both fields are set.
                sessionFactory = built;
            }
        }
    }

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
        return cacheEntry(config).sessionFactory;
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
        return cacheEntry(config).metaDataEntity;
    }

    /**
     * Looks up - building it if required - the cache entry for a specified
     * {@link DatabaseConfiguration}.
     *
     * @param config The {@link DatabaseConfiguration} to obtain the entry for.
     * @return A fully initialized {@link CachedSessionFactory}.
     * 
     * @throws ExceptionInInitializerError Thrown if the {@code config} instance was incorrect or incomplete.
     */
    private static CachedSessionFactory cacheEntry(DatabaseConfiguration config)
    {
        validate(config);
        CachedSessionFactory entry = sessionFactoryMap.computeIfAbsent(keyOf(config),
                key -> new CachedSessionFactory());
        entry.initialize(config);
        return entry;
    }

    /**
     * Derives the cache key of a specified {@link DatabaseConfiguration}. This is the single place
     * where the key is built, so a connection affecting field added later needs to be picked up here
     * only.
     * <p>
     * The values are snapshotted rather than the configuration being used as the key itself:
     * {@link DatabaseConfiguration} is public, mutable and subclassed, and callers hold on to the very
     * instance they handed to JWPL. A configuration mutated after first use therefore simply maps to
     * a different key - and, mutated back, to the original entry again.
     *
     * @param config The {@link DatabaseConfiguration} to derive a key for.
     * @return The derived {@link SessionFactoryKey}.
     */
    static SessionFactoryKey keyOf(DatabaseConfiguration config)
    {
        return new SessionFactoryKey(config.getLanguage(), config.getHost(), config.getDatabase(),
                config.getJdbcURL(), config.getDatabaseDriver(), config.getUser(),
                digest(config.getPassword()));
    }

    /**
     * Digests a password so that it can take part in the cache key without being retained in the
     * clear.
     *
     * @param password The password to digest; may be {@code null}.
     * @return The SHA-256 hex digest, or {@code null} for a {@code null} password - which keeps
     *         {@code null} and {@code ""} distinguishable.
     */
    private static String digest(String password)
    {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of()
                    .formatHex(sha256.digest(password.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e) {
            // Every JDK is required to provide SHA-256, so this is unreachable in practice.
            throw new IllegalStateException("SHA-256 is not available in this JVM.", e);
        }
    }

    /**
     * Rejects a {@link DatabaseConfiguration} which lacks a value required to open a connection.
     *
     * @param config The {@link DatabaseConfiguration} to check.
     * 
     * @throws ExceptionInInitializerError Thrown if the {@code config} instance was incorrect or incomplete.
     */
    private static void validate(DatabaseConfiguration config)
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
