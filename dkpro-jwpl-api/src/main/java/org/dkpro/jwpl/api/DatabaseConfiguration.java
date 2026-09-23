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

import java.util.Properties;

/**
 * A {@link DatabaseConfiguration} is used to establish a database connection and set various
 * parameters.
 */
public class DatabaseConfiguration
{

    /**
     * The default size of Hibernate's built-in connection pool, see
     * {@link #setConnectionPoolSize(int)}.
     */
    public static final int DEFAULT_CONNECTION_POOL_SIZE = 5;

    /**
     * The default maximum number of categories each {@link Wikipedia} instance keeps in memory,
     * see {@link #setCategoryCacheSize(int)}.
     */
    public static final int DEFAULT_CATEGORY_CACHE_SIZE = 1000;

    private String host;
    private String database;
    private String user;
    private String password;
    private WikiConstants.Language language;
    private String jdbcURL;
    private String databaseDriver;
    private final Properties hibernateProperties = new Properties();
    private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
    private int categoryCacheSize = DEFAULT_CATEGORY_CACHE_SIZE;

    /**
     * A no-arg constructor required by frameworks.
     * It is recommended to use
     * {@link DatabaseConfiguration#DatabaseConfiguration(String, String, String, String,
     * String, String, WikiConstants.Language)} instead.
     */
    public DatabaseConfiguration()
    {
    }

    /**
     * A constructor for MySQL backends, i.e. the default production setting.
     *
     * @param host
     *            The hostname the machine the database is hosted on.
     * @param database
     *            The name of the database to connect to.
     * @param user
     *            The username as part of the credentials used for authentication.
     * @param password
     *            The password as part of the credentials used for authentication.
     * @param language
     *            The {@link WikiConstants.Language} used for the underlying connection.
     */
    public DatabaseConfiguration(String host, String database, String user, String password,
            WikiConstants.Language language)
    {

        this("com.mysql.jdbc.Driver", "jdbc:mysql://" + host + "/" + database, host, database, user,
                password, language);
    }

    /**
     * A constructor for an explicit DBMS specific configuration.
     *
     * @param databaseDriver
     *            The fully qualified name of the JDBC driver.
     * @param jdbcURL
     *            A valid JDBC url used to open connections.
     * @param host
     *            The hostname the machine the database is hosted on.
     * @param database
     *            The name of the database to connect to.
     * @param user
     *            The username as part of the credentials used for authentication.
     * @param password
     *            The password as part of the credentials used for authentication.
     * @param language
     *            The {@link WikiConstants.Language} used for the underlying connection.
     */
    public DatabaseConfiguration(String databaseDriver, String jdbcURL, String host,
            String database, String user, String password, WikiConstants.Language language)
    {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.language = language;

        this.setDatabaseDriver(databaseDriver);
        this.setJdbcURL(jdbcURL);
    }

    /**
     * @return {@code True} if collation is supported by the database backend, else {@code false}.
     */
    boolean supportsCollation()
    {
        if (databaseDriver != null) {
            return databaseDriver.contains("mysql") || databaseDriver.contains("mariadb");
        }
        else {
            return false;
        }
    }

    /**
     * @param database
     *            The name of the database.
     */
    public void setDatabase(String database)
    {
        this.database = database;
    }

    /**
     * @param host
     *            The host where the database is running. Set to "localhost", if the database is
     *            running locally.
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * @param password
     *            The password to access the database.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @param user
     *            The database user.
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @param language
     *            The language of the Wikipedia data.
     */
    public void setLanguage(WikiConstants.Language language)
    {
        this.language = language;
    }

    /**
     * @return The name of the database.
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @return The host where the database is running.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return The password to access the database.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @return The database user.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @return The language of the Wikipedia data.
     */
    public WikiConstants.Language getLanguage()
    {
        return language;
    }

    /**
     * @param databaseDriver
     *            the databaseDriver to set
     */
    public void setDatabaseDriver(String databaseDriver)
    {
        this.databaseDriver = databaseDriver;
    }

    /**
     * @return the databaseDriver
     */
    public String getDatabaseDriver()
    {
        return databaseDriver;
    }

    /**
     * @param jdbcURL
     *            the jdbcURL to set
     */
    public void setJdbcURL(String jdbcURL)
    {
        this.jdbcURL = jdbcURL;
    }

    /**
     * @return the jdbcURL
     */
    public String getJdbcURL()
    {
        return jdbcURL;
    }

    /**
     * @return The size of Hibernate's built-in connection pool, i.e. the value JWPL applies as
     *         {@code hibernate.connection.pool_size}. Defaults to
     *         {@value #DEFAULT_CONNECTION_POOL_SIZE}.
     *
     * @see #setConnectionPoolSize(int)
     */
    public int getConnectionPoolSize()
    {
        return connectionPoolSize;
    }

    /**
     * Sets the size of Hibernate's built-in connection pool ({@code hibernate.connection.pool_size}).
     * <p>
     * The built-in pool does not wait for a connection to become available: once all of its
     * connections are in use, the next transaction fails immediately with a
     * {@code HibernateException}. As JWPL binds a session to the current thread, every thread that
     * uses the API concurrently holds one connection while a transaction is open. Callers using one
     * configuration from several threads therefore need a pool of at least as many connections as
     * threads - or a production grade pool such as C3P0 or HikariCP, see
     * {@code dkpro-jwpl-api/README.md}.
     * <p>
     * A value given for {@code hibernate.connection.pool_size} via
     * {@link #setHibernateProperty(String, String)} takes precedence over this one. Like those
     * settings, the size is read only when the session factory for this configuration is built, so
     * set it <i>before</i> the first {@code new Wikipedia(config)}.
     *
     * @param connectionPoolSize
     *            The maximum number of pooled connections. Must be at least {@code 1}.
     *
     * @throws IllegalArgumentException Thrown if {@code connectionPoolSize} is less than {@code 1}.
     */
    public void setConnectionPoolSize(int connectionPoolSize)
    {
        if (connectionPoolSize < 1) {
            throw new IllegalArgumentException(
                    "The connection pool size must be at least 1, but was " + connectionPoolSize);
        }
        this.connectionPoolSize = connectionPoolSize;
    }

    /**
     * @return The maximum number of categories each {@link Wikipedia} instance created for this
     *         configuration keeps in memory. Defaults to {@value #DEFAULT_CATEGORY_CACHE_SIZE};
     *         {@code 0} means that categories are not cached.
     *
     * @see #setCategoryCacheSize(int)
     */
    public int getCategoryCacheSize()
    {
        return categoryCacheSize;
    }

    /**
     * Sets the maximum number of categories each {@link Wikipedia} instance keeps in memory.
     * <p>
     * {@link Wikipedia#getCategory(int)} and the navigation methods built on it, such as
     * {@link Category#getParents()} and {@link Category#getChildren()}, serve repeated lookups of
     * the same category from this cache instead of querying the database again. Once the cache is
     * full, the least recently used category is evicted. Only the plain columns of a category (id,
     * page id and name) are cached; its links and pages are still read from the database whenever
     * they are requested. Categories that do not exist are not cached.
     * <p>
     * The cache belongs to a {@link Wikipedia} instance and may be used from several threads. The
     * size is read when the instance is created, so set it <i>before</i> {@code new Wikipedia(config)}.
     * As the data is expected not to change after the import, a cached category is never refreshed;
     * see {@link Wikipedia#clearCategoryCache()}.
     *
     * @param categoryCacheSize
     *            The maximum number of cached categories. Must not be negative; {@code 0} disables
     *            the cache.
     *
     * @throws IllegalArgumentException Thrown if {@code categoryCacheSize} is negative.
     */
    public void setCategoryCacheSize(int categoryCacheSize)
    {
        if (categoryCacheSize < 0) {
            throw new IllegalArgumentException(
                    "The category cache size must not be negative, but was " + categoryCacheSize);
        }
        this.categoryCacheSize = categoryCacheSize;
    }

    /**
     * Additional Hibernate settings supplied by the caller. They are merged <i>last</i> into the
     * Hibernate configuration and therefore override JWPL's own defaults, including
     * {@code hibernate.hbm2ddl.auto}.
     * <p>
     * The returned instance is live and mutable — changes to it are picked up as-is. Populate it
     * <i>before</i> the first {@code new Wikipedia(config)} for a given language/host/database
     * combination, because session factories are cached JVM-wide and built only once per
     * combination.
     * <p>
     * See {@code dkpro-jwpl-api/README.md} for the legacy-schema use case this exists for.
     *
     * @return The live, never {@code null}, bag of Hibernate settings.
     */
    public Properties getHibernateProperties()
    {
        return hibernateProperties;
    }

    /**
     * Sets a single additional Hibernate setting.
     *
     * @param key
     *            The Hibernate setting name, e.g. {@code hibernate.hbm2ddl.auto}.
     * @param value
     *            The value to set for {@code key}.
     *
     * @see #getHibernateProperties()
     */
    public void setHibernateProperty(String key, String value)
    {
        hibernateProperties.setProperty(key, value);
    }

    /**
     * Replaces all additional Hibernate settings with the given ones.
     *
     * @param properties
     *            The settings to use. A {@code null} argument clears the current settings.
     *
     * @see #getHibernateProperties()
     */
    public void setHibernateProperties(Properties properties)
    {
        hibernateProperties.clear();
        if (properties != null) {
            hibernateProperties.putAll(properties);
        }
    }

}
