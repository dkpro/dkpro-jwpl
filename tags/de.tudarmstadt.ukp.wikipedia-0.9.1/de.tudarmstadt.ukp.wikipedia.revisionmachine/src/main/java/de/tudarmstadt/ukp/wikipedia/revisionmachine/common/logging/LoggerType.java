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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging;

/**
 * This class contains all keys for diff tool loggers.
 * 
 * 
 * 
 */
public enum LoggerType
{

	/** DiffTool Error Logger */
	DIFF_TOOL_ERROR,

	/** DiffTool Logger */
	DIFF_TOOL,

	/** Article Output Logger */
	ARTICLE_OUTPUT,

	/** UNCOMPRESSED Consumer Logger */
	CONSUMER_SQL,

	/** Diff Consumer Logger */
	CONSUMER_DIFF,

	/** Task Consumer Logger */
	CONSUMER_TASK,

	/** Artcile Producer Logger */
	PRODUCER_ARTICLES,

	/** Producer Archives Logger */
	PRODUCER_ARCHIVES,

	/** Diff Producer Logger */
	PRODUCER_DIFFS,

	/** Consumer Producer Logger */
	PRODUCER_CONSUMERS
}
