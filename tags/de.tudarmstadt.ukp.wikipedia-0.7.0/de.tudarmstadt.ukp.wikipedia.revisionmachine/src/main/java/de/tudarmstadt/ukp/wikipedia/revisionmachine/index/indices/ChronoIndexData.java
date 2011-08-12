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
 * This class represents the data used by the ChronoIndex.
 * 
 * 
 * 
 * 
 */
public class ChronoIndexData
	implements Comparable<ChronoIndexData>
{

	/**
	 * Flag - whether the data should be sorted choronlogical or in order of the
	 * revision counter
	 */
	private boolean chronoSort;

	/** Index value (Chronological order position) */
	private int index;

	/** Revision counter */
	private int revisionCounter;

	/** Timestamp value */
	private long time;

	/**
	 * (Constructor) Creates a new ChronoInfo object.
	 * 
	 * @param time
	 *            Timestamp value
	 * @param revisionCounter
	 *            RevisionCounter
	 */
	public ChronoIndexData(final long time, final int revisionCounter)
	{
		this.time = time;
		this.revisionCounter = revisionCounter;
		this.chronoSort = true;
	}

	/**
	 * Compares this ChronoInfo to the given info.
	 * 
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final ChronoIndexData info)
	{

		long value;

		if (chronoSort) {
			value = this.time - info.time;
		}
		else {
			value = this.revisionCounter - info.revisionCounter;
		}

		if (value == 0) {
			return 0;
		}
		else if (value > 0) {
			return 1;
		}
		else {
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return (this != (ChronoIndexData) obj) ? false : true;
	}

	/**
	 * Returns the index value.
	 * 
	 * @return index value
	 */
	public int getIndex()
	{
		return this.index;
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
	 * Returns the timestamp value.
	 * 
	 * @return timestamp value
	 */
	public long getTime()
	{
		return time;
	}

	/**
	 * Sets the index value.
	 * 
	 * @param index
	 *            index value
	 */
	public void setIndex(final int index)
	{
		this.index = index;
	}

	/**
	 * Sets the sort flag.
	 * 
	 * @param chronoSort
	 *            TRUE for chronological sorting, FALSE for revision counter
	 *            sorting
	 */
	public void setSortFlag(final boolean chronoSort)
	{
		this.chronoSort = chronoSort;
	}
}
