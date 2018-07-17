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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api.chrono;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * This class represents an object in the chrono storage space.
 *
 * A node contains multiple links: - Reference to the chrono full revision
 *
 * - links to the previous and next index block an index reference describes the
 * chronological order
 *
 * - links to the previous and next counter block an counter reference describes
 * the normal order
 *
 *
 * 1
 */
public class ChronoStorageBlock
{

	/** Reference to the chrono full revision */
	private ChronoFullRevision cfr;

	/** Index of the revision */
	private int revisionIndex;

	/** Revision */
	private Revision rev;

	/** Flag, indicating whether the revision was already returned or not */
	private boolean delivered;

	/** Reference to the previous index block */
	private ChronoStorageBlock indexPrev;

	/** Reference to the next index block */
	private ChronoStorageBlock indexNext;

	/** Reference to the previous counter block */
	private ChronoStorageBlock counterPrev;

	/** Reference to the next counter block */
	private ChronoStorageBlock counterNext;

	/**
	 * Returns the related chrono full revision.
	 *
	 * @return chrono full revision
	 */
	public ChronoFullRevision getChronoFullRevision()
	{
		return this.cfr;
	}

	/**
	 * Returns the next counter block.
	 *
	 * @return next counter block
	 */
	public ChronoStorageBlock getCounterNext()
	{
		return counterNext;
	}

	/**
	 * Sets the next counter block.
	 *
	 * @param counterNext
	 *            next counter block
	 */
	public void setCounterNext(final ChronoStorageBlock counterNext)
	{
		this.counterNext = counterNext;
	}

	/**
	 * Returns the previous counter block.
	 *
	 * @return previous counter block
	 */
	public ChronoStorageBlock getCounterPrev()
	{
		return counterPrev;
	}

	/**
	 * Sets the previous counter block.
	 *
	 * @param counterPrev
	 *            previous counter block
	 */
	public void setCounterPrev(final ChronoStorageBlock counterPrev)
	{
		this.counterPrev = counterPrev;
	}

	/**
	 * Returns the next index block.
	 *
	 * @return next index block
	 */
	public ChronoStorageBlock getIndexNext()
	{
		return indexNext;
	}

	/**
	 * Sets the next index block.
	 *
	 * @param indexNext
	 *            next index block
	 */
	public void setIndexNext(final ChronoStorageBlock indexNext)
	{
		this.indexNext = indexNext;
	}

	/**
	 * Returns the previous index block.
	 *
	 * @return previous index block
	 */
	public ChronoStorageBlock getIndexPrev()
	{
		return indexPrev;
	}

	/**
	 * Sets the previous index block.
	 *
	 * @param indexPrev
	 *            previous counter block
	 */
	public void setIndexPrev(final ChronoStorageBlock indexPrev)
	{
		this.indexPrev = indexPrev;
	}

	/**
	 * (Constructor) Creates a new ChronoStorageBlock.
	 *
	 * @param cfr
	 *            Reference to the chrono full revision
	 * @param revisionIndex
	 *            Index of this revision
	 * @param rev
	 *            Reference to the revision
	 */
	public ChronoStorageBlock(final ChronoFullRevision cfr,
			final int revisionIndex, final Revision rev)
	{

		this.cfr = cfr;

		this.revisionIndex = revisionIndex;
		this.rev = rev;
		this.delivered = false;
	}

	public Revision getRev()
	{
		return rev;
	}

	/**
	 * Returns whether this revision was already returned or not.
	 *
	 * @return flag
	 */
	public boolean isDelivered()
	{
		return delivered;
	}

	/**
	 * Sets whether this revision was already returned or not.
	 *
	 * @param delivered
	 *            flag
	 */
	public void setDelivered(final boolean delivered)
	{
		this.delivered = delivered;
	}

	/**
	 * Returns the revision index.
	 *
	 * @return revision index
	 */
	public int getRevisionIndex()
	{
		return revisionIndex;
	}

	/**
	 * Returns the revision counter.
	 *
	 * @return revision counter
	 */
	public int getRevisionCounter()
	{
		return this.rev.getRevisionCounter();
	}

	public int length()
	{
		return this.rev.getRevisionText().length();
	}
}
