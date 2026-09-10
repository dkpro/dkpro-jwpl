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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
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
        String sql = new SQLEncoder(null).binaryTask(task(1))[0].getQuery();
        assertTrue(sql.endsWith(",null,1,1);"), sql);
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
}
