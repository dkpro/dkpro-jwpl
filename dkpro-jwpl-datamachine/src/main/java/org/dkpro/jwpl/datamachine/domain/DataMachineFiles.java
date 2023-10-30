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

import java.io.File;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;

public class DataMachineFiles extends Files {
  private final static String INPUT_PAGELINKS = "pagelinks.sql";
  private final static String INPUT_PAGESARTICLES = "pages-articles.xml";
  private final static String INPUT_CATEGORYLINKS = "categorylinks.sql";
  private final static String INPUT_PAGESMETACURRENT = "pages-meta-current.xml";

  private final static String GENERATED_PAGE = "page.bin";
  private final static String GENERATED_REVISION = "revision.bin";
  private final static String GENERATED_TEXT = "text.bin";
  /*
   * discussions.bin is currently unused. Discussions are put in pages.bin
   */
  private final static String GENERATED_DISCUSSIONS = "discussions.bin";

  private final static String ARCHIVE_EXTENSION = ".gz";

  private File dataDirectory = new File(".");
  private boolean compressGeneratedFiles = false;

  private File inputPagelinks = null;
  private File inputPagesarticles = null;
  private File inputCategorylinks = null;
  private File inputPagesMetaCurrent = null;

  public DataMachineFiles(ILogger logger) {
    super(logger);
    outputDirectory = setOutputDirectory(dataDirectory);
  }

  public DataMachineFiles(DataMachineFiles files) {
    super(files);
    this.dataDirectory = files.dataDirectory;
    this.compressGeneratedFiles = files.compressGeneratedFiles;
    this.inputPagelinks = files.inputPagelinks;
    this.inputPagesarticles = files.inputPagesarticles;
    this.inputCategorylinks = files.inputCategorylinks;
    this.inputPagesMetaCurrent = files.inputPagesMetaCurrent;
  }

  private File setOutputDirectory(File parentDirectory) {
    File result = new File(parentDirectory.getAbsolutePath()
            + File.separator + OUTPUT_DIRECTORY);

    return result;
  }

  public void setDataDirectory(String newDataDirectory) {
    File inputDataDirectory = new File(newDataDirectory);
    if (inputDataDirectory.isDirectory()) {
      this.dataDirectory = inputDataDirectory;
      this.outputDirectory = setOutputDirectory(dataDirectory);
    } else {
      logger.log(dataDirectory
              + " is not a directory. Continue read from "
              + this.dataDirectory.getAbsolutePath());
    }

  }

  public boolean checkDatamachineSourceFiles() {
    File[] filesInDataDirectory = dataDirectory.listFiles();
    if (filesInDataDirectory.length > 2) {
      for (File currentFile : filesInDataDirectory) {

        //TODO improve file check. Only accept files that come in a supported compression format
        String currentFileName = currentFile.getName();
        if (currentFileName.contains(INPUT_PAGESARTICLES)) {
          inputPagesarticles = currentFile;
        } else if (currentFileName.contains(INPUT_PAGELINKS)) {
          inputPagelinks = currentFile;
        } else if (currentFileName.contains(INPUT_CATEGORYLINKS)) {
          inputCategorylinks = currentFile;
        } else if (currentFileName.contains(INPUT_PAGESMETACURRENT)) {
          inputPagesMetaCurrent = currentFile;
        }
      }
    }
    // either inputPagesarticles or inputPagesMetaCurrent have to be placed
    // in the input directory
    return !((inputPagesarticles == null && inputPagesMetaCurrent == null)
            || inputPagelinks == null || inputCategorylinks == null);
  }

  public String getGeneratedPage() {
    return getGeneratedPath(GENERATED_PAGE);
  }

  public String getGeneratedRevision() {
    return getGeneratedPath(GENERATED_REVISION);
  }

  public String getGeneratedText() {
    return getGeneratedPath(GENERATED_TEXT);
  }

  public String getGeneratedDiscussions() {
    return getGeneratedPath(GENERATED_DISCUSSIONS);
  }

  public String getInputPageLinks() {
    return (inputPagelinks != null) ? inputPagelinks.getAbsolutePath()
            : null;
  }

  public String getInputPagesArticles() {
    return (inputPagesarticles != null) ? inputPagesarticles
            .getAbsolutePath() : null;
  }

  public String getInputCategoryLinks() {
    return (inputCategorylinks != null) ? inputCategorylinks
            .getAbsolutePath() : null;
  }

  public String getInputPagesMetaCurrent() {
    return (inputPagesMetaCurrent != null) ? inputPagesMetaCurrent
            .getAbsolutePath() : null;
  }


  private String getGeneratedPath(String fileName) {
    String path = dataDirectory.getAbsolutePath() + File.separator
            + fileName;
    if (compressGeneratedFiles) {
      path = path.concat(ARCHIVE_EXTENSION);
    }
    return path;
  }

  /**
   * @see DataMachineFiles#setCompressGeneratedFiles(boolean)
   */
  public boolean isCompressGeneratedFiles() {
    return compressGeneratedFiles;
  }

  /**
   * Set the input parameter to {@code true} it you want to GZip the temporary
   * files and save a disk space. <b>Attention:</b> {@code DataInputStream}
   * can have problems reading from a compressed file. This can be a reason
   * for strange side effects like heap overflow or some other exceptions. <br>
   * For UKP-Developers: you can save much more disk space if you'll parse the
   * page-articles XML Dump every time you need it: during processPage(),
   * processRevision() and processText(). See TimeMachine solution especially
   * the package org.dkpro.jwpl.timemachine.dump.xml
   *
   * @param compressGeneratedFiles
   */
  public void setCompressGeneratedFiles(boolean compressGeneratedFiles) {
    this.compressGeneratedFiles = compressGeneratedFiles;
  }

  @Override
  public boolean checkAll() {
    return checkOutputDirectory() && checkDatamachineSourceFiles();
  }
}
