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
package org.dkpro.jwpl.api.testdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that a name lookup reads the rows it returns through an index instead of scanning the
 * table. Each engine reports plans differently, so every engine has its own probe, see
 * {@link #forEngine(JwplTestDatabase.Engine)}.
 */
public interface PlanProbe
{
    /** The outcome of a probe. */
    enum Status
    {
        PASSED, FAILED, SKIPPED
    }

    /**
     * The outcome of a probe.
     *
     * @param status The status.
     * @param detail What was measured, for the test report.
     */
    record Result(Status status, String detail)
    {
    }

    /**
     * Probes the plan of {@code sql}, binding {@code value} to every parameter.
     *
     * @param c     A privileged connection to the database, see
     *              {@link JwplTestDatabase#privilegedConnection(org.dkpro.jwpl.api.DatabaseConfiguration)}.
     * @param sql   The statement with {@code ?} parameters.
     * @param value The value bound to every parameter.
     * @return The outcome.
     * @throws SQLException If the database fails in an unexpected way.
     */
    Result probe(Connection c, String sql, String value) throws SQLException;

    /**
     * The switch has no default branch on purpose: a new engine does not compile until it has a
     * probe.
     *
     * @param engine The engine under test.
     * @return The probe for {@code engine}.
     */
    static PlanProbe forEngine(JwplTestDatabase.Engine engine)
    {
        return switch (engine) {
            case HSQLDB -> (c, sql, value) -> new Result(Status.SKIPPED,
                    "HSQLDB exposes no plan format the probe understands");
            case MARIADB, MYSQL -> new MySqlFamilyPlanProbe();
            case POSTGRESQL -> new PostgresPlanProbe();
        };
    }

    /**
     * @param e An exception thrown by a probe statement.
     * @return Whether MySQL or MariaDB refused to compare the collations of the column and the
     *         value, in which case there is no plan.
     */
    static boolean isCollationMismatch(SQLException e)
    {
        return Set.of(1267, 1270, 1271).contains(e.getErrorCode());
    }

    /**
     * Binds {@code value} to all parameters of {@code ps}.
     *
     * @param ps    The statement.
     * @param sql   Its SQL.
     * @param value The value.
     * @throws SQLException If binding fails.
     */
    static void bindAll(PreparedStatement ps, String sql, String value) throws SQLException
    {
        long parameters = sql.chars().filter(ch -> ch == '?').count();
        for (int i = 1; i <= parameters; i++) {
            ps.setString(i, value);
        }
    }

    /**
     * Counts the rows the handler reads while running the statement, which covers every access
     * path, including those EXPLAIN reports optimistically, and checks the access type EXPLAIN
     * reports.
     */
    final class MySqlFamilyPlanProbe
        implements PlanProbe
    {
        private static final Set<String> INDEX_TYPES = Set.of("ref", "eq_ref", "const", "range");
        private static final Set<String> NAME_INDEXES = Set.of("name_index", "nameIndex",
                "page_name_index");

        @Override
        public Result probe(Connection c, String sql, String value) throws SQLException
        {
            String type;
            String key;
            try (PreparedStatement ps = c.prepareStatement("EXPLAIN " + sql)) {
                bindAll(ps, sql, value);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    type = rs.getString("type");
                    key = rs.getString("key");
                }
            }
            catch (SQLException e) {
                if (isCollationMismatch(e)) {
                    return new Result(Status.SKIPPED, "no plan: " + e.getMessage());
                }
                throw e;
            }

            try (Statement s = c.createStatement()) {
                s.execute("FLUSH STATUS");
            }
            long before = handlerReads(c);
            long overhead = handlerReads(c) - before;
            long start = handlerReads(c);
            int rows = 0;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bindAll(ps, sql, value);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows++;
                    }
                }
            }
            long reads = handlerReads(c) - start - overhead;

            String detail = "type=" + type + ", key=" + key + ", rows=" + rows + ", handlerReads="
                    + reads;
            boolean indexed = INDEX_TYPES.contains(type) && NAME_INDEXES.contains(key);
            return new Result(indexed && reads <= rows + 3 ? Status.PASSED : Status.FAILED,
                    detail);
        }

        private static long handlerReads(Connection c) throws SQLException
        {
            long sum = 0;
            try (Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery("SHOW SESSION STATUS WHERE Variable_name IN"
                            + " ('Handler_read_key', 'Handler_read_next',"
                            + " 'Handler_read_rnd_next', 'Handler_read_first',"
                            + " 'Handler_read_prev', 'Handler_read_rnd')")) {
                while (rs.next()) {
                    sum += rs.getLong(2);
                }
            }
            return sum;
        }
    }

    /**
     * Runs {@code EXPLAIN (ANALYZE, FORMAT JSON)} and rejects sequential scans and index scans
     * that filter out more than a couple of rows.
     */
    final class PostgresPlanProbe
        implements PlanProbe
    {
        private static final Pattern REMOVED = Pattern
                .compile("\"Rows Removed by (?:Filter|Index Recheck)\":\\s*(\\d+)");

        @Override
        public Result probe(Connection c, String sql, String value) throws SQLException
        {
            StringBuilder plan = new StringBuilder();
            try (PreparedStatement ps = c
                    .prepareStatement("EXPLAIN (ANALYZE, FORMAT JSON) " + sql)) {
                bindAll(ps, sql, value);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        plan.append(rs.getString(1));
                    }
                }
            }
            String json = plan.toString();
            long removed = 0;
            Matcher m = REMOVED.matcher(json);
            while (m.find()) {
                removed += Long.parseLong(m.group(1));
            }
            boolean seqScan = json.contains("\"Seq Scan\"");
            Matcher node = Pattern.compile("\"Node Type\":\\s*\"([^\"]+)\"").matcher(json);
            StringBuilder nodes = new StringBuilder();
            while (node.find()) {
                nodes.append(nodes.length() == 0 ? "" : ",").append(node.group(1));
            }
            String detail = "nodes=" + nodes + ", rowsRemoved=" + removed;
            return new Result(!seqScan && removed <= 2 ? Status.PASSED : Status.FAILED, detail);
        }
    }
}
