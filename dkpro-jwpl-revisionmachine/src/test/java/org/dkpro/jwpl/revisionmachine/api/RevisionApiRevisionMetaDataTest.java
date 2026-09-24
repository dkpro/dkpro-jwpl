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
package org.dkpro.jwpl.revisionmachine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.revisionmachine.BaseJWPLTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link RevisionApi#getRevisionMetaData(int)} against the revisions returned by the
 * single revision lookups of {@link RevisionApi}.
 */
public class RevisionApiRevisionMetaDataTest
    extends BaseJWPLTest
{

    private static final String DB_NAME = "wikiapi_simple_20090119_stripped";

    private RevisionApi revisionApi;

    @BeforeEach
    public void setUp() throws WikiApiException
    {
        revisionApi = new RevisionApi(obtainHSDLDBConfiguration(DB_NAME, Language.simple_english));
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        if (revisionApi != null) {
            revisionApi.close();
        }
    }

    private List<Integer> queryInts(String sql, int param) throws Exception
    {
        List<Integer> values = new ArrayList<>();
        try (PreparedStatement statement = revisionApi.connection.prepareStatement(sql)) {
            if (param > 0) {
                statement.setInt(1, param);
            }
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                values.add(result.getInt(1));
            }
        }
        return values;
    }

    @Test
    public void testMatchesSingleRevisionLookups() throws Exception
    {
        List<Integer> articleIds = queryInts("SELECT ArticleID FROM index_articleID_rc_ts", 0);
        assertFalse(articleIds.isEmpty());

        int revisionCount = 0;
        int articlesWithUniqueTimestamps = 0;
        for (int articleId : articleIds) {
            List<Revision> revisions = revisionApi.getRevisionMetaData(articleId);
            List<Integer> expectedIds = queryInts(
                    "SELECT RevisionID FROM revisions WHERE ArticleID=?", articleId);
            assertEquals(expectedIds.size(), revisions.size());
            assertEquals(new HashSet<>(expectedIds), idsOf(revisions));
            revisionCount += revisions.size();

            Set<Timestamp> timestamps = new HashSet<>();
            Revision previous = null;
            for (Revision revision : revisions) {
                Revision expected = revisionApi.getRevision(revision.getRevisionID());
                assertSameMetaData(expected, revision);
                if (previous != null) {
                    assertFalse(revision.getTimeStamp().before(previous.getTimeStamp()));
                }
                timestamps.add(revision.getTimeStamp());
                previous = revision;
            }

            // the timestamp lookup is only unambiguous without duplicate timestamps
            if (timestamps.size() == revisions.size()) {
                articlesWithUniqueTimestamps++;
                for (Revision revision : revisions) {
                    assertEquals(revision.getRevisionID(), revisionApi
                            .getRevision(articleId, revision.getTimeStamp()).getRevisionID());
                }
            }
        }
        assertEquals(382, revisionCount);
        assertTrue(articlesWithUniqueTimestamps > 0);
    }

    @Test
    public void testTextIsLoadedLazily() throws Exception
    {
        int articleId = revisionApi.getPageIdForRevisionId(1307720);
        for (Revision revision : revisionApi.getRevisionMetaData(articleId)) {
            assertEquals(revisionApi.getRevision(revision.getRevisionID()).getRevisionText(),
                    revision.getRevisionText());
        }
    }

    @Test
    public void testUnknownArticleThrowsWikiPageNotFoundException()
    {
        assertThrows(WikiPageNotFoundException.class,
                () -> revisionApi.getRevisionMetaData(Integer.MAX_VALUE));
    }

    private static Set<Integer> idsOf(List<Revision> revisions)
    {
        Set<Integer> ids = new HashSet<>();
        for (Revision revision : revisions) {
            ids.add(revision.getRevisionID());
        }
        return ids;
    }

    private static void assertSameMetaData(Revision expected, Revision actual)
    {
        assertEquals(expected.getRevisionID(), actual.getRevisionID());
        assertEquals(expected.getPrimaryKey(), actual.getPrimaryKey());
        assertEquals(expected.getRevisionCounter(), actual.getRevisionCounter());
        assertEquals(expected.getArticleID(), actual.getArticleID());
        assertEquals(expected.getTimeStamp(), actual.getTimeStamp());
        assertEquals(expected.getComment(), actual.getComment());
        assertEquals(expected.isMinor(), actual.isMinor());
        assertEquals(expected.getContributorName(), actual.getContributorName());
        assertEquals(expected.getContributorId(), actual.getContributorId());
        assertEquals(expected.contributorIsRegistered(), actual.contributorIsRegistered());
        assertEquals(expected.getNamespace(), actual.getNamespace());
    }
}
