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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TitleIteratorTest
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

    @Test
    public void test_titleIteratorTest()
    {

        int nrOfTitles = 0;
        Iterable<Title> iterable = wiki.getTitles();
        assertNotNull(iterable);
        for (Title t : iterable) {
            assertNotNull(t);
            nrOfTitles++;
        }
        assertEquals(42, nrOfTitles, "Number of titles == 42");

    }

    /**
     * Every title must be returned exactly once, independent of the batch boundaries.
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 5, 41, 42, 43, 500 })
    public void test_titleIteratorCompleteAndWithoutDuplicates(int bufferSize) throws Exception
    {
        final String sql = "select p.name from PageMapLine as p";
        List<String> names = wiki
                .__inTransaction(session -> session.createQuery(sql, String.class).list());
        List<String> expected = new ArrayList<>();
        for (String name : names) {
            expected.add(new Title(name).getWikiStyleTitle());
        }

        List<String> actual = new ArrayList<>();
        for (Title t : new TitleIterable(wiki, bufferSize)) {
            assertNotNull(t);
            actual.add(t.getWikiStyleTitle());
        }

        assertEquals(42, actual.size());
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }
}
