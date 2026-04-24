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

import org.dkpro.jwpl.api.testdb.DbEngineCondition;
import org.dkpro.jwpl.api.testdb.JwplTestDatabase;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Shared base class that hands subclasses a {@link DatabaseConfiguration} pointing
 * at the JVM-wide test database managed by {@link JwplTestDatabase}. Pick the engine
 * via the {@code jwpl.test.db} system property ({@code hsqldb}, {@code mariadb},
 * {@code mysql}); tests needing Docker are skipped automatically when Docker is absent.
 */
@ExtendWith(DbEngineCondition.class)
public abstract class BaseJWPLTest
{

    protected static Wikipedia wiki;

    protected static DatabaseConfiguration obtainDbConfiguration()
    {
        return JwplTestDatabase.instance().configuration();
    }
}
