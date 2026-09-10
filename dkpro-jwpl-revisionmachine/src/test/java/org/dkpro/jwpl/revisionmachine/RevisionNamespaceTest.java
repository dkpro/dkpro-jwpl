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
package org.dkpro.jwpl.revisionmachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reads the namespace of revisions from databases with and without the Namespace column. The test
 * data set was created before the column existed, a copy of it gets the column added.
 */
public class RevisionNamespaceTest
    extends BaseJWPLTest
{

    private static final String DATABASE = "wikiapi_simple_20090119_stripped";

    // A revision of the page 'Car', the only page in the stripped test data set
    private static final int REVISION_ID = 1142935;

    @TempDir
    static Path tempDir;

    private static DatabaseConfiguration legacyDb;

    private static DatabaseConfiguration namespaceDb;

    @BeforeAll
    public static void setUpDatabases() throws Exception
    {
        legacyDb = obtainHSDLDBConfiguration(DATABASE, Language.simple_english);

        Files.copy(Path.of("src/test/resources/db", DATABASE + ".script"),
                tempDir.resolve(DATABASE + ".script"));
        String url = "jdbc:hsqldb:file:" + tempDir.resolve(DATABASE) + ";shutdown=true";
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE revisions ADD COLUMN Namespace INTEGER");
            statement.execute("UPDATE revisions SET Namespace = 0");
        }
        namespaceDb = new DatabaseConfiguration("org.hsqldb.jdbcDriver", url, "localhost",
                DATABASE, "sa", "", Language.simple_english);
    }

    @Test
    public void testRevisionApiReadsNamespace() throws Exception
    {
        RevisionApi revisionApi = new RevisionApi(namespaceDb);
        try {
            assertEquals(Integer.valueOf(0), revisionApi.getRevision(REVISION_ID).getNamespace());
        }
        finally {
            revisionApi.close();
        }
    }

    @Test
    public void testRevisionApiWithoutNamespaceColumn() throws Exception
    {
        RevisionApi revisionApi = new RevisionApi(legacyDb);
        try {
            Revision revision = revisionApi.getRevision(REVISION_ID);
            assertEquals(REVISION_ID, revision.getRevisionID());
            assertNull(revision.getNamespace());
        }
        finally {
            revisionApi.close();
        }
    }

    @Test
    public void testRevisionIteratorReadsNamespace() throws Exception
    {
        RevisionIterator revisionIterator = new RevisionIterator(
                new RevisionAPIConfiguration(namespaceDb));
        try {
            assertTrue(revisionIterator.hasNext());
            assertEquals(Integer.valueOf(0), revisionIterator.next().getNamespace());
        }
        finally {
            revisionIterator.close();
        }
    }

    @Test
    public void testRevisionIteratorWithoutNamespaceColumn() throws Exception
    {
        RevisionIterator revisionIterator = new RevisionIterator(
                new RevisionAPIConfiguration(legacyDb));
        try {
            assertTrue(revisionIterator.hasNext());
            assertNull(revisionIterator.next().getNamespace());
        }
        finally {
            revisionIterator.close();
        }
    }
}
