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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionCodecData;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionEncoder;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionEncoderInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * Alternative to the SQLEncoder - writes data files instead of UNCOMPRESSED dumps
 *	@author Oliver Ferschke
 *
 */
public class DataFileEncoder
{

	/** Reference to the RevisionApi */
	private final RevisionEncoderInterface encoder;

	/** Last used ID of a full revision */
	private int lastFullRevID = -1;

	/**
	 * (Constructor) Creates a new SQLEncoder object.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while accessing the logger
	 */
	public DataFileEncoder()
		throws ConfigurationException
	{
		this.encoder = new RevisionEncoder();

//		tableRevision = "CREATE TABLE IF NOT EXISTS revisions ("
//				+ "PrimaryKey INT UNSIGNED NOT NULL AUTO_INCREMENT, "
//				+ "FullRevisionID INTEGER UNSIGNED NOT NULL, "
//				+ "RevisionCounter INTEGER UNSIGNED NOT NULL, "
//				+ "RevisionID INTEGER UNSIGNED NOT NULL, "
//				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
//				+ "Timestamp BIGINT NOT NULL, "
//				+ "Revision MEDIUMTEXT NOT NULL, "
//				+ "Comment MEDIUMTEXT, "
//				+ "Minor TINYINT NOT NULL, "
//				+ "ContributorName TEXT NOT NULL, "
//				+ "ContributorId INTEGER UNSIGNED, "
//				+ "ContributorIsRegistered TINYINT NOT NULL, "
//				+ "PRIMARY KEY(PrimaryKey)"
//				+ ") TYPE = MyISAM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;";

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

		return encoding;
	}

	/* (non-Javadoc)
	 * @see de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.dump.codec.SQLEncoderInterface#encodeTask(de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task)
	 */
	public List<String> encodeTask(final Task<Diff> task)
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

		ArrayList<String> list = new ArrayList<String>();

		String tempData = new String();

		int size = task.size();
		for (int i = 0; i < size; i++) {

			diff = task.get(i);

			if (diff.isFullRevision()) {
				this.lastFullRevID = diff.getRevisionID();
			}

			/*
			 * prepare values that might be null
			 * because we don't want quotes if they are null
			 *
			 * Furthermore, escape quote-characters. Quotes are used as the "ENCLOSED BY" character
			 * in MySQL to mark begin and end of Strings
			 */

			//prepare values that might be null
			//because we don't want quotes if they are null
			String comm = diff.getComment();
			String comment = comm==null?"\\N":"\""+escape(comm)+"\"";

			Integer cId = diff.getContributorId();
			String contributorId = cId==null?"\\N":cId.toString();

			String cName = diff.getContributorName();
			String contributorName = cName==null?"\\N":"\""+escape(cName)+"\"";

			//Prepare the actual data item
			tempData = "\\N,"
					+ this.lastFullRevID + ","
					+ diff.getRevisionCounter() + ","
					+ diff.getRevisionID()+ ","
					+ articleId + ","
					+ diff.getTimeStamp().getTime()+ ",\""
					+ encodeDiff(task, diff) + "\","
					+ comment+","
					+ (diff.isMinor()?"1":"0")+","
					+ contributorName+","
					+ contributorId+ ","
					+ (diff.getContributorIsRegistered()?"1":"0");

			//add item to the list
			list.add(tempData);
		}

		return list;
	}

	private String escape(String str){
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
	}

}
