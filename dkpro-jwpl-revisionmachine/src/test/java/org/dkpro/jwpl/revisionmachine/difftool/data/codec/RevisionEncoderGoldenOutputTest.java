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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.Deflater;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation.DiffCalculator;
import org.dkpro.jwpl.revisionmachine.difftool.data.SurrogateModes;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the {@link RevisionEncoder} produces exactly the bytes of the original encoder
 * implementation (see issue #582) and that the output still decodes to the original revisions.
 * <p>
 * The corpus consists of the diffs the {@link DiffCalculator} computes for synthetic revision
 * histories, which cover every diff action, multibyte UTF-8 text, HTML entities and large full
 * revisions, plus a few hand-built diffs with surrogate pairs.
 */
public class RevisionEncoderGoldenOutputTest
{

    private static final String ENCODING = StandardCharsets.UTF_8.toString();

    private static final String[] WORDS = { "Wikipedia", "revision", "the", "and", "of",
            "Käse", "Straße", "日本語", "История",
            "[[Link|label]]", "{{Template}}", "&amp;", "&lt;ref&gt;", "&nbsp;", "été",
            "==Heading==", "\n", "\n\n", "'''bold'''", "€" };

    @AfterAll
    public static void resetConfiguration()
    {
        configure(true);
    }

    @Test
    public void testCompressedOutputIsIdenticalToOriginalEncoder() throws Exception
    {
        assertCorpus(true);
    }

    @Test
    public void testUncompressedOutputIsIdenticalToOriginalEncoder() throws Exception
    {
        assertCorpus(false);
    }

    @Test
    public void testDiffsWithSurrogatePairs() throws Exception
    {
        configure(true);
        String previous = "a😀b " + repeat("𝄞 x", 400);

        Diff full = new Diff();
        RevisionCodecData fullCodec = new RevisionCodecData();
        full.add(part(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, previous, fullCodec));
        full.setCodecData(fullCodec);

        Diff edit = new Diff();
        RevisionCodecData editCodec = new RevisionCodecData();
        edit.add(part(DiffAction.INSERT, 1, 0, "😁ä", editCodec));
        edit.add(part(DiffAction.REPLACE, 5, 3, "😂", editCodec));
        edit.add(part(DiffAction.DELETE, 20, 7, null, editCodec));
        edit.setCodecData(editCodec);

        String current = assertEncoding(full, null);
        assertEquals(previous, current);
        assertEquals(edit.buildRevision(previous), assertEncoding(edit, previous));
    }

    private static void assertCorpus(boolean compression) throws Exception
    {
        configure(compression);

        Set<DiffAction> actions = EnumSet.noneOf(DiffAction.class);
        int diffCount = 0;

        Random random = new Random(582);
        int[] initialLengths = { 1, 40, 2_000, 30_000, 250_000 };
        for (int article = 0; article < initialLengths.length; article++) {
            List<String> history = history(random, initialLengths[article], 45);
            List<Diff> diffs = calculateDiffs(article + 1, history);
            assertEquals(history.size(), diffs.size());

            String previous = null;
            for (int i = 0; i < diffs.size(); i++) {
                Diff diff = diffs.get(i);
                for (Iterator<DiffPart> parts = diff.iterator(); parts.hasNext();) {
                    actions.add(parts.next().getAction());
                }

                String current = assertEncoding(diff, previous);
                assertEquals(StringEscapeUtils.unescapeHtml4(history.get(i)), current);
                previous = current;
                diffCount++;
            }
        }

        assertTrue(diffCount > 200);
        assertEquals(EnumSet.of(DiffAction.FULL_REVISION_UNCOMPRESSED, DiffAction.INSERT,
                DiffAction.DELETE, DiffAction.REPLACE, DiffAction.CUT, DiffAction.PASTE), actions);
    }

    /**
     * Encodes the diff with the current and the original encoder, compares the bytes and decodes
     * the result.
     *
     * @return the revision rebuilt from the decoded diff
     */
    private static String assertEncoding(Diff diff, String previous) throws Exception
    {
        RevisionCodecData codecData = diff.getCodecData();
        RevisionEncoder encoder = new RevisionEncoder();
        OriginalEncoder original = new OriginalEncoder(isCompressionEnabled());

        byte[] binary = encoder.binaryDiff(codecData, diff);
        String base64 = encoder.encodeDiff(codecData, diff);
        assertArrayEquals(original.binaryDiff(codecData, diff), binary);
        assertEquals(original.encodeDiff(codecData, diff), base64);

        String expected = diff.buildRevision(previous);

        RevisionDecoder decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(binary);
        assertEquals(expected, decoder.decode().buildRevision(previous));

        decoder = new RevisionDecoder(ENCODING);
        decoder.setInput(base64);
        assertEquals(expected, decoder.decode().buildRevision(previous));

        return expected;
    }

    private static List<Diff> calculateDiffs(int articleId, List<String> history)
        throws Exception
    {
        List<Diff> diffs = new ArrayList<>();
        TaskTransmitterInterface transmitter = new TaskTransmitterInterface()
        {
            @Override
            public void transmitDiff(Task<Diff> result)
            {
                diffs.addAll(result.getContainer());
            }

            @Override
            public void transmitPartialDiff(Task<Diff> result)
            {
                diffs.addAll(result.getContainer());
            }

            @Override
            public void close()
            {
                // nothing to close
            }
        };

        ArticleInformation header = new ArticleInformation();
        header.setArticleId(articleId);
        header.setArticleName("Article " + articleId);

        Task<Revision> task = new Task<>(header, 1);
        for (int i = 0; i < history.size(); i++) {
            Revision revision = new Revision(i + 1);
            revision.setArticleID(articleId);
            revision.setRevisionID(articleId * 1000 + i);
            revision.setTimeStamp(new Timestamp(1_000_000_000_000L + i * 60_000L));
            revision.setRevisionText(history.get(i));
            task.add(revision);
        }

        new DiffCalculator(transmitter).process(task);
        return diffs;
    }

    /**
     * Creates a revision history whose revisions differ by random inserts, deletes, replacements
     * and block moves. Consecutive revisions are always different, otherwise the diff calculator
     * would skip them.
     */
    private static List<String> history(Random random, int initialLength, int revisions)
    {
        List<String> history = new ArrayList<>();
        StringBuilder text = new StringBuilder(words(random, initialLength));
        history.add(text.toString());

        while (history.size() < revisions) {
            int edits = 1 + random.nextInt(4);
            for (int e = 0; e < edits; e++) {
                int length = text.length();
                int position = length == 0 ? 0 : random.nextInt(length);
                int span = length == 0 ? 0 : Math.min(length - position, 1 + random.nextInt(60));
                switch (random.nextInt(4)) {
                case 0:
                    text.insert(position, words(random, 1 + random.nextInt(80)));
                    break;
                case 1:
                    text.delete(position, position + span);
                    break;
                case 2:
                    text.replace(position, position + span, words(random, 1 + random.nextInt(40)));
                    break;
                default:
                    // move a block that is long enough to be detected as cut and paste
                    int block = Math.min(length - position, 20 + random.nextInt(200));
                    if (block >= 20 && length > block) {
                        String moved = text.substring(position, position + block);
                        text.delete(position, position + block);
                        text.insert(random.nextInt(text.length() + 1), moved);
                    }
                    break;
                }
            }

            // RevisionDecoder cannot read an empty full revision, so keep the texts non-empty
            String next = text.toString();
            if (!next.isEmpty() && !StringEscapeUtils.unescapeHtml4(next).equals(
                    StringEscapeUtils.unescapeHtml4(history.get(history.size() - 1)))) {
                history.add(next);
            }
        }
        assertFalse(history.isEmpty());
        return history;
    }

    private static String words(Random random, int length)
    {
        StringBuilder builder = new StringBuilder(length + 20);
        while (builder.length() < length) {
            builder.append(WORDS[random.nextInt(WORDS.length)]).append(' ');
        }
        return builder.toString();
    }

    private static String repeat(String text, int count)
    {
        StringBuilder builder = new StringBuilder(text.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }

    private static DiffPart part(DiffAction action, int start, int length, String text,
            RevisionCodecData codecData)
    {
        DiffPart part = new DiffPart(action);
        part.setStart(start);
        part.setLength(length);
        part.setText(text);

        if (action != DiffAction.FULL_REVISION_UNCOMPRESSED) {
            codecData.checkBlocksizeS(start);
        }
        if (action == DiffAction.REPLACE || action == DiffAction.DELETE) {
            codecData.checkBlocksizeE(length);
        }
        if (text != null) {
            codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);
        }
        return part;
    }

    private static void configure(boolean compression)
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED, compression);
        settings.setConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION, 10);
        settings.setConfigParameter(ConfigurationKeys.MODE_SURROGATES, SurrogateModes.REPLACE);
        settings.setConfigParameter(ConfigurationKeys.VERIFICATION_DIFF, true);
        new ConfigurationManager(settings);
    }

    private static boolean isCompressionEnabled() throws Exception
    {
        return (Boolean) ConfigurationManager.getInstance()
                .getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
    }

    /**
     * Copy of the encoder as released in 2.2.0: the output buffer is sized with the bit count, the
     * text is written byte by byte and the compressed data is read in chunks of 1000 bytes.
     */
    private static final class OriginalEncoder
    {

        private final boolean compression;

        private RevisionCodecData codecData;

        private ByteArrayOutputStream stream;

        private int buffer;

        private int bufferLength;

        OriginalEncoder(boolean compression)
        {
            this.compression = compression;
        }

        byte[] binaryDiff(RevisionCodecData codecData, Diff diff) throws Exception
        {
            byte[] bData = encode(codecData, diff);
            if (compression) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                output.write(-128);
                output.write(deflate(bData));
                byte[] compressed = output.toByteArray();
                return bData.length + 1 < compressed.length - 1 ? bData : compressed;
            }
            return bData;
        }

        String encodeDiff(RevisionCodecData codecData, Diff diff) throws Exception
        {
            byte[] bData = encode(codecData, diff);
            Base64.Encoder encoder = Base64.getEncoder();
            if (compression) {
                byte[] compressed = deflate(bData);
                if (bData.length + 1 < compressed.length) {
                    return encoder.encodeToString(bData);
                }
                return "_" + encoder.encodeToString(compressed);
            }
            return encoder.encodeToString(bData);
        }

        private static byte[] deflate(byte[] input)
        {
            Deflater compresser = new Deflater();
            try {
                compresser.setInput(input);
                compresser.finish();

                byte[] output = new byte[1000];
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int cLength;
                do {
                    cLength = compresser.deflate(output);
                    stream.write(output, 0, cLength);
                }
                while (cLength == 1000);
                return stream.toByteArray();
            }
            finally {
                compresser.end();
            }
        }

        private byte[] encode(RevisionCodecData codecData, Diff diff) throws Exception
        {
            this.codecData = codecData;
            this.stream = new ByteArrayOutputStream(codecData.totalSizeInBits());
            this.buffer = 0;
            this.bufferLength = 0;

            writeBits(3, 0);
            writeBits(5, codecData.getBlocksizeS());
            writeBits(5, codecData.getBlocksizeE());
            writeBits(5, codecData.getBlocksizeB());
            writeBits(5, codecData.getBlocksizeL());
            fill();

            for (Iterator<DiffPart> parts = diff.iterator(); parts.hasNext();) {
                DiffPart part = parts.next();
                switch (part.getAction()) {
                case FULL_REVISION_UNCOMPRESSED:
                    writeBits(3, 1);
                    writeText(part.getText());
                    break;
                case INSERT:
                    writeBits(3, 2);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeText(part.getText());
                    break;
                case DELETE:
                    writeBits(3, 3);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeE(), part.getLength());
                    fill();
                    break;
                case REPLACE:
                    writeBits(3, 4);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeE(), part.getLength());
                    writeText(part.getText());
                    break;
                case CUT:
                    writeBits(3, 5);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeE(), part.getLength());
                    writeBits(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));
                    fill();
                    break;
                case PASTE:
                    writeBits(3, 6);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));
                    fill();
                    break;
                default:
                    throw new IllegalStateException(part.getAction().toString());
                }
            }
            return stream.toByteArray();
        }

        private void writeText(String text) throws Exception
        {
            byte[] bText = text.getBytes(ENCODING);
            writeBits(codecData.getBlocksizeL(), bText.length);
            fill();
            for (byte b : bText) {
                stream.write(0xFF & b);
            }
        }

        private void writeBits(int length, int value)
        {
            for (int i = length - 1; i >= 0; i--) {
                buffer |= ((value >> i) & 1) << (7 - bufferLength);
                bufferLength++;
                if (bufferLength == 8) {
                    stream.write(buffer);
                    buffer = 0;
                    bufferLength = 0;
                }
            }
        }

        private void fill()
        {
            while (bufferLength != 0) {
                writeBits(1, 0);
            }
            buffer = 0;
        }
    }
}
