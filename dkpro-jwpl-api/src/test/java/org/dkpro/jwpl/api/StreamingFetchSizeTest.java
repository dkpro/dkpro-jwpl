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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the fetch size {@link Wikipedia#streamingFetchSize(String)} selects per JDBC driver.
 */
class StreamingFetchSizeTest
{
    static Stream<Arguments> drivers()
    {
        return Stream.of(
                Arguments.of("com.mysql.cj.jdbc.Driver", Integer.MIN_VALUE),
                Arguments.of("com.mysql.jdbc.Driver", Integer.MIN_VALUE),
                Arguments.of("org.mariadb.jdbc.Driver", Wikipedia.MARIADB_STREAMING_FETCH_SIZE),
                Arguments.of("org.postgresql.Driver", Wikipedia.POSTGRESQL_STREAMING_FETCH_SIZE),
                Arguments.of("org.hsqldb.jdbc.JDBCDriver", Wikipedia.DEFAULT_STREAMING_FETCH_SIZE),
                Arguments.of("org.hsqldb.jdbcDriver", Wikipedia.DEFAULT_STREAMING_FETCH_SIZE),
                Arguments.of("org.h2.Driver", Wikipedia.DEFAULT_STREAMING_FETCH_SIZE));
    }

    @ParameterizedTest
    @MethodSource("drivers")
    void testStreamingFetchSizePerDriver(String driver, int expected)
    {
        assertEquals(expected, Wikipedia.streamingFetchSize(driver));
    }

    /**
     * The JDBC specification requires a fetch size {@code >= 0}; only MySQL Connector/J deviates.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "org.mariadb.jdbc.Driver", "org.postgresql.Driver",
            "org.hsqldb.jdbc.JDBCDriver", "", "com.mysqlx.Driver", "mariadb" })
    void testStreamingFetchSizeIsNotNegativeForOtherDrivers(String driver)
    {
        assertTrue(Wikipedia.streamingFetchSize(driver) > 0, String.valueOf(driver));
    }
}
