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

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(sql.contains("ALTER TABLE revisions ENABLE KEYS;"), sql);
        assertTrue(sql.contains("'CREATE INDEX articleIdx ON revisions(ArticleID, RevisionCounter)'"),
                sql);
        assertTrue(sql.contains(
                "'CREATE INDEX articleTsIdx ON revisions(ArticleID, Timestamp, RevisionCounter)'"),
                sql);
        assertTrue(sql.indexOf("ALTER TABLE revisions ENABLE KEYS;") < sql
                .indexOf("CREATE INDEX articleTsIdx"), sql);
    }

    @Test
    public void testRevisionIndexKeyIsAddedAfterTheLoad() throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setOutputPath(outputDir.toString());

        SQLFileWriter writer = new SQLFileWriter(config);
        String header = Files.readString(outputDir.resolve("revisionIndex.sql"));
        writer.finish();
        writer.close();

        String sql = Files.readString(outputDir.resolve("revisionIndex.sql"));
        assertTrue(header.contains("CREATE TABLE index_revisionID (RevisionID INTEGER UNSIGNED"
                + " NOT NULL, RevisionPK INTEGER UNSIGNED NOT NULL,"
                + " FullRevisionPK INTEGER UNSIGNED NOT NULL);"), header);
        assertFalse(header.contains("PRIMARY KEY(RevisionID)"), header);
        assertTrue(sql.trim().endsWith("ALTER TABLE index_revisionID ADD PRIMARY KEY (RevisionID);"),
                sql);
        // DISABLE/ENABLE KEYS never cover a primary key, so they are not written for the index tables
        assertFalse(sql.contains("DISABLE KEYS"), sql);
        assertFalse(sql.contains("ALTER TABLE index_articleID_rc_ts"), sql);
        assertFalse(sql.contains("ALTER TABLE index_chronological"), sql);
    }
}
