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

import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.MARIADB;
import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.MYSQL;
import static org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine.POSTGRESQL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.testdb.CapturingStatementInspector;
import org.dkpro.jwpl.api.testdb.ColumnProfile;
import org.dkpro.jwpl.api.testdb.EnabledOnEngines;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine;
import org.dkpro.jwpl.api.testdb.PlanProbe;
import org.dkpro.jwpl.api.testdb.PlanProbe.Result;
import org.dkpro.jwpl.api.testdb.PlanProbe.Status;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

/**
 * C5: the name lookups read their rows through the name index on every profile of the column,
 * instead of scanning the table. The plan is taken of the SQL Hibernate really issued for an API
 * call, see {@link CapturingStatementInspector}.
 * <p>
 * A control row runs the query of JWPL 2.3.0, which forced {@code COLLATE utf8mb4_bin}, and
 * expects it to fail where it is known to scan: on MariaDB with {@code utf8mb3} or {@code latin1}
 * columns. That shows the probe tells the two apart. Elsewhere the old query used the index too
 * (MySQL 8.4 plans a range scan), and the control row is only reported.
 */
@EnabledOnEngines({ MARIADB, MYSQL, POSTGRESQL })
public class NameLookupPlanTest
    extends BaseJWPLTest
{
    /** The query of {@code existsPage(String)} in JWPL 2.3.0. */
    static final String OLD_EXISTS_PAGE_QUERY = "select p.id from PageMapLine as p"
            + " where p.name = ? COLLATE utf8mb4_bin";

    private static final Pattern NAME_COMPARISON = Pattern.compile("(?i)\\bname\\s*=\\s*\\?");

    private static final List<String> PROBES = List.of("Mixed_Case",
            "Emoji_😁_probe");

    /** An API call issuing a name lookup. */
    private interface Call
    {
        void run(Wikipedia wiki, String probe) throws WikiApiException;
    }

    @TestFactory
    Stream<DynamicNode> plans()
    {
        Engine engine = JwplTestDatabase.selectEngine();
        PlanProbe probe = PlanProbe.forEngine(engine);
        return ColumnProfile.forEngine(engine).stream().map(profile -> {
            List<DynamicNode> tests = new ArrayList<>();
            for (String p : PROBES) {
                tests.add(planTest(engine, profile, probe, "existsPage", p,
                        (w, t) -> w.existsPage(t)));
                tests.add(planTest(engine, profile, probe, "getCategory", p,
                        (w, t) -> w.getCategory(t)));
                tests.add(planTest(engine, profile, probe, "getPage", p,
                        (w, t) -> w.getPage(t)));
            }
            if (engine != POSTGRESQL) {
                tests.add(dynamicTest("[" + engine + "/" + profile + "] control: 2.3.0 query",
                        () -> {
                            Result r = run(profile, probe, OLD_EXISTS_PAGE_QUERY, "Mixed_Case");
                            System.out.println("PLAN " + engine + "/" + profile
                                    + " control(COLLATE utf8mb4_bin) " + r);
                            if (engine == MARIADB && (profile == ColumnProfile.MB3_GENERAL_CI
                                    || profile == ColumnProfile.LATIN1_SWEDISH_CI)) {
                                assertEquals(Status.FAILED, r.status(),
                                        "the probe must reject the known full scan: " + r);
                            }
                        }));
            }
            return dynamicContainer("[" + engine + "/" + profile + "]", tests);
        });
    }

    private static DynamicNode planTest(Engine engine, ColumnProfile profile, PlanProbe probe,
            String name, String title, Call call)
    {
        return dynamicTest("[" + engine + "/" + profile + "] " + name + " "
                + NameLookupSpec.escape(title), () -> {
                    Wikipedia wiki = new Wikipedia(JwplTestDatabase.provisionVariant(profile));
                    CapturingStatementInspector.clear();
                    try {
                        call.run(wiki, title);
                    }
                    catch (WikiApiException | RuntimeException e) {
                        // only the statement matters here; the contract test checks outcomes
                    }
                    String sql = CapturingStatementInspector.captured().stream()
                            .filter(s -> NAME_COMPARISON.matcher(s).find()).findFirst()
                            .orElseThrow(() -> new AssertionError("No name lookup issued by "
                                    + name + ": " + CapturingStatementInspector.captured()));
                    Result r = run(profile, probe, sql, NameLookupOracle.normalize(title));
                    System.out.println("PLAN " + engine + "/" + profile + " " + name + " "
                            + NameLookupSpec.escape(title) + " " + r + " sql=" + sql);
                    assertNotEquals(Status.FAILED, r.status(), sql + ": " + r.detail());
                });
    }

    private static Result run(ColumnProfile profile, PlanProbe probe, String sql, String value)
        throws Exception
    {
        try (Connection c = JwplTestDatabase
                .privilegedConnection(JwplTestDatabase.provisionVariant(profile))) {
            return probe.probe(c, sql, value);
        }
    }
}
