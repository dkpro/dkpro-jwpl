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
package org.dkpro.jwpl.wikimachine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RedirectsTest
{

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "#REDIRECT [[Article]];Article",
            "#REDIRECT [[Article #Anchor]];Article",
            "#REDIRECT [[Article|Alt]];Article",
            "#REDIRECT [[ Article # Anchor | Alt ]];Article",
            "#REDIRECT [[Portal:Recht/Liste der Rechtsthemen]];Recht/Liste_der_Rechtsthemen",
            "#REDIRECT [[Star Wars: Episode I]];Star_Wars:_Episode_I",
            "#REDIRECT [[englische Grammatik]];Englische_Grammatik",
            "#WEITERLEITUNG [[Ziel]] weiterer Text [[Anderes]];Ziel" })
    void testGetRedirectDestination(String pageText, String expected)
    {
        assertEquals(expected, Redirects.getRedirectDestination(pageText));
    }

    @ParameterizedTest
    @ValueSource(strings = { "#REDIRECT [[#]]", "#REDIRECT [[##]]", "#REDIRECT [[|]]",
            "#REDIRECT [[]]", "#REDIRECT Article" })
    void testGetRedirectDestinationReturnsNullForInvalidTargets(String pageText)
    {
        assertNull(Redirects.getRedirectDestination(pageText));
    }
}
