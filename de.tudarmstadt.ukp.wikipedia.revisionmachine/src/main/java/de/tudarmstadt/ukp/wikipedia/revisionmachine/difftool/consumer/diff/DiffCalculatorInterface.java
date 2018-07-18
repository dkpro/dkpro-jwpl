/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DiffException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;

/**
 * The DiffCalculatorInterface represents the interface to the diff processing
 * unit.
 *
 * Please notice that there is no default method to return the generated diff.
 * The currently implementation uses the TaskTransmitterInterface (given as
 * parameter of the constructor) to send the diffed data to the DiffProducer.
 *
 *
 *
 */
public interface DiffCalculatorInterface
{

	/**
	 * This method process the given task to generate the diff.
	 *
	 * @param task
	 *            RevisionTask
	 *
	 * @throws DiffException
	 *             if the diff process fails
	 *
	 * @throws TimeoutException
	 *             if the TaskTransmitter times out during the transmission of
	 *             the task to the DiffProducer.
	 *
	 * @throws UnsupportedEncodingException
	 *             if the CharacterSet defined in the configuration is not
	 *             supported by JAVA.
	 */
	public void process(final Task<Revision> task)
		throws DiffException, TimeoutException, UnsupportedEncodingException;

	/**
	 * This method is used to delete all information concerning the partial task
	 * processing.
	 *
	 * This method has to be called if the process method throws an exception.
	 */
	public void reset();
	
	
	/**
	 * Close Stream of Transmitter
	 * @throws IOException
	 * @throws SQLException
	 */
	public void closeTransmitter() throws IOException, SQLException;

}
