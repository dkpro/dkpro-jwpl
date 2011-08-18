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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ArticleReaderException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;

/**
 * This interface represents the link to the input.
 * 
 * 
 * 
 */
public interface ArticleReaderInterface
{

	/**
	 * Determines whether another task is available or not.
	 * 
	 * This method has to be called before calling the next() method.
	 * 
	 * @return TRUE | FALSE
	 * 
	 * @throws ArticleReaderException
	 *             if the parsing of the input fails
	 */
	boolean hasNext()
		throws ArticleReaderException;

	/**
	 * Returns the next RevisionTask.
	 * 
	 * @return RevisionTask.
	 * 
	 * @throws ArticleReaderException
	 *             if the parsing of the input fails
	 */
	Task<Revision> next()
		throws ArticleReaderException;

	/**
	 * Resets the task processing status of the ArticleReader.
	 * 
	 * This method has to be called if the hasNext() or next() methods throw an
	 * exception.
	 */
	void resetTaskCompleted();

	/**
	 * Returns the number of bytes that the ArticleReader has processed.
	 * 
	 * @return number of bytes (current position in the file / archive)
	 */
	long getBytePosition();
}
