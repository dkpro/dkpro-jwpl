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

import java.io.ByteArrayOutputStream;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;

/**
 * The BitWriter buffers bit that will be written byte-by-byte to an output
 * stream.
 * 
 * 
 * 
 */
public class BitWriter
{

	/** Output buffer */
	private ByteArrayOutputStream stream;

	/** Buffer to store the bits */
	private int buffer;

	/** Number of stored bits */
	private byte bufferLength = 0;

	/**
	 * Constructor Creates a BitWriter with a byte buffer of the given length.
	 * 
	 * @param length
	 *            Length of the byte buffer
	 */
	public BitWriter(final int length)
	{
		this.stream = new ByteArrayOutputStream(length);
	}

	/**
	 * Constructor Creates a BitWriter with a standard buffer.
	 */
	public BitWriter()
	{
		this.stream = new ByteArrayOutputStream();
	}

	/**
	 * Writes a byte to the buffer.
	 * 
	 * @param val
	 *            an integer representing a full byte
	 * 
	 * @throws EncodingException
	 *             if the value is out range
	 */
	private void write(final int val)
		throws EncodingException
	{

		if (val < 0 || val > 255) {
			throw ErrorFactory.createEncodingException(
					ErrorKeys.DIFFTOOL_ENCODING_VALUE_OUT_OF_RANGE,
					"byte value out of range: " + val);
		}

		this.stream.write(val);
	}

	/**
	 * Writes a single bit to the buffer.
	 * 
	 * @param bit
	 *            0 or 1
	 * @throws EncodingException
	 *             if the input is neither 0 nor 1.
	 */
	public void writeBit(final int bit)
		throws EncodingException
	{

		if (bit != 0 && bit != 1) {
			throw ErrorFactory.createEncodingException(
					ErrorKeys.DIFFTOOL_ENCODING_VALUE_OUT_OF_RANGE,
					"bit value out of range: " + bit);
		}

		this.buffer |= bit << (7 - this.bufferLength);
		this.bufferLength++;

		if (bufferLength == 8) {

			write(buffer);

			this.bufferLength = 0;
			this.buffer = 0;
		}
	}

	/**
	 * Writes a positive integer to the buffer.
	 * 
	 * @param length
	 *            the number of bits to write
	 * @param value
	 *            an integer value
	 * 
	 * @throws EncodingException
	 *             if the length of the input is more than 31 bits.
	 */
	public void writeValue(final int length, final int value)
		throws EncodingException
	{
		if (length > 31) {
			throw ErrorFactory.createEncodingException(
					ErrorKeys.DIFFTOOL_ENCODING_VALUE_OUT_OF_RANGE,
					"more than maximum length: " + value);
		}

		for (int i = length - 1; i >= 0; i--) {
			writeBit((value >> i) & 1);
		}
	}

	/**
	 * Writes the byte array to the buffer. The currently used buffer will be
	 * filled with zero bits before is is written in front of the byte-array.
	 * 
	 * @param bText
	 *            byte array
	 * 
	 * @throws EncodingException
	 *             if the writing fails
	 */
	public void write(final byte[] bText)
		throws EncodingException
	{

		writeFillBits();

		int l = bText.length;
		for (int i = 0; i < l; i++) {
			write(0xFF & bText[i]);
		}
	}

	/**
	 * The currently used buffer will be filled with zero bits before is is
	 * written in the buffer.
	 * 
	 * @throws EncodingException
	 *             if the writing fails
	 */
	public void writeFillBits()
		throws EncodingException
	{

		while (this.bufferLength != 0) {
			writeBit(0);
		}

		this.buffer = 0;
	}

	/**
	 * Returns the content of the buffer as byte-array.
	 * 
	 * @return byte-array
	 */
	public byte[] toByteArray()
	{
		return this.stream.toByteArray();
	}
}
