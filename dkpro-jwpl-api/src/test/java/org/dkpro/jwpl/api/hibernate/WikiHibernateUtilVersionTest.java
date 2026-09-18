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
package org.dkpro.jwpl.api.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the detection of the Hibernate major version which {@link WikiHibernateUtil#reattach}
 * relies on.
 */
public class WikiHibernateUtilVersionTest
{

    @ParameterizedTest
    @CsvSource({ "6.6.40.Final, 6", "7.4.5.Final, 7", "8, 8" })
    void testMajorVersion(String versionString, int expected)
    {
        assertEquals(expected, WikiHibernateUtil.majorVersion(versionString));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "[WORKING]", "x.y.z" })
    void testUnknownMajorVersionSelectsTheMostRecentBehavior(String versionString)
    {
        assertEquals(Integer.MAX_VALUE, WikiHibernateUtil.majorVersion(versionString));
    }
}
