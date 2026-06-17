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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.util.DumpFileDiscovery;

/**
 * A {@link Files} implementation specific for the DataMachine tool.
 * It defines file name constants and provides methods for
 * input/output directory building rules and checks.
 * <p>
 * Wikimedia publishes large XML dumps split across several files (see
 * {@link DumpFileDiscovery}). For the {@code pages-articles} and {@code pages-meta-current}
 * roles this class keeps the ordered list of parts and exposes both the legacy singular
 * getter (first part of the ordered list, for backwards compatibility) and a list getter
 * that returns every part.
 *
 * @see Files
 */
public class DataMachineFiles
    extends Files
{
    private final static String INPUT_PAGELINKS = "pagelinks.sql";
    private final static String INPUT_CATEGORYLINKS = "categorylinks.sql";
    private final static String INPUT_PAGESARTICLES = "pages-articles";
    private final static String INPUT_PAGESMETACURRENT = "pages-meta-current";

    private final static String GENERATED_PAGE = "page.bin";
    private final static String GENERATED_REVISION = "revision.bin";
    private final static String GENERATED_TEXT = "text.bin";
    /*
     * discussions.bin is currently unused. Discussions are put in pages.bin
     */
    private final static String GENERATED_DISCUSSIONS = "discussions.bin";

    private final static String ARCHIVE_EXTENSION = ".gz";

    private final static Set<String> SUPPORTED_EXTENSIONS = Set.of("bz2", "gz", "7z");

    private File dataDirectory = new File(".");
    private boolean compressGeneratedFiles = false;

    private File inputPagelinks = null;
    private File inputCategorylinks = null;
    private List<File> inputPagesarticles = new ArrayList<>();
    private List<File> inputPagesMetaCurrent = new ArrayList<>();

    /**
     * Instantiates a {@link Files} object with the specified {@code logger}.
     *
     * @param logger The {@link ILogger logger} to use.
     */
    public DataMachineFiles(ILogger logger)
    {
        super(logger);
        outputDirectory = setOutputDirectory(dataDirectory);
    }

    /**
     * Instantiates a copy of the specified {@link DataMachineFiles} object.
     *
     * @param files The {@link DataMachineFiles files} to use for the copy.
     */
    public DataMachineFiles(DataMachineFiles files)
    {
        super(files);
        this.dataDirectory = files.dataDirectory;
        this.inputPagelinks = files.inputPagelinks;
        this.inputPagesarticles = new ArrayList<>(files.inputPagesarticles);
        this.inputCategorylinks = files.inputCategorylinks;
        this.inputPagesMetaCurrent = new ArrayList<>(files.inputPagesMetaCurrent);
        this.compressGeneratedFiles = files.compressGeneratedFiles;
    }

    private File setOutputDirectory(File parentDirectory)
    {
        return new File(parentDirectory.getAbsolutePath() + File.separator + OUTPUT_DIRECTORY);
    }

    void setDataDirectory(String newDataDirectory)
    {
        if (newDataDirectory == null || newDataDirectory.isBlank()) {
            throw new IllegalArgumentException("New data directory must not be null or blank!");
        }
        File inputDataDirectory = new File(newDataDirectory);
        if (inputDataDirectory.isDirectory()) {
            this.dataDirectory = inputDataDirectory;
            this.outputDirectory = setOutputDirectory(dataDirectory);
        } else {
            logger.log(inputDataDirectory + " is not a directory. Continue using: "
                    + this.dataDirectory.getAbsolutePath());
        }

    }

    private boolean checkDataMachineSourceFiles()
    {
        final FileFilter supportedFormatFilter = file -> {
            final String name = file.getName();
            return name.endsWith(".7z") || name.endsWith(".gz") || name.endsWith(".bz2");
        };
        final File[] files = dataDirectory.listFiles(supportedFormatFilter);
        if (files != null && files.length >= 3) {
            final List<File> articleParts = new ArrayList<>();
            final List<File> metaCurrentParts = new ArrayList<>();
            for (File currentFile : files) {
                final String name = currentFile.getName();
                if (DumpFileDiscovery.matchesRole(name, INPUT_PAGESARTICLES, SUPPORTED_EXTENSIONS)) {
                    articleParts.add(currentFile);
                }
                else if (DumpFileDiscovery.matchesRole(name, INPUT_PAGESMETACURRENT,
                        SUPPORTED_EXTENSIONS)) {
                    metaCurrentParts.add(currentFile);
                }
                else if (name.contains(INPUT_PAGELINKS)) {
                    inputPagelinks = currentFile;
                }
                else if (name.contains(INPUT_CATEGORYLINKS)) {
                    inputCategorylinks = currentFile;
                }
            }
            inputPagesarticles = DumpFileDiscovery.orderByPageRange(articleParts);
            inputPagesMetaCurrent = DumpFileDiscovery.orderByPageRange(metaCurrentParts);
        }
        // either inputPagesarticles or inputPagesMetaCurrent have to be placed
        // in the input directory
        return !((inputPagesarticles.isEmpty() && inputPagesMetaCurrent.isEmpty())
                || inputPagelinks == null || inputCategorylinks == null);
    }

    /**
     * @return Retrieves the absolute path of the {@link #GENERATED_PAGE} file.
     */
    public String getGeneratedPage()
    {
        return getGeneratedPath(GENERATED_PAGE);
    }

    /**
     * @return Retrieves the absolute path of the {@link #GENERATED_REVISION} file.
     */
    public String getGeneratedRevision()
    {
        return getGeneratedPath(GENERATED_REVISION);
    }

    /**
     * @return Retrieves the absolute path of the {@link #GENERATED_TEXT} file.
     */
    public String getGeneratedText()
    {
        return getGeneratedPath(GENERATED_TEXT);
    }

    /**
     * @return Retrieves the absolute path of the {@link #GENERATED_DISCUSSIONS} file.
     */
    public String getGeneratedDiscussions()
    {
        return getGeneratedPath(GENERATED_DISCUSSIONS);
    }

    /**
     * @return Retrieves the absolute path of the {@code pagelinks.sql} file.
     */
    public String getInputPageLinks()
    {
        if (inputPagelinks == null) {
            checkDataMachineSourceFiles();
        }
        return inputPagelinks != null ? inputPagelinks.getAbsolutePath() : null;
    }

    /**
     * @return Retrieves the absolute path of the first {@code pages-articles.xml} part,
     *         or {@code null} if none was discovered. For multi-part dumps, prefer
     *         {@link #getInputPagesArticlesFiles()}.
     */
    public String getInputPagesArticles()
    {
        if (inputPagesarticles.isEmpty()) {
            checkDataMachineSourceFiles();
        }
        return inputPagesarticles.isEmpty() ? null : inputPagesarticles.get(0).getAbsolutePath();
    }

    /**
     * @return Absolute paths of all {@code pages-articles.xml} parts ordered by ascending page
     *         range. Empty if the dump is not available. A single-file dump yields a list of
     *         size 1.
     */
    public List<String> getInputPagesArticlesFiles()
    {
        if (inputPagesarticles.isEmpty()) {
            checkDataMachineSourceFiles();
        }
        return toAbsolutePathList(inputPagesarticles);
    }

    /**
     * @return Retrieves the absolute path of the {@code categorylinks.sql} file.
     */
    public String getInputCategoryLinks()
    {
        if (inputCategorylinks == null) {
            checkDataMachineSourceFiles();
        }
        return inputCategorylinks != null ? inputCategorylinks.getAbsolutePath() : null;
    }

    /**
     * @return Retrieves the absolute path of the first {@code pages-meta-current.xml} part,
     *         or {@code null} if none was discovered. For multi-part dumps, prefer
     *         {@link #getInputPagesMetaCurrentFiles()}.
     */
    public String getInputPagesMetaCurrent()
    {
        if (inputPagesMetaCurrent.isEmpty()) {
            checkDataMachineSourceFiles();
        }
        return inputPagesMetaCurrent.isEmpty() ? null
                : inputPagesMetaCurrent.get(0).getAbsolutePath();
    }

    /**
     * @return Absolute paths of all {@code pages-meta-current.xml} parts ordered by ascending
     *         page range. Empty if the dump is not available.
     */
    public List<String> getInputPagesMetaCurrentFiles()
    {
        if (inputPagesMetaCurrent.isEmpty()) {
            checkDataMachineSourceFiles();
        }
        return toAbsolutePathList(inputPagesMetaCurrent);
    }

    private static List<String> toAbsolutePathList(List<File> files)
    {
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> paths = new ArrayList<>(files.size());
        for (File f : files) {
            paths.add(f.getAbsolutePath());
        }
        return paths;
    }

    private String getGeneratedPath(String fileName)
    {
        String path = dataDirectory.getAbsolutePath() + File.separator + fileName;
        if (compressGeneratedFiles) {
            path = path.concat(ARCHIVE_EXTENSION);
        }
        return path;
    }

    /**
     * @return {@code true} if temporary files are GZip compressed, {@code false} otherwise.
     */
    public boolean isCompressGeneratedFiles()
    {
        return compressGeneratedFiles;
    }

    /**
     * Set the input parameter to {@code true} if you want to GZip the temporary files and save a
     * disk space. <b>Attention:</b> {@code DataInputStream} can have problems reading from a
     * compressed file. This can be a reason for strange side effects like heap overflow or some
     * other exceptions. <br>
     * For UKP-Developers: you can save much more disk space if you'll parse the page-articles XML
     * Dump every time you need it: during processPage(), processRevision() and processText(). See
     * TimeMachine solution especially the package org.dkpro.jwpl.timemachine.dump.xml
     *
     * @param compressGeneratedFiles {@code true} if you want to GZip the temporary files,
     *                               {@code false} otherwise.
     */
    public void setCompressGeneratedFiles(boolean compressGeneratedFiles)
    {
        this.compressGeneratedFiles = compressGeneratedFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkAll()
    {
        return checkOutputDirectory() && checkDataMachineSourceFiles();
    }
}
