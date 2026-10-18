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

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests that the backend specific Hibernate settings follow the JDBC url. Building the
 * configuration does not open a connection, so no database is needed.
 */
public class BackendPropertiesTest
{

    private static final String HBM2DDL = "hibernate.hbm2ddl.auto";

    @ParameterizedTest
    @CsvSource({
        "org.hsqldb.jdbcDriver, jdbc:hsqldb:mem:jwpl, none",
        "org.mariadb.jdbc.Driver, jdbc:mariadb://localhost/jwpl, validate",
        "com.mysql.cj.jdbc.Driver, jdbc:mysql://localhost/jwpl, validate",
        "org.postgresql.Driver, jdbc:postgresql://localhost/jwpl, validate" })
    public void testSchemaValidationFollowsTheUrl(String driver, String url, String expected)
    {
        DatabaseConfiguration config = new DatabaseConfiguration(driver, url, "localhost", "jwpl",
                "user", "password", Language._test);
        assertEquals(expected, WikiHibernateUtil.getConfiguration(config, MetaData.class)
                .getProperties().getProperty(HBM2DDL));
    }
}
