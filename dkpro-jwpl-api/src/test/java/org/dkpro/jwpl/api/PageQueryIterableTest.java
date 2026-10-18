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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class PageQueryIterableTest
    extends BaseJWPLTest
{

    private PageQuery pq;

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

    @Test
    public void testNumberOfPagesOfAQueryWithoutConditions() throws WikiApiException
    {
        assertEquals(34, new PageQueryIterable(wiki, pq).size());
        assertEquals(34, wiki.getNumberOfPages(pq));
    }

    @Test
    public void testNumberOfPagesMatchesTheNumberOfIteratedPages() throws WikiApiException
    {
        pq.setTitlePattern("Wikipedia%");
        pq.setOnlyArticlePages(true);

        int iterated = 0;
        for (Page page : wiki.getPages(pq)) {
            assertNotNull(page);
            iterated++;
        }

        assertTrue(iterated >= 1);
        assertEquals(iterated, wiki.getNumberOfPages(pq));
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

    private static Stream<Arguments> constrainedQueries()
    {
        return Stream.of(
                Arguments.of("minIndegree=1", 17, (Consumer<PageQuery>) q -> q.setMinIndegree(1)),
                Arguments.of("maxIndegree=2", 26, (Consumer<PageQuery>) q -> q.setMaxIndegree(2)),
                Arguments.of("minOutdegree=3", 9, (Consumer<PageQuery>) q -> q.setMinOutdegree(3)),
                Arguments.of("maxOutdegree=1", 22, (Consumer<PageQuery>) q -> q.setMaxOutdegree(1)),
                Arguments.of("minRedirects=1", 6, (Consumer<PageQuery>) q -> q.setMinRedirects(1)),
                Arguments.of("minCategories=2", 7,
                        (Consumer<PageQuery>) q -> q.setMinCategories(2)),
                Arguments.of("maxTokens=20", 32, (Consumer<PageQuery>) q -> q.setMaxTokens(20)),
                Arguments.of("invalid indegree range", 34, (Consumer<PageQuery>) q -> {
                    q.setMinIndegree(5);
                    q.setMaxIndegree(2);
                }),
                Arguments.of("articles, title pattern, degree range", 1,
                        (Consumer<PageQuery>) q -> {
                            q.setOnlyArticlePages(true);
                            q.setTitlePattern("Wiki%");
                            q.setMinIndegree(1);
                            q.setMaxOutdegree(20);
                        }),
                Arguments.of("articles, redirects, categories and tokens", 1,
                        (Consumer<PageQuery>) q -> {
                            q.setOnlyArticlePages(true);
                            q.setMaxRedirects(1);
                            q.setMinCategories(1);
                            q.setMinTokens(10);
                        }));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("constrainedQueries")
    public void testConstrainedQueryMatchesPerPageEvaluation(String name, int expected,
            Consumer<PageQuery> config)
        throws WikiApiException
    {
        config.accept(pq);
        List<Integer> selected = new ArrayList<>();
        for (Page page : wiki.getPages(pq)) {
            assertNotNull(page);
            selected.add(page.getPageId());
        }

        PageQuery reference = new PageQuery();
        config.accept(reference);
        List<Integer> expectedIds = evaluatePerPage(reference);

        assertEquals(expected, selected.size());
        assertEquals(expectedIds, selected.stream().sorted().toList());

        PageQuery countQuery = new PageQuery();
        config.accept(countQuery);
        assertEquals(expected, wiki.getNumberOfPages(countQuery));
    }

    /**
     * Evaluates the query constraints page by page through the public {@link Page} API, as a
     * reference for the optimized evaluation in {@link PageQueryIterable}.
     */
    private static List<Integer> evaluatePerPage(PageQuery q) throws WikiApiException
    {
        List<Integer> ids = new ArrayList<>();
        for (Integer pageId : wiki.getPageIds()) {
            Page page = wiki.getPage(pageId);
            if (q.onlyArticlePages() && page.isDisambiguation()
                    || q.onlyDisambiguationPages() && !page.isDisambiguation()) {
                continue;
            }
            String pattern = q.getTitlePattern();
            if (pattern != null && !pattern.isBlank() && !page.getTitle().getRawTitleText()
                    .matches(pattern.replace("%", ".*").replace("_", "."))) {
                continue;
            }
            if (!inRange(page.getNumberOfInlinks(), q.getMinIndegree(), q.getMaxIndegree())
                    || !inRange(page.getNumberOfOutlinks(), q.getMinOutdegree(),
                            q.getMaxOutdegree())
                    || !inRange(page.getRedirects().size(), q.getMinRedirects(),
                            q.getMaxRedirects())
                    || !inRange(page.getCategories().size(), q.getMinCategories(),
                            q.getMaxCategories())
                    || !inRange(page.getPlainText().split(" ").length, q.getMinTokens(),
                            q.getMaxTokens())) {
                continue;
            }
            ids.add(pageId);
        }
        ids.sort(null);
        return ids;
    }

    private static boolean inRange(int value, int min, int max)
    {
        if (min < 0 || max < 0 || min > max) {
            return true;
        }
        return value >= min && value <= max;
    }
}
