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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionCodecData;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.ISizeable;

/**
 * This class contains the diff information used to create single revision.
 *
 *
 *
 */
public class Diff
	implements ISizeable
{

	/** Reference to the codec */
	private RevisionCodecData codecData;

	/** List of DiffParts */
	private final List<DiffPart> parts;

	/** Revision counter */
	private int revisionCounter;

	/** Revision ID */
	private int revisionID;

	/** Timestamp */
	private Timestamp timeStamp;

	/** Username/IP of the contributor who created this revision */
	private String contributorName;

	/** ID of the contributor who created this revision */
	private Integer contributorId;

	/** Determine whether the contributor was registered.
	 * True: contributorName= username
	 * False: contributorName= IP
	 */
	private boolean contributorIsRegistered;

	/** The user comment for this revision*/
	private String comment;

	/** Determine whether revision is a minor revision */
	private boolean isMinor = false;

	/**
	 * (Constructor) Creates a new empty Diff.
	 */
	public Diff()
	{
		this.parts = new ArrayList<DiffPart>();
	}

	/**
	 * Adds a DiffPart.
	 *
	 * @param diff
	 *            DiffPart
	 */
	public void add(final DiffPart diff)
	{
		this.parts.add(diff);
	}

	/**
	 * Builds the current revision.
	 *
	 * @param previousRevision
	 *            content of the previous revision
	 * @return current revision
	 */
	public String buildRevision(final char[] previousRevision)
	{
		String prevRev = null;
		if (previousRevision != null) {
			prevRev = String.valueOf(previousRevision);
		}

		return buildRevision(prevRev);
	}

	/**
	 * Builds the current revision.
	 *
	 * @param previousRevision
	 *            content of the previous revision
	 * @return current revision
	 */
	public String buildRevision(final String previousRevision)
	{

		HashMap<String, String> bufferMap = new HashMap<String, String>();

		StringBuilder output = new StringBuilder();
		if (previousRevision != null) {
			output.append(previousRevision);
		}

		int size = parts.size();
		DiffPart part;

		for (int i = 0; i < size; i++) {

			part = parts.get(i);

			switch (part.getAction()) {
			case FULL_REVISION_UNCOMPRESSED:
				output = new StringBuilder();
				output.insert(0, part.getText());
				break;
			case INSERT:
				output.insert(part.getStart(), part.getText());
				break;
			case DELETE:
				output.delete(part.getStart(), part.getEnd());
				break;
			case REPLACE:
				output.replace(part.getStart(), part.getEnd(), part.getText());
				break;
			case CUT:
				bufferMap.put(part.getText(),
						output.substring(part.getStart(), part.getEnd()));
				output.delete(part.getStart(), part.getEnd());
				break;
			case PASTE:
				output.insert(part.getStart(), bufferMap.remove(part.getText()));
				break;
			default:
				throw new RuntimeException("UNKNOWN PART ACTION");
			}
		}

		return output.toString();
	}

	/**
	 * Returns an estimation of the size used to stored the data.
	 *
	 * @return estimated size
	 */
	public long byteSize()
	{

		long byteSize = 3;

		int size = parts.size();

		for (int i = 0; i < size; i++) {
			byteSize += this.parts.get(i).byteSize();
		}

		return byteSize;
	}

	/**
	 * Returns the referenced diff part.
	 *
	 * @param index
	 *            index of the diff part
	 * @return diff part
	 */
	public DiffPart get(final int index)
	{
		return this.parts.get(index);
	}

	/**
	 * Returns the codec data.
	 *
	 * @return codec
	 */
	public RevisionCodecData getCodecData()
	{
		return codecData;
	}

	/**
	 * Returns the revision counter.
	 *
	 * @return revision counter
	 */
	public int getRevisionCounter()
	{
		return this.revisionCounter;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.tud.ukp.kulessa.delta.data.IRevisionChange#getRevisionID()
	 */
	public int getRevisionID()
	{
		return revisionID;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.tud.ukp.kulessa.delta.data.IRevisionChange#getTimeStamp()
	 */
	public Timestamp getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * Returns whether the revision described by this diff is a full revision or
	 * not.
	 *
	 * @return TRUE | FALSE
	 */
	public boolean isFullRevision()
	{
		if (this.parts.size() == 1) {
			DiffPart p = this.parts.get(0);
			if (p.getAction() == DiffAction.FULL_REVISION_UNCOMPRESSED) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.tud.ukp.kulessa.delta.data.IRevisionChange#iterator()
	 */
	public Iterator<DiffPart> iterator()
	{
		return this.parts.iterator();
	}

	/**
	 * Sets the codec data.
	 *
	 * @param codecData
	 *            coded data
	 */
	public void setCodecData(final RevisionCodecData codecData)
	{
		this.codecData = codecData;
	}

	/**
	 * Sets the revision counter.
	 *
	 * @param revisionCounter
	 *            revision counter
	 */
	public void setRevisionCoutner(final int revisionCounter)
	{
		this.revisionCounter = revisionCounter;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.tud.ukp.kulessa.delta.data.IRevisionChange#setRevisionID(int)
	 */
	public void setRevisionID(final int revisionID)
	{
		this.revisionID = revisionID;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.tud.ukp.kulessa.delta.data.IRevisionChange#setTimeStamp(java.lang.
	 * String)
	 */
	public void setTimeStamp(final Timestamp timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	/**
	 * Returns the number of stored diff parts.
	 *
	 * @return number of diff parts
	 */
	public int size()
	{
		return this.parts.size();
	}

	/**
	 * Returns the string representation of the diff content.
	 *
	 * @return string representation of the diff parts
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.size(); i++) {
			builder.append(parts.get(i).toString() + "\n");
		}
		return builder.toString();
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		return comment;
	}

	public void setMinor(boolean isMinor)
	{
		this.isMinor = isMinor;
	}

	public boolean isMinor()
	{
		return isMinor;
	}

	public void setContributorName(String contributorName)
	{
		this.contributorName = contributorName;
	}

	public String getContributorName()
	{
		return contributorName;
	}

	public void setContributorIsRegistered(boolean contributorIsRegistered)
	{
		this.contributorIsRegistered = contributorIsRegistered;
	}

	public boolean getContributorIsRegistered()
	{
		return contributorIsRegistered;
	}

	public void setContributorId(Integer contributorId)
	{
		this.contributorId = contributorId;
	}

	public Integer getContributorId()
	{
		return contributorId;
	}
}
