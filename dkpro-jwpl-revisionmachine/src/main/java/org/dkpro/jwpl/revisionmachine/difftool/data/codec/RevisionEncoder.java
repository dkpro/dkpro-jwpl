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
package org.dkpro.jwpl.revisionmachine.difftool.data.codec;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Iterator;
import java.util.zip.Deflater;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.EncodingException;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * The RevisionEncoder class contains methods to encode the diff information.
 */
public class RevisionEncoder
    implements RevisionEncoderInterface
{

    /**
     * Reference to the codec
     */
    private RevisionCodecData codecData;

    /**
     * Reference to the BitWriter
     */
    private BitWriter data;

    /**
     * Configuration Parameter - Zip Compression
     */
    private final boolean MODE_ZIP_COMPRESSION;

    /**
     * Configuration Parameter - Wikipedia Encoding
     */
    private final String WIKIPEDIA_ENCODING;

    /**
     * Size of the buffer used to zip compress diffs
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * (Constructor) Creates a new RevisionEnocder object.
     *
     * @throws ConfigurationException
     *             if an error occurs while accessing the configuration parameters
     */
    public RevisionEncoder() throws ConfigurationException
    {

        ConfigurationManager config = ConfigurationManager.getInstance();

        WIKIPEDIA_ENCODING = (String) config
                .getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);
        MODE_ZIP_COMPRESSION = (Boolean) config
                .getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
    }

    @Override
    public byte[] binaryDiff(final RevisionCodecData codecData, final Diff diff)
        throws UnsupportedEncodingException, EncodingException
    {

        byte[] bData = encode(codecData, diff);
        if (MODE_ZIP_COMPRESSION) {

            // The output starts with the zip flag followed by the compressed data
            byte[] output = deflate(bData, true);
            if (bData.length + 1 < output.length - 1) {
                return bData;
            }
            else {
                return output;
            }
        }

        return bData;
    }

    /**
     * Compresses the given data.
     *
     * @param input
     *            data to compress
     * @param zipFlag
     *            whether the zip flag (-128) is written in front of the compressed data
     * @return compressed data, optionally prefixed by the zip flag
     */
    private static byte[] deflate(final byte[] input, final boolean zipFlag)
    {
        final Deflater compresser = new Deflater();
        try {
            compresser.setInput(input);
            compresser.finish();

            final byte[] output = new byte[BUFFER_SIZE];
            final ByteArrayOutputStream stream = new ByteArrayOutputStream(
                    Math.max(BUFFER_SIZE, input.length / 2));
            if (zipFlag) {
                stream.write(-128);
            }

            while (!compresser.finished()) {
                final int cLength = compresser.deflate(output);
                stream.write(output, 0, cLength);
            }
            return stream.toByteArray();
        }
        finally {
            compresser.end();
        }
    }

    /**
     * Creates the binary encoding of the diff while using the codec information.
     *
     * @param codecData
     *            codec
     * @param diff
     *            diff
     * @return binary data
     * @throws UnsupportedEncodingException
     *             if the character encoding is unsupported
     * @throws EncodingException
     *             if the encoding failed
     */
    private byte[] encode(final RevisionCodecData codecData, final Diff diff)
        throws UnsupportedEncodingException, EncodingException
    {

        // totalSizeInBits() is a bit count, the BitWriter expects a capacity in bytes
        this.data = new BitWriter((codecData.totalSizeInBits() + 7) / 8);
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
             * case FULL_REVISION_COMPRESSED: encodeFullRevisionCompressed(part); break;
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
     * @throws EncodingException
     *             if the encoding failed
     */
    private void encodeCodecData(final RevisionCodecData codecData) throws EncodingException
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
     * @throws EncodingException
     *             if the encoding failed
     */
    private void encodeCut(final DiffPart part) throws EncodingException
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
        data.writeValue(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));

        data.writeFillBits();

    }

    /**
     * Encodes a Delete operation.
     *
     * @param part
     *            Reference to the Delete operation
     * @throws EncodingException
     *             if the encoding failed
     */
    private void encodeDelete(final DiffPart part) throws EncodingException
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

    @Override
    public String encodeDiff(final RevisionCodecData codecData, final Diff diff)
        throws UnsupportedEncodingException, EncodingException
    {

        String sEncoding;
        byte[] bData = encode(codecData, diff);
        Base64.Encoder encoder = Base64.getEncoder();
        if (MODE_ZIP_COMPRESSION) {

            byte[] output = deflate(bData, false);

            if (bData.length + 1 < output.length) {
                sEncoding = encoder.encodeToString(bData);
            }
            else {
                sEncoding = "_" + encoder.encodeToString(output);
            }
        }
        else {
            sEncoding = encoder.encodeToString(bData);
        }

        return sEncoding;
    }

    /**
     * Encodes a FullRevision operation.
     *
     * @param part
     *            Reference to the FullRevision operation
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
     * @throws EncodingException
     *             if the encoding failed
     */
    private void encodePaste(final DiffPart part) throws EncodingException
    {

        // C
        data.writeBit(1);
        data.writeBit(1);
        data.writeBit(0);

        // S
        data.writeValue(codecData.getBlocksizeS(), part.getStart());

        // B
        data.writeValue(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));

        data.writeFillBits();
    }

    /**
     * Encodes a Replace operation.
     *
     * @param part
     *            Reference to the replace operation
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
