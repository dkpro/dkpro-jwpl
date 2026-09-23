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
package org.dkpro.jwpl.revisionmachine.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;

/**
 * Iterates over the database to retrieve the necessary information for the index generation.
 * <p>
 * The iterator owns the database connection it opens, hence it must be closed by its caller - use
 * it as a resource of a try-with-resources statement.
 */
public class IndexIterator
    implements Iterator<Revision>, AutoCloseable
{

    /**
     * Keyset query for one page of revisions. The explicit ordering by primary key is required for
     * the paging to be correct, as the next page starts after the last primary key read.
     */
    private static final String PAGE_QUERY = "SELECT PrimaryKey, RevisionCounter,"
            + " RevisionID, ArticleID, Timestamp, FullRevisionID FROM revisions"
            + " WHERE PrimaryKey > ? ORDER BY PrimaryKey";

    /**
     * Reference to the database connection
     */
    private final Connection connection;

    /**
     * Reference to the ResultSet
     */
    private ResultSet result;

    /**
     * Reference to the prepared statement, reused for every page
     */
    private PreparedStatement statement;

    /**
     * Currently used primary kes
     */
    private int primaryKey;

    /**
     * Configuration parameter - maximum size of a result set
     */
    private final int MAX_NUMBER_RESULTS;

    /**
     * Creates the IndexIterator object.
     *
     * @param config
     *            Reference to the configuration
     * @throws WikiApiException
     *             if an error occurs
     */
    public IndexIterator(final RevisionAPIConfiguration config) throws WikiApiException
    {
        this(openConnection(config), config.getBufferSize());
    }

    /**
     * Creates the IndexIterator object on top of an existing connection, which the iterator then
     * owns and closes.
     *
     * @param connection
     *            Reference to the database connection
     * @param bufferSize
     *            maximum size of a result set, a value {@code <= 0} disables paging
     */
    IndexIterator(final Connection connection, final int bufferSize)
    {
        this.primaryKey = -1;

        this.statement = null;
        this.result = null;

        this.MAX_NUMBER_RESULTS = bufferSize;
        this.connection = connection;
    }

    private static Connection openConnection(final RevisionAPIConfiguration config)
        throws WikiApiException
    {
        try {
            String driverDB = "com.mysql.jdbc.Driver";
            Class.forName(driverDB);

            return DriverManager.getConnection(
                    "jdbc:mysql://" + config.getHost() + "/" + config.getDatabase(),
                    config.getUser(), config.getPassword());
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Queries the database for more revision information.
     *
     * @return {@code true} if the result set contains elements {@code false} otherwise
     * @throws SQLException
     *             if an error occurs while accessing the database
     */
    private boolean query() throws SQLException
    {
        if (statement == null) {
            String query = PAGE_QUERY;
            if (MAX_NUMBER_RESULTS > 0) {
                query += " LIMIT " + MAX_NUMBER_RESULTS;
            }
            statement = this.connection.prepareStatement(query);
        }

        statement.setInt(1, primaryKey);
        result = statement.executeQuery();
        return result.next();
    }

    /**
     * Returns the next revision information. (Does not contain the encoded diff)
     *
     * @return Revision
     */
    @Override
    public Revision next()
    {
        try {
            Revision revision = new Revision(result.getInt(2));

            this.primaryKey = result.getInt(1);
            revision.setPrimaryKey(this.primaryKey);

            revision.setRevisionID(result.getInt(3));
            revision.setArticleID(result.getInt(4));
            revision.setTimeStamp(new Timestamp(result.getLong(5)));
            revision.setFullRevisionID(result.getInt(6));

            return revision;

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns TRUE if another revision information is available.
     *
     * @return TRUE | FALSE
     */
    @Override
    public boolean hasNext()
    {
        try {
            if (result != null && result.next()) {
                return true;
            }

            closeResultSet();

            if (query()) {
                return true;
            }

            // The iteration has ended - release the statement and its result set right away
            // instead of holding them until the iterator itself is closed.
            closeResultResources();
            return false;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the {@link ResultSet} of the current batch, so that a new batch can be queried with
     * the same statement.
     *
     * @throws SQLException
     *             if an error occurs while closing the result set
     */
    private void closeResultSet() throws SQLException
    {
        try {
            if (result != null) {
                result.close();
            }
        }
        finally {
            result = null;
        }
    }

    /**
     * Closes the reused {@link PreparedStatement}, and with it the {@link ResultSet} it produced.
     *
     * @throws SQLException
     *             if an error occurs while closing the statement
     */
    private void closeResultResources() throws SQLException
    {
        try {
            if (statement != null) {
                // Closing a statement closes the result set it produced along with it.
                statement.close();
            }
        }
        finally {
            statement = null;
            result = null;
        }
    }

    /**
     * Closes the current batch and the database connection this iterator opened.
     *
     * @throws SQLException
     *             if an error occurs while closing the connection
     */
    @Override
    public void close() throws SQLException
    {
        try {
            closeResultResources();
        }
        finally {
            connection.close();
        }
    }

    /**
     * unsupported method
     *
     * @throws UnsupportedOperationException
     * @deprecated Don't cal this method as it will throw an exception at runtime.
     */
    @Deprecated(since = "1.1")
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
