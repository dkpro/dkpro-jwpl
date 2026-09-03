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
     *
     * @param parser The parser positioned on the row to process.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     * @throws IOException Thrown if the parser did not provide a target title.
     */
    public static void processCategoryLink(CategorylinksParser parser, LinkRowSink sink)
        throws IOException
    {
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
        final int clFrom = parser.getClFrom();
        switch (parser.getClType()) {
        case PAGE:
            if (sink.isKnownArticleId(clFrom)) {
                emitMembership(sink, categoryId, clFrom, clTo);
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
                emitMembership(sink, categoryId, clFrom, clTo);
            }
            else if (sink.isKnownCategoryId(clFrom)) {
                sink.writeSubcategory(categoryId, clFrom);
            }
            break;
        }
    }

    private static void emitMembership(LinkRowSink sink, int categoryId, int clFrom, String clTo)
    {
        sink.writeCategoryMembership(categoryId, clFrom);
        if (clTo.equals(sink.getDisambiguationCategoryTitle())) {
            sink.recordDisambiguation(clFrom);
        }
    }

    /**
     * Processes one row of the {@code pagelinks} table.
     *
     * @param parser The parser positioned on the row to process.
     * @param sink   The sink to look up ids in and to write the resulting rows to.
     */
    public static void processPageLink(PagelinksParser parser, LinkRowSink sink)
    {
        final String plTo = parser.getPlTo();
        if (plTo == null) {
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
}
