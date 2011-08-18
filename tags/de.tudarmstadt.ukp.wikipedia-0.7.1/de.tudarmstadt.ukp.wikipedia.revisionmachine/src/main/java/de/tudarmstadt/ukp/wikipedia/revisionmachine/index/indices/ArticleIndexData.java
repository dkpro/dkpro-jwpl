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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices;

/**
 * This class represents the data used by the ArticleIndex. One objects
 * represents one revision block.
 * 
 * 
 * 
 */
public class ArticleIndexData
{

	/** Last number of a block of revisions */
	private long endRevisionCount;

	/** ID of the full revision */
	private long fullRevisionID;

	/** PK of the full revision */
	private long fullRevisionPrimaryKey;

	/** First number of a block of revisions */
	private long startRevisionCount;

	/**
	 * Returns the last revision counter of this block.
	 * 
	 * @return revision counter
	 */
	public long getEndRevisionCount()
	{
		return endRevisionCount;
	}

	/**
	 * Returns the ID of the full revision.
	 * 
	 * @return ID of the full revision
	 */
	public long getFullRevisionID()
	{
		return fullRevisionID;
	}

	/**
	 * Returns the PK of the full revision.
	 * 
	 * @return PK of the full revision
	 */
	public long getFullRevisionPrimaryKey()
	{
		return fullRevisionPrimaryKey;
	}

	/**
	 * Returns the first revision counter of this block.
	 * 
	 * @return revision counter
	 */
	public long getStartRevisionCount()
	{
		return startRevisionCount;
	}

	/**
	 * Sets the last revision counter of this block.
	 * 
	 * @param endRevisionCount
	 *            revision counter
	 */
	public void setEndRevisionCount(final long endRevisionCount)
	{
		this.endRevisionCount = endRevisionCount;
	}

	/**
	 * Sets the ID of the full revision.
	 * 
	 * @param fullRevisionID
	 *            ID of the full revision
	 */
	public void setFullRevisionID(final long fullRevisionID)
	{
		this.fullRevisionID = fullRevisionID;
	}

	/**
	 * Sets the PK of the full revision.
	 * 
	 * @param fullRevisionPrimaryKey
	 *            PK of the full revision
	 */
	public void setFullRevisionPrimaryKey(final long fullRevisionPrimaryKey)
	{
		this.fullRevisionPrimaryKey = fullRevisionPrimaryKey;
	}

	/**
	 * Sets the first revision counter of this block.
	 * 
	 * @param startRevisionCount
	 *            revision counter
	 */
	public void setStartRevisionCount(final long startRevisionCount)
	{
		this.startRevisionCount = startRevisionCount;
	}
}
