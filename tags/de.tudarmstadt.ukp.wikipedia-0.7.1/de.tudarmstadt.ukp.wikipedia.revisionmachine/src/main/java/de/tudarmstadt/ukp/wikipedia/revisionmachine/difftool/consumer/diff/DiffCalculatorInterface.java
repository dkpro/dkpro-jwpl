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
