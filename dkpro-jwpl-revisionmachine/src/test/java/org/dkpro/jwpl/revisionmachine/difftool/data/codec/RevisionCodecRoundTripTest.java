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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RevisionCodecRoundTripTest
{

    private static final String ENCODING = StandardCharsets.UTF_8.toString();

    @BeforeAll
    public static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED, true);
        new ConfigurationManager(settings);
    }

    @Test
    public void testSmallRevision() throws Exception
    {
        assertRoundTrip("Some text");
    }

    @Test
    public void testCompressedRevisionLargerThanBuffer() throws Exception
    {
        String text = repeat(50_000);
        byte[] binary = new RevisionEncoder().binaryDiff(codecData(text), diff(text));
        assertEquals(-128, binary[0]);
        assertTrue(new RevisionEncoder().encodeDiff(codecData(text), diff(text)).startsWith("_"));

        assertRoundTrip(text);
    }

    @Test
    public void testInflatedSizeAroundMultiplesOfThousand() throws Exception
    {
        // covers inflated sizes of exactly 1000 and 2000 bytes, the edge case of the former loop
        for (int length = 980; length <= 2020; length++) {
            assertRoundTrip(repeat(length));
        }
    }

    @Test
    public void testIncompressibleRevision() throws Exception
    {
        Random random = new Random(42);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            builder.append((char) ('!' + random.nextInt(90)));
        }
        assertRoundTrip(builder.toString());
    }

    @Test
    public void testTruncatedCompressedRevisionFails() throws Exception
    {
        String text = repeat(50_000);
        byte[] binary = new RevisionEncoder().binaryDiff(codecData(text), diff(text));
        assertEquals(-128, binary[0]);

        byte[] truncated = Arrays.copyOf(binary, binary.length / 2);
        RevisionDecoder decoder = new RevisionDecoder(ENCODING);
        assertThrows(RuntimeException.class, () -> decoder.setInput(truncated));
    }

    private static void assertRoundTrip(String text) throws Exception
    {
        RevisionEncoder encoder = new RevisionEncoder();
        byte[] binary = encoder.binaryDiff(codecData(text), diff(text));
        String base64 = encoder.encodeDiff(codecData(text), diff(text));

        RevisionDecoder decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(binary);
        assertEquals(text, decoder.decode().buildRevision((String) null));

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(new ByteArrayInputStream(binary), true);
        assertEquals(text, decoder.decode().buildRevision((String) null));

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(base64);
        assertEquals(text, decoder.decode().buildRevision((String) null));

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(new ByteArrayInputStream(base64.getBytes(StandardCharsets.US_ASCII)),
                false);
        assertEquals(text, decoder.decode().buildRevision((String) null));
    }

    private static String repeat(int length)
    {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + i % 26));
        }
        return builder.toString();
    }

    private static RevisionCodecData codecData(String text)
    {
        RevisionCodecData codecData = new RevisionCodecData();
        codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);
        return codecData;
    }

    private static Diff diff(String text)
    {
        DiffPart part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
        part.setText(text);

        Diff diff = new Diff();
        diff.add(part);
        return diff;
    }
}
