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
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * The RevisionApi class contains methods to encode the diff information.
 *
 *
 *
 */
public class RevisionEncoder
	implements RevisionEncoderInterface
{

	/** Reference to the codec */
	private RevisionCodecData codecData;

	/** Reference to the BitWriter */
	private BitWriter data;

	/** Configuration Parameter - Zip Compression */
	private final boolean MODE_ZIP_COMPRESSION;

	/** Configuration Parameter - Wikipedia Encoding */
	private final String WIKIPEDIA_ENCODING;

	/**
	 * (Constructor) Creates a new RevisionEnocder object.
	 *
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 *             parameters
	 */
	public RevisionEncoder()
		throws ConfigurationException
	{

		ConfigurationManager config = ConfigurationManager.getInstance();

		WIKIPEDIA_ENCODING = (String) config
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);

		MODE_ZIP_COMPRESSION = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.tud.ukp.kulessa.delta.data.codec.RevisionEncoderInterface#binaryDiff
	 * (de.tud.ukp.kulessa.delta.data.codec.RevisionCodecData,
	 * de.tud.ukp.kulessa.delta.data.tasks.content.Diff)
	 */
	@Override
	public byte[] binaryDiff(final RevisionCodecData codecData, final Diff diff)
		throws UnsupportedEncodingException, EncodingException
	{

		byte[] bData = encode(codecData, diff);
		if (MODE_ZIP_COMPRESSION) {

			Deflater compresser = new Deflater();
			compresser.setInput(bData);
			compresser.finish();

			byte[] output = new byte[1000];
			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			int cLength;
			do {
				cLength = compresser.deflate(output);
				stream.write(output, 0, cLength);
			}
			while (cLength == 1000);

			output = stream.toByteArray();
			if (bData.length + 1 < output.length) {
				return bData;
			}
			else {

				stream = new ByteArrayOutputStream();
				stream.write(new byte[] { -128 }, 0, 1);
				stream.write(output, 0, output.length);

				return stream.toByteArray();
			}
		}

		return bData;
	}

	/**
	 * Creates the binary encoding of the diff while using the codec
	 * information.
	 *
	 * @param codecData
	 *            codec
	 * @param diff
	 *            diff
	 * @return binary data
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private byte[] encode(final RevisionCodecData codecData, final Diff diff)
		throws UnsupportedEncodingException, EncodingException
	{

		this.data = new BitWriter(codecData.totalSizeInBits());
		encodeCodecData(codecData);

		DiffPart part;

		Iterator<DiffPart> partIt = diff.iterator();
		while (partIt.hasNext()) {
			part = partIt.next();

			switch (part.getAction()) {
			case FULL_REVISION_UNCOMPRESSED:
				encodeFullRevisionUncompressed(part);
				break;
			case INSERT:
				encodeInsert(part);
				break;
			case DELETE:
				encodeDelete(part);
				break;
			case REPLACE:
				encodeReplace(part);
				break;
			case CUT:
				encodeCut(part);
				break;
			case PASTE:
				encodePaste(part);
				break;
			/*
			 * case FULL_REVISION_COMPRESSED:
			 * encodeFullRevisionCompressed(part); break;
			 */
			default:
				throw new RuntimeException();
			}
		}

		return data.toByteArray();
	}

	/**
	 * Encodes the codecData.
	 *
	 * @param codecData
	 *            Reference to the codec
	 *
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeCodecData(final RevisionCodecData codecData)
		throws EncodingException
	{

		this.codecData = codecData;

		// C
		data.writeBit(0);
		data.writeBit(0);
		data.writeBit(0);

		// BLOCK SIZES - S E B L
		this.data.writeValue(5, codecData.getBlocksizeS());
		this.data.writeValue(5, codecData.getBlocksizeE());
		this.data.writeValue(5, codecData.getBlocksizeB());
		this.data.writeValue(5, codecData.getBlocksizeL());

		// 1 Bit
		data.writeFillBits();
	}

	/**
	 * Encodes a Cut operation.
	 *
	 * @param part
	 *            Reference to the Cut operation
	 *
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeCut(final DiffPart part)
		throws EncodingException
	{

		// C
		data.writeBit(1);
		data.writeBit(0);
		data.writeBit(1);

		// S
		data.writeValue(codecData.getBlocksizeS(), part.getStart());

		// E
		data.writeValue(codecData.getBlocksizeE(), part.getLength());

		// B
		data.writeValue(codecData.getBlocksizeB(),
				Integer.parseInt(part.getText()));

		data.writeFillBits();

	}

	/**
	 * Encodes a Delete operation.
	 *
	 * @param part
	 *            Reference to the Delete operation
	 *
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeDelete(final DiffPart part)
		throws EncodingException
	{

		// C
		data.writeBit(0);
		data.writeBit(1);
		data.writeBit(1);

		// S
		data.writeValue(codecData.getBlocksizeS(), part.getStart());

		// E
		data.writeValue(codecData.getBlocksizeE(), part.getLength());

		data.writeFillBits();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.tud.ukp.kulessa.delta.data.codec.RevisionEncoderInterface#encodeDiff
	 * (de.tud.ukp.kulessa.delta.data.codec.RevisionCodecData,
	 * de.tud.ukp.kulessa.delta.data.tasks.content.Diff)
	 */
	@Override
	public String encodeDiff(final RevisionCodecData codecData, final Diff diff)
		throws UnsupportedEncodingException, EncodingException
	{

		String sEncoding;
		byte[] bData = encode(codecData, diff);
		if (MODE_ZIP_COMPRESSION) {

			Deflater compresser = new Deflater();
			compresser.setInput(bData);
			compresser.finish();

			byte[] output = new byte[1000];
			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			int cLength;
			do {
				cLength = compresser.deflate(output);
				stream.write(output, 0, cLength);
			}
			while (cLength == 1000);

			output = stream.toByteArray();

			if (bData.length + 1 < output.length) {
				sEncoding = Base64.encodeBase64String(bData);
			}
			else {
				sEncoding = "_" + Base64.encodeBase64String(output);
			}
		}
		else {
			sEncoding = Base64.encodeBase64String(bData);
		}

		return sEncoding;
	}

	/**
	 * Encodes a FullRevision operation.
	 *
	 * @param part
	 *            Reference to the FullRevision operation
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeFullRevisionUncompressed(final DiffPart part)
		throws UnsupportedEncodingException, EncodingException
	{

		// C
		data.writeBit(0);
		data.writeBit(0);
		data.writeBit(1);

		// L T
		String text = part.getText();
		byte[] bText = text.getBytes(WIKIPEDIA_ENCODING);

		data.writeValue(codecData.getBlocksizeL(), bText.length);
		data.write(bText);

	}

	/**
	 * Encodes an Insert operation.
	 *
	 * @param part
	 *            Reference to the Insert operation
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeInsert(final DiffPart part)
		throws UnsupportedEncodingException, EncodingException
	{

		// C
		data.writeBit(0);
		data.writeBit(1);
		data.writeBit(0);

		// S
		data.writeValue(codecData.getBlocksizeS(), part.getStart());

		// L T
		String text = part.getText();
		byte[] bText = text.getBytes(WIKIPEDIA_ENCODING);

		data.writeValue(codecData.getBlocksizeL(), bText.length);
		data.write(bText);
	}

	/**
	 * Encodes a Paste operation.
	 *
	 * @param part
	 *            Reference to the Paste operation
	 *
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodePaste(final DiffPart part)
		throws EncodingException
	{

		// C
		data.writeBit(1);
		data.writeBit(1);
		data.writeBit(0);

		// S
		data.writeValue(codecData.getBlocksizeS(), part.getStart());

		// B
		data.writeValue(codecData.getBlocksizeB(),
				Integer.parseInt(part.getText()));

		data.writeFillBits();
	}

	/**
	 * Encodes a Replace operation.
	 *
	 * @param part
	 *            Reference to the replace operation
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 * @throws EncodingException
	 *             if the encoding failed
	 */
	private void encodeReplace(final DiffPart part)
		throws UnsupportedEncodingException, EncodingException
	{

		// C
		data.writeBit(1);
		data.writeBit(0);
		data.writeBit(0);

		// S
		data.writeValue(codecData.getBlocksizeS(), part.getStart());

		// E
		data.writeValue(codecData.getBlocksizeE(), part.getLength());

		// L T
		String text = part.getText();
		byte[] bText = text.getBytes(WIKIPEDIA_ENCODING);

		data.writeValue(codecData.getBlocksizeL(), bText.length);
		data.write(bText);
	}
}
