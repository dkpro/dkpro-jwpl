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
package org.dkpro.jwpl.revisionmachine.index.writer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SQLFileWriterTest
{

    @TempDir
    Path outputDir;

    @Test
    public void testFinishCreatesRevisionIndexes() throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setOutputPath(outputDir.toString());

        SQLFileWriter writer = new SQLFileWriter(config);
        writer.finish();
        writer.close();

        String sql = Files.readString(outputDir.resolve("revisionIndex.sql"));
        assertTrue(sql.contains("CREATE INDEX articleIdx ON revisions(ArticleID);"), sql);
        assertTrue(sql.contains(
                "CREATE INDEX articleTsIdx ON revisions(ArticleID, Timestamp, RevisionCounter);"),
                sql);
    }
}
