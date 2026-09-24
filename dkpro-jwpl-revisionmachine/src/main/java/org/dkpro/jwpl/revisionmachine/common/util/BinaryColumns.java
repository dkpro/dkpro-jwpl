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
package org.dkpro.jwpl.revisionmachine.common.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Detects whether the {@code Revision} column of a result holds the binary encoded diffs (as
 * written by the binary mode of the DiffTool database output) or the base 64 encoded ones.
 */
public final class BinaryColumns
{

    private BinaryColumns()
    {
    }

    /**
     * Returns whether the given SQL type is a binary one. JDBC drivers report a {@code MEDIUMBLOB}
     * column as {@link Types#LONGVARBINARY}, but depending on the driver and the server a binary
     * column may also be reported as {@link Types#VARBINARY}, {@link Types#BINARY} or
     * {@link Types#BLOB}.
     *
     * @param sqlType
     *            SQL type from {@link java.sql.Types}
     * @return {@code true} if the type is binary
     */
    public static boolean isBinary(final int sqlType)
    {
        switch (sqlType) {
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
        case Types.BINARY:
        case Types.BLOB:
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns whether the given column of a result is a binary one.
     *
     * @param metaData
     *            meta data of the result
     * @param column
     *            index of the column, starting at 1
     * @return {@code true} if the column is binary
     * @throws SQLException
     *             if the meta data cannot be accessed
     * @see #isBinary(int)
     */
    public static boolean isBinary(final ResultSetMetaData metaData, final int column)
        throws SQLException
    {
        return isBinary(metaData.getColumnType(column));
    }
}
