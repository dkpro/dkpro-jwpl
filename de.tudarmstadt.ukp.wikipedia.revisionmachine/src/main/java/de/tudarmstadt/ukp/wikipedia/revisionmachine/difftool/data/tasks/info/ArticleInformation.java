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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.MathUtilities;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;

/**
 * This class contains all statistical information related to one article.
 * 
 * 
 * 
 */
public class ArticleInformation
{

	/** Article ID */
	private int articleId;

	/** Name of the article */
	private String articleName;

	/** Diffed size of the article */
	private long diffedSize;

	/** Number of diff parts used */
	private int diffPartCounter;

	/** Encoded size of the article */
	private long encodedSize;

	/** UNCOMPRESSED encoded size of the article */
	private long encodedSQLSize;

	/** Time the task entered the system */
	private long enteringTime;

	/** Time the task exited the system */
	private long exitingTime;

	/** Number of ignored revisions */
	private int ignoredRevisionsCounter;

	/** Original size of the article */
	private long originalSize;

	/** Time used to diff the task */
	private long processingTimeDiff;

	/** Time used to read the task */
	private long processingTimeRead;

	/** Time used to encode the task */
	private long processingTimeSQL;

	/** Number of parsed revisions related to this article */
	private int readRevisionCounter;

	/** Value of the revision counter after finishing the diff processing */
	private int revisionCounter;

	/**
	 * (Constructor) Creates a new ArticleInformation object.
	 */
	public ArticleInformation()
	{
		this.articleId = -1;
		// this.timeStamp = null;
		this.articleName = null;

		this.revisionCounter = 0;
		this.ignoredRevisionsCounter = 0;
		this.diffPartCounter = 0;

		this.originalSize = 0;
		this.diffedSize = 0;
		this.encodedSize = 0;
		this.encodedSQLSize = 0;

		this.enteringTime = 0;
		this.exitingTime = 0;
	}

	/**
	 * Returns the ID of the article.
	 * 
	 * @return Article ID
	 */
	public int getArticleId()
	{
		return articleId;
	}

	/**
	 * Returns the name of the article.
	 * 
	 * @return Article name
	 */
	public String getArticleName()
	{
		return articleName;
	}

	/**
	 * Returns the diffed size of the article.
	 * 
	 * @return diffed size
	 */
	public long getDiffedSize()
	{
		return diffedSize;
	}

	/**
	 * Returns the number of diff parts.
	 * 
	 * @return number of diff parts
	 */
	public int getDiffPartCounter()
	{
		return diffPartCounter;
	}

	/**
	 * Returns the encoded size of the article.
	 * 
	 * @return encoded size
	 */
	public long getEncodedSize()
	{
		return encodedSize;
	}

	/**
	 * Returns the size of the article after the sql encoding.
	 * 
	 * @return size after encoding
	 */
	public long getEncodedSQLSize()
	{
		return encodedSQLSize;
	}

	/**
	 * Returns the entering time.
	 * 
	 * @return entering time
	 */
	public long getEnteringTime()
	{
		return enteringTime;
	}

	/**
	 * Returns the exiting time.
	 * 
	 * @return exiting time
	 */
	public long getExitingTime()
	{
		return exitingTime;
	}

	/**
	 * Returns the number of ignored revisions.
	 * 
	 * @return number of ignored revisions
	 */
	public int getIgnoredRevisionsCounter()
	{
		return ignoredRevisionsCounter;
	}

	/**
	 * Returns the original size of the article.
	 * 
	 * @return original size
	 */
	public long getOriginalSize()
	{
		return originalSize;
	}

	/**
	 * Returns the time used for the diff encoding.
	 * 
	 * @return processing time diff
	 */
	public long getProcessingTimeDiff()
	{
		return processingTimeDiff;
	}

	/**
	 * Returns the time used for reading the task.
	 * 
	 * @return processing time reading
	 */
	public long getProcessingTimeRead()
	{
		return processingTimeRead;
	}

	/**
	 * Returns the time used for the sql encoding.
	 * 
	 * @return processing time encoding
	 */
	public long getProcessingTimeSQL()
	{
		return processingTimeSQL;
	}

	/**
	 * Returns the number of parsed revisions.
	 * 
	 * @return number of parsed revisions
	 */
	public int getReadRevisionCounter()
	{
		return readRevisionCounter;
	}

	/**
	 * Returns the revision counter.
	 * 
	 * @return revision counter
	 */
	public int getRevisionCounter()
	{
		return revisionCounter;
	}

	/**
	 * Sets the ID of the article.
	 * 
	 * @param articleId
	 *            Article ID
	 */
	public void setArticleId(final int articleId)
	{
		this.articleId = articleId;
	}

	/**
	 * Sets the name of the article.
	 * 
	 * @param articleName
	 *            Article name
	 */
	public void setArticleName(final String articleName)
	{
		this.articleName = articleName;
	}

	/**
	 * Sets the diffed size of the article.
	 * 
	 * @param diffedSize
	 *            diffed size
	 */
	public void setDiffedSize(final long diffedSize)
	{
		this.diffedSize = diffedSize;
	}

	/**
	 * Sets the number of diff parts.
	 * 
	 * @param diffPartCounter
	 *            number of diff parts
	 */
	public void setDiffPartCounter(final int diffPartCounter)
	{
		this.diffPartCounter = diffPartCounter;
	}

	/**
	 * Sets the encoded size of the article.
	 * 
	 * @param encodedSize
	 *            encoded size
	 */
	public void setEncodedSize(final long encodedSize)
	{
		this.encodedSize = encodedSize;
	}

	/**
	 * Sets the size of the article after the sql encoding.
	 * 
	 * @param encodedSQLSize
	 *            size after encoding
	 */
	public void setEncodedSQLSize(final long encodedSQLSize)
	{
		this.encodedSQLSize = encodedSQLSize;
	}

	/**
	 * Sets the entering time of the first task for this article.
	 * 
	 * @param enteringTime
	 *            entering time
	 */
	public void setEnteringTime(final long enteringTime)
	{
		this.enteringTime = enteringTime;
	}

	/**
	 * Sets the exiting time of the last task for this article.
	 * 
	 * @param exitingTime
	 *            exiting time
	 */
	public void setExitingTime(final long exitingTime)
	{
		this.exitingTime = exitingTime;
	}

	/**
	 * Sets the number of ignored revisions.
	 * 
	 * @param ignoredRevisionsCounter
	 *            number of ignored revisions
	 */
	public void setIgnoredRevisionsCounter(final int ignoredRevisionsCounter)
	{
		this.ignoredRevisionsCounter = ignoredRevisionsCounter;
	}

	/**
	 * Sets the original size of the article.
	 * 
	 * @param originalSize
	 *            original size
	 */
	public void setOriginalSize(final long originalSize)
	{
		this.originalSize = originalSize;
	}

	/**
	 * Sets the time used for the diff encoding.
	 * 
	 * @param processingTimeDiff
	 *            processing time diff
	 */
	public void setProcessingTimeDiff(final long processingTimeDiff)
	{
		this.processingTimeDiff = processingTimeDiff;
	}

	/**
	 * Sets the time used for reading the task.
	 * 
	 * @param processingTimeRead
	 *            processing time reading
	 */
	public void setProcessingTimeRead(final long processingTimeRead)
	{
		this.processingTimeRead = processingTimeRead;
	}

	/**
	 * Sets the time used for the sql encoding.
	 * 
	 * @param processingTimeSQL
	 *            processing time encoding
	 */
	public void setProcessingTimeSQL(final long processingTimeSQL)
	{
		this.processingTimeSQL = processingTimeSQL;
	}

	/**
	 * Sets the number of parsed revisions.
	 * 
	 * @param readRevisionCounter
	 *            number of parsed revisions
	 */
	public void setReadRevisionCounter(final int readRevisionCounter)
	{
		this.readRevisionCounter = readRevisionCounter;
	}

	/**
	 * Sets the revision counter.
	 * 
	 * @param nrRevisions
	 *            revision counter
	 */
	public void setRevisionCounter(final int nrRevisions)
	{
		this.revisionCounter = nrRevisions;
	}

	/**
	 * Returns the string representation of this object. Used for logging the
	 * statistical data.
	 * 
	 * @return content representation
	 */
	public String toString()
	{

		long sysTime = this.exitingTime - this.enteringTime;

		StringBuilder b = new StringBuilder();
		b.append("\n[\tARTICLEID:       \t");
		b.append(articleId);
		b.append("\r\n\tARTICLENAME:       \t");
		b.append(articleName);
		b.append("\r\n\r\n\tNUMBER REVISIONS:\t[");
		b.append(this.revisionCounter);
		b.append(" + ");
		b.append(this.ignoredRevisionsCounter);
		b.append(" = ");
		b.append(this.readRevisionCounter);
		b.append("]\r\n\tNUMBER DIFFPARTS:\t");
		b.append(this.diffPartCounter);
		b.append("\r\n\r\n\tSYSTEM TIME:     \t[ 100% ]\t");
		b.append(Time.toClock(sysTime));
		b.append("\r\n\tREADING TIME:    \t[");
		b.append(MathUtilities.percentFrom(this.processingTimeRead, sysTime));
		b.append("]\t");
		b.append(Time.toClock(this.processingTimeRead));
		b.append("\r\n\tDIFFING TIME:    \t[");
		b.append(MathUtilities.percentFrom(this.processingTimeDiff, sysTime));
		b.append("]\t");
		b.append(Time.toClock(this.processingTimeDiff));
		b.append("\r\n\tENCODING TIME:   \t[");
		b.append(MathUtilities.percentFrom(this.processingTimeSQL, sysTime));
		b.append("]\t");
		b.append(Time.toClock(this.processingTimeSQL));
		b.append("\r\n\r\n\tORIGINAL SIZE:   \t[ 100% ]\t");
		b.append(this.originalSize);
		b.append("\r\n\tDIFFED SIZE:     \t[");
		b.append(MathUtilities.percentFrom(this.diffedSize, this.originalSize));
		b.append("]\t");
		b.append(this.diffedSize);
		b.append("\r\n\tENCODED SIZE:    \t[");
		b.append(MathUtilities.percentFrom(this.encodedSize, this.originalSize));
		b.append("]\t");
		b.append(this.encodedSize);
		b.append("\r\n\tENCODED UNCOMPRESSED SIZE:    \t[");
		b.append(MathUtilities.percentFrom(this.encodedSQLSize,
				this.originalSize));
		b.append("]\t");
		b.append(this.encodedSQLSize);
		b.append("\r\n]\r\n");

		return b.toString();
	}
}
