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
import java.util.function.IntPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * A memory efficient {@link LinkTargetResolver} backed by two primitive keyed fastutil hash maps.
 * <p>
 * A full {@code linktarget} table of a large wiki holds hundreds of millions of rows, so neither
 * boxing the keys nor allocating one wrapper object per entry is affordable. Only the namespaces a
 * caller is actually interested in should be loaded; see
 * {@link #load(LinktargetParser, IntPredicate)} and {@link #ARTICLE_TALK_AND_CATEGORY}.
 */
public final class FastUtilLinkTargetResolver
    implements LinkTargetResolver
{

    private static final Logger LOG = LoggerFactory.getLogger(FastUtilLinkTargetResolver.class);

    /**
     * Keeps the namespaces the JWPL import pipeline is able to match a link against: articles
     * ({@code 0}), article talk pages ({@code 1}) and categories ({@code 14}).
     */
    public static final IntPredicate ARTICLE_TALK_AND_CATEGORY = ns -> ns == 0 || ns == 1
            || ns == 14;

    private final Long2ObjectOpenHashMap<String> titleById = new Long2ObjectOpenHashMap<>();
    private final Long2IntOpenHashMap namespaceById = new Long2IntOpenHashMap();

    /**
     * Instantiates an empty {@link FastUtilLinkTargetResolver}.
     */
    public FastUtilLinkTargetResolver()
    {
        namespaceById.defaultReturnValue(NAMESPACE_UNKNOWN);
    }

    /**
     * Registers a link target.
     *
     * @param ltId      The {@code lt_id} of the target.
     * @param namespace The {@code lt_namespace} of the target.
     * @param title     The {@code lt_title} of the target.
     */
    public void add(long ltId, int namespace, String title)
    {
        titleById.put(ltId, title);
        namespaceById.put(ltId, namespace);
    }

    /**
     * Drains the given {@link LinktargetParser} into a new resolver, keeping only the rows whose
     * namespace is accepted by {@code keepNamespace}. The parser is closed afterwards.
     *
     * @param parser        The parser to read the {@code linktarget} dump from.
     * @param keepNamespace The namespaces to retain. Must not be {@code null}.
     * @return A populated {@link FastUtilLinkTargetResolver}.
     * @throws IOException Thrown if IO errors occurred while reading the dump.
     */
    public static FastUtilLinkTargetResolver load(LinktargetParser parser,
            IntPredicate keepNamespace)
        throws IOException
    {
        final FastUtilLinkTargetResolver resolver = new FastUtilLinkTargetResolver();
        long rows = 0;
        try (parser) {
            while (parser.next()) {
                rows++;
                if (keepNamespace.test(parser.getLtNamespace())) {
                    resolver.add(parser.getLtId(), parser.getLtNamespace(), parser.getLtTitle());
                }
            }
        }
        LOG.info("Loaded {} link targets from the linktarget dump ({} rows read).",
                resolver.size(), rows);
        return resolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(long ltId)
    {
        return titleById.get(ltId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNamespace(long ltId)
    {
        return namespaceById.get(ltId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size()
    {
        return titleById.size();
    }
}
