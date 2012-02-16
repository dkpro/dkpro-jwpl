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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks;

/**
 * This Enumerator lists the different types of tasks.
 * 
 * 
 * 
 */
public enum TaskTypes
{

	/** dummy task */
	DUMMY,

	/** if this task is received from a consumer, it will shutdown afterwards */
	ENDTASK,

	/** if the article id is black listed */
	BANNED_TASK,

	/** full task containing all revisions of one article */
	TASK_FULL,

	/** task containing the first part of revisions of one article */
	TASK_PARTIAL_FIRST,

	/** task containing some revisions of one article */
	TASK_PARTIAL,

	/** task containing the last part of revisions from one article */
	TASK_PARTIAL_LAST
}
