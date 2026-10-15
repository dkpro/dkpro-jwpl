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
package org.dkpro.jwpl.util.templates.generator.simple;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates data to write into SQL dump file
 */
public class WikipediaTemplateInfoDumpWriter
{
    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, Integer> tplNameToTplId;
    private final String outputPath;
    private final String charset;

    /**
     * The template names this dump inserts into the tplid-tplname table. A name occurring in both
     * the page index and the revision index must only be inserted once, or the two indices end up
     * pointing at two ids of the same name (see issue #95).
     */
    private final Set<String> insertedTemplateNames = new HashSet<>();

    private final boolean tableExists;

    /**
     * The maximum size in bytes of a single statement of the dump, see the MySQL setting
     * {@code max_allowed_packet}.
     */
    private final long maxAllowedPacket;

    /**
     * Creates a dump writer that does not limit the size of a single statement.
     */
    public WikipediaTemplateInfoDumpWriter(String outputPath, String charset,
            Map<String, Integer> tplNameToTplId, boolean tableExists)
    {
        this(outputPath, charset, tplNameToTplId, tableExists, Long.MAX_VALUE);
    }

    /**
     * Creates a dump writer that splits the rows of a template over several statements whenever a
     * single statement would exceed {@code maxAllowedPacket} bytes.
     */
    public WikipediaTemplateInfoDumpWriter(String outputPath, String charset,
            Map<String, Integer> tplNameToTplId, boolean tableExists, long maxAllowedPacket)
    {
        this.tplNameToTplId = tplNameToTplId;
        this.outputPath = outputPath;
        this.charset = charset;
        this.tableExists = tableExists;
        this.maxAllowedPacket = maxAllowedPacket;
    }

    /**
     * Write SQL statements for data defined in {@code dataSourceToUse} and for table in
     * {@code tableToWrite}. The ids of a template are split over as many {@code REPLACE}
     * statements as needed to keep each of them within {@link #maxAllowedPacket} encoded bytes.
     *
     * @param writer
     *            writer to write the statements to
     * @param dataSourceToUse
     *            source to use for statement generation, mapping a template name to its sorted
     *            ids
     * @param tableToWrite
     *            table to use to store data
     */
    private void writeSQLStatementsForDataInTable(Writer writer,
            Map<String, int[]> dataSourceToUse, String tableToWrite)
        throws IOException
    {
        String prefix = "REPLACE INTO " + tableToWrite + " VALUES ";
        long prefixBytes = encodedLength(prefix);
        for (Entry<String, int[]> e : dataSourceToUse.entrySet()) {
            String curTemplateName = e.getKey();
            int[] curPageIds = e.getValue();

            if (!curTemplateName.isEmpty() && curPageIds.length > 0) {
                String id;
                if (tplNameToTplId.containsKey(curTemplateName)) {
                    // if template name has an id in the tplname-id map
                    id = tplNameToTplId.get(curTemplateName).toString();
                }
                else if (insertedTemplateNames.add(curTemplateName)) {
                    // if template name does not have an id in the tplname-id map
                    writer.write("INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                            + " (templateName) VALUES ('" + curTemplateName + "');");
                    writer.write("\r\n");
                    id = "LAST_INSERT_ID()";
                }
                else {
                    // the name was already inserted by an earlier statement of this dump - which
                    // happens whenever the page index and the revision index are created in one
                    // run - so its id is looked up instead of being created a second time
                    id = "(SELECT templateId FROM " + GeneratorConstants.TABLE_TPLID_TPLNAME
                            + " WHERE templateName = '" + curTemplateName + "' LIMIT 1)";
                }

                // a tuple is "(" + id + ", " + pId + ")"; the id is the only part that may hold
                // characters outside ASCII, so only its length has to be measured in bytes
                long idTupleBytes = encodedLength(id) + 4;
                long statementBytes = 0;
                for (int pId : curPageIds) {
                    String curPageId = Integer.toString(pId);
                    long tupleBytes = idTupleBytes + curPageId.length();
                    if (statementBytes == 0) {
                        writer.write(prefix);
                        statementBytes = prefixBytes + tupleBytes;
                    }
                    else if (statementBytes + 1 + tupleBytes + 1 > maxAllowedPacket) {
                        // the tuple (plus separator and terminating ';') would exceed the packet
                        writer.write(";\r\n");
                        writer.write(prefix);
                        statementBytes = prefixBytes + tupleBytes;
                    }
                    else {
                        writer.write(",");
                        statementBytes += 1 + tupleBytes;
                    }
                    writer.write("(" + id + ", ");
                    writer.write(curPageId);
                    writer.write(")");
                }
                writer.write(";\r\n");
            }
        }
    }

    /**
     * Writes the SQL statements for table template id -&gt; page id
     *
     * @param writer
     *            writer to write the statements to
     * @param tableExists
     *            if table does not exist create index for this table
     * @param dataSourceToUse
     *            data source to use for sql statement generation
     */
    private void writePageSQLStatement(Writer writer, boolean tableExists,
            Map<String, int[]> dataSourceToUse)
        throws IOException
    {
        // Statement creates table for Template Id -> Page Id
        writer.write("CREATE TABLE IF NOT EXISTS " + GeneratorConstants.TABLE_TPLID_PAGEID
                + " (templateId INTEGER UNSIGNED NOT NULL,"
                + "pageId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, pageId));\r\n");

        // Statement for data into templateId -> pageId
        writeSQLStatementsForDataInTable(writer, dataSourceToUse,
                GeneratorConstants.TABLE_TPLID_PAGEID);

        if (!tableExists) {
            // Create index statement if table does not exist
            writer.write("CREATE INDEX pageIdx ON " + GeneratorConstants.TABLE_TPLID_PAGEID
                    + "(pageId);");
            writer.write("\r\n");
        }
    }

    /**
     * Generate SQL statement for table template id -> template name
     *
     * @param tableExists
     *            if this table does not exist create index
     * @return sql statement
     */
    private String generateTemplateIdSQLStatement(boolean tableExists)
    {
        StringBuffer output = new StringBuffer();

        // Statement creates table for Template Id -> Template Name; the prefix index on the name
        // serves the lookups by name and is declared inline because the table may already exist
        output.append("CREATE TABLE IF NOT EXISTS " + GeneratorConstants.TABLE_TPLID_TPLNAME + " ("
                + "templateId INTEGER NOT NULL AUTO_INCREMENT,"
                + "templateName MEDIUMTEXT NOT NULL, " + "PRIMARY KEY(templateId), "
                + "INDEX tplNameIdx(templateName(191))); \r\n");

        if (!tableExists) {
            output.append("CREATE INDEX tplIdx ON " + GeneratorConstants.TABLE_TPLID_TPLNAME
                    + "(templateId);");
            output.append("\r\n");
        }
        return output.toString();

    }

    /**
     * Writes the SQL statements for table template id -&gt; revision id
     *
     * @param writer
     *            writer to write the statements to
     * @param tableExists
     *            if table does not exist create index for this table
     * @param dataSourceToUse
     *            data source to use for SQL statement generation
     */
    private void writeRevisionSQLStatement(Writer writer, boolean tableExists,
            Map<String, int[]> dataSourceToUse)
        throws IOException
    {
        // Statement creates table for Template Id -> Revision Id
        writer.write("CREATE TABLE IF NOT EXISTS " + GeneratorConstants.TABLE_TPLID_REVISIONID
                + " (templateId INTEGER UNSIGNED NOT NULL,"
                + "revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId));\r\n");

        // Statement for data into templateId -> revisionId
        writeSQLStatementsForDataInTable(writer, dataSourceToUse,
                GeneratorConstants.TABLE_TPLID_REVISIONID);

        if (!tableExists) {
            // Create index statement if table does not exist
            writer.write("CREATE INDEX revisionIdx ON " + GeneratorConstants.TABLE_TPLID_REVISIONID
                    + "(revisionID);");
            writer.write("\r\n");
        }
    }

    /**
     * @return the length of {@code s} in bytes when encoded with the charset of the dump
     */
    private long encodedLength(String s)
    {
        return s.getBytes(Charset.forName(charset)).length;
    }

    /**
     * Generate and write SQL statements to output file.
     *
     * @param revTableExists
     *            if revision table does not exist -&gt; create index
     * @param pageTableExists
     *            if page table does not exist -&gt; create index
     * @param mode
     *            generation mode
     */
    void writeSQL(boolean revTableExists, boolean pageTableExists, GeneratorMode mode)
    {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(outputPath)), charset))) {
            writer.write(generateTemplateIdSQLStatement(this.tableExists));

            if (mode.active_for_pages) {
                writePageSQLStatement(writer, pageTableExists, mode.templateNameToPageId);
            }
            if (mode.active_for_revisions) {
                writeRevisionSQLStatement(writer, revTableExists, mode.templateNameToRevId);
            }
        }
        catch (IOException e) {
            logger.error("Error writing SQL file: {}", e.getMessage(), e);
        }
    }

}
