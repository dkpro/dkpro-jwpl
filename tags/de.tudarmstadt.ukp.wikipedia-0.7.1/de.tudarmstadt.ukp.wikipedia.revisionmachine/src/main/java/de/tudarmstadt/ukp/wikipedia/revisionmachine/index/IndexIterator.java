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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;

/**
 * Iterates over the database to retrieve the necessary information for the
 * index generation.
 * 
 * 
 * 
 */
public class IndexIterator
	implements Iterator<Revision>
{

	/** Reference to the database connection */
	private Connection connection;

	/** Reference to the ResultSet */
	private ResultSet result;

	/** Reference to the statement */
	private Statement statement;

	/** Currently used primary kes */
	private int primaryKey;

	/** Configuration parameter - maximum size of a result set */
	private final int MAX_NUMBER_RESULTS;

	/**
	 * (Constructor) Creates the IndexIterator object.
	 * 
	 * @param config
	 *            Reference to the configuration
	 * 
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public IndexIterator(final RevisionAPIConfiguration config)
		throws WikiApiException
	{

		try {
			this.primaryKey = -1;

			this.statement = null;
			this.result = null;

			String driverDB = "com.mysql.jdbc.Driver";
			Class.forName(driverDB);

			MAX_NUMBER_RESULTS = config.getBufferSize();

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
	 * Queries the database for more revision information.
	 * 
	 * @return TRUE if the resultset contains elements FALSE otherwise
	 * 
	 * @throws SQLException
	 *             if an error occurs while accessing the database
	 */
	private boolean query()
		throws SQLException
	{
		statement = this.connection.createStatement();

		String query = "SELECT PrimaryKey, RevisionCounter,"
				+ " RevisionID, ArticleID, Timestamp, FullRevisionID "
				+ "FROM revisions";

		if (primaryKey > 0) {
			query += " WHERE PrimaryKey > " + primaryKey;
		}

		if (MAX_NUMBER_RESULTS > 0) {
			query += " LIMIT " + MAX_NUMBER_RESULTS;
		}

		result = statement.executeQuery(query);
		return result.next();
	}

	/**
	 * Returns the next revision information. (Does not contain the encoded
	 * diff)
	 * 
	 * @return Revision
	 */
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

			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns TRUE if another revision information is available.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean hasNext()
	{
		try {
			if (result != null && result.next()) {
				return true;
			}

			if (this.statement != null) {
				this.statement.close();
			}
			if (this.result != null) {
				this.result.close();
			}

			return query();

		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * unsupported method
	 * 
	 * @deprecated
	 * @throws UnsupportedOperationException
	 */
	@Deprecated
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
