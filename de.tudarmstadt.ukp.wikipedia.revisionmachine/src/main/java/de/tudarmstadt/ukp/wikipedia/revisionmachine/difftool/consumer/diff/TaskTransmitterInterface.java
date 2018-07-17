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
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * The TaskTransmitterInterface handles the transmission of DiffTasks to the
 * DiffProducer.
 *
 *
 *
 */
public interface TaskTransmitterInterface
{

	/**
	 * Sends the given task to the DiffProducer - FullTaskPool.
	 *
	 * @param result
	 *            DiffTask of type TaskTypes.FULL_TASK or
	 *            TaskTypes.PARTIAL_TASK_FIRST
	 *
	 * @throws TimeoutException
	 *             if the TaskTransmitter times out during the transmission of
	 *             the task to the DiffProducer.
	 */
	void transmitDiff(final Task<Diff> result)
		throws TimeoutException;

	/**
	 * Sends the given task to the DiffProducer - PartialTaskPool.
	 *
	 * @param result
	 *            DiffTask of type TaskTypes.PARTIAL_TASK or
	 *            TaskTypes.PARTIAL_TASK_LAST
	 *
	 * @throws TimeoutException
	 *             if the TaskTransmitter times out during the transmission of
	 *             the task to the DiffProducer.
	 */
	void transmitPartialDiff(final Task<Diff> result)
		throws TimeoutException;
	
	
	/**
	 * Close stream
	 * @throws IOException
	 * @throws SQLException
	 */
	public void close() throws IOException, SQLException;

}
