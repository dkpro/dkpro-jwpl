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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.datamachine.dump.xml.XML2Binary;
import org.dkpro.jwpl.wikimachine.domain.AbstractSnapshotGenerator;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.ISnapshotGenerator;
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

/**
 * Transforms a database from mediawiki format to JWPL format.<br>
 * The transformation produces .txt files for the different tables in the JWPL database.
 *
 * @see AbstractSnapshotGenerator
 * @see ISnapshotGenerator
 */
public class DataMachineGenerator
    extends AbstractSnapshotGenerator
{

    private DataMachineFiles files = null;
    private IDumpVersion version = null;

    /**
     * Instantiates a {@link DataMachineGenerator} within the provided environment.
     *
     * @param environmentFactory The {@link IEnvironmentFactory factory} to use for bean creation.
     */
    public DataMachineGenerator(IEnvironmentFactory environmentFactory)
    {
        super(environmentFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFiles(Files files)
    {
        this.files = (DataMachineFiles) files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception
    {
        version = envFactory.getDumpVersion();
        MetaData metaData = MetaData.initWithConfig(configuration);
        version.initialize(null);
        version.setMetaData(metaData);
        version.setFiles(files);
        processInputDump();
    }

    private void processInputDump() throws IOException
    {

        logger.log("Parsing input dumps...");
        final List<String> parts = getPagesArticlesFiles();
        final List<InputStream> streams = new ArrayList<>(parts.size());
        for (String part : parts) {
            streams.add(decompressor.getInputStream(part));
        }
        // A single-file dump reduces to a one-element list; the multi-part XML2Binary
        // constructor handles both cases uniformly via MultiPartXmlDumpReader.
        new XML2Binary(streams, files);

        dumpVersionProcessor.setDumpVersions(new IDumpVersion[] { version });

        logger.log("Processing table page...");
        dumpVersionProcessor.processPage(createPageParser());

        logger.log("Processing table categorylinks...");
        dumpVersionProcessor.processCategorylinks(createCategorylinksParser());

        logger.log("Processing table pagelinks...");
        dumpVersionProcessor.processPagelinks(createPagelinksParser());

        logger.log("Processing table revision...");
        dumpVersionProcessor.processRevision(createRevisionParser());

        logger.log("Processing table text...");
        dumpVersionProcessor.processText(createTextParser());

        logger.log("Writing metadata...");
        dumpVersionProcessor.writeMetaData();

        logger.log("Finished");
    }

    /**
     * Selects the input articles dump in preferred order: {@code pages-meta-current} (when
     * available — includes discussions) falls back to {@code pages-articles}. Returns every
     * part of the selected role in ascending page-range order; a single-file dump yields a
     * list of size 1.
     *
     * @return the ordered list of input articles dump parts
     * @throws IOException If neither dump role is present.
     */
    private List<String> getPagesArticlesFiles() throws IOException
    {
        final List<String> metaCurrent = files.getInputPagesMetaCurrentFiles();
        if (!metaCurrent.isEmpty()) {
            logger.log("Discussions are available");
            return metaCurrent;
        }
        final List<String> articles = files.getInputPagesArticlesFiles();
        if (!articles.isEmpty()) {
            logger.log("Discussions are unavailable");
            return articles;
        }
        throw new IOException("No pages-articles or pages-meta-current dump found in the input "
                + "directory.");
    }

    private PageParser createPageParser() throws IOException
    {
        String pageFile = files.getGeneratedPage();

        DumpTableInputStream pageTableInputStream = envFactory.getDumpTableInputStream();
        pageTableInputStream.initialize(decompressor.getInputStream(pageFile), DumpTableEnum.PAGE);

        PageParser pageParser = envFactory.getPageParser();
        pageParser.setInputStream(pageTableInputStream);
        return pageParser;
    }

    private CategorylinksParser createCategorylinksParser() throws IOException
    {
        String categorylinksFile = files.getInputCategoryLinks();
        return new CategorylinksParser(decompressor.getInputStream(categorylinksFile));
    }

    private PagelinksParser createPagelinksParser() throws IOException
    {
        String pagelinksFile = files.getInputPageLinks();
        return new PagelinksParser(decompressor.getInputStream(pagelinksFile));
    }

    private RevisionParser createRevisionParser() throws IOException
    {
        String revisionFile = files.getGeneratedRevision();

        DumpTableInputStream revisionTableInputStream = envFactory
                .getDumpTableInputStream();
        revisionTableInputStream.initialize(decompressor.getInputStream(revisionFile),
                DumpTableEnum.REVISION);

        RevisionParser revisionParser = envFactory.getRevisionParser();
        revisionParser.setInputStream(revisionTableInputStream);
        return revisionParser;
    }

    private TextParser createTextParser() throws IOException
    {
        String textFile = files.getGeneratedText();

        DumpTableInputStream textTableInputStream = envFactory.getDumpTableInputStream();
        textTableInputStream.initialize(decompressor.getInputStream(textFile), DumpTableEnum.TEXT);

        TextParser textParser = envFactory.getTextParser();
        textParser.setInputStream(textTableInputStream);
        return textParser;
    }

}
