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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.chrono.ChronoIterator;
import org.dkpro.jwpl.revisionmachine.common.util.Time;

/**
 * This class represents the iteration in chronological order.
 */
public class ChronoRevisionIterator
    implements RevisionIteratorInterface
{

    /**
     * Reference to the configuration parameters
     */
    private final RevisionAPIConfiguration config;

    /**
     * Reference to the database connection
     */
    private final Connection connection;

    /**
     * Reference to the currently used result set
     */
    private ResultSet resultArticles;

    /**
     * Statement for the batches of the article index, prepared on first use and reused for every
     * batch until the iterator is closed
     */
    private PreparedStatement articlesStatement;

    /**
     * Statement for the mapping lookup in {@code index_chronological}, prepared on first use and
     * reused for every article until the iterator is closed
     */
    private PreparedStatement mappingStatement;

    /**
     * Whether the revisions table has a Namespace column, {@code null} until it is probed. The
     * schema does not change during an iteration, so the probe is shared by the revision
     * iterators of all articles.
     */
    private Boolean hasNamespaceColumn;

    /**
     * Number of revisions of the current read article
     */
    private int maxRevision;

    /**
     * Reference to the Revision Iterator
     */
    private RevisionIterator revisionIterator;

    /**
     * Reference to the ChronoIterator
     */
    private ChronoIterator chronoIterator;

    /**
     * Retrieval mode
     */
    private int modus;

    /**
     * Retrieval mode id - undefined
     */
    private final static int INIT = 0;

    /**
     * Retrieval mode id - article is in chronological order
     */
    private final static int ITERATE_WITHOUT_MAPPING = 2;

    /**
     * Retrieval mode id - article is not in chronological order
     */
    private final static int ITERATE_WITH_MAPPING = 1;

    /**
     * ID of the current article (Should be 0 to enable an iteration over all article)
     */
    private int currentArticleID;

    /**
     * ID of the last article to retrieve
     */
    private int lastArticleID;

    /**
     * Parameter - buffer size
     */
    private final int MAX_NUMBER_RESULTS;

    /**
     * (Constructor) Creates a new ChronoRevisionIterator
     *
     * @param config
     *            Reference to the configuration parameters
     * @throws WikiApiException
     *             if an error occurs
     */
    public ChronoRevisionIterator(final RevisionAPIConfiguration config) throws WikiApiException
    {
        this(config, openConnection(config));
    }

    /**
     * (Constructor) Creates a new ChronoRevisionIterator that uses the given connection.
     *
     * @param config
     *            Reference to the configuration parameters
     * @param connection
     *            Reference to the database connection, closed along with the iterator
     */
    ChronoRevisionIterator(final RevisionAPIConfiguration config, final Connection connection)
    {
        this.config = config;
        this.MAX_NUMBER_RESULTS = config.getBufferSize();

        this.resultArticles = null;
        this.currentArticleID = 0;
        this.lastArticleID = -1;

        reset();

        this.connection = connection;
    }

    /**
     * Opens the connection to the database described by the configuration.
     *
     * @param config
     *            Reference to the configuration parameters
     * @return the connection
     * @throws WikiApiException
     *             if an error occurs
     */
    private static Connection openConnection(final RevisionAPIConfiguration config)
        throws WikiApiException
    {
        try {
            return AbstractRevisionService.openConnection(config);
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * (Constructor) Creates a new ChronoRevisionIterator
     *
     * @param config
     *            Reference to the configuration parameters
     * @throws WikiApiException
     *             if an error occurs
     */
    public ChronoRevisionIterator(final RevisionAPIConfiguration config, final int firstArticleID,
            final int lastArticleID)
        throws WikiApiException
    {

        this(config);

        this.currentArticleID = firstArticleID - 1;
        this.lastArticleID = lastArticleID;
    }

    /**
     * Retrieves the next articles from the article index.
     *
     * @return whether the query contains results or not
     * @throws SQLException
     *             if an error occurs while executing the query
     */
    private boolean queryArticle() throws SQLException
    {
        closeArticleResources();

        if (articlesStatement == null) {
            articlesStatement = this.connection.prepareStatement(
                    "SELECT ArticleID, FullRevisionPKs, RevisionCounter "
                            + "FROM index_articleID_rc_ts WHERE ArticleID > ? "
                            + "ORDER BY ArticleID LIMIT ?");
        }
        articlesStatement.setInt(1, this.currentArticleID);
        articlesStatement.setInt(2, MAX_NUMBER_RESULTS);

        resultArticles = articlesStatement.executeQuery();

        if (resultArticles.next()) {

            this.currentArticleID = resultArticles.getInt(1);
            return (this.lastArticleID == -1) || (this.currentArticleID <= this.lastArticleID);
        }

        return false;
    }

    /**
     * Returns the statement for the mapping lookup, preparing it on first use.
     *
     * @return the prepared statement
     * @throws SQLException
     *             if an error occurs while preparing the statement
     */
    private PreparedStatement mappingStatement() throws SQLException
    {
        if (mappingStatement == null) {
            mappingStatement = this.connection.prepareStatement(
                    "SELECT Mapping FROM index_chronological WHERE ArticleID=? LIMIT 1");
        }
        return mappingStatement;
    }

    /**
     * Returns whether the revisions table has a Namespace column, probing it on first use.
     *
     * @return {@code true} if the column exists, {@code false} otherwise
     * @throws SQLException
     *             if an error occurs while querying the database
     */
    private boolean hasNamespaceColumn() throws SQLException
    {
        if (hasNamespaceColumn == null) {
            hasNamespaceColumn = RevisionsTable.hasNamespaceColumn(connection);
        }
        return hasNamespaceColumn;
    }

    /**
     * Resets the modus to INIT.
     */
    private void reset()
    {
        this.modus = INIT;
    }

    /**
     * Initiates the iteration over of a new article.
     *
     * @return First Revision
     * @throws WikiApiException
     *             if an error occurs
     */
    private Revision init() throws WikiApiException
    {

        try {
            currentArticleID = resultArticles.getInt(1);
            String fullRevisionPKs = resultArticles.getString(2);
            String revisionCounters = resultArticles.getString(3);

            int index = revisionCounters.lastIndexOf(' ');
            if (index == -1) {
                throw new RuntimeException("Invalid revisioncounter content");
            }

            this.maxRevision = Integer
                    .parseInt(revisionCounters.substring(index + 1, revisionCounters.length()));

            PreparedStatement statement = mappingStatement();
            statement.setInt(1, currentArticleID);
            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    this.modus = ITERATE_WITH_MAPPING;

                    this.chronoIterator = new ChronoIterator(config, connection,
                            result.getString(1), fullRevisionPKs, revisionCounters);

                    if (this.chronoIterator.hasNext()) {
                        return this.chronoIterator.next();
                    }
                    else {
                        throw new RuntimeException("cIt Revision query failed");
                    }

                    /*
                     * this.revisionIndex = 1;
                     *
                     * revisionEncoder = new RevisionApi(config, connection); return
                     * revisionEncoder.getRevision(currentArticleID, revisionIndex);
                     */

                }
                else {

                    this.modus = ITERATE_WITHOUT_MAPPING;

                    index = fullRevisionPKs.indexOf(' ');
                    if (index == -1) {
                        index = fullRevisionPKs.length();
                    }

                    int currentPK = Integer.parseInt(fullRevisionPKs.substring(0, index));

                    // The revisions of the article are stored under consecutive primary keys,
                    // the last one is currentPK + maxRevision - 1. RevisionIterator also returns
                    // the revision after its end key, hence the end key is the one before the
                    // last. For an article with a single revision it is currentPK - 1.
                    this.revisionIterator = new RevisionIterator(config, currentPK,
                            currentPK + maxRevision - 2, connection, hasNamespaceColumn());

                    if (revisionIterator.hasNext()) {
                        return revisionIterator.next();
                    }
                    else {
                        throw new RuntimeException("Revision query failed");
                    }
                }
            }

        }
        catch (WikiApiException e) {
            throw e;
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
    }

    /**
     * Returns the next revision.
     *
     * @return Revision
     */
    public Revision next()
    {
        try {
          return switch (modus) {
            case INIT -> init();
            case ITERATE_WITH_MAPPING -> chronoIterator.next();

            // revisionEncoder.getRevision(currentArticleID, revisionIndex);

            case ITERATE_WITHOUT_MAPPING -> revisionIterator.next();
            default -> throw new RuntimeException("Illegal mode");
          };
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether another revision is available or not.
     *
     * @return TRUE or FALSE
     */
    public boolean hasNext()
    {

        try {
            switch (modus) {
            case INIT:
                return queryArticle();

            case ITERATE_WITH_MAPPING:
                if (chronoIterator.hasNext()) {
                    return true;
                }

                closeChronoIterator();
                reset();

                if (resultArticles.next()) {

                    this.currentArticleID = resultArticles.getInt(1);
                    return (this.lastArticleID == -1)
                            || (this.currentArticleID <= this.lastArticleID);
                }

                return queryArticle();

            case ITERATE_WITHOUT_MAPPING:

                if (revisionIterator.hasNext()) {
                    return true;
                }

                reset();

                if (resultArticles.next()) {

                    this.currentArticleID = resultArticles.getInt(1);
                    return (this.lastArticleID == -1)
                            || (this.currentArticleID <= this.lastArticleID);
                }

                return queryArticle();

            default:
                throw new RuntimeException("Illegal mode");
            }

        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is unsupported.
     *
     * @deprecated Do not use as the method will throw an exception at runtime.
     */
    @Override
    @Deprecated(since = "1.0")
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * This method closes the connection to the input component.
     *
     * @throws SQLException
     *             if an error occurs while closing the connection to the database.
     */
    @Override
    public void close() throws SQLException
    {
        try {
            closeChronoIterator();
        }
        finally {
            try {
                closeArticleResources();
            }
            finally {
                try {
                    closeStatements();
                }
                finally {
                    if (this.connection != null) {
                        this.connection.close();
                    }
                }
            }
        }
    }

    /**
     * Closes the ChronoIterator of the current article, if any.
     *
     * @throws SQLException
     *             if an error occurs while closing its statement
     */
    private void closeChronoIterator() throws SQLException
    {
        try {
            if (chronoIterator != null) {
                chronoIterator.close();
            }
        }
        finally {
            chronoIterator = null;
        }
    }

    /**
     * Closes the result set of the current article batch.
     *
     * @throws SQLException
     *             if an error occurs while closing the result set
     */
    private void closeArticleResources() throws SQLException
    {
        try {
            if (resultArticles != null) {
                resultArticles.close();
            }
        }
        finally {
            resultArticles = null;
        }
    }

    /**
     * Closes the prepared statements for the article batches and the mapping lookup.
     *
     * @throws SQLException
     *             if an error occurs while closing a statement
     */
    private void closeStatements() throws SQLException
    {
        try {
            if (articlesStatement != null) {
                articlesStatement.close();
            }
        }
        finally {
            articlesStatement = null;
            try {
                if (mappingStatement != null) {
                    mappingStatement.close();
                }
            }
            finally {
                mappingStatement = null;
            }
        }
    }

    public static void main(final String[] args) throws Exception
    {

        RevisionAPIConfiguration config = new RevisionAPIConfiguration();

        config.setHost("localhost");
        config.setDatabase("en_wiki");
        config.setUser("root");
        config.setPassword("1234");

        config.setCharacterSet("UTF-8");
        config.setBufferSize(10000);
        config.setMaxAllowedPacket(1024 * 1023);
        config.setChronoStorageSpace(400 * 1024 * 1024);

        long count = 1;
        long last = 0, now, start = System.currentTimeMillis();

        Revision rev;
        ChronoRevisionIterator it = new ChronoRevisionIterator(config);

        System.out.println(Time.toClock(System.currentTimeMillis() - start));

        while (it.hasNext()) {
            rev = it.next();

            if (count++ % 1000 == 0) {

                now = System.currentTimeMillis() - start;
                if (it.chronoIterator != null) {
                    System.out.println(it.chronoIterator.getStorageSize());
                }
                if (rev != null) {
                    System.out.println(rev);
                }
                System.out
                        .println(Time.toClock(now) + "\t" + (now - last) + "\tREBUILDING " + count);
                last = now;
            }
        }

        System.out.println(Time.toClock(System.currentTimeMillis() - start));
    }
}
