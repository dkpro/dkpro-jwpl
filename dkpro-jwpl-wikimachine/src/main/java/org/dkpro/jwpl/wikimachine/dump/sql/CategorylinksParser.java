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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Parser for the SQL file that defines the table {@code categorylinks}.
 * <p>
 * The layout of that table is probed from the {@code CREATE TABLE} header of the dump rather than
 * assumed, so both the legacy layout
 * {@code (cl_from, cl_to, cl_sortkey, cl_timestamp, ...)} and the normalised layout of
 * MediaWiki 1.43+
 * {@code (cl_from, cl_sortkey, cl_timestamp, cl_sortkey_prefix, cl_type, cl_collation_id, cl_target_id)}
 * are supported. On the normalised layout the target title is resolved through the
 * {@code linktarget} table, which the caller has to supply as a {@link LinkTargetResolver}.
 * <p>
 * A fix for Issue #102 has been provided by Google Code user {@code astronautguo}.
 *
 * @see SQLFileParser
 */
public class CategorylinksParser
    extends SQLFileParser
{

    private static final Logger LOG = LoggerFactory.getLogger(CategorylinksParser.class);

    private static final String TABLE = "categorylinks";

    private static final String CL_FROM = "cl_from";
    private static final String CL_TO = "cl_to";
    private static final String CL_TYPE = "cl_type";
    private static final String CL_TARGET_ID = "cl_target_id";

    /** The supported layouts of the {@code categorylinks} table. */
    private enum Layout
    {
        /** {@code cl_to} carries the target title. */
        LEGACY,
        /** {@code cl_target_id} references {@code linktarget.lt_id}. */
        NORMALISED,
        /** No header was found; the legacy column positions are assumed. */
        POSITIONAL
    }

    /**
     * The fields of the table {@code categorylinks}.<br>
     * These fields are updated on each read value.
     */
    private int clFrom;
    private String clTo;
    private long clTargetId;
    private CategoryLinkType clType = CategoryLinkType.UNKNOWN;

    private final LinkTargetResolver resolver;
    private final SQLRowReader rowReader;

    private Layout layout;
    private int idxFrom;
    private int idxTo;
    private int idxType;
    private int idxTargetId;

    private long rowCount;
    private long resolvedCount;
    private long unresolvedCount;

    /**
     * Instantiates a {@link CategorylinksParser} via an input stream. Suitable for dumps that use
     * the legacy layout only.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     * @throws IOException Thrown if IO errors occurred or if the dump uses the normalised layout,
     *                     for which a {@link LinkTargetResolver} is mandatory.
     */
    public CategorylinksParser(InputStream inputStream) throws IOException
    {
        this(inputStream, null);
    }

    /**
     * Instantiates a {@link CategorylinksParser} via an input stream.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     * @param resolver    The resolver for {@code cl_target_id} values, or {@code null} if none is
     *                    available. Mandatory for dumps using the normalised layout.
     * @throws IOException Thrown if IO errors occurred or if the layout of the dump is not
     *                     supported.
     */
    public CategorylinksParser(InputStream inputStream, LinkTargetResolver resolver)
        throws IOException
    {
        this.resolver = resolver;
        init(inputStream);
        configureLayout();
        rowReader = new SQLRowReader(st, TABLE);
    }

    private void configureLayout() throws IOException
    {
        if (!hasHeader()) {
            layout = Layout.POSITIONAL;
            idxFrom = 0;
            idxTo = 1;
            idxType = SQLRowReader.NO_COLUMN;
            idxTargetId = SQLRowReader.NO_COLUMN;
            warnNoHeader(TABLE, "(cl_from, cl_to, cl_sortkey, cl_timestamp, ...)");
        }
        else if (indexOf(CL_TO) >= 0) {
            layout = Layout.LEGACY;
            idxFrom = requireColumn(TABLE, CL_FROM);
            idxTo = indexOf(CL_TO);
            idxType = indexOf(CL_TYPE);
            idxTargetId = SQLRowReader.NO_COLUMN;
            LOG.info("Detected the legacy 'categorylinks' layout (cl_to present).");
        }
        else if (indexOf(CL_TARGET_ID) >= 0) {
            layout = Layout.NORMALISED;
            idxFrom = requireColumn(TABLE, CL_FROM);
            idxTargetId = indexOf(CL_TARGET_ID);
            idxType = indexOf(CL_TYPE);
            idxTo = SQLRowReader.NO_COLUMN;
            if (resolver == null) {
                throw new IOException("The 'categorylinks' dump uses the normalised MediaWiki "
                        + "layout (cl_target_id references linktarget.lt_id), but no linktarget "
                        + "dump was supplied. Add the '*-linktarget.sql.gz' dump of the same wiki "
                        + "and dump date to the input directory (DataMachine) or set the "
                        + "'linkTargetFile' entry in the configuration (TimeMachine). Columns "
                        + "found in 'categorylinks': " + describeColumns() + ".");
            }
            LOG.info("Detected the normalised 'categorylinks' layout (cl_target_id present); "
                    + "resolving targets through {} link targets.", resolver.size());
        }
        else {
            throw new IOException("Unsupported 'categorylinks' schema in the SQL dump: neither the "
                    + "legacy column 'cl_to' nor the normalised column 'cl_target_id' is present. "
                    + "Columns found (in declaration order): " + describeColumns() + ". Expected "
                    + "either the legacy layout [cl_from, cl_to, cl_sortkey, cl_timestamp, ...] or "
                    + "the normalised layout [cl_from, cl_sortkey, cl_timestamp, "
                    + "cl_sortkey_prefix, cl_type, cl_collation_id, cl_target_id] (MediaWiki "
                    + "1.43+). CREATE TABLE header read at line " + st.lineno() + ".");
        }
    }

    /**
     * @return Returns the {@code cl_from}.
     */
    public int getClFrom()
    {
        return clFrom;
    }

    /**
     * @return Returns the {@code cl_to}, that is, the SQL escaped title of the target category.
     *         On the normalised layout this value is resolved through the {@code linktarget}
     *         table.
     */
    public String getClTo()
    {
        return clTo;
    }

    /**
     * @return Returns the {@code cl_target_id}, or {@code 0} if the dump does not carry that
     *         column.
     */
    public long getClTargetId()
    {
        return clTargetId;
    }

    /**
     * @return Returns the {@code cl_type}, or {@link CategoryLinkType#UNKNOWN} if the dump does
     *         not carry that column.
     */
    public CategoryLinkType getClType()
    {
        return clType;
    }

    /**
     * @return The number of tuples read so far.
     */
    public long getRowCount()
    {
        return rowCount;
    }

    /**
     * @return The number of tuples whose target title could be determined.
     */
    public long getResolvedCount()
    {
        return resolvedCount;
    }

    /**
     * @return The number of tuples that were skipped because their {@code cl_target_id} could not
     *         be resolved.
     */
    public long getUnresolvedCount()
    {
        return unresolvedCount;
    }

    /**
     * Verifies that the parse as a whole produced a plausible result. To be called once the last
     * row has been read.
     *
     * @throws IOException Thrown if rows were read from a normalised dump but not a single link
     *                     target could be resolved.
     */
    public void checkPostConditions() throws IOException
    {
        if (layout == Layout.NORMALISED && rowCount > 0 && resolvedCount == 0) {
            throw new IOException("Parsed " + rowCount + " rows from the 'categorylinks' dump but "
                    + "not a single cl_target_id could be resolved through the linktarget dump ("
                    + resolver.size() + " link targets loaded). The linktarget dump does not match "
                    + "this categorylinks dump. Aborting instead of producing an empty category "
                    + "graph (see issue #491).");
        }
        if (unresolvedCount > 0) {
            LOG.warn("{} of {} categorylinks rows referenced a link target that was not loaded and "
                    + "were skipped.", unresolvedCount, rowCount);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean next() throws IOException
    {
        while (true) {
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
            rowCount++;

            clFrom = rowReader.requireInt(CL_FROM, idxFrom);
            clType = idxType == SQLRowReader.NO_COLUMN ? CategoryLinkType.UNKNOWN
                    : CategoryLinkType.fromDumpValue(rowReader.requireString(CL_TYPE, idxType));

            final boolean usable;
            if (layout == Layout.NORMALISED) {
                clTargetId = rowReader.requireLong(CL_TARGET_ID, idxTargetId);
                final String title = resolver.getTitle(clTargetId);
                if (title == null) {
                    unresolvedCount++;
                    LOG.debug("Skipping categorylinks row: cl_target_id {} is not known to the "
                            + "linktarget dump.", clTargetId);
                    clTo = null;
                    usable = false;
                }
                else {
                    clTo = title;
                    resolvedCount++;
                    usable = true;
                }
            }
            else {
                clTo = SQLEscape.escape(rowReader.requireString(CL_TO, idxTo));
                resolvedCount++;
                usable = true;
            }

            consumeRowSeparator();

            if (usable) {
                return true;
            }
        }
    }

    private void consumeRowSeparator() throws IOException
    {
        // read ',' or ';'. If ';' is found then skip statement or expect eof.
        st.nextToken();
        if (st.ttype == StreamTokenizer.TT_EOF) {
            EOF_reached = true;
        }
        else if (st.ttype == ';') {
            skipStatements();
        }
    }
}
