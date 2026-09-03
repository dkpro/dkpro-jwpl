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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines common utilities for the classes {@link CategorylinksParser},
 * {@link PagelinksParser} and {@link LinktargetParser}.
 * <p>
 * Besides skipping the SQL preamble of a {@code mysqldump} file, this class also
 * <em>captures</em> the {@code CREATE TABLE} header it walks over. The captured, ordered
 * mapping of column names to tuple positions allows subclasses to read values
 * <em>by column name</em> instead of by a hardcoded token position. That is what makes a
 * single parser implementation work across the many schema revisions MediaWiki has shipped
 * over the years - most notably the normalisation of {@code categorylinks.cl_to} into
 * {@code categorylinks.cl_target_id} and of {@code pagelinks.pl_namespace}/{@code pl_to}
 * into {@code pagelinks.pl_target_id} (MediaWiki 1.43+).
 *
 * @version 0.3 <br>
 *          {@code SQLFileParser} captures the {@code CREATE TABLE} header so that subclasses
 *          can probe the layout of the dump at hand instead of assuming a fixed column order.
 *          (see issue #491)
 * @version 0.2 <br>
 *          <code>SQLFileParser</code> don't create a BufferedReader by himself but entrust it to
 *          <code>BufferedReaderFactory</code>. Thereby, BufferedReaders are created according to
 *          archive type and try to uncompress the file on the fly. (Ivan Galkin 15.01.2009)
 */
abstract class SQLFileParser implements AutoCloseable
{

    private static final Logger LOG = LoggerFactory.getLogger(SQLFileParser.class);

    /**
     * Keywords that may open a <em>table constraint</em> instead of a column definition inside a
     * {@code CREATE TABLE} body. A definition starting with one of these does not contribute a
     * column.
     */
    private static final Set<String> TABLE_CONSTRAINT_KEYWORDS = Set.of("primary", "unique", "key",
            "index", "constraint", "fulltext", "spatial", "check", "foreign", "period");

    /** The stream associated with the SQL content to parse. */
    protected InputStream stream;
    /** The tokenizer instance used to parse the underlying {@link #stream}.*/
    protected StreamTokenizer st;
    /** Whether the end of file has been reached. */
    protected boolean EOF_reached;

    /** Ordered mapping of the (lower-cased) column name to its zero based position in a tuple. */
    private final Map<String, Integer> columnIndex = new LinkedHashMap<>();
    /** Ordered mapping of the (lower-cased) column name to its declared SQL type. */
    private final Map<String, String> columnTypes = new LinkedHashMap<>();
    /** The name of the table of the most recently seen {@code CREATE TABLE} statement. */
    private String tableName;
    /** Whether a complete {@code CREATE TABLE} header has been captured. */
    private boolean headerParsed;

    /* Transient state of the header capturing state machine. */
    private boolean sawCreate;
    private boolean inHeader;
    private int depth;
    private boolean expectName;
    private String lastColumn;

    /**
     * Init a {@link SQLFileParser} via an input stream.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     *
     * @throws IOException Thrown if IO errors occurred during initialization.
     */
    protected void init(InputStream inputStream) throws IOException
    {
        stream = inputStream;
        st = new StreamTokenizer(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        // Under the default syntax table '_' is an ordinary character, which would split
        // 'cl_from' into three tokens and make capturing column names impossible. An unquoted
        // '_' never occurs inside a VALUES tuple, so widening the word characters is safe.
        st.wordChars('_', '_');

        EOF_reached = false;
        skipStatements();

    }

    /**
     * Skip the SQL statements for table creation and the prefix <br>
     * INSERT INTO TABLE .... VALUES for values insertion.<br>
     * Read tokens until the word 'VALUES' is reached or the EOF.
     * <p>
     * While skipping, the column list of the most recent {@code CREATE TABLE} statement is
     * captured and made available via {@link #getColumnIndex()}.
     *
     * @throws IOException
     *             Thrown if IO errors occurred.
     */
    protected void skipStatements() throws IOException
    {
        while (true) {
            st.nextToken();
            if (null != st.sval && st.sval.equalsIgnoreCase("VALUES")) {
                // the next token is the start of a value
                break;
            }
            if (st.ttype == StreamTokenizer.TT_EOF) {
                // the end of the file is reached
                EOF_reached = true;
                break;
            }
            captureHeaderToken();
        }
    }

    /**
     * Feeds the token the tokenizer has just read into the {@code CREATE TABLE} capturing state
     * machine. The captured column map survives repeated {@link #skipStatements()} calls (a dump
     * may contain several {@code INSERT} statements for one table) and is only reset when another
     * {@code CREATE TABLE} statement begins.
     */
    private void captureHeaderToken()
    {
        if (!inHeader) {
            if (st.ttype == StreamTokenizer.TT_WORD && "CREATE".equalsIgnoreCase(st.sval)) {
                sawCreate = true;
                return;
            }
            if (sawCreate && st.ttype == StreamTokenizer.TT_WORD && "TABLE".equalsIgnoreCase(st.sval)) {
                inHeader = true;
                depth = 0;
                expectName = false;
                lastColumn = null;
                tableName = null;
                headerParsed = false;
                columnIndex.clear();
                columnTypes.clear();
                sawCreate = false;
                return;
            }
            sawCreate = false;
            return;
        }
        if (depth == 0) {
            if (st.ttype == '(') {
                depth = 1;
                expectName = true;
                return;
            }
            if (st.ttype == ';') {
                // e.g. CREATE TABLE a LIKE b;
                inHeader = false;
                return;
            }
            if (st.ttype == StreamTokenizer.TT_WORD && tableName == null
                    && !"IF".equalsIgnoreCase(st.sval) && !"NOT".equalsIgnoreCase(st.sval)
                    && !"EXISTS".equalsIgnoreCase(st.sval)) {
                tableName = st.sval;
            }
            return;
        }
        if (st.ttype == '(') {
            depth++;
            return;
        }
        if (st.ttype == ')') {
            depth--;
            if (depth == 0) {
                inHeader = false;
                headerParsed = true;
            }
            return;
        }
        if (depth != 1) {
            // int(8), varbinary(230), enum('page','subcat','file'), KEY x (a,b), ...
            return;
        }
        if (st.ttype == ',') {
            expectName = true;
            lastColumn = null;
            return;
        }
        if (st.ttype != StreamTokenizer.TT_WORD) {
            // backticks are ordinary characters and are simply ignored
            return;
        }
        if (expectName) {
            expectName = false;
            if (TABLE_CONSTRAINT_KEYWORDS.contains(st.sval.toLowerCase(Locale.ROOT))) {
                return;
            }
            final String name = st.sval.toLowerCase(Locale.ROOT);
            columnIndex.putIfAbsent(name, columnIndex.size());
            lastColumn = name;
            return;
        }
        if (lastColumn != null && !columnTypes.containsKey(lastColumn)) {
            columnTypes.put(lastColumn, st.sval.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * @return {@code true} if a complete {@code CREATE TABLE} header has been captured,
     *         {@code false} for a dump that starts right at an {@code INSERT} statement.
     */
    protected boolean hasHeader()
    {
        return headerParsed;
    }

    /**
     * @return The table name of the captured {@code CREATE TABLE} statement, or {@code null}.
     */
    protected String getTableName()
    {
        return tableName;
    }

    /**
     * @return An unmodifiable, insertion ordered mapping of column name to its zero based
     *         position within a tuple. Empty if no header was captured.
     */
    protected Map<String, Integer> getColumnIndex()
    {
        return Collections.unmodifiableMap(columnIndex);
    }

    /**
     * @return An unmodifiable, insertion ordered mapping of column name to its declared SQL type.
     */
    protected Map<String, String> getColumnTypes()
    {
        return Collections.unmodifiableMap(columnTypes);
    }

    /**
     * @param column The column name to look up, case insensitive.
     * @return The zero based position of {@code column}, or {@code -1} if it is not present.
     */
    protected int indexOf(String column)
    {
        if (column == null) {
            return -1;
        }
        return columnIndex.getOrDefault(column.toLowerCase(Locale.ROOT), -1);
    }

    /**
     * @param names The column names to look for, case insensitive.
     * @return {@code true} if every name in {@code names} is present in the captured header.
     */
    protected boolean hasColumns(String... names)
    {
        for (String name : names) {
            if (indexOf(name) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return A human readable, ordered rendering of the captured columns and their declared
     *         types, for use in diagnostic messages.
     */
    protected String describeColumns()
    {
        final StringBuilder sb = new StringBuilder("[");
        for (Map.Entry<String, Integer> entry : columnIndex.entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            final String type = columnTypes.get(entry.getKey());
            if (type != null) {
                sb.append(' ').append(type);
            }
        }
        return sb.append(']').toString();
    }

    /**
     * @param table  The table the column is expected in - used for the error message only.
     * @param column The required column name.
     * @return The zero based position of {@code column}.
     * @throws IOException Thrown if {@code column} is not present in the captured header.
     */
    protected int requireColumn(String table, String column) throws IOException
    {
        final int index = indexOf(column);
        if (index < 0) {
            throw new IOException("Required column '" + column + "' is missing from the '" + table
                    + "' CREATE TABLE header (line " + st.lineno() + "). Columns found: "
                    + describeColumns() + ".");
        }
        return index;
    }

    /**
     * Logs that no {@code CREATE TABLE} header could be found and that the parser therefore falls
     * back to the legacy positional column order.
     *
     * @param table        The name of the table being parsed.
     * @param assumedOrder The positional column order assumed by the fallback.
     */
    protected static void warnNoHeader(String table, String assumedOrder)
    {
        LOG.warn("No CREATE TABLE header found in the '{}' dump; falling back to the legacy "
                + "positional column order {}. If this dump uses the normalised MediaWiki layout "
                + "the extracted links will be incomplete or empty.", table, assumedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        stream.close();
    }

    /**
     * Must be implemented by the {@link PagelinksParser}, the {@link CategorylinksParser} and the
     * {@link LinktargetParser} classes.
     *
     * @return {@code true} if a new value is now available, {@code false} otherwise.
     * @throws IOException
     *             Thrown if IO errors occurred.
     */
    abstract boolean next() throws IOException;
}
