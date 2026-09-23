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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Verifies that the exact, case-sensitive name lookups of {@link Wikipedia#existsPage(String)} and
 * {@link Category#Category(Wikipedia, String)} can use the name indexes on MySQL and MariaDB. An
 * explicit collation in the comparison, e.g. {@code COLLATE utf8mb4_bin}, would force a full scan
 * instead.
 */
@EnabledIf("isMySqlFamily")
public class NameLookupIndexTest
    extends BaseJWPLTest
{

    static boolean isMySqlFamily()
    {
        return JwplTestDatabase.selectEngine() != JwplTestDatabase.Engine.HSQLDB;
    }

    @Test
    public void testExistsPageQueryUsesNameIndex() throws SQLException
    {
        assertIndexLookup(Wikipedia.PAGE_NAMES_BY_NAME_QUERY,
                "Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval",
                "name_index");
    }

    @Test
    public void testCategoryQueryUsesNameIndex() throws SQLException
    {
        assertIndexLookup(Category.CATEGORY_BY_NAME_QUERY, "People_of_UKP", "nameIndex");
    }

    private static void assertIndexLookup(String query, String name, String index)
        throws SQLException
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        String sql = "EXPLAIN " + query.replaceAll(":\\w+", "?");
        try (Connection c = DriverManager.getConnection(db.getJdbcURL(), db.getUser(),
                db.getPassword());
                PreparedStatement ps = c.prepareStatement(sql)) {
            long parameters = sql.chars().filter(ch -> ch == '?').count();
            for (int i = 1; i <= parameters; i++) {
                ps.setString(i, name);
            }
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "EXPLAIN returned no plan for: " + sql);
                assertEquals("ref", rs.getString("type"), sql);
                assertEquals(index, rs.getString("key"), sql);
            }
        }
    }
}
