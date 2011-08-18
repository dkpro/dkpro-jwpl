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

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;

/**
 * This class contains the constants for the DiffActions.
 * 
 * 
 * 
 */
public enum DiffAction
{

	/** Codec */
	DECODER_DATA((byte) 0),

	/** Full Revision */
	FULL_REVISION_UNCOMPRESSED((byte) 1),

	/** Insert operation */
	INSERT((byte) 2),

	/** Delete operation */
	DELETE((byte) 3),

	/** Replace operation */
	REPLACE((byte) 4),

	/** Cut operation */
	CUT((byte) 5),

	/** Paste operation */
	PASTE((byte) 6)/*
					 * ,
					 * 
					 * FULL_REVISION_COMPRESSED((byte)7)
					 */;

	/** byte constant */
	private byte code;

	/**
	 * Creates a DiffAction.
	 * 
	 * @param code
	 *            byte constant
	 */
	private DiffAction(final byte code)
	{
		this.code = code;
	}

	/**
	 * Returns the byte constant
	 * 
	 * @return value of the constant
	 */
	public byte getValue()
	{
		return code;
	}

	/**
	 * Returns the appropriate DiffAction value.
	 * 
	 * @param val
	 *            byte value
	 * @return DiffAction
	 * 
	 * @throws DecodingException
	 *             if the value does not match one of the predefined byte
	 *             constants
	 */
	public static DiffAction parse(final int val)
		throws DecodingException
	{

		switch (val) {
		case 0:
			return DECODER_DATA;
		case 1:
			return FULL_REVISION_UNCOMPRESSED;
		case 2:
			return INSERT;
		case 3:
			return DELETE;
		case 4:
			return REPLACE;
		case 5:
			return CUT;
		case 6:
			return PASTE;
			// case 7: return FULL_REVISION_COMPRESSED;
		default:
			throw ErrorFactory.createDecodingException(
					ErrorKeys.DIFFTOOL_ENCODING_INVALID_VALUE,
					"Invalid value: " + val);
		}
	}
}
