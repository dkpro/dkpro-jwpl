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
