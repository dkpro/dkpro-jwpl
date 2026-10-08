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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.NameLookupSpec.Lookup;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.api.testdb.ContractFixture;
import org.dkpro.jwpl.api.testdb.ContractFixture.Kind;
import org.dkpro.jwpl.api.testdb.ContractFixture.Stored;

/**
 * Computes the outcome the contract requires for a lookup, in plain Java over
 * {@link ContractFixture#STORED}, comparing names by code point after the normalization of the
 * production {@link Title}.
 * <ul>
 * <li>C1: exact lookups match names by code point equality.</li>
 * <li>C2: among categories of the same name, the lowest id wins.</li>
 * <li>Among page map entries of the same name, the lowest entry id wins.</li>
 * <li>{@code LIKE} patterns follow SQL: case-sensitive, {@code %} and {@code _} are wildcards,
 * and there is no escape character.</li>
 * </ul>
 */
final class NameLookupOracle
{
    static final String NOT_FOUND = "NOT_FOUND";

    private NameLookupOracle()
    {
        // static only
    }

    /**
     * @param lookup   The lookup.
     * @param probe    The argument.
     * @param storable Which names the columns of the database under test can hold.
     * @return The outcome required by the contract.
     */
    static String expected(Lookup lookup, String probe, Predicate<String> storable)
    {
        List<Stored> stored = ContractFixture.STORED.stream()
                .filter(s -> s.preloaded() || storable.test(s.name())).toList();
        String norm = normalize(probe);
        return switch (lookup) {
            case EXISTS_PAGE -> String.valueOf(pages(stored).anyMatch(s -> s.name().equals(norm)));
            case GET_CATEGORY -> stored.stream()
                    .filter(s -> s.kind() == Kind.CATEGORY && s.name().equals(norm))
                    .min(Comparator.comparingInt(Stored::id)).map(s -> "cat:" + s.pageId())
                    .orElse(NOT_FOUND);
            case GET_PAGE -> firstPage(stored, norm);
            case GET_PAGE_BY_EXACT_TITLE -> firstPage(stored, probe);
            case GET_PAGE_IDS -> idsOrNotFound(
                    pages(stored).filter(s -> s.name().equals(norm)).map(Stored::pageId).toList());
            case GET_PAGE_IDS_CI -> {
                String lower = probe.toLowerCase(Locale.ROOT).replace(' ', '_');
                yield idsOrNotFound(pages(stored)
                        .filter(s -> s.name().toLowerCase(Locale.ROOT).equals(lower))
                        .map(Stored::pageId).toList());
            }
            case GET_CATEGORIES_OF -> "cats:" + sorted(pages(stored)
                    .filter(s -> s.name().equals(norm))
                    .flatMap(s -> ContractFixture.PAGE_CATEGORIES
                            .getOrDefault(s.pageId(), List.of()).stream())
                    .filter(c -> stored.stream().anyMatch(
                            s -> s.kind() == Kind.CATEGORY && s.pageId() == c))
                    .toList());
            case PAGE_QUERY_LIKE -> {
                Pattern like = like(probe);
                yield "ids:" + sorted(pages(stored).filter(s -> like.matcher(s.name()).matches())
                        .map(Stored::pageId).toList());
            }
            case DISCUSSION_ARCHIVES -> {
                String prefix = WikiConstants.DISCUSSION_PREFIX + norm + "/";
                yield "ids:" + sorted(pages(stored).filter(s -> s.name().startsWith(prefix))
                        .map(Stored::pageId).toList());
            }
        };
    }

    /**
     * @param probe A title.
     * @return The page id of the article {@code probe} names exactly, if stored at all.
     */
    static Optional<Integer> articlePageId(String probe)
    {
        String norm = normalize(probe);
        return pages(ContractFixture.STORED).filter(s -> s.name().equals(norm))
                .map(Stored::pageId).findFirst();
    }

    static String normalize(String probe)
    {
        try {
            return new Title(probe).getWikiStyleTitle();
        }
        catch (WikiTitleParsingException e) {
            return probe;
        }
    }

    static String sorted(Collection<Integer> ids)
    {
        return new TreeSet<>(ids).toString();
    }

    private static Stream<Stored> pages(List<Stored> stored)
    {
        return stored.stream().filter(s -> s.kind() == Kind.PAGE);
    }

    private static String firstPage(List<Stored> stored, String name)
    {
        return pages(stored).filter(s -> s.name().equals(name))
                .min(Comparator.comparingInt(Stored::id)).map(s -> "page:" + s.pageId())
                .orElse(NOT_FOUND);
    }

    private static String idsOrNotFound(List<Integer> ids)
    {
        return ids.isEmpty() ? NOT_FOUND : "ids:" + sorted(ids);
    }

    private static Pattern like(String pattern)
    {
        String regex = pattern.chars().mapToObj(ch -> switch (ch) {
            case '%' -> ".*";
            case '_' -> ".";
            default -> Pattern.quote(String.valueOf((char) ch));
        }).collect(Collectors.joining());
        return Pattern.compile(regex, Pattern.DOTALL);
    }
}
