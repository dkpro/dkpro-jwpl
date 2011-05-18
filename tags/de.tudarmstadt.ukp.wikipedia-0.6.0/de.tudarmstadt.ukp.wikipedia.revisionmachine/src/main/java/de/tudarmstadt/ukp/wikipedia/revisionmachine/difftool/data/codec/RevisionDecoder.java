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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DecodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffAction;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * The RevisionDecoder class contains methods to decode an encoded diff
 * information.
 * 
 * 
 * 
 */
public class RevisionDecoder
{

	/** Reference to the BitReader */
	private BitReader r;

	/** Configuration Parameter - Wikipedia Encoding */
	private final String WIKIPEDIA_ENCODING;

	/**
	 * (Constructor) Creates a new RevisionDecoder object.
	 * 
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 *             parameters
	 */
	private RevisionDecoder()
		throws ConfigurationException
	{

		// Load config parameters
		ConfigurationManager config = ConfigurationManager.getInstance();

		WIKIPEDIA_ENCODING = (String) config
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);
	}

	/**
	 * (Constructor) Creates a new RevisionDecoder object.
	 * 
	 * @param wikipediaEncoding
	 *            Character encoding
	 */
	public RevisionDecoder(final String wikipediaEncoding)
	{

		WIKIPEDIA_ENCODING = wikipediaEncoding;
	}

	/**
	 * (Constructor) Creates a new RevisionDecoder object.
	 * 
	 * @param input
	 *            binary encoded diff
	 * 
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 *             parameters
	 */
	public RevisionDecoder(final byte[] input)
		throws ConfigurationException
	{

		this();
		if (input[0] == -128) {
			r = new BitReader(inflateInput(input, 1));
		}
		else {
			r = new BitReader(input);
		}
	}

	/**
	 * Decodes the information and returns the Diff.
	 * 
	 * @return Diff
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	public Diff decode()
		throws UnsupportedEncodingException, DecodingException
	{

		int header = r.read(3);
		if (DiffAction.parse(header) != DiffAction.DECODER_DATA) {

			throw new DecodingException("Invalid codecData code: " + header);
		}

		int blockSize_C = 3;
		int blockSize_S = r.read(5);
		int blockSize_E = r.read(5);
		int blockSize_B = r.read(5);
		int blockSize_L = r.read(5);
		r.read(1);

		if (blockSize_S < 0 || blockSize_S > 31) {
			throw new DecodingException("blockSize_S out of range: "
					+ blockSize_S);
		}
		if (blockSize_E < 0 || blockSize_E > 31) {
			throw new DecodingException("blockSize_E out of range: "
					+ blockSize_E);
		}
		if (blockSize_B < 0 || blockSize_B > 31) {
			throw new DecodingException("blockSize_B out of range: "
					+ blockSize_B);
		}
		if (blockSize_L < 0 || blockSize_L > 31) {
			throw new DecodingException("blockSize_L out of range: "
					+ blockSize_L);
		}

		return decode(blockSize_C, blockSize_S, blockSize_E, blockSize_B,
				blockSize_L);
	}

	/**
	 * Decodes the information, after the codec was successfully decoded, and
	 * returns the Diff.
	 * 
	 * @param blockSize_C
	 *            length of a C block
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_E
	 *            length of a E block
	 * @param blockSize_B
	 *            length of a B block
	 * @param blockSize_L
	 *            length of a L block
	 * @return Diff
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private Diff decode(final int blockSize_C, final int blockSize_S,
			final int blockSize_E, final int blockSize_B, final int blockSize_L)
		throws UnsupportedEncodingException, DecodingException
	{

		int code = r.read(blockSize_C);
		Diff diff = new Diff();

		while (code != -1) {
			// System.out.print(code + "\t");

			switch (DiffAction.parse(code)) {
			case FULL_REVISION_UNCOMPRESSED:
				diff.add(decodeFullRevision(blockSize_L));
				break;
			case INSERT:
				diff.add(decodeAdd(blockSize_S, blockSize_L));
				break;
			case DELETE:
				diff.add(decodeDelete(blockSize_S, blockSize_E));
				break;
			case REPLACE:
				diff.add(decodeReplace(blockSize_S, blockSize_E, blockSize_L));
				break;
			case CUT:
				diff.add(decodeCut(blockSize_S, blockSize_E, blockSize_B));
				break;
			case PASTE:
				diff.add(decodePaste(blockSize_S, blockSize_B, r));
				break;
			default:
				throw new DecodingException("Invalid block_c code: " + code);
			}

			// System.out.println();
			code = r.read(blockSize_C);
		}

		return diff;
	}

	/**
	 * Decodes an Add operation.
	 * 
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_L
	 *            length of a L block
	 * @return DiffPart, Add operation
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodeAdd(final int blockSize_S, final int blockSize_L)
		throws UnsupportedEncodingException, DecodingException
	{

		if (blockSize_S < 1 || blockSize_L < 1) {
			throw new DecodingException("Invalid value for blockSize_S: "
					+ blockSize_S + " or blockSize_L: " + blockSize_L);
		}

		int s = r.read(blockSize_S);
		int l = r.read(blockSize_L);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (int i = 0; i < l; i++) {
			output.write(r.readByte());
		}

		DiffPart part = new DiffPart(DiffAction.INSERT);
		part.setStart(s);
		part.setText(output.toString(WIKIPEDIA_ENCODING));

		return part;
	}

	/**
	 * Decodes a Cut operation.
	 * 
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_E
	 *            length of a E block
	 * @param blockSize_B
	 *            length of a B block
	 * @return DiffPart, Cut operation
	 * 
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodeCut(final int blockSize_S, final int blockSize_E,
			final int blockSize_B)
		throws DecodingException
	{

		if (blockSize_S < 1 || blockSize_E < 1 || blockSize_B < 1) {
			throw new DecodingException("Invalid value for blockSize_S: "
					+ blockSize_S + ", blockSize_E: " + blockSize_E
					+ " or blockSize_B: " + blockSize_B);
		}

		int s = r.read(blockSize_S);
		int e = r.read(blockSize_E);
		int b = r.read(blockSize_B);

		DiffPart part = new DiffPart(DiffAction.CUT);
		part.setStart(s);
		part.setLength(e);
		part.setText(Integer.toString(b));

		r.skip();

		return part;
	}

	/**
	 * Decodes a Delete operation.
	 * 
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_E
	 *            length of a E block
	 * @return DiffPart, Delete operation
	 * 
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodeDelete(final int blockSize_S, final int blockSize_E)
		throws DecodingException
	{

		if (blockSize_S < 1 || blockSize_E < 1) {
			throw new DecodingException("Invalid value for blockSize_S: "
					+ blockSize_S + " or blockSize_E: " + blockSize_E);
		}

		int s = r.read(blockSize_S);
		int e = r.read(blockSize_E);

		DiffPart part = new DiffPart(DiffAction.DELETE);
		part.setStart(s);
		part.setLength(e);

		r.skip();

		return part;
	}

	/**
	 * Decodes a FullRevision operation.
	 * 
	 * @param blockSize_L
	 *            length of a L block
	 * @return DiffPart, FullRevision
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodeFullRevision(final int blockSize_L)
		throws UnsupportedEncodingException, DecodingException
	{

		if (blockSize_L < 1) {
			throw new DecodingException("Invalid value for blockSize_L: "
					+ blockSize_L);
		}

		int l = r.read(blockSize_L);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (int i = 0; i < l; i++) {
			output.write(r.readByte());
		}
		DiffPart part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
		part.setText(output.toString(WIKIPEDIA_ENCODING));

		return part;
	}

	/**
	 * Decodes a Paste operation.
	 * 
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_B
	 *            length of a B block
	 * @return DiffPart, Paste operation
	 * 
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodePaste(final int blockSize_S, final int blockSize_B,
			final BitReader r)
		throws DecodingException
	{

		if (blockSize_S < 1 || blockSize_B < 1) {
			throw new DecodingException("Invalid value for blockSize_S: "
					+ blockSize_S + " or blockSize_B: " + blockSize_B);
		}

		int s = r.read(blockSize_S);
		int b = r.read(blockSize_B);

		DiffPart part = new DiffPart(DiffAction.PASTE);
		part.setStart(s);
		part.setText(Integer.toString(b));

		r.skip();

		return part;
	}

	/**
	 * Decodes a Replace operation.
	 * 
	 * @param blockSize_S
	 *            length of a S block
	 * @param blockSize_E
	 *            length of a E block
	 * @param blockSize_L
	 *            length of a L block
	 * @return DiffPart, Replace operation
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws DecodingException
	 *             if the decoding failed
	 */
	private DiffPart decodeReplace(final int blockSize_S,
			final int blockSize_E, final int blockSize_L)
		throws UnsupportedEncodingException, DecodingException
	{

		if (blockSize_S < 1 || blockSize_E < 1 || blockSize_L < 1) {
			throw new DecodingException("Invalid value for blockSize_S: "
					+ blockSize_S + ", blockSize_E: " + blockSize_E
					+ " or blockSize_L: " + blockSize_L);
		}

		int s = r.read(blockSize_S);
		int e = r.read(blockSize_E);
		int l = r.read(blockSize_L);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (int i = 0; i < l; i++) {
			output.write(r.readByte());
		}

		DiffPart part = new DiffPart(DiffAction.REPLACE);
		part.setStart(s);
		part.setLength(e);
		part.setText(output.toString(WIKIPEDIA_ENCODING));

		return part;
	}

	/**
	 * Inflates the zipped input.
	 * 
	 * @param zipinput
	 *            zipped input
	 * @param start
	 *            start position
	 * @return inflated input
	 */
	private byte[] inflateInput(final byte[] zipinput, final int start)
	{
		ByteArrayOutputStream stream;
		try {
			byte[] compressedInput = zipinput;
			Inflater decompresser = new Inflater();
			decompresser.setInput(compressedInput, start,
					compressedInput.length - start);

			byte[] output = new byte[1000];
			stream = new ByteArrayOutputStream();

			int cLength;
			do {
				cLength = decompresser.inflate(output);
				stream.write(output, 0, cLength);
			}
			while (cLength == 1000);

		}
		catch (DataFormatException e) {
			throw new RuntimeException(e);
		}

		return stream.toByteArray();
	}

	/**
	 * Assigns the binary input.
	 * 
	 * @param input
	 *            binary encoded diff
	 */
	public void setInput(final byte[] input)
	{

		if (input[0] == -128) {
			r = new BitReader(inflateInput(input, 1));
		}
		else {
			r = new BitReader(input);
		}
	}

	/**
	 * Assigns an input stream.
	 * 
	 * @param input
	 *            Reference to an input stream
	 * @param binary
	 *            flag, whether the data is binary or not
	 * 
	 * @throws IOException
	 *             if an error occurs while reading the stream
	 */
	public void setInput(final InputStream input, final boolean binary)
		throws IOException
	{

		if (!binary) {

			int v = input.read();
			StringBuilder buffer = new StringBuilder();

			// Check for the no-zip flag
			boolean zipFlag = (char) v == '_';
			if (zipFlag) {
				v = input.read();
			}

			while (v != -1) {
				buffer.append((char) v);
				v = input.read();
			}

			if (zipFlag) {
				r = new BitReader(inflateInput(
						Base64.decodeBase64(buffer.toString()), 0));
			}
			else {
				r = new BitReader(Base64.decodeBase64(buffer.toString()));
			}
		}
		else {

			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			byte[] bData;
			int l = input.available();
			while (l != 0) {

				bData = new byte[l];

				if (input.read(bData) != l) {
					throw new RuntimeException("ILLEGAL NUMBER OF BYTES READ");
				}
				stream.write(bData);

				l = input.available();
			}

			if (input.read() != -1) {
				throw new RuntimeException("END OF STREAM NOT REACHED");
			}

			bData = stream.toByteArray();

			boolean zipFlag = bData[0] == -128;

			if (zipFlag) {
				r = new BitReader(inflateInput(bData, 1));
			}
			else {
				r = new BitReader(bData);
			}
		}
	}

	/**
	 * Assigns base 64 encoded input.
	 * 
	 * @param input
	 *            base 64 encoded diff
	 * 
	 * @throws DecodingException
	 *             if the decoding fails
	 */
	public void setInput(final String input)
		throws DecodingException
	{

		boolean zipFlag = input.charAt(0) == '_';
		if (zipFlag) {
			r = new BitReader(inflateInput(
					Base64.decodeBase64(input.substring(1)), 0));
		}
		else {
			byte[] data = Base64.decodeBase64(input);
			if (data == null) {

				for (int i = 0; i < input.length(); i++) {
					System.err.println(i + ": " + (int) input.charAt(i)
							+ " <> " + input.charAt(i));
				}

				throw new DecodingException("BASE 64 DECODING FAILED: " + input);
			}
			r = new BitReader(data);
		}
	}
}
