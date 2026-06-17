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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.util.TimestampUtil;

public class TimeMachineFiles
    extends Files
{

    private static final String NO_CATEGORYLINKS = "category links file not found";
    private static final String NO_METAHISTORY = "meta history file not found";
    private static final String NO_PAGELINKS = "page links file not found";

    private final List<String> metaHistoryFiles = new ArrayList<>();
    private String pageLinksFile;
    private String categoryLinksFile;
    private String timeStamp = "";

    public TimeMachineFiles(ILogger logger)
    {
        super(logger);
    }

    public TimeMachineFiles(TimeMachineFiles files)
    {
        super(files);
        this.metaHistoryFiles.addAll(files.metaHistoryFiles);
        this.pageLinksFile = files.pageLinksFile;
        this.categoryLinksFile = files.categoryLinksFile;
    }

    /**
     * Add a subdirectory called "timestamp" to the current output directory
     *
     * @param timestamp Corresponds to the name of a new subdirectory.
     */
    public void setTimestamp(Timestamp timestamp)
    {
        timeStamp = TimestampUtil.toMediaWikiString(timestamp) + File.separator;
    }

    /**
     * @return The first (or only) meta-history dump file, or {@code null} if none was configured.
     *         For multi-part dumps, prefer {@link #getMetaHistoryFiles()}.
     */
    public String getMetaHistoryFile()
    {
        return metaHistoryFiles.isEmpty() ? null : metaHistoryFiles.get(0);
    }

    /**
     * Replaces the meta-history configuration with the single given path.
     *
     * @param metaHistoryFile Absolute or relative path to the meta-history dump. May be
     *                        {@code null} to clear the configuration.
     */
    public void setMetaHistoryFile(String metaHistoryFile)
    {
        this.metaHistoryFiles.clear();
        if (metaHistoryFile != null) {
            this.metaHistoryFiles.add(metaHistoryFile);
        }
    }

    /**
     * @return An unmodifiable view of the ordered meta-history dump parts. A single-file dump
     *         yields a list of size 1; never {@code null}.
     */
    public List<String> getMetaHistoryFiles()
    {
        return Collections.unmodifiableList(metaHistoryFiles);
    }

    /**
     * Replaces the meta-history configuration with the given ordered list of parts. The order
     * must reflect the ascending page-range order expected by the downstream multi-part XML
     * pipeline.
     *
     * @param metaHistoryFiles Ordered list of part paths. Must not be {@code null} or empty and
     *                         must not contain {@code null} elements.
     * @throws IllegalArgumentException If the argument violates the above.
     */
    public void setMetaHistoryFiles(List<String> metaHistoryFiles)
    {
        if (metaHistoryFiles == null || metaHistoryFiles.isEmpty()) {
            throw new IllegalArgumentException("'metaHistoryFiles' must not be null or empty.");
        }
        for (int i = 0; i < metaHistoryFiles.size(); i++) {
            if (metaHistoryFiles.get(i) == null) {
                throw new IllegalArgumentException("'metaHistoryFiles[" + i + "]' is null.");
            }
        }
        this.metaHistoryFiles.clear();
        this.metaHistoryFiles.addAll(metaHistoryFiles);
    }

    public String getPageLinksFile()
    {
        return pageLinksFile;
    }

    public void setPageLinksFile(String pageLinksFile)
    {
        this.pageLinksFile = pageLinksFile;
    }

    public String getCategoryLinksFile()
    {
        return categoryLinksFile;
    }

    public void setCategoryLinksFile(String categoryLinksFile)
    {
        this.categoryLinksFile = categoryLinksFile;
    }

    public boolean checkInputFile(String fileName, String errorMessage)
    {
        File inputFile = new File(fileName);
        boolean result = inputFile.exists() && inputFile.canRead();
        if (!result) {
            logger.log(errorMessage);
        }
        return result;
    }

    @Override
    protected String getOutputPath(String fileName)
    {
        File outputSubDirectory = new File(outputDirectory.getAbsolutePath() + File.separator + timeStamp);
        outputSubDirectory.mkdir();
        return outputDirectory.getAbsolutePath() + File.separator + timeStamp + fileName;
    }

    @Override
    public boolean checkAll()
    {
        if (!checkOutputDirectory()) {
            return false;
        }
        if (metaHistoryFiles.isEmpty()) {
            logger.log(NO_METAHISTORY);
            return false;
        }
        for (String part : metaHistoryFiles) {
            if (!checkInputFile(part, NO_METAHISTORY)) {
                return false;
            }
        }
        return checkInputFile(pageLinksFile, NO_PAGELINKS)
                && checkInputFile(categoryLinksFile, NO_CATEGORYLINKS);
    }
}
