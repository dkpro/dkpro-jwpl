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
package org.dkpro.jwpl.wikimachine.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers to recognise and order Wikimedia multi-part dump files.
 * <p>
 * Wikimedia publishes large XML dumps split across several files using the naming scheme
 * {@code <prefix>-<role><N>.xml-p<start>p<end>.<ext>} (for example
 * {@code dewiki-20260101-pages-articles1.xml-p1p297012.bz2},
 * {@code dewiki-20260101-pages-articles2.xml-p297013p1262093.bz2}). Older / smaller dumps use the
 * single-file scheme {@code <prefix>-<role>.xml.<ext>}. This utility:
 * <ul>
 *   <li>detects whether a filename is part of a multi-part dump — {@link #hasPageRange};</li>
 *   <li>matches filenames against a known dump role (e.g. {@code pages-articles}) accepting both
 *       the single-file and multi-part naming schemes — {@link #matchesRole};</li>
 *   <li>orders a collection of multi-part files by ascending start page id —
 *       {@link #orderByPageRange}.</li>
 * </ul>
 * All methods tolerate absolute paths: only the file name is inspected.
 */
public final class DumpFileDiscovery
{

    /**
     * Suffix that marks a multi-part dump file; captures the start and end page id of the range.
     * Example match on {@code foo-pages-articles1.xml-p297013p1262093.bz2} yields start=297013,
     * end=1262093.
     */
    private static final Pattern PAGE_RANGE = Pattern.compile("-p(\\d+)p(\\d+)(?=\\.)");

    private DumpFileDiscovery()
    {
        // static-only
    }

    /**
     * @param fileName A file name or path whose last component is inspected.
     * @return {@code true} if the name carries the multi-part suffix {@code -p<start>p<end>.}.
     */
    public static boolean hasPageRange(String fileName)
    {
        if (fileName == null) {
            return false;
        }
        return PAGE_RANGE.matcher(lastSegment(fileName)).find();
    }

    /**
     * @param file Any {@link File}, absolute or relative.
     * @return {@code true} if the file's name carries the multi-part suffix.
     */
    public static boolean hasPageRange(File file)
    {
        return file != null && hasPageRange(file.getName());
    }

    /**
     * Matches a filename against a known Wikimedia dump role under either naming scheme:
     * <ul>
     *   <li>single-file: {@code <prefix><role>.xml.<ext>}</li>
     *   <li>multi-part:  {@code <prefix><role><N>.xml-p<start>p<end>.<ext>}</li>
     * </ul>
     * The matcher is anchored on the role substring and the {@code .xml} marker, so similarly
     * named dumps such as {@code pages-articles-multistream.xml.bz2} are correctly rejected when
     * the requested role is {@code pages-articles}.
     *
     * @param fileName File name (or path whose last segment is the file name).
     * @param role     Role token as it appears in the dump name, e.g. {@code pages-articles},
     *                 {@code pages-meta-current}, {@code pages-meta-history}.
     * @param extensions Supported archive extensions without dot, e.g. {@code ["bz2", "gz", "7z"]}.
     * @return {@code true} if {@code fileName} matches the role under either scheme.
     */
    public static boolean matchesRole(String fileName, String role, Collection<String> extensions)
    {
        if (fileName == null || role == null || extensions == null || extensions.isEmpty()) {
            return false;
        }
        final String name = lastSegment(fileName);
        final String extAlt = String.join("|", extensions);
        // Either: ...<role>.xml.<ext>
        //     or: ...<role>\d+.xml-p<start>p<end>.<ext>
        final Pattern p = Pattern.compile(
                ".*" + Pattern.quote(role) + "(\\d+\\.xml-p\\d+p\\d+|\\.xml)\\.(" + extAlt + ")$");
        return p.matcher(name).matches();
    }

    /**
     * Returns a new list containing {@code files} ordered for multi-part consumption: files with a
     * {@code -p<start>p<end>} suffix are sorted by ascending start page id; files without such a
     * suffix preserve their relative input order and come first. Stable for equal starts.
     *
     * @param files Input files in any order. {@code null} elements are rejected.
     * @return A new ordered {@link List}.
     * @throws IllegalArgumentException If {@code files} is {@code null} or contains a null element.
     */
    public static List<File> orderByPageRange(Collection<File> files)
    {
        if (files == null) {
            throw new IllegalArgumentException("'files' must not be null.");
        }
        final List<File> out = new ArrayList<>(files.size());
        for (File f : files) {
            if (f == null) {
                throw new IllegalArgumentException("'files' contains a null element.");
            }
            out.add(f);
        }
        out.sort(Comparator.comparingLong(DumpFileDiscovery::pageRangeStart));
        return out;
    }

    /**
     * @return The start page id encoded in the file name's {@code -p<start>p<end>} suffix, or
     *         {@link Long#MIN_VALUE} if absent. Files without a range therefore sort before
     *         ranged parts in {@link #orderByPageRange}.
     */
    static long pageRangeStart(File file)
    {
        if (file == null) {
            return Long.MIN_VALUE;
        }
        final Matcher m = PAGE_RANGE.matcher(file.getName());
        if (!m.find()) {
            return Long.MIN_VALUE;
        }
        try {
            return Long.parseLong(m.group(1));
        }
        catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    private static String lastSegment(String path)
    {
        final int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
