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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation;

/**
 * Contains the information for a block. Used for the Diff Calculation.
 * 
 * 
 * 
 */
public class DiffBlock
	implements Comparable<DiffBlock>
{

	/** Block ID */
	private int id;

	/** Start position in revision A */
	private int revAStart;

	/** End position in revision A */
	private int revAEnd;

	/** Start position in revision B */
	private int revBStart;

	/** End position in revision B */
	private int revBEnd;

	/**
	 * Flag, indicating the sorting order TRUE sorting after the start position
	 * of revision A FALSE sorting after the start position of revision B
	 */
	private boolean ab;

	/**
	 * (DiffBlock) Creates a new DiffBlock.
	 * 
	 * @param id
	 *            ID of the block
	 * @param revAStart
	 *            start position of revision A
	 * @param revAEnd
	 *            end position of revision A
	 * @param revBStart
	 *            start position of revision B
	 * @param revBEnd
	 *            end position of revision B
	 * @param ab
	 *            sorting order flag
	 */
	public DiffBlock(final int id, final int revAStart, final int revAEnd,
			final int revBStart, final int revBEnd, final boolean ab)
	{
		this.id = id;
		this.revAStart = revAStart;
		this.revAEnd = revAEnd;
		this.revBStart = revBStart;
		this.revBEnd = revBEnd;
		this.ab = ab;
	}

	/**
	 * Compares the positions of both blocks.
	 * 
	 * @param b
	 *            Block
	 */
	public int compareTo(final DiffBlock b)
	{
		if (ab) {
			return this.revAStart - b.revAStart;
		}
		else {
			return this.revBStart - b.revBStart;
		}
	}

	/**
	 * Returns whether the block is valid or not.
	 * 
	 * @return TRUE if the block has a ID of the value -1 FALSE otherwise
	 */
	public boolean isUnknown()
	{
		return (id == -1);
	}

	/**
	 * Returns the ID of this block.
	 * 
	 * @return string representation
	 */
	public String toString()
	{
		return Integer.toString(id);
	}

	/**
	 * Returns the ID of this block.
	 * 
	 * @return ID of this block
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns the end position of the block in revision A.
	 * 
	 * @return end position revision A
	 */
	public int getRevAEnd()
	{
		return revAEnd;
	}

	/**
	 * Returns the start position of the block in revision A.
	 * 
	 * @return start position revision A
	 */
	public int getRevAStart()
	{
		return revAStart;
	}

	/**
	 * Returns the end position of the block in revision B.
	 * 
	 * @return end position revision B
	 */
	public int getRevBEnd()
	{
		return revBEnd;
	}

	/**
	 * Returns the start position of the block in revision B.
	 * 
	 * @return start position revision B
	 */
	public int getRevBStart()
	{
		return revBStart;
	}
}
