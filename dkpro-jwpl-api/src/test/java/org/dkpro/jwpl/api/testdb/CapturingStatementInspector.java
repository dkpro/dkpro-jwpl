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
package org.dkpro.jwpl.api.testdb;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Records the SQL Hibernate prepares on the current thread, so a test can inspect the plan of the
 * statement an API call really issued rather than a copy of a query constant. It is registered on
 * the variant databases, see {@link JwplTestDatabase#provisionVariant(ColumnProfile)}.
 */
public class CapturingStatementInspector
    implements StatementInspector
{
    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<List<String>> CAPTURED = ThreadLocal
            .withInitial(ArrayList::new);

    @Override
    public String inspect(String sql)
    {
        CAPTURED.get().add(sql);
        return sql;
    }

    /**
     * Forgets the statements recorded so far on the current thread.
     */
    public static void clear()
    {
        CAPTURED.get().clear();
    }

    /**
     * @return The statements recorded on the current thread since the last {@link #clear()}.
     */
    public static List<String> captured()
    {
        return List.copyOf(CAPTURED.get());
    }
}
