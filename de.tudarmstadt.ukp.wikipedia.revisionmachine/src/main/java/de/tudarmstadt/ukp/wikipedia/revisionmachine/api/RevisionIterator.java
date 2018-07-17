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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionDecoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part of the JWPL Revision API
 *
 * This class represents the interface to iterate through multiple revisions.
 *
 */
public class RevisionIterator extends AbstractRevisionService implements RevisionIteratorInterface
{
	
	private static final Logger logger = LoggerFactory.getLogger(RevisionIterator.class);

	/** Reference to the ResultSet */
	private ResultSet result;

	/** Reference to the Statement */
	private PreparedStatement statement;

	/** Binary Data Flag */
	private boolean binaryData;

	/** Text of the previous revision */
	private String previousRevision;

	/** Current primary key */
	private int primaryKey;

	/** Primary key indicating the end of the data */
	private int endPK;

	/** ID of the current article */
	private int currentArticleID;

	/** The last known revision counter */
	private int currentRevCounter;

	/** Configuration parameter - indicates the maximum size of a querry. */
	private final int MAX_NUMBER_RESULTS;

	/** Should load revision text? */
	private boolean shouldLoadRevisionText;

	/**
	 * The revisionapi for this iterator - used by the Revision object
	 * in case of lazy loading
	 */
	private RevisionApi revApi= null;

	public boolean shouldLoadRevisionText()
	{
		return shouldLoadRevisionText;
	}

	public void setShouldLoadRevisionText(boolean shouldLoadRevisionText)
	{
		this.shouldLoadRevisionText = shouldLoadRevisionText;
	}

	/**
	 * (Constructor) Creates a new RevisionIterator object.
	 *
	 * @param config
	 *            Reference to the configuration object
	 * @param startPK
	 *            Start index
	 * @param endPK
	 *            End index
	 * @param connection
	 *            Reference to the connection
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionIterator(final RevisionAPIConfiguration config,
			final int startPK, final int endPK, final Connection connection)
		throws WikiApiException
	{

		if (startPK < 0 || endPK < 0 || startPK > endPK || connection == null) {
			throw new IllegalArgumentException("Illegal argument");
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
	 * (Constructor) Creates a new RevisionIterator object.
	 *
	 * @param config
	 *            Reference to the configuration object
	 * @param startPK
	 *            Start index
	 *
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
	 * (Constructor) Creates a new RevisionIterator object.
	 *
	 * @param config
	 *            Reference to the configuration object
	 * @param startPK
	 *            Start index
	 * @param endPK
	 *            End index
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionIterator(final RevisionAPIConfiguration config,
			final int startPK, final int endPK)
		throws WikiApiException
	{

		this(config, startPK);

		if (endPK < 0 || startPK > endPK) {
			throw new IllegalArgumentException("Illegal argument");
		}

		this.endPK = endPK;
	}

	/**
	 * (Constructor) Creates a new RevisionIterator object.
	 *
	 * @param config
	 *            Reference to the configuration object
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionIterator(final RevisionAPIConfiguration config)
		throws WikiApiException
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
	 * (Constructor) Creates a new RevisionIterator object.
	 *
	 * @param config
	 *            Reference to the configuration object
	 * @param shouldLoadRevisionText
	 *            should load revision text
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionIterator(final RevisionAPIConfiguration config,
			boolean shouldLoadRevisionText)
		throws WikiApiException
	{
		this(config);
		this.shouldLoadRevisionText = shouldLoadRevisionText;
	}

	public RevisionIterator(final DatabaseConfiguration db)
		throws WikiApiException
	{
		this(getRevisionAPIConfig(db));
	}

	private static RevisionAPIConfiguration getRevisionAPIConfig(
			final DatabaseConfiguration db)
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
	 * Sends the query to the database and stores the result. The {@link java.sql.Statement} and
	 * {@link ResultSet} connection will not be closed.
	 *
	 * @return {@code true}, if the result set has another element {@code false}, otherwise
	 *
	 * @throws SQLException
	 *             if an error occurs while accessing the database.
	 */
	private boolean query()
		throws SQLException
	{
		String query = "SELECT PrimaryKey, Revision, RevisionCounter,"
				+ " RevisionID, ArticleID, Timestamp, FullRevisionID, ContributorName, ContributorId, Comment, Minor, ContributorIsRegistered "
				+ "FROM revisions";

		if (primaryKey > 0) {
			query += " WHERE PrimaryKey > " + primaryKey;
		}

		if (MAX_NUMBER_RESULTS > 0) {
			query += " LIMIT ";

			if (primaryKey + MAX_NUMBER_RESULTS > endPK) {
				query += (endPK - primaryKey + 1); // TODO: +1 ?
			}
			else {
				query += MAX_NUMBER_RESULTS;
			}

		}
		else if (endPK != Integer.MAX_VALUE) {
			query += " LIMIT " + (endPK - primaryKey + 1);
		}

		try{
			statement=this.connection.prepareStatement(query);
			result = statement.executeQuery();
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			try {
				boolean connectionReady = !connection.isClosed() && connection.isValid(5);
				logger.debug("Connection ready: {}", connectionReady);
				if(!connectionReady) {
					connection = getConnection(config);
				}
				statement = this.connection.prepareStatement(query);
				result = statement.executeQuery(query);
			} catch (WikiApiException wae) {
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
				this.currentRevCounter = 0;
				this.currentArticleID = articleID;
			}

			if (revCount - 1 != this.currentRevCounter) {

				logger.error("Invalid RevCounter -" + " [ArticleId "
						+ articleID + ", RevisionId " + result.getInt(4)
						+ ", RevisionCounter " +revCount + "] - Expected: "
						+ (this.currentRevCounter + 1));

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
				RevisionDecoder decoder = new RevisionDecoder(
						config.getCharacterSet());

				if (binaryData) {
					decoder.setInput(result.getBinaryStream(2), true);
				}
				else {
					decoder.setInput(result.getString(2));
				}
				diff = decoder.decode();

				try {
					currentRevision = diff.buildRevision(previousRevision);
				}
				catch (Exception e) {
					this.previousRevision = null;
					logger.error("Reconstruction failed -"
							+ " [ArticleId " + result.getInt(5)
							+ ", RevisionId " + result.getInt(4)
							+ ", RevisionCounter " + result.getInt(3) + "]");
					return null;
				}

				previousRevision = currentRevision;
				revision.setRevisionText(currentRevision);
			} else {
				if(revApi==null){
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

			return revision;

		}
		catch (DecodingException | SQLException | IOException | WikiApiException e) {
			throw new RuntimeException(e);
		}
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

			// Close old queries
			if (this.statement != null) {
				this.statement.close();
			}
			if (this.result != null) {
				this.result.close();
			}

			if (primaryKey <= endPK) { // TODO: <= ?
				return query();
			}

			return false;

		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method is unsupported and will result in a {@link UnsupportedOperationException}.
	 *
	 * @deprecated
	 */
	@Override
	@Deprecated
	public void remove()
	{
		throw new UnsupportedOperationException();
	}


	// TODO This should go into a demo or test class separated from the code here...
	@Deprecated
	public static void main(final String[] args)
			throws Exception
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
					System.out.println(rev.toString());
				}
			}
		}

		// w.close();
		System.out.println(Time.toClock(System.currentTimeMillis() - start));
	}
}
