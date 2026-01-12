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
package org.dkpro.jwpl.api.util;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides extra method(s) for working directly with a database.
 *
 * @deprecated To be removed without replacement.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class DbUtilities
{

    private final Connection conn;

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Instantiates a {@link DbUtilities} object.
     *
     * @param conn A valid {@link Connection} instance. Must not be {@code null}.
     */
    public DbUtilities(Connection conn)
    {
        this.conn = conn;
    }

    /**
     * Checks if a table identified by {@code tableName} exists on the currently connected database.
     *
     * @param tableName The name of the table to check for. Must not be {@code null}.
     * @return {@code True} if it exists, {@code false} otherwise.
     */
    public boolean tableExists(String tableName)
    {

        try {
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };

            // get all table names
            ResultSet resultSet = dbmd.getTables(null, null, "%", types);

            while (resultSet.next()) {
                if (resultSet.getString("TABLE_NAME").equals(tableName)) {
                    return true;
                }
            }
        }
        catch (SQLException e) {
            logger.error("Table {} does not exist.", tableName, new RuntimeException(e));
        }

        return false;
    }

}
