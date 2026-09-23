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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.DataFileEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that the {@link DataFileWriter} writes every encoded diff and still rotates its output
 * file once the size limit is exceeded, although it only flushes at the end of a task (see issue
 * #581).
 */
class DataFileWriterTest
{

    @TempDir
    Path outputDir;

    private void configure(final long sizeLimit)
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        settings.setConfigParameter(ConfigurationKeys.PATH_OUTPUT_SQL_FILES,
                outputDir.toString());
        settings.setConfigParameter(ConfigurationKeys.LIMIT_SQL_FILE_SIZE, sizeLimit);
        new ConfigurationManager(settings);
    }

    private String read(final String name) throws Exception
    {
        return new String(Files.readAllBytes(outputDir.resolve(name)), StandardCharsets.UTF_8);
    }

    @Test
    void writesAllTasksIntoOneFileBelowTheLimit() throws Exception
    {
        configure(1000000000L);
        DataFileEncoder encoder = new DataFileEncoder();
        String first = encoder.encodeTask(task(42)).get(0);
        String second = encoder.encodeTask(task(43)).get(0);

        DataFileWriter writer = new DataFileWriter("output");
        writer.process(task(42));
        writer.process(task(43));
        writer.close();

        assertEquals(first + ";" + second + ";", read("output_1.csv"));
    }

    @Test
    void rotatesTheFileAfterEachTaskAboveTheLimit() throws Exception
    {
        configure(1L);
        DataFileEncoder encoder = new DataFileEncoder();
        String first = encoder.encodeTask(task(42)).get(0);
        String second = encoder.encodeTask(task(43)).get(0);

        DataFileWriter writer = new DataFileWriter("output");
        writer.process(task(42));
        writer.process(task(43));
        writer.close();

        assertEquals(first + ";", read("output_1.csv"));
        assertEquals(second + ";", read("output_2.csv"));
        assertEquals("", read("output_3.csv"));
    }

    /**
     * Creates a full task holding a single full revision of the given article.
     */
    private static Task<Diff> task(final int articleId) throws Exception
    {
        ArticleInformation header = new ArticleInformation();
        header.setArticleId(articleId);
        header.setArticleName("Article_" + articleId);
        header.setNamespace(0);

        String text = "Some text of article " + articleId;
        DiffPart part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);
        part.setText(text);

        RevisionCodecData codecData = new RevisionCodecData();
        codecData.checkBlocksizeL(text.getBytes(StandardCharsets.UTF_8).length);

        Diff diff = new Diff();
        diff.setRevisionCoutner(1);
        diff.setRevisionID(articleId * 10);
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
