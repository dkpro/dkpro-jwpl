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
package org.dkpro.jwpl.wikimachine.domain;

import java.io.IOException;

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * A processor of Wikipedias dump related revisions.
 *
 * @see IDumpVersion
 */
public class DumpVersionProcessor
{

    private static ILogger logger;

    private Integer step2Log = 100000;
    private Integer step2GC = step2Log * 10;
    private Integer step2Flush = step2GC;
    private IDumpVersion[] versions;

    /**
     * Instantiates a {@link DumpVersionProcessor} with a specified {@link ILogger}.
     *
     * @param initialLogger The {@link ILogger} to use initially.
     */
    public DumpVersionProcessor(ILogger initialLogger)
    {
        logger = initialLogger;
    }

    /**
     * Sets the collection of {@link IDumpVersion versions} to process (next).
     * @param versions A non-empty array of {@link IDumpVersion versions}.
     */
    public void setDumpVersions(IDumpVersion[] versions)
    {
        this.versions = versions;
    }

    /**
     * Configures the parameter {@code step2Log}.
     * @param step2Log A positive value for the number of steps to log.
     */
    public void setStep2Log(Integer step2Log)
    {
        this.step2Log = step2Log;
    }

    /**
     * Configures the parameter {@code step2GC}.
     * @param step2GC A positive value for the number of steps until garbage collection (GC) is triggered.
     */
    public void setStep2GC(Integer step2GC)
    {
        this.step2GC = step2GC;
    }

    /**
     * Configures the parameter {@code step2Flush}.
     * @param step2Flush A positive value for the number of steps until resources are flushed.
     */
    public void setStep2Flush(Integer step2Flush)
    {
        this.step2Flush = step2Flush;
    }

    /**
     * Processes a revision row.
     *
     * @param revisionParser A valid {@link RevisionParser} instance.
     *                       
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processRevision(RevisionParser revisionParser) throws IOException
    {
        for (IDumpVersion version : versions) {
            version.initRevisionParsing();
        }
        int counter = 0;
        while (revisionParser.next()) {
            for (IDumpVersion version : versions) {
                version.processRevisionRow(revisionParser);
            }

            logAndClear(++counter, "Revision");
        }

        for (IDumpVersion version : versions) {
            version.exportAfterRevisionParsing();
            version.freeAfterRevisionParsing();
        }

        revisionParser.close();
    }

    /**
     * Processes a page row.
     *
     * @param pageParser A valid {@link PageParser} instance.
     *                   
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processPage(PageParser pageParser) throws IOException
    {
        for (IDumpVersion version : versions) {
            version.initPageParsing();
        }

        int counter = 0;
        while (pageParser.next()) {
            for (IDumpVersion version : versions) {
                version.processPageRow(pageParser);
            }
            logAndClear(++counter, "Pages");
        }

        for (IDumpVersion version : versions) {
            version.exportAfterPageParsing();
            version.freeAfterPageParsing();
        }

        pageParser.close();
    }

    /**
     * Processes a category link row.
     *
     * @param categorylinksParser A valid {@link CategorylinksParser} instance.
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processCategorylinks(CategorylinksParser categorylinksParser) throws IOException
    {
        try (categorylinksParser) {
            for (IDumpVersion version : versions) {
                version.initCategoryLinksParsing();
            }

            int counter = 0;
            while (categorylinksParser.next()) {
                for (IDumpVersion version : versions) {
                    version.processCategoryLinksRow(categorylinksParser);
                }
                logAndClear(++counter, "Categorylinks");
            }

            for (IDumpVersion version : versions) {
                version.exportAfterCategoryLinksParsing();
                version.freeAfterCategoryLinksParsing();
            }
        }
    }

    /**
     * Processes a page link row.
     *
     * @param pagelinksParser A valid {@link PagelinksParser} instance.
     *
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processPagelinks(PagelinksParser pagelinksParser) throws IOException
    {
        try (pagelinksParser) {
            for (IDumpVersion version : versions) {
                version.initPageLinksParsing();
            }

            int counter = 0;
            while (pagelinksParser.next()) {
                for (IDumpVersion version : versions) {
                    version.processPageLinksRow(pagelinksParser);
                }
                logAndClear(++counter, "Pagelinks");
            }

            for (IDumpVersion version : versions) {
                version.exportAfterPageLinksParsing();
                version.freeAfterPageLinksParsing();
            }
        }
    }

    /**
     * Processes a text row.
     *
     * @param textParser A valid {@link TextParser} instance.
     *
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public void processText(TextParser textParser) throws IOException
    {
        for (IDumpVersion version : versions) {
            version.initTextParsing();
        }

        int counter = 0;
        while (textParser.next()) {
            for (IDumpVersion version : versions) {
                version.processTextRow(textParser);
            }
            if (step2Flush != 0 && counter % step2Flush == 0) {
                for (IDumpVersion version : versions) {
                    version.flushByTextParsing();
                }
            }
            logAndClear(++counter, "Text");

        }

        for (IDumpVersion version : versions) {
            version.exportAfterTextParsing();
            version.freeAfterTextParsing();
        }

        textParser.close();
    }

    /**
     * Exports the (current) {@link MetaData} for all versions.
     *
     * @throws IOException Thrown if IO errors occurred during export.
     */
    public void writeMetaData() throws IOException
    {
        for (IDumpVersion version : versions) {
            version.writeMetaData();
        }
    }

    private void logAndClear(int counter, String event)
    {
        if (step2Log != 0 && counter % step2Log == 0) {
            String message = event + " " + counter;
            logger.log(message);
        }
    }

}
