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
package org.dkpro.jwpl.revisionmachine.index.indices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks the exact output of the {@link ChronoIndex} (see issue #595).
 */
class ChronoIndexTest
{

    private static void feed(final ChronoIndex index)
    {
        // out-of-order timestamps, two revisions share a timestamp
        index.add(1, 1, 100L);
        index.add(1, 2, 300L);
        index.add(1, 3, 200L);
        index.add(1, 4, 200L);

        // chronological order: no entry is written
        index.add(2, 1, 10L);
        index.add(2, 2, 20L);

        index.add(3, 1, 50L);
        index.add(3, 2, 40L);

        index.finalizeIndex();
    }

    @Test
    void testDataFileOutput()
    {
        ChronoIndex index = new ChronoIndex();
        feed(index);

        assertEquals(1, index.size());
        assertEquals("1,\"2 4 3 2 4 3\",\"2 3 3 4 4 2\"\n" + "3,\"1 2 2 1\",\"1 2 2 1\"\n",
                index.remove().toString());
    }

    @Test
    void testSqlOutput()
    {
        ChronoIndex index = new ChronoIndex(16760832);
        feed(index);

        assertEquals(1, index.size());
        assertEquals("INSERT INTO index_chronological VALUES "
                + "(1,'2 4 3 2 4 3','2 3 3 4 4 2'),(3,'1 2 2 1','1 2 2 1');",
                index.remove().toString());
    }

    @Test
    void testLargeArticle()
    {
        int n = 200_000;
        ChronoIndex index = new ChronoIndex();
        // reverse chronological order: revision i is the (n - i + 1)-th in time
        for (int i = 1; i <= n; i++) {
            index.add(1, i, n - i);
        }
        index.finalizeIndex();

        String row = index.remove().toString();
        assertEquals("1,\"1 " + n + " 2 " + (n - 1), row.substring(0, row.indexOf(" 3 ")));
    }
}
