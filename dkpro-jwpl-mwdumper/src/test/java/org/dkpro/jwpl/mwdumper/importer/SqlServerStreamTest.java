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
package org.dkpro.jwpl.mwdumper.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SqlServerStreamTest
{
    /** Number of statements created on the fake connection. */
    private int created;

    /** Number of statements closed on the fake connection. */
    private int closed;

    /** SQL strings passed to {@link Statement#execute(String)}. */
    private final List<String> executed = new ArrayList<>();

    private boolean connectionClosed;

    private Connection fakeConnection(boolean failOnExecute)
    {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "createStatement":
                        created++;
                        return fakeStatement(failOnExecute);
                    case "close":
                        connectionClosed = true;
                        return null;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private Statement fakeStatement(boolean failOnExecute)
    {
        return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { Statement.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "setEscapeProcessing":
                        return null;
                    case "execute":
                        if (failOnExecute) {
                            throw new SQLException("failure");
                        }
                        executed.add((String) args[0]);
                        return false;
                    case "close":
                        closed++;
                        return null;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    @Test
    public void testWriteStatementClosesEachStatement() throws IOException
    {
        SqlServerStream stream = new SqlServerStream(fakeConnection(false));
        stream.writeStatement("BEGIN");
        stream.writeStatement(new StringBuilder("INSERT INTO t VALUES (1)"));
        stream.writeStatement("COMMIT");

        assertEquals(List.of("BEGIN", "INSERT INTO t VALUES (1)", "COMMIT"), executed);
        assertEquals(3, created);
        assertEquals(created, closed);

        stream.close();
        assertTrue(connectionClosed);
    }

    @Test
    public void testWriteStatementClosesStatementOnFailure()
    {
        SqlServerStream stream = new SqlServerStream(fakeConnection(true));
        IOException e = assertThrows(IOException.class, () -> stream.writeStatement("BEGIN"));

        assertTrue(e.getCause() instanceof SQLException);
        assertEquals(1, created);
        assertEquals(1, closed);
    }
}
