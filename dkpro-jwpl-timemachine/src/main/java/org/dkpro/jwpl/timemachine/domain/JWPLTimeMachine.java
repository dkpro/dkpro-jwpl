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
package org.dkpro.jwpl.timemachine.domain;

import java.util.Optional;
import java.util.ServiceLoader;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Configuration;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;

/**
 * The command line tool of the DBMapping Tool of the JWPL.<br>
 * The {@link #main(String[])} method gets the path of a configuration file as argument.
 */
public class JWPLTimeMachine
{

    private static final String PROP_TOTAL_ENTITY_SIZE_LIMIT = "jdk.xml.totalEntitySizeLimit";

    private static final String PROP_MAX_GENERAL_ENTITY_SIZE_LIMIT = "jdk.xml.maxGeneralEntitySizeLimit";

    /**
     * Indicates that the corresponding JAXP limit is not enforced.
     */
    private static final String NO_LIMIT = "0";

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_USAGE = 255;

    private static final IEnvironmentFactory environmentFactory;
    private static final ILogger logger;

    static {
        Optional<IEnvironmentFactory> candidate = ServiceLoader.load(IEnvironmentFactory.class).findFirst();
        environmentFactory = candidate.orElseThrow(
                () -> new RuntimeException("Error detecting required runtime environment components! " +
                        "Check your classpath and/or configuration."));
        logger = environmentFactory.getLogger();
        logger.log("Initializing environment with JWPL's internal bean factory: "
                + environmentFactory.getClass().getName());
    }

    private static final long startTime = System.currentTimeMillis();

    /**
     * Checks given arguments
     *
     * @param args
     *            <br>
     *            args[0] the settings file like described in {@link SettingsXML}<br>
     * @return {@code true} if all necessary arguments are given and {@code false} otherwise.
     * @see SettingsXML
     */
    private static boolean checkArgs(String[] args)
    {
        return args.length > 0;
    }

    /**
     * The entry point of the TimeMachine tool.
     * <p>
     * Terminates the JVM with a non-zero exit status if the arguments are incomplete or the
     * processing fails, see {@link #run(String[])}.
     *
     * @param args args[0] the settings file like described in {@link SettingsXML}
     */
    public static void main(String[] args)
    {
        int status = run(args);
        if (status != EXIT_SUCCESS) {
            System.exit(status);
        }
    }

    /**
     * Runs the TimeMachine tool without terminating the JVM.
     *
     * @param args The arguments as described for {@link #main(String[])}.
     * @return {@code 0} on success, {@code 255} if the arguments are incomplete, and {@code 1} if
     *         the configuration is invalid or the processing failed.
     */
    static int run(String[] args)
    {
        try {
            if (checkArgs(args)) {
                // The dumps processed here are trusted input whose entity sizes legitimately
                // exceed the JAXP defaults. See #201 and #504.
                System.setProperty(PROP_TOTAL_ENTITY_SIZE_LIMIT, NO_LIMIT);
                System.setProperty(PROP_MAX_GENERAL_ENTITY_SIZE_LIMIT, NO_LIMIT);
                logger.log("parsing configuration file....");
                Configuration config = SettingsXML.loadConfiguration(args[0], logger);
                TimeMachineFiles files = SettingsXML.loadFiles(args[0], logger);

                if (config != null && files != null) {
                    if (files.checkAll() && config.checkTimeConfig()) {
                        logger.log("processing data ...");

                        ISnapshotGenerator generator = environmentFactory.getSnapshotGenerator();
                        generator.setConfiguration(config);
                        generator.setFiles(files);
                        generator.start();

                        logger.log("End of the application. Working time = "
                                + (System.currentTimeMillis() - startTime) + " ms");
                        return EXIT_SUCCESS;
                    }
                }
                return EXIT_FAILURE;
            } else {
                logger.log("Usage: java -jar JWPLTimeMachine.jar <CONFIG_FILE>");
                return EXIT_USAGE;
            }
        }
        catch (Exception e) {
            logger.log(e);
            return EXIT_FAILURE;
        }
    }
}
