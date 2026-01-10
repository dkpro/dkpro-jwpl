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
package org.dkpro.jwpl.wikimachine.dump.version;

import java.io.IOException;
import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * An abstraction for DumpVersion realization.
 */
public interface IDumpVersion
{

    /**
     * Formats the value of the bit to a String representation.
     * <p>
     * This is the way bit values are written in {@code .txt} dump files.
     *
     * @param b The boolean value to format.
     * @return {@code 1} if the given boolean is {@code true} or an empty String otherwise.
     */
    static String formatBoolean(boolean b)
    {
        return b ? new String(new byte[] { 1 }) : "";
    }

    /**
     * Configures the specified {@link ILogger}.
     *
     * @param logger The logger to use.
     */
    void setLogger(ILogger logger);

    /**
     * Toggles the category redirect skip.
     *
     * @param skipCategory {@code true} if category redirects should be skipped, {@code false} otherwise.
     */
    void setCategoryRedirectsSkip(boolean skipCategory);

    /**
     * Toggles the page redirect skip.
     *
     * @param skipPage {@code true} if page redirects should be skipped, {@code false} otherwise.
     */
    void setPageRedirectsSkip(boolean skipPage);

    /**
     * Initializes and associates a processing phase with a specified {@link Timestamp revision timestamp}.
     * @param timestamp The {@link Timestamp} to set.
     */
    void initialize(Timestamp timestamp);

    /**
     * Specifies the {@link MetaData meta data} to use.
     * @param commonMetaData A valid {@link MetaData} instance.
     */
    void setMetaData(MetaData commonMetaData);

    /**
     * Specifies the {@link Files version files} to use.
     * @param versionFiles A valid {@link Files} instance.
     */
    void setFiles(Files versionFiles);

    /**
     * Starts the parsing of a page revision row.
     */
    void initRevisionParsing();

    /**
     * Processes a revision row.
     *
     * @param revisionParser A valid {@link RevisionParser} instance.
     */
    void processRevisionRow(RevisionParser revisionParser);

    /**
     * Exports the results of revision parsing; to a file for instance.
     */
    void exportAfterRevisionParsing();

    /**
     * Clears internal state after a revision parsing phase.
     */
    void freeAfterRevisionParsing();

    /**
     * Starts the parsing of a page row.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    void initPageParsing() throws IOException;

    /**
     * Processes a page row.
     *
     * @param pageParser A valid {@link PageParser} instance.
     */
    void processPageRow(PageParser pageParser);

    /**
     * Exports the results of page parsing; to a file for instance.
     */
    void exportAfterPageParsing();

    /**
     * Clears internal state after a page parsing phase.
     */
    void freeAfterPageParsing();

    /**
     * Starts the parsing of a category links row.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    void initCategoryLinksParsing() throws IOException;

    /**
     * Processes a category link row.
     *
     * @param clParser A valid {@link CategorylinksParser} instance.
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    void processCategoryLinksRow(CategorylinksParser clParser) throws IOException;

    /**
     * Exports the results of category link parsing; to a file for instance.
     */
    void exportAfterCategoryLinksParsing();

    /**
     * Clears internal state after a category link parsing phase.
     */
    void freeAfterCategoryLinksParsing();

    /**
     * Starts the parsing of a page links row.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    void initPageLinksParsing() throws IOException;

    /**
     * Processes a page link row.
     *
     * @param plParser A valid {@link PagelinksParser} instance.
     */
    void processPageLinksRow(PagelinksParser plParser);

    /**
     * Exports the results of page link parsing; to a file for instance.
     */
    void exportAfterPageLinksParsing();

    /**
     * Clears internal state after a page link parsing phase.
     */
    void freeAfterPageLinksParsing();

    /**
     * Starts the parsing of a text row.
     * 
     * @throws IOException Thrown if IO errors occurred.
     */
    void initTextParsing() throws IOException;

    /**
     * Processes a text row.
     *
     * @param textParser A valid {@link TextParser} instance.
     */
    void processTextRow(TextParser textParser);

    /**
     * Ensures internal buffers to be flushed before export phase.
     */
    void flushByTextParsing();

    /**
     * Exports the results of text parsing; to a file for instance.
     */
    void exportAfterTextParsing();

    /**
     * Clears internal state after a text parsing phase.
     */
    void freeAfterTextParsing();

    /**
     * Exports the (current) {@link MetaData}; to a file for instance.
     *
     * @throws IOException Thrown if IO errors occurred during export.
     */
    void writeMetaData() throws IOException;

}
