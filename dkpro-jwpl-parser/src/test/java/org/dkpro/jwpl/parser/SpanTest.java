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
package org.dkpro.jwpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpanTest
{

    @ParameterizedTest(name = "(2, 8) contains ({0}, {1}): {2}")
    @CsvSource({
            "2, 8, true",
            "3, 5, true",
            "2, 2, true",
            "5, 5, true",
            "8, 8, false",
            "1, 5, false",
            "5, 9, false",
            "0, 2, false",
            "8, 10, false",
            "0, 10, false" })
    void contains(int start, int end, boolean expected)
    {
        assertEquals(expected, new Span(2, 8).contains(new Span(start, end)));
    }
}
