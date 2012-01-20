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

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * The SQLEncoderInterface provides the link to the SQLEncoder who will define
 * the formatting of the output.
 *
 * @author Simon
 *
 */
public interface SQLEncoderInterface
{

	/**
	 * Returns the tables for textual output.
	 *
	 * Each Array entry will contain a single sql command.
	 *
	 * @return sql command to create the tables
	 */
	public String[] getTable();

	/**
	 * Returns the tables for binary output.
	 *
	 * Each Array entry will contain a single sql command.
	 *
	 * @return sql command to create the tables
	 */
	public String[] getBinaryTable();


	/**
	 * Returns the binary encoding of the given DiffTask.
	 *
	 * Each Array entry will contain a single sql command.
	 *
	 * @param task
	 *            DiffTask
	 * @return binary encoding of the task.
	 *
	 * @throws ConfigurationException
	 *             if problems occurred while initializing the components
	 *
	 * @throws UnsupportedEncodingException
	 *             if the CharacterSet defined in the configuration is not
	 *             supported by JAVA.
	 *
	 * @throws DecodingException
	 *             if the decoding process fails (during the verification
	 *             process)
	 *
	 * @throws EncodingException
	 *             if the encoding process fails
	 *
	 * @throws SQLConsumerException
	 *             if the verification process fails
	 *
	 */
	public SQLEncoding[] binaryTask(final Task<Diff> task)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException;

	/**
	 * Returns the textual encoding of the given DiffTask.
	 *
	 * Each Array entry will contain a single sql command.
	 *
	 * @param task
	 *            DiffTask
	 * @return binary encoding of the task.
	 *
	 * @throws ConfigurationException
	 *             if problems occurred while initializing the components
	 *
	 * @throws UnsupportedEncodingException
	 *             if the CharacterSet defined in the configuration is not
	 *             supported by JAVA.
	 *
	 * @throws DecodingException
	 *             if the decoding process fails (during the verification
	 *             process)
	 *
	 * @throws EncodingException
	 *             if the encoding process fails
	 *
	 * @throws SQLConsumerException
	 *             if the verification process fails
	 *
	 */
	public SQLEncoding[] encodeTask(final Task<Diff> task)
		throws ConfigurationException, UnsupportedEncodingException,
		DecodingException, EncodingException, SQLConsumerException;

}
