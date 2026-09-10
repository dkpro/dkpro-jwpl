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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the columns of the {@code revisions} table that not every RevisionMachine database has.
 */
final class RevisionsTable
{

    /**
     * Name of the column that holds the namespace of the page a revision belongs to. Databases
     * created by RevisionMachine versions before 2.2.0 do not have it.
     */
    static final String NAMESPACE_COLUMN = "Namespace";

    private RevisionsTable()
    {
    }

    /**
     * Checks whether the {@code revisions} table has a {@value #NAMESPACE_COLUMN} column.
     *
     * @param connection
     *            the connection to the RevisionMachine database
     * @return {@code true} if the column exists, {@code false} otherwise
     * @throws SQLException
     *             if an error occurs while querying the database
     */
    static boolean hasNamespaceColumn(final Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM revisions WHERE 1 = 0")) {
            ResultSetMetaData metaData = result.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (NAMESPACE_COLUMN.equalsIgnoreCase(metaData.getColumnName(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Reads the namespace from the given column of the current row.
     *
     * @param result
     *            the result set, positioned on a row
     * @param columnIndex
     *            the index of the {@value #NAMESPACE_COLUMN} column in the result set
     * @return the namespace, or {@code null} if it was not recorded for this revision
     * @throws SQLException
     *             if the column cannot be read
     */
    static Integer getNamespace(final ResultSet result, final int columnIndex) throws SQLException
    {
        int namespace = result.getInt(columnIndex);
        return result.wasNull() ? null : namespace;
    }
}
