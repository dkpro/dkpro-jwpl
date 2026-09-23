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
package org.dkpro.jwpl.wikimachine.dump.version;

import java.io.IOException;

import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;

/**
 * Holds the single copy of the algorithms that turn a {@code categorylinks} respectively a
 * {@code pagelinks} row into the JWPL output rows. All {@link IDumpVersion} implementations
 * delegate here through a {@link LinkRowSink}.
 */
public final class LinkRowProcessor
{

    private LinkRowProcessor()
    {
        // static-only
    }

    /**
     * Processes one row of the {@code categorylinks} table.
     * <p>
     * When the dump carries a {@code cl_type} column the kind of membership is taken from it;
     * otherwise it is inferred from whether the source page id is a registered article or a
     * registered category, which is the behaviour of all dumps predating that column. In either
     * case a row is only written for a source page that is actually registered.
     * <p>
     * If the parser resolves its targets to ids (see
     * {@link CategorylinksParser#hasResolvedTargetIds()}), the row is handed to
     * {@link #processCategoryLinkResolved(CategorylinksParser, LinkRowSink)}.
     *
     * @param parser The parser positioned on the row to process.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     * @throws IOException Thrown if the parser did not provide a target title.
     */
    public static void processCategoryLink(CategorylinksParser parser, LinkRowSink sink)
        throws IOException
    {
        if (parser.hasResolvedTargetIds()) {
            processCategoryLinkResolved(parser, sink);
            return;
        }
        final String clTo = parser.getClTo();
        if (clTo == null) {
            throw new IOException("Parsing error." + CategorylinksParser.class.getName()
                    + " returned null value in " + sink.getClass().getName());
        }
        final Integer categoryId = sink.categoryIdByTitle(clTo);
        if (categoryId == null) {
            // discard links with non-registered targets
            return;
        }
        classifyCategoryLink(parser, sink, categoryId,
                clTo.equals(sink.getDisambiguationCategoryTitle()));
    }

    /**
     * Processes one row of the {@code categorylinks} table whose target has already been resolved
     * to the page id of a registered category by a
     * {@link org.dkpro.jwpl.wikimachine.dump.sql.ResolvedLinkTargets}. The row is classified
     * exactly as in {@link #processCategoryLink(CategorylinksParser, LinkRowSink)}.
     *
     * @param parser The parser positioned on the row to process. Its targets must have been
     *               resolved against {@code sink}.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     */
    public static void processCategoryLinkResolved(CategorylinksParser parser, LinkRowSink sink)
    {
        final int categoryId = parser.getResolvedTargetId();
        if (categoryId < 0) {
            // discard links with non-registered targets
            return;
        }
        classifyCategoryLink(parser, sink, categoryId, parser.isDisambiguationTarget());
    }

    private static void classifyCategoryLink(CategorylinksParser parser, LinkRowSink sink,
            int categoryId, boolean disambiguation)
    {
        final int clFrom = parser.getClFrom();
        switch (parser.getClType()) {
        case PAGE:
            if (sink.isKnownArticleId(clFrom)) {
                emitMembership(sink, categoryId, clFrom, disambiguation);
            }
            break;
        case SUBCAT:
            if (sink.isKnownCategoryId(clFrom)) {
                sink.writeSubcategory(categoryId, clFrom);
            }
            break;
        case FILE:
            // file members are never registered as articles or categories
            break;
        case UNKNOWN:
        default:
            if (sink.isKnownArticleId(clFrom)) {
                emitMembership(sink, categoryId, clFrom, disambiguation);
            }
            else if (sink.isKnownCategoryId(clFrom)) {
                sink.writeSubcategory(categoryId, clFrom);
            }
            break;
        }
    }

    private static void emitMembership(LinkRowSink sink, int categoryId, int clFrom,
            boolean disambiguation)
    {
        sink.writeCategoryMembership(categoryId, clFrom);
        if (disambiguation) {
            sink.recordDisambiguation(clFrom);
        }
    }

    /**
     * Processes one row of the {@code pagelinks} table.
     * <p>
     * Only links whose <em>target</em> lives in the main namespace are kept. The title of a
     * {@code pagelinks} row is namespace local, so {@code [[Wikipedia:Stub]]} and {@code [[Stub]]}
     * both arrive here as the title {@code Stub} and are told apart by
     * {@link PagelinksParser#getPlNamespace()} alone. Without that check every link into a
     * non-article namespace whose title happens to match an article is written as a link to that
     * article (see issue #97). Article links are the only ones the JWPL page link tables model;
     * adding the remaining namespaces is issue #38.
     * <p>
     * If the parser resolves its targets to ids (see {@link PagelinksParser#hasResolvedTargetIds()}),
     * the row is handed to {@link #processPageLinkResolved(PagelinksParser, LinkRowSink)}.
     *
     * @param parser The parser positioned on the row to process.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     */
    public static void processPageLink(PagelinksParser parser, LinkRowSink sink)
    {
        if (parser.hasResolvedTargetIds()) {
            processPageLinkResolved(parser, sink);
            return;
        }
        final String plTo = parser.getPlTo();
        if (plTo == null) {
            return;
        }
        if (parser.getPlNamespace() != AbstractDumpVersion.NS_MAIN) {
            // discard links pointing outside the main namespace
            return;
        }
        final int plFrom = parser.getPlFrom();
        // skip redirects if skipPage is enabled
        if (sink.isSkipPageEnabled() && !sink.isKnownArticleId(plFrom)) {
            return;
        }
        final Integer pageId = sink.pageIdByTitle(plTo);
        if (pageId == null) {
            return;
        }
        sink.writePageLink(plFrom, pageId);
    }

    /**
     * Processes one row of the {@code pagelinks} table whose target has already been resolved to
     * the page id of a registered article by a
     * {@link org.dkpro.jwpl.wikimachine.dump.sql.ResolvedLinkTargets}. Only article targets are
     * ever resolved, so the namespace check of
     * {@link #processPageLink(PagelinksParser, LinkRowSink)} is implied.
     *
     * @param parser The parser positioned on the row to process. Its targets must have been
     *               resolved against {@code sink}.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     */
    public static void processPageLinkResolved(PagelinksParser parser, LinkRowSink sink)
    {
        final int pageId = parser.getResolvedTargetId();
        if (pageId < 0) {
            return;
        }
        final int plFrom = parser.getPlFrom();
        // skip redirects if skipPage is enabled
        if (sink.isSkipPageEnabled() && !sink.isKnownArticleId(plFrom)) {
            return;
        }
        sink.writePageLink(plFrom, pageId);
    }
}
