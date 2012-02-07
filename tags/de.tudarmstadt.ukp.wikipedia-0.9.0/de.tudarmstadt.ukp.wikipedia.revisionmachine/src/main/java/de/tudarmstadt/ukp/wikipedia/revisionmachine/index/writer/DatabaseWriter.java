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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.AbstractIndex;

/**
 * This class writes the output of the index generator to a database.
 *
 *
 *
 */
public class DatabaseWriter
	implements IndexWriterInterface
{

	/** Reference to the database connection */
	private final Connection connection;

	/**
	 * (Constructor) Creates a new DatabaseWriter.
	 *
	 * @param config
	 *            Reference to the configuration paramters
	 *
	 * @throws ClassNotFoundException
	 *             if the JDBC Driver could not be located
	 *
	 * @throws SQLException
	 *             if an error occured while creating the index tables
	 */
	public DatabaseWriter(final RevisionAPIConfiguration config)
		throws ClassNotFoundException, SQLException
	{

		String driverDB = "com.mysql.jdbc.Driver";
		Class.forName(driverDB);

		this.connection = DriverManager
				.getConnection("jdbc:mysql://" + config.getHost() + "/"
						+ config.getDatabase(), config.getUser(),
						config.getPassword());

		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE index_articleID_rc_ts ("
				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
				+ "FullRevisionPKs MEDIUMTEXT NOT NULL, "
				+ "RevisionCounter MEDIUMTEXT NOT NULL, "
				+ "FirstAppearance BIGINT NOT NULL, "
				+ "LastAppearance BIGINT NOT NULL, "
				+ "PRIMARY KEY(ArticleID));");
		statement.close();

		statement = connection.createStatement();
		statement.execute("CREATE TABLE index_revisionID ("
				+ "RevisionID INTEGER UNSIGNED NOT NULL, "
				+ "RevisionPK INTEGER UNSIGNED NOT NULL, "
				+ "FullRevisionPK INTEGER UNSIGNED NOT NULL, "
				+ "PRIMARY KEY(RevisionID));");
		statement.close();

		statement = connection.createStatement();
		statement.execute("CREATE TABLE index_chronological ("
				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
				+ "Mapping MEDIUMTEXT NOT NULL, "
				+ "ReverseMapping MEDIUMTEXT NOT NULL, "
				+ "PRIMARY KEY(ArticleID));");
		statement.close();

		//disable keys now - reenable after inserts

		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_articleID_rc_ts DISABLE KEYS;");
		statement.close();
		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_revisionID DISABLE KEYS;");
		statement.close();

		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_chronological DISABLE KEYS;");
		statement.close();
	}

	/**
	 * Writes the buffered finalzed queries to the output.
	 *
	 * @param index
	 *            Reference to an index
	 * @throws SQLException
	 *             if an error occured while transmitting the output
	 */
	public void write(final AbstractIndex index)
		throws SQLException
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
	 * Wraps up the index generation process and writes all remaining statements
	 * e.g. concerning UNCOMPRESSED-Indexes on the created tables.
	 *
	 * @throws SQLException
	 *             if an error occured while accessing the database
	 */
	public void finish() throws SQLException{
		Statement statement = connection.createStatement();
		statement.execute("CREATE INDEX articleIdx on revisions(ArticleID);");
		statement.close();
		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_articleID_rc_ts ENABLE KEYS;");
		statement.close();
		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_revisionID ENABLE KEYS;");
		statement.close();
		statement = connection.createStatement();
		statement.execute("ALTER TABLE index_chronological ENABLE KEYS;");
		statement.close();
	}

	/**
	 * Closes the file or the database connection.
	 *
	 * @throws SQLException
	 *             if an error occured while closing the database connection
	 */
	public void close()
		throws SQLException
	{
		this.connection.close();
	}
}
