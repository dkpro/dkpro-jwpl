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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.LinkTargetResolver;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link LinkRowProcessor} classifies a {@code categorylinks} row the same way on the
 * legacy and on the normalised layout: from {@code cl_type} where the dump carries that column and
 * from the registered ids where it does not (see issue #491).
 */
class LinkRowProcessorTest
{

    /**
     * A {@code categorylinks} dump carrying {@code cl_type}, in the normalised layout of
     * MediaWiki 1.43+. The rows are, in order: a page member, a subcategory, a file member, a page
     * member whose source is not registered at all, and a stale {@code page} row whose source
     * happens to be a registered category - the one row the two layouts classify differently.
     */
    private static final String CATEGORYLINKS_WITH_TYPE = """
            CREATE TABLE `categorylinks` (
              `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `cl_sortkey` varbinary(230) NOT NULL DEFAULT '',
              `cl_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
              `cl_sortkey_prefix` varbinary(255) NOT NULL DEFAULT '',
              `cl_type` enum('page','subcat','file') NOT NULL DEFAULT 'page',
              `cl_collation_id` smallint(5) unsigned NOT NULL DEFAULT 0,
              `cl_target_id` bigint(20) unsigned NOT NULL,
              PRIMARY KEY (`cl_from`,`cl_target_id`)
            ) ENGINE=InnoDB;
            INSERT INTO `categorylinks` VALUES \
            (11,'A','2025-01-01 00:00:00','','page',1,700),\
            (21,'B','2025-01-01 00:00:00','','subcat',1,700),\
            (31,'C','2025-01-01 00:00:00','','file',1,700),\
            (99,'D','2025-01-01 00:00:00','','page',1,700),\
            (22,'E','2025-01-01 00:00:00','','page',1,700);
            """;

    /**
     * The same rows in the legacy layout of a dump predating {@code cl_type}, so that the
     * classification has to fall back to the registered ids.
     */
    private static final String CATEGORYLINKS_WITHOUT_TYPE = """
            CREATE TABLE `categorylinks` (
              `cl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `cl_to` varbinary(255) NOT NULL DEFAULT '',
              `cl_sortkey` varbinary(255) NOT NULL DEFAULT '',
              `cl_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
              PRIMARY KEY (`cl_from`,`cl_to`)
            ) ENGINE=InnoDB;
            INSERT INTO `categorylinks` VALUES \
            (11,'Top_Level','A','2009-01-01 00:00:00'),\
            (21,'Top_Level','B','2009-01-01 00:00:00'),\
            (31,'Top_Level','C','2009-01-01 00:00:00'),\
            (99,'Top_Level','D','2009-01-01 00:00:00'),\
            (22,'Top_Level','E','2009-01-01 00:00:00');
            """;

    private static final String PAGELINKS_LEGACY = """
            CREATE TABLE `pagelinks` (
              `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `pl_namespace` int(11) NOT NULL DEFAULT 0,
              `pl_to` varbinary(255) NOT NULL DEFAULT '',
              `pl_from_namespace` int(11) NOT NULL DEFAULT 0,
              UNIQUE KEY `pl_from` (`pl_from`,`pl_namespace`,`pl_to`)
            ) ENGINE=InnoDB;
            INSERT INTO `pagelinks` VALUES (11,0,'Main_Page',0),(41,0,'Main_Page',0),\
            (11,0,'Nirvana',0);
            """;

    private static InputStream stream(String sql)
    {
        return new ByteArrayInputStream(sql.getBytes(UTF_8));
    }

    /**
     * lt_id 700 is {@code Category:Top Level}, everything else is unknown.
     */
    private static LinkTargetResolver resolver()
    {
        return new LinkTargetResolver()
        {
            @Override
            public String getTitle(long ltId)
            {
                return ltId == 700 ? "Top_Level" : null;
            }

            @Override
            public int getNamespace(long ltId)
            {
                return ltId == 700 ? 14 : NAMESPACE_UNKNOWN;
            }

            @Override
            public long size()
            {
                return 1;
            }
        };
    }

    private static RecordingLinkRowSink sink()
    {
        return new RecordingLinkRowSink().withCategory("Top_Level", 500).withArticle("Main_Page", 1)
                .withArticle("Article", 11).withCategory("Subcategory", 21)
                .withCategory("Second_Subcategory", 22);
    }

    @Test
    void classifiesByClTypeWhenTheDumpCarriesIt() throws Exception
    {
        final RecordingLinkRowSink sink = sink();
        try (CategorylinksParser parser = new CategorylinksParser(stream(CATEGORYLINKS_WITH_TYPE),
                resolver())) {
            while (parser.next()) {
                LinkRowProcessor.processCategoryLink(parser, sink);
            }
        }
        // the 'page' row of the registered article 11
        assertEquals(of("500->11"), sink.memberships);
        // the 'subcat' row of the registered category 21. The stale 'page' row of category 22 is
        // NOT turned into a subcategory edge: cl_type says it is a page membership, and page 22 is
        // not a registered article.
        assertEquals(of("500->21"), sink.subcategories);
        // the 'file' row and the row of the unregistered page 99 contribute nothing
    }

    @Test
    void infersTheKindFromTheRegisteredIdsWhenClTypeIsAbsent() throws Exception
    {
        final RecordingLinkRowSink sink = sink();
        try (CategorylinksParser parser = new CategorylinksParser(
                stream(CATEGORYLINKS_WITHOUT_TYPE))) {
            while (parser.next()) {
                LinkRowProcessor.processCategoryLink(parser, sink);
            }
        }
        // page 11 is a registered article, so its row becomes a membership ...
        assertEquals(of("500->11"), sink.memberships);
        // ... while pages 21 and 22 are registered categories, so their rows become subcategories.
        // Note that the last row is the one the typed dump drops: without cl_type there is no way
        // to tell a stale page membership from a subcategory edge.
        assertEquals(of("500->21", "500->22"), sink.subcategories);
    }

    @Test
    void recordsADisambiguationWhenTheTargetIsTheDisambiguationCategory() throws Exception
    {
        final RecordingLinkRowSink sink = sink().withDisambiguationCategory("Top_Level");
        try (CategorylinksParser parser = new CategorylinksParser(stream(CATEGORYLINKS_WITH_TYPE),
                resolver())) {
            while (parser.next()) {
                LinkRowProcessor.processCategoryLink(parser, sink);
            }
        }
        assertEquals(of(11), sink.disambiguations);
    }

    @Test
    void keepsPageLinksOfRegisteredArticlesOnly() throws Exception
    {
        final RecordingLinkRowSink sink = sink();
        try (PagelinksParser parser = new PagelinksParser(stream(PAGELINKS_LEGACY))) {
            while (parser.next()) {
                LinkRowProcessor.processPageLink(parser, sink);
            }
        }
        // page 41 is not a registered article and 'Nirvana' is not a registered page
        assertEquals(of("11->1"), sink.pageLinks);
    }

    @Test
    void keepsPageLinksOfUnregisteredSourcesWhenSkipPageIsDisabled() throws Exception
    {
        final RecordingLinkRowSink sink = sink().withSkipPage(false);
        try (PagelinksParser parser = new PagelinksParser(stream(PAGELINKS_LEGACY))) {
            while (parser.next()) {
                LinkRowProcessor.processPageLink(parser, sink);
            }
        }
        assertEquals(of("11->1", "41->1"), sink.pageLinks);
        assertEquals(emptyList(), sink.memberships);
    }
}
