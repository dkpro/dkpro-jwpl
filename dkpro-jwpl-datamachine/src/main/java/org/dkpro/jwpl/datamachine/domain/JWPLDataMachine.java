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
package org.dkpro.jwpl.datamachine.domain;

import static org.dkpro.jwpl.wikimachine.util.ExitStatus.EXIT_FAILURE;
import static org.dkpro.jwpl.wikimachine.util.ExitStatus.EXIT_SUCCESS;
import static org.dkpro.jwpl.wikimachine.util.ExitStatus.EXIT_USAGE;

import java.util.Optional;
import java.util.ServiceLoader;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Configuration;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.util.ExitStatus;

/**
 * The command line tool that starts the transformation
 * of a Mediawiki dump archive into JWPL format.
 */
public class JWPLDataMachine
{

    private static final String PROP_TOTAL_ENTITY_SIZE_LIMIT = "jdk.xml.totalEntitySizeLimit";

    private static final String PROP_MAX_GENERAL_ENTITY_SIZE_LIMIT = "jdk.xml.maxGeneralEntitySizeLimit";

    /**
     * Indicates that the corresponding JAXP limit is not enforced.
     */
    private static final String NO_LIMIT = "0";

    private static final int LANG_ARG = 0;
    private static final int MAINCATEGORY_ARG = 1;
    private static final int DISAMBIGUATION_ARG = 2;
    private static final int DATADIR_ARG = 3;

    private static final String USAGE = "Please use\n"
            + "\tjava -jar JWPLDataMachine.jar <LANGUAGE> <TOP_CATEGORY_NAME> <DISAMBIGUATION_CATEGORY_NAME> <SOURCE_DIRECTORY>\n\n"
            + "The source directory must contain files\n" + "\tpagelinks.sql\n"
            + "\tpages-articles.xml\n" + "\tcategorylinks.sql\n"
            + "\tlinktarget.sql (required for dumps produced by MediaWiki 1.43 and later, where\n"
            + "\t                categorylinks/pagelinks reference their targets by id; ignored\n"
            + "\t                for older dumps)\n"
            + "GZip, BZip2, and 7z compressed archives of above-named files are also allowed.\n"
            + "Note that the linktarget table is loaded into memory; for large wikis increase the\n"
            + "heap of the JVM accordingly (e.g. -Xmx8g).\n"
            + "Only direct members of <DISAMBIGUATION_CATEGORY_NAME> are flagged as disambiguation\n"
            + "pages; members of its subcategories are not. For the English Wikipedia use\n"
            + "All_article_disambiguation_pages, which every disambiguation template (e.g. {{hndis}}) populates.\n"
            + "External archive utilities (e.g. lbzip2 or pigz) can be configured in a decompressor.xml,\n"
            + "which is enabled via -Djwpl.decompressor.xml=<PATH> (see documentation for more help).\n"
            + "With -Djwpl.decompressor.readahead=true the built-in decompression runs on a separate\n"
            + "thread, overlapping with the parsing of the dump.\n";

    private static final long startTime = System.currentTimeMillis();

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


    /**
     * The entry point of the DataMachine tool.
     * <p>
     * Terminates the JVM with exit status 255 if the arguments are incomplete, and with 1 if the
     * source files are missing or the transformation fails.
     *
     * @param args Expects four arguments. Must be in the ordering as specified as follows:
     *        {@code <LANGUAGE> <TOP_CATEGORY_NAME> <DISAMBIGUATION_CATEGORY_NAME> <SOURCE_DIRECTORY>}.
     */
    public static void main(String[] args)
    {
        ExitStatus.exitOnFailure(run(args));
    }

    /**
     * Runs the DataMachine tool.
     *
     * @param args The arguments as described for {@link #main(String[])}.
     * @return {@code 0} on success, {@code 255} if the arguments are incomplete, and {@code 1} if
     *         the source files are missing or the transformation failed.
     */
    private static int run(String[] args)
    {
        if (args.length > 3) {
            // The dumps processed here are trusted input whose entity sizes legitimately
            // exceed the JAXP defaults. See #201 and #504.
            System.setProperty(PROP_TOTAL_ENTITY_SIZE_LIMIT, NO_LIMIT);
            System.setProperty(PROP_MAX_GENERAL_ENTITY_SIZE_LIMIT, NO_LIMIT);
            Configuration config = getConfigFromArgs(args);
            DataMachineFiles files = new DataMachineFiles(logger);
            files.setDataDirectory(args[DATADIR_ARG]);
            if (files.checkAll()) {
                try {

                    ISnapshotGenerator generator = environmentFactory.getSnapshotGenerator();
                    generator.setConfiguration(config);
                    generator.setFiles(files);
                    generator.start();

                    logger.log("End of the application. Working time = "
                            + (System.currentTimeMillis() - startTime) + " ms");
                    return EXIT_SUCCESS;
                } catch (Exception e) {
                    logger.log(e);
                    return EXIT_FAILURE;
                }
            } else {
                logger.log("Not all necessary source files could be found in " + args[DATADIR_ARG]);
                return EXIT_FAILURE;
            }

        } else {
            System.out.println(USAGE);
            return EXIT_USAGE;
        }
    }

    private static Configuration getConfigFromArgs(String[] args)
    {
        Configuration config = new Configuration(logger);
        config.setLanguage(args[LANG_ARG]);
        config.setMainCategory(args[MAINCATEGORY_ARG]);
        config.setDisambiguationCategory(args[DISAMBIGUATION_ARG]);
        return config;
    }
}
