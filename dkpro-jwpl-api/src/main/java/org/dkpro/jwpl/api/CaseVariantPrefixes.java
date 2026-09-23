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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Builds {@code like} patterns that let a {@code lower(name) = :title} lookup on
 * {@code PageMapLine} seek {@code name_index} instead of scanning the whole table, see
 * {@link Wikipedia#getPageIdsCaseInsensitive(String)}.
 * <p>
 * A B-tree index on {@code name} cannot serve {@code lower(name) = ?}. Every name satisfying that
 * predicate, however, starts with a string whose lower case is a prefix of the lower-cased title.
 * For a short prefix of the title, this class enumerates all such strings as patterns of the form
 * {@code 'Prefix%'}. Each pattern is an index range, and the {@code lower(name)} predicate stays in
 * the query to decide which rows match, so the patterns only prune rows and never change the result.
 * <p>
 * That argument relies on two properties of the backend, which the caller has to establish:
 * {@code like} compares characters one by one and exactly (a binary collation), and every character
 * the backend lower-cases into a prefix character is enumerated here. The latter is derived from the
 * Unicode case mappings of the JDK, and prefixes are restricted to Latin, Greek, Cyrillic, and
 * Armenian characters, whose case mappings are long stable. Database case tables that stem from an
 * older Unicode version therefore know no more mappings into these characters than the JDK does.
 */
final class CaseVariantPrefixes
{
    /** The character escaping {@code %}, {@code _}, and itself in the returned patterns. */
    static final char ESCAPE = '!';

    /**
     * Upper bound for the number of patterns, i.e. of index ranges a lookup is split into. A longer
     * prefix narrows each range, yet multiplies the number of ranges by the number of case variants
     * of every character added.
     */
    static final int MAX_PATTERNS = 64;

    /** Characters from this code point on end a prefix: U+0530 is the end of Cyrillic Supplement. */
    private static final int PREFIX_CHAR_BOUND = 0x0530;

    /**
     * For every character below {@link #PREFIX_CHAR_BOUND}, the characters which lower-case into it,
     * itself included. Supplementary code points are left out, as Unicode maps none of them into
     * this range.
     */
    private static final String[] VARIANTS = buildVariants();

    /**
     * How the backend computes {@code lower(name)}.
     */
    enum Lowering
    {
        /** One character at a time, as MySQL and MariaDB do. */
        PER_CHARACTER,

        /**
         * With the context-sensitive rules of {@link String#toLowerCase(Locale)}, as HSQLDB does.
         * Under the Turkish and Azeri rules an {@code I} followed by a combining dot above collapses
         * into one {@code i}, so a prefix ends after its first {@code i}.
         */
        CONTEXTUAL
    }

    private CaseVariantPrefixes()
    {
        // utility class
    }

    /**
     * @param lowerCaseTitle The lower-cased title the {@code lower(name)} predicate compares with.
     * @param lowering       How the backend lower-cases {@code name}.
     * @return The patterns, to be used with {@code like ... escape '!'}. Every name {@code n} with
     *         {@code lower(n) = lowerCaseTitle} matches at least one of them. Empty if the title
     *         does not start with a character a prefix can be built from.
     */
    static List<String> of(String lowerCaseTitle, Lowering lowering)
    {
        List<StringBuilder> prefixes = new ArrayList<>();
        prefixes.add(new StringBuilder());
        boolean empty = true;
        for (int i = 0; i < lowerCaseTitle.length(); i++) {
            char c = lowerCaseTitle.charAt(i);
            if (!isPrefixChar(c)) {
                break;
            }
            String variants = VARIANTS[c];
            if (prefixes.size() * variants.length() > MAX_PATTERNS) {
                break;
            }
            List<StringBuilder> extended = new ArrayList<>(prefixes.size() * variants.length());
            for (StringBuilder prefix : prefixes) {
                for (int v = 0; v < variants.length(); v++) {
                    extended.add(appendEscaped(new StringBuilder(prefix), variants.charAt(v)));
                }
            }
            prefixes = extended;
            empty = false;
            if (c == 'i' && lowering == Lowering.CONTEXTUAL) {
                break;
            }
        }
        if (empty) {
            return Collections.emptyList();
        }
        List<String> patterns = new ArrayList<>(prefixes.size());
        for (StringBuilder prefix : prefixes) {
            patterns.add(prefix.append('%').toString());
        }
        return patterns;
    }

    private static boolean isPrefixChar(char c)
    {
        if (c >= PREFIX_CHAR_BOUND) {
            return false;
        }
        // A combining mark may be the tail of a single character whose lower case expands into
        // several characters, e.g. U+0130 into i and U+0307. It must not be matched on its own.
        int type = Character.getType(c);
        return type != Character.NON_SPACING_MARK && type != Character.ENCLOSING_MARK
                && type != Character.COMBINING_SPACING_MARK;
    }

    private static StringBuilder appendEscaped(StringBuilder sb, char c)
    {
        if (c == ESCAPE || c == '%' || c == '_') {
            sb.append(ESCAPE);
        }
        return sb.append(c);
    }

    private static String[] buildVariants()
    {
        StringBuilder[] variants = new StringBuilder[PREFIX_CHAR_BOUND];
        for (int c = 0; c < PREFIX_CHAR_BOUND; c++) {
            variants[c] = new StringBuilder();
            addVariant(variants, c, (char) c);
            // Covers characters that lower-case differently depending on their context, such as
            // the final sigma, whose upper case is the capital sigma.
            addVariant(variants, c, Character.toUpperCase((char) c));
            addVariant(variants, c, Character.toTitleCase((char) c));
        }
        Locale[] locales = { Locale.ROOT, Locale.getDefault(), Locale.forLanguageTag("tr"),
                Locale.forLanguageTag("az"), Locale.forLanguageTag("lt") };
        for (int cp = 0; cp <= Character.MAX_VALUE; cp++) {
            char c = (char) cp;
            if (Character.isSurrogate(c)) {
                continue;
            }
            addVariant(variants, Character.toLowerCase(c), c);
            // Full case mappings, which may expand one character into several.
            String s = String.valueOf(c);
            for (Locale locale : locales) {
                addVariant(variants, s.toLowerCase(locale).charAt(0), c);
            }
        }
        String[] result = new String[PREFIX_CHAR_BOUND];
        for (int c = 0; c < PREFIX_CHAR_BOUND; c++) {
            result[c] = variants[c].toString();
        }
        return result;
    }

    private static void addVariant(StringBuilder[] variants, int lowerCase, char variant)
    {
        if (lowerCase < PREFIX_CHAR_BOUND && variants[lowerCase].indexOf(String.valueOf(variant)) < 0) {
            variants[lowerCase].append(variant);
        }
    }
}
