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
package org.dkpro.jwpl.revisionmachine.index.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec.SQLEncoder;
import org.dkpro.jwpl.revisionmachine.index.indices.AbstractIndex;

/**
 * This class writes the output of the index generator to an SQL file.
 */
public class SQLFileWriter
    implements IndexWriterInterface
{

    /**
     * Reference to the Writer object
     */
    private final Writer writer;

    /**
     * Creates a new SQLFileWriter.
     *
     * @param config
     *            Reference to the configuration parameters
     * @throws IOException
     *             if an error occurred while writing the file
     */
    public SQLFileWriter(final RevisionAPIConfiguration config) throws IOException
    {

        writer = new BufferedWriter(
                new FileWriter(new File(config.getOutputPath(), "revisionIndex.sql")));

        writer.write("CREATE TABLE index_articleID_rc_ts ("
                + "ArticleID INTEGER UNSIGNED NOT NULL, " + "FullRevisionPKs MEDIUMTEXT NOT NULL, "
                + "RevisionCounter MEDIUMTEXT NOT NULL, " + "FirstAppearance BIGINT NOT NULL, "
                + "LastAppearance BIGINT NOT NULL, " + "PRIMARY KEY(ArticleID));");

        writer.write("CREATE TABLE index_revisionID (" + "RevisionID INTEGER UNSIGNED NOT NULL, "
                + "RevisionPK INTEGER UNSIGNED NOT NULL, "
                + "FullRevisionPK INTEGER UNSIGNED NOT NULL, " + "PRIMARY KEY(RevisionID));");

        writer.write("CREATE TABLE index_chronological (" + "ArticleID INTEGER UNSIGNED NOT NULL, "
                + "Mapping MEDIUMTEXT NOT NULL, " + "ReverseMapping MEDIUMTEXT NOT NULL, "
                + "PRIMARY KEY(ArticleID));");
        writer.write("\r\n");

        // disable keys now - re-enable at the end of the SQL file
        writer.write("ALTER TABLE index_articleID_rc_ts DISABLE KEYS;\r\n");
        writer.write("ALTER TABLE index_revisionID DISABLE KEYS;\r\n");
        writer.write("ALTER TABLE index_chronological DISABLE KEYS;\r\n");

        writer.flush();
    }

    /**
     * Writes the buffered finalized queries to the output.
     *
     * @param index
     *            Reference to an index
     * @throws IOException
     *             if an error occurred while writing the output
     */
    @Override
    public void write(final AbstractIndex index) throws IOException
    {

        StringBuilder cmd;
        while (index.size() > 0) {
            System.out.println("Transmit Index [" + index + "]");
            cmd = index.remove();
            cmd.append("\r\n");
            writer.write(cmd.toString());
        }

        writer.flush();
    }

    /**
     * Closes the file or the database connection.
     *
     * @throws IOException
     *             if an error occurred while closing the file
     */
    @Override
    public void close() throws IOException
    {
        this.writer.close();
    }

    /**
     * Wraps up the index generation process and writes all remaining statements e.g. concerning
     * UNCOMPRESSED-Indexes on the created tables.
     *
     * @throws IOException
     *             if an error occurred while writing to the file
     */
    @Override
    public void finish() throws IOException
    {

        // build the keys of the revisions table in case they are still disabled from the bulk load
        writer.write(SQLEncoder.ENABLE_KEYS + "\r\n");
        // tables created by older versions of the DiffTool do not declare the article index yet,
        // and the composite timestamp index is only created here, so it may already exist on reruns
        writeCreateIndexIfMissing(DatabaseWriter.ARTICLE_INDEX,
                DatabaseWriter.CREATE_ARTICLE_INDEX);
        writeCreateIndexIfMissing(DatabaseWriter.ARTICLE_TIMESTAMP_INDEX,
                DatabaseWriter.CREATE_ARTICLE_TIMESTAMP_INDEX);
        writer.write("ALTER TABLE index_articleID_rc_ts ENABLE KEYS;\r\n");
        writer.write("ALTER TABLE index_revisionID ENABLE KEYS;\r\n");
        writer.write("ALTER TABLE index_chronological ENABLE KEYS;\r\n");
        writer.flush();

    }

    /**
     * Writes statements which create an index on the revisions table unless it already exists.
     *
     * @param name
     *            name of the index
     * @param createStatement
     *            statement which creates the index
     * @throws IOException
     *             if an error occurred while writing to the file
     */
    private void writeCreateIndexIfMissing(final String name, final String createStatement)
        throws IOException
    {
        writer.write("SET @jwplHasIndex = (SELECT COUNT(*) FROM information_schema.statistics"
                + " WHERE table_schema = DATABASE() AND table_name = 'revisions'"
                + " AND index_name = '" + name + "');\r\n");
        writer.write("SET @jwplCreateIndex = IF(@jwplHasIndex = 0, '" + createStatement
                + "', 'DO 0');\r\n");
        writer.write("PREPARE jwplCreateIndex FROM @jwplCreateIndex;\r\n");
        writer.write("EXECUTE jwplCreateIndex;\r\n");
        writer.write("DEALLOCATE PREPARE jwplCreateIndex;\r\n");
    }
}
