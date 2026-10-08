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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dkpro.jwpl.api.NameLookupSpec.Case;
import org.dkpro.jwpl.api.NameLookupSpec.Divergence;
import org.dkpro.jwpl.api.testdb.ContractFixture;
import org.dkpro.jwpl.api.testdb.ContractFixture.Stored;
import org.junit.jupiter.api.Test;

/**
 * Checks the name lookup contract itself, without a database, so it runs in every build.
 */
public class NameLookupContractSpecTest
{
    @Test
    public void testExpectedOutcomesMatchTheOracle()
    {
        for (Case c : NameLookupSpec.cases()) {
            assertEquals(NameLookupOracle.expected(c.lookup(), c.probe(), name -> true),
                    c.expected(), c.caseId() + " (" + c.why() + ")");
        }
    }

    @Test
    public void testCaseIdsAreUnique()
    {
        Set<String> seen = new HashSet<>();
        for (Case c : NameLookupSpec.cases()) {
            assertTrue(seen.add(c.caseId()), "Duplicate case id " + c.caseId());
        }
    }

    @Test
    public void testDivergencesReferToCasesAndIssues()
    {
        Map<String, Case> cases = NameLookupSpec.cases().stream()
                .collect(Collectors.toMap(Case::caseId, c -> c));
        List<Divergence> divergences = NameLookupSpec.divergences();
        for (Divergence d : divergences) {
            Case c = cases.get(d.caseId());
            assertTrue(c != null, "Divergence for unknown case " + d.caseId());
            assertTrue(NameLookupSpec.ISSUE.matcher(d.issue()).matches(),
                    d.caseId() + ": a divergence must name an issue, not '" + d.issue() + "'");
            assertNotEquals(c.expected(), d.observed(),
                    d.caseId() + ": a divergence must differ from the contract");
            assertTrue(!d.profiles().isEmpty(), d.caseId() + ": no profiles");
            d.profiles().forEach(p -> assertTrue(p.appliesTo(d.engine()),
                    d.caseId() + ": " + p + " does not apply to " + d.engine()));
        }
        for (int i = 0; i < divergences.size(); i++) {
            for (int j = i + 1; j < divergences.size(); j++) {
                Divergence a = divergences.get(i);
                Divergence b = divergences.get(j);
                if (a.caseId().equals(b.caseId()) && a.engine() == b.engine()) {
                    Set<Object> overlap = new HashSet<>(a.profiles());
                    overlap.retainAll(b.profiles());
                    assertTrue(overlap.isEmpty(), a.caseId() + ": overlapping divergences");
                }
            }
        }
    }

    @Test
    public void testStoredNamesRoundTripThroughEscaping()
    {
        for (Stored s : ContractFixture.STORED) {
            assertEquals(s.name(), NameLookupSpec.unescape(NameLookupSpec.escape(s.name())));
        }
    }

    /**
     * The oracle normalizes with the production {@link Title}, so a bug there would go unnoticed;
     * these literals guard against that.
     */
    @Test
    public void testNormalizationLiterals()
    {
        assertEquals("Mixed_Case", NameLookupOracle.normalize("mixed_Case"));
        assertEquals("Wikipedia_API", NameLookupOracle.normalize("wikipedia API"));
        assertEquals("Trailing_probe", NameLookupOracle.normalize("Trailing_probe"));
    }
}
