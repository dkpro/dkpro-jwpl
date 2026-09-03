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
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * A Parser for the SQL file that defines the table {@code linktarget}.
 * <p>
 * MediaWiki 1.43 introduced this table to normalise the link tables: {@code categorylinks} and
 * {@code pagelinks} no longer carry the title of the link target but a {@code lt_id} foreign key
 * into {@code linktarget}. Its columns are {@code lt_id}, {@code lt_namespace} and
 * {@code lt_title}.
 *
 * @see SQLFileParser
 * @see LinkTargetResolver
 */
public class LinktargetParser
    extends SQLFileParser
{

    private static final String TABLE = "linktarget";

    private static final String LT_ID = "lt_id";
    private static final String LT_NAMESPACE = "lt_namespace";
    private static final String LT_TITLE = "lt_title";

    /**
     * The fields of the table {@code linktarget}.<br>
     * These fields are updated on each read value.
     */
    private long ltId;
    private int ltNamespace;
    private String ltTitle;

    private int idxId;
    private int idxNamespace;
    private int idxTitle;

    private final SQLRowReader rowReader;

    /**
     * Instantiates a {@link LinktargetParser} via an input stream.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     * @throws IOException Thrown if IO errors occurred or if the dump uses an unsupported schema.
     */
    public LinktargetParser(InputStream inputStream) throws IOException
    {
        init(inputStream);
        configureLayout();
        rowReader = new SQLRowReader(st, TABLE);
    }

    private void configureLayout() throws IOException
    {
        if (!hasHeader()) {
            warnNoHeader(TABLE, "(lt_id, lt_namespace, lt_title)");
            idxId = 0;
            idxNamespace = 1;
            idxTitle = 2;
        }
        else if (hasColumns(LT_ID, LT_NAMESPACE, LT_TITLE)) {
            idxId = indexOf(LT_ID);
            idxNamespace = indexOf(LT_NAMESPACE);
            idxTitle = indexOf(LT_TITLE);
        }
        else {
            throw new IOException("Unsupported 'linktarget' schema in the SQL dump: expected the "
                    + "columns [lt_id, lt_namespace, lt_title]. Columns found (in declaration "
                    + "order): " + describeColumns() + ". CREATE TABLE header read at line "
                    + st.lineno() + ".");
        }
    }

    /**
     * @return Returns the {@code lt_id}.
     */
    public long getLtId()
    {
        return ltId;
    }

    /**
     * @return Returns the {@code lt_namespace}.
     */
    public int getLtNamespace()
    {
        return ltNamespace;
    }

    /**
     * @return Returns the {@code lt_title}, SQL escaped. The value is the MediaWiki database key
     *         of the target, that is, it already uses underscores instead of blanks and carries no
     *         namespace prefix.
     */
    public String getLtTitle()
    {
        return ltTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean next() throws IOException
    {
        if (EOF_reached) {
            return false;
        }
        // read '('
        st.nextToken();
        if (st.ttype == StreamTokenizer.TT_EOF) {
            EOF_reached = true;
            return false;
        }
        if (!rowReader.readRow()) {
            EOF_reached = true;
            return false;
        }
        ltId = rowReader.requireLong(LT_ID, idxId);
        ltNamespace = rowReader.requireInt(LT_NAMESPACE, idxNamespace);
        ltTitle = SQLEscape.escape(rowReader.requireString(LT_TITLE, idxTitle));

        // read ',' or ';'. If ';' is found then skip statement or expect eof.
        st.nextToken();
        if (st.ttype == StreamTokenizer.TT_EOF) {
            EOF_reached = true;
        }
        else if (st.ttype == ';') {
            skipStatements();
        }
        return true;
    }
}
