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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer;

/**
 * Enumerator describing the states a consumer can have.
 * 
 * 
 * 
 */
public enum ConsumerStates
{

	/** Consumer has been created */
	INIT,

	/**
	 * Consumer is running but has not received the start signal (or has
	 * received a stop signal)
	 */
	WAITING,

	/** Consumer is sleeping */
	SLEEPING,

	/** Consumer is sleeping while processing a partial task */
	SLEEPING_PARTIAL_TASK,

	/** Consumer is sleeping before transmitting the data to the producer */
	SLEEPING_TRANSMITTING,

	/** Consumer is transmitting the data to the producer */
	TRANSMITTING,

	/** Consumer is processing a full task */
	PROCCESSING_FULL_TASK,

	/** Consumer is processing a partial task */
	PROCCESSING_PARTIAL_TASK
}
