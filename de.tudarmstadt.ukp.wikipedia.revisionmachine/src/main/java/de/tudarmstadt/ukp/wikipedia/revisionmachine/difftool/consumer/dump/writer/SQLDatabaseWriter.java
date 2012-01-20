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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.writer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.WriterInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoding;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class writes the output to a database.
 *
 *
 *
 */
public class SQLDatabaseWriter
	implements WriterInterface
{

	/** Reference to the database connection */
	private Connection connection;

	/** Reference to the logger */
	protected Logger logger;

	/** Reference to the sql encoder */
	protected SQLEncoderInterface sqlEncoder;

	/**
	 * (Constructor) Creates a new SQLDatabaseWriter object.
	 *
	 * @param logger
	 *            Reference to the logger
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while accessing the logger
	 */
	public SQLDatabaseWriter(final Logger logger)
		throws ConfigurationException, LoggingException
	{

		this.logger = logger;

		ConfigurationManager config = ConfigurationManager.getInstance();

		String host = (String) config
				.getConfigParameter(ConfigurationKeys.SQL_HOST);
		String user = (String) config
				.getConfigParameter(ConfigurationKeys.SQL_USERNAME);
		String password = (String) config
				.getConfigParameter(ConfigurationKeys.SQL_PASSWORD);
		String sTable = (String) config
				.getConfigParameter(ConfigurationKeys.SQL_DATABASE);

		try {
			String driverDB = "com.mysql.jdbc.Driver";
			Class.forName(driverDB);

			this.connection = DriverManager.getConnection("jdbc:mysql://"
					+ host + "/" + sTable, user, password);

			init();
			writeHeader();

		}
		catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		}
		catch (SQLException e) {
			throw new ConfigurationException(e);
		}
	}

	/**
	 * This method will close the connection to the output.
	 *
	 * @throws IOException
	 *             if problems occurred while closing the file or process.
	 *
	 * @throws SQLConsumerException
	 *             if problems occurred while closing the connection to the
	 *             database.
	 */
	@Override
	public void close()
		throws SQLException
	{
		this.connection.close();
		this.connection = null;
	}

	/**
	 * Creates the sql encoder.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while accessing the logger
	 */
	protected void init()
		throws ConfigurationException, LoggingException
	{

		this.sqlEncoder = new SQLEncoder(logger);
	}

	/**
	 * This method will process the given DiffTask and send it to the specified
	 * output.
	 *
	 * @param task
	 *            DiffTask
	 *
	 * @throws ConfigurationException
	 *             if problems occurred while initializing the components
	 *
	 * @throws IOException
	 *             if problems occurred while writing the output (to file or
	 *             archive)
	 *
	 * @throws SQLConsumerException
	 *             if problems occurred while writing the output (to the sql
	 *             producer database)
	 */
	@Override
	public void process(final Task<Diff> task)
		throws ConfigurationException, IOException, SQLConsumerException
	{

		int i = -1;
		SQLEncoding[] queries = null;

		try {
				queries = sqlEncoder.encodeTask(task);

				Statement query;
				int size = queries.length;
				for (i = 0; i < size; i++) {

					query = connection.createStatement();
					query.executeUpdate(queries[i].getQuery());
					query.close();
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
					ErrorKeys.DIFFTOOL_SQLCONSUMER_DATABASEWRITER_EXCEPTION, q,
					e);

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
	 * Retrieves the encoded sql orders and executes them.
	 *
	 * @throws SQLException
	 *             if an error occurred while accessing the database
	 */
	private void writeHeader()
		throws SQLException
	{

		Statement query;
		String[] revTableHeaderQueries;

		revTableHeaderQueries = sqlEncoder.getTable();

		//commit revision table header
		for (String revTableHeaderQuery : revTableHeaderQueries) {
			query = connection.createStatement();

			query.executeUpdate(revTableHeaderQuery);
			query.close();
		}

	}
}
