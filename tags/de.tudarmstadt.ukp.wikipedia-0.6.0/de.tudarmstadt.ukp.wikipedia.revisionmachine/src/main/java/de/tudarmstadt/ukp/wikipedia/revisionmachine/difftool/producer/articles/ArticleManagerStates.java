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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.articles;

/**
 * This class contains the possible states of the ArticleManager.
 * 
 * @author Simon Kulessa
 * @version 0.5.0
 */
public enum ArticleManagerStates
{

	/** No task available */
	NO_TASK,

	/** ArticleManager is shutting down */
	FINISHED,

	/** Task available */
	TASK_AVAILABLE
}
