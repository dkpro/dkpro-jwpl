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
package org.dkpro.jwpl.util.templates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;

/**
 * An in-memory HSQLDB database for the query tests of {@link WikipediaTemplateInfo}. It holds
 * the template name table ({@code templates}) with the templates
 * <ol>
 * <li>{@code infobox_city}</li>
 * <li>{@code infobox_person}</li>
 * <li>{@code cite_web}</li>
 * <li>{@code stub}</li>
 * </ol>
 * where the number is the template id. The template index tables and any further tables are
 * created by the tests.
 */
final class TemplateIndexTestDatabase
    implements AutoCloseable
{
    private final Connection connection;

    private TemplateIndexTestDatabase(Connection connection)
    {
        this.connection = connection;
    }

    /**
     * Opens a new in-memory database holding the template name table.
     *
     * @param name
     *            the name of the database, unique per test class
     * @return the database
     * @throws SQLException
     *             If the database could not be created
     */
    static TemplateIndexTestDatabase open(String name) throws SQLException
    {
        TemplateIndexTestDatabase database = new TemplateIndexTestDatabase(
                DriverManager.getConnection("jdbc:hsqldb:mem:" + name, "sa", ""));
        database.execute(
                "CREATE TABLE " + GeneratorConstants.TABLE_TPLID_TPLNAME
                        + " (templateId INTEGER NOT NULL PRIMARY KEY,"
                        + " templateName VARCHAR(255) NOT NULL)",
                "INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                        + " VALUES (1, 'infobox_city'), (2, 'infobox_person'), (3, 'cite_web'),"
                        + " (4, 'stub')");
        return database;
    }

    /**
     * Creates a template index table the way the template info generator does, with a
     * {@code NOT NULL} template id and page or revision id, and fills it.
     *
     * @param table
     *            the name of the index table, e.g. {@link GeneratorConstants#TABLE_TPLID_PAGEID}
     * @param idColumn
     *            the name of the page or revision id column
     * @param rows
     *            the rows of the index, each a pair of template id and page or revision id
     * @throws SQLException
     *             If the table could not be created or filled
     */
    void createIndex(String table, String idColumn, int[]... rows) throws SQLException
    {
        execute("CREATE TABLE " + table + " (templateId INTEGER NOT NULL, " + idColumn
                + " INTEGER NOT NULL, UNIQUE (templateId, " + idColumn + "))");
        try (PreparedStatement statement = connection
                .prepareStatement("INSERT INTO " + table + " VALUES (?, ?)")) {
            for (int[] row : rows) {
                statement.setInt(1, row[0]);
                statement.setInt(2, row[1]);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Executes the given statements.
     *
     * @param sql
     *            the statements, without parameters
     * @throws SQLException
     *             If a statement failed
     */
    void execute(String... sql) throws SQLException
    {
        try (Statement statement = connection.createStatement()) {
            for (String single : sql) {
                statement.execute(single);
            }
        }
    }

    /**
     * Runs a query built by {@link WikipediaTemplateInfo} with the given template names bound by
     * {@link WikipediaTemplateInfo#bindTemplateNames(PreparedStatement, List, boolean)}.
     *
     * @param sql
     *            the query
     * @param names
     *            the template names or fragments to bind
     * @param prefix
     *            whether the names are bound as prefix patterns
     * @return the integer values of the first column, in the order of the result
     * @throws SQLException
     *             If the query failed
     */
    List<Integer> queryInts(String sql, List<String> names, boolean prefix) throws SQLException
    {
        List<Integer> values = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            WikipediaTemplateInfo.bindTemplateNames(statement, names, prefix);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    values.add(result.getInt(1));
                }
            }
        }
        return values;
    }

    @Override
    public void close() throws SQLException
    {
        execute("SHUTDOWN");
        connection.close();
    }
}
