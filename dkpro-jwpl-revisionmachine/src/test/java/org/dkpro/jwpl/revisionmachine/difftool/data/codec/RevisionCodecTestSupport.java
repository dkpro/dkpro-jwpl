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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * Shared setup and assertions for tests that encode and decode revisions.
 */
public final class RevisionCodecTestSupport
{

    /**
     * Character encoding of the revision texts
     */
    public static final String ENCODING = StandardCharsets.UTF_8.toString();

    private RevisionCodecTestSupport()
    {
    }

    /**
     * Installs the default configuration, with zip compression enabled or disabled.
     *
     * @param zipCompression
     *            whether the encoder compresses the encoded revisions
     */
    public static void configure(boolean zipCompression)
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED,
                zipCompression);
        new ConfigurationManager(settings);
    }

    /**
     * Derives the codec data from the parts of a diff, like the DiffCalculator does while it
     * creates them.
     *
     * @param diff
     *            the diff
     * @return the codec data for the diff
     * @throws Exception
     *             if a text cannot be encoded
     */
    public static RevisionCodecData codecData(Diff diff) throws Exception
    {
        RevisionCodecData codecData = new RevisionCodecData();
        for (int i = 0; i < diff.size(); i++) {
            DiffPart part = diff.get(i);
            switch (part.getAction()) {
            case FULL_REVISION_UNCOMPRESSED:
                codecData.checkBlocksizeL(part.getText().getBytes(ENCODING).length);
                break;
            case INSERT:
                codecData.checkBlocksizeS(part.getStart());
                codecData.checkBlocksizeL(part.getText().getBytes(ENCODING).length);
                break;
            case DELETE:
                codecData.checkBlocksizeS(part.getStart());
                codecData.checkBlocksizeE(part.getLength());
                break;
            case REPLACE:
                codecData.checkBlocksizeS(part.getStart());
                codecData.checkBlocksizeE(part.getLength());
                codecData.checkBlocksizeL(part.getText().getBytes(ENCODING).length);
                break;
            case CUT:
                codecData.checkBlocksizeS(part.getStart());
                codecData.checkBlocksizeE(part.getLength());
                codecData.checkBlocksizeB(Integer.parseInt(part.getText()));
                break;
            case PASTE:
                codecData.checkBlocksizeS(part.getStart());
                codecData.checkBlocksizeB(Integer.parseInt(part.getText()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported action: " + part.getAction());
            }
        }
        return codecData;
    }

    /**
     * Encodes a diff as binary data and as Base64 string, decodes both from arrays and streams and
     * asserts that each decoded diff builds the expected revision.
     *
     * @param diff
     *            the diff to encode
     * @param codecData
     *            the codec data of the diff
     * @param previous
     *            the revision the diff is applied to, or {@code null}
     * @param expected
     *            the expected revision
     * @throws Exception
     *             if the encoding or decoding fails
     */
    public static void assertDecodes(Diff diff, RevisionCodecData codecData, String previous,
            String expected)
        throws Exception
    {
        RevisionEncoder encoder = new RevisionEncoder();
        byte[] binary = encoder.binaryDiff(codecData, diff);
        String base64 = encoder.encodeDiff(codecData, diff);

        RevisionDecoder decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(binary);
        assertEquals(expected, decoder.decode().buildRevision(previous), "binary");

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(new ByteArrayInputStream(binary), true);
        assertEquals(expected, decoder.decode().buildRevision(previous), "binary stream");

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(base64);
        assertEquals(expected, decoder.decode().buildRevision(previous), "Base64");

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(new ByteArrayInputStream(base64.getBytes(StandardCharsets.US_ASCII)),
                false);
        assertEquals(expected, decoder.decode().buildRevision(previous), "Base64 stream");
    }
}
