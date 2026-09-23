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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common base class that handles the aspect of database connection handling.
 * <p>
 * Implementations own the database connection they open, hence they must be closed by their
 * caller - use them as a resource of a try-with-resources statement.
 */
public abstract class AbstractRevisionService
    implements AutoCloseable
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractRevisionService.class);

    /**
     * The JDBC driver used if no JDBC URL is configured.
     */
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    /**
     * Reference to database connection
     */
    protected Connection connection;

    /**
     * Reference to the configuration parameters
     */
    protected RevisionAPIConfiguration config;

    /**
     * Helper method to obtain a connection via the given {@link RevisionAPIConfiguration}
     * parameter.
     *
     * @param config
     *            Must not be {@code null}.
     * @return A valid {@link Connection} to the database endpoint.
     * @throws WikiApiException
     *             Thrown if errors occurred while opening a connection.
     */
    protected Connection getConnection(RevisionAPIConfiguration config) throws WikiApiException
    {
        Connection c;
        try {

            c = openConnection(config);
            if (!c.isValid(5)) {
                throw new WikiApiException("Connection could not be established.");
            }
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new WikiApiException(e);
        }

        return c;
    }

    /**
     * Opens a new {@link Connection} as described by the given {@link RevisionAPIConfiguration}.
     * <p>
     * If a JDBC URL is configured, it is used as is, and the configured database driver (if any)
     * is loaded beforehand. Otherwise, a MySQL connection is opened via
     * {@code jdbc:mysql://host/database}, built from the configured host and database name.
     *
     * @param config
     *            Must not be {@code null}.
     * @return A new {@link Connection} to the database endpoint. The caller must close it.
     * @throws SQLException
     *             Thrown if the connection could not be opened.
     * @throws ClassNotFoundException
     *             Thrown if the JDBC driver class could not be loaded.
     */
    public static Connection openConnection(RevisionAPIConfiguration config)
        throws SQLException, ClassNotFoundException
    {
        final String driverDB = hasJdbcURL(config) ? config.getDatabaseDriver() : MYSQL_DRIVER;
        if (driverDB != null && !driverDB.isBlank()) {
            Class.forName(driverDB);
        }
        return DriverManager.getConnection(resolveJdbcURL(config), config.getUser(),
                config.getPassword());
    }

    /**
     * Returns the JDBC URL to connect to: the configured JDBC URL if present, otherwise
     * {@code jdbc:mysql://host/database} built from the configured host and database name.
     *
     * @param config
     *            Must not be {@code null}.
     * @return The JDBC URL to use for the given configuration.
     */
    static String resolveJdbcURL(RevisionAPIConfiguration config)
    {
        return hasJdbcURL(config) ? config.getJdbcURL()
                : "jdbc:mysql://" + config.getHost() + "/" + config.getDatabase();
    }

    private static boolean hasJdbcURL(RevisionAPIConfiguration config)
    {
        return config.getJdbcURL() != null && !config.getJdbcURL().isBlank();
    }

    /**
     * This method closes any open {@link Connection connections} to the database.
     *
     * @throws SQLException
     *             if an error occurs while closing the connection
     */
    @Override
    public final void close() throws SQLException
    {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    protected void reconnect() throws SQLException
    {
        close();
        try {
            this.connection = getConnection(config);
        }
        catch (WikiApiException e) {
            close();
            logger.error("Could not reconnect. Closing connection...", e);
        }
    }
}
