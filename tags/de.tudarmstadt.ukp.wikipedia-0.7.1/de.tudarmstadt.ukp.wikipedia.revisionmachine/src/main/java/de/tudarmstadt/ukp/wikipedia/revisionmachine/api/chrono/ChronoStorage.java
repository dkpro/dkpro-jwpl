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

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;

/**
 * This class represents the chrono storage.
 * 
 * 
 * 1
 */
public class ChronoStorage
{

	/** Index of the currently used revision */
	private int revisionIndex;

	/** Reference to the first chrono storage block */
	private ChronoStorageBlock first;

	/** Reference to the last chrono storage block */
	private ChronoStorageBlock last;

	/** Map containing the chrono storage block and their index keys */
	private Map<Integer, ChronoStorageBlock> storage;

	/** Reverse mapping */
	private Map<Integer, Integer> mapping;

	/**
	 * Map containing reference to the chrono full revisions (Mapping of
	 * revision counter and their full revision blocks)
	 */
	private Map<Integer, ChronoFullRevision> fullRevStorage;

	/** Reference to the first chrono full revision */
	private ChronoFullRevision firstCFR;

	/** Size of the chrono storage */
	private long size;

	/** Configuration parameter - maximum size of this storage */
	private final long MAX_STORAGE_SIZE;

	/**
	 * (Constructor) Creates a ChronoStorage object
	 * 
	 * @param config
	 *            Reference to the configuration
	 * @param mapping
	 *            Mapping information (revision counter -> chronological
	 *            revision counter)
	 * @param firstCFR
	 *            Head of the double linked list of full revisions blocks
	 * @param fullRevStorage
	 *            Mapping of revision counter and their full revision blocks
	 */
	public ChronoStorage(final RevisionAPIConfiguration config,
			final Map<Integer, Integer> mapping,
			final ChronoFullRevision firstCFR,
			final Map<Integer, ChronoFullRevision> fullRevStorage)
	{

		this.revisionIndex = 0;
		this.last = null;
		this.first = null;
		this.storage = new HashMap<Integer, ChronoStorageBlock>();

		this.mapping = mapping;
		this.fullRevStorage = fullRevStorage;
		this.firstCFR = firstCFR;

		MAX_STORAGE_SIZE = config.getChronoStorageSpace();
	}

	/**
	 * Adds a revision to the chrono storage.
	 * 
	 * @param rev
	 *            reference to the revision
	 */
	public void add(final Revision rev)
	{

		int revIndex = rev.getRevisionCounter();
		if (this.mapping.containsKey(revIndex)) {
			revIndex = this.mapping.get(revIndex);
		}

		// System.out.println("Store " + rev.getRevisionCounter() + " with " +
		// revIndex);

		ChronoFullRevision cfr = this.fullRevStorage.get(rev
				.getRevisionCounter());
		ChronoStorageBlock block = new ChronoStorageBlock(cfr, revIndex, rev);
		cfr.add(block);

		if (revIndex < revisionIndex) {
			// System.out.println("Revision has already been processed: " +
			// revIndex);
			block.setDelivered(true);
			return;
		}

		clean();

		if (this.storage.containsKey(revIndex)) {
			// throw new IllegalArgumentException(revisionIndex +
			// "- Object already contained: " + revIndex);
			return;
		}

		storage.put(revIndex, block);
		size += block.length();

		if (first == null) {
			first = block;
			last = block;
		}
		else {

			ChronoStorageBlock previous = null, current = first;
			do {
				if (revIndex < current.getRevisionIndex()) {

					block.setIndexPrev(previous);
					block.setIndexNext(current);

					if (previous != null) {
						previous.setIndexNext(block);
					}
					current.setIndexPrev(block);

					if (current == first) {
						this.first = block;
					}

					return;
				}

				previous = current;
				current = current.getIndexNext();

			}
			while (current != null);

			// Add to end of list
			previous.setIndexNext(block);
			block.setIndexPrev(previous);

			this.last = block;
		}
	}

	/**
	 * Returns whether more chrono storage blocks are available.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean hasMore()
	{
		return this.first != null;
	}

	/**
	 * Removes a revision from the chrono storage.
	 * 
	 * @return
	 */
	public Revision remove()
	{

		ChronoStorageBlock block = first;
		this.revisionIndex = block.getRevisionIndex();

		ChronoStorageBlock next = block.getIndexNext();
		this.first = next;

		if (next != null) {
			this.first.setIndexPrev(null);
		}
		else {
			this.last = null;
		}

		/*
		 * System.out.println("Deliver " + block.getRevisionIndex() + "  RI|RC "
		 * + block.getRevisionCounter()); if (first != null) {
		 * System.out.println("OnTop: " + first.getRevisionIndex()); }
		 */
		block.setDelivered(true);

		// Remove from fullRevSet
		ChronoFullRevision cfr = block.getChronoFullRevision();
		cfr.remove(block.getRevisionCounter());

		if (storage.remove(block.getRevisionIndex()) == null) {
			throw new RuntimeException("VALUE WAS NOT REMOVED FROM STORAGE");
		}

		// Subtract size
		Revision rev = block.getRev();
		size -= rev.getRevisionText().length();
		return rev;
	}

	/**
	 * Checks whether the specified chrono storage block is contained or not.
	 * 
	 * @param revisionIndex
	 *            chronological order index
	 * @return
	 */
	public boolean contains(final int revisionIndex)
	{
		return this.storage.containsKey(revisionIndex);
	}

	/**
	 * Checks whether the chrono storage block is on top or not.
	 * 
	 * @param revisionIndex
	 *            chronological order index
	 * @return
	 */
	public boolean isTop(final int revisionIndex)
	{
		if (this.first != null) {
			return this.first.getRevisionIndex() == revisionIndex;
		}

		return false;
	}

	/**
	 * Returns the revision of the specified chrono storage block.
	 * 
	 * @param revisionIndex
	 *            chronological order index
	 * @return
	 */
	public Revision get(final int revisionIndex)
	{
		if (this.storage.containsKey(revisionIndex)) {

			ChronoStorageBlock block = this.storage.get(revisionIndex);
			return block.getRev();
		}
		return null;
	}

	/** Temporary variable - total size of the chrono storage */
	private long totalSize;

	/**
	 * Reduces the amount of used storage by discarding chrono storage blocks.
	 */
	public void clean()
	{

		ChronoFullRevision cfr = firstCFR;
		totalSize = size;
		while (cfr != null) {
			totalSize += cfr.size();
			cfr = cfr.getNext();
		}

		if (totalSize < MAX_STORAGE_SIZE) {
			return;
		}

		cfr = firstCFR;
		while (cfr != null) {
			totalSize += cfr.clean(revisionIndex, 0);
			cfr = cfr.getNext();
		}

		ChronoStorageBlock block;
		while (last != null && totalSize >= MAX_STORAGE_SIZE) {

			// System.out.println("CLEAN " + last.getRevisionIndex());

			// Retrieve previous block
			block = last.getIndexPrev();

			// Subtract size
			if (storage.remove(last.getRevisionIndex()) == null) {
				throw new RuntimeException("VALUE WAS NOT REMOVED FROM STORAGE");
			}
			totalSize -= last.length();
			size += last.length();

			// Delete references
			if (block != null) {
				block.setIndexNext(null);
			}
			last.setIndexPrev(null);

			cfr = last.getChronoFullRevision();
			totalSize += cfr.size()
					- cfr.clean(revisionIndex, last.getRevisionIndex());

			if (last == first) {
				first = null;
			}

			// Set the new last
			last = block;
		}

		System.gc();
	}

	/**
	 * Returns a description of the chrono storage size.
	 * 
	 * @return current revision index | storage size | size | total size
	 */
	public String getStorageSize()
	{
		return this.revisionIndex + " | " + this.storage.size() + " | "
				+ this.size + " | " + totalSize;
	}
}
