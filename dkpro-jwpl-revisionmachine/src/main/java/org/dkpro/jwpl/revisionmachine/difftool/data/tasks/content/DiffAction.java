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
package org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content;

import java.io.Serializable;

import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;

/**
 * This class contains the constants for the DiffActions.
 *
 *
 *
 */
public enum DiffAction implements Serializable
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
	private final byte code;

	/**
	 * Creates a DiffAction.
	 *
	 * @param code
	 *            byte constant
	 */
  DiffAction(final byte code)
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
