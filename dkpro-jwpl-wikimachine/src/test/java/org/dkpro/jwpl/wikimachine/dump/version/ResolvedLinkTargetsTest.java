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
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.FastUtilLinkTargetResolver;
import org.dkpro.jwpl.wikimachine.dump.sql.LinkTargetResolver;
import org.dkpro.jwpl.wikimachine.dump.sql.LinktargetParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.ResolvedLinkTargets;
import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests that resolving {@code lt_id} values to page ids once through a {@link ResolvedLinkTargets}
 * produces exactly the rows the title based {@link FastUtilLinkTargetResolver} path produces
 * (see issue #553).
 */
class ResolvedLinkTargetsTest
{

    /**
     * Two registered categories (one of them the disambiguation category), a category without a
     * category page, the talk and the project page of {@code Main Page}, the article itself, a red
     * link, titles carrying a quote and a backslash, and a template.
     */
    private static final String LINKTARGET = """
            CREATE TABLE `linktarget` (
              `lt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
              `lt_namespace` int(11) NOT NULL,
              `lt_title` varbinary(255) NOT NULL,
              PRIMARY KEY (`lt_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=binary;
            INSERT INTO `linktarget` VALUES \
            (700,14,'Top_Level'),(701,14,'Disambiguation'),(702,14,'Red_Category'),\
            (703,1,'Main_Page'),(704,4,'Main_Page'),(800,0,'Main_Page'),(801,0,'Red_Link'),\
            (802,0,'O\\'Brien'),(803,0,'Back\\\\slash'),(804,10,'Main_Page'),\
            (805,0,'Top_Level');
            """;

    private static final String CATEGORYLINKS = """
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
            (12,'B','2025-01-01 00:00:00','','page',1,701),\
            (11,'C','2025-01-01 00:00:00','','page',1,701),\
            (21,'D','2025-01-01 00:00:00','','subcat',1,700),\
            (11,'E','2025-01-01 00:00:00','','page',1,702),\
            (13,'F','2025-01-01 00:00:00','','page',1,700),\
            (31,'G','2025-01-01 00:00:00','','file',1,700),\
            (11,'H','2025-01-01 00:00:00','','page',1,9999);
            """;

    private static final String PAGELINKS = """
            CREATE TABLE `pagelinks` (
              `pl_from` int(8) unsigned NOT NULL DEFAULT 0,
              `pl_from_namespace` int(11) NOT NULL DEFAULT 0,
              `pl_target_id` bigint(20) unsigned NOT NULL,
              PRIMARY KEY (`pl_from`,`pl_target_id`)
            ) ENGINE=InnoDB;
            INSERT INTO `pagelinks` VALUES (11,0,800),(11,0,801),(11,0,802),(11,0,803),\
            (11,0,703),(11,0,704),(11,0,804),(11,0,700),(12,0,800),(41,0,800),(11,0,9999);
            """;

    private static InputStream stream(String sql)
    {
        return new ByteArrayInputStream(sql.getBytes(UTF_8));
    }

    private static RecordingLinkRowSink sink(boolean skipPage)
    {
        return new RecordingLinkRowSink().withCategory("Top_Level", 500)
                .withCategory("Disambiguation", 501).withCategory("Subcategory", 21)
                .withArticle("Main_Page", 1).withArticle(SQLEscape.escape("O'Brien"), 2)
                // page titles are escaped twice, link titles once: never matches (see #553)
                .withArticle(SQLEscape.escape(SQLEscape.escape("Back\\slash")), 3)
                .withArticle("Article", 11).withArticle("Disambiguation_Page", 12)
                .withDisambiguationCategory("Disambiguation").withSkipPage(skipPage);
    }

    private static ResolvedLinkTargets resolved(LinkRowSink sink) throws IOException
    {
        return ResolvedLinkTargets.load(new LinktargetParser(stream(LINKTARGET)), sink);
    }

    private static void process(LinkTargetResolver resolver, LinkRowSink sink) throws IOException
    {
        try (CategorylinksParser parser = new CategorylinksParser(stream(CATEGORYLINKS),
                resolver)) {
            while (parser.next()) {
                LinkRowProcessor.processCategoryLink(parser, sink);
            }
            parser.checkPostConditions();
        }
        try (PagelinksParser parser = new PagelinksParser(stream(PAGELINKS), resolver)) {
            while (parser.next()) {
                LinkRowProcessor.processPageLink(parser, sink);
            }
            parser.checkPostConditions();
        }
    }

    private static List<List<?>> output(RecordingLinkRowSink sink)
    {
        return of(sink.memberships, sink.subcategories, sink.pageLinks, sink.disambiguations);
    }

    @Test
    void loadKeepsResolvableArticlesAndCategoriesOnly() throws Exception
    {
        final ResolvedLinkTargets targets = resolved(sink(true));

        assertEquals(500, targets.getCategoryId(700));
        assertEquals(501, targets.getCategoryId(701));
        assertEquals(1, targets.getPageId(800));
        assertEquals(2, targets.getPageId(802));
        assertEquals(14, targets.getNamespace(700));
        assertEquals(0, targets.getNamespace(800));
        assertEquals(4, targets.size());

        // a category and an article without a registered page are dropped ...
        assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getCategoryId(702));
        assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getPageId(801));
        // ... and so are the backslash title, the talk page, the project page and the template
        assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getPageId(803));
        for (long ltId : new long[] { 703, 704, 804 }) {
            assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getPageId(ltId));
            assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getCategoryId(ltId));
            assertEquals(LinkTargetResolver.NAMESPACE_UNKNOWN, targets.getNamespace(ltId));
        }
        // an article target is never a category target and vice versa
        assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getCategoryId(805));
        assertEquals(ResolvedLinkTargets.UNRESOLVED, targets.getPageId(700));

        assertTrue(targets.isDisambiguationTarget(701));
        assertFalse(targets.isDisambiguationTarget(700));
        assertNull(targets.getTitle(800));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void producesTheSameRowsAsTheTitleBasedPath(boolean skipPage) throws Exception
    {
        final RecordingLinkRowSink legacyTalk = sink(skipPage);
        process(FastUtilLinkTargetResolver.load(new LinktargetParser(stream(LINKTARGET)),
                FastUtilLinkTargetResolver.ARTICLE_TALK_AND_CATEGORY), legacyTalk);

        final RecordingLinkRowSink legacy = sink(skipPage);
        process(FastUtilLinkTargetResolver.load(new LinktargetParser(stream(LINKTARGET)),
                FastUtilLinkTargetResolver.ARTICLE_AND_CATEGORY), legacy);

        final RecordingLinkRowSink sink = sink(skipPage);
        process(resolved(sink), sink);

        assertEquals(output(legacyTalk), output(legacy));
        assertEquals(output(legacy), output(sink));
    }

    @Test
    void writesTheExpectedRows() throws Exception
    {
        final RecordingLinkRowSink sink = sink(true);
        process(resolved(sink), sink);

        assertEquals(of("500->11", "501->12", "501->11"), sink.memberships);
        assertEquals(of("500->21"), sink.subcategories);
        assertEquals(of("11->1", "11->2", "12->1"), sink.pageLinks);
        assertEquals(of(12, 11), sink.disambiguations);
    }

    @Test
    void countsRowsWithoutARegisteredTargetAsUnresolved() throws Exception
    {
        final RecordingLinkRowSink sink = sink(true);
        final ResolvedLinkTargets targets = resolved(sink);
        try (CategorylinksParser parser = new CategorylinksParser(stream(CATEGORYLINKS),
                targets)) {
            assertTrue(parser.hasResolvedTargetIds());
            while (parser.next()) {
                assertNull(parser.getClTo());
            }
            // the red category and the unknown id
            assertEquals(6, parser.getResolvedCount());
            assertEquals(2, parser.getUnresolvedCount());
        }
        try (PagelinksParser parser = new PagelinksParser(stream(PAGELINKS), targets)) {
            assertTrue(parser.hasResolvedTargetIds());
            while (parser.next()) {
                assertNull(parser.getPlTo());
                assertEquals(0, parser.getPlNamespace());
            }
            // Main_Page (three times) and O'Brien
            assertEquals(4, parser.getResolvedCount());
            assertEquals(7, parser.getUnresolvedCount());
        }
    }
}
