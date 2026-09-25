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

import static org.dkpro.jwpl.revisionmachine.api.ChronoTestData.FIRST_PK;
import static org.dkpro.jwpl.revisionmachine.api.ChronoTestData.REVISIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.api.ChronoTestData;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Iterates the revisions of the page 'Car' in the stripped test data set through a chronological
 * mapping and compares them with the revisions delivered by the {@link RevisionIterator}.
 */
public class ChronoIteratorTest
{

    @TempDir
    static Path tempDir;

    private static String url;

    private static List<Revision> revisionsOfCar;

    @BeforeAll
    public static void setUpDatabase() throws Exception
    {
        url = ChronoTestData.copyDatabase(tempDir);
        revisionsOfCar = ChronoTestData.revisionsOfCar(ChronoTestData.configuration(url), url);
    }

    /**
     * Delivers the last {@code reversed} revisions of 'Car' in reverse order. With a small storage
     * space, revisions are rebuilt from the revisions kept in the storage.
     */
    @ParameterizedTest(name = "storage space {0}, reversed {1}")
    @CsvSource({ "20000, 1", "20000, 40", "20000, 382", "200000, 1", "200000, 40",
            "200000, 382", "9223372036854775807, 1", "9223372036854775807, 40",
            "9223372036854775807, 382" })
    public void testRevisionsAreRebuiltFromStoredRevisions(final long storageSpace,
            final int reversed)
        throws Exception
    {
        RevisionAPIConfiguration config = ChronoTestData.configuration(url);
        config.setChronoStorageSpace(storageSpace);

        List<Revision> expected = ChronoTestData.reversedTail(revisionsOfCar, reversed);
        String mapping = ChronoTestData.reversedMapping(REVISIONS, reversed);

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            ChronoIterator chronoIterator = new ChronoIterator(config, connection, mapping,
                    String.valueOf(FIRST_PK), "1 " + REVISIONS);
            try {
                for (Revision expectedRevision : expected) {
                    assertTrue(chronoIterator.hasNext());
                    Revision revision = chronoIterator.next();
                    assertNotNull(revision, "Revision " + expectedRevision.getRevisionCounter());
                    assertSame(Revision.class, revision.getClass());
                    assertEquals(expectedRevision.getRevisionCounter(),
                            revision.getRevisionCounter());
                    assertEquals(expectedRevision.getRevisionID(), revision.getRevisionID());
                    assertEquals(expectedRevision.getRevisionText(), revision.getRevisionText(),
                            "Revision " + expectedRevision.getRevisionCounter());
                }
                assertFalse(chronoIterator.hasNext());
            }
            finally {
                chronoIterator.close();
            }
        }
    }
}
