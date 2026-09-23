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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.revisionmachine.BaseJWPLTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies the single-query lookup of article ID and timestamp used by the
 * {@code *BeforeRevision} methods of {@link RevisionApi}, and the connection setup of
 * {@link AbstractRevisionService}.
 */
public class RevisionApiArticleIdAndTimestampTest
    extends BaseJWPLTest
{

    private static final String DB_NAME = "wikiapi_simple_20090119_stripped";

    private DatabaseConfiguration dbConfig;

    private RevisionApi revisionApi;

    @BeforeEach
    public void setUp() throws WikiApiException
    {
        dbConfig = obtainHSDLDBConfiguration(DB_NAME, Language.simple_english);
        revisionApi = new RevisionApi(dbConfig);
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        if (revisionApi != null) {
            revisionApi.close();
        }
    }

    @Test
    public void testMatchesPreviousTwoStepLookupForAllRevisions() throws Exception
    {
        List<Integer> revisionIds = new ArrayList<>();
        try (PreparedStatement statement = revisionApi.connection
                .prepareStatement("SELECT RevisionID FROM index_revisionID")) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                revisionIds.add(result.getInt(1));
            }
        }
        // first, a middle and the last revision, the latter far from the full revision checkpoint
        assertTrue(revisionIds.contains(9149));
        assertTrue(revisionIds.contains(1142935));
        assertTrue(revisionIds.contains(1307720));
        assertEquals(382, revisionIds.size());

        for (int revisionId : revisionIds) {
            long[] expected = { revisionApi.getPageIdForRevisionId(revisionId),
                    revisionApi.getRevision(revisionId).getTimeStamp().getTime() };
            assertArrayEquals(expected, revisionApi.getArticleIdAndTimestamp(revisionId),
                    "Mismatch for revision " + revisionId);
        }
    }

    @Test
    public void testUnknownRevisionThrowsWikiPageNotFoundException()
    {
        assertThrows(WikiPageNotFoundException.class,
                () -> revisionApi.getArticleIdAndTimestamp(1));
    }

    @Test
    public void testInvalidRevisionThrowsWikiApiException()
    {
        WikiApiException e = assertThrows(WikiApiException.class,
                () -> revisionApi.getArticleIdAndTimestamp(0));
        assertFalse(e instanceof WikiPageNotFoundException);
        assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void testOpenConnectionUsesConfiguredJdbcURL() throws Exception
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration(dbConfig);
        config.setHost("unknown-host");
        config.setDatabaseDriver(null);
        try (Connection c = AbstractRevisionService.openConnection(config)) {
            assertTrue(c.isValid(5));
        }
    }

    @Test
    public void testResolveJdbcURLFallsBackToHostAndDatabase()
    {
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("db.example.org");
        config.setDatabase("revisions");
        assertEquals("jdbc:mysql://db.example.org/revisions",
                AbstractRevisionService.resolveJdbcURL(config));

        config.setJdbcURL(" ");
        assertEquals("jdbc:mysql://db.example.org/revisions",
                AbstractRevisionService.resolveJdbcURL(config));

        config.setJdbcURL("jdbc:mariadb://db.example.org/revisions?useCompression=true");
        assertEquals("jdbc:mariadb://db.example.org/revisions?useCompression=true",
                AbstractRevisionService.resolveJdbcURL(config));
    }
}
