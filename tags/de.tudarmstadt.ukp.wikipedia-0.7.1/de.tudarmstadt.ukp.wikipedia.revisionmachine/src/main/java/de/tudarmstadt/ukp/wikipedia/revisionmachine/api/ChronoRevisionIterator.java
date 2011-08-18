/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Project Website:
 * 	http://jwpl.googlecode.com
 * 
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.chrono.ChronoIterator;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;

/**
 * This class represents the iteration in chronological order.
 */
public class ChronoRevisionIterator
	implements RevisionIteratorInterface
{

	/** Reference to the configuration parameters */
	private final RevisionAPIConfiguration config;

	/** Reference to the database connection */
	private Connection connection;

	/** Reference to the currently used result set */
	private ResultSet resultArticles;

	/** Number of revisions of the current read article */
	private int maxRevision;

	/** Reference to the Revision Iterator */
	private RevisionIterator revisionIterator;

	/** Reference to the ChronoIterator */
	private ChronoIterator chronoIterator;

	/** Retrieval mode */
	private int modus;

	/** Retrieval mode id - undefined */
	private final static int INIT = 0;

	/** Retrieval mode id - article is in chronological order */
	private final static int ITERATE_WITHOUT_MAPPING = 2;

	/** Retrieval mode id - article is not in chronological order */
	private final static int ITERATE_WITH_MAPPING = 1;

	/**
	 * ID of the current article (Should be 0 to enable an iteration over all
	 * article)
	 */
	private int currentArticleID = 0;

	/** ID of the last article to retrieve */
	private int lastArticleID;

	/** Parameter - buffer size */
	private final int MAX_NUMBER_RESULTS;

	/**
	 * (Constructor) Creates a new ChronoRevisionIterator
	 *
	 * @param config
	 *            Reference to the configuration parameters
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public ChronoRevisionIterator(final RevisionAPIConfiguration config)
		throws WikiApiException
	{

		this.config = config;
		try {
			this.MAX_NUMBER_RESULTS = config.getBufferSize();

			this.resultArticles = null;
			this.currentArticleID = 0;
			this.lastArticleID = -1;

			reset();

			String driverDB = "com.mysql.jdbc.Driver";
			Class.forName(driverDB);

			this.connection = DriverManager.getConnection("jdbc:mysql://"
					+ config.getHost() + "/" + config.getDatabase(),
					config.getUser(), config.getPassword());

		}
		catch (SQLException e) {
			throw new WikiApiException(e);
		}
		catch (ClassNotFoundException e) {
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
	public ChronoRevisionIterator(final RevisionAPIConfiguration config,
			final int firstArticleID, final int lastArticleID)
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
	private boolean queryArticle()
		throws SQLException
	{

		Statement statement = this.connection.createStatement();

		String query = "SELECT ArticleID, FullRevisionPKs, RevisionCounter "
				+ "FROM index_articleID_rc_ts " + "WHERE articleID > "
				+ this.currentArticleID + " LIMIT " + MAX_NUMBER_RESULTS;

		resultArticles = statement.executeQuery(query);

		if (resultArticles.next()) {

			this.currentArticleID = resultArticles.getInt(1);
			return (this.lastArticleID == -1)
					|| (this.currentArticleID <= this.lastArticleID);
		}

		return false;
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
	private Revision init()
		throws WikiApiException
	{

		try {
			currentArticleID = resultArticles.getInt(1);
			String fullRevisionPKs = resultArticles.getString(2);
			String revisionCounters = resultArticles.getString(3);

			int index = revisionCounters.lastIndexOf(' ');
			if (index == -1) {
				throw new RuntimeException("Invalid revisioncounter content");
			}

			this.maxRevision = Integer.parseInt(revisionCounters.substring(
					index + 1, revisionCounters.length()));

			Statement statement = null;
			ResultSet result = null;

			try {
				statement = this.connection.createStatement();
				result = statement.executeQuery("SELECT Mapping "
						+ "FROM index_chronological " + "WHERE ArticleID="
						+ currentArticleID + " LIMIT 1");

				if (result.next()) {

					this.modus = ITERATE_WITH_MAPPING;

					this.chronoIterator = new ChronoIterator(config,
							connection, result.getString(1), fullRevisionPKs,
							revisionCounters);

					if (this.chronoIterator.hasNext()) {
						return this.chronoIterator.next();
					}
					else {
						throw new RuntimeException("cIt Revision query failed");
					}

					/*
					 * this.revisionIndex = 1;
					 *
					 * revisionEncoder = new RevisionApi(config, connection);
					 * return revisionEncoder.getRevision(currentArticleID,
					 * revisionIndex);
					 */

				}
				else {

					this.modus = ITERATE_WITHOUT_MAPPING;

					index = fullRevisionPKs.indexOf(' ');
					if (index == -1) {
						index = fullRevisionPKs.length();
					}

					int currentPK = Integer.parseInt(fullRevisionPKs.substring(
							0, index));

					// TODO CHECK! -2 instead of -1 gets rid of the extra
					//				resivsion from the next article
					this.revisionIterator = new RevisionIterator(config,
							currentPK, currentPK + maxRevision - 2,
							connection);

					if (revisionIterator.hasNext()) {
						return revisionIterator.next();
					}
					else {
						throw new RuntimeException("Revision query failed");
					}
				}
			}
			finally {

				if (statement != null) {
					statement.close();
				}
				if (result != null) {
					result.close();
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
			switch (modus) {
			case INIT:
				return init();

			case ITERATE_WITH_MAPPING:
				return chronoIterator.next();

				// revisionEncoder.getRevision(currentArticleID, revisionIndex);

			case ITERATE_WITHOUT_MAPPING:
				return revisionIterator.next();

			default:
				throw new RuntimeException("Illegal mode");
			}
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

				reset();

				if (resultArticles.next()) {

					this.currentArticleID = resultArticles.getInt(1);
					return (this.lastArticleID == -1)
							|| (this.currentArticleID <= this.lastArticleID);
				}

				resultArticles.close();
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

				resultArticles.close();
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
	 * @deprecated
	 * @throws UnsupportedOperationException
	 */
	@Deprecated
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method closes the connection to the input component.
	 *
	 * @throws SQLException
	 *             if an error occurs while closing the connection to the
	 *             database.
	 */
	public void close()
		throws SQLException
	{
		if (this.connection != null) {
			this.connection.close();
		}
	}

	public static void main(final String[] args)
		throws Exception
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
					System.out.println(rev.toString());
				}
				System.out.println(Time.toClock(now) + "\t" + (now - last)
						+ "\tREBUILDING " + count);
				last = now;
			}
		}

		System.out.println(Time.toClock(System.currentTimeMillis() - start));
	}
}
