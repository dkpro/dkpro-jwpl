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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PageQueryTest
{

    @Test
    public void testDefaultConstructor()
    {
        PageQuery pq = new PageQuery();
        assertEquals(0, pq.getMinCategories());
        assertEquals(0, pq.getMinIndegree());
        assertEquals(0, pq.getMinOutdegree());
        assertEquals(0, pq.getMinRedirects());
        assertEquals(0, pq.getMinTokens());
        assertEquals("", pq.getTitlePattern());
        assertFalse(pq.onlyArticlePages());
        assertFalse(pq.onlyDisambiguationPages());
    }

    @Test
    public void testSetAndGetMinCategories()
    {
        PageQuery pq = new PageQuery();
        pq.setMinCategories(5);
        assertEquals(5, pq.getMinCategories());
    }

    @Test
    public void testSetAndGetMinIndegree()
    {
        PageQuery pq = new PageQuery();
        pq.setMinIndegree(10);
        assertEquals(10, pq.getMinIndegree());
    }

    @Test
    public void testSetAndGetMinOutdegree()
    {
        PageQuery pq = new PageQuery();
        pq.setMinOutdegree(7);
        assertEquals(7, pq.getMinOutdegree());
    }

    @Test
    public void testSetAndGetMinRedirects()
    {
        PageQuery pq = new PageQuery();
        pq.setMinRedirects(3);
        assertEquals(3, pq.getMinRedirects());
    }

    @Test
    public void testSetAndGetMinTokens()
    {
        PageQuery pq = new PageQuery();
        pq.setMinTokens(100);
        assertEquals(100, pq.getMinTokens());
    }

    @Test
    public void testSetAndGetTitlePattern()
    {
        PageQuery pq = new PageQuery();
        pq.setTitlePattern("Test%");
        assertEquals("Test%", pq.getTitlePattern());
    }

    @Test
    public void testSetAndGetOnlyArticlePages()
    {
        PageQuery pq = new PageQuery();
        pq.setOnlyArticlePages(true);
        assertTrue(pq.onlyArticlePages());

        pq.setOnlyArticlePages(false);
        assertFalse(pq.onlyArticlePages());
    }

    @Test
    public void testSetAndGetOnlyDisambiguationPages()
    {
        PageQuery pq = new PageQuery();
        pq.setOnlyDisambiguationPages(true);
        assertTrue(pq.onlyDisambiguationPages());

        pq.setOnlyDisambiguationPages(false);
        assertFalse(pq.onlyDisambiguationPages());
    }

    @Test
    public void testGetQueryInfo()
    {
        PageQuery pq = new PageQuery();
        String queryInfo = pq.getQueryInfo();
        assertNotNull(queryInfo);
        assertTrue(queryInfo.contains("MinCategories:"));
        assertTrue(queryInfo.contains("MinIndegree:"));
        assertTrue(queryInfo.contains("MinOutdegree:"));
        assertTrue(queryInfo.contains("MinRedirects:"));
        assertTrue(queryInfo.contains("MinTokens:"));
        assertTrue(queryInfo.contains("Title pattern:"));
        assertTrue(queryInfo.contains("Only article pages:"));
        assertTrue(queryInfo.contains("Only disambiguation pages:"));
    }

    @Test
    public void testGetQueryInfoWithCustomValues()
    {
        PageQuery pq = new PageQuery();
        pq.setMinCategories(5);
        pq.setMinIndegree(10);
        pq.setMinOutdegree(15);
        pq.setMinRedirects(20);
        pq.setMinTokens(25);
        pq.setTitlePattern("TestPattern");
        pq.setOnlyArticlePages(true);
        pq.setOnlyDisambiguationPages(true);

        String queryInfo = pq.getQueryInfo();
        assertTrue(queryInfo.contains("MinCategories: 5"));
        assertTrue(queryInfo.contains("MinIndegree:   10"));
        assertTrue(queryInfo.contains("MinOutdegree:  15"));
        assertTrue(queryInfo.contains("MinRedirects:  20"));
        assertTrue(queryInfo.contains("MinTokens:     25"));
        assertTrue(queryInfo.contains("Title pattern: TestPattern"));
        assertTrue(queryInfo.contains("Only article pages:        true"));
        assertTrue(queryInfo.contains("Only disambiguation pages: true"));
    }
}
