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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.io.IOException;

import org.dkpro.jwpl.wikimachine.dump.version.LinkRowSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * A {@link LinkTargetResolver} that maps each {@code lt_id} straight to the page id of the article
 * or category it denotes in <em>one</em> {@link LinkRowSink}, instead of keeping its title.
 * <p>
 * The targets are resolved once, while the {@code linktarget} dump is loaded, through the very
 * {@link LinkRowSink#pageIdByTitle(String)} and {@link LinkRowSink#categoryIdByTitle(String)}
 * lookups the title based path uses per row, so a link is matched exactly when it is matched
 * there. Targets outside the namespaces {@code 0} and {@code 14} and targets that do not resolve
 * to a registered page (red links, unknown titles) are dropped, which keeps only a primitive
 * {@code lt_id -> page id} entry per usable target in memory.
 * <p>
 * The resolved ids are only valid for the sink they were resolved against, and only as long as
 * its title maps do not change. This holds for the single dump version of the DataMachine once
 * the {@code page} table has been processed; it does not hold for the TimeMachine, which keeps
 * separate title maps per version and has to use a {@link FastUtilLinkTargetResolver}.
 * <p>
 * {@link CategorylinksParser} and {@link PagelinksParser} recognise this resolver and expose the
 * resolved id through {@link CategorylinksParser#getResolvedTargetId()} respectively
 * {@link PagelinksParser#getResolvedTargetId()}. As no titles are retained, {@link #getTitle(long)}
 * always returns {@code null}.
 */
public final class ResolvedLinkTargets
    implements LinkTargetResolver
{

    private static final Logger LOG = LoggerFactory.getLogger(ResolvedLinkTargets.class);

    /** Returned by {@link #getPageId(long)} and {@link #getCategoryId(long)} for no match. */
    public static final int UNRESOLVED = -1;

    private static final int NS_MAIN = 0;
    private static final int NS_CATEGORY = 14;

    private final Long2IntOpenHashMap pageIdByLtId = new Long2IntOpenHashMap();
    private final Long2IntOpenHashMap categoryIdByLtId = new Long2IntOpenHashMap();
    private final LongOpenHashSet disambiguationLtIds = new LongOpenHashSet();

    private ResolvedLinkTargets()
    {
        pageIdByLtId.defaultReturnValue(UNRESOLVED);
        categoryIdByLtId.defaultReturnValue(UNRESOLVED);
    }

    /**
     * Drains the given {@link LinktargetParser} into a new instance, resolving every article and
     * category target against {@code sink}. The parser is closed afterwards.
     *
     * @param parser The parser to read the {@code linktarget} dump from.
     * @param sink   The sink whose title lookups define the page ids. Its article and category
     *               title maps have to be complete, i.e. the {@code page} table must have been
     *               processed already.
     * @return A populated {@link ResolvedLinkTargets}.
     * @throws IOException Thrown if IO errors occurred while reading the dump.
     */
    public static ResolvedLinkTargets load(LinktargetParser parser, LinkRowSink sink)
        throws IOException
    {
        final ResolvedLinkTargets targets = new ResolvedLinkTargets();
        final String disambiguationTitle = sink.getDisambiguationCategoryTitle();
        long rows = 0;
        try (parser) {
            while (parser.next()) {
                rows++;
                final int namespace = parser.getLtNamespace();
                if (namespace == NS_MAIN) {
                    final Integer pageId = sink.pageIdByTitle(parser.getLtTitle());
                    if (pageId != null) {
                        targets.pageIdByLtId.put(parser.getLtId(), pageId.intValue());
                    }
                }
                else if (namespace == NS_CATEGORY) {
                    final String title = parser.getLtTitle();
                    final Integer categoryId = sink.categoryIdByTitle(title);
                    if (categoryId != null) {
                        targets.categoryIdByLtId.put(parser.getLtId(), categoryId.intValue());
                        if (title.equals(disambiguationTitle)) {
                            targets.disambiguationLtIds.add(parser.getLtId());
                        }
                    }
                }
            }
        }
        LOG.info("Resolved {} article and {} category link targets from the linktarget dump ({} "
                + "rows read).", targets.pageIdByLtId.size(), targets.categoryIdByLtId.size(),
                rows);
        return targets;
    }

    /**
     * @param ltId A {@code linktarget.lt_id} value.
     * @return The page id of the article {@code ltId} denotes, or {@link #UNRESOLVED}.
     */
    public int getPageId(long ltId)
    {
        return pageIdByLtId.get(ltId);
    }

    /**
     * @param ltId A {@code linktarget.lt_id} value.
     * @return The page id of the category {@code ltId} denotes, or {@link #UNRESOLVED}.
     */
    public int getCategoryId(long ltId)
    {
        return categoryIdByLtId.get(ltId);
    }

    /**
     * @param ltId A {@code linktarget.lt_id} value.
     * @return {@code true} if {@code ltId} denotes the disambiguation category of the sink.
     */
    public boolean isDisambiguationTarget(long ltId)
    {
        return disambiguationLtIds.contains(ltId);
    }

    /**
     * Titles are not retained by this resolver.
     *
     * @param ltId A {@code linktarget.lt_id} value.
     * @return Always {@code null}.
     */
    @Override
    public String getTitle(long ltId)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only resolved targets are known, so this is either {@code 0}, {@code 14} or
     * {@link #NAMESPACE_UNKNOWN}.
     */
    @Override
    public int getNamespace(long ltId)
    {
        if (pageIdByLtId.containsKey(ltId)) {
            return NS_MAIN;
        }
        if (categoryIdByLtId.containsKey(ltId)) {
            return NS_CATEGORY;
        }
        return NAMESPACE_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size()
    {
        return (long) pageIdByLtId.size() + categoryIdByLtId.size();
    }
}
