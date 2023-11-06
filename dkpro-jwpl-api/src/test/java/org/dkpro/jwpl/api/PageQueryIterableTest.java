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

import java.util.Iterator;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class PageQueryIterableTest
    extends BaseJWPLTest
{

    private PageQuery pq;

    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainHSDLDBConfiguration();
        try {
            wiki = new Wikipedia(db);
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }
    }

    @BeforeEach
    public void setup() {
        pq = new PageQuery();
    }

    // Shows now exception occurs during creation
    @Test
    public void testCreatePageQueryIterable() throws WikiApiException
    {
        PageQueryIterable pqi = new PageQueryIterable(wiki, pq);
        assertNotNull(pqi);
    }

    // Example with ' character in titlePattern verifies issue #124
    @ParameterizedTest
    @ValueSource(strings = {"Wikipedia%", "Wiki_edia%", "Moore'%"})
    public void testIteratorWithValidTitlePattern(String input) throws WikiApiException
    {
        pq.setTitlePattern(input);
        pq.setOnlyArticlePages(true);
        PageQueryIterable pqi = new PageQueryIterable(wiki, pq);
        assertNotNull(pqi);
        Iterator<Page> it = pqi.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            count++;
            assertNotNull(it.next());
        }
        assertTrue(count >= 1);
    }

    // Example with null or blank titlePattern => should fetch all ID's in DB
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    public void testIteratorWithEmptyTitlePattern(String input) throws WikiApiException
    {
        pq.setTitlePattern(input);
        PageQueryIterable pqi = new PageQueryIterable(wiki, pq);
        assertNotNull(pqi);
        Iterator<Page> it = pqi.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            count++;
            assertNotNull(it.next());
        }
        assertEquals(34, count);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Wikipedia%", "Wiki_edia%", "Moore'%"})
    public void testIteratorWithMaTokens(String input) throws WikiApiException
    {
        pq.setTitlePattern(input);
        pq.setMaxTokens(20);
        PageQueryIterable pqi = new PageQueryIterable(wiki, pq);
        assertNotNull(pqi);
        Iterator<Page> it = pqi.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            count++;
            assertNotNull(it.next());
        }
        assertTrue(count >= 1);
    }
}
