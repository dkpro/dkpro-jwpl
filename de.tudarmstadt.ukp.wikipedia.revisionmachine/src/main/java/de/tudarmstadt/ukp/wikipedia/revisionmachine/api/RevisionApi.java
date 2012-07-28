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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionDecoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * This class can access the database and retrieve single revisions.
 *
 * @author Simon Kulessa
 * @author Oliver Ferschke
 */
public class RevisionApi
{

	/** Reference to database connection */
	private Connection connection;

	/** Reference to the configuration parameters */
	private final RevisionAPIConfiguration config;

	/**
	 * (Constructor) Creates a new RevisionApi object with an existing database
	 * connection.
	 *
	 * @param config
	 *            Reference to the configuration parameters
	 * @param connection
	 *            Reference to the database connection
	 * @throws WikiApiException
	 */
	public RevisionApi(final RevisionAPIConfiguration config,
			final Connection connection)
		throws WikiApiException
	{

		this.config = config;
		this.connection = connection;
	}

	/**
	 * (Constructor) Creates a new RevisionApi object.
	 *
	 * @param config
	 *            Reference to the configuration parameters
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionApi(final RevisionAPIConfiguration config)
		throws WikiApiException
	{

		this.config = config;
		this.connection = getConnection(config);
	}

	/**
	 * (Constructor) Creates a new RevisionApi object.
	 *
	 * @param config
	 *            A database configuration object
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public RevisionApi(final DatabaseConfiguration dbConfig)
		throws WikiApiException
	{

		RevisionAPIConfiguration config = new RevisionAPIConfiguration();
		config.setHost(dbConfig.getHost());
		config.setDatabase(dbConfig.getDatabase());
		config.setUser(dbConfig.getUser());
		config.setPassword(dbConfig.getPassword());
		config.setLanguage(dbConfig.getLanguage());

		this.config = config;
		this.connection = getConnection(config);
	}

	/**
	 * Returns the number of revisions for the specified article.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return number of revisions
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public int getNumberOfRevisions(final int articleID)
		throws WikiApiException
	{

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			PreparedStatement statement = null;
			ResultSet result = null;
			String revCounters;

			try {
				// Retrieve the fullRevisionPK and calculate the limit
				statement = this.connection
						.prepareStatement("SELECT RevisionCounter "
								+ "FROM index_articleID_rc_ts "
								+ "WHERE ArticleID=? LIMIT 1");
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				if (result.next()) {

					revCounters = result.getString(1);

				}
				else {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
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

			int index = revCounters.lastIndexOf(' ');
			if (index == -1) {
				throw new WikiApiException("Article data is inconsistent");
			}

			return Integer.parseInt(revCounters.substring(index + 1,
					revCounters.length()));

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the timestamps of all revisions that have been made before
	 * the given revision.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return number of revisions
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public List<Timestamp> getRevisionTimestampsBeforeRevision(final int revisionId)
		throws WikiApiException
	{
		List<Timestamp> timestamps = new LinkedList<Timestamp>();

		int articleID = getPageIdForRevisionId(revisionId); //TODO do this in the SQL query
		Timestamp ts = getRevision(revisionId).getTimeStamp(); //TODO do this in the SQL query

		try {
			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				statement = connection.prepareStatement("SELECT Timestamp FROM revisions WHERE ArticleID=? AND Timestamp < "+ts.getTime());
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				// Make the query
				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}
				while (result.next()) {
					timestamps.add(new Timestamp(result.getLong(1)));
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

			return timestamps;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


	/**
	 * Returns the timestamps of all revisions connected to the specified
	 * article.
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return collection of timestampf of all revisions
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public List<Timestamp> getRevisionTimestamps(final int articleID)
		throws WikiApiException
	{

		List<Timestamp> timestamps = new LinkedList<Timestamp>();

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				statement = connection.prepareStatement("SELECT Timestamp "
						+ "FROM revisions WHERE ArticleID=?");
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				// Make the query
				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}
				while (result.next()) {

					timestamps.add(new Timestamp(result.getLong(1)));

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

			return timestamps;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the number of unique contributors to an article based on the
	 * people who revised the article (revision contributors).<br/>
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return the number of unique contributors to the article
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public int getNumberOfUniqueContributors(final int articleID)
	throws WikiApiException{
		return getNumberOfUniqueContributors(articleID, false);
	}

	/**
	 * Returns the number of unique contributors to an article based on the
	 * people who revised the article (revision contributors).<br/>
	 * It is possible to only count the registerd users, if onlyRegistered is set to true.<br/>
	 * <br/>
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param onlyRegistered
	 *            defines whether to count only registered users (true), or all users (false)
	 * @return the number of unique contributors to the article
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public int getNumberOfUniqueContributors(final int articleID, boolean onlyRegistered)
		throws WikiApiException
	{

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			int contrCount=0;
			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				StringBuffer sqlString= new StringBuffer();
				sqlString.append("SELECT COUNT(DISTINCT ContributorName) FROM revisions WHERE ArticleID=?");
				if(onlyRegistered){
					sqlString.append(" AND ContributorIsRegistered=1");
				}

				statement = connection.prepareStatement(sqlString.toString());

				statement.setInt(1, articleID);
				result = statement.executeQuery();

				// Make the query
				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}

				if (result.next()) {
					contrCount=result.getInt(1);
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

			return contrCount;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the number of unique contributors to an article that have contributed
	 * before the given revision.<br/>
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param revisionID
	 *            revision before which to count the contributors
	 * @return the number of unique contributors to the article
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public int getNumberOfUniqueContributorsBeforeRevision(final int revisionID)
	throws WikiApiException{
		return getNumberOfUniqueContributorsBeforeRevision(revisionID, false);
	}


	/**
	 * Returns the number of unique contributors to an article that have contributed
	 * before the given revision.<br/>
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param revisionID
	 *            revision before which to count the contributors
	 * @return the number of unique contributors to the article
	 * @param onlyRegistered
	 *            defines whether to count only registered users (true), or all users (false)
	 * @return the number of unique contributors to the article
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public int getNumberOfUniqueContributorsBeforeRevision(final int revisionID, boolean onlyRegistered)
		throws WikiApiException
	{

		try {
			if (revisionID < 1) {
				throw new IllegalArgumentException();
			}

			int articleID = getPageIdForRevisionId(revisionID);
			Timestamp ts = getRevision(revisionID).getTimeStamp();

			int contrCount=0;
			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				StringBuffer sqlString= new StringBuffer();
				sqlString.append("SELECT COUNT(DISTINCT ContributorName) FROM revisions WHERE ArticleID=? AND Timestamp<?");
				if(onlyRegistered){
					sqlString.append(" AND ContributorIsRegistered=1");
				}

				statement = connection.prepareStatement(sqlString.toString());

				statement.setInt(1, articleID);
				statement.setLong(2, ts.getTime());
				result = statement.executeQuery();

				// Make the query
				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}

				if (result.next()) {
					contrCount=result.getInt(1);
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

			return contrCount;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


	/**
	 * Returns a map of usernames mapped to the timestamps of their contributions
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return map of Timestamp-DiffPart-Collection pairs
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public Map<String, Timestamp> getUserContributionMap(final int articleID)
		throws WikiApiException
	{
		return getUserContributionMap(articleID, null);
	}
	/**
	 * Returns a map of usernames mapped to the timestamps of their contributions
	 *
	 * Users of certain user groups (e.g. bots) can be filtered by providing
	 * the unwanted groups in the {@code groupFilter}. Nothing is filtered if
	 * the {@code groupFilter} is null or empty.<br/>
	 * <br/>
	 * Filtered results also include unregistered users (because they cannot
	 * be filtered using user groups) In order to get results containing only registered
	 * users, use getUserContributionMap(final int articleID, List<String> groupfilter, boolean onlyRegistered) and
	 * set {@code onlyRegistered=true}.<br/>
	 * <br/>
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param groupfilter
	 *            a list of unwanted user groups
	 * @return map of Timestamp-DiffPart-Collection pairs
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public Map<String, Timestamp> getUserContributionMap(final int articleID, String[] groupfilter)
		throws WikiApiException
	{
		return getUserContributionMap(articleID, groupfilter, false);
	}

	/**
	 * Returns a map of usernames mapped to the timestamps of their contributions.<br/>
	 * <br/>
	 * Users of certain user groups (e.g. bots) can be filtered by providing
	 * the unwanted groups in the {@code groupFilter}. Nothing is filtered if
	 * the {@code groupFilter} is null or empty.<br/>
	 * <br/>
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param groupfilter
	 *            a list of unwanted user groups
	 * @param onlyRegistered
	 *            true, if result should only contain registered users. false, else
	 * @return map of Timestamp-DiffPart-Collection pairs
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	@SuppressWarnings("unused")
	public Map<String, Timestamp> getUserContributionMap(final int articleID, String[] groupfilter, boolean onlyRegistered)
		throws WikiApiException
	{

		Map<String, Timestamp> authorTSMap = new HashMap<String, Timestamp>();

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			PreparedStatement statement = null;
			ResultSet result = null;

			try {

				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				StringBuilder statementStr = new StringBuilder();

				if(groupfilter==null||groupfilter.length<1||!tableExists("user_groups")){
					//create statement WITHOUT filter
					statementStr.append("SELECT ContributorName, Timestamp FROM revisions WHERE ArticleID=?");
					statement = connection.prepareStatement(statementStr.toString());
					statement.setInt(1, articleID);
				}else{
					//create statement WITH filter
					statementStr.append("SELECT ContributorName, Timestamp FROM revisions AS rev, user_groups AS ug  WHERE ArticleID=?");
					statementStr.append(" AND rev.ContributorId=ug.ug_user");
					for (String element : groupfilter) {
						statementStr.append(" AND NOT ug.ug_group=?");
					}
					//and combine with results from unregistered users
					if(!onlyRegistered){
						statementStr.append(" UNION ( SELECT ContributorName, Timestamp FROM revisions AS rev WHERE ArticleID=? AND rev.ContributorId IS NULL)");
					}

					statement = connection.prepareStatement(statementStr.toString());
					//insert article id in prepared statement
					statement.setInt(1, articleID);

					//insert filtered groups in prepared statement
					int curPrepStatValueIdx = 2;
					for(String group:groupfilter){
						statement.setString(curPrepStatValueIdx++, group);
					}
					if(!onlyRegistered){
						//insert article id for second select in prepared statement
						statement.setInt(curPrepStatValueIdx, articleID);
					}

				}

				result = statement.executeQuery();

				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}
				while (result.next()) {
					// Write data from current revision to Map
					authorTSMap.put(result.getString(1), new Timestamp(result.getLong(2)));
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

			return authorTSMap;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the group assignments of the specified user
	 *
	 * @param userID
	 *            ID of the user (NOT THE USERNAME)
	 * @return collection of user groups
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public List<String> getUserGroups(final int userID)
		throws WikiApiException
	{

		List<String> groups = new LinkedList<String>();

		try {
			if (userID < 1) {
				throw new IllegalArgumentException();
			}

			if(!tableExists("user_groups")){
				throw new WikiInitializationException(
				"User group assignment data is missing. Please download user_groups.sql for this Wikipedia from http://dumps.wikimedia.org and import the data into this database.");
			}

			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				statement = connection.prepareStatement("SELECT ug_group "
						+ "FROM user_groups WHERE ug_user=?");
				statement.setInt(1, userID);
				result = statement.executeQuery();

				// Make the query
				if (result == null) {
					throw new WikiPageNotFoundException(
							"The user  with the ID " + userID
									+ " was not found.");
				}
				while (result.next()) {

					groups.add(result.getString(1));

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

			return groups;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns a map of timestamps mapped on the corresponding
	 * DiffPart-Collections. Can be used to compile statistics over all changes
	 * that have been made in one article.
	 *
	 * In order to make this query fast, create a MySQL-Index (BTREE) on the
	 * ArticleID in the revisions-table.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return map of Timestamp-DiffPart-Collection pairs
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 * @author Oliver Ferschke
	 */
	public Map<Timestamp, Collection<DiffPart>> getTimestampToRevisionMap(
			final int articleID)
		throws WikiApiException
	{

		Map<Timestamp, Collection<DiffPart>> tsDiffPartsMap = new HashMap<Timestamp, Collection<DiffPart>>();

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			PreparedStatement statement = null;
			ResultSet result = null;
			RevisionDecoder decoder = new RevisionDecoder(
					config.getCharacterSet());

			try {

				// Check if necessary index exists
				if (!indexExists("revisions")) {
					throw new WikiInitializationException(
							"Please create an index on revisions(ArticleID) in order to make this query feasible.");
				}

				statement = connection
						.prepareStatement("SELECT Timestamp, Revision "
								+ "FROM revisions WHERE ArticleID=?");
				;
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				if (result == null) {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
				}
				while (result.next()) {

					// Decode String and create Diff-Object
					boolean binaryData = result.getMetaData().getColumnType(2) == Types.LONGVARBINARY;
					if (binaryData) {
						decoder.setInput(result.getBinaryStream(2), true);
					}
					else {
						decoder.setInput(result.getString(2));
					}
					Diff diff = decoder.decode();

					// Get DiffParts from Diff Object
					Collection<DiffPart> parts = new LinkedList<DiffPart>();
					Iterator<DiffPart> it = diff.iterator();
					while (it.hasNext()) {
						parts.add(it.next());
					}

					// Write data from current revision to Map
					tsDiffPartsMap.put(new Timestamp(result.getLong(1)), parts);

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

			return tsDiffPartsMap;

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the timestamp of the first revision connected to the specified
	 * article.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return first date of appearance or the article does not exist
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public Timestamp getFirstDateOfAppearance(final int articleID)
		throws WikiApiException
	{
		return getDateOfAppearance(articleID, "FirstAppearance");
	}

	/**
	 * Returns the timestamp of the last revision connected to the specified
	 * article.
	 *
	 * @param articleID
	 *            ID of the article
	 * @return last date of appearance or the article does not exist
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public Timestamp getLastDateOfAppearance(final int articleID)
		throws WikiApiException
	{
		return getDateOfAppearance(articleID, "LastAppearance");
	}

	/**
	 * Returns the timestamp of the first or last revision connected to the
	 * specified article.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param firstOrLast
	 *            <code>"FirstAppearance"</code> if first date of appearance
	 *            should be returned. <code>"LastAppearance"</code> if last date
	 *            of appearance should be returned.
	 *
	 * @return first date of appearance or the article does not exist
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	private Timestamp getDateOfAppearance(final int articleID,
			final String firstOrLast)
		throws WikiApiException
	{

		try {
			if (articleID < 1) {
				throw new IllegalArgumentException();
			}

			PreparedStatement statement = null;
			ResultSet result = null;
			long time;

			try {
				statement = this.connection.prepareStatement("SELECT "
						+ firstOrLast + " FROM index_articleID_rc_ts "
						+ "WHERE ArticleID=? LIMIT 1");
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				if (result.next()) {

					time = result.getLong(1);

				}
				else {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
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

			return new Timestamp(time);

		}
		catch (WikiApiException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**(
	 * Returns the by the id specified revision.
	 *
	 * @param revisionID
	 *            ID of the revison
	 * @return Revision
	 *
	 * @throws WikiApiException
	 *             if an error occurs or the revision does not exists.
	 */
	public Revision getRevision(final int revisionID)
		throws WikiApiException
	{

		try {
			if (revisionID < 1) {
				throw new IllegalArgumentException();
			}

			int fullRevPK = -1;
			int limit = 1;

			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				statement = this.connection
						.prepareStatement("SELECT FullRevisionPK, RevisionPK "
								+ "FROM index_revisionID "
								+ "WHERE revisionID=? LIMIT 1");
				statement.setInt(1, revisionID);
				result = statement.executeQuery();

				if (result.next()) {
					fullRevPK = result.getInt(1);
					limit = (result.getInt(2) - fullRevPK) + 1;

				}
				else {
					throw new WikiPageNotFoundException(
							"The revision with the ID " + revisionID
									+ " was not found.");
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

			return buildRevisionMetaData(fullRevPK, limit);

		}
		catch (WikiPageNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


	/**(
	 * Returns the pageId (ArticleId) for the given revision
	 *
	 * @param revisionID
	 *            ID of the revison
	 * @return the page if for the given revision
	 *
	 * @throws WikiApiException
	 *             if an error occurs or the revision does not exists.
	 */
	public int getPageIdForRevisionId(final int revisionID)
		throws WikiApiException
	{

		try {
			if (revisionID < 1) {
				throw new IllegalArgumentException();
			}

			int pageId=-1;

			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				statement = this.connection
						.prepareStatement("SELECT r.ArticleID "
								+ "FROM revisions as r, index_revisionID as idx "
								+ "WHERE idx.RevisionID=? AND idx.RevisionPK=r.PrimaryKey LIMIT 1");
				statement.setInt(1, revisionID);
				result = statement.executeQuery();

				if (result.next()) {
					pageId = result.getInt(1);
				}
				else {
					throw new WikiPageNotFoundException(
							"The revision with the ID " + revisionID
									+ " was not found.");
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

			return pageId;

		}
		catch (WikiPageNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


	/**
	 * Returns the by the article ID and revisionCounter specified revision.
	 * Note that this method returns the revision in chronological order.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param revisionCounter
	 *            number of revision
	 * @return Revision
	 *
	 * @throws WikiApiException
	 *             if an error occurs or the revision does not exists.
	 */
	public Revision getRevision(final int articleID, final int revisionCounter)
		throws WikiApiException
	{

		try {
			if (articleID < 1 || revisionCounter < 1) {
				throw new IllegalArgumentException();
			}

			int revisionIndex = checkMapping(articleID, revisionCounter);
			String fullRevisions, revCounters;

			PreparedStatement statement = null;
			ResultSet result = null;

			try {
				statement = this.connection
						.prepareStatement("SELECT FullRevisionPKs, RevisionCounter FROM index_articleID_rc_ts WHERE ArticleID=? LIMIT 1");
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				if (result.next()) {

					fullRevisions = result.getString(1);
					revCounters = result.getString(2);

				}
				else {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
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

			return getReferencedRevision(articleID, revisionIndex,
					fullRevisions, revCounters);

		}
		catch (WikiPageNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/**
	 * Returns the by the article ID and timestamp specified revision. Note that
	 * the timestamp is not an unique identifier of a revision related to an
	 * article. The returned revision should be the first revision that can be
	 * found inside the database.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param time
	 *            Timestamp
	 * @return Revision
	 *
	 * @throws WikiApiException
	 *             if an error occurs or the revision does not exists.
	 */
	public Revision getRevision(final int articleID, final Timestamp time)
		throws WikiApiException
	{

		try {

			PreparedStatement statement = null;
			ResultSet result = null;
			String fullRevisions;
			String revisionCounters;

			if (articleID < 1 || time == null || time.getTime() <= 0) {
				throw new IllegalArgumentException();
			}

			int firstPK = -1, lastPK = -1;
			try {
				statement = this.connection
						.prepareStatement("SELECT FullRevisionPKs, RevisionCounter,"
								+ " FirstAppearance "
								+ "FROM index_articleID_rc_ts "
								+ "WHERE ArticleID=? LIMIT 1");
				statement.setInt(1, articleID);
				result = statement.executeQuery();

				if (result.next()) {

					fullRevisions = result.getString(1);
					revisionCounters = result.getString(2);
					long firstDate = result.getLong(3);

					// Find first and last FullRevision PK
					int max = fullRevisions.length();
					int index = fullRevisions.indexOf(' ');
					if (index == -1) {
						index = max;
					}

					firstPK = Integer.parseInt(fullRevisions
							.substring(0, index));

					index = revisionCounters.lastIndexOf(' ') + 1;
					lastPK = firstPK
							+ Integer.parseInt(revisionCounters.substring(
									index, revisionCounters.length()));

					if (time.getTime() < firstDate) {
						throw new WikiPageNotFoundException(
								"No revision before the " + "specified date ["
										+ time + "]");
					}
				}
				else {
					throw new WikiPageNotFoundException(
							"The article with the ID " + articleID
									+ " was not found.");
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
			try {
				statement = this.connection
						.prepareStatement("SELECT RevisionCounter FROM revisions WHERE PrimaryKey >= ? AND PrimaryKey < ? AND Timestamp <= ? ORDER BY Timestamp DESC LIMIT 1");
				statement.setInt(1, firstPK);
				statement.setInt(2, lastPK);
				statement.setLong(3, time.getTime());
				result = statement.executeQuery();

				if (result.next()) {
					int revisionCount = result.getInt(1);
					return getReferencedRevision(articleID, revisionCount,
							fullRevisions, revisionCounters);
				}
				else {
					throw new WikiPageNotFoundException(
							"The revision with the specified timestamp was not found.");
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
		catch (WikiPageNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}

	/*--------------------------------------------------------------------------*/
	/* Internal methods */
	/*--------------------------------------------------------------------------*/

	/**
	 * This method maps the chronological order to the revisionCounter.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param revisionCounter
	 *            chronological position
	 *
	 * @return position in the chronological order
	 *
	 * @throws SQLException
	 *             if an error occurs while accesing the database.
	 */
	protected int checkMapping(final int articleID, final int revisionCounter)
		throws SQLException
	{

		PreparedStatement statement = null;
		ResultSet result = null;

		// Check for the correct revisionCounter mapping
		try {
			statement = this.connection
					.prepareStatement("SELECT Mapping "
							+ "FROM index_chronological "
							+ "WHERE ArticleID=? LIMIT 1");
			statement.setInt(1, articleID);
			result = statement.executeQuery();

			if (result.next()) {

				String mapping = result.getString(1);
				return getMapping(mapping, revisionCounter);

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

		return revisionCounter;
	}

	/**
	 * This method maps the revisionCounter to the chronological order.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param revisionCounter
	 *            chronological position
	 *
	 * @return position in the chronological order
	 *
	 * @throws SQLException
	 *             if an error occurs while accesing the database.
	 *
	 * @deprecated this method should only be used for internal processes
	 */
	@Deprecated
	public int checkReverseMapping(final int articleID,
			final int revisionCounter)
		throws SQLException
	{

		PreparedStatement statement = null;
		ResultSet result = null;

		// Check for the correct revisionCounter mapping
		try {
			statement = this.connection
					.prepareStatement("SELECT ReverseMapping FROM index_chronological WHERE ArticleID=? LIMIT 1");
			statement.setInt(1, articleID);
			result = statement.executeQuery();

			if (result.next()) {

				String mapping = result.getString(1);
				return getMapping(mapping, revisionCounter);

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

		return revisionCounter;
	}

	/**
	 * This method returns the correct mapping of the given input.
	 *
	 * @param mapping
	 *            mapping sequence
	 * @param revisionCounter
	 *            index to map
	 * @return mapped index
	 */
	private int getMapping(final String mapping, final int revisionCounter)
	{

		String tempA, tempB;

		int length = 0;
		int revC = -1, mapC = -1;
		int index, max = mapping.length();

		while (length < max && revC < revisionCounter) {

			// Read revisionCounter
			index = mapping.indexOf(' ', length);
			tempA = mapping.substring(length, index);
			length = index + 1;

			// Read mappedCounter
			index = mapping.indexOf(' ', length);
			if (index == -1) {
				index = mapping.length();
			}
			tempB = mapping.substring(length, index);
			length = index + 1;

			// Parse values
			revC = Integer.parseInt(tempA);
			mapC = Integer.parseInt(tempB);

			// System.out.println(revC + " -> " + mapC);
		}

		if (revC == revisionCounter) {
			// System.out.println(revC + " >> " + mapC);
			return mapC;
		}

		return revisionCounter;
	}

	/**
	 * This method identifies the correct full revision and retrieves the
	 * reference revision.
	 *
	 * @param articleID
	 *            ID of the article
	 * @param revisionIndex
	 *            number of revision
	 * @param fullRevisions
	 *            list of full revisions
	 * @param revCounters
	 *            list of revision counters
	 * @return Revision
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	private Revision getReferencedRevision(final int articleID,
			final int revisionIndex, final String fullRevisions,
			final String revCounters)
		throws WikiApiException
	{

		try {
			int fullRevPK = -1;
			int limit = 1;

			String fullRev = null;

			int revA = -1, revB = -1;
			int lengthFR = 0;
			int lengthRC = 0;
			int index;
			int max = fullRevisions.length();

			while (lengthFR < max && revB < revisionIndex) {

				// Read fullRevisionPK (as string)
				index = fullRevisions.indexOf(' ', lengthFR);
				if (index == -1) {
					index = max;
				}

				fullRev = fullRevisions.substring(lengthFR, index);
				lengthFR = index + 1;

				// Read start revision counter
				index = revCounters.indexOf(' ', lengthRC);
				revA = Integer.parseInt(revCounters.substring(lengthRC, index));
				lengthRC = index + 1;

				// Read end revision counter
				index = revCounters.indexOf(' ', lengthRC);
				if (index == -1) {
					index = revCounters.length();
				}
				revB = Integer.parseInt(revCounters.substring(lengthRC, index));
				lengthRC = index + 1;
			}

			if (revisionIndex > revB) {
				throw new WikiPageNotFoundException("The article with the ID "
						+ articleID + " has no revision number "
						+ revisionIndex);
			}

			fullRevPK = Integer.parseInt(fullRev);
			limit = (revisionIndex - revA) + 1;

			// Build the revision
			return buildRevisionMetaData(fullRevPK, limit);

		}
		catch (WikiPageNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			throw new WikiApiException(e);
		}
	}


	/**
	 * This method queries and builds the specified revision.
	 *
	 * @param fullRevPK
	 *            PK of the full revision
	 * @param limit
	 *            number of revision to query
	 * @return Revision
	 *
	 * @throws SQLException
	 *             if an error occurs while retrieving data from the sql
	 *             database.
	 * @throws IOException
	 *             if an error occurs while reading the content stream
	 * @throws DecodingException
	 *             if an error occurs while decoding the content of the revision
	 * @throws WikiPageNotFoundException
	 *             if the revision was not found
	 */
	public void setRevisionTextAndParts(Revision revision)
	{

		//TODO: referenced
		PreparedStatement statement = null;
		ResultSet result = null;

		try {

			int fullRevPK = -1;
			int limit = 1;
			try {
				statement = this.connection
						.prepareStatement("SELECT FullRevisionPK, RevisionPK "
								+ "FROM index_revisionID "
								+ "WHERE revisionID=? LIMIT 1");
				statement.setInt(1, revision.getRevisionID());
				result = statement.executeQuery();

				if (result.next()) {
					fullRevPK = result.getInt(1);
					limit = (result.getInt(2) - fullRevPK) + 1;

				}
				else {
					throw new WikiPageNotFoundException(
							"The revision with the ID "
									+ revision.getRevisionID()
									+ " was not found.");
				}

				statement = this.connection
						.prepareStatement("SELECT Revision, PrimaryKey, RevisionCounter, RevisionID, ArticleID, Timestamp, Comment, Minor, ContributorName, ContributorId, ContributorIsRegistered "
								+ "FROM revisions "
								+ "WHERE PrimaryKey >= ? LIMIT " + limit);
				statement.setInt(1, fullRevPK);
				result = statement.executeQuery();

				String previousRevision = null, currentRevision = null;

				Diff diff = null;
				RevisionDecoder decoder;

				boolean binaryData = result.getMetaData().getColumnType(1) == Types.LONGVARBINARY;

				while (result.next()) {

					decoder = new RevisionDecoder(config.getCharacterSet());

					if (binaryData) {
						decoder.setInput(result.getBinaryStream(1), true);
					}
					else {
						decoder.setInput(result.getString(1));
					}

					diff = decoder.decode();
					currentRevision = diff.buildRevision(previousRevision);

					previousRevision = currentRevision;


				}

				Collection<DiffPart> parts = new LinkedList<DiffPart>();
				Iterator<DiffPart> it = diff.iterator();
				while (it.hasNext()) {
					parts.add(it.next());
				}
				revision.setParts(parts);

				revision.setRevisionText(currentRevision);

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
		catch (WikiPageNotFoundException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}



	/**
	 * This method queries and builds the specified revision.
	 *
	 * @param fullRevPK
	 *            PK of the full revision
	 * @param limit
	 *            number of revision to query
	 * @return Revision
	 *
	 * @throws SQLException
	 *             if an error occurs while retrieving data from the sql
	 *             database.
	 * @throws IOException
	 *             if an error occurs while reading the content stream
	 * @throws DecodingException
	 *             if an error occurs while decoding the content of the revision
	 * @throws WikiPageNotFoundException
	 *             if the revision was not found
	 */
//	private Revision buildRevision(final int fullRevPK, final int limit)
//		throws SQLException, IOException, DecodingException,
//		WikiPageNotFoundException
//	{
//
//		PreparedStatement statement = null;
//		ResultSet result = null;
//		// System.out.println("buildRevision -- PK: " + fullRevPK + "\tLimit: "
//		// + limit);
//		try {
//			statement = this.connection
//					.prepareStatement("SELECT Revision, PrimaryKey, RevisionCounter, RevisionID, ArticleID, Timestamp, Comment, Minor, ContributorName, ContributorId, ContributorIsRegistered "
//							+ "FROM revisions "
//							+ "WHERE PrimaryKey >= ? LIMIT " + limit);
//			statement.setInt(1, fullRevPK);
//			result = statement.executeQuery();
//
//			String previousRevision = null, currentRevision = null;
//
//			Diff diff;
//			RevisionDecoder decoder;
//			Revision revision = null;
//
//			boolean binaryData = result.getMetaData().getColumnType(1) == Types.LONGVARBINARY;
//
//			while (result.next()) {
//
//				decoder = new RevisionDecoder(config.getCharacterSet());
//
//				if (binaryData) {
//					decoder.setInput(result.getBinaryStream(1), true);
//				}
//				else {
//					decoder.setInput(result.getString(1));
//				}
//
//				diff = decoder.decode();
//				currentRevision = diff.buildRevision(previousRevision);
//
//				previousRevision = currentRevision;
//
//				revision = new Revision(result.getInt(3), this);
//				revision.setRevisionText(currentRevision);
//				revision.setPrimaryKey(result.getInt(2));
//				revision.setRevisionID(result.getInt(4));
//				revision.setArticleID(result.getInt(5));
//				revision.setTimeStamp(new Timestamp(result.getLong(6)));
//				revision.setComment(result.getString(7));
//				revision.setMinor(result.getBoolean(8));
//				revision.setContributorName(result.getString(9));
//
//				//we should not use getInt(), because result may be null
//				String contribIdString = result.getString(10);
//				Integer contributorId=contribIdString==null?null:Integer.parseInt(contribIdString);
//				revision.setContributorId(contributorId);
//
//				revision.setContributorIsRegistered(result.getBoolean(11));
//				Collection<DiffPart> parts = new LinkedList<DiffPart>();
//				Iterator<DiffPart> it = diff.iterator();
//				while (it.hasNext()) {
//					parts.add(it.next());
//				}
//				revision.setParts(parts);
//			}
//
//			return revision;
//
//		}
//		finally {
//			if (statement != null) {
//				statement.close();
//			}
//			if (result != null) {
//				result.close();
//			}
//		}
//	}


	/**
	 * This method queries and builds the specified revision.
	 *
	 * @param fullRevPK
	 *            PK of the full revision
	 * @param limit
	 *            number of revision to query
	 * @return Revision
	 *
	 * @throws SQLException
	 *             if an error occurs while retrieving data from the sql
	 *             database.
	 * @throws IOException
	 *             if an error occurs while reading the content stream
	 * @throws DecodingException
	 *             if an error occurs while decoding the content of the revision
	 * @throws WikiPageNotFoundException
	 *             if the revision was not found
	 */
	private Revision buildRevisionMetaData(final int fullRevPK, final int limit) throws SQLException{

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = this.connection
					.prepareStatement("SELECT Revision, PrimaryKey, RevisionCounter, RevisionID, ArticleID, Timestamp, Comment, Minor, ContributorName, ContributorId, ContributorIsRegistered "
							+ "FROM revisions "
							+ "WHERE PrimaryKey >= ? LIMIT " + limit);
			statement.setInt(1, fullRevPK);
			result = statement.executeQuery();

			Revision revision = null;
			if(result.last()) {
				revision = new Revision(result.getInt(3), this);

				revision.setPrimaryKey(result.getInt(2));
				revision.setRevisionID(result.getInt(4));
				revision.setArticleID(result.getInt(5));
				revision.setTimeStamp(new Timestamp(result.getLong(6)));
				revision.setComment(result.getString(7));
				revision.setMinor(result.getBoolean(8));
				revision.setContributorName(result.getString(9));

				//we should not use getInt(), because result may be null
				String contribIdString = result.getString(10);
				Integer contributorId=contribIdString==null?null:Integer.parseInt(contribIdString);
				revision.setContributorId(contributorId);

				revision.setContributorIsRegistered(result.getBoolean(11));
			}
			return revision;

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

	private Connection getConnection(RevisionAPIConfiguration config)
		throws WikiApiException
	{
		Connection c;
		try {

			String driverDB = "com.mysql.jdbc.Driver";
			Class.forName(driverDB);

			c = DriverManager.getConnection("jdbc:mysql://" + config.getHost()
					+ "/" + config.getDatabase(), config.getUser(),
					config.getPassword());
			if (!c.isValid(5)) {
				throw new WikiApiException(
						"Connection could not be established.");
			}
		}
		catch (SQLException e) {
			throw new WikiApiException(e);
		}
		catch (ClassNotFoundException e) {
			throw new WikiApiException(e);
		}

		return c;
	}

	/**
	 * This method closes the connection to the database.
	 *
	 * @throws SQLException
	 *             if an error occurs while closing the connection
	 */
	public void close()
		throws SQLException
	{
		if (this.connection != null) {
			this.connection.close();
		}
	}

	public void reconnect() throws SQLException{
		close();
		try{
			this.connection=getConnection(config);
		}catch(WikiApiException e){
			close();
			System.err.println("Could not reconnect. Closing connection...");
		}
	}
	
	/**
	 * Checks if some index (besides the PRIMARY-Index) exists in a given table.
	 *
	 * @param table
	 *            the table to check
	 * @return true, if index exists, false else
	 * @throws SQLException
	 *             if an error occurs connecting to or querying the db
	 */
	private boolean indexExists(String table)
		throws SQLException
	{
		return indexExists(table, null);
	}

	/**
	 * Checks if an index with a specific name exists in a given table.
	 *
	 * @param table
	 *            the table to check
	 * @param indexName
	 *            the name of the index (may be null)
	 * @return true, if index exists, false else
	 * @throws SQLException
	 *             if an error occurs connecting to or querying the db
	 * @author Oliver Ferschke
	 */
	private boolean indexExists(String table, String indexName)
		throws SQLException
	{

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = this.connection.prepareStatement("SHOW INDEX FROM "
					+ table + " WHERE Key_name!= 'PRIMARY'");
			result = statement.executeQuery();

			// Check if an index exists (because otherwise the query would
			// be awfully slow. Note that the existence of ANY index will
			// suffice - we might want to check for a specific index.
			if (result == null || !result.next()) {
				return false;
			}

			/*
			 * SOME INDEX EXISTS! We can now check for the existence of a
			 * specific index
			 */
			if (indexName != null) {
				// go back to first result

				result.first();
				// check all existing indexes for the specific index name
				boolean specificIndexExists = false;
				while (result.next()) {
					if (result.getString(3).equals(indexName)) {
						specificIndexExists = true;
					}
				}
				return specificIndexExists ? true : false;

			}
			else {
				// we have an index, but don't want to check for an index with
				// a specific name

				return true;
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

	/**
	 * Checks if a specific table exists
	 *
	 * @param table
	 *            the table to check

	 * @return true, if table exists, false else
	 * @throws SQLException
	 *             if an error occurs connecting to or querying the db
	 * @author Oliver Ferschke
	 */
	private boolean tableExists(String table)
		throws SQLException
	{

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = this.connection.prepareStatement("SHOW TABLES;");
			result = statement.executeQuery();

			if (result == null) {
				return false;
			}
			boolean found = false;
			while(result.next()){
					if(table.equalsIgnoreCase(result.getString(1))){
						found = true;
					}
			}
			return found;

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


	public static void main(String[] args)
		throws Exception
	{

		RevisionAPIConfiguration config = new RevisionAPIConfiguration();

		config.setHost("localhost");
		config.setDatabase("en_wiki");
		config.setUser("root");
		config.setPassword("1234");

		config.setCharacterSet("UTF-8");
		config.setBufferSize(20000);
		config.setMaxAllowedPacket(1024 * 1024);

		RevisionApi rev = new RevisionApi(config);

		Revision r;

		// System.out.println(rev.getNumberOfRevisions(12));
		// System.out.println(rev.getFirstDateOfAppearance(12));
		// System.out.println(rev.getLastDateOfAppearance(12));

		// r = rev.getRevision(31596, new Timestamp(1011743960000l));
		r = rev.getRevision(233181);

		System.out.println(r.toString() + "\t" + r.getRevisionText());
		// System.out.println(rev.getRevision(979005).getRevisionText());
		// System.out.println(rev.getRevision(2, new
		// Timestamp(1216747716000l)).getRevisionText());

	}
}
