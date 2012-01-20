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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.WriterInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.DataFileEncoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class writes the output to a data file (not an sql file)
 */
public class DataFileWriter
	implements WriterInterface
{

	/** File counter */
	private int fileCounter;

	/** Configuration parameter - maximum size of an output file */
	private final long LIMIT_SQL_FILE_SIZE;

	/**
	 * Configuration parameter - Flag, that indicates whether the statistical
	 * output is enabled or not
	 */
	private final boolean MODE_STATISTICAL_OUTPUT;

	/**
	 * Name of the related sql consumer - used as prefix for the output
	 * filenames
	 */
	private String outputName;

	/** Configuration parameter - output path */
	private final String PATH_OUTPUT_DATA_FILES;

	/** Reference to the DataFileEncoder */
	protected DataFileEncoder dataFileEncoder;

	/** Reference to the output file */
	private File dataFile;

	/** Reference to the file writer */
	private Writer writer;

	private final String WIKIPEDIA_ENCODING;

	/**
	 * (Constructor) Creates a new SQLFileWriter object.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	private DataFileWriter()
		throws ConfigurationException
	{

		// Load config parameters
		ConfigurationManager config = ConfigurationManager.getInstance();

		LIMIT_SQL_FILE_SIZE = (Long) config
				.getConfigParameter(ConfigurationKeys.LIMIT_SQL_FILE_SIZE);

		PATH_OUTPUT_DATA_FILES = (String) config
				.getConfigParameter(ConfigurationKeys.PATH_OUTPUT_SQL_FILES);

		MODE_STATISTICAL_OUTPUT = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

		WIKIPEDIA_ENCODING = (String) config
		.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);

		// Create sql file
		fileCounter = 0;
	}


	/**
	 * (Constructor) Creates a new SQLFileWriter object.
	 *
	 * @param outputName
	 *            Name of the sql consumer
	 * @param logger
	 *            Reference to a logger
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while accessing the logger
	 */
	public DataFileWriter(final String outputName)
		throws IOException, ConfigurationException, LoggingException
	{

		this();

		this.outputName = outputName;

		init();
		writeHeader();
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
		throws IOException
	{
		this.writer.close();
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

		this.dataFileEncoder = new DataFileEncoder();
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

		try {
			List<String> data = dataFileEncoder.encodeTask(task);

			for (String d : data) {
				this.writer.write(d + ";");
				this.writer.flush();
			}

			if (task.getTaskType() == TaskTypes.TASK_FULL
					|| task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {

				if (this.dataFile.length() > LIMIT_SQL_FILE_SIZE) {
					writeHeader();
				}

				if (!MODE_STATISTICAL_OUTPUT) {
					System.out.println(task.toString());
				}

			}
			else {
				System.out.println(task.toString());
			}

		}
		catch (DecodingException e) {

			throw ErrorFactory.createSQLConsumerException(
					ErrorKeys.DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION, e);

		}
		catch (EncodingException e) {

			throw ErrorFactory.createSQLConsumerException(
					ErrorKeys.DIFFTOOL_SQLCONSUMER_FILEWRITER_EXCEPTION, e);
		}
	}

	/**
	 * Creates a new output file and writes the header information.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws IOException
	 *             if an error occurred while writing a file
	 */
	protected void writeHeader()
		throws ConfigurationException, IOException
	{

		if (writer != null) {
			writer.close();
		}

		this.fileCounter++;
		String filePath = PATH_OUTPUT_DATA_FILES + this.outputName + "_"
				+ fileCounter+".csv";

		this.dataFile = new File(filePath);

		this.writer = new BufferedWriter(new OutputStreamWriter(
		        new FileOutputStream(filePath), WIKIPEDIA_ENCODING));


		this.writer.flush();
	}
}
