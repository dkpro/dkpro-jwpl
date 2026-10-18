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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.testdb.ColumnProfile;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Pins the collations the name lookup tests rely on: the server default the shared fixture gets,
 * and the collation of every variant database. An image bump that changes a default then fails
 * here, instead of silently changing what the other tests cover.
 */
public class SchemaAssumptionTest
    extends BaseJWPLTest
{
    @TestFactory
    Stream<DynamicTest> collations()
    {
        Engine engine = JwplTestDatabase.selectEngine();
        Stream<DynamicTest> shared = Stream.of(dynamicTest("[" + engine + "] shared fixture",
                () -> assertCollations(engine, obtainDbConfiguration(),
                        ColumnProfile.DEFAULT.expectedCollation(engine))));
        Stream<DynamicTest> variants = ColumnProfile.forEngine(engine).stream()
                .map(p -> dynamicTest("[" + engine + "/" + p + "]",
                        () -> assertCollations(engine, JwplTestDatabase.provisionVariant(p),
                                p.expectedCollation(engine))));
        return Stream.concat(shared, variants);
    }

    private static void assertCollations(Engine engine, DatabaseConfiguration db,
            String expected)
        throws SQLException
    {
        try (Connection c = JwplTestDatabase.rawConnection(db)) {
            for (String table : new String[] { "PageMapLine", "Page", "Category" }) {
                assertEquals(expected, collation(engine, c, db.getDatabase(), table),
                        db.getDatabase() + "." + table + ".name");
            }
        }
    }

    private static String collation(Engine engine, Connection c, String database, String table)
        throws SQLException
    {
        String sql = switch (engine) {
            case MARIADB, MYSQL -> "SELECT COLLATION_NAME FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = 'name'";
            case POSTGRESQL -> "SELECT CASE WHEN co.collname = 'default'"
                    + " THEN (SELECT datcollate FROM pg_database WHERE datname = ?)"
                    + " ELSE co.collname END FROM pg_attribute a"
                    + " JOIN pg_class t ON a.attrelid = t.oid"
                    + " JOIN pg_collation co ON a.attcollation = co.oid"
                    + " WHERE t.relname = ? AND a.attname = 'name'";
            case HSQLDB -> "SELECT COLLATION_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                    + " WHERE ? IS NOT NULL AND TABLE_NAME = ? AND COLUMN_NAME = 'NAME'";
        };
        String tableName = switch (engine) {
            case MARIADB, MYSQL -> table;
            case POSTGRESQL -> table.toLowerCase(Locale.ROOT);
            case HSQLDB -> table.toUpperCase(Locale.ROOT);
        };
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, database);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
}
