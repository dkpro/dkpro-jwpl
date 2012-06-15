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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Surrogates;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.WikipediaXMLWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.SurrogateModes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionCodecData;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionDecoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionEncoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionEncoderInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This creates the SQL statements
 *	@author Oliver Ferschke
 *
 */
public class SQLEncoder
	implements SQLEncoderInterface
{

	/** UNCOMPRESSED Statement for tables containing binary encoded diff information */
	private final String binaryTableRevision;

	/** Reference to the RevisionApi */
	private final RevisionEncoderInterface encoder;

	/** Last used ID of a full revision */
	private int lastFullRevID = -1;

	/** Configuration parameter - Maximum size of a sql statement */
	private final long LIMIT_SQL_STATEMENT_SIZE;

	/** Reference to the logger */
	private final Logger logger;

	/** Configuration parameter - Path for the debug logger */
	private final String LOGGING_PATH_DEBUG;

	/** Configuration parameter - Path for the DiffTool logger */
	private final String LOGGING_PATH_DIFFTOOL;

	/**
	 * Configuration parameter - Flag, which indicates whether debug output is
	 * enabled or not
	 */
	private final boolean MODE_DEBUG_OUTPUT_ACTIVATED;

	/** Configuration parameter - Surrogate Mode */
	private final SurrogateModes MODE_SURROGATES;

	/** UNCOMPRESSED Statement for tables containing base 64 encoded diff information */
	private final String tableRevision;

	/**
	 * Configuration parameter - Flag, which indicates whether the verification
	 * of the encoding is enabled or not
	 */
	private final boolean VERIFICATION_ENCODING;

	/** Configuration Parameter - Wikipedia Encoding */
	private final String WIKIPEDIA_ENCODING;

	/**
	 * (Constructor) Creates a new SQLEncoder object.
	 *
	 * @param logger
	 *            Reference to the logger
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while accessing the logger
	 */
	public SQLEncoder(final Logger logger)
		throws ConfigurationException, LoggingException
	{

		this.logger = logger;

		// Load config parameters
		ConfigurationManager config = ConfigurationManager.getInstance();

		MODE_DEBUG_OUTPUT_ACTIVATED = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_DEBUG_OUTPUT);

		VERIFICATION_ENCODING = (Boolean) config
				.getConfigParameter(ConfigurationKeys.VERIFICATION_ENCODING);

		LOGGING_PATH_DIFFTOOL = (String) config
				.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL);

		LOGGING_PATH_DEBUG = (String) config
				.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DEBUG);

		LIMIT_SQL_STATEMENT_SIZE = (Long) config
				.getConfigParameter(ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET);

		MODE_SURROGATES = (SurrogateModes) config
				.getConfigParameter(ConfigurationKeys.MODE_SURROGATES);

		WIKIPEDIA_ENCODING = (String) config
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);

		this.encoder = new RevisionEncoder();

		tableRevision = "CREATE TABLE IF NOT EXISTS revisions ("
				+ "PrimaryKey INT UNSIGNED NOT NULL AUTO_INCREMENT, "
				+ "FullRevisionID INTEGER UNSIGNED NOT NULL, "
				+ "RevisionCounter INTEGER UNSIGNED NOT NULL, "
				+ "RevisionID INTEGER UNSIGNED NOT NULL, "
				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
				+ "Timestamp BIGINT NOT NULL, "
				+ "Revision MEDIUMTEXT NOT NULL, "
				+ "Comment MEDIUMTEXT, "
				+ "Minor TINYINT NOT NULL, "
				+ "ContributorName TEXT NOT NULL, "
				+ "ContributorId INTEGER UNSIGNED, "
				+ "ContributorIsRegistered TINYINT NOT NULL, "
				+ "PRIMARY KEY(PrimaryKey)"
				+ ") TYPE = MyISAM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;";

		binaryTableRevision = "CREATE TABLE IF NOT EXISTS revisions ("
				+ "PrimaryKey INT UNSIGNED NOT NULL AUTO_INCREMENT, "
				+ "FullRevisionID INTEGER UNSIGNED NOT NULL, "
				+ "RevisionCounter INTEGER UNSIGNED NOT NULL, "
				+ "RevisionID INTEGER UNSIGNED NOT NULL, "
				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
				+ "Timestamp BIGINT NOT NULL, "
				+ "Revision MEDIUMBLOB NOT NULL,"
				+ "Comment MEDIUMTEXT, "
				+ "Minor TINYINT NOT NULL, "
				+ "ContributorName TEXT NOT NULL, "
				+ "ContributorId INTEGER UNSIGNED, "
				+ "ContributorIsRegistered TINYINT NOT NULL, "
				+ "PRIMARY KEY(PrimaryKey)"
				+ ") TYPE = MyISAM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;";

	}

	/**
	 * @param task
	 * @param diff
	 * @return
	 * @throws ConfigurationException
	 * @throws UnsupportedEncodingException
	 * @throws DecodingException
	 * @throws EncodingException
	 * @throws SQLConsumerException
	 */
	protected byte[] binaryDiff(final Task<Diff> task, final Diff diff)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException
	{

		RevisionCodecData codecData = diff.getCodecData();
		byte[] encoding = encoder.binaryDiff(codecData, diff);

		if (VERIFICATION_ENCODING) {
			RevisionDecoder decoder = new RevisionDecoder(WIKIPEDIA_ENCODING);
			decoder.setInput(encoding);
			Diff decDiff = decoder.decode();

			verify(task, decDiff, diff);
		}

		return encoding;
	}

	/* (non-Javadoc)
	 * @see de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface#binaryTask(de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task)
	 */
	@Override
	public SQLEncoding[] binaryTask(final Task<Diff> task)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException
	{

		// this.task = task;
		if (task.getTaskType() == TaskTypes.TASK_FULL
				|| task.getTaskType() == TaskTypes.TASK_PARTIAL_FIRST) {

			this.lastFullRevID = -1;
		}

		int articleId = task.getHeader().getArticleId();
		Diff diff;

		ArrayList<SQLEncoding> list = new ArrayList<SQLEncoding>();

		SQLEncoding revisionsEncoding = new SQLEncoding();
		SQLEncoding usersEncoding = new SQLEncoding();
		revisionsEncoding.append("INSERT INTO revisions VALUES");
		usersEncoding.append("INSERT INTO users VALUES");

		byte[] tempBinaryData;
		String tempData = new String();

		int size = task.size();
		for (int i = 0; i < size; i++) {
			diff = task.get(i);

			/*
			 * Process revision table
			 */
			if (diff.isFullRevision()) {
				this.lastFullRevID = diff.getRevisionID();
			}

			//prepare values that might be null
			//because we don't want quotes if they are null
			String comm = diff.getComment();
			String comment = comm==null?null:"'"+comm+"'";

			Integer cId = diff.getContributorId();
			String contributorId = cId==null?null:cId.toString();

			// save the query and binary data temporary
			tempData = "(null, " + this.lastFullRevID + ","
					+ diff.getRevisionCounter() + "," + diff.getRevisionID()
					+ "," + articleId + "," + diff.getTimeStamp().getTime()
					+ ",?,"+comment+","+(diff.isMinor()?"1":"0")+","+contributorId+ ","+(diff.getContributorIsRegistered()?"1":"0")+")";
			tempBinaryData = binaryDiff(task, diff);

			// if the limit would be reached start a new encoding
			if ((revisionsEncoding.byteSize() + tempBinaryData.length + tempData.length() >= LIMIT_SQL_STATEMENT_SIZE) && (i!=0)) {
				revisionsEncoding.append(";");
				list.add(revisionsEncoding);

				revisionsEncoding = new SQLEncoding();
				revisionsEncoding.append("INSERT INTO revisions VALUES");
			}

			if (revisionsEncoding.size() > 0) {
				revisionsEncoding.append(",");
			}
			revisionsEncoding.append(tempData);
			revisionsEncoding.addBinaryData(tempBinaryData);

		}

		// Add the pending encoding
		if (revisionsEncoding.size() > 0) {
			revisionsEncoding.append(";");
			list.add(revisionsEncoding);
		}


		// Transform the list into an array
		SQLEncoding[] queries = new SQLEncoding[list.size()];
		return list.toArray(queries);
	}

	/**
	 * Encodes the diff.
	 *
	 * @param task
	 *            Reference to the DiffTask
	 * @param diff
	 *            Diff to encode
	 * @return Base 64 encoded Diff
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 * @throws EncodingException
	 *             if the encoding failed
	 * @throws SQLConsumerException
	 *             if an error occurred while encoding the diff
	 */
	protected String encodeDiff(final Task<Diff> task, final Diff diff)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException
	{

		RevisionCodecData codecData = diff.getCodecData();
		String encoding = encoder.encodeDiff(codecData, diff);

		if (VERIFICATION_ENCODING) {
			RevisionDecoder decoder = new RevisionDecoder(WIKIPEDIA_ENCODING);
			decoder.setInput(encoding);
			Diff decDiff = decoder.decode();

			verify(task, decDiff, diff);
		}

		return encoding;
	}

	/* (non-Javadoc)
	 * @see de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface#encodeTask(de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task)
	 */
	@Override
	public SQLEncoding[] encodeTask(final Task<Diff> task)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException
	{

		// this.task = task;
		if (task.getTaskType() == TaskTypes.TASK_FULL
				|| task.getTaskType() == TaskTypes.TASK_PARTIAL_FIRST) {

			this.lastFullRevID = -1;
		}

		int articleId = task.getHeader().getArticleId();
		Diff diff;

		ArrayList<SQLEncoding> list = new ArrayList<SQLEncoding>();

		SQLEncoding revisionEncoding = new SQLEncoding();
		revisionEncoding.append("INSERT INTO revisions VALUES");

		String tempData = new String();

		int size = task.size();
		for (int i = 0; i < size; i++) {

			diff = task.get(i);

			/*
			 * Process revision table
			 */
			if (diff.isFullRevision()) {
				this.lastFullRevID = diff.getRevisionID();
			}

			//prepare values that might be null
			//because we don't want quotes if they are null
			String comm = diff.getComment();
			String comment = comm==null?null:"'"+comm+"'";

			Integer cId = diff.getContributorId();
			String contributorId = cId==null?null:cId.toString();

			// save the query temporary
			tempData = "(null," + this.lastFullRevID + ","
					+ diff.getRevisionCounter() + "," + diff.getRevisionID()
					+ "," + articleId + "," + diff.getTimeStamp().getTime()
					+ ",'" + encodeDiff(task, diff) + "',"+comment+","+(diff.isMinor()?"1":"0")+",'"+diff.getContributorName() +"',"+contributorId+ ","+(diff.getContributorIsRegistered()?"1":"0")+")";

			// if the limit would be reached start a new encoding
			if ((revisionEncoding.byteSize() + tempData.length() >= LIMIT_SQL_STATEMENT_SIZE) && (i!=0)) {
				revisionEncoding.append(";");
				list.add(revisionEncoding);

				revisionEncoding = new SQLEncoding();
				revisionEncoding.append("INSERT INTO revisions VALUES");
			}

			if (revisionEncoding.byteSize() > 30) {
				revisionEncoding.append(",");
			}
			revisionEncoding.append(tempData);

		}

		// Add the pending encodings
		if (revisionEncoding.byteSize() > 30) {
			revisionEncoding.append(";");
			list.add(revisionEncoding);
		}

		// Transform the list into an array
		SQLEncoding[] queries = new SQLEncoding[list.size()];
		return list.toArray(queries);
	}

	/* (non-Javadoc)
	 * @see de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface#getBinaryTable()
	 */
	@Override
	public String[] getBinaryTable()
	{
		return new String[] { binaryTableRevision };
	}

	/* (non-Javadoc)
	 * @see de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface#getTable()
	 */
	@Override
	public String[] getTable()
	{
		return new String[] { tableRevision };
	}



	/**
	 * Verifies that the decoded diff is identical to the original diff.
	 *
	 * @param task
	 *            DiffTask
	 * @param decodedDiff
	 *            diff created from encoding the decoded diff information
	 * @param originalDiff
	 *            original diff
	 *
	 * @throws SQLConsumerException
	 *             if an error occurs
	 */
	private void verify(final Task<Diff> task, final Diff decodedDiff,
			final Diff originalDiff)
		throws SQLConsumerException
	{

		String orig = originalDiff.toString();
		String deco = decodedDiff.toString();

		boolean notEqual = !orig.equals(deco);

		if (notEqual && MODE_SURROGATES == SurrogateModes.REPLACE) {

			char[] origDiff = orig.toCharArray();

			// TODO: test
			if (Surrogates.scan(origDiff)) {

				String repDiff = new String(Surrogates.replace(origDiff));
				notEqual = !repDiff.equals(deco);
			}
		}

		if (notEqual) {

			if (MODE_DEBUG_OUTPUT_ACTIVATED) {

				try {
					// System.out.println("DEBUG\t" + task.toString());

					WikipediaXMLWriter writer = new WikipediaXMLWriter(
							LOGGING_PATH_DIFFTOOL + LOGGING_PATH_DEBUG
									+ task.getHeader().getArticleName()
									+ ".dbg");

					switch (task.getTaskType()) {
					case TASK_FULL:
					case TASK_PARTIAL_FIRST:
						writer.writeDiff(task);
						break;

					case TASK_PARTIAL:
					case TASK_PARTIAL_LAST: {

						int revCount = originalDiff.getRevisionCounter();
						Diff d;
						boolean fullRev = false;

						for (int diffCount = 0; !fullRev
								&& diffCount < originalDiff.size(); diffCount++) {

							d = task.get(diffCount);
							if (d.getRevisionCounter() <= revCount
									&& d.isFullRevision()) {
								fullRev = true;
								writer.writeDiff(task, diffCount);
							}
						}

						if (!fullRev) {
							writer.writeDiffFile(task);
						}

					}
						break;
					default:
						throw new IOException("Unknown TaskType");
						// TODO: Debug output
					}

					writer.close();
				}
				catch (IOException e) {
					ConsumerLogMessages.logException(logger, e);
				}
			}

			throw ErrorFactory
					.createSQLConsumerException(
							ErrorKeys.DIFFTOOL_SQLCONSUMER_ENCODING_VERIFICATION_FAILED,
							"Redecoding of "
									+ task.getHeader().getArticleName()
									+ " failed at revision "
									+ originalDiff.getRevisionCounter() + ".");
		}
	}

}
