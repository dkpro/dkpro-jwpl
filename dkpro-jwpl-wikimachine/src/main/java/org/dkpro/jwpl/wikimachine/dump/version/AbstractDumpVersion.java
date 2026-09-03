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

import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.domain.Files;
import org.dkpro.jwpl.wikimachine.domain.MetaData;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.util.TxtFileWriter;

/**
 * A base {@link IDumpVersion} implementation that defines several common constants
 * and provides a group of {@link TxtFileWriter writers} for concrete subclasses.
 * <p>
 * Wikipedia namespace codes are defined according to the:
 * <a href="https://en.wikipedia.org/wiki/Wikipedia:Namespace">
 *   Wikipedia:Namespace</a> article.
 * A more compact list is found at:
 * <a href="https://en.wikipedia.org/wiki/Special:NamespaceInfo">
 *   https://en.wikipedia.org/wiki/Special:NamespaceInfo</a>.
 *
 * <p>
 * It also provides the shared implementation of the category link and page link row processing by
 * delegating to {@link LinkRowProcessor}; concrete subclasses only supply the {@link LinkRowSink}
 * lookup hooks over their own map flavour.
 *
 * @see IDumpVersion
 */
public abstract class AbstractDumpVersion
    implements IDumpVersion, LinkRowSink
{

    /** Namespace code for articles */
    protected static final int NS_MAIN = 0;
    /** Namespace code for talk on articles */
    protected static final int NS_TALK = 1;
    /** Namespace code for user pages */
    protected static final int NS_USER = 2;
    /** Namespace code for talk related to user pages */
    protected static final int NS_USER_TALK = 3;
    /** Namespace code for Wikipedia administration pages */
    protected static final int NS_WIKIPEDIA = 4;
    /** Namespace code for talk related to Wikipedia administration pages */
    protected static final int NS_WIKIPEDIA_TALK = 5;
    /** Namespace code for Wikipedia media content */
    protected static final int NS_FILE = 6;
    /** Namespace code for talk related to Wikipedia media content */
    protected static final int NS_FILE_TALK = 7;
    /** Namespace code for Mediawiki system messages */
    protected static final int NS_MEDIAWIKI = 8;
    /** Namespace code for talk related to Mediawiki system messages */
    protected static final int NS_MEDIAWIKI_TALK = 9;
    /** Namespace code for templates */
    protected static final int NS_TEMPLATE = 10;
    /** Namespace code for talk related to templates */
    protected static final int NS_TEMPLATE_TALK = 11;
    /** Namespace code for how-to and information pages */
    protected static final int NS_HELP = 12;
    /** Namespace code for talk related to how-to and information pages */
    protected static final int NS_HELP_TALK = 13;
    /** Namespace code for category pages */
    protected static final int NS_CATEGORY = 14;
    /** Namespace code for talk related to category pages */
    protected static final int NS_CATEGORY_TALK = 15;
    /** (Discontinued) Namespace code for thread pages */
    protected static final int NS_THREAD = 90;
    /** (Discontinued) Namespace code for talk related to thread pages */
    protected static final int NS_THREAD_TALK = 91;
    /** (Discontinued) Namespace code for summary pages */
    protected static final int NS_SUMMARY = 92;
    /** (Discontinued) Namespace code for talk related to summary pages */
    protected static final int NS_SUMMARY_TALK = 93;
    /** Namespace code for portal pages */
    protected static final int NS_PORTAL = 100;
    /** Namespace code for talk related to portal pages */
    protected static final int NS_PORTAL_TALK = 101;
    /** Namespace code for book pages */
    protected static final int NS_BOOK = 108;
    /** Namespace code for talk related to books */
    protected static final int NS_BOOK_TALK = 109;
    /** Namespace code for draft pages */
    protected static final int NS_DRAFT = 118;
    /** Namespace code for talk related to draft pages */
    protected static final int NS_DRAFT_TALK = 119;

    /** The timestamp of the (current) dump revision. */
    protected int timestamp;

    /** The {@link MetaData} for the (current) dump version. */
    protected MetaData metaData;

    /** A generic {@link TxtFileWriter}. */
    protected TxtFileWriter txtFW;
    /** A {@link TxtFileWriter} for page categories. */
    protected TxtFileWriter pageCategories;
    /** A {@link TxtFileWriter} for category pages. */
    protected TxtFileWriter categoryPages;
    /** A {@link TxtFileWriter} for category in-links. */
    protected TxtFileWriter categoryInlinks;
    /** A {@link TxtFileWriter} for category out-links. */
    protected TxtFileWriter categoryOutlinks;
    /** A {@link TxtFileWriter} for page in-links. */
    protected TxtFileWriter pageInlinks;
    /** A {@link TxtFileWriter} for page out-links. */
    protected TxtFileWriter pageOutlinks;
    /** A {@link TxtFileWriter} for page(s). */
    protected TxtFileWriter page = null;
    /** A {@link TxtFileWriter} for page map line. */
    protected TxtFileWriter pageMapLine = null;
    /** A {@link TxtFileWriter} for page redirects. */
    protected TxtFileWriter pageRedirects = null;

    /** A {@link Files version files} reference */
    protected Files versionFiles;
    /** A {@link ILogger logger} reference */
    protected ILogger logger = null;

    /** Whether to skip a category or not. Default: {@code true}. */
    protected boolean skipCategory = true;
    /** Whether to skip a page or not. Default: {@code true}. */
    protected boolean skipPage = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCategoryRedirectsSkip(boolean skipCategory)
    {
        this.skipCategory = skipCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageRedirectsSkip(boolean skipPage)
    {
        this.skipPage = skipPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(ILogger logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMetaData(MetaData metaData)
    {
        this.metaData = metaData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFiles(Files versionFiles)
    {
        this.versionFiles = versionFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initPageParsing() throws IOException
    {
        txtFW = new TxtFileWriter(versionFiles.getOutputCategory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportAfterPageParsing()
    {
        txtFW.flush();
        txtFW.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initCategoryLinksParsing() throws IOException
    {
        pageCategories = new TxtFileWriter(versionFiles.getOutputPageCategories());
        categoryPages = new TxtFileWriter(versionFiles.getOutputCategoryPages());
        categoryInlinks = new TxtFileWriter(versionFiles.getOutputCategoryInlinks());
        categoryOutlinks = new TxtFileWriter(versionFiles.getOutputCategoryOutlinks());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportAfterCategoryLinksParsing()
    {
        pageCategories.flush();
        pageCategories.close();

        categoryPages.flush();
        categoryPages.close();

        categoryInlinks.flush();
        categoryInlinks.close();

        categoryOutlinks.flush();
        categoryOutlinks.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initPageLinksParsing() throws IOException
    {
        pageInlinks = new TxtFileWriter(versionFiles.getOutputPageInlinks());
        pageOutlinks = new TxtFileWriter(versionFiles.getOutputPageOutlinks());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportAfterPageLinksParsing()
    {
        // export the written tables
        pageInlinks.flush();
        pageInlinks.close();

        pageOutlinks.flush();
        pageOutlinks.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initTextParsing() throws IOException
    {
        page = new TxtFileWriter(versionFiles.getOutputPage());
        pageMapLine = new TxtFileWriter(versionFiles.getOutputPageMapLine());
        pageRedirects = new TxtFileWriter(versionFiles.getOutputPageRedirects());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportAfterTextParsing()
    {
        page.flush();
        page.close();
        pageRedirects.flush();
        pageRedirects.close();
        pageMapLine.flush();
        pageMapLine.close();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flushByTextParsing()
    {
        page.flush();
        pageMapLine.flush();
        pageRedirects.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void processCategoryLinksRow(CategorylinksParser clParser) throws IOException
    {
        LinkRowProcessor.processCategoryLink(clParser, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void processPageLinksRow(PagelinksParser plParser)
    {
        LinkRowProcessor.processPageLink(plParser, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isSkipPageEnabled()
    {
        return skipPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisambiguationCategoryTitle()
    {
        return metaData.getDisambiguationCategory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writeCategoryMembership(int categoryId, int pageId)
    {
        categoryPages.addRow(categoryId, pageId);
        pageCategories.addRow(pageId, categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writeSubcategory(int parentCategoryId, int childCategoryId)
    {
        categoryOutlinks.addRow(parentCategoryId, childCategoryId);
        categoryInlinks.addRow(childCategoryId, parentCategoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writePageLink(int fromPageId, int toPageId)
    {
        pageOutlinks.addRow(fromPageId, toPageId);
        pageInlinks.addRow(toPageId, fromPageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportAfterRevisionParsing()
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initRevisionParsing()
    {

    }

    /**
     * {@inheritDoc}
     * <p>
     * This is the canonical {@code MetaData.txt} layout shared by the DataMachine and the
     * TimeMachine. Exactly nine tab separated values are written, in this order:
     * <ol>
     *   <li>{@code id}</li>
     *   <li>{@code language}</li>
     *   <li>{@code disambiguationCategory}</li>
     *   <li>{@code mainCategory}</li>
     *   <li>{@code nrofPages}</li>
     *   <li>{@code nrofRedirects}</li>
     *   <li>{@code nrofDisambiguationPages}</li>
     *   <li>{@code nrofCategories}</li>
     *   <li>{@code version}</li>
     * </ol>
     * The ninth value maps onto the {@code MetaData.version} database column. An absent version is
     * emitted as {@link MetaData#NULL_MARKER} — the {@code LOAD DATA INFILE} {@code NULL} marker —
     * rather than as the literal string {@code null} the underlying writer would otherwise produce
     * for a {@code null} element.
     */
    @Override
    public void writeMetaData() throws IOException
    {
        try (var outputFile = new TxtFileWriter(versionFiles.getOutputMetadata())) {
            // ID, LANGUAGE, DISAMBIGUATION_CATEGORY, MAIN_CATEGORY, nrOfPages, nrOfRedirects,
            // nrOfDisambiguationPages, nrOfCategories, version
            outputFile.addRow(metaData.getId(), metaData.getLanguage(),
                    metaData.getDisambiguationCategory(), metaData.getMainCategory(),
                    metaData.getNrOfPages(), metaData.getNrOfRedirects(),
                    metaData.getNrOfDisambiguations(), metaData.getNrOfCategories(),
                    metaData.getVersion() != null ? metaData.getVersion()
                            : MetaData.NULL_MARKER);
        }
    }

}
