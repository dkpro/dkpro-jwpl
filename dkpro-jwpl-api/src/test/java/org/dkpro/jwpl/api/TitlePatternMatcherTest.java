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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TitlePatternMatcherTest
{
    @ParameterizedTest(name = "[{index}] ''{0}'' ~ ''{1}'' = {2}")
    @CsvSource(delimiter = '|', value = {
        // literals are compared exactly
        "Under_score%    | Under_scoreA     | true",
        "Under_score%    | UnderXscoreA     | true",
        "under%          | Under_scoreA     | false",
        "UNDER%          | Under_scoreA     | false",
        "Cafe%           | Café_probe       | false",
        "Café%           | Café_probe       | true",
        "Köln_probe      | Koln_probe       | false",
        // % matches any sequence, also an empty one
        "%               | ''               | true",
        "%               | Anything         | true",
        "A%B             | AB               | true",
        "A%B             | AxyzB            | true",
        "A%B             | AxyzBc           | false",
        "%_%             | ''               | false",
        "Pct%            | Pct%Literal      | true",
        // _ matches exactly one character
        "A_C             | ABC              | true",
        "A_C             | AC               | false",
        "A_C             | ABBC             | false",
        "Emoji__probe    | Emoji_😀_probe   | false",
        "Emoji___probe   | Emoji_😀_probe   | true",
        "Emoji____probe  | Emoji_😀_probe   | true",
        // a backslash escapes the next character or is a literal backslash
        "A\\_C           | A_C              | true",
        "A\\_C           | ABC              | false",
        "A\\_C           | A\\BC            | true",
        "A\\%            | A%               | true",
        "A\\%            | Ax               | false",
        "A\\\\           | A\\              | true",
        "A\\             | A\\              | true",
        "A\\             | A                | false",
        // a pattern without wildcards matches only the name itself
        "Mixed_Case      | Mixed_Case       | true",
        "Mixed_Case      | Mixed_case       | false",
        "Mixed!Case      | Mixed_Case       | false",
        "Trailing_probe  | 'Trailing_probe ' | false",
    })
    void testMatches(String pattern, String name, boolean expected)
    {
        assertEquals(expected, new TitlePatternMatcher(pattern).matches(name));
    }
}
