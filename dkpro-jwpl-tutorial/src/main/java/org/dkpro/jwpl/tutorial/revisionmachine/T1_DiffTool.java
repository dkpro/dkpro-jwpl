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

import org.dkpro.jwpl.revisionmachine.difftool.DiffTool;
import org.dkpro.jwpl.revisionmachine.difftool.DiffToolThread;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationReader;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;

/**
 * RevisionMachine Tutorial 1
 * <p>
 * Runs the DiffTool, which reads a Wikipedia meta-history dump and writes the revisions of all
 * pages in the configured namespaces as a sequence of diffs. Every n-th revision (see
 * {@code COUNTER_FULL_REVISION}) is stored as a full revision.
 * <p>
 * The only argument is the path to a DiffTool configuration file, for instance a copy of
 * {@code src/main/resources/revisionmachine/difftool-config.xml} with adjusted paths. Such a file
 * can also be created with the Swing application
 * {@link org.dkpro.jwpl.revisionmachine.difftool.config.gui.ConfigGUI}.
 * <p>
 * This is what {@link DiffTool#main(String[])} does as well. The output - SQL or CSV files, or
 * the revisions table directly - has to be imported into a MySQL / MariaDB database before the
 * indexes can be generated (see {@link T2_IndexGenerator}).
 */
public class T1_DiffTool
{

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1) {
            System.err.println("Usage: T1_DiffTool <path to difftool-config.xml>");
            return;
        }

        // read the XML configuration
        ConfigSettings config = new ConfigurationReader(args[0]).read();

        // the DiffTool runs in its own thread; wait for it to finish
        DiffToolThread diffTool = new DiffToolThread(config);
        diffTool.start();
        diffTool.join();
    }
}
