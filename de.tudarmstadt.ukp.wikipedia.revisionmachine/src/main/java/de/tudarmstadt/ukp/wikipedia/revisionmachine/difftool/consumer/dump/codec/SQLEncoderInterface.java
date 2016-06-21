/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
