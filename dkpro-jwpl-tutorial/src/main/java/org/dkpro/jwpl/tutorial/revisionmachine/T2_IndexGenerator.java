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
package org.dkpro.jwpl.tutorial.revisionmachine;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.difftool.config.OutputTypes;
import org.dkpro.jwpl.revisionmachine.index.IndexGenerator;

/**
 * RevisionMachine Tutorial 2
 * <p>
 * Generates the index tables {@code index_articleID_rc_ts}, {@code index_revisionID} and
 * {@code index_chronological} for a database that contains the imported output of the DiffTool
 * (see {@link T1_DiffTool}). The Revision API needs these tables.
 * <p>
 * With {@link OutputTypes#DATABASE}, the index tables are written directly into the database.
 * Afterwards, the IndexGenerator enables the keys of the {@code revisions} table and creates the
 * indexes {@code articleIdx} and {@code articleTsIdx} on it if they are missing (revisions tables
 * created by the current DiffTool already declare both).
 * <p>
 * The same can be done on the command line with
 * {@link IndexGenerator#main(String[])} and a properties file such as
 * {@code src/main/resources/revisionmachine/indexgenerator.properties}.
 */
public class T2_IndexGenerator
{

    public static void main(String[] args) throws WikiApiException
    {
        // configure the connection to the database that contains the revisions table
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("SERVER_URL");
        config.setDatabase("DATABASE");
        config.setUser("USER");
        config.setPassword("PASSWORD");

        // number of revisions read from the database per query
        config.setBufferSize(15000);
        // should not exceed the max_allowed_packet setting of the database server
        config.setMaxAllowedPacket(16 * 1024 * 1023);

        // write the index tables directly into the database; OutputTypes.SQL and
        // OutputTypes.DATAFILE write SQL or CSV files into config.getOutputPath() instead
        config.setOutputType(OutputTypes.DATABASE);

        new IndexGenerator(config).generate();
    }
}
