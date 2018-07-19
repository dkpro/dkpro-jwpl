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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;

/**
 * The BitReader buffers a byte-array.
 *
 *
 *
 */
public class BitReader
{

	/** Current index in the byte array */
	private int inputIndex;

	/** Byte input array */
	private byte[] input;

	/** Buffer used to store a single byte */
	private int buffer;

	/** Length of the bits in the buffer that have not been read yet */
	private int bufferLength;

	/**
	 * Constructor of the BitReader
	 *
	 * @param input
	 *            byte input array
	 */
	public BitReader(final byte[] input)
	{
		this.input = input;

		this.buffer = 0;
		this.bufferLength = -1;
		this.inputIndex = 0;
	}

	/**
	 * Reads the next bit from the input.
	 *
	 * @return 0 or 1
	 *
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	public int readBit()
		throws DecodingException
	{

		if (bufferLength == -1) {
			buffer = readByte();
			if (buffer == -1) {
				return -1;
			}

			bufferLength = 7;
		}

		return (buffer >> bufferLength--) & 1;
	}

	/**
	 * Reads the next length-bits from the input.
	 *
	 * The maximum value of bits that could be read is 31. (Maximum value of a
	 * positive number that could be stored in an integer without any
	 * conversion.)
	 *
	 * @param length
	 *            number of bits to read
	 * @return content as integer value or -1 if the end of the stream has been
	 *         reached
	 *
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	public int read(final int length)
		throws DecodingException
	{

		if (length > 31) {
			throw ErrorFactory.createDecodingException(
					ErrorKeys.DIFFTOOL_DECODING_VALUE_OUT_OF_RANGE,
					"more than maximum length: " + length);
		}

		int v, b = 0;
		for (int i = length - 1; i >= 0; i--) {
			v = readBit();
			if (v == -1) {
				if (i != length - 1) {
					throw ErrorFactory
							.createDecodingException(ErrorKeys.DIFFTOOL_DECODING_UNEXPECTED_END_OF_STREAM);
				}

				return -1;
			}
			b |= v << i;
		}

		return b;
	}

	/**
	 * Resets the buffer.
	 */
	public void skip()
	{
		this.buffer = 0;
		this.bufferLength = -1;
	}

	/**
	 * Reads the next character in the input Note: The current content of the
	 * buffer will be deleted. This method should only be used for reading the
	 * textual content of the diff-part.
	 *
	 * @return the next character in the string
	 *
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	public int readByte()
		throws DecodingException
	{

		skip();
		if (input == null || inputIndex >= input.length) {
			return -1;
		}

		return 0xFF & input[inputIndex++];
	}
}
