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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;

/**
 * Test fixture for the chronological iteration over the revisions of the page 'Car', the only
 * page in the stripped test data set.
 */
public final class ChronoTestData
{

    public static final String DATABASE = "wikiapi_simple_20090119_stripped";

    // Primary keys of the revisions of the page 'Car'
    public static final int FIRST_PK = 71500;

    public static final int LAST_PK = 71881;

    // Number of revisions of the page 'Car'
    public static final int REVISIONS = LAST_PK - FIRST_PK + 1;

    private ChronoTestData()
    {
    }

    /**
     * Copies the test data set into the given directory.
     *
     * @return the JDBC URL of the copy
     */
    public static String copyDatabase(final Path dir) throws Exception
    {
        Files.copy(Path.of("src/test/resources/db", DATABASE + ".script"),
                dir.resolve(DATABASE + ".script"));
        return "jdbc:hsqldb:file:" + dir.resolve(DATABASE) + ";shutdown=true";
    }

    /**
     * @return a configuration for the copy of the test data set at the given JDBC URL
     */
    public static RevisionAPIConfiguration configuration(final String url)
    {
        return new RevisionAPIConfiguration(new DatabaseConfiguration("org.hsqldb.jdbcDriver",
                url, "localhost", DATABASE, "sa", "", Language.simple_english));
    }

    /**
     * Retrieves the revisions of the page 'Car' in order with the {@link RevisionIterator}, as
     * baseline for the chronological iteration.
     *
     * @return the revisions of the page 'Car'
     */
    public static List<Revision> revisionsOfCar(final RevisionAPIConfiguration config,
            final String url)
        throws Exception
    {
        List<Revision> revisions = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            RevisionIterator revisionIterator = new RevisionIterator(config, FIRST_PK, LAST_PK,
                    connection);
            while (revisionIterator.hasNext()) {
                revisions.add(revisionIterator.next());
            }
        }
        assertEquals(REVISIONS, revisions.size());
        return revisions;
    }

    /**
     * Builds a chronological mapping that delivers the last {@code reversed} revisions of the
     * given revisions in reverse order.
     *
     * @return the mapping, as stored in the chronological index
     */
    public static String reversedMapping(final int revisions, final int reversed)
    {
        StringBuilder mapping = new StringBuilder();
        for (int revisionCounter = revisions - reversed + 1; revisionCounter <= revisions;
                revisionCounter++) {
            if (mapping.length() > 0) {
                mapping.append(' ');
            }
            mapping.append(revisionCounter).append(' ')
                    .append(2 * revisions - reversed + 1 - revisionCounter);
        }
        return mapping.toString();
    }

    /**
     * Returns the given revisions with the last {@code reversed} revisions in reverse order.
     */
    public static <T> List<T> reversedTail(final List<T> revisions, final int reversed)
    {
        int count = revisions.size();
        List<T> expected = new ArrayList<>(revisions.subList(0, count - reversed));
        for (int i = count - 1; i >= count - reversed; i--) {
            expected.add(revisions.get(i));
        }
        return expected;
    }
}
