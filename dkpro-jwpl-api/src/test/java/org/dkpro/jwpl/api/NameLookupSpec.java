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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dkpro.jwpl.api.testdb.ColumnProfile;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase.Engine;

/**
 * The name lookup contract: the cases of {@code contract/name-lookups.tsv} and the known
 * divergences of {@code contract/name-lookups-divergences.tsv}.
 * <p>
 * Outcomes are written in a small vocabulary: {@code true}, {@code false}, {@code page:<id>},
 * {@code cat:<id>}, {@code ids:[..]}, {@code cats:[..]}, {@code NOT_FOUND}, and {@code EXC:<code>}
 * for any other exception, where the code is the vendor error code or SQL state of the
 * underlying {@link java.sql.SQLException}, or the exception class.
 */
final class NameLookupSpec
{
    /** The lookups under contract. */
    enum Lookup
    {
        /** {@link Wikipedia#existsPage(String)}; exact. */
        EXISTS_PAGE(true),
        /** {@link Wikipedia#getCategory(String)}; exact. */
        GET_CATEGORY(true),
        /** {@link Wikipedia#getPage(String)}; ideally exact, follows the collation. */
        GET_PAGE(false),
        /** {@link Wikipedia#getPageByExactTitle(String)}; exact, without normalization. */
        GET_PAGE_BY_EXACT_TITLE(true),
        /** {@link Wikipedia#getPageIds(String)}; ideally exact, follows the collation. */
        GET_PAGE_IDS(false),
        /** {@link Wikipedia#getPageIdsCaseInsensitive(String)}. */
        GET_PAGE_IDS_CI(false),
        /** {@link Wikipedia#getCategories(String)}; ideally exact, follows the collation. */
        GET_CATEGORIES_OF(false),
        /** {@link Wikipedia#getPages(PageQuery)} with a title pattern; SQL LIKE semantics. */
        PAGE_QUERY_LIKE(false),
        /** {@link Wikipedia#getDiscussionArchives(int)}; the probe names the article. */
        DISCUSSION_ARCHIVES(false);

        private final boolean exact;

        Lookup(boolean exact)
        {
            this.exact = exact;
        }

        boolean isExact()
        {
            return exact;
        }
    }

    /**
     * A case of the contract.
     *
     * @param caseId   A unique id.
     * @param lookup   The lookup.
     * @param probe    The argument passed to the lookup.
     * @param expected The expected outcome on a column that can hold every name of the fixture.
     * @param why      What the case pins down.
     */
    record Case(String caseId, Lookup lookup, String probe, String expected, String why)
    {
        @Override
        public String toString()
        {
            return caseId;
        }
    }

    /**
     * An outcome that deviates from the contract on some engines and profiles, and is accepted
     * until the issue that tracks it is fixed. It must occur exactly as recorded, otherwise it is
     * stale.
     *
     * @param caseId   The case.
     * @param engine   The engine.
     * @param profiles The profiles it occurs on.
     * @param observed The outcome that occurs instead of the expected one.
     * @param issue    The issue tracking it.
     */
    record Divergence(String caseId, Engine engine, Set<ColumnProfile> profiles, String observed,
            String issue)
    {
        boolean appliesTo(String id, Engine e, ColumnProfile profile)
        {
            return caseId.equals(id) && engine == e && profiles.contains(profile);
        }
    }

    static final String CASES = "contract/name-lookups.tsv";
    static final String DIVERGENCES = "contract/name-lookups-divergences.tsv";

    /** The issue reference a divergence must carry: an issue number or a placeholder to file. */
    static final Pattern ISSUE = Pattern.compile("#(\\d+|NEW-[a-z0-9-]+)");

    private static final Pattern ESCAPE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");

    private NameLookupSpec()
    {
        // loaders only
    }

    static List<Case> cases()
    {
        List<Case> cases = new ArrayList<>();
        for (String[] f : rows(CASES, 5)) {
            cases.add(new Case(f[0], Lookup.valueOf(f[1]), unescape(f[2]), f[3], f[4]));
        }
        return cases;
    }

    static List<Divergence> divergences()
    {
        List<Divergence> divergences = new ArrayList<>();
        for (String[] f : rows(DIVERGENCES, 5)) {
            Set<ColumnProfile> profiles = Arrays.stream(f[2].split(","))
                    .map(p -> ColumnProfile.valueOf(p.trim())).collect(Collectors.toSet());
            divergences.add(new Divergence(f[0], Engine.valueOf(f[1]), profiles, unescape(f[3]),
                    f[4]));
        }
        return divergences;
    }

    static Optional<Divergence> divergence(List<Divergence> all, String caseId, Engine engine,
            ColumnProfile profile)
    {
        return all.stream().filter(d -> d.appliesTo(caseId, engine, profile)).findFirst();
    }

    /**
     * @param text A field that may contain {@code \\uXXXX} escapes.
     * @return The field with the escapes replaced by the characters.
     */
    static String unescape(String text)
    {
        Matcher m = ESCAPE.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(
                    String.valueOf((char) Integer.parseInt(m.group(1), 16))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * @param text A string.
     * @return The string with every character outside printable ASCII, and every space, written as
     *         a {@code \\uXXXX} escape.
     */
    static String escape(String text)
    {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch > 0x20 && ch < 0x7f && ch != '\\') {
                sb.append(ch);
            }
            else {
                sb.append(String.format("\\u%04x", (int) ch));
            }
        }
        return sb.toString();
    }

    private static List<String[]> rows(String resource, int columns)
    {
        List<String[]> rows = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\t", -1);
                if (fields.length != columns) {
                    throw new IllegalStateException(resource + ": expected " + columns
                            + " tab separated columns in: " + line);
                }
                rows.add(fields);
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return rows;
    }
}
