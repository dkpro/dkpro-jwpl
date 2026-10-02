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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifies that {@link DiffCalculator} produces exactly the same diffs as the original
 * longest-match search, which is kept below as a reference implementation (see issue #549).
 */
class DiffCalculatorEquivalenceTest
{

    private static final String WIKI_TEXT = "{{Infobox settlement|name=Darmstadt|state=Hesse}}\n"
            + "'''Darmstadt''' is a city in the state of [[Hesse]] in [[Germany]]. "
            + "It is located in the southern part of the [[Rhine-Main]] area.\n"
            + "== History ==\nThe city was first mentioned in the 11th century. "
            + "The city was the capital of the [[Grand Duchy of Hesse]].\n"
            + "{| class=\"wikitable\"\n|-\n| 1900 || 72,000\n|-\n| 1950 || 94,000\n"
            + "|-\n| 2000 || 138,000\n|}\n"
            + "== Science ==\nThe city is home to [[TU Darmstadt]] and [[GSI]].\n"
            + "[[Category:Cities in Hesse]]\n[[Category:Darmstadt]]\n[[de:Darmstadt]]";

    private static void configure(int minimumLength)
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING,
                minimumLength);
        new ConfigurationManager(settings);
    }

    private static List<String[]> handPickedPairs()
    {
        String w = WIKI_TEXT;
        List<String[]> pairs = new ArrayList<>();
        pairs.add(new String[] { w, w });
        pairs.add(new String[] { w, w + "\n[[fr:Darmstadt]]" });
        pairs.add(new String[] { w, "{{Stub}}\n" + w });
        // edits at both ends
        pairs.add(new String[] { w, "{{Short description|City in Hesse}}\n"
                + w.replace("[[de:Darmstadt]]", "[[de:Darmstadt]]\n[[fr:Darmstadt]]") });
        pairs.add(new String[] { w, w.replace("Infobox", "Infobox German")
                .replace("Category:Darmstadt", "Category:University towns") });
        // section move
        int history = w.indexOf("== History ==");
        int science = w.indexOf("== Science ==");
        int category = w.indexOf("[[Category:");
        pairs.add(new String[] { w, w.substring(0, history) + w.substring(science, category)
                + w.substring(history, science) + w.substring(category) });
        // partial blanking and its revert
        pairs.add(new String[] { w, w.substring(0, history) + w.substring(category) });
        pairs.add(new String[] { w.substring(0, history) + w.substring(category), w });
        // repeated blocks, which produce many equally long matches
        String row = "|-\n| x || 1 || 2 || 3\n";
        pairs.add(new String[] { "{|\n" + row.repeat(20) + "|}", "{|\n" + row.repeat(23) + "|}" });
        pairs.add(new String[] { "a" + row.repeat(10) + "b", "c" + row.repeat(7) + "d" });
        pairs.add(new String[] { "abcdefghijklmnopqrstuvwxyz".repeat(3),
                "zyxwvutsrqponmlkjihgfedcba" + "abcdefghijklmnopqrstuvwxyz".repeat(2) });
        // full rewrite and non-ASCII characters
        pairs.add(new String[] { w, new StringBuilder(w).reverse().toString() });
        pairs.add(new String[] { "Käse 日本語 ".repeat(8) + "x",
                "y" + "日本語 Käse ".repeat(8) });
        // very short revisions
        pairs.add(new String[] { "a", "b" });
        pairs.add(new String[] { "ab", "ba" });
        pairs.add(new String[] { "abc", "abcabc" });
        pairs.add(new String[] { "aaaa", "aaaaaaaa" });
        return pairs;
    }

    private static String randomText(Random random, int length, int alphabetSize)
    {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + random.nextInt(alphabetSize)));
        }
        return builder.toString();
    }

    private static String mutate(Random random, String text, int alphabetSize)
    {
        StringBuilder builder = new StringBuilder(text);
        int edits = 1 + random.nextInt(6);
        for (int e = 0; e < edits; e++) {
            int length = builder.length();
            int pos = length == 0 ? 0 : random.nextInt(length + 1);
            int span = 1 + random.nextInt(Math.max(1, length / 4 + 1));
            int end = Math.min(length, pos + span);
            switch (random.nextInt(5)) {
                case 0 -> builder.insert(pos, randomText(random, span, alphabetSize));
                case 1 -> builder.delete(pos, end);
                case 2 -> builder.replace(pos, end, randomText(random, span, alphabetSize));
                case 3 -> {
                    // move a block
                    String block = builder.substring(pos, end);
                    builder.delete(pos, end);
                    builder.insert(random.nextInt(builder.length() + 1), block);
                }
                default -> {
                    // duplicate a block
                    String block = builder.substring(pos, end);
                    builder.insert(random.nextInt(builder.length() + 1), block);
                }
            }
        }
        // make sure that both ends are touched in most cases
        if (random.nextBoolean()) {
            builder.insert(0, (char) ('a' + random.nextInt(alphabetSize)));
            builder.append((char) ('a' + random.nextInt(alphabetSize)));
        }
        return builder.length() == 0 ? "a" : builder.toString();
    }

    private static void assertSameDiff(DiffCalculator calculator, ReferenceDiffCalculator reference,
            String revA, String revB)
        throws Exception
    {
        Diff expected = reference.generateDiff(revA.toCharArray(), revB.toCharArray());
        Diff actual = calculator.generateDiff(revA.toCharArray(), revB.toCharArray());

        String message = "revA=\"" + revA + "\", revB=\"" + revB + "\"";
        assertEquals(expected.toString(), actual.toString(), message);
        assertEquals(expected.getCodecData().toString(), actual.getCodecData().toString(),
                message);
        assertEquals(revB, actual.buildRevision(revA), message);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 3, 12, 50 })
    void handPickedPairsProduceIdenticalDiffs(int minimumLength) throws Exception
    {
        configure(minimumLength);
        DiffCalculator calculator = new DiffCalculator(null);
        ReferenceDiffCalculator reference = new ReferenceDiffCalculator(minimumLength);

        for (String[] pair : handPickedPairs()) {
            assertSameDiff(calculator, reference, pair[0], pair[1]);
            assertSameDiff(calculator, reference, pair[1], pair[0]);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 3, 12, 50 })
    void randomPairsProduceIdenticalDiffs(int minimumLength) throws Exception
    {
        configure(minimumLength);
        DiffCalculator calculator = new DiffCalculator(null);
        ReferenceDiffCalculator reference = new ReferenceDiffCalculator(minimumLength);

        Random random = new Random(549L + minimumLength);
        for (int n = 0; n < 400; n++) {
            int alphabetSize = 1 + random.nextInt(random.nextBoolean() ? 4 : 26);
            String revA = randomText(random, 1 + random.nextInt(400), alphabetSize);
            if (random.nextInt(3) == 0) {
                // repeated content, as in tables and navigation boxes
                revA = revA.substring(0, Math.min(revA.length(), 40)).repeat(1 + random.nextInt(8));
            }
            String revB = mutate(random, revA, alphabetSize);
            assertSameDiff(calculator, reference, revA, revB);
        }
    }

    /**
     * Copy of the diff generation of {@link DiffCalculator} as it was before issue #549, using the
     * boxed character to position mapping and the unpruned longest-match search.
     */
    private static final class ReferenceDiffCalculator
    {

        private final int VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING;

        private final BlockManagementInterface blocks;

        private int blockCount;

        private boolean[] revABlocked;

        private boolean[] revBBlocked;

        private HashMap<Character, ArrayList<Integer>> positions;

        private ArrayList<DiffBlock> queueA;

        private ArrayList<DiffBlock> queueB;

        private int longestMatch_size;

        private int longestMatch_start;

        ReferenceDiffCalculator(int minimumLength) throws Exception
        {
            this.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING = minimumLength;
            this.blocks = new BlockManagement();
        }

        Diff generateDiff(final char[] revA, final char[] revB) throws Exception
        {

            blockCount = 0;
            queueA = new ArrayList<>();
            queueB = new ArrayList<>();

            revABlocked = new boolean[revA.length];
            revBBlocked = new boolean[revB.length];

            int revAStartIndex = 0, revAEndIndex = revA.length - 1;
            int revBStartIndex = 0, revBEndIndex = revB.length - 1;

            while (revAStartIndex <= revAEndIndex && revBStartIndex <= revBEndIndex
                    && revA[revAStartIndex] == revB[revBStartIndex]) {

                revABlocked[revAStartIndex] = true;
                revBBlocked[revBStartIndex] = true;
                revAStartIndex++;
                revBStartIndex++;
            }

            // First Block
            if (revAStartIndex != 0) {
                queueA.add(
                        new DiffBlock(this.blockCount, 0, revAStartIndex, 0, revBStartIndex, true));
                queueB.add(new DiffBlock(this.blockCount, 0, revAStartIndex, 0, revBStartIndex,
                        false));
                this.blockCount++;
            }

            while (revAStartIndex < revAEndIndex && revBStartIndex < revBEndIndex
                    && revA[revAEndIndex] == revB[revBEndIndex]) {

                revABlocked[revAEndIndex] = true;
                revBBlocked[revBEndIndex] = true;
                revAEndIndex--;
                revBEndIndex--;
            }

            // Last Block
            if (revAEndIndex + 1 != revA.length) {
                queueA.add(new DiffBlock(this.blockCount, revAEndIndex + 1, revA.length,
                        revBEndIndex + 1, revB.length, true));
                queueB.add(new DiffBlock(this.blockCount, revAEndIndex + 1, revA.length,
                        revBEndIndex + 1, revB.length, false));
                this.blockCount++;
            }

            scan(revA, revAStartIndex, revAEndIndex);

            ArrayList<Integer> list;
            char c;

            int i = revBStartIndex;
            while (i < revBEndIndex) {

                c = revB[i];
                list = positions.get(c);

                if (list != null && findLongestMatch(revA, list, revB, i)) {

                    i += longestMatch_size;
                }
                else {
                    i++;
                }
            }

            int j;
            for (i = revAStartIndex; i <= revAEndIndex; i++) {
                if (!revABlocked[i]) {
                    j = i;
                    while (i + 1 <= revAEndIndex && !revABlocked[++i]) {
                    }

                    if (i + 1 > revAEndIndex) {
                        i++;
                    }

                    queueA.add(new DiffBlock(-1, j, i, -1, -1, true));
                }
            }

            for (i = revBStartIndex; i <= revBEndIndex; i++) {
                if (!revBBlocked[i]) {
                    j = i;
                    while (i + 1 <= revBEndIndex && !revBBlocked[++i]) {
                    }

                    if (i + 1 > revBEndIndex) {
                        i++;
                    }

                    queueB.add(new DiffBlock(-1, -1, -1, j, i, false));
                }
            }

            Collections.sort(queueA);
            Collections.sort(queueB);

            return blocks.manage(revA, revB, queueA, queueB);
        }

        private void scan(final char[] input, final int start, final int end)
        {

            this.positions = new HashMap<>();
            ArrayList<Integer> list;

            char c;
            for (int i = start; i < end; i++) {
                c = input[i];

                list = positions.computeIfAbsent(c, k -> new ArrayList<>());

                list.add(i);
            }
        }

        private boolean findLongestMatch(final char[] revA, final ArrayList<Integer> list,
                final char[] revB, final int index)
        {

            int match;
            longestMatch_size = -1;

            int size = list.size();
            int revAsize = revA.length;
            int revBsize = revB.length;

            int start, end, count;
            for (int i = 0; i < size; i++) {

                start = list.get(i);
                if (!revABlocked[start] && !revBBlocked[index + 1]) {

                    count = index + 1;
                    end = start + 1;

                    while (end < revAsize && count < revBsize && revA[end] == revB[count]
                            && !revABlocked[end] && !revBBlocked[count]) {
                        end++;
                        count++;
                    }

                    match = end - start;
                    if (match > longestMatch_size) {
                        longestMatch_size = match;
                        longestMatch_start = start;
                    }
                }
            }

            if (longestMatch_size <= VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING) {
                return false;
            }

            queueA.add(new DiffBlock(this.blockCount, longestMatch_start,
                    longestMatch_start + longestMatch_size, index, index + longestMatch_size,
                    true));
            queueB.add(new DiffBlock(this.blockCount, longestMatch_start,
                    longestMatch_start + longestMatch_size, index, index + longestMatch_size,
                    false));

            blockCount++;

            for (int i = 0, j = longestMatch_start, k = index; i < longestMatch_size;
                    i++, j++, k++) {
                revABlocked[j] = true;
                revBBlocked[k] = true;
            }

            return true;
        }
    }
}
