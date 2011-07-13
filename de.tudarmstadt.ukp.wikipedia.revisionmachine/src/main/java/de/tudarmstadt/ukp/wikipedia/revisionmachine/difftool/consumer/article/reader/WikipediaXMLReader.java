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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader;

import java.io.IOException;
import java.io.Reader;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ArticleReaderException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.SingleKeywordTree;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.WikipediaXMLKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.ArticleReaderInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.SQLEscape;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * This class parses the wikipedia xml format.
 *
 *
 *
 */
public class WikipediaXMLReader
	implements ArticleReaderInterface
{

	/** Reference to the reader */
	private Reader input;

	/** Current position in the xml content */
	private long bytePosition;

	/** Reference to the xml keyword tree */
	private SingleKeywordTree<WikipediaXMLKeys> keywords;

	/** Configuration parameter - Maximum size of a revision task */
	private final long LIMIT_TASK_SIZE_REVISIONS;

	/** Reference to the name checker */
	private AbstractNameChecker articleNameChecker;

	/**
	 * (Constructor) Creates a new WikipediaXMLReader.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	private WikipediaXMLReader()
		throws ConfigurationException
	{

		this.bytePosition = 0;

		this.taskHeader = null;
		this.lastTaskCompleted = true;

		ConfigurationManager config = ConfigurationManager.getInstance();

		LIMIT_TASK_SIZE_REVISIONS = (Long) config
				.getConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_REVISIONS);

		initialize();

	}

	/**
	 * (Constructor) Creates a new WikipediaXMLReader.
	 *
	 * @param input
	 *            Reference to the reader
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public WikipediaXMLReader(final Reader input)
		throws ConfigurationException
	{

		this();
		this.articleNameChecker = null;
		this.input = input;
	}

	/**
	 * (Constructor) Creates a new WikipediaXMLReader.
	 *
	 * @param input
	 *            Reference to the reader
	 * @param articleNameChecker
	 *            Reference to a name checker
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public WikipediaXMLReader(final Reader input,
			final AbstractNameChecker articleNameChecker)
		throws ConfigurationException
	{

		this();
		this.articleNameChecker = articleNameChecker;
		this.input = input;
	}

	/**
	 * Creates and initializes the xml keyword tree.
	 */
	private void initialize()
	{
		this.keywords = new SingleKeywordTree<WikipediaXMLKeys>();

		keywords.addKeyword(WikipediaXMLKeys.KEY_START_PAGE.getKeyword(),
				WikipediaXMLKeys.KEY_START_PAGE);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_PAGE.getKeyword(),
				WikipediaXMLKeys.KEY_END_PAGE);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_TITLE.getKeyword(),
				WikipediaXMLKeys.KEY_START_TITLE);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_TITLE.getKeyword(),
				WikipediaXMLKeys.KEY_END_TITLE);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_ID.getKeyword(),
				WikipediaXMLKeys.KEY_START_ID);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_ID.getKeyword(),
				WikipediaXMLKeys.KEY_END_ID);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_REVISION.getKeyword(),
				WikipediaXMLKeys.KEY_START_REVISION);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_REVISION.getKeyword(),
				WikipediaXMLKeys.KEY_END_REVISION);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_TIMESTAMP.getKeyword(),
				WikipediaXMLKeys.KEY_START_TIMESTAMP);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_TIMESTAMP.getKeyword(),
				WikipediaXMLKeys.KEY_END_TIMESTAMP);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_TEXT.getKeyword(),
				WikipediaXMLKeys.KEY_START_TEXT);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_TEXT.getKeyword(),
				WikipediaXMLKeys.KEY_END_TEXT);
		keywords.addKeyword(WikipediaXMLKeys.KEY_MINOR_FLAG.getKeyword(),
				WikipediaXMLKeys.KEY_MINOR_FLAG);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_COMMENT.getKeyword(),
				WikipediaXMLKeys.KEY_START_COMMENT);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_COMMENT.getKeyword(),
				WikipediaXMLKeys.KEY_END_COMMENT);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_IP.getKeyword(),
				WikipediaXMLKeys.KEY_START_IP);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_IP.getKeyword(),
				WikipediaXMLKeys.KEY_END_IP);
		keywords.addKeyword(WikipediaXMLKeys.KEY_START_USERNAME.getKeyword(),
				WikipediaXMLKeys.KEY_START_USERNAME);
		keywords.addKeyword(WikipediaXMLKeys.KEY_END_USERNAME.getKeyword(),
				WikipediaXMLKeys.KEY_END_USERNAME);

		//TODO automatically read <namespaces> * </namespaces>
		//TODO load namespace-mappings into article name checker
	}

	/**
	 * Reads a single byte
	 *
	 * @return integer value of the byte or -1 if the end of the stream was
	 *         reached
	 *
	 * @throws IOException
	 *             if an error occurs while reading the input
	 */
	private int read()
		throws IOException
	{
		this.bytePosition++;
		return input.read();
	}

	/** Temporary variable - reference to the article information */
	private ArticleInformation taskHeader;

	/**
	 * Temporary variable - Flag which indicates that the last task was
	 * completed
	 */
	private boolean lastTaskCompleted;

	/**
	 * Temporary variable - Task part counter
	 */
	private int taskPartCounter;

	/**
	 * Temporary variable - Task revision counter
	 */
	private int taskRevisionCounter;

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
	public boolean hasNext()
		throws ArticleReaderException
	{

		try {
			if (!this.lastTaskCompleted) {
				return true;
			}

			this.keywords.reset();

			int b = read();
			while (b != -1) {

				if (keywords.check((char) b)) {
					switch (keywords.getValue()) {
					case KEY_START_PAGE:
						// taskStartPosition = bytePosition;
						return true;
					}
					keywords.reset();
				}

				b = read();
			}

			return false;

		}
		catch (Exception e) {
			throw new ArticleReaderException(e);
		}
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
	protected boolean readHeader()
		throws IOException, ArticleReaderException
	{

		this.taskHeader = new ArticleInformation();

		int size, r = read();
		StringBuilder buffer = null;

		while (r != -1) {

			if (buffer != null) {
				buffer.append((char) r);
			}

			if (this.keywords.check((char) r)) {
				switch (this.keywords.getValue()) {

				case KEY_START_TITLE:
				case KEY_START_ID:
					buffer = new StringBuilder();
					break;

				case KEY_END_TITLE:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_TITLE.getKeyword()
									.length(), size);

					this.taskHeader.setArticleName(buffer.toString());
					if (this.articleNameChecker != null) {
						if (!this.articleNameChecker
								.checkArticle(this.taskHeader.getArticleName())) {
							return false;
						}
					}

					buffer = null;
					break;

				case KEY_END_ID:
					size = buffer.length();
					buffer.delete(
							size
									- WikipediaXMLKeys.KEY_END_ID.getKeyword()
											.length(), size);

					this.taskHeader.setArticleId(Integer.parseInt(buffer
							.toString()));
					buffer = null;
					break;

				case KEY_START_REVISION:
					this.keywords.reset();
					return true;

				default:
					throw ErrorFactory
							.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_KEYWORD);
				}

				this.keywords.reset();
			}

			r = read();
		}

		throw ErrorFactory
				.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_END_OF_FILE);
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
	protected Revision readRevision()
		throws IOException, ArticleReaderException
	{

		this.taskRevisionCounter++;
		Revision revision = new Revision(this.taskRevisionCounter);

		int size, r = read();
		boolean hasId = false;

		StringBuilder buffer = null;
		this.keywords.reset();

		while (r != -1) {

			if (buffer != null) {
				buffer.append((char) r);
			}

			if (this.keywords.check((char) r)) {
				switch (this.keywords.getValue()) {

				case KEY_START_TEXT:

				case KEY_START_TIMESTAMP:

				case KEY_START_COMMENT:

				case KEY_START_IP:

				case KEY_START_USERNAME:
					buffer = new StringBuilder();
					break;

				case KEY_START_ID:
					if (!hasId) {
						buffer = new StringBuilder();
					}
					break;

				case KEY_END_ID:
					if (!hasId) {
						size = buffer.length();
						buffer.delete(size
								- WikipediaXMLKeys.KEY_END_ID.getKeyword()
										.length(), size);

						revision.setRevisionID(Integer.parseInt(buffer
								.toString()));
						buffer = null;

						hasId = true;
					}
					break;

				case KEY_END_TIMESTAMP:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_TIMESTAMP.getKeyword()
									.length(), size);

					revision.setTimeStamp(buffer.toString());
					buffer = null;
					break;

				case KEY_END_TEXT:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_TEXT.getKeyword()
									.length(), size);

					revision.setRevisionText(buffer.toString());
					buffer = null;
					break;

				case KEY_END_COMMENT:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_COMMENT.getKeyword()
									.length(), size);
					//escape comment string
					revision.setComment(SQLEscape.escape(buffer.toString()));
					buffer = null;
					break;

				case KEY_END_IP:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_IP.getKeyword()
									.length(), size);
					//escape id string
					revision.setContributorID(SQLEscape.escape(buffer.toString()));
					revision.setContributorIsRegistered(false);
					buffer = null;
					break;

				case KEY_END_USERNAME:
					size = buffer.length();
					buffer.delete(size
							- WikipediaXMLKeys.KEY_END_USERNAME.getKeyword()
									.length(), size);
					//escape id string
					revision.setContributorID(SQLEscape.escape(buffer.toString()));
					revision.setContributorIsRegistered(true);
					buffer = null;
					break;

				case KEY_MINOR_FLAG:
					revision.setMinor(true);
					buffer = null;
					break;

				case KEY_END_REVISION:
					this.keywords.reset();
					return revision;

				default:
					throw ErrorFactory
							.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_KEYWORD);
				}

				this.keywords.reset();
			}

			r = read();
		}

		throw ErrorFactory
				.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_END_OF_FILE);
	}

	/**
	 * Returns the next RevisionTask.
	 *
	 * @return RevisionTask.
	 *
	 * @throws ArticleReaderException
	 *             if the parsing of the input fails
	 */
	public Task<Revision> next()
		throws ArticleReaderException
	{

		try {
			this.keywords.reset();

			// if new article read header, otherwise use old one
			if (this.lastTaskCompleted) {
				this.lastTaskCompleted = false;

				this.taskPartCounter = 1;
				this.taskRevisionCounter = -1;

				if (!readHeader()) {

					this.lastTaskCompleted = true;
					return null;

				}
			}
			else {
				this.taskPartCounter++;
			}

			Task<Revision> task = new Task<Revision>(this.taskHeader,
					this.taskPartCounter);
			task.add(readRevision());

			int r = read();
			while (r != -1) {
				if (this.keywords.check((char) r)) {

					switch (this.keywords.getValue()) {

					case KEY_START_REVISION:

						if (task.byteSize() >= LIMIT_TASK_SIZE_REVISIONS) {
							this.lastTaskCompleted = false;

							if (this.taskPartCounter == 1) {
								task.setTaskType(TaskTypes.TASK_PARTIAL_FIRST);
							}
							else {
								task.setTaskType(TaskTypes.TASK_PARTIAL);
							}

							return task;
						}

						task.add(readRevision());
						break;

					case KEY_END_PAGE:

						this.lastTaskCompleted = true;
						if (this.taskPartCounter > 1) {
							task.setTaskType(TaskTypes.TASK_PARTIAL_LAST);
						}

						return task;

					default:
						throw new IOException();
					}

					this.keywords.reset();
				}

				r = read();
			}

			throw ErrorFactory
					.createArticleReaderException(ErrorKeys.DELTA_CONSUMERS_TASK_READER_WIKIPEDIAXMLREADER_UNEXPECTED_END_OF_FILE);

		}
		catch (ArticleReaderException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ArticleReaderException(e);
		}
	}

	/**
	 * Resets the task processing status of the ArticleReader.
	 *
	 * This method has to be called if the hasNext() or next() methods throw an
	 * exception.
	 */
	public void resetTaskCompleted()
	{
		this.lastTaskCompleted = true;
	}

	/**
	 * Returns the number of bytes that the ArticleReader has processed.
	 *
	 * @return number of bytes (current position in the file / archive)
	 */
	public long getBytePosition()
	{
		return this.bytePosition;
	}
}
