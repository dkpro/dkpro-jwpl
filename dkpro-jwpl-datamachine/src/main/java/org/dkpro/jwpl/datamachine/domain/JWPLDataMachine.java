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

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Configuration;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.factory.SpringFactory;

/**
 * Starts the transformation from Mediawiki dump format to JWPL dump format.
 */
public class JWPLDataMachine {

  private static final int LANG_ARG = 0;
  private static final int MAINCATEGORY_ARG = 1;
  private static final int DISAMBIGUATION_ARG = 2;
  private static final int DATADIR_ARG = 3;

  private static final String USAGE = "Please use\n"
          + "\tjava -jar JWPLDataMachine.jar <LANGUAGE> <TOP_CATEGORY_NAME> <DISAMBIGUATION_CATEGORY_NAME> <SOURCE_DIRECTORY>\n\n"
          + "The source directory must contain files\n"
          + "\tpagelinks.sql\n"
          + "\tpages-articles.xml\n"
          + "\tcategorylinks.sql\n"
          + "GZip or BZip2 compressed archives of above-named files are also allowed.\n"
          + "Please set up a decompressor.xml for a usage of other external archive utilities (see documentation for more help).\n";

  private static final long startTime = System.currentTimeMillis();

  private static final IEnvironmentFactory environmentFactory = SpringFactory.getInstance();

  private static final ILogger logger = environmentFactory.getLogger();

  public static void main(String[] args) {
    if (args.length > 3) {
      Configuration config = getConfigFromArgs(args);
      DataMachineFiles files = new DataMachineFiles(logger);
      files.setDataDirectory(args[DATADIR_ARG]);
      if (files.checkAll()) {
        try {

          ISnapshotGenerator generator = environmentFactory
                  .getSnapshotGenerator();
          generator.setConfiguration(config);
          generator.setFiles(files);
          generator.start();

          logger.log("End of the application. Working time = "
                  + (System.currentTimeMillis()
                  - startTime) + " ms");
        } catch (Exception e) {
          logger.log(e);
        }
      } else {
        logger.log("Not all necessary source files could be found in "
                + args[DATADIR_ARG]);
      }

    } else {
      System.out.println(USAGE);
    }

  }

  private static Configuration getConfigFromArgs(String[] args) {
    Configuration config = new Configuration(logger);
    config.setLanguage(args[LANG_ARG]);
    config.setMainCategory(args[MAINCATEGORY_ARG]);
    config.setDisambiguationCategory(args[DISAMBIGUATION_ARG]);

    return config;
  }

}
