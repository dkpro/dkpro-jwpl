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
package org.dkpro.jwpl.revisionmachine.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class IndexIteratorTest
{

    // Inserted out of primary key order on purpose, so that the physical order differs.
    private static final int[] PRIMARY_KEYS = { 7, 3, 12, 1, 9, 5, 11, 2, 8, 4, 10, 6 };

    private static Connection createDatabase() throws SQLException
    {
        Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";sql.syntax_mys=true", "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE revisions (PrimaryKey INT NOT NULL,"
                    + " RevisionCounter INT NOT NULL, RevisionID INT NOT NULL,"
                    + " ArticleID INT NOT NULL, Timestamp BIGINT NOT NULL,"
                    + " FullRevisionID INT NOT NULL, PRIMARY KEY (PrimaryKey))");
        }
        try (PreparedStatement insert = connection
                .prepareStatement("INSERT INTO revisions VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int pk : PRIMARY_KEYS) {
                insert.setInt(1, pk);
                insert.setInt(2, pk);
                insert.setInt(3, 100 + pk);
                insert.setInt(4, 1000 + pk);
                insert.setLong(5, 10000L + pk);
                insert.setInt(6, 1);
                insert.executeUpdate();
            }
        }
        return connection;
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 4, 5, 12, 100 })
    public void testVisitsEveryRowOnceInPrimaryKeyOrder(int bufferSize) throws SQLException
    {
        Connection connection = createDatabase();
        List<Integer> visited = new ArrayList<>();
        try (IndexIterator iterator = new IndexIterator(connection, bufferSize)) {
            while (iterator.hasNext()) {
                Revision revision = iterator.next();
                assertEquals(100 + revision.getPrimaryKey(), revision.getRevisionID());
                assertEquals(1000 + revision.getPrimaryKey(), revision.getArticleID());
                visited.add(revision.getPrimaryKey());
            }
            assertFalse(iterator.hasNext());
        }

        List<Integer> expected = new ArrayList<>();
        for (int pk = 1; pk <= PRIMARY_KEYS.length; pk++) {
            expected.add(pk);
        }
        assertEquals(expected, visited);
        assertTrue(connection.isClosed());
    }
}
