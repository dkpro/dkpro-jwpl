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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.exception.WikiInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the schema probes of {@link RevisionApi} against a minimal fake JDBC connection that
 * answers {@code SHOW INDEX} and {@code SHOW TABLES} and counts how often they are executed.
 */
public class RevisionApiSchemaProbeTest
{

    private final List<String> indexNames = new ArrayList<>();

    private final List<String> tableNames = new ArrayList<>();

    private int showIndexCount;

    private int showTablesCount;

    private RevisionApi revisionApi;

    @BeforeEach
    public void setUp()
    {
        revisionApi = new RevisionApi(new RevisionAPIConfiguration(), fakeConnection());
    }

    @Test
    public void testIndexProbeIsCached() throws Exception
    {
        indexNames.add("articleids");
        for (int i = 0; i < 5; i++) {
            assertTrue(revisionApi.getRevisionTimestamps(1).isEmpty());
        }
        assertEquals(1, showIndexCount);
    }

    @Test
    public void testMissingIndexIsCheckedAgain() throws Exception
    {
        assertThrows(WikiInitializationException.class, () -> revisionApi.getRevisionTimestamps(1));
        indexNames.add("articleids");
        assertTrue(revisionApi.getRevisionTimestamps(1).isEmpty());
        assertTrue(revisionApi.getRevisionTimestamps(1).isEmpty());
        assertEquals(2, showIndexCount);
    }

    @Test
    public void testNamedIndexInFirstRowIsFound() throws Exception
    {
        indexNames.add("userids");
        assertTrue(revisionApi.getUserRevisionIds(1).isEmpty());
    }

    @Test
    public void testNamedIndexInLaterRowIsFound() throws Exception
    {
        indexNames.add("articleids");
        indexNames.add("userids");
        assertTrue(revisionApi.getUserRevisionIds(1).isEmpty());
    }

    @Test
    public void testMissingNamedIndex()
    {
        indexNames.add("articleids");
        assertThrows(WikiInitializationException.class, () -> revisionApi.getUserRevisionIds(1));
    }

    @Test
    public void testTableProbeIsCached() throws Exception
    {
        tableNames.add("revisions");
        assertThrows(WikiInitializationException.class, () -> revisionApi.getUserGroups(1));
        tableNames.add("USER_GROUPS");
        for (int i = 0; i < 5; i++) {
            assertTrue(revisionApi.getUserGroups(1).isEmpty());
        }
        assertEquals(2, showTablesCount);
    }

    private Connection fakeConnection()
    {
        return proxy(Connection.class, (method, args) -> {
            if ("prepareStatement".equals(method)) {
                return fakeStatement((String) args[0]);
            }
            return null;
        });
    }

    private PreparedStatement fakeStatement(String sql)
    {
        return proxy(PreparedStatement.class, (method, args) -> {
            if (!"executeQuery".equals(method)) {
                return null;
            }
            if (sql.startsWith("SHOW INDEX")) {
                showIndexCount++;
                // Key_name is the third column of SHOW INDEX
                return fakeResultSet(indexNames, 3);
            }
            if (sql.startsWith("SHOW TABLES")) {
                showTablesCount++;
                return fakeResultSet(tableNames, 1);
            }
            return fakeResultSet(List.of(), 1);
        });
    }

    private ResultSet fakeResultSet(List<String> values, int column)
    {
        final List<String> rows = new ArrayList<>(values);
        final int[] cursor = { -1 };
        return proxy(ResultSet.class, (method, args) -> {
            switch (method) {
            case "next":
                return ++cursor[0] < rows.size();
            case "getString":
                assertEquals(column, args[0]);
                return rows.get(cursor[0]);
            default:
                return null;
            }
        });
    }

    private interface Handler
    {
        Object handle(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, Handler handler)
    {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> handler.handle(method.getName(), args)));
    }
}
