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
package org.dkpro.jwpl.wikimachine.dump.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link UnsyncBufferedReader} and verifies that a {@link StreamTokenizer} fed by it yields
 * exactly the token stream it yields when fed by a {@link BufferedReader} (see issue #552).
 */
class UnsyncBufferedReaderTest
{

    /** Escapes, a multibyte title, a MySQL hint, a negative number and several statements. */
    private static final String EDGE_CASES = """
            /*!40101 SET NAMES binary*/;
            -- comment line
            INSERT INTO `linktarget` VALUES (1,-14,'Café_\\'x\\'_\\\\_\\0_\\n_\\t_\\r_\\b_\\Z_\\"_\\101'),\
            (2,0,NULL),(3,0,"döuble"),(4,1,'😀_emoji');
            INSERT INTO `linktarget` VALUES (5,0,'second_statement'),(6,0,'x');
            """;

    static Stream<Arguments> inputs()
    {
        final List<Arguments> args = new ArrayList<>();
        for (String sql : List.of(SqlFixtures.CATEGORYLINKS_LEGACY,
                SqlFixtures.CATEGORYLINKS_LEGACY_NO_TYPE, SqlFixtures.CATEGORYLINKS_NORMALISED,
                SqlFixtures.PAGELINKS_NORMALISED, SqlFixtures.PAGELINKS_PRE_2014,
                SqlFixtures.PAGELINKS_POST_2014, SqlFixtures.LINKTARGET, EDGE_CASES)) {
            for (int size : new int[] { 1, 2, 3, 7, 64, UnsyncBufferedReader.DEFAULT_BUFFER_SIZE }) {
                args.add(Arguments.of(sql, size));
            }
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void yieldsSameTokensAsBufferedReader(String sql, int bufferSize) throws IOException
    {
        final List<String> expected = tokens(new BufferedReader(new InputStreamReader(
                SqlFixtures.stream(sql), StandardCharsets.UTF_8)));
        final List<String> actual = tokens(new UnsyncBufferedReader(new InputStreamReader(
                SqlFixtures.stream(sql), StandardCharsets.UTF_8), bufferSize));
        assertEquals(expected, actual);
    }

    @Test
    void deliversCharsAcrossRefills() throws IOException
    {
        final String text = "abcdefghij";
        try (Reader reader = new UnsyncBufferedReader(new StringReader(text), 3)) {
            assertEquals('a', reader.read());
            final char[] chunk = new char[8];
            // a bulk read never spans a refill, it returns the rest of the buffer
            assertEquals(2, reader.read(chunk, 0, chunk.length));
            assertEquals("bc", new String(chunk, 0, 2));
            assertEquals(0, reader.read(chunk, 0, 0));
            final StringBuilder rest = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                rest.append((char) c);
            }
            assertEquals("defghij", rest.toString());
            assertEquals(-1, reader.read());
            assertEquals(-1, reader.read(chunk, 0, chunk.length));
        }
    }

    @Test
    void rejectsInvalidArguments()
    {
        assertThrows(NullPointerException.class, () -> new UnsyncBufferedReader(null));
        assertThrows(IllegalArgumentException.class,
                () -> new UnsyncBufferedReader(new StringReader(""), 0));
    }

    /**
     * Tokenizes {@code reader} with the syntax table {@link SQLFileParser} uses and renders each
     * token including its type, numeric value and string value.
     */
    private static List<String> tokens(Reader reader) throws IOException
    {
        final StreamTokenizer st = new StreamTokenizer(reader);
        st.wordChars('_', '_');
        final List<String> tokens = new ArrayList<>();
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            tokens.add(st.ttype + "|" + st.nval + "|" + st.sval + "|" + st.lineno());
        }
        return tokens;
    }
}
