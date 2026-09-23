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
package org.dkpro.jwpl.util.templates.generator.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dkpro.jwpl.api.util.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests resolving the ids of templates that already exist in the database, which the
 * {@link WikipediaTemplateInfoGenerator} does before writing the dump.
 */
class WikipediaTemplateInfoGeneratorTest
{

    @Test
    void resolvesExistingIdsUnderTheOriginalTemplateNames()
    {
        String quoted = StringUtils.sqlEscape("o'neil");
        Map<String, Integer> existing = Map.of("infobox", 1, "cite_web", 2, "o\\'neil", 3);
        Map<String, Integer> resolved = new HashMap<>();

        WikipediaTemplateInfoGenerator.resolveTemplateIds(existing,
                Set.of("infobox", "cite web", quoted, "unknown"), resolved);

        assertEquals(Map.of("infobox", 1, "cite web", 2, quoted, 3), resolved);
    }

    @Test
    void keepsIdsAlreadyResolvedForAnotherIndex()
    {
        Map<String, Integer> existing = Map.of("infobox", 1, "stub", 2);
        Map<String, Integer> resolved = new HashMap<>();

        WikipediaTemplateInfoGenerator.resolveTemplateIds(existing, Set.of("infobox"), resolved);
        resolved.put("infobox", 42);
        WikipediaTemplateInfoGenerator.resolveTemplateIds(existing, Set.of("infobox", "stub"),
                resolved);

        assertEquals(Map.of("infobox", 42, "stub", 2), resolved);
    }
}
