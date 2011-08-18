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
