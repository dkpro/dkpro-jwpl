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
package org.dkpro.jwpl.revisionmachine.api.chrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Iterates the revisions of the page 'Car' in the stripped test data set through a chronological
 * mapping and compares them with the revisions delivered by the {@link RevisionIterator}.
 */
public class ChronoIteratorTest
{

    private static final String DATABASE = "wikiapi_simple_20090119_stripped";

    // Primary keys of the revisions of the page 'Car', the only page in the test data set
    private static final int FIRST_PK = 71500;

    private static final int LAST_PK = 71881;

    // Number of revisions at the end of 'Car' that are delivered in reverse order
    private static final int REVERSED = 40;

    @TempDir
    Path tempDir;

    @Test
    public void testRevisionsAreRebuiltFromCachedRevisions() throws Exception
    {
        Files.copy(Path.of("src/test/resources/db", DATABASE + ".script"),
                tempDir.resolve(DATABASE + ".script"));
        String url = "jdbc:hsqldb:file:" + tempDir.resolve(DATABASE) + ";shutdown=true";
        RevisionAPIConfiguration config = new RevisionAPIConfiguration(
                new DatabaseConfiguration("org.hsqldb.jdbcDriver", url, "localhost", DATABASE,
                        "sa", "", Language.simple_english));
        // Small, so that revisions are rebuilt from the revisions kept in the storage
        config.setChronoStorageSpace(200_000);

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            List<Revision> revisions = new ArrayList<>();
            RevisionIterator revisionIterator = new RevisionIterator(config, FIRST_PK, LAST_PK,
                    connection);
            while (revisionIterator.hasNext()) {
                revisions.add(revisionIterator.next());
            }
            int count = revisions.size();
            assertEquals(LAST_PK - FIRST_PK + 1, count);

            StringBuilder mapping = new StringBuilder();
            for (int revisionCounter = count - REVERSED + 1; revisionCounter <= count;
                    revisionCounter++) {
                if (mapping.length() > 0) {
                    mapping.append(' ');
                }
                mapping.append(revisionCounter).append(' ')
                        .append(2 * count - REVERSED + 1 - revisionCounter);
            }

            List<Revision> expected = new ArrayList<>(revisions.subList(0, count - REVERSED));
            for (int i = count - 1; i >= count - REVERSED; i--) {
                expected.add(revisions.get(i));
            }

            ChronoIterator chronoIterator = new ChronoIterator(config, connection,
                    mapping.toString(), String.valueOf(FIRST_PK), "1 " + count);
            for (Revision expectedRevision : expected) {
                assertTrue(chronoIterator.hasNext());
                Revision revision = chronoIterator.next();
                assertNotNull(revision, "Revision " + expectedRevision.getRevisionCounter());
                assertEquals(expectedRevision.getRevisionCounter(),
                        revision.getRevisionCounter());
                assertEquals(expectedRevision.getRevisionID(), revision.getRevisionID());
                assertEquals(expectedRevision.getRevisionText(), revision.getRevisionText(),
                        "Revision " + expectedRevision.getRevisionCounter());
            }
            assertFalse(chronoIterator.hasNext());
        }
    }
}
