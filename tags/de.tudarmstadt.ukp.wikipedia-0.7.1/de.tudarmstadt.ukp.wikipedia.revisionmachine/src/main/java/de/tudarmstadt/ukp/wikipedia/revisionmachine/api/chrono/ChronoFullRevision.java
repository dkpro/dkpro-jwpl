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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api.chrono;

import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * ChronoFullRevision
 * 
 * 
 * 1
 */
public class ChronoFullRevision
{

	/** PrimaryKey of the full revision */
	private int fullRevisionPK;

	/** First revision counter / revision counter of the full revision */
	private int startRC;

	/** Last revision counter based on the full revision */
	private int endRC;

	/** Reference to the chrono storage block */
	private ChronoStorageBlock first;

	/** Set containing the IDs of revisions that could be reconstructed */
	private Set<Integer> set;

	/** Link to the next full revision */
	private ChronoFullRevision next;

	/** Link to the previous full revision */
	private ChronoFullRevision prev;

	/** Number of bytes contained in this object */
	private long size;

	/**
	 * (Constructor) Creates a new ChronoFullRevision object.
	 * 
	 * @param fullRevisionPK
	 *            primary key of a full revision
	 * @param startRC
	 *            revision counter of the full revision
	 * @param endRC
	 *            last revision counter based on the full revision
	 */
	public ChronoFullRevision(final int fullRevisionPK, final int startRC,
			final int endRC)
	{

		this.fullRevisionPK = fullRevisionPK;
		this.startRC = startRC;
		this.endRC = endRC;

		this.size = 0;

		this.set = new HashSet<Integer>();
		for (int i = startRC; i <= endRC; i++) {
			this.set.add(i);
		}
	}

	/**
	 * Returns the reference to the ChronoStorageBlock.
	 * 
	 * @return chrono storage block
	 */
	public ChronoStorageBlock getFirst()
	{
		return this.first;
	}

	/**
	 * Sets the reference of the ChronoStorageBlock.
	 * 
	 * @param block
	 *            chrono storage block
	 */
	public void setFirst(final ChronoStorageBlock block)
	{
		this.first = block;
	}

	/**
	 * Adds a ChonoStorageBlock to this chrono full revision object.
	 * 
	 * @param block
	 *            reference to the chrono storage block
	 */
	public void add(final ChronoStorageBlock block)
	{

		int revCount = block.getRevisionCounter();
		this.size += block.length();

		if (first == null) {
			first = block;
		}
		else {

			ChronoStorageBlock previous = null, current = first;
			do {
				if (revCount < current.getRevisionCounter()) {

					block.setCounterPrev(previous);
					block.setCounterNext(current);

					if (previous != null) {
						previous.setCounterNext(block);
					}

					current.setCounterPrev(block);

					if (current == this.first) {
						this.first = block;
					}

					return;
				}

				previous = current;
				current = current.getCounterNext();

			}
			while (current != null);

			// Add to end of list
			previous.setCounterNext(block);
			block.setCounterPrev(previous);
		}
	}

	/**
	 * Returns the nearest available revision to the specified revision counter.
	 * 
	 * @param revisionCounter
	 *            revision counter
	 * @return Revision
	 */
	public Revision getNearest(final int revisionCounter)
	{

		if (first != null) {

			ChronoStorageBlock previous = null, current = first;
			while (current != null
					&& current.getRevisionCounter() <= revisionCounter) {
				previous = current;
				current = current.getCounterNext();
			}

			return previous.getRev();
		}

		return null;
	}

	/**
	 * Removes the revision counter from the list of reconstructible revisions.
	 * 
	 * @param revisionCounter
	 *            revision counter
	 */
	public void remove(final int revisionCounter)
	{
		this.set.remove(revisionCounter);
		if (this.set.isEmpty()) {
			clean(0, 0);
		}
	}

	/**
	 * Removes all revision counter information starting with the specified
	 * revision.
	 * 
	 * @param invalidRevisionCounter
	 *            invalid revision counter
	 * 
	 *            public void unavailable(final int invalidRevisionCounter) {
	 * 
	 *            for (int i = invalidRevisionCounter; i <= this.endRC; i++) {
	 *            this.set.remove(i); }
	 * 
	 *            if (this.set.isEmpty()) { clean(0, 0); } }
	 */

	/**
	 * Returns whether more revisions can be reconstructed by the use of this
	 * chrono full revision.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean isEmpty()
	{
		return this.set.isEmpty();
	}

	/**
	 * Returns the next chrono full revision.
	 * 
	 * @return next chrono full revision
	 */
	public ChronoFullRevision getNext()
	{
		return next;
	}

	/**
	 * Sets the link to the next chrono full revision.
	 * 
	 * @param next
	 *            next chrono full revision
	 */
	public void setNext(final ChronoFullRevision next)
	{
		this.next = next;
	}

	/**
	 * Returns the previous chrono full revision.
	 * 
	 * @return previous chrono full revision
	 */
	public ChronoFullRevision getPrev()
	{
		return prev;
	}

	/**
	 * Sets the link to the previous chrono full revision.
	 * 
	 * @param prev
	 *            previous chrono full revision
	 */
	public void setPrev(final ChronoFullRevision prev)
	{
		this.prev = prev;
	}

	/**
	 * Reduces the storage space.
	 * 
	 * @param currentRevisionIndex
	 *            index of the current revision
	 * @param revisionIndex
	 *            index of the revision
	 * @return size of used storage
	 */
	public long clean(final int currentRevisionIndex, final int revisionIndex)
	{

		if (first == null) {
			return 0;
		}
		else if (this.set.isEmpty()) {
			this.first = null;
			this.size = 0;
			return 0;
		}

		ChronoStorageBlock next, prev, current = first;
		boolean remove;

		do {
			remove = false;

			if (current.isDelivered()) {

				next = current.getCounterNext();

				if (next != null) {
					if (current.getRevisionCounter() + 1 == next
							.getRevisionCounter()) {
						remove = true;
					}
				}

			}
			else if (current.getIndexNext() == null
					&& current.getIndexPrev() == null) {

				remove = (current.getRevisionIndex() < currentRevisionIndex)
						|| (current.getRevisionIndex() == revisionIndex);
			}

			if (remove) {
				// System.out.println("Clearn CFR : " +
				// current.getRevisionCounter());

				prev = current.getCounterPrev();
				next = current.getCounterNext();

				current.setCounterNext(null);
				current.setCounterPrev(null);

				if (prev != null) {
					prev.setCounterNext(next);
				}
				if (next != null) {
					next.setCounterPrev(prev);
				}
				if (current == first) {
					this.first = next;
				}

				this.size -= current.length();
				current = next;
			}

			if (current != null) {
				current = current.getCounterNext();
			}

		}
		while (current != null);

		return this.size;
	}

	/**
	 * Returns the size of this chrono full revision.
	 * 
	 * @return size
	 */
	public long size()
	{
		return this.size;
	}

	/**
	 * Returns the last revision counter based on this full revision.
	 * 
	 * @return last revision counter
	 */
	public int getEndRC()
	{
		return endRC;
	}

	/**
	 * Returns the pk of the full revision.
	 * 
	 * @return pk of the full revision
	 */
	public int getFullRevisionPK()
	{
		return fullRevisionPK;
	}

	/**
	 * Returns the revision counter of the full revision.
	 * 
	 * @return first revision counter
	 */
	public int getStartRC()
	{
		return startRC;
	}
}
