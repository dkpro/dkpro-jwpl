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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec;

/**
 * The RevisionCodecData class contains all necessary information to encode the
 * diff information.
 * 
 * Block C 3bit operation value Block S start position Block E length (end
 * position = start position + length) Block B block id Block L length of the t
 * block Block T block containing L bytes data
 * 
 * 
 * 
 */
public class RevisionCodecData
{

	/** Maximum size of a S block */
	private int blocksize_S;

	/** Maximum size of a E block */
	private int blocksize_E;

	/** Maximum size of an B block */
	private int blocksize_B;

	/** Maximum size of an L block */
	private int blocksize_L;

	/** Number of C blocks */
	private int countC;

	/** Number of S blocks */
	private int countS;

	/** Number of E blocks */
	private int countE;

	/** Number of B blocks */
	private int countB;

	/** Number of L blocks */
	private int countL;

	/** Number of T blocks */
	private int countT;

	/** Whether the information has already been converted or not */
	private boolean converted;

	/**
	 * Constructor Creates a new RevisionCodecData object.
	 */
	public RevisionCodecData()
	{
		this.converted = false;
		this.blocksize_S = 0;
		this.blocksize_E = 0;
		this.blocksize_B = 0;
		this.blocksize_L = 0;
	}

	/**
	 * Gathers the information about an s block.
	 * 
	 * @param value
	 *            start position
	 */
	public void checkBlocksizeS(final int value)
	{
		if (value > blocksize_S) {
			this.blocksize_S = value;
		}
		this.countS++;
		this.countC++;
	}

	/**
	 * Gathers the information about an e block.
	 * 
	 * @param value
	 *            length of the diff-block
	 */
	public void checkBlocksizeE(final int value)
	{
		if (value > blocksize_E) {
			this.blocksize_E = value;
		}
		this.countE++;
	}

	/**
	 * Gathers the information about an b block.
	 * 
	 * @param value
	 *            block id
	 */
	public void checkBlocksizeB(final int value)
	{
		if (value > blocksize_B) {
			this.blocksize_B = value;
		}
		this.countB++;
	}

	/**
	 * Gathers the information about an l block.
	 * 
	 * @param value
	 *            length of the text block
	 */
	public void checkBlocksizeL(final int value)
	{
		if (value > blocksize_L) {
			this.blocksize_L = value;
		}
		this.countL++;
		this.countT += value;
	}

	/**
	 * Converts the input information into their log2 values. If an operation is
	 * contained in the diff, the minimum number of bits used to encode this
	 * block is 1 byte.
	 * 
	 * @return number of bytes needed to encode the associated diff
	 */
	public int totalSizeInBits()
	{

		if (converted) {

			return 24 + this.countC * 3 + this.countS * blocksize_S
					+ this.countE * blocksize_E + this.countB * blocksize_B
					+ this.countL * blocksize_L + this.countT * 8;
		}

		converted = true;
		// System.out.println(this.toString());

		if (this.blocksize_B > 0) {
			this.blocksize_B = (int) Math.ceil(Math.log(blocksize_B + 1)
					/ Math.log(2.));
		}
		else if (this.countB > 0) {
			this.blocksize_B = 1;
		}

		if (this.blocksize_E > 0) {
			this.blocksize_E = (int) Math.ceil(Math.log(blocksize_E + 1)
					/ Math.log(2.));
		}
		else if (this.countE > 0) {
			this.blocksize_E = 1;
		}

		if (this.blocksize_L > 0) {
			this.blocksize_L = (int) Math.ceil(Math.log(blocksize_L + 1)
					/ Math.log(2.));
		}
		else if (this.countL > 0) {
			this.blocksize_L = 1;
		}

		if (this.blocksize_S > 0) {
			this.blocksize_S = (int) Math.ceil(Math.log(blocksize_S + 1)
					/ Math.log(2.));
		}
		else if (this.countS > 0) {
			this.blocksize_S = 1;
		}

		return 24 + this.countC * 3 + this.countS * blocksize_S + this.countE
				* blocksize_E + this.countB * blocksize_B + this.countL
				* blocksize_L + this.countT * 8;
	}

	/**
	 * Returns the number of bits used to encode a B block. This method is
	 * intended to used after the conversion.
	 * 
	 * @return block bit-length
	 */
	public int getBlocksizeB()
	{
		return this.blocksize_B;
	}

	/**
	 * Returns the number of bits used to encode a E block. This method is
	 * intended to used after the conversion.
	 * 
	 * @return block bit-length
	 */
	public int getBlocksizeE()
	{
		return this.blocksize_E;
	}

	/**
	 * Returns the number of bits used to encode a L block. This method is
	 * intended to used after the conversion.
	 * 
	 * @return block bit-length
	 */
	public int getBlocksizeL()
	{
		return this.blocksize_L;
	}

	/**
	 * Returns the number of bits used to encode a S block. This method is
	 * intended to used after the conversion.
	 * 
	 * @return block bit-length
	 */
	public int getBlocksizeS()
	{
		return this.blocksize_S;
	}

	/**
	 * String representation of the revision codec data.
	 * 
	 * @return string representation
	 */
	public String toString()
	{
		return this.blocksize_S + " " + this.blocksize_E + " "
				+ this.blocksize_B + " " + this.blocksize_L;
	}

	/**
	 * Whether the information has already converted to the log2 basis or not.
	 * 
	 * @return conversion information
	 */
	public boolean isConverted()
	{
		return this.converted;
	}
}
