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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.EncodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.common.exceptions.LoggingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.SQLConsumerException;
import org.dkpro.jwpl.revisionmachine.common.logging.Logger;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.WriterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoding;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class writes the output to a database.
 * <p>
 * By default, the diffs are stored base 64 encoded in a {@code MEDIUMTEXT} column, using the same
 * statements as the SQL file output. If {@link ConfigurationKeys#MODE_BINARY_OUTPUT_ENABLED} is
 * set, the revisions table is created with a {@code MEDIUMBLOB} column instead, and the diffs are
 * stored binary through a prepared statement. This saves the base 64 overhead, about a quarter of
 * the size of the column. The readers of the RevisionMachine support both kinds of tables.
 */
public class SQLDatabaseWriter
    implements WriterInterface
{

    /**
     * Reference to the database connection
     */
    private Connection connection;

    /**
     * Flag, which indicates whether the diffs are stored binary ({@code MEDIUMBLOB}) instead of
     * base 64 encoded ({@code MEDIUMTEXT})
     */
    private final boolean binaryOutput;

    /**
     * Statement which inserts the rows in the binary mode, {@code null} in the textual mode
     */
    private PreparedStatement insertStatement;

    /**
     * Reference to the logger
     */
    protected final Logger logger;

    /**
     * Reference to the SQL encoder
     */
    protected SQLEncoderInterface sqlEncoder;

    /**
     * (Constructor) Creates a new SQLDatabaseWriter object.
     *
     * @param logger
     *            Reference to the logger
     * @throws ConfigurationException
     *             if an error occurred while accessing the configuration
     * @throws LoggingException
     *             if an error occurred while accessing the logger
     */
    public SQLDatabaseWriter(final Logger logger) throws ConfigurationException, LoggingException
    {

        this.logger = logger;

        ConfigurationManager config = ConfigurationManager.getInstance();

        String host = (String) config.getConfigParameter(ConfigurationKeys.SQL_HOST);
        String user = (String) config.getConfigParameter(ConfigurationKeys.SQL_USERNAME);
        String password = (String) config.getConfigParameter(ConfigurationKeys.SQL_PASSWORD);
        String sTable = (String) config.getConfigParameter(ConfigurationKeys.SQL_DATABASE);
        this.binaryOutput = (Boolean) config
                .getConfigParameter(ConfigurationKeys.MODE_BINARY_OUTPUT_ENABLED);

        try {
            String driverDB = "com.mysql.jdbc.Driver";
            Class.forName(driverDB);

            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + sTable,
                    user, password);
        }
        catch (ClassNotFoundException | SQLException e) {
            throw new ConfigurationException(e);
        }

        // The connection is open by now, and no caller can reach it any more once the
        // constructor fails - so every failure below has to release it.
        try {
            init();
            writeHeader();
            if (binaryOutput) {
                this.insertStatement = connection.prepareStatement(SQLEncoder.INSERT_REVISION);
            }
        }
        catch (SQLException e) {
            ConfigurationException wrapped = new ConfigurationException(e);
            closeQuietly(wrapped);
            throw wrapped;
        }
        catch (ConfigurationException | LoggingException | RuntimeException e) {
            closeQuietly(e);
            throw e;
        }
    }

    /**
     * Closes the database connection while another failure is already on its way out. A failure to
     * do so is attached to that one rather than replacing it.
     *
     * @param primary
     *            The failure that made the connection unreachable.
     */
    private void closeQuietly(Exception primary)
    {
        try {
            closeConnection();
        }
        catch (SQLException e) {
            primary.addSuppressed(e);
        }
    }

    /**
     * This method will build the keys of the revisions table, which were disabled for the bulk
     * load, and close the connection to the output.
     *
     * @throws SQLException
     *             if problems occurred while building the keys or closing the connection to the
     *             database.
     */
    @Override
    public void close() throws SQLException
    {
        try (Statement query = connection.createStatement()) {
            query.executeUpdate(SQLEncoder.ENABLE_KEYS);
        }
        finally {
            closeConnection();
        }
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException
     *             if problems occurred while closing the connection to the database.
     */
    private void closeConnection() throws SQLException
    {
        try {
            if (this.insertStatement != null) {
                this.insertStatement.close();
                this.insertStatement = null;
            }
        }
        finally {
            this.connection.close();
        }
        this.connection = null;
    }

    /**
     * Creates the SQL encoder.
     *
     * @throws ConfigurationException
     *             if an error occurred while accessing the configuration
     * @throws LoggingException
     *             if an error occurred while accessing the logger
     */
    protected void init() throws ConfigurationException, LoggingException
    {
        this.sqlEncoder = new SQLEncoder(logger);
    }

    /**
     * This method will process the given DiffTask and send it to the specified output.
     *
     * @param task
     *            DiffTask
     * @throws ConfigurationException
     *             if problems occurred while initializing the components
     * @throws IOException
     *             if problems occurred while writing the output (to file or archive)
     * @throws SQLConsumerException
     *             if problems occurred while writing the output (to the SQL producer database)
     */
    @Override
    public void process(final Task<Diff> task)
        throws ConfigurationException, IOException, SQLConsumerException
    {

        if (binaryOutput) {
            processBinary(task);
            return;
        }

        int i = -1;
        SQLEncoding[] queries = null;

        try {
            queries = sqlEncoder.encodeTask(task);

            int size = queries.length;
            for (i = 0; i < size; i++) {
                try (Statement query = connection.createStatement()) {
                    query.executeUpdate(queries[i].getQuery());
                }
            }
            // System.out.println(task.toString());

        }
        catch (SQLException e) {

            String q;
            if (queries == null || queries.length <= i || queries[i] == null) {
                q = "<unidentified query>";
            }
            else {
                q = queries[i].toString();
            }
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION, q, e);
        }
        catch (DecodingException e) {
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION, e);
        }
        catch (EncodingException e) {
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION, e);
        }
    }

    /**
     * Stores the given DiffTask with binary encoded diffs.
     *
     * @param task
     *            DiffTask
     * @throws ConfigurationException
     *             if problems occurred while initializing the components
     * @throws IOException
     *             if the character encoding of the revision text is not supported
     * @throws SQLConsumerException
     *             if problems occurred while encoding the task or writing it to the database
     */
    private void processBinary(final Task<Diff> task)
        throws ConfigurationException, IOException, SQLConsumerException
    {
        try {
            sqlEncoder.binaryTask(task, insertStatement);
        }
        catch (SQLException e) {
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION,
                    "Binary insert of the revisions of " + task.getHeader().getArticleName(), e);
        }
        catch (DecodingException e) {
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION, e);
        }
        catch (EncodingException e) {
            throw ErrorFactory.createSQLConsumerException(
                    ErrorKeys.DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION, e);
        }
    }

    /**
     * Retrieves the encoded SQL orders and executes them.
     *
     * @throws SQLException
     *             if an error occurred while accessing the database
     */
    private void writeHeader() throws SQLException
    {

        String[] revTableHeaderQueries = binaryOutput ? sqlEncoder.getBinaryTable()
                : sqlEncoder.getTable();

        // commit revision table header
        for (String revTableHeaderQuery : revTableHeaderQueries) {
            try (Statement query = connection.createStatement()) {
                query.executeUpdate(revTableHeaderQuery);
            }
        }

    }
}
