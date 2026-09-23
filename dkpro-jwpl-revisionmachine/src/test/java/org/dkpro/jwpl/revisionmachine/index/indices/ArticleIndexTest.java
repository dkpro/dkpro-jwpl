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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks the output of the {@link ArticleIndex} and that the passed list is emptied (see issue
 * #595).
 */
class ArticleIndexTest
{

    private static ArticleIndexData block(final long primaryKey, final long start, final long end)
    {
        ArticleIndexData info = new ArticleIndexData();
        info.setFullRevisionPrimaryKey(primaryKey);
        info.setStartRevisionCount(start);
        info.setEndRevisionCount(end);
        return info;
    }

    @Test
    void testOutputAndListIsEmptied()
    {
        ArticleIndex index = new ArticleIndex(16760832);
        List<ArticleIndexData> infoList = new ArrayList<>();

        infoList.add(block(11, 1, 5));
        infoList.add(block(16, 6, 7));
        index.add(1, 100L, 200L, infoList);
        assertTrue(infoList.isEmpty());

        infoList.add(block(21, 1, 3));
        index.add(2, 300L, 400L, infoList);
        assertTrue(infoList.isEmpty());

        index.finalizeIndex();

        assertEquals(1, index.size());
        assertEquals("INSERT INTO index_articleID_rc_ts VALUES "
                + "(1,'11 16','1 5 6 7',100,200),(2,'21','1 3',300,400);",
                index.remove().toString());
    }
}
