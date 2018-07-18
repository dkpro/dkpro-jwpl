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
package de.tudarmstadt.ukp.wikipedia.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbUtilities {

    private Connection conn;

	private final Log logger = LogFactory.getLog(getClass());

    public DbUtilities(Connection conn) {
        this.conn = conn;
    }

    public boolean tableExists(String tableName) {

        try {
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = {"TABLE"};

            // get all table names
            ResultSet resultSet = dbmd.getTables(null, null, "%", types);

            while (resultSet.next()) {
                if (resultSet.getString("TABLE_NAME").equals(tableName)) {
                    return true;
                }
            }
        }
        catch (SQLException e) {
            logger.error("Table " + tableName + " does not exist.", new Throwable() );
        }

        return false;
    }

}
