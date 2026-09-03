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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Arrays;

/**
 * Reads a single {@code (v1,v2,...,vn)} tuple of an SQL {@code INSERT ... VALUES} statement in a
 * column-count agnostic way and gives typed access to the individual values by their zero based
 * position.
 * <p>
 * Reading the whole tuple - rather than walking a hardcoded token pattern - is what allows one
 * parser implementation to serve every historic layout of a MediaWiki dump table: columns that a
 * given layout does not care about are simply read and discarded, and the tuple is always consumed
 * up to its closing parenthesis, so the token stream can never get out of sync.
 * <p>
 * Note that a quoted value which happens to <em>be</em> {@code ')'} or {@code ','} is reported by
 * {@link StreamTokenizer} with the quote character as its type, never as the ordinary characters
 * {@code 41}/{@code 44}. Data can therefore not desynchronise the collection loop.
 */
final class SQLRowReader
{

    /** Marker for "this layout does not have such a column". */
    static final int NO_COLUMN = -1;

    private static final int INITIAL_CAPACITY = 16;

    private final StreamTokenizer st;
    private final String table;

    private int[] types = new int[INITIAL_CAPACITY];
    private double[] numbers = new double[INITIAL_CAPACITY];
    private String[] strings = new String[INITIAL_CAPACITY];
    private int columnsSeen;

    /**
     * @param st    The tokenizer to read from. Must be positioned right behind the opening
     *              {@code '('} of a tuple when {@link #readRow()} is called.
     * @param table The name of the table being read - used in diagnostic messages only.
     */
    SQLRowReader(StreamTokenizer st, String table)
    {
        this.st = st;
        this.table = table;
    }

    /**
     * Reads one tuple up to and including its closing parenthesis.
     *
     * @return {@code true} if a tuple was read, {@code false} if the end of file was reached.
     * @throws IOException Thrown if IO errors occurred.
     */
    boolean readRow() throws IOException
    {
        columnsSeen = 0;
        int column = 0;
        while (true) {
            st.nextToken();
            if (st.ttype == StreamTokenizer.TT_EOF) {
                return false;
            }
            if (st.ttype == ')') {
                columnsSeen = column + 1;
                return true;
            }
            if (st.ttype == ',') {
                column++;
                continue;
            }
            capture(column);
        }
    }

    /**
     * @return The number of values the most recently read tuple contained.
     */
    int getColumnCount()
    {
        return columnsSeen;
    }

    /**
     * @param column   The column name - used in diagnostic messages only.
     * @param ordinal  The zero based position of the value within the tuple.
     * @return The value at {@code ordinal} as a {@code long}.
     * @throws IOException Thrown if the tuple is too short or the value is not numeric.
     */
    long requireLong(String column, int ordinal) throws IOException
    {
        checkPresent(column, ordinal);
        if (types[ordinal] != StreamTokenizer.TT_NUMBER) {
            throw new IOException(malformed(column, ordinal, "a numeric literal"));
        }
        return (long) numbers[ordinal];
    }

    /**
     * @param column   The column name - used in diagnostic messages only.
     * @param ordinal  The zero based position of the value within the tuple.
     * @return The value at {@code ordinal} as an {@code int}.
     * @throws IOException Thrown if the tuple is too short or the value is not numeric.
     */
    int requireInt(String column, int ordinal) throws IOException
    {
        return (int) requireLong(column, ordinal);
    }

    /**
     * @param column   The column name - used in diagnostic messages only.
     * @param ordinal  The zero based position of the value within the tuple.
     * @return The raw (tokenizer decoded, not yet SQL escaped) string value at {@code ordinal}.
     * @throws IOException Thrown if the tuple is too short or the value is not a string.
     */
    String requireString(String column, int ordinal) throws IOException
    {
        checkPresent(column, ordinal);
        final int type = types[ordinal];
        if (type != '\'' && type != '"' && type != StreamTokenizer.TT_WORD) {
            throw new IOException(malformed(column, ordinal, "a string literal"));
        }
        return strings[ordinal];
    }

    private void checkPresent(String column, int ordinal) throws IOException
    {
        if (ordinal < 0 || ordinal >= columnsSeen) {
            throw new IOException("Truncated row in table '" + table + "' at line " + st.lineno()
                    + ": the tuple has " + columnsSeen + " values but column '" + column
                    + "' is declared at index " + ordinal + ".");
        }
    }

    private String malformed(String column, int ordinal, String expectation)
    {
        return "Malformed value for column '" + column + "' (index " + ordinal + ") of table '"
                + table + "' at line " + st.lineno() + ": expected " + expectation
                + " but read " + describe(ordinal) + ".";
    }

    private String describe(int ordinal)
    {
        final int type = types[ordinal];
        if (type == StreamTokenizer.TT_NUMBER) {
            return "the number " + numbers[ordinal];
        }
        if (type == StreamTokenizer.TT_WORD) {
            return "the bare word '" + strings[ordinal] + "'";
        }
        if (type == '\'' || type == '"') {
            return "the quoted string '" + strings[ordinal] + "'";
        }
        return "the character '" + (char) type + "'";
    }

    private void capture(int column)
    {
        ensureCapacity(column + 1);
        types[column] = st.ttype;
        numbers[column] = st.nval;
        strings[column] = st.sval;
    }

    private void ensureCapacity(int required)
    {
        if (required <= types.length) {
            return;
        }
        final int capacity = Math.max(required, types.length * 2);
        types = Arrays.copyOf(types, capacity);
        numbers = Arrays.copyOf(numbers, capacity);
        strings = Arrays.copyOf(strings, capacity);
    }
}
