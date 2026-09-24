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
package org.dkpro.jwpl.util.templates.generator.simple;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.PageIterator;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiInitializationException;
import org.dkpro.jwpl.api.util.StringUtils;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.parser.Template;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;
import org.dkpro.jwpl.parser.mediawiki.ShowTemplateNamesAndParameters;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.dkpro.jwpl.util.templates.WikipediaTemplateInfo;
import org.dkpro.jwpl.util.templates.WikipediaTemplateInfo.TemplateIds;
import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class determines which page in a JWPL database contains which templates. It produces an SQL
 * file that will add this data to the existing database. It can then be accessed by the
 * {@link WikipediaTemplateInfo} class.
 */
public class WikipediaTemplateInfoGenerator
{
    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private final MediaWikiParser parser;
    private Wikipedia wiki;

    private final DatabaseConfiguration dbConf;

    private final int pageBuffer;

    private final Map<String, Set<Integer>> TPLNAME_TO_REVISIONIDS = new HashMap<>();
    private final Map<String, Set<Integer>> TPLNAME_TO_PAGEIDS = new HashMap<>();
    private final Map<String, Integer> tplNameToTplId = new HashMap<>();

    private final String charset;
    // private final long maxAllowedPacket;
    private final String outputPath;

    private final int VERBOSITY = 500;

    private final TemplateFilter pageFilter;

    private final TemplateFilter revisionFilter;

    private boolean revisionTableExists;

    private boolean pageTableExists;

    private final GeneratorMode mode;

    public WikipediaTemplateInfoGenerator(DatabaseConfiguration dbc, int pageBuffer, String charset,
            String outputPath, long maxAllowedPacket, TemplateFilter pageFilter,
            TemplateFilter revisionFilter, GeneratorMode mode)
        throws WikiApiException
    {
        this.dbConf = dbc;
        this.pageBuffer = pageBuffer;

        MediaWikiParserFactory pf = new MediaWikiParserFactory(dbc.getLanguage());
        pf.setTemplateParserClass(ShowTemplateNamesAndParameters.class);
        parser = pf.createParser();

        // this.maxAllowedPacket = maxAllowedPacket;
        this.charset = charset;
        this.outputPath = outputPath;

        // Filters
        this.pageFilter = pageFilter;
        this.revisionFilter = revisionFilter;
        this.mode = mode;

    }

    /**
     * Fill map(mapToFill) with template data
     *
     * @param textForTemplateExtraction
     *            text for template extraction
     * @param filterToApply
     *            filter to apply for templates
     * @param id
     *            id of a page/revision
     * @param mapToFill
     *            map to fill with data
     */
    private void fillMapWithTemplateData(String textForTemplateExtraction,
            TemplateFilter filterToApply, int id, Map<String, Set<Integer>> mapToFill)
    {
        Set<String> names = getTemplateNames(textForTemplateExtraction);
        // Update the map with template values for current page
        for (String name : names) {

            // filter templates - only use templates from a provided
            // whitelist
            if (filterToApply.acceptTemplate(name)) {
                // Create records for TEMPLATE->PAGES/REVISION map
                if (mapToFill.containsKey(name)) {
                    // add the page id to the set for the current template
                    Set<Integer> pIdList = mapToFill.remove(name);
                    pIdList.add(id);
                    mapToFill.put(name, pIdList);
                }
                else {
                    // add new list with page id of current page
                    Set<Integer> newIdList = new HashSet<>();
                    newIdList.add(id);
                    mapToFill.put(name, newIdList);
                }
            }
        }
    }

    /**
     * Extracts templates from pages and revisions
     */
    private void extractTemplates() throws WikiApiException
    {
        PageIterator pageIter = new PageIterator(getWiki(), true, pageBuffer);
        RevisionAPIConfiguration revConfig = new RevisionAPIConfiguration(dbConf);
        RevisionApi revApi = new RevisionApi(revConfig);

        int pageCounter = 0;
        long[] revisionCounter = { 0L };

        while (pageIter.hasNext()) {
            pageCounter++;

            if (pageCounter % VERBOSITY == 0) {
                logger.info("{} pages processed ...", pageCounter);
            }

            Page curPage = pageIter.next();
            int curPageId = curPage.getPageId();

            // PROCESS PAGES
            if (mode.active_for_pages) {
                fillMapWithTemplateData(curPage.getText(), pageFilter, curPageId,
                        TPLNAME_TO_PAGEIDS);
            }

            // PROCESS REVISIONS
            if (mode.active_for_revisions) {
                List<Timestamp> tsList = revApi.getRevisionTimestamps(curPageId);
                forEachRevisionOfPage(revApi, revConfig, curPageId, tsList, curRevision -> {
                    revisionCounter[0]++;
                    if (revisionCounter[0] % (VERBOSITY * 10) == 0) {
                        logger.info("{} revisions processed ...", revisionCounter[0]);
                    }

                    fillMapWithTemplateData(curRevision.getRevisionText(), revisionFilter,
                            curRevision.getRevisionID(), TPLNAME_TO_REVISIONIDS);
                });
            }
        }
    }

    /**
     * Passes all revisions of the given page, with their text, to the given action. The revisions
     * are read in one sequential pass over the primary key range of the page, so that every diff
     * is applied once instead of rebuilding each revision from its preceding full revision.
     *
     * @param revApi
     *            the revision API whose connection is used; must not be {@code null}
     * @param revConfig
     *            the configuration of {@code revApi}; must not be {@code null}
     * @param pageId
     *            the id of the page
     * @param timestamps
     *            the timestamps of all revisions of the page
     * @param action
     *            the action to apply to each revision
     * @throws WikiApiException
     *             if the revisions cannot be accessed
     */
    static void forEachRevisionOfPage(RevisionApi revApi, RevisionAPIConfiguration revConfig,
            int pageId, List<Timestamp> timestamps, Consumer<Revision> action)
        throws WikiApiException
    {
        if (timestamps.isEmpty()) {
            return;
        }

        // The revisions of a page occupy consecutive primary keys in revision counter order.
        Revision anchor = revApi.getRevision(pageId, Collections.min(timestamps));
        int firstPK = anchor.getPrimaryKey() - anchor.getRevisionCounter() + 1;
        int lastPK = firstPK + timestamps.size() - 1;

        // The iterator shares the connection of revApi and must therefore not be closed; draining
        // it closes its statement. It may read one row beyond lastPK that belongs to another page.
        RevisionIterator revIter = new RevisionIterator(revConfig, firstPK, lastPK,
                revApi.getConnection());
        int rowsRead = 0;
        while (revIter.hasNext()) {
            if (rowsRead++ > timestamps.size()) {
                logger.warn("Unexpected revision data for page {}, skipping remaining revisions.",
                        pageId);
                break;
            }
            Revision curRevision = revIter.next();
            if (curRevision != null && curRevision.getArticleID() == pageId) {
                action.accept(curRevision);
            }
        }
    }

    /**
     * Extracts templates from pages only
     */
    private void processPages()
    {
        PageIterator pageIter = new PageIterator(getWiki(), true, pageBuffer);

        int pageCounter = 0;

        while (pageIter.hasNext()) {
            pageCounter++;

            if (pageCounter % VERBOSITY == 0) {
                logger.info("{} pages processed ...", pageCounter);
            }

            Page curPage = pageIter.next();
            int curPageId = curPage.getPageId();

            fillMapWithTemplateData(curPage.getText(), pageFilter, curPageId, TPLNAME_TO_PAGEIDS);
        }
    }

    /**
     * Processes only revision templates using the Revision Iterator
     */
    private void processRevisions()
    {
        logger.info("Processing revisions, extracting template information ...");
        RevisionIterator revisionIter = null;
        try {
            revisionIter = new RevisionIterator(dbConf);

            int revCounter = 0;
            while (revisionIter.hasNext()) {
                revCounter++;

                if (revCounter % VERBOSITY == 0) {
                    logger.info("{} revisions processed ...", revCounter);
                }

                Revision curRevision = revisionIter.next();
                int curRevisionId = curRevision.getRevisionID();

                fillMapWithTemplateData(curRevision.getRevisionText(), revisionFilter,
                        curRevisionId, TPLNAME_TO_REVISIONIDS);
            }
        }
        catch (WikiApiException e) {
            logger.error("Error initializing Revision Iterator", e);
        }
        finally {
            if (revisionIter != null) {
                try {
                    revisionIter.close();
                }
                catch (SQLException e) {
                    logger.error("Error closing RevisionIterator", e);
                }
            }
        }
    }

    /**
     * Start generator
     */
    public void process() throws Exception
    {
        WikipediaTemplateInfo info = new WikipediaTemplateInfo(getWiki());
        pageTableExists = info.tableExists(GeneratorConstants.TABLE_TPLID_PAGEID);
        revisionTableExists = info.tableExists(GeneratorConstants.TABLE_TPLID_REVISIONID);

        if (mode.useRevisionIterator) {
            if (mode.active_for_revisions) {
                processRevisions();
            }
            if (mode.active_for_pages) {
                processPages();
            }
        }
        else {
            try {
                extractTemplates();
            }
            catch (WikiApiException e) {
                logger.error("Error extracting templates.", e);
            }
        }

        ////////////////////

        logger.info("Generating template indices ...");
        boolean tableWithTemplatesExists;

        tableWithTemplatesExists = true;

        boolean resolvePageTemplates = mode.active_for_pages && pageTableExists;
        boolean resolveRevisionTemplates = mode.active_for_revisions && revisionTableExists;
        if (resolvePageTemplates || resolveRevisionTemplates) {
            try {
                TemplateIds existingIds = info.loadTemplateIds();
                if (resolvePageTemplates) {
                    resolveTemplateIds(existingIds::getTemplateId, TPLNAME_TO_PAGEIDS.keySet(),
                            tplNameToTplId);
                }
                if (resolveRevisionTemplates) {
                    resolveTemplateIds(existingIds::getTemplateId, TPLNAME_TO_REVISIONIDS.keySet(),
                            tplNameToTplId);
                }
            }
            catch (WikiApiException e) {
                logger.error("Problems generating template indices!", e);
            }
        }

        ////////////////////
        logger.info("Writing SQL dump ...");

        WikipediaTemplateInfoDumpWriter writer = new WikipediaTemplateInfoDumpWriter(
                this.outputPath, this.charset, this.tplNameToTplId, tableWithTemplatesExists);
        mode.templateNameToPageId = TPLNAME_TO_PAGEIDS;
        mode.templateNameToRevId = TPLNAME_TO_REVISIONIDS;
        writer.writeSQL(revisionTableExists, pageTableExists, mode);

        ////////////////////
    }

    /**
     * Puts the ids of templates that already exist in the database into the given map. Templates
     * without an id get a new one in the dump writer.
     *
     * @param existingIds
     *            returns the id of an existing template for its SQL escaped name, or {@code -1} if
     *            there is none, such as {@link TemplateIds#getTemplateId(String)}. Must not be
     *            {@code null}.
     * @param templateNames
     *            SQL escaped template names to resolve
     * @param tplNameToTplId
     *            the map to put the resolved ids into, keyed by the given template names
     */
    static void resolveTemplateIds(ToIntFunction<String> existingIds, Set<String> templateNames,
            Map<String, Integer> tplNameToTplId)
    {
        for (String name : templateNames) {
            if (tplNameToTplId.containsKey(name)) {
                continue;
            }
            int id = existingIds.applyAsInt(name);
            if (id != -1) {
                tplNameToTplId.put(name, id);
            }
        }
    }

    /**
     * Returns the set of names of all templates that are contained in the given article (without
     * duplicates).<br>
     * <p>
     * Note: The names are SQL escaped using {@link StringUtils#sqlEscape(String)}.
     *
     * @param pageText
     *            the page to get the templates from
     * @return a set of template names (without duplicates)
     */
    private Set<String> getTemplateNames(String pageText)
    {
        Set<String> names = new HashSet<>();
        if (!pageText.isEmpty()) {
            try {
                ParsedPage pp = parser.parse(pageText);
                List<Template> templates = pp.getTemplates();
                for (Template t : templates) {
                    names.add(StringUtils.sqlEscape(t.getName().toLowerCase()));
                }
            }
            catch (Exception e) {
                // Most likely parsing problems
                logger.error("Problems parsing page!", e);
            }
        }
        return names;
    }

    private Wikipedia getWiki()
    {
        if (this.wiki == null) {
            Wikipedia nWiki = null;
            try {
                nWiki = new Wikipedia(dbConf);
            }
            catch (WikiInitializationException e) {
                logger.error("Error initializing Wiki connection!", e);
            }
            return nWiki;
        }
        else {
            return this.wiki;
        }
    }

}
