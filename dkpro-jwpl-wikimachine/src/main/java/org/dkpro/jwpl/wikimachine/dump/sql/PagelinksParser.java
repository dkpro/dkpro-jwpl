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
 * A Parser for the SQL file that defines the table {@code pagelinks}.
 * <p>
 * The layout of that table is probed from the {@code CREATE TABLE} header of the dump rather than
 * assumed, so the pre July 2014 layout {@code (pl_from, pl_namespace, pl_to)}, the post July 2014
 * layout {@code (pl_from, pl_namespace, pl_to, pl_from_namespace)} and the normalised layout of
 * MediaWiki 1.43+ {@code (pl_from, pl_from_namespace, pl_target_id)} are all supported. On the
 * normalised layout the target namespace and title are resolved through the {@code linktarget}
 * table, which the caller has to supply as a {@link LinkTargetResolver}.
 * <p>
 * A fix for Issue #102 has been provided by Google Code user {@code astronautguo}.
 *
 * @see SQLFileParser
 */
public class PagelinksParser
    extends SQLFileParser
{

    private static final Logger LOG = LoggerFactory.getLogger(PagelinksParser.class);

    private static final String TABLE = "pagelinks";

    private static final String PL_FROM = "pl_from";
    private static final String PL_NAMESPACE = "pl_namespace";
    private static final String PL_TO = "pl_to";
    private static final String PL_TARGET_ID = "pl_target_id";

    /** The supported layouts of the {@code pagelinks} table. */
    private enum Layout
    {
        /** {@code pl_namespace} and {@code pl_to} carry the target. */
        LEGACY,
        /** {@code pl_target_id} references {@code linktarget.lt_id}. */
        NORMALISED,
        /** No header was found; the legacy column positions are assumed. */
        POSITIONAL
    }

    /**
     * The fields of the table {@code pagelinks}.<br>
     * These fields are updated on each read value.
     */
    private int plFrom;
    private int plNamespace;
    private String plTo;
    private long plTargetId;

    private final LinkTargetResolver resolver;
    private final SQLRowReader rowReader;

    private Layout layout;
    private int idxFrom;
    private int idxNamespace;
    private int idxTo;
    private int idxTargetId;

    private long rowCount;
    private long resolvedCount;
    private long unresolvedCount;

    /**
     * Instantiates a {@link PagelinksParser} via an input stream. Suitable for dumps that use the
     * legacy layout only.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     *
     * @throws IOException Thrown if IO errors occurred or if the dump uses the normalised layout,
     *                     for which a {@link LinkTargetResolver} is mandatory.
     */
    public PagelinksParser(InputStream inputStream) throws IOException
    {
        this(inputStream, null);
    }

    /**
     * Instantiates a {@link PagelinksParser} via an input stream.
     *
     * @param inputStream A valid {@link InputStream} to read SQL content from.
     * @param resolver    The resolver for {@code pl_target_id} values, or {@code null} if none is
     *                    available. Mandatory for dumps using the normalised layout.
     *
     * @throws IOException Thrown if IO errors occurred or if the layout of the dump is not
     *                     supported.
     */
    public PagelinksParser(InputStream inputStream, LinkTargetResolver resolver) throws IOException
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
            idxNamespace = 1;
            idxTo = 2;
            idxTargetId = SQLRowReader.NO_COLUMN;
            warnNoHeader(TABLE, "(pl_from, pl_namespace, pl_to, [pl_from_namespace])");
        }
        else if (hasColumns(PL_NAMESPACE, PL_TO)) {
            layout = Layout.LEGACY;
            idxFrom = requireColumn(TABLE, PL_FROM);
            idxNamespace = indexOf(PL_NAMESPACE);
            idxTo = indexOf(PL_TO);
            idxTargetId = SQLRowReader.NO_COLUMN;
            LOG.info("Detected the legacy 'pagelinks' layout (pl_namespace/pl_to present).");
        }
        else if (indexOf(PL_TARGET_ID) >= 0) {
            layout = Layout.NORMALISED;
            idxFrom = requireColumn(TABLE, PL_FROM);
            idxTargetId = indexOf(PL_TARGET_ID);
            idxNamespace = SQLRowReader.NO_COLUMN;
            idxTo = SQLRowReader.NO_COLUMN;
            if (resolver == null) {
                throw new IOException("The 'pagelinks' dump uses the normalised MediaWiki layout "
                        + "(pl_target_id references linktarget.lt_id), but no linktarget dump was "
                        + "supplied. Add the '*-linktarget.sql.gz' dump of the same wiki and dump "
                        + "date to the input directory (DataMachine) or set the 'linkTargetFile' "
                        + "entry in the configuration (TimeMachine). Columns found in 'pagelinks': "
                        + describeColumns() + ".");
            }
            LOG.info("Detected the normalised 'pagelinks' layout (pl_target_id present); resolving "
                    + "targets through {} link targets.", resolver.size());
        }
        else {
            throw new IOException("Unsupported 'pagelinks' schema in the SQL dump: neither the "
                    + "legacy columns 'pl_namespace'/'pl_to' nor the normalised column "
                    + "'pl_target_id' are present. Columns found (in declaration order): "
                    + describeColumns() + ". Expected either the legacy layout [pl_from, "
                    + "pl_namespace, pl_to] / [pl_from, pl_namespace, pl_to, pl_from_namespace] or "
                    + "the normalised layout [pl_from, pl_from_namespace, pl_target_id] (MediaWiki "
                    + "1.43+). CREATE TABLE header read at line " + st.lineno() + ".");
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
                // the end of the file is reached
                EOF_reached = true;
                return false;
            }
            if (!rowReader.readRow()) {
                EOF_reached = true;
                return false;
            }
            rowCount++;

            plFrom = rowReader.requireInt(PL_FROM, idxFrom);

            final boolean usable;
            if (layout == Layout.NORMALISED) {
                plTargetId = rowReader.requireLong(PL_TARGET_ID, idxTargetId);
                final String title = resolver.getTitle(plTargetId);
                if (title == null) {
                    unresolvedCount++;
                    LOG.debug("Skipping pagelinks row: pl_target_id {} is not known to the "
                            + "linktarget dump.", plTargetId);
                    plTo = null;
                    plNamespace = LinkTargetResolver.NAMESPACE_UNKNOWN;
                    usable = false;
                }
                else {
                    plTo = title;
                    plNamespace = resolver.getNamespace(plTargetId);
                    resolvedCount++;
                    usable = true;
                }
            }
            else {
                plNamespace = rowReader.requireInt(PL_NAMESPACE, idxNamespace);
                plTo = SQLEscape.escape(rowReader.requireString(PL_TO, idxTo));
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

    /**
     * @return Returns the {@code pl_from}.
     */
    public int getPlFrom()
    {
        return plFrom;
    }

    /**
     * @return Returns the namespace of the link <em>target</em>. On the legacy layout this is the
     *         {@code pl_namespace} column; on the normalised layout it is resolved through the
     *         {@code linktarget} table. The source namespace {@code pl_from_namespace} is not
     *         exposed.
     */
    public int getPlNamespace()
    {
        return plNamespace;
    }

    /**
     * @return Returns the {@code pl_to}, that is, the SQL escaped title of the link target. On the
     *         normalised layout this value is resolved through the {@code linktarget} table.
     */
    public String getPlTo()
    {
        return plTo;
    }

    /**
     * @return Returns the {@code pl_target_id}, or {@code 0} if the dump does not carry that
     *         column.
     */
    public long getPlTargetId()
    {
        return plTargetId;
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
     * @return The number of tuples that were skipped because their {@code pl_target_id} could not
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
            throw new IOException("Parsed " + rowCount + " rows from the 'pagelinks' dump but not "
                    + "a single pl_target_id could be resolved through the linktarget dump ("
                    + resolver.size() + " link targets loaded). The linktarget dump does not match "
                    + "this pagelinks dump. Aborting instead of producing an empty page link graph "
                    + "(see issue #491).");
        }
        if (unresolvedCount > 0) {
            LOG.warn("{} of {} pagelinks rows referenced a link target that was not loaded and "
                    + "were skipped.", unresolvedCount, rowCount);
        }
    }
}
