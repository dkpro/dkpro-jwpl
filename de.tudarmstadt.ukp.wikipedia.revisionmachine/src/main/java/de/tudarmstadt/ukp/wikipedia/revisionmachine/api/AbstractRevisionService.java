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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A common base class that handles the aspect of database connection handling.
 */
public abstract class AbstractRevisionService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRevisionService.class);

    /** Reference to database connection */
    protected Connection connection;

    /** Reference to the configuration parameters */
    protected RevisionAPIConfiguration config;

    /**
     * Helper method to obtain a connection via the given {@link RevisionAPIConfiguration} parameter.
     * @param config Must not be {@code null}.
     * @return A valid {@link Connection} to the database endpoint.
     * @throws WikiApiException Thrown if errors occurred while opening a connection.
     */
    protected Connection getConnection(RevisionAPIConfiguration config) throws WikiApiException
    {
        Connection c;
        try {

            String driverDB = config.getDatabaseDriver();
            Class.forName(driverDB);

            c = DriverManager.getConnection(config.getJdbcURL(), config.getUser(), config.getPassword());
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
     * This method closes any open {@link Connection connections} to the database.
     *
     * @throws SQLException
     *             if an error occurs while closing the connection
     */
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
