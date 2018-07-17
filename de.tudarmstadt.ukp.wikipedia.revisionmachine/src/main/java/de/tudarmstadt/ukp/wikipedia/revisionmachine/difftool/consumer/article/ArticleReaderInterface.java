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
