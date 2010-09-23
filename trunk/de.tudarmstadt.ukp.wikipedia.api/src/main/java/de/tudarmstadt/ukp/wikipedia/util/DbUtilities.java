/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.util;

import java.sql.*;

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
