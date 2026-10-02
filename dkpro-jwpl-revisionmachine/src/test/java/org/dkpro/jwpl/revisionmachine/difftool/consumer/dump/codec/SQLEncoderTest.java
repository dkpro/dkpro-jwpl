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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.SQLEscape;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionDecoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SQLEncoderTest
{

    @BeforeAll
    public static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    public void testTablesHaveNamespaceColumn() throws Exception
    {
        SQLEncoder encoder = new SQLEncoder(null);

        assertTrue(encoder.getTable()[0].contains("Namespace INTEGER"));
        assertTrue(encoder.getBinaryTable()[0].contains("Namespace INTEGER"));
    }

    @Test
    public void testTablesDeclareEngineAndArticleIndexes() throws Exception
    {
        SQLEncoder encoder = new SQLEncoder(null);

        for (String[] table : new String[][] { encoder.getTable(), encoder.getBinaryTable() }) {
            assertEquals(2, table.length);
            assertTrue(table[0].contains(") ENGINE = MyISAM "), table[0]);
            assertFalse(table[0].contains("TYPE ="), table[0]);
            assertTrue(table[0].contains("KEY articleIdx (ArticleID, RevisionCounter)"), table[0]);
            assertTrue(table[0].contains("KEY articleTsIdx (ArticleID, Timestamp, RevisionCounter)"),
                    table[0]);
            assertEquals(SQLEncoder.DISABLE_KEYS, table[1]);
        }
    }

    @Test
    public void testEncodeTaskWritesNamespace() throws Exception
    {
        // ... ContributorId, ContributorIsRegistered, Namespace)
        String sql = new SQLEncoder(null).encodeTask(task(1))[0].getQuery();
        assertTrue(sql.endsWith(",null,1,1);"), sql);
    }

    @Test
    public void testEncodeTaskWritesNullForUnknownNamespace() throws Exception
    {
        String sql = new SQLEncoder(null).encodeTask(task(null))[0].getQuery();
        assertTrue(sql.endsWith(",null,1,null);"), sql);
    }

    @Test
    public void testBinaryTaskWritesNamespace() throws Exception
    {
        RecordingStatement statement = new RecordingStatement();
        new SQLEncoder(null).binaryTask(task(1), statement.proxy());

        assertEquals(1, statement.rows.size());
        assertEquals(1, statement.rows.get(0).get(12));
    }

    @Test
    public void testBinaryTaskWritesNullForUnknownNamespace() throws Exception
    {
        RecordingStatement statement = new RecordingStatement();
        new SQLEncoder(null).binaryTask(task(null), statement.proxy());

        assertEquals(1, statement.rows.size());
        assertTrue(statement.rows.get(0).containsKey(12));
        assertNull(statement.rows.get(0).get(12));
    }

    @Test
    public void testBinaryTaskBindsAllColumns() throws Exception
    {
        String comment = "It's a \"quote\", a back\\slash\nand a new line";
        String name = "O'Brien \\ Co";
        Task<Diff> task = twoRevisionTask(SQLEscape.escape(comment), SQLEscape.escape(name), 5);

        RecordingStatement statement = new RecordingStatement();
        long size = new SQLEncoder(null).binaryTask(task, statement.proxy());

        assertEquals(List.of(2), statement.batches);
        assertEquals(2, statement.rows.size());
        assertTrue(size > 0);

        Map<Integer, Object> full = statement.rows.get(0);
        Map<Integer, Object> insert = statement.rows.get(1);
        for (Map<Integer, Object> row : statement.rows) {
            assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), row.keySet());
        }

        // FullRevisionID, RevisionCounter, RevisionID, ArticleID, Timestamp
        assertEquals(List.of(7, 1, 7, 42, 1000L),
                List.of(full.get(1), full.get(2), full.get(3), full.get(4), full.get(5)));
        assertEquals(List.of(7, 2, 8, 42, 2000L), List.of(insert.get(1), insert.get(2),
                insert.get(3), insert.get(4), insert.get(5)));

        // the comment and the name are stored as they were before the XML reader escaped them
        assertEquals(comment, full.get(7));
        assertNull(insert.get(7));
        assertEquals(1, full.get(8));
        assertEquals(0, insert.get(8));
        assertEquals(name, full.get(9));
        assertEquals("null", insert.get(9));
        assertEquals(5, full.get(10));
        assertNull(insert.get(10));
        assertEquals(1, full.get(11));
        assertEquals(0, insert.get(11));
        assertEquals(0, full.get(12));

        // the revision column holds the binary encoding, which decodes to the original diff
        for (int i = 0; i < 2; i++) {
            RevisionDecoder decoder = new RevisionDecoder(StandardCharsets.UTF_8.name());
            decoder.setInput(new ByteArrayInputStream((byte[]) statement.rows.get(i).get(6)),
                    true);
            assertEquals(task.get(i).toString(), decoder.decode().toString());
        }
    }

    @Test
    public void testBinaryTaskSplitsBatchesAtThePacketLimit() throws Exception
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET, 200L);
        new ConfigurationManager(settings);
        try {
            SQLEncoder encoder = new SQLEncoder(null);
            RecordingStatement statement = new RecordingStatement();
            encoder.binaryTask(twoRevisionTask(null, "Someone", null), statement.proxy());

            assertEquals(List.of(1, 1), statement.batches);
        }
        finally {
            setUpConfiguration();
        }
    }

    @Test
    public void testBinaryTaskStoresTheSameValuesAsEncodeTask() throws Exception
    {
        String comment = SQLEscape.escape("a 'b' \\ c");
        Task<Diff> task = twoRevisionTask(comment, SQLEscape.escape("x\"y"), 3);

        String sql = new SQLEncoder(null).encodeTask(task)[0].getQuery();
        RecordingStatement statement = new RecordingStatement();
        new SQLEncoder(null).binaryTask(task, statement.proxy());

        // the textual encoding embeds the escaped values as string literals
        Map<Integer, Object> row = statement.rows.get(0);
        assertTrue(sql.contains(",'" + SQLEscape.escape((String) row.get(7)) + "',1,'"
                + SQLEscape.escape((String) row.get(9)) + "',3,1,0),"), sql);
        assertTrue(sql.endsWith(",null,0,'null',null,0,0);"), sql);
    }

    @Test
    public void testDataFileEncoderWritesNamespace() throws Exception
    {
        String row = new DataFileEncoder().encodeTask(task(1)).get(0);
        assertTrue(row.endsWith(",\\N,1,1"), row);
    }

    @Test
    public void testDataFileEncoderWritesNullForUnknownNamespace() throws Exception
    {
        String row = new DataFileEncoder().encodeTask(task(null)).get(0);
        assertTrue(row.endsWith(",\\N,1,\\N"), row);
    }

    /**
     * Creates a task holding a single full revision of an article in the given namespace.
     */
    private static Task<Diff> task(Integer namespace) throws Exception
    {
        ArticleInformation header = new ArticleInformation();
        header.setArticleId(42);
        header.setArticleName("Talk:Main_Page");
        header.setNamespace(namespace);

        String text = "Some text";
        DiffPart part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
        part.setText(text);

        RevisionCodecData codecData = new RevisionCodecData();
        codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);

        Diff diff = new Diff();
        diff.setRevisionCoutner(1);
        diff.setRevisionID(7);
        diff.setTimeStamp(new Timestamp(0));
        diff.setContributorName("Someone");
        diff.setContributorIsRegistered(true);
        diff.add(part);
        diff.setCodecData(codecData);

        Task<Diff> task = new Task<>(header, 1);
        task.add(diff);
        return task;
    }
    /**
     * Creates a task with a full revision and a revision that inserts text into it.
     */
    private static Task<Diff> twoRevisionTask(String comment, String contributorName,
            Integer contributorId)
    {
        ArticleInformation header = new ArticleInformation();
        header.setArticleId(42);
        header.setArticleName("Main_Page");
        header.setNamespace(0);

        String text = "Some text";
        DiffPart full = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
        full.setText(text);

        String inserted = " and more";
        DiffPart insert = new DiffPart(DiffAction.INSERT);
        insert.setStart(text.length());
        insert.setText(inserted);

        RevisionCodecData codecData = new RevisionCodecData();
        codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);
        codecData.checkBlocksizeS(text.length());
        codecData.checkBlocksizeL(inserted.getBytes(StandardCharsets.UTF_8).length);

        Diff first = new Diff();
        first.setRevisionCoutner(1);
        first.setRevisionID(7);
        first.setTimeStamp(new Timestamp(1000));
        first.setComment(comment);
        first.setMinor(true);
        first.setContributorName(contributorName);
        first.setContributorId(contributorId);
        first.setContributorIsRegistered(true);
        first.add(full);
        first.setCodecData(codecData);

        Diff second = new Diff();
        second.setRevisionCoutner(2);
        second.setRevisionID(8);
        second.setTimeStamp(new Timestamp(2000));
        second.add(insert);
        second.setCodecData(codecData);

        Task<Diff> task = new Task<>(header, 1);
        task.add(first);
        task.add(second);
        return task;
    }

    /**
     * Records the parameters and batches of a prepared statement.
     */
    private static final class RecordingStatement
        implements InvocationHandler
    {

        private final List<Map<Integer, Object>> rows = new ArrayList<>();

        private final List<Integer> batches = new ArrayList<>();

        private Map<Integer, Object> parameters = new HashMap<>();

        private int pending;

        PreparedStatement proxy()
        {
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] { PreparedStatement.class }, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            String name = method.getName();
            if (name.equals("setNull")) {
                parameters.put((Integer) args[0], null);
            }
            else if (name.startsWith("set")) {
                parameters.put((Integer) args[0], args[1]);
            }
            else if (name.equals("addBatch")) {
                rows.add(parameters);
                parameters = new HashMap<>();
                pending++;
            }
            else if (name.equals("executeBatch")) {
                batches.add(pending);
                int[] counts = new int[pending];
                pending = 0;
                return counts;
            }
            else {
                throw new UnsupportedOperationException(name);
            }
            return null;
        }
    }
}
