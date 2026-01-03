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
package org.dkpro.jwpl.revisionmachine.difftool;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationReader;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;

/**
 * The command line tool to the start the DiffTool application.
 */
public class DiffTool
{

    private static final String USAGE = "Please use\n"
            + "\tjava -jar JWPLRevisionMachine.jar "
            + "org.dkpro.jwpl.revisionmachine.difftool.DiffTool <CONFIG_FILE>\n\n"
            + "Hint: Set up a difftool-config.xml via the GUI tool (see documentation for more help).\n";

    /**
     * No object - Utility class
     */
    private DiffTool()
    {
    }

    /**
     * Starts the DiffTool application.
     *
     * @param args
     *            program arguments args[0] has to be the path to the configuration file
     */
    public static void main(final String[] args)
    {

        if (args.length == 1) {
            try {
                ConfigSettings config = new ConfigurationReader(args[0]).read();
                new DiffToolThread(config).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Arg for configuration file is missing
            System.out.println(USAGE);
            System.exit(255);
        }
    }
}
