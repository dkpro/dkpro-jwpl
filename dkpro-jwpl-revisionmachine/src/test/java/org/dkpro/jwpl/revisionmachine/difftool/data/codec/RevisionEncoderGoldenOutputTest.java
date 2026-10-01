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
 * Verifies that the {@link RevisionEncoder} produces exactly the same bytes as the encoder released
 * in version 2.2.0, before the buffer and bulk-write changes of issue #582, and that its output
 * still decodes to the original revision texts.
 * <p>
 * <b>Where the golden output comes from:</b> the test does not read stored files. The expected
 * ("golden") bytes are computed at test time by {@link ReferenceEncoder}, a self-contained copy of
 * the 2.2.0 encoder at the bottom of this class. Every diff is encoded by the reference encoder
 * (expected) and by the current {@link RevisionEncoder} (actual), and both results must be equal.
 * There is nothing to regenerate. If the encoding format is ever changed on purpose, this test
 * fails by design, and {@link ReferenceEncoder} has to be updated to the new format.
 * <p>
 * <b>Which diffs are checked:</b>
 * <ul>
 * <li>The diffs the {@link DiffCalculator} computes for synthetic revision histories. The histories
 * are generated with a fixed random seed, so every run checks the same diffs. They cover every
 * {@link DiffAction}, multibyte UTF-8 text, HTML entities and full revisions of up to 250 KB.</li>
 * <li>A few hand-built diffs whose texts contain surrogate pairs (characters outside the Basic
 * Multilingual Plane, such as emojis).</li>
 * </ul>
 */
public class RevisionEncoderGoldenOutputTest
{

    /** Character encoding of the revision texts, as expected by encoder and decoder. */
    private static final String TEXT_ENCODING = StandardCharsets.UTF_8.toString();

    /** Fixed seed (the issue number), so the generated revision histories are reproducible. */
    private static final long RANDOM_SEED = 582;

    /**
     * Length (in characters) of the first revision of each synthetic article. One article is
     * generated per entry, from a single word up to a large article of 250 KB.
     */
    private static final int[] FIRST_REVISION_LENGTHS = { 1, 40, 2_000, 30_000, 250_000 };

    /** Number of revisions generated for each synthetic article. */
    private static final int REVISIONS_PER_ARTICLE = 45;

    /** Every n-th revision is stored as a full revision instead of a diff. */
    private static final int FULL_REVISION_INTERVAL = 10;

    /** Lower bound for the number of diffs, so a broken generator cannot pass with few diffs. */
    private static final int MIN_DIFF_COUNT = 200;

    /** All diff actions the encoder supports. The synthetic corpus must contain each of them. */
    private static final Set<DiffAction> ALL_DIFF_ACTIONS = EnumSet.of(
            DiffAction.FULL_REVISION_UNCOMPRESSED, DiffAction.INSERT, DiffAction.DELETE,
            DiffAction.REPLACE, DiffAction.CUT, DiffAction.PASTE);

    /**
     * Words the synthetic revision texts are built from: ASCII, multibyte UTF-8 (2 and 3 bytes per
     * character), wiki markup, HTML entities and line breaks.
     */
    private static final String[] VOCABULARY = { "Wikipedia", "revision", "the", "and", "of",
            "Käse", "Straße", "日本語", "История",
            "[[Link|label]]", "{{Template}}", "&amp;", "&lt;ref&gt;", "&nbsp;", "été",
            "==Heading==", "\n", "\n\n", "'''bold'''", "€" };

    /** Kinds of random edits applied between two revisions of a synthetic article. */
    private static final int EDIT_INSERT = 0;
    private static final int EDIT_DELETE = 1;
    private static final int EDIT_REPLACE = 2;
    // Any other value moves a block of text (see applyRandomEdit)
    private static final int EDIT_KIND_COUNT = 4;

    /** Minimum length of a moved block, so the diff calculator detects it as cut and paste. */
    private static final int MIN_MOVED_BLOCK_LENGTH = 20;

    /**
     * The diff tool configuration is a global singleton. Leave it with zip compression enabled,
     * which is the default, for the tests that run after this class.
     */
    @AfterAll
    public static void resetConfiguration()
    {
        configureDiffTool(true);
    }

    @Test
    public void testCompressedOutputIsIdenticalToOriginalEncoder() throws Exception
    {
        // given: both encoders compress their output with zip (deflate)
        configureDiffTool(true);

        // when/then: every diff of the synthetic corpus is encoded identically by both encoders
        // and decodes back to its revision text
        assertSyntheticCorpusIsEncodedLikeReferenceEncoder();
    }

    @Test
    public void testUncompressedOutputIsIdenticalToOriginalEncoder() throws Exception
    {
        // given: both encoders write their output without compression
        configureDiffTool(false);

        // when/then: every diff of the synthetic corpus is encoded identically by both encoders
        // and decodes back to its revision text
        assertSyntheticCorpusIsEncodedLikeReferenceEncoder();
    }

    @Test
    public void testDiffsWithSurrogatePairs() throws Exception
    {
        // given: a full revision whose text contains surrogate pairs ...
        configureDiffTool(true);
        final String baseRevisionText = "a😀b " + repeat("𝄞 x", 400);
        final Diff fullRevisionDiff = createDiff(
                createDiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED, 0, 0, baseRevisionText));

        // ... and a diff that edits this text with surrogate pairs and multibyte characters
        final Diff editDiff = createDiff(
                createDiffPart(DiffAction.INSERT, 1, 0, "😁ä"),
                createDiffPart(DiffAction.REPLACE, 5, 3, "😂"),
                createDiffPart(DiffAction.DELETE, 20, 7, null));

        // when: the diffs are applied in memory, without encoding them
        final String noPreviousRevisionText = null;
        final String actualBaseRevisionText = fullRevisionDiff.buildRevision(noPreviousRevisionText);
        final String expectedEditedRevisionText = editDiff.buildRevision(baseRevisionText);

        // then: the full revision diff reproduces its text
        assertEquals(baseRevisionText, actualBaseRevisionText,
                "The full revision diff must reproduce the base revision text");

        // and: both diffs are encoded like the reference encoder and decode to the same texts
        assertEncodedLikeReferenceEncoderAndDecodable(fullRevisionDiff, noPreviousRevisionText,
                baseRevisionText);
        assertEncodedLikeReferenceEncoderAndDecodable(editDiff, baseRevisionText,
                expectedEditedRevisionText);
    }

    /**
     * Generates the synthetic revision histories, lets the {@link DiffCalculator} compute their
     * diffs and checks every diff with
     * {@link #assertEncodedLikeReferenceEncoderAndDecodable(Diff, String, String)}. Finally checks
     * that the corpus was large enough and contained every diff action.
     * <p>
     * The diff tool must have been configured with {@link #configureDiffTool(boolean)} before.
     */
    private static void assertSyntheticCorpusIsEncodedLikeReferenceEncoder() throws Exception
    {
        final Random random = new Random(RANDOM_SEED);
        final Set<DiffAction> actualDiffActions = EnumSet.noneOf(DiffAction.class);
        int actualDiffCount = 0;

        for (int articleIndex = 0; articleIndex < FIRST_REVISION_LENGTHS.length; articleIndex++) {
            // given: the revision history of a synthetic article
            final int articleId = articleIndex + 1;
            final List<String> revisionTexts = createRevisionHistory(random,
                    FIRST_REVISION_LENGTHS[articleIndex], REVISIONS_PER_ARTICLE);

            // when: the diff calculator computes the diffs between consecutive revisions
            final List<Diff> diffs = calculateDiffs(articleId, revisionTexts);

            // then: there is exactly one diff (or full revision) per revision
            assertEquals(revisionTexts.size(), diffs.size(),
                    "Number of diffs for article " + articleId);

            // and: every diff rebuilds its revision, is encoded like the reference encoder and
            // decodes again. Each diff is applied to the revision text before it.
            String previousRevisionText = null;
            for (int revisionIndex = 0; revisionIndex < diffs.size(); revisionIndex++) {
                final Diff diff = diffs.get(revisionIndex);
                actualDiffActions.addAll(collectDiffActions(diff));

                // The diff calculator works on the unescaped revision text
                final String expectedRevisionText = StringEscapeUtils
                        .unescapeHtml4(revisionTexts.get(revisionIndex));
                final String actualRevisionText = diff.buildRevision(previousRevisionText);
                assertEquals(expectedRevisionText, actualRevisionText, "Revision text rebuilt "
                        + "from diff " + revisionIndex + " of article " + articleId);

                assertEncodedLikeReferenceEncoderAndDecodable(diff, previousRevisionText,
                        expectedRevisionText);

                previousRevisionText = actualRevisionText;
                actualDiffCount++;
            }
        }

        // then: the corpus is large enough and covers every diff action
        assertTrue(actualDiffCount > MIN_DIFF_COUNT,
                "Expected more than " + MIN_DIFF_COUNT + " diffs, but got " + actualDiffCount);
        assertEquals(ALL_DIFF_ACTIONS, actualDiffActions,
                "The synthetic corpus must contain every diff action");
    }

    /**
     * Checks one diff in three steps:
     * <ol>
     * <li>The current {@link RevisionEncoder} produces the same binary output
     * ({@code binaryDiff}) as the {@link ReferenceEncoder}.</li>
     * <li>The current {@link RevisionEncoder} produces the same Base64 output ({@code encodeDiff})
     * as the {@link ReferenceEncoder}.</li>
     * <li>Both outputs decode with the {@link RevisionDecoder} to a diff that, applied to
     * {@code previousRevisionText}, yields {@code expectedRevisionText}.</li>
     * </ol>
     *
     * @param diff
     *            the diff to encode
     * @param previousRevisionText
     *            the text the diff is applied to, or {@code null} for a full revision without
     *            predecessor
     * @param expectedRevisionText
     *            the revision text the decoded diff has to produce
     */
    private static void assertEncodedLikeReferenceEncoderAndDecodable(final Diff diff,
            final String previousRevisionText, final String expectedRevisionText)
        throws Exception
    {
        // when: the reference encoder and the current encoder encode the same diff
        final EncodedDiff expectedEncoding = encodeWithReferenceEncoder(diff);
        final EncodedDiff actualEncoding = encodeWithCurrentEncoder(diff);

        // then: both encoders produce identical output
        assertArrayEquals(expectedEncoding.binary(), actualEncoding.binary(),
                "Binary output (binaryDiff) differs from the reference encoder");
        assertEquals(expectedEncoding.base64(), actualEncoding.base64(),
                "Base64 output (encodeDiff) differs from the reference encoder");

        // and: both forms of the current output decode back to the expected revision text
        final String revisionTextDecodedFromBinary = decodeAndApply(
                createDecoder(actualEncoding.binary()), previousRevisionText);
        assertEquals(expectedRevisionText, revisionTextDecodedFromBinary,
                "Revision text decoded from the binary output");

        final String revisionTextDecodedFromBase64 = decodeAndApply(
                createDecoder(actualEncoding.base64()), previousRevisionText);
        assertEquals(expectedRevisionText, revisionTextDecodedFromBase64,
                "Revision text decoded from the Base64 output");
    }

    /**
     * Output of an encoder for one diff, in both forms the encoder offers.
     *
     * @param binary
     *            result of {@code binaryDiff}
     * @param base64
     *            result of {@code encodeDiff}
     */
    private record EncodedDiff(byte[] binary, String base64)
    {
    }

    /**
     * @return the output of the current {@link RevisionEncoder} for the given diff (actual)
     */
    private static EncodedDiff encodeWithCurrentEncoder(final Diff diff) throws Exception
    {
        final RevisionCodecData codecData = diff.getCodecData();
        final RevisionEncoder encoder = new RevisionEncoder();
        final byte[] binary = encoder.binaryDiff(codecData, diff);
        final String base64 = encoder.encodeDiff(codecData, diff);
        return new EncodedDiff(binary, base64);
    }

    /**
     * @return the output of the 2.2.0 {@link ReferenceEncoder} for the given diff (expected)
     */
    private static EncodedDiff encodeWithReferenceEncoder(final Diff diff) throws Exception
    {
        final RevisionCodecData codecData = diff.getCodecData();
        final ReferenceEncoder encoder = new ReferenceEncoder(isZipCompressionEnabled());
        final byte[] binary = encoder.binaryDiff(codecData, diff);
        final String base64 = encoder.encodeDiff(codecData, diff);
        return new EncodedDiff(binary, base64);
    }

    /** @return a decoder that reads the given binary encoder output */
    private static RevisionDecoder createDecoder(final byte[] binaryInput)
    {
        final RevisionDecoder decoder = new RevisionDecoder(TEXT_ENCODING);
        decoder.setInput(binaryInput);
        return decoder;
    }

    /** @return a decoder that reads the given Base64 encoder output */
    private static RevisionDecoder createDecoder(final String base64Input) throws Exception
    {
        final RevisionDecoder decoder = new RevisionDecoder(TEXT_ENCODING);
        decoder.setInput(base64Input);
        return decoder;
    }

    /**
     * Decodes a diff and applies it to the previous revision text.
     *
     * @return the revision text the decoded diff produces
     */
    private static String decodeAndApply(final RevisionDecoder decoder,
            final String previousRevisionText)
        throws Exception
    {
        final Diff decodedDiff = decoder.decode();
        return decodedDiff.buildRevision(previousRevisionText);
    }

    /** @return the actions of all parts of the given diff */
    private static Set<DiffAction> collectDiffActions(final Diff diff)
    {
        final Set<DiffAction> actions = EnumSet.noneOf(DiffAction.class);
        for (Iterator<DiffPart> parts = diff.iterator(); parts.hasNext();) {
            actions.add(parts.next().getAction());
        }
        return actions;
    }

    /**
     * Runs the {@link DiffCalculator} on an article with the given revision texts.
     *
     * @param articleId
     *            ID of the synthetic article
     * @param revisionTexts
     *            texts of the revisions, oldest first
     * @return the diffs the calculator emits, one per revision and in the same order. With the
     *         configuration of {@link #configureDiffTool(boolean)}, every
     *         {@value #FULL_REVISION_INTERVAL}th one is a full revision.
     */
    private static List<Diff> calculateDiffs(final int articleId, final List<String> revisionTexts)
        throws Exception
    {
        final ArticleInformation article = new ArticleInformation();
        article.setArticleId(articleId);
        article.setArticleName("Article " + articleId);

        final Task<Revision> revisionTask = new Task<>(article, 1);
        for (int revisionIndex = 0; revisionIndex < revisionTexts.size(); revisionIndex++) {
            // The revision counter starts at 1, revision IDs are unique per article
            final Revision revision = new Revision(revisionIndex + 1);
            revision.setArticleID(articleId);
            revision.setRevisionID(articleId * 1000 + revisionIndex);
            revision.setTimeStamp(new Timestamp(1_000_000_000_000L + revisionIndex * 60_000L));
            revision.setRevisionText(revisionTexts.get(revisionIndex));
            revisionTask.add(revision);
        }

        final DiffCollector diffCollector = new DiffCollector();
        new DiffCalculator(diffCollector).process(revisionTask);
        return diffCollector.diffs;
    }

    /**
     * Receives the diff tasks of the {@link DiffCalculator} and keeps all their diffs in order.
     */
    private static final class DiffCollector
        implements TaskTransmitterInterface
    {
        private final List<Diff> diffs = new ArrayList<>();

        @Override
        public void transmitDiff(final Task<Diff> result)
        {
            diffs.addAll(result.getContainer());
        }

        @Override
        public void transmitPartialDiff(final Task<Diff> result)
        {
            diffs.addAll(result.getContainer());
        }

        @Override
        public void close()
        {
            // nothing to close
        }
    }

    /**
     * Creates the revision history of a synthetic article. Each revision is derived from the one
     * before by one to four random edits (see {@link #applyRandomEdit(Random, StringBuilder)}).
     * <p>
     * A revision is only added if it differs from the one before after HTML unescaping, because
     * the diff calculator skips unchanged revisions. Empty revisions are left out as well, because
     * the {@link RevisionDecoder} cannot read an empty full revision.
     *
     * @param random
     *            source of the random edits
     * @param firstRevisionLength
     *            minimum length (in characters) of the first revision
     * @param revisionCount
     *            number of revisions to create
     * @return the revision texts, oldest first. They may contain HTML entities.
     */
    private static List<String> createRevisionHistory(final Random random,
            final int firstRevisionLength, final int revisionCount)
    {
        final List<String> revisionTexts = new ArrayList<>();
        final StringBuilder text = new StringBuilder(randomText(random, firstRevisionLength));
        revisionTexts.add(text.toString());

        while (revisionTexts.size() < revisionCount) {
            final int editCount = 1 + random.nextInt(4);
            for (int edit = 0; edit < editCount; edit++) {
                applyRandomEdit(random, text);
            }

            final String candidateText = text.toString();
            final String lastAddedText = revisionTexts.get(revisionTexts.size() - 1);
            final boolean changed = !StringEscapeUtils.unescapeHtml4(candidateText)
                    .equals(StringEscapeUtils.unescapeHtml4(lastAddedText));
            if (!candidateText.isEmpty() && changed) {
                revisionTexts.add(candidateText);
            }
        }
        return revisionTexts;
    }

    /**
     * Applies one random edit to the text: an insert, a delete, a replacement or a move of a text
     * block. Moves of blocks shorter than {@value #MIN_MOVED_BLOCK_LENGTH} characters are skipped,
     * so the text may stay unchanged.
     */
    private static void applyRandomEdit(final Random random, final StringBuilder text)
    {
        // Note: the order of the random calls determines the generated corpus. Keep it.
        final int length = text.length();
        final int position = length == 0 ? 0 : random.nextInt(length);
        final int span = length == 0 ? 0 : Math.min(length - position, 1 + random.nextInt(60));

        switch (random.nextInt(EDIT_KIND_COUNT)) {
        case EDIT_INSERT:
            text.insert(position, randomText(random, 1 + random.nextInt(80)));
            break;
        case EDIT_DELETE:
            text.delete(position, position + span);
            break;
        case EDIT_REPLACE:
            text.replace(position, position + span, randomText(random, 1 + random.nextInt(40)));
            break;
        default:
            // Move a block that is long enough to be detected as cut and paste
            final int blockLength = Math.min(length - position,
                    MIN_MOVED_BLOCK_LENGTH + random.nextInt(200));
            if (blockLength >= MIN_MOVED_BLOCK_LENGTH && length > blockLength) {
                final String movedBlock = text.substring(position, position + blockLength);
                text.delete(position, position + blockLength);
                text.insert(random.nextInt(text.length() + 1), movedBlock);
            }
            break;
        }
    }

    /**
     * @return random words from the {@link #VOCABULARY}, each followed by a space, with at least
     *         {@code minLength} characters in total
     */
    private static String randomText(final Random random, final int minLength)
    {
        final StringBuilder builder = new StringBuilder(minLength + 20);
        while (builder.length() < minLength) {
            builder.append(VOCABULARY[random.nextInt(VOCABULARY.length)]).append(' ');
        }
        return builder.toString();
    }

    /** @return the given text repeated {@code count} times */
    private static String repeat(final String text, final int count)
    {
        final StringBuilder builder = new StringBuilder(text.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }

    /**
     * Creates a diff from the given parts, together with the codec data (the bit widths of the
     * encoded fields) the encoder needs for it, as the {@link DiffCalculator} would.
     */
    private static Diff createDiff(final DiffPart... parts)
    {
        final Diff diff = new Diff();
        final RevisionCodecData codecData = new RevisionCodecData();
        for (DiffPart part : parts) {
            diff.add(part);
            registerFieldSizes(codecData, part);
        }
        diff.setCodecData(codecData);
        return diff;
    }

    /** @return a diff part with the given action, start position, length and text */
    private static DiffPart createDiffPart(final DiffAction action, final int start,
            final int length, final String text)
    {
        final DiffPart part = new DiffPart(action);
        part.setStart(start);
        part.setLength(length);
        part.setText(text);
        return part;
    }

    /**
     * Lets the codec data grow its bit widths so that the start position, the length and the text
     * length (in UTF-8 bytes) of the part fit, for the fields the encoder writes for this action.
     */
    private static void registerFieldSizes(final RevisionCodecData codecData, final DiffPart part)
    {
        final DiffAction action = part.getAction();
        if (action != DiffAction.FULL_REVISION_UNCOMPRESSED) {
            codecData.checkBlocksizeS(part.getStart());
        }
        if (action == DiffAction.REPLACE || action == DiffAction.DELETE) {
            codecData.checkBlocksizeE(part.getLength());
        }
        if (part.getText() != null) {
            codecData.checkBlocksizeL(part.getText().getBytes(StandardCharsets.UTF_8).length);
        }
    }

    /**
     * Sets up the global diff tool configuration that {@link DiffCalculator} and
     * {@link RevisionEncoder} read: a full revision every {@value #FULL_REVISION_INTERVAL}
     * revisions, surrogate characters replaced instead of discarding the revision, and diff
     * verification enabled.
     *
     * @param zipCompressionEnabled
     *            whether the encoders compress their output
     */
    private static void configureDiffTool(final boolean zipCompressionEnabled)
    {
        final ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED,
                zipCompressionEnabled);
        settings.setConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION,
                FULL_REVISION_INTERVAL);
        settings.setConfigParameter(ConfigurationKeys.MODE_SURROGATES, SurrogateModes.REPLACE);
        settings.setConfigParameter(ConfigurationKeys.VERIFICATION_DIFF, true);
        new ConfigurationManager(settings);
    }

    /**
     * @return whether zip compression is enabled in the current global configuration, so the
     *         {@link ReferenceEncoder} uses the same setting as the {@link RevisionEncoder}
     */
    private static boolean isZipCompressionEnabled() throws Exception
    {
        final ConfigurationManager configuration = ConfigurationManager.getInstance();
        return (Boolean) configuration
                .getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
    }

    /**
     * Copy of the {@link RevisionEncoder} as released in 2.2.0, which produces the expected
     * (golden) output of this test. It differs from the current encoder only in how it works
     * internally, not in its output: the output buffer is sized with the bit count instead of the
     * byte count, texts are written byte by byte, and compressed data is read in chunks of 1000
     * bytes.
     * <p>
     * Do not "fix" or modernize this class: its purpose is to stay as it was.
     */
    private static final class ReferenceEncoder
    {

        /** Marks compressed binary output (first byte). */
        private static final int COMPRESSED_BINARY_MARKER = -128;

        /** Marks compressed Base64 output (first character). */
        private static final String COMPRESSED_BASE64_MARKER = "_";

        private static final int DEFLATE_CHUNK_SIZE = 1000;

        private final boolean zipCompressionEnabled;

        private RevisionCodecData codecData;

        private ByteArrayOutputStream output;

        /** Bits not yet written to {@link #output}, left-aligned in the lowest byte. */
        private int pendingBits;

        /** Number of bits in {@link #pendingBits} (0 to 7). */
        private int pendingBitCount;

        ReferenceEncoder(final boolean zipCompressionEnabled)
        {
            this.zipCompressionEnabled = zipCompressionEnabled;
        }

        /**
         * @return the binary encoding of the diff: compressed (with a marker byte in front) if
         *         compression is enabled and pays off, otherwise uncompressed
         */
        byte[] binaryDiff(final RevisionCodecData codecData, final Diff diff) throws Exception
        {
            final byte[] uncompressed = encode(codecData, diff);
            if (!zipCompressionEnabled) {
                return uncompressed;
            }

            final ByteArrayOutputStream markedCompressed = new ByteArrayOutputStream();
            markedCompressed.write(COMPRESSED_BINARY_MARKER);
            markedCompressed.write(deflate(uncompressed));
            final byte[] compressed = markedCompressed.toByteArray();
            return uncompressed.length + 1 < compressed.length - 1 ? uncompressed : compressed;
        }

        /**
         * @return the Base64 encoding of the diff: compressed (with a marker character in front)
         *         if compression is enabled and pays off, otherwise uncompressed
         */
        String encodeDiff(final RevisionCodecData codecData, final Diff diff) throws Exception
        {
            final byte[] uncompressed = encode(codecData, diff);
            final Base64.Encoder base64 = Base64.getEncoder();
            if (!zipCompressionEnabled) {
                return base64.encodeToString(uncompressed);
            }

            final byte[] compressed = deflate(uncompressed);
            if (uncompressed.length + 1 < compressed.length) {
                return base64.encodeToString(uncompressed);
            }
            return COMPRESSED_BASE64_MARKER + base64.encodeToString(compressed);
        }

        /** @return the input compressed with a default {@link Deflater} */
        private static byte[] deflate(final byte[] input)
        {
            final Deflater deflater = new Deflater();
            try {
                deflater.setInput(input);
                deflater.finish();

                final byte[] chunk = new byte[DEFLATE_CHUNK_SIZE];
                final ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                int chunkLength;
                do {
                    chunkLength = deflater.deflate(chunk);
                    compressed.write(chunk, 0, chunkLength);
                }
                while (chunkLength == DEFLATE_CHUNK_SIZE);
                return compressed.toByteArray();
            }
            finally {
                deflater.end();
            }
        }

        /**
         * Writes the codec header (the bit widths) and then each diff part as a 3-bit operation
         * code followed by its fields.
         *
         * @return the uncompressed encoding of the diff
         */
        private byte[] encode(final RevisionCodecData codecData, final Diff diff) throws Exception
        {
            this.codecData = codecData;
            this.output = new ByteArrayOutputStream(codecData.totalSizeInBits());
            this.pendingBits = 0;
            this.pendingBitCount = 0;

            // Header: code 0, then the bit widths of the start, length, block and text length
            writeBits(3, 0);
            writeBits(5, codecData.getBlocksizeS());
            writeBits(5, codecData.getBlocksizeE());
            writeBits(5, codecData.getBlocksizeB());
            writeBits(5, codecData.getBlocksizeL());
            writeFillBits();

            for (Iterator<DiffPart> parts = diff.iterator(); parts.hasNext();) {
                final DiffPart part = parts.next();
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
                    writeFillBits();
                    break;
                case REPLACE:
                    writeBits(3, 4);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeE(), part.getLength());
                    writeText(part.getText());
                    break;
                case CUT:
                    // The text of a cut or paste part is the number of the moved block
                    writeBits(3, 5);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeE(), part.getLength());
                    writeBits(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));
                    writeFillBits();
                    break;
                case PASTE:
                    writeBits(3, 6);
                    writeBits(codecData.getBlocksizeS(), part.getStart());
                    writeBits(codecData.getBlocksizeB(), Integer.parseInt(part.getText()));
                    writeFillBits();
                    break;
                default:
                    throw new IllegalStateException(part.getAction().toString());
                }
            }
            return output.toByteArray();
        }

        /** Writes the byte length of the text, fill bits and then the text bytes one by one. */
        private void writeText(final String text) throws Exception
        {
            final byte[] textBytes = text.getBytes(TEXT_ENCODING);
            writeBits(codecData.getBlocksizeL(), textBytes.length);
            writeFillBits();
            for (byte textByte : textBytes) {
                output.write(0xFF & textByte);
            }
        }

        /** Writes the lowest {@code bitCount} bits of the value, most significant bit first. */
        private void writeBits(final int bitCount, final int value)
        {
            for (int i = bitCount - 1; i >= 0; i--) {
                pendingBits |= ((value >> i) & 1) << (7 - pendingBitCount);
                pendingBitCount++;
                if (pendingBitCount == 8) {
                    output.write(pendingBits);
                    pendingBits = 0;
                    pendingBitCount = 0;
                }
            }
        }

        /** Pads the current byte with 0 bits, so the next write starts at a byte boundary. */
        private void writeFillBits()
        {
            while (pendingBitCount != 0) {
                writeBits(1, 0);
            }
            pendingBits = 0;
        }
    }
}
