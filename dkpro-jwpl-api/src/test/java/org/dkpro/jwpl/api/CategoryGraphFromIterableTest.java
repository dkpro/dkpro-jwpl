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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the construction of a {@link CategoryGraph} from an {@link Iterable} of categories, as in
 * {@code new CategoryGraph(wiki, category.getDescendants())}. Every test here runs under a timeout:
 * consuming such an iterable used to never terminate (see issue #10), and a hanging test is what
 * that failure looks like.
 */
public class CategoryGraphFromIterableTest
    extends BaseJWPLTest
{

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

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = SEPARATE_THREAD)
    public void testGraphOfTheDescendantsOfACategory() throws WikiApiException
    {
        // the category 'UKP' has the nine descendants 7 to 15
        Category ukp = wiki.getCategory("UKP");

        CategoryGraph catGraph = new CategoryGraph(wiki, ukp.getDescendants());

        assertEquals(9, catGraph.getNumberOfNodes());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = SEPARATE_THREAD)
    public void testGraphOfAnExplicitListOfCategories() throws WikiApiException
    {
        List<Category> categories = List.of(wiki.getCategory("UKP"), wiki.getCategory("SIR"));

        CategoryGraph catGraph = new CategoryGraph(wiki, categories);

        assertEquals(2, catGraph.getNumberOfNodes());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = SEPARATE_THREAD)
    public void testGraphOfAnEmptyIterable() throws WikiApiException
    {
        CategoryGraph catGraph = new CategoryGraph(wiki, List.<Category> of());

        assertEquals(0, catGraph.getNumberOfNodes());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = SEPARATE_THREAD)
    public void testFilteredGraphOfTheDescendantsOfACategory() throws WikiApiException
    {
        Category ukp = wiki.getCategory("UKP");

        CategoryGraph catGraph = new CategoryGraph(wiki, ukp.getDescendants(), List.of("SIR"));

        assertEquals(8, catGraph.getNumberOfNodes());
    }
}
