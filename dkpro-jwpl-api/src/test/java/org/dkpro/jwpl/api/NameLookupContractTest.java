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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.NameLookupSpec.Case;
import org.dkpro.jwpl.api.NameLookupSpec.Divergence;
import org.dkpro.jwpl.api.NameLookupSpec.Lookup;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiInitializationException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.api.testdb.ColumnProfile;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the name lookup contract of {@code contract/name-lookups.tsv} against a database of the
 * selected engine for every {@link ColumnProfile} of that engine. An outcome that differs from the
 * contract fails unless {@code contract/name-lookups-divergences.tsv} records exactly that
 * outcome; a recorded divergence that no longer occurs fails as stale.
 */
public class NameLookupContractTest
    extends BaseJWPLTest
{
    private static final Map<ColumnProfile, Wikipedia> WIKIS = new ConcurrentHashMap<>();

    @TestFactory
    Stream<DynamicNode> contract()
    {
        Engine engine = JwplTestDatabase.selectEngine();
        List<Case> cases = NameLookupSpec.cases();
        List<Divergence> divergences = NameLookupSpec.divergences();
        return ColumnProfile.forEngine(engine).stream().map(profile -> dynamicContainer(
                "[" + engine + "/" + profile + "]",
                cases.stream().map(c -> dynamicTest(
                        "[" + engine + "/" + profile + "] " + c.lookup() + " " + c.caseId(),
                        () -> check(engine, profile, c, divergences)))));
    }

    private static void check(Engine engine, ColumnProfile profile, Case c,
            List<Divergence> divergences)
        throws Exception
    {
        Wikipedia wiki = wiki(profile);
        String expected = NameLookupOracle.expected(c.lookup(), c.probe(), profile::canStore);
        String observed = invoke(wiki, c);
        String record = c.caseId() + "\t" + engine + "\t" + profile + "\t"
                + NameLookupSpec.escape(observed) + "\t#NEW-";
        Optional<Divergence> divergence = NameLookupSpec.divergence(divergences, c.caseId(),
                engine, profile);
        if (divergence.isPresent()) {
            if (observed.equals(expected)) {
                fail("Stale divergence, remove it (" + divergence.get().issue() + "): "
                        + c.caseId() + " now yields the expected " + expected);
            }
            assertEquals(divergence.get().observed(), observed,
                    "Divergence " + divergence.get().issue() + " changed; record: " + record);
        }
        else {
            String contract = observed.startsWith("EXC:") ? "C0 violated" : c.why();
            assertEquals(expected, observed, "Probe '" + NameLookupSpec.escape(c.probe())
                    + "': " + contract + ". To accept it, record: " + record);
        }
        checkWithinCollation(profile, c, observed);
    }

    /**
     * C6: the collation-following lookups return a subset of what the collation of the column
     * matches, and nothing less than the exact matches.
     */
    private static void checkWithinCollation(ColumnProfile profile, Case c, String observed)
        throws SQLException
    {
        if (c.lookup() != Lookup.GET_PAGE && c.lookup() != Lookup.GET_PAGE_IDS) {
            return;
        }
        if (!observed.startsWith("page:") && !observed.startsWith("ids:")) {
            return;
        }
        String bound = c.lookup() == Lookup.GET_PAGE ? NameLookupOracle.normalize(c.probe())
                : c.probe();
        Set<Integer> matched = new HashSet<>();
        Set<Integer> exact = new HashSet<>();
        try (Connection conn = JwplTestDatabase
                .rawConnection(JwplTestDatabase.provisionVariant(profile));
                PreparedStatement ps = conn.prepareStatement(
                        "select pageID, name from PageMapLine where name = ?")) {
            ps.setString(1, bound);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matched.add(rs.getInt(1));
                    if (bound.equals(rs.getString(2))) {
                        exact.add(rs.getInt(1));
                    }
                }
            }
        }
        catch (SQLException e) {
            // the column collation cannot match the probe at all; C0 covers the lookup itself
            return;
        }
        Set<Integer> result = parseIds(observed);
        assertTrue(matched.containsAll(result),
                "C6: " + observed + " is not within the collation matches " + matched);
        if (c.lookup() == Lookup.GET_PAGE_IDS) {
            assertTrue(result.containsAll(exact),
                    "C6: " + observed + " misses exact matches " + exact);
        }
    }

    private static Set<Integer> parseIds(String outcome)
    {
        String body = outcome.substring(outcome.indexOf(':') + 1).replace("[", "")
                .replace("]", "").trim();
        if (body.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(body.split(",")).map(s -> Integer.valueOf(s.trim()))
                .collect(Collectors.toSet());
    }

    private static Wikipedia wiki(ColumnProfile profile)
    {
        return WIKIS.computeIfAbsent(profile, p -> {
            try {
                return new Wikipedia(JwplTestDatabase.provisionVariant(p));
            }
            catch (WikiInitializationException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Calls the lookup and maps its result into the vocabulary of the contract. Only
     * {@link WikiApiException}s are part of the contract; any other exception becomes
     * {@code EXC:<code>}, which violates C0 unless recorded as a divergence.
     */
    static String invoke(Wikipedia wiki, Case c)
    {
        String probe = c.probe();
        try {
            return switch (c.lookup()) {
                case EXISTS_PAGE -> String.valueOf(wiki.existsPage(probe));
                case GET_CATEGORY -> "cat:" + wiki.getCategory(probe).getPageId();
                case GET_PAGE -> "page:" + wiki.getPage(probe).getPageId();
                case GET_PAGE_BY_EXACT_TITLE -> "page:" + wiki.getPageByExactTitle(probe)
                        .getPageId();
                case GET_PAGE_IDS -> "ids:" + NameLookupOracle.sorted(wiki.getPageIds(probe));
                case GET_PAGE_IDS_CI -> "ids:"
                        + NameLookupOracle.sorted(wiki.getPageIdsCaseInsensitive(probe));
                case GET_CATEGORIES_OF -> "cats:" + NameLookupOracle.sorted(wiki
                        .getCategories(probe).stream().map(Category::getPageId).toList());
                case PAGE_QUERY_LIKE -> {
                    PageQuery query = new PageQuery();
                    query.setTitlePattern(probe);
                    List<Integer> ids = new ArrayList<>();
                    wiki.getPages(query).forEach(p -> ids.add(p.getPageId()));
                    yield "ids:" + NameLookupOracle.sorted(ids);
                }
                case DISCUSSION_ARCHIVES -> {
                    int article = NameLookupOracle.articlePageId(probe).orElseThrow(
                            () -> new IllegalStateException("No article " + probe));
                    List<Integer> ids = new ArrayList<>();
                    wiki.getDiscussionArchives(article).forEach(p -> ids.add(p.getPageId()));
                    yield "ids:" + NameLookupOracle.sorted(ids);
                }
            };
        }
        catch (WikiPageNotFoundException e) {
            return NameLookupOracle.NOT_FOUND;
        }
        catch (WikiApiException e) {
            return "API_EXC:" + e.getClass().getSimpleName();
        }
        catch (RuntimeException e) {
            return "EXC:" + code(e);
        }
    }

    private static String code(Throwable e)
    {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql) {
                return sql.getErrorCode() != 0 ? String.valueOf(sql.getErrorCode())
                        : sql.getSQLState();
            }
        }
        return e.getClass().getSimpleName();
    }
}
