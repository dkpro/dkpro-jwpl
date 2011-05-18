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
 * Enumerator describing the modes a consumer can have.
 * 
 * 
 * 
 */
public enum ConsumerMode
{

	/** Consumer can process all tasks (full tasks + partial tasks) */
	All,

	/** Consumer can process only full tasks */
	FullTasksOnly
}
