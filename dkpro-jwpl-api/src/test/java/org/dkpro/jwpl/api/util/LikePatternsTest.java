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
package org.dkpro.jwpl.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class LikePatternsTest
{
    @Test
    void testEscapeLeavesOtherCharactersUntouched()
    {
        assertEquals("", LikePatterns.escape(""));
        assertEquals("Plain title\\x'", LikePatterns.escape("Plain title\\x'"));
    }

    @Test
    void testEscapeEscapesWildcardsAndEscapeChar()
    {
        assertEquals("Under!_score!%sign!!bang", LikePatterns.escape("Under_score%sign!bang"));
    }

    @Test
    void testPrefixAppendsWildcard()
    {
        assertEquals("Discussion:Under!_scoreA/%", LikePatterns.prefix("Discussion:Under_scoreA/"));
        assertEquals("%", LikePatterns.prefix(""));
    }

    @Test
    void testEscapeClause()
    {
        assertEquals(" escape '!'", LikePatterns.ESCAPE_CLAUSE);
    }

    @Test
    void testPrefixMatchesLiterallyOnHsqldb() throws SQLException
    {
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:likepatterns", "sa", "");
                Statement s = c.createStatement()) {
            s.execute("CREATE TABLE names (name VARCHAR(255))");
            s.execute("INSERT INTO names VALUES ('Under_scoreA'), ('UnderXscoreA'), "
                    + "('Pct%Literal'), ('PctXYLiteral'), ('Bang!x'), ('Bangx'), "
                    + "('Back\\slash'), ('BackXslash')");
            assertEquals(List.of("Under_scoreA"), select(c, "Under_score"));
            assertEquals(List.of("Pct%Literal"), select(c, "Pct%"));
            assertEquals(List.of("Bang!x"), select(c, "Bang!"));
            assertEquals(List.of("Back\\slash"), select(c, "Back\\"));
            s.execute("SHUTDOWN");
        }
    }

    private static List<String> select(Connection c, String literalPrefix) throws SQLException
    {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT name FROM names WHERE name LIKE ?" + LikePatterns.ESCAPE_CLAUSE
                        + " ORDER BY name")) {
            ps.setString(1, LikePatterns.prefix(literalPrefix));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }
        return names;
    }
}
