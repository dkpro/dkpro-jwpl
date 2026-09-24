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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Iterator;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.util.Time;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionDecoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of the JWPL Revision API
 * <p>
 * This class represents the interface to iterate through multiple revisions.
 */
public class RevisionIterator
    extends AbstractRevisionService
    implements RevisionIteratorInterface
{

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Reference to the ResultSet
     */
    private ResultSet result;

    /**
     * Reference to the Statement, prepared once and reused for every page
     */
    private PreparedStatement statement;

    /**
     * SQL of the reused page statement, {@code null} until the first query
     */
    private String pageQuery;

    /**
     * Binary Data Flag
     */
    private boolean binaryData;

    /**
     * Text of the previous revision
     */
    private String previousRevision;

    /**
     * Primary key of the revision whose text is stored in {@link #previousRevision}, {@code -1}
     * if none has been reconstructed yet
     */
    private int previousRevisionPK = -1;

    /**
     * Current primary key
     */
    private int primaryKey;

    /**
     * Primary key indicating the end of the data
     */
    private int endPK;

    /**
     * ID of the current article
     */
    private int currentArticleID;

    /**
     * The last known revision counter
     */
    private int currentRevCounter;

    /**
     * Configuration parameter - indicates the maximum size of a query.
     */
    private final int MAX_NUMBER_RESULTS;

    /**
     * Should load revision text?
     */
    private boolean shouldLoadRevisionText;

    /**
     * The revision-api for this iterator - used by the Revision object in case of lazy loading
     */
    private RevisionApi revApi = null;

    /**
     * Whether the revisions table has a Namespace column, {@code null} until the first query
     */
    private Boolean hasNamespaceColumn;

    public boolean shouldLoadRevisionText()
    {
        return shouldLoadRevisionText;
    }

    public void setShouldLoadRevisionText(boolean shouldLoadRevisionText)
    {
        this.shouldLoadRevisionText = shouldLoadRevisionText;
    }

    /**
     * Creates a new RevisionIterator object.
     * <p>
     * The iteration returns the revisions with a primary key from {@code startPK} to
     * {@code endPK} and, if there is one, the revision that follows {@code endPK}.
     *
     * @param config
     *            Reference to the configuration object
     * @param startPK
     *            Start index, not negative
     * @param endPK
     *            End index, at least {@code startPK}
     * @param connection
     *            Reference to the connection, not {@code null}
     * @throws IllegalArgumentException
     *             if {@code startPK} is negative, {@code endPK} is less than {@code startPK} or
     *             {@code connection} is {@code null}
     * @throws WikiApiException
     *             if an error occurs
     */
    public RevisionIterator(final RevisionAPIConfiguration config, final int startPK,
            final int endPK, final Connection connection)
        throws WikiApiException
    {
        this(config, connection, startPK, endPK);
        if (startPK < 0 || endPK < startPK) {
            throw new IllegalArgumentException("Expected 0 <= startPK <= endPK, but got startPK "
                    + startPK + " and endPK " + endPK);
        }
    }

    /**
     * Creates a new RevisionIterator object for which the presence of the Namespace column is
     * already known, so that the revisions table does not have to be probed again.
     * <p>
     * The iteration returns the revisions with a primary key from {@code startPK} to
     * {@code endPK} and, if there is one, the revision that follows {@code endPK}. Unlike for the
     * public constructors, {@code endPK} may be {@code startPK - 1}, so that the iteration
     * returns the revision at {@code startPK} only.
     *
     * @param config
     *            Reference to the configuration object
     * @param startPK
     *            Start index, not negative
     * @param endPK
     *            End index, at least {@code startPK - 1}
     * @param connection
     *            Reference to the connection, not {@code null}
     * @param hasNamespaceColumn
     *            whether the revisions table has a Namespace column, {@code null} if unknown
     * @throws IllegalArgumentException
     *             if {@code startPK} is negative, {@code endPK} is less than {@code startPK - 1}
     *             or {@code connection} is {@code null}
     * @throws WikiApiException
     *             if an error occurs
     */
    RevisionIterator(final RevisionAPIConfiguration config, final int startPK, final int endPK,
            final Connection connection, final Boolean hasNamespaceColumn)
        throws WikiApiException
    {
        this(config, connection, startPK, endPK);
        if (startPK < 0 || endPK < startPK - 1) {
            throw new IllegalArgumentException("Expected 0 <= startPK <= endPK + 1, but got"
                    + " startPK " + startPK + " and endPK " + endPK);
        }
        this.hasNamespaceColumn = hasNamespaceColumn;
    }

    /**
     * Initializes the fields shared by the constructors that iterate over a range of primary
     * keys with a given connection. The range is validated by the calling constructor.
     *
     * @param config
     *            Reference to the configuration object
     * @param connection
     *            Reference to the connection, not {@code null}
     * @param startPK
     *            Start index
     * @param endPK
     *            End index
     * @throws IllegalArgumentException
     *             if {@code connection} is {@code null}
     */
    private RevisionIterator(final RevisionAPIConfiguration config, final Connection connection,
            final int startPK, final int endPK)
    {
        if (connection == null) {
            throw new IllegalArgumentException("The connection must not be null");
        }

        this.primaryKey = startPK - 1;
        this.endPK = endPK;
        this.config = config;

        this.currentArticleID = -1;
        this.currentRevCounter = -1;

        MAX_NUMBER_RESULTS = config.getBufferSize();

        this.connection = connection;
    }

    /**
     * Creates a new RevisionIterator object.
     *
     * @param config
     *            Reference to the configuration object
     * @param startPK
     *            Start index
     * @throws WikiApiException
     *             if an error occurs
     */
    public RevisionIterator(final RevisionAPIConfiguration config, final int startPK)
        throws WikiApiException
    {

        this(config);

        if (startPK < 0) {
            throw new IllegalArgumentException("Illegal argument");
        }

        this.primaryKey = startPK - 1;
    }

    /**
     * Creates a new RevisionIterator object.
     * <p>
     * The iteration returns the revisions with a primary key from {@code startPK} to
     * {@code endPK} and, if there is one, the revision that follows {@code endPK}.
     *
     * @param config
     *            Reference to the configuration object
     * @param startPK
     *            Start index
     * @param endPK
     *            End index
     * @throws WikiApiException
     *             if an error occurs
     */
    public RevisionIterator(final RevisionAPIConfiguration config, final int startPK, final int endPK)
        throws WikiApiException
    {

        this(config, startPK);

        if (endPK < 0 || startPK > endPK) {
            throw new IllegalArgumentException("Illegal argument");
        }

        this.endPK = endPK;
    }

    /**
     * Creates a new RevisionIterator object.
     *
     * @param config
     *            Reference to the configuration object
     * @throws WikiApiException
     *             if an error occurs
     */
    public RevisionIterator(final RevisionAPIConfiguration config) throws WikiApiException
    {

        this.config = config;
        this.primaryKey = -1;
        this.endPK = Integer.MAX_VALUE;

        this.statement = null;
        this.result = null;
        this.previousRevision = null;
        MAX_NUMBER_RESULTS = config.getBufferSize();

        connection = getConnection(config);
    }

    /**
     * Creates a new RevisionIterator object.
     *
     * @param config
     *            Reference to the configuration object
     * @param shouldLoadRevisionText
     *            should load revision text
     * @throws WikiApiException
     *             if an error occurs
     */
    public RevisionIterator(final RevisionAPIConfiguration config, boolean shouldLoadRevisionText)
        throws WikiApiException
    {
        this(config);
        this.shouldLoadRevisionText = shouldLoadRevisionText;
    }

    public RevisionIterator(final DatabaseConfiguration db) throws WikiApiException
    {
        this(getRevisionAPIConfig(db));
    }

    private static RevisionAPIConfiguration getRevisionAPIConfig(final DatabaseConfiguration db)
    {
        RevisionAPIConfiguration revAPIConfig = new RevisionAPIConfiguration();

        revAPIConfig.setHost(db.getHost());
        revAPIConfig.setDatabase(db.getDatabase());
        revAPIConfig.setDatabaseDriver(db.getDatabaseDriver());
        revAPIConfig.setJdbcURL(db.getJdbcURL());
        revAPIConfig.setUser(db.getUser());
        revAPIConfig.setPassword(db.getPassword());
        revAPIConfig.setLanguage(db.getLanguage());

        return revAPIConfig;
    }

    /**
     * Sends the query to the database and stores the result. The {@link java.sql.Statement} is
     * reused for the next page, the {@link ResultSet} will not be closed.
     *
     * @return {@code true}, if the result set has another element {@code false}, otherwise
     * @throws SQLException
     *             if an error occurs while accessing the database.
     */
    private boolean query() throws SQLException
    {
        if (hasNamespaceColumn == null) {
            hasNamespaceColumn = RevisionsTable.hasNamespaceColumn(connection);
        }

        int limit = -1;
        if (MAX_NUMBER_RESULTS > 0) {
            if (primaryKey + MAX_NUMBER_RESULTS > endPK) {
                limit = endPK - primaryKey + 1; // TODO: +1 ?
            }
            else {
                limit = MAX_NUMBER_RESULTS;
            }
        }
        else if (endPK != Integer.MAX_VALUE) {
            limit = endPK - primaryKey + 1;
        }

        if (pageQuery == null) {
            // The keyset paging continues after the last primary key read, hence the explicit order
            pageQuery = "SELECT PrimaryKey, Revision, RevisionCounter,"
                    + " RevisionID, ArticleID, Timestamp, FullRevisionID, ContributorName, ContributorId, Comment, Minor, ContributorIsRegistered"
                    + (hasNamespaceColumn ? ", Namespace" : "") + " FROM revisions"
                    + " WHERE PrimaryKey > ? ORDER BY PrimaryKey"
                    + (limit >= 0 ? " LIMIT ?" : "");
        }

        try {
            if (statement == null) {
                statement = this.connection.prepareStatement(pageQuery);
            }
            result = executePageQuery(limit);
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            try {
                boolean connectionReady = !connection.isClosed() && connection.isValid(5);
                logger.debug("Connection ready: {}", connectionReady);
                if (!connectionReady) {
                    connection = getConnection(config);
                }
                statement = this.connection.prepareStatement(pageQuery);
                result = executePageQuery(limit);
            }
            catch (WikiApiException wae) {
                logger.error(wae.getLocalizedMessage(), wae);
            }
        }

        if (result.next()) {
            binaryData = result.getMetaData().getColumnType(2) == Types.LONGVARBINARY;
            return true;
        }

        return false;
    }

    /**
     * Binds the keyset cursor and the page size to the reused statement and executes it.
     *
     * @param limit
     *            maximum number of rows of the page, ignored if the query has no limit
     * @return the result of the page
     * @throws SQLException
     *             if an error occurs while accessing the database.
     */
    private ResultSet executePageQuery(final int limit) throws SQLException
    {
        statement.setInt(1, primaryKey);
        if (limit >= 0) {
            statement.setInt(2, limit);
        }
        return statement.executeQuery();
    }

    /**
     * Returns the next revision.
     *
     * @return next revision
     */
    @Override
    public Revision next()
    {
        try {

            int revCount, articleID;

            revCount = result.getInt(3);
            articleID = result.getInt(5);

            if (articleID != this.currentArticleID) {
                // The first revision of the iteration may be in the middle of an article
                this.currentRevCounter = this.currentArticleID > 0 ? 0 : revCount - 1;
                this.currentArticleID = articleID;
            }

            if (revCount - 1 != this.currentRevCounter) {

                logger.error("Invalid RevCounter - [ArticleId {}, RevisionId {}, RevisionCounter {}] - Expected: {}",
                      articleID, result.getInt(4), revCount, this.currentRevCounter + 1);

                this.currentRevCounter = revCount;
                this.previousRevision = null;

                return null;
            }

            this.currentRevCounter = revCount;
            this.primaryKey = result.getInt(1);

            Revision revision = new Revision(revCount);
            revision.setPrimaryKey(this.primaryKey);
            if (!shouldLoadRevisionText) {
                String currentRevision;

                Diff diff;
                RevisionDecoder decoder = new RevisionDecoder(config.getCharacterSet());

                if (binaryData) {
                    decoder.setInput(result.getBytes(2));
                }
                else {
                    decoder.setInput(result.getString(2));
                }
                diff = decoder.decode();

                if (!diff.isFullRevision() && previousRevisionPK != this.primaryKey - 1) {
                    // The iteration did not decode the preceding revision of the diff chain,
                    // e.g. due to the start position or a switch from lazy mode
                    previousRevision = reconstructPreviousRevision(result.getInt(4));
                }

                try {
                    currentRevision = diff.buildRevision(previousRevision);
                }
                catch (Exception e) {
                    this.previousRevision = null;
                    logger.error("Reconstruction failed - [ArticleId {}, RevisionId {}, RevisionCounter {}]",
                            result.getInt(5), result.getInt(4), result.getInt(3), e);
                    return null;
                }

                previousRevision = currentRevision;
                previousRevisionPK = this.primaryKey;
                revision.setRevisionText(currentRevision);
            }
            else {
                if (revApi == null) {
                    revApi = new RevisionApi(config);
                }
                revision.setRevisionApi(revApi);
            }

            revision.setRevisionID(result.getInt(4));
            revision.setArticleID(articleID);
            revision.setTimeStamp(new Timestamp(result.getLong(6)));
            revision.setFullRevisionID(result.getInt(7));
            revision.setContributorName(result.getString(8));
            revision.setContributorId(result.getInt(9));
            revision.setComment(result.getString(10));
            revision.setMinor(result.getBoolean(11));
            revision.setContributorIsRegistered(result.getBoolean(12));

            if (Boolean.TRUE.equals(hasNamespaceColumn)) {
                revision.setNamespace(RevisionsTable.getNamespace(result, 13));
            }

            return revision;

        }
        catch (DecodingException | SQLException | IOException | WikiApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reconstructs the text of the revision that precedes the current one in its diff chain,
     * starting from the full revision the chain is based on.
     *
     * @param revisionID
     *            ID of the current revision
     * @return text of the preceding revision
     * @throws SQLException
     *             if an error occurs while accessing the database.
     * @throws WikiApiException
     *             if the current revision is not indexed
     * @throws DecodingException
     *             if a diff could not be decoded
     * @throws IOException
     *             if a diff could not be read
     */
    private String reconstructPreviousRevision(final int revisionID)
        throws SQLException, WikiApiException, DecodingException, IOException
    {
        int fullRevisionPK;
        try (PreparedStatement indexStatement = connection.prepareStatement(
                "SELECT FullRevisionPK FROM index_revisionID WHERE RevisionID=? LIMIT 1")) {
            indexStatement.setInt(1, revisionID);
            try (ResultSet indexResult = indexStatement.executeQuery()) {
                if (!indexResult.next()) {
                    throw new WikiApiException("The diff chain of the revision with the ID "
                            + revisionID + " cannot be reconstructed, it is not indexed.");
                }
                fullRevisionPK = indexResult.getInt(1);
            }
        }

        String text = null;
        try (PreparedStatement chainStatement = connection.prepareStatement(
                "SELECT Revision FROM revisions WHERE PrimaryKey >= ? AND PrimaryKey < ?"
                        + " ORDER BY PrimaryKey")) {
            chainStatement.setInt(1, fullRevisionPK);
            chainStatement.setInt(2, primaryKey);
            try (ResultSet chain = chainStatement.executeQuery()) {
                boolean binary = chain.getMetaData().getColumnType(1) == Types.LONGVARBINARY;
                while (chain.next()) {
                    RevisionDecoder decoder = new RevisionDecoder(config.getCharacterSet());
                    if (binary) {
                        decoder.setInput(chain.getBytes(1));
                    }
                    else {
                        decoder.setInput(chain.getString(1));
                    }
                    text = decoder.decode().buildRevision(text);
                }
            }
        }
        return text;
    }

    /**
     * Returns whether another revision is available or not.
     */
    @Override
    public boolean hasNext()
    {
        try {
            if (result != null && result.next()) {
                return true;
            }

            // Close the old result, the statement is reused for the next page
            if (this.result != null) {
                this.result.close();
                this.result = null;
            }

            if (primaryKey <= endPK && query()) { // TODO: <= ?
                return true;
            }

            // The iteration has ended - release the statement and its result set
            if (this.statement != null) {
                this.statement.close();
                this.statement = null;
            }
            this.result = null;
            return false;

        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is unsupported and will result in a {@link UnsupportedOperationException}.
     *
     * @deprecated Don't call this method as it will result in an exception at runtime.
     */
    @Override
    @Deprecated(since = "1.1")
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated(since = "1.1", forRemoval = true)
    // TODO This should go into a demo or test class separated from the code here...
    public static void main(final String[] args) throws Exception
    {

        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("localhost");
        config.setDatabase("en_wiki");
        config.setUser("root");
        config.setPassword("1234");

        config.setCharacterSet("UTF-8");
        config.setBufferSize(20000);
        config.setMaxAllowedPacket(16 * 1024 * 1023);

        long count = 1;
        long start = System.currentTimeMillis();

        Revision rev;
        Iterator<Revision> it = new RevisionIterator(config);

        System.out.println(Time.toClock(System.currentTimeMillis() - start));

        while (it.hasNext()) {
            rev = it.next();

            if (count++ % 10000 == 0) {

                if (rev != null) {
                    System.out.println(rev);
                }
            }
        }

        // w.close();
        System.out.println(Time.toClock(System.currentTimeMillis() - start));
    }
}
