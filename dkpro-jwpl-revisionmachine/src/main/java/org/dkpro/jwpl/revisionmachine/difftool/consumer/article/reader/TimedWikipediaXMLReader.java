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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader;

import java.io.IOException;
import java.io.Reader;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ArticleReaderException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * This version of the WikipediaXMLReader collects statistical information
 * when it is running. Besides that, it does the same as WikipediaXMLReader.
 *
 */
public class TimedWikipediaXMLReader
	extends WikipediaXMLReader
{

	/** Temporary variable - start position of the article */
	private long taskStartPosition;

	/** Temporary variable - time the parsing of the article started */
	private long startTime;

	/** Temporary variable - time needed to parse the article */
	private long processingTimeRead;

	/** Temporary variable - number of parsed revisions */
	private int readRevisionCounter;

	/** Temporary variable - The time the task entered the system */
	private long enteringTime;

	/**
	 * Temporary variable - Flag which indicates that the last task was
	 * completed
	 */
	private boolean lastTaskCompleted;

	/**
	 * (Constructor) Creates a new WikipediaXMLReader.
	 *
	 * @param input
	 *            Reference to the reader
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public TimedWikipediaXMLReader(final Reader input)
		throws ConfigurationException
	{

		super(input);
		this.lastTaskCompleted = true;
	}

	/**
	 * (Constructor) Creates a new TimedWikipediaXMLReader.
	 *
	 * @param input
	 *            Reference to the reader
	 * @param articleNameChecker
	 *            Reference to a name checker
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public TimedWikipediaXMLReader(final Reader input,
			final ArticleFilter articleNameChecker)
		throws ConfigurationException
	{

		super(input, articleNameChecker);
	}

	/**
	 * Reads the header of an article.
	 *
	 * @return FALSE if the article was not accepted by the articleNameChecker
	 *         TRUE if no name checker was used, or if the articleNameChecker
	 *         accepted the ArticleName
	 *
	 * @throws IOException
	 *             if an error occurs while reading from the input
	 * @throws ArticleReaderException
	 *             if an error occurs while parsing the input
	 */
	@Override
	protected boolean readHeader()
		throws IOException, ArticleReaderException
	{
		this.enteringTime = startTime;
		return super.readHeader();
	}

	/**
	 * Reads a single revision from an article.
	 *
	 * @return Revision
	 *
	 * @throws IOException
	 *             if an error occurs while reading from the input
	 * @throws ArticleReaderException
	 *             if an error occurs while parsing the input
	 */
	@Override
	protected Revision readRevision()
		throws IOException, ArticleReaderException
	{

		Revision rev = super.readRevision();
		this.readRevisionCounter++;
		return rev;
	}

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
	@Override
	public boolean hasNext()
		throws ArticleReaderException
	{

		if (super.hasNext()) {

			if (lastTaskCompleted) {
				this.taskStartPosition = this.getBytePosition();
				this.processingTimeRead = 0;
				this.readRevisionCounter = 0;
				this.lastTaskCompleted = false;
			}

			return true;
		}
		return false;
	}

	/**
	 * Returns the next RevisionTask.
	 *
	 * @return RevisionTask.
	 *
	 * @throws ArticleReaderException
	 *             if the parsing of the input fails
	 */
	@Override
	public Task<Revision> next()
		throws ArticleReaderException
	{
		this.startTime = System.currentTimeMillis();

		Task<Revision> task = super.next();

		processingTimeRead += System.currentTimeMillis() - startTime;

		if (task != null) {
			if (task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST
					|| task.getTaskType() == TaskTypes.TASK_FULL) {

				lastTaskCompleted = true;

				ArticleInformation info = task.getHeader();
				info.setEnteringTime(enteringTime);
				info.setOriginalSize(this.getBytePosition() - taskStartPosition);
				info.setProcessingTimeRead(processingTimeRead);
				info.setReadRevisionCounter(readRevisionCounter);

			}
			else {
				lastTaskCompleted = false;
			}
		}
		else {
			lastTaskCompleted = true;
		}

		return task;
	}

	/**
	 * Resets the task processing status of the ArticleReader.
	 *
	 * This method has to be called if the hasNext() or next() methods throw an
	 * exception.
	 */
	@Override
	public void resetTaskCompleted()
	{
		lastTaskCompleted = true;
		super.resetTaskCompleted();
	}
}
