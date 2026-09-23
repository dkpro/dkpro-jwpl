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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link BlockManagement} produces the same diffs as the former implementation, which
 * rebuilt the text of the new revision only to measure its length (see issue #583).
 */
class BlockManagementTest
{

    private static final String P1 = "The quick brown fox jumps over the lazy dog near the river bank.\n";
    private static final String P2 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do.\n";
    private static final String P3 = "Käse und Brötchen 日本語 with a grin 😀 here.\n";
    private static final String P4 = "Wikipedia is a free online encyclopedia written by volunteers.\n";

    @BeforeAll
    static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    void representativePairsProduceTheFormerDiffs() throws Exception
    {
        String[][] pairs = {
                // insert
                { P1 + P2, P1 + "An inserted sentence of some length.\n" + P2 },
                // delete
                { P1 + P2 + P4, P1 + P4 },
                // replace
                { P1 + P2 + P4, P1 + "A completely different middle part.\n" + P4 },
                // moved paragraphs (cut / paste)
                { P1 + P2 + P3 + P4, P4 + P3 + P2 + P1 },
                { P1 + P2 + P3 + P4, P3 + P1 + P4 + P2 },
                // non-BMP and CJK text
                { P3 + P1, P1 + "😀😁 中文 " + P3 },
                // everything replaced, text appended or removed at both ends
                { P1, P2 },
                { P2, "x" + P2 + "y" },
                { "x" + P2 + "y", P2 } };

        Set<DiffAction> seen = EnumSet.noneOf(DiffAction.class);
        for (String[] pair : pairs) {
            seen.addAll(compare(pair[0], pair[1]));
        }
        assertTrue(seen.containsAll(EnumSet.of(DiffAction.INSERT, DiffAction.DELETE,
                DiffAction.REPLACE, DiffAction.CUT, DiffAction.PASTE)), seen.toString());
    }

    @Test
    void randomEditsProduceTheFormerDiffs() throws Exception
    {
        Random random = new Random(583);
        String[] paragraphs = { P1, P2, P3, P4 };
        for (int i = 0; i < 200; i++) {
            List<String> a = new ArrayList<>();
            for (int j = 0; j < 3 + random.nextInt(5); j++) {
                a.add(paragraphs[random.nextInt(paragraphs.length)] + j);
            }
            List<String> b = new ArrayList<>(a);
            for (int edits = 1 + random.nextInt(4); edits > 0; edits--) {
                int k = random.nextInt(b.size());
                switch (random.nextInt(4)) {
                case 0 -> b.add(k, paragraphs[random.nextInt(paragraphs.length)] + "new");
                case 1 -> {
                    if (b.size() > 1) {
                        b.remove(k);
                    }
                }
                case 2 -> b.set(k, b.get(k).substring(0, b.get(k).length() / 2) + "😀");
                default -> b.add(random.nextInt(b.size()), b.remove(k));
                }
            }
            compare(String.join("", a), String.join("", b));
        }
    }

    /**
     * Diffs both revisions with the {@link DiffCalculator} and checks that the current and the
     * former block management yield identical diff parts and codec block sizes.
     *
     * @return actions contained in the diff
     */
    private static Set<DiffAction> compare(String a, String b) throws Exception
    {
        char[] revA = a.toCharArray();
        char[] revB = b.toCharArray();

        ComparingBlockManagement comparing = new ComparingBlockManagement();
        DiffCalculator calculator = new DiffCalculator(null);
        Field blocks = DiffCalculator.class.getDeclaredField("blocks");
        blocks.setAccessible(true);
        blocks.set(calculator, comparing);
        Method generateDiff = DiffCalculator.class.getDeclaredMethod("generateDiff", char[].class,
                char[].class);
        generateDiff.setAccessible(true);

        Diff actual = (Diff) generateDiff.invoke(calculator, revA, revB);
        Diff expected = comparing.expected;

        assertEquals(expected.size(), actual.size(), actual.toString());
        Set<DiffAction> actions = EnumSet.noneOf(DiffAction.class);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
            actions.add(actual.get(i).getAction());
        }

        RevisionCodecData e = expected.getCodecData();
        RevisionCodecData c = actual.getCodecData();
        assertEquals(e.getBlocksizeS(), c.getBlocksizeS());
        assertEquals(e.getBlocksizeE(), c.getBlocksizeE());
        assertEquals(e.getBlocksizeB(), c.getBlocksizeB());
        assertEquals(e.getBlocksizeL(), c.getBlocksizeL());

        assertEquals(b, actual.buildRevision(revA));
        return actions;
    }

    /**
     * Runs the former implementation on a copy of the block queues before delegating to
     * {@link BlockManagement}.
     */
    private static final class ComparingBlockManagement
        implements BlockManagementInterface
    {
        private final BlockManagement current;
        private Diff expected;

        ComparingBlockManagement() throws Exception
        {
            this.current = new BlockManagement();
        }

        @Override
        public Diff manage(char[] revA, char[] revB, ArrayList<DiffBlock> queueA,
                ArrayList<DiffBlock> queueB)
            throws UnsupportedEncodingException
        {
            expected = new FormerBlockManagement().manage(revA, revB, new ArrayList<>(queueA),
                    new ArrayList<>(queueB));
            Diff diff = current.manage(revA, revB, queueA, queueB);
            assertTrue(queueA.isEmpty() && queueB.isEmpty());
            return diff;
        }
    }

    /**
     * The block management as it was before issue #583, reconstructing the new revision.
     */
    private static final class FormerBlockManagement
    {
        private final StringBuilder version = new StringBuilder();
        private final Diff diff = new Diff();
        private final Map<Integer, String> bufferMap = new HashMap<>();
        private final RevisionCodecData codecData = new RevisionCodecData();

        Diff manage(char[] revA, char[] revB, ArrayList<DiffBlock> queueA,
                ArrayList<DiffBlock> queueB)
        {
            DiffBlock curA = null, curB = null;
            while (!queueA.isEmpty() || !queueB.isEmpty() || curB != null) {
                if (!queueA.isEmpty() && curA == null) {
                    curA = queueA.remove(0);
                }
                if (!queueB.isEmpty() && curB == null) {
                    curB = queueB.remove(0);
                }
                if (curA != null && curB != null) {
                    if (curA.getId() == curB.getId()) {
                        if (curA.getId() == -1) {
                            String text = copy(revB, curB.getRevBStart(), curB.getRevBEnd());
                            DiffPart action = part(DiffAction.REPLACE);
                            length(action, curA);
                            text(action, text);
                        }
                        else {
                            version.append(copy(revA, curA.getRevAStart(), curA.getRevAEnd()));
                        }
                        curA = null;
                        curB = null;
                    }
                    else if (curA.getId() == -1) {
                        length(part(DiffAction.DELETE), curA);
                        curA = null;
                    }
                    else if (curB.getId() == -1) {
                        text(part(DiffAction.INSERT),
                                copy(revB, curB.getRevBStart(), curB.getRevBEnd()));
                        curB = null;
                    }
                    else if (bufferMap.containsKey(curB.getId())) {
                        paste(curB);
                        curB = null;
                    }
                    else {
                        cut(revA, curA);
                        curA = null;
                    }
                }
                else if (curA != null) {
                    length(part(DiffAction.DELETE), curA);
                    curA = null;
                }
                else if (bufferMap.containsKey(curB.getId())) {
                    paste(curB);
                    curB = null;
                }
                else {
                    text(part(DiffAction.INSERT),
                            copy(revB, curB.getRevBStart(), curB.getRevBEnd()));
                    curB = null;
                }
            }
            diff.setCodecData(codecData);
            return diff;
        }

        private static String copy(char[] array, int start, int end)
        {
            StringBuilder text = new StringBuilder();
            for (int j = start; j < end; j++) {
                text.append(array[j]);
            }
            return text.toString();
        }

        private DiffPart part(DiffAction type)
        {
            DiffPart action = new DiffPart(type);
            action.setStart(version.length());
            codecData.checkBlocksizeS(version.length());
            diff.add(action);
            return action;
        }

        private void length(DiffPart action, DiffBlock curA)
        {
            action.setLength(curA.getRevAEnd() - curA.getRevAStart());
            codecData.checkBlocksizeE(action.getLength());
        }

        private void text(DiffPart action, String text)
        {
            action.setText(text);
            codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);
            version.append(text);
        }

        private void cut(char[] revA, DiffBlock curA)
        {
            DiffPart action = part(DiffAction.CUT);
            length(action, curA);
            action.setText(Integer.toString(curA.getId()));
            codecData.checkBlocksizeB(curA.getId());
            bufferMap.put(curA.getId(), copy(revA, curA.getRevAStart(), curA.getRevAEnd()));
        }

        private void paste(DiffBlock curB)
        {
            DiffPart action = part(DiffAction.PASTE);
            action.setText(Integer.toString(curB.getId()));
            codecData.checkBlocksizeB(curB.getId());
            version.append(bufferMap.remove(curB.getId()));
        }
    }
}
