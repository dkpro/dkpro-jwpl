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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CategoryIteratorTest
    extends BaseJWPLTest
{

    /**
     * Made this static so that following tests don't run if assumption fails. (With AT_Before,
     * tests also would not be executed but marked as passed) This could be changed back as soon as
     * JUnit ignored tests after failed assumptions
     */
    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }
    }

    /**
     * The test wikipedia contains 17 categories.
     */
    @Test
    public void test_categoryIteratorTest()
    {
        int nrOfPages = 0;

        for (Category c : wiki.getCategories()) {
            nrOfPages++;
        }
        assertEquals(17, nrOfPages, "Number of categories == 17");

    }

    /**
     * The test wikipedia contains 17 categories.
     */
    @Test
    public void test_categoryIteratorTestBufferSize()
    {

        for (int bufferSize = 1; bufferSize <= 100; bufferSize += 5) {
            Iterator<Category> catIter = wiki.getCategories(bufferSize).iterator();
            int nrOfPages = 0;
            while (catIter.hasNext()) {
                @SuppressWarnings("unused")
                Category c = catIter.next();
                nrOfPages++;
            }
            assertEquals(17, nrOfPages, "Number of categories == 17");
        }
    }

    /**
     * Every category must be returned exactly once, independent of the batch boundaries.
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 5, 16, 17, 18, 500 })
    public void test_categoryIteratorCompleteAndWithoutDuplicates(int bufferSize)
    {
        Set<Integer> expected = wiki.__getCategories();

        List<Integer> pageIds = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (Category c : wiki.getCategories(bufferSize)) {
            assertNotNull(c);
            pageIds.add(c.getPageId());
            ids.add(c.__getId());
        }

        assertEquals(expected.size(), pageIds.size());
        assertEquals(pageIds.size(), new HashSet<>(pageIds).size());
        assertEquals(expected, new HashSet<>(pageIds));
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(ids.get(i - 1) < ids.get(i), "Categories must be ordered by id");
        }
    }

    /**
     * Lazy collections of categories returned by the iterator must remain accessible.
     */
    @Test
    public void test_categoryIteratorLazyAccess() throws Exception
    {
        for (Category c : wiki.getCategories(3)) {
            Category loaded = wiki.getCategory(c.getPageId());
            assertEquals(loaded.getParentIDs(), c.getParentIDs());
            assertEquals(loaded.getChildrenIDs(), c.getChildrenIDs());
            assertEquals(loaded.getArticleIds(), c.getArticleIds());
            assertEquals(loaded.getParents().size(), c.getParents().size());
            assertEquals(loaded.getChildren().size(), c.getChildren().size());
        }
    }
}
