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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dkpro.jwpl.api.hibernate.PageMapLine;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Verifies that the reference DDL indexes {@code Page.name} and {@code PageMapLine.pageID}, which
 * {@link Wikipedia#getCategories(String)} and {@link Wikipedia#existsPage(int)} filter on, and that
 * the entity annotations document the same index names.
 */
public class SchemaIndexTest
{

    @ParameterizedTest
    @ValueSource(strings = { "db/schema-hsqldb.sql", "db/schema-hsqldb-legacy.sql" })
    public void testSchemaIndexesLookupColumns(String schema) throws SQLException
    {
        String url = "jdbc:hsqldb:mem:schema_index_"
                + schema.replaceAll("[^A-Za-z0-9]", "_");
        JwplTestDatabase.provision(url, "sa", "", schema);

        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            assertEquals("NAME", indexedColumns(c, "PAGE").get("PAGE_NAME_INDEX"));
            assertEquals("PAGEID", indexedColumns(c, "PAGEMAPLINE").get("PAGEID_INDEX"));
            assertEquals("NAME", indexedColumns(c, "PAGEMAPLINE").get("NAME_INDEX"));
        }
    }

    @Test
    public void testEntityAnnotationsMatchSchema()
    {
        Index pageIndex = org.dkpro.jwpl.api.hibernate.Page.class.getAnnotation(Table.class)
                .indexes()[0];
        assertEquals("page_name_index", pageIndex.name());
        assertEquals("name", pageIndex.columnList());

        Index[] pageMapLineIndexes = PageMapLine.class.getAnnotation(Table.class).indexes();
        assertTrue(Arrays.stream(pageMapLineIndexes).anyMatch(
                i -> i.name().equals("pageID_index") && i.columnList().equals("pageID")));
    }

    private static Map<String, String> indexedColumns(Connection c, String table)
        throws SQLException
    {
        Map<String, String> result = new HashMap<>();
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet rs = md.getIndexInfo(null, null, table, false, false)) {
            while (rs.next()) {
                String name = rs.getString("INDEX_NAME");
                String column = rs.getString("COLUMN_NAME");
                if (name != null && column != null) {
                    result.put(name.toUpperCase(Locale.ROOT), column.toUpperCase(Locale.ROOT));
                }
            }
        }
        return result;
    }
}
