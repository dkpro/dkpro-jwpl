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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data;

/**
 * This class contains all capacity states.
 * 
 * 
 * 
 */
public enum CapacityValue
{

	/** Capacity is available */
	FREE,

	/** Task limit has been reached */
	TASK_LIMIT_REACHED,

	/** Cache limit has been reached */
	CACHE_LIMIT_REACHED,

	/** OutOfMemory */
	NO_MEMORY_AVAILABLE,

	/** Producer is shutting down */
	PRODUCER_SHUTDOWN
}
