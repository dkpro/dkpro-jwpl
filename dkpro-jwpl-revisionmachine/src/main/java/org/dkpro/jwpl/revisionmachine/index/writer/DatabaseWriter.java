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
package org.dkpro.jwpl.revisionmachine.index.writer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dkpro.jwpl.revisionmachine.api.AbstractRevisionService;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoder;
import org.dkpro.jwpl.revisionmachine.index.indices.AbstractIndex;

/**
 * This class writes the output of the index generator to a database.
 */
public class DatabaseWriter
    implements IndexWriterInterface
{

    /**
     * Name of the index on the article ID of the revisions table
     */
    static final String ARTICLE_INDEX = "articleIdx";

    /**
     * Statement which creates the article index on revisions tables that do not declare it
     */
    static final String CREATE_ARTICLE_INDEX = "CREATE INDEX " + ARTICLE_INDEX
            + " ON revisions(ArticleID, RevisionCounter)";

    /**
     * Name of the composite index for timestamp-based lookups on the revisions table
     */
    static final String ARTICLE_TIMESTAMP_INDEX = "articleTsIdx";

    /**
     * Statement which creates the composite timestamp index on revisions tables that do not
     * declare it
     */
    static final String CREATE_ARTICLE_TIMESTAMP_INDEX = "CREATE INDEX " + ARTICLE_TIMESTAMP_INDEX
            + " ON revisions(ArticleID, Timestamp, RevisionCounter)";

    /**
     * Statement which creates the revision index table. The table gets no primary key here, as its
     * rows arrive grouped by article, i.e. in close to random RevisionID order. The key is added
     * once after the load by {@link #ADD_REVISION_INDEX_KEY}.
     */
    static final String CREATE_REVISION_INDEX_TABLE = "CREATE TABLE index_revisionID ("
            + "RevisionID INTEGER UNSIGNED NOT NULL, " + "RevisionPK INTEGER UNSIGNED NOT NULL, "
            + "FullRevisionPK INTEGER UNSIGNED NOT NULL);";

    /**
     * Statement which adds the primary key of the revision index table after the load
     */
    static final String ADD_REVISION_INDEX_KEY = "ALTER TABLE index_revisionID"
            + " ADD PRIMARY KEY (RevisionID);";

    /**
     * Reference to the database connection
     */
    private final Connection connection;

    /**
     * Creates a new DatabaseWriter.
     *
     * @param config
     *            Reference to the configuration parameters
     * @throws ClassNotFoundException
     *             if the JDBC Driver could not be located
     * @throws SQLException
     *             if an error occurred while creating the index tables
     */
    public DatabaseWriter(final RevisionAPIConfiguration config)
        throws ClassNotFoundException, SQLException
    {

        this.connection = AbstractRevisionService.openConnection(config);

        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE index_articleID_rc_ts ("
                + "ArticleID INTEGER UNSIGNED NOT NULL, " + "FullRevisionPKs MEDIUMTEXT NOT NULL, "
                + "RevisionCounter MEDIUMTEXT NOT NULL, " + "FirstAppearance BIGINT NOT NULL, "
                + "LastAppearance BIGINT NOT NULL, " + "PRIMARY KEY(ArticleID));");
        statement.close();

        statement = connection.createStatement();
        statement.execute(CREATE_REVISION_INDEX_TABLE);
        statement.close();

        statement = connection.createStatement();
        statement.execute("CREATE TABLE index_chronological ("
                + "ArticleID INTEGER UNSIGNED NOT NULL, " + "Mapping MEDIUMTEXT NOT NULL, "
                + "ReverseMapping MEDIUMTEXT NOT NULL, " + "PRIMARY KEY(ArticleID));");
        statement.close();
    }

    /**
     * Writes the buffered finalized queries to the output.
     *
     * @param index
     *            Reference to an index
     * @throws SQLException
     *             if an error occurred while transmitting the output
     */
    @Override
    public void write(final AbstractIndex index) throws SQLException
    {

        Statement statement;
        StringBuilder cmd;

        while (index.size() > 0) {

            System.out.println("Transmit Index [" + index + "]");

            cmd = index.remove();
            // System.out.println(cmd.toString());

            statement = connection.createStatement();
            statement.execute(cmd.toString());
            statement.close();
        }
    }

    /**
     * Wraps up the index generation process and writes all remaining statements e.g. concerning
     * UNCOMPRESSED-Indexes on the created tables.
     *
     * @throws SQLException
     *             if an error occurred while accessing the database
     */
    @Override
    public void finish() throws SQLException
    {
        // build the keys of the revisions table in case they are still disabled from the bulk load
        Statement statement = connection.createStatement();
        statement.execute(SQLEncoder.ENABLE_KEYS);
        statement.close();
        // the DiffTool declares both indexes when it creates the revisions table, tables created
        // by older versions lack one or both of them
        createIndexIfMissing(ARTICLE_INDEX, CREATE_ARTICLE_INDEX);
        createIndexIfMissing(ARTICLE_TIMESTAMP_INDEX, CREATE_ARTICLE_TIMESTAMP_INDEX);
        // build the primary key of the revision index in one pass after the load
        statement = connection.createStatement();
        statement.execute(ADD_REVISION_INDEX_KEY);
        statement.close();
    }

    /**
     * Creates an index on the revisions table unless it already exists.
     *
     * @param name
     *            name of the index
     * @param createStatement
     *            statement which creates the index
     * @throws SQLException
     *             if an error occurred while accessing the database
     */
    private void createIndexIfMissing(final String name, final String createStatement)
        throws SQLException
    {
        if (!hasIndex(name)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(createStatement + ";");
            }
        }
    }

    /**
     * Checks whether the revisions table already has the given index.
     *
     * @param name
     *            name of the index
     * @return {@code true} if the index exists, {@code false} otherwise
     * @throws SQLException
     *             if an error occurred while accessing the database
     */
    private boolean hasIndex(final String name) throws SQLException
    {
        try (ResultSet indices = connection.getMetaData().getIndexInfo(connection.getCatalog(),
                null, "revisions", false, false)) {
            while (indices.next()) {
                if (name.equalsIgnoreCase(indices.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Closes the file or the database connection.
     *
     * @throws SQLException
     *             if an error occurred while closing the database connection
     */
    @Override
    public void close() throws SQLException
    {
        this.connection.close();
    }
}
