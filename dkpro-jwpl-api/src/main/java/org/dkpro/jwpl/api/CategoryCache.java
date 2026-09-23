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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A bounded, thread-safe cache of loaded {@link Category.Row category rows}, keyed by their page
 * id. When full, the least recently used entry is evicted.
 * <p>
 * Only immutable {@link Category.Row} snapshots are kept, never Hibernate entities. A cached entry
 * is therefore never associated with a Hibernate session, and threads sharing it cannot interfere
 * with each other's sessions.
 * <p>
 * A cache with a maximum size of {@code 0} is disabled: it stores nothing and every lookup misses.
 *
 * @see DatabaseConfiguration#setCategoryCacheSize(int)
 */
final class CategoryCache
{

    private final int maxSize;
    private final Map<Integer, Category.Row> rows;

    /**
     * Creates a cache for at most {@code maxSize} rows.
     *
     * @param maxSize
     *            The maximum number of cached rows. Must not be negative; {@code 0} disables the
     *            cache.
     * @throws IllegalArgumentException
     *             Thrown if {@code maxSize} is negative.
     */
    CategoryCache(int maxSize)
    {
        if (maxSize < 0) {
            throw new IllegalArgumentException(
                    "The category cache size must not be negative, but was " + maxSize);
        }
        this.maxSize = maxSize;
        // access order turns the insertion ordered map into an LRU map
        this.rows = new LinkedHashMap<>(16, 0.75f, true)
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Category.Row> eldest)
            {
                return size() > CategoryCache.this.maxSize;
            }
        };
    }

    /**
     * @return {@code true} if this cache stores rows, {@code false} if it is disabled.
     */
    boolean isEnabled()
    {
        return maxSize > 0;
    }

    /**
     * @param pageId
     *            The page id of a category.
     * @return The cached row for {@code pageId}, or {@code null} if it is not cached.
     */
    synchronized Category.Row get(int pageId)
    {
        // A lookup changes the access order, so it has to be synchronized, too.
        return isEnabled() ? rows.get(pageId) : null;
    }

    /**
     * Caches a row under its page id, evicting the least recently used row if the cache is full.
     * Does nothing if the cache is disabled.
     *
     * @param row
     *            The row to cache. Must not be {@code null}.
     */
    synchronized void put(Category.Row row)
    {
        if (isEnabled()) {
            rows.put(row.pageId(), row);
        }
    }

    /**
     * @return The number of cached rows.
     */
    synchronized int size()
    {
        return rows.size();
    }

    /**
     * Removes all cached rows.
     */
    synchronized void clear()
    {
        rows.clear();
    }
}
