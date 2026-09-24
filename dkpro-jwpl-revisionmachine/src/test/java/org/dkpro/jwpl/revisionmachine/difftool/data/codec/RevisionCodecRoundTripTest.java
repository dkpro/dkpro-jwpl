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

import static org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecTestSupport.ENCODING;
import static org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecTestSupport.assertDecodes;
import static org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecTestSupport.codecData;
import static org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecTestSupport.configure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class RevisionCodecRoundTripTest
{

    private static final String PREVIOUS = "abcdefghijklmnopqrstuvwxyz0123456789";

    @BeforeEach
    @AfterEach
    public void setUpConfiguration()
    {
        configure(true);
    }

    @ParameterizedTest
    @CsvSource({ "'', true", "'', false", "Some text, true", "Some text, false" })
    public void testRoundTrip(String text, boolean zipCompression) throws Exception
    {
        configure(zipCompression);
        assertRoundTrip(text);
    }

    static Stream<Arguments> emptyTextParts()
    {
        // The encoder pads each text block to the next byte boundary. The empty part is followed
        // by an insert, so the decoder has to skip the fill bits before it reads the next part.
        // The start and the length of the following text vary the block sizes, so that the
        // empty part never ends on a byte boundary.
        return Stream.of(
                Arguments.of(part(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, ""), "x"),
                Arguments.of(part(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, ""), "xy"),
                Arguments.of(part(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, ""), "wxyz"),
                Arguments.of(part(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, ""), "stuvwxyz"),
                Arguments.of(part(DiffAction.INSERT, 1, 0, ""), "xy"),
                Arguments.of(part(DiffAction.INSERT, 2, 0, ""), "xy"),
                Arguments.of(part(DiffAction.INSERT, 9, 0, ""), "xy"),
                Arguments.of(part(DiffAction.INSERT, 20, 0, ""), "xy"),
                Arguments.of(part(DiffAction.REPLACE, 1, 1, ""), "xy"),
                Arguments.of(part(DiffAction.REPLACE, 4, 1, ""), "xy"),
                Arguments.of(part(DiffAction.REPLACE, 9, 1, ""), "xy"),
                Arguments.of(part(DiffAction.REPLACE, 20, 1, ""), "xy"));
    }

    @ParameterizedTest
    @MethodSource("emptyTextParts")
    public void testEmptyTextIsFollowedByFillBits(DiffPart emptyPart, String followingText)
        throws Exception
    {
        Diff diff = new Diff();
        diff.add(emptyPart);
        diff.add(part(DiffAction.INSERT, 0, 0, followingText));

        RevisionCodecData codecData = codecData(diff);
        codecData.totalSizeInBits();
        int bits = 3 + codecData.getBlocksizeL();
        if (emptyPart.getAction() != DiffAction.FULL_REVISION_UNCOMPRESSED) {
            bits += codecData.getBlocksizeS();
        }
        if (emptyPart.getAction() == DiffAction.REPLACE) {
            bits += codecData.getBlocksizeE();
        }
        assertNotEquals(0, bits % 8, "the empty part has to end within a byte");

        assertDecodes(diff, codecData, PREVIOUS, diff.buildRevision(PREVIOUS));
    }

    @Test
    public void testEmptyTextFollowedByFurtherParts() throws Exception
    {
        // an empty text block has to be followed by the fill bits like a non-empty one
        Diff diff = new Diff();
        diff.add(part(DiffAction.REPLACE, 2, 3, ""));
        diff.add(part(DiffAction.INSERT, 4, 0, ""));
        diff.add(part(DiffAction.DELETE, 0, 1, null));
        diff.add(part(DiffAction.INSERT, 1, 0, "xy"));

        assertDecodes(diff, codecData(diff), PREVIOUS, diff.buildRevision(PREVIOUS));
    }

    @Test
    public void testCompressedRevisionLargerThanBuffer() throws Exception
    {
        String text = repeat(50_000);
        Diff diff = diff(text);
        byte[] binary = new RevisionEncoder().binaryDiff(codecData(diff), diff);
        assertEquals(-128, binary[0]);
        assertTrue(new RevisionEncoder().encodeDiff(codecData(diff), diff).startsWith("_"));

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
        Diff diff = diff(text);
        byte[] binary = new RevisionEncoder().binaryDiff(codecData(diff), diff);
        assertEquals(-128, binary[0]);

        byte[] truncated = Arrays.copyOf(binary, binary.length / 2);
        RevisionDecoder decoder = new RevisionDecoder(ENCODING);
        assertThrows(RuntimeException.class, () -> decoder.setInput(truncated));
    }

    private static void assertRoundTrip(String text) throws Exception
    {
        Diff diff = diff(text);
        assertDecodes(diff, codecData(diff), null, text);
    }

    private static String repeat(int length)
    {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + i % 26));
        }
        return builder.toString();
    }

    private static DiffPart part(DiffAction action, int start, int length, String text)
    {
        DiffPart part = new DiffPart(action);
        part.setStart(start);
        part.setLength(length);
        part.setText(text);
        return part;
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
