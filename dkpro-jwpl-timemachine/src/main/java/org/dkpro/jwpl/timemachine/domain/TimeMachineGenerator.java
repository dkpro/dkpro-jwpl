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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.wikimachine.domain.AbstractSnapshotGenerator;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableEnum;
import org.dkpro.jwpl.wikimachine.dump.xml.DumpTableInputStream;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.dkpro.jwpl.wikimachine.factory.IEnvironmentFactory;
import org.dkpro.jwpl.wikimachine.util.TimestampUtil;

/**
 * Generate dumps as .txt files for the JWPL database from given MediaWiki dump files.<br>
 * By specifying a 'from' and a 'to' time stamps and the number of days to take as interval<br>
 * this class produces multiple dump versions.
 */
public class TimeMachineGenerator
    extends AbstractSnapshotGenerator
{

    private IDumpVersion[] versions = null;
    private TimeMachineFiles initialFiles = null;

    public TimeMachineGenerator(IEnvironmentFactory environmentFactory)
    {
        super(environmentFactory);
    }

    @Override
    public void setFiles(Files files)
    {
        initialFiles = (TimeMachineFiles) files;
    }

    private Integer calculateSnapshotsCount(Timestamp from, Timestamp to, Integer dayInterval)
    {
        Integer result = 0;

        for (Timestamp i = from; i.before(to); i = TimestampUtil.getNextTimestamp(i, dayInterval)) {
            result++;
        }

        return result;
    }

    @Override
    public void start() throws Exception
    {

        Timestamp fromTimestamp = configuration.getFromTimestamp();
        Timestamp toTimestamp = configuration.getToTimestamp();
        int each = configuration.getEach();

        int snapshotsCount = fromTimestamp.equals(toTimestamp) ? 1
                : calculateSnapshotsCount(fromTimestamp, toTimestamp, each);

        if (snapshotsCount > 0) {

            versions = new IDumpVersion[snapshotsCount];
            logger.log("Dumps to be generated:");

            for (int i = 0; i < snapshotsCount; i++) {

                Timestamp currentTimestamp = TimestampUtil.getNextTimestamp(fromTimestamp,
                        i * each);
                logger.log(currentTimestamp);

                MetaData commonMetaData = MetaData.initWithConfig(configuration);
                commonMetaData.setTimestamp(currentTimestamp);

                IDumpVersion version = envFactory.getDumpVersion();

                version.initialize(currentTimestamp);
                version.setMetaData(commonMetaData);
                TimeMachineFiles currentFiles = new TimeMachineFiles(initialFiles);
                currentFiles.setTimestamp(currentTimestamp);
                version.setFiles(currentFiles);
                versions[i] = version;
            }

            processInputDumps();

        }
        else {
            logger.log("No timestamps.");
        }
    }

    private void processInputDumps() throws IOException
    {

        dumpVersionProcessor.setDumpVersions(versions);

        logger.log("Processing the revision table");
        dumpVersionProcessor.processRevision(createRevisionParser());

        logger.log("Processing the page table");
        dumpVersionProcessor.processPage(createPageParser());

        logger.log("Processing the categorylinks table");
        dumpVersionProcessor.processCategorylinks(createCategorylinksParser());

        logger.log("Processing the pagelinks table");
        dumpVersionProcessor.processPagelinks(createPagelinksParser());

        logger.log("Processing the text table");
        dumpVersionProcessor.processText(createTextParser());

        logger.log("Writing meta data");
        dumpVersionProcessor.writeMetaData();
    }

    private RevisionParser createRevisionParser() throws IOException
    {
        DumpTableInputStream revisionTableInputStream = envFactory.getDumpTableInputStream();
        revisionTableInputStream.initialize(openMetaHistoryStreams(), DumpTableEnum.REVISION);

        RevisionParser revisionParser = envFactory.getRevisionParser();
        revisionParser.setInputStream(revisionTableInputStream);

        return revisionParser;
    }

    private PageParser createPageParser() throws IOException
    {
        DumpTableInputStream pageTableInputStream = envFactory.getDumpTableInputStream();
        pageTableInputStream.initialize(openMetaHistoryStreams(), DumpTableEnum.PAGE);

        PageParser pageParser = envFactory.getPageParser();
        pageParser.setInputStream(pageTableInputStream);

        return pageParser;
    }

    private CategorylinksParser createCategorylinksParser() throws IOException
    {

        String categorylinks = initialFiles.getCategoryLinksFile();
        InputStream categorylinksStream = decompressor.getInputStream(categorylinks);

        return new CategorylinksParser(categorylinksStream);

    }

    private PagelinksParser createPagelinksParser() throws IOException
    {

        String pagelinks = initialFiles.getPageLinksFile();

        InputStream pagelinksStream = decompressor.getInputStream(pagelinks);
        return new PagelinksParser(pagelinksStream);

    }

    private TextParser createTextParser() throws IOException
    {
        DumpTableInputStream textTableInputStream = envFactory.getDumpTableInputStream();
        textTableInputStream.initialize(openMetaHistoryStreams(), DumpTableEnum.TEXT);

        TextParser textParser = envFactory.getTextParser();
        textParser.setInputStream(textTableInputStream);

        return textParser;
    }

    /**
     * Opens a decompressed stream per configured meta-history part, preserving order. A
     * single-file dump yields a list of size 1; the call site hands the list to
     * {@link DumpTableInputStream#initialize(List, DumpTableEnum)} which dispatches to the
     * single- or multi-part SAX pipeline transparently.
     */
    private List<InputStream> openMetaHistoryStreams() throws IOException
    {
        final List<String> parts = initialFiles.getMetaHistoryFiles();
        final List<InputStream> streams = new ArrayList<>(parts.size());
        for (String part : parts) {
            streams.add(decompressor.getInputStream(part));
        }
        return streams;
    }
}
