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
package org.dkpro.jwpl.revisionmachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RevisionIteratorTest
    extends BaseJWPLTest
{

    // Note: In the stripped HSQLDB data set only 382 revisions exist for the Page 'Car'
    private static final int GLOBAL_REVISION_COUNT = 382;

    private static Wikipedia wiki = null;
    private static RevisionAPIConfiguration config = null;

    // The object under test
    private RevisionIterator revisionIterator = null;

    /**
     * Made this static so that following tests don't run if assumption fails. (With AT_Before,
     * tests also would not be executed but marked as passed) This could be changed back as soon as
     * JUnit ignored tests after failed assumptions
     */
    @BeforeAll
    public static void setupWikipedia()
    {
        DatabaseConfiguration db = obtainHSDLDBConfiguration("wikiapi_simple_20090119_stripped",
                Language.simple_english);
        try {
            wiki = new Wikipedia(db);
            config = new RevisionAPIConfiguration(wiki.getDatabaseConfiguration());
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }
        assertNotNull(wiki);
        assertNotNull(config);

    }

    @BeforeEach
    public void setupInstanceUnderTest()
    {
        try {
            revisionIterator = new RevisionIterator(config);
            assertNotNull(revisionIterator);
        }
        catch (WikiApiException e) {
            fail("RevisionIterator could not be initialized: " + e.getLocalizedMessage());
        }
    }

    @AfterEach
    public void cleanUpInstanceUnderTest()
    {
        if (revisionIterator != null) {
            try {
                revisionIterator.close();
            }
            catch (SQLException e) {
                fail("RevisionIterator could not be shut down correctly: "
                        + e.getLocalizedMessage());
            }
        }
    }

    @Test
    public void iteratorTest()
    {

        int i = 0;

        while (revisionIterator.hasNext() && i < 500) {
            Revision revision = revisionIterator.next();
            assertNotNull(revision);
            assertTrue(revision.getArticleID() > 0);
            assertTrue(revision.getFullRevisionID() > 0);
            assertTrue(revision.getRevisionCounter() > 0);
            assertNotNull(revision.getRevisionText());
            assertNotNull(revision.getTimeStamp());
            i++;
        }

        assertEquals(GLOBAL_REVISION_COUNT, i);
    }

    @Test
    public void pagedIteratorTest() throws WikiApiException, SQLException
    {
        RevisionAPIConfiguration pagedConfig = new RevisionAPIConfiguration(
                wiki.getDatabaseConfiguration());
        pagedConfig.setBufferSize(50);

        int i = 0;
        int previousPrimaryKey = 0;
        try (RevisionIterator pagedIterator = new RevisionIterator(pagedConfig)) {
            while (pagedIterator.hasNext() && i < 500) {
                Revision revision = pagedIterator.next();
                assertNotNull(revision);
                assertTrue(revision.getPrimaryKey() > previousPrimaryKey);
                previousPrimaryKey = revision.getPrimaryKey();
                i++;
            }
        }

        assertEquals(GLOBAL_REVISION_COUNT, i);
    }

    @Test
    public void lazyLoadingTest()
    {
        ArrayList<String> texts = new ArrayList<>();
        int i = 0;

        while (revisionIterator.hasNext() && i < 500) {
            Revision revision = revisionIterator.next();
            assertNotNull(revision);
            texts.add(revision.getRevisionText());
            i++;
        }
        assertEquals(GLOBAL_REVISION_COUNT, i);

        ArrayList<String> lazyLoadedTexts = new ArrayList<>();
        i = 0;

        // create new iterator with lazy loading
        try {
            revisionIterator = new RevisionIterator(config, true);
        }
        catch (WikiApiException e) {
            fail("RevisionIterator could not be initialized with lazy loading = 'true': "
                    + e.getLocalizedMessage());
        }

        while (revisionIterator.hasNext() && i < 1000) {
            Revision revision = revisionIterator.next();
            lazyLoadedTexts.add(revision.getRevisionText());
            i++;
        }
        assertEquals(GLOBAL_REVISION_COUNT, i);

        for (int j = 0; j < texts.size(); j++) {
            if (!texts.get(j).equals(lazyLoadedTexts.get(j))) {
                fail();
            }
        }
        // close iterator
        try {
            revisionIterator.close();
        }
        catch (SQLException e) {
            fail("RevisionIterator could not be shut down correctly: " + e.getLocalizedMessage());
        }

    }


    @Test
    public void startInsideDiffChainTest() throws WikiApiException, SQLException
    {
        Map<Integer, Integer> revisionIDs = readRevisionIDsByPrimaryKey();
        List<Integer> primaryKeys = new ArrayList<>(revisionIDs.keySet());
        int endPK = primaryKeys.get(primaryKeys.size() - 1);

        // Start positions in the middle of the diff chain of 'Car'
        for (int startIndex : new int[] { 1, 12, 200, 329 }) {
            int startPK = primaryKeys.get(startIndex);
            assertStartsInsideDiffChain(revisionIDs.get(startPK));

            try (RevisionIterator iterator = new RevisionIterator(config, startPK, endPK)) {
                assertTextsOfRemainingRevisions(iterator, revisionIDs, startIndex, -1);
            }
        }
    }

    @Test
    public void switchFromLazyToEagerTest() throws WikiApiException, SQLException
    {
        Map<Integer, Integer> revisionIDs = readRevisionIDsByPrimaryKey();

        for (int switchIndex : new int[] { 1, 10, 100 }) {
            try (RevisionIterator iterator = new RevisionIterator(config, true)) {
                assertTextsOfRemainingRevisions(iterator, revisionIDs, 0, switchIndex);
            }
        }
    }

    @Test
    public void switchFromEagerToLazyAndBackTest() throws WikiApiException, SQLException
    {
        Map<Integer, Integer> revisionIDs = readRevisionIDsByPrimaryKey();

        try (RevisionIterator iterator = new RevisionIterator(config);
                RevisionApi revisionApi = new RevisionApi(config)) {
            int i = 0;
            while (iterator.hasNext()) {
                if (i == 20) {
                    iterator.setShouldLoadRevisionText(true);
                }
                else if (i == 40) {
                    iterator.setShouldLoadRevisionText(false);
                }
                Revision revision = iterator.next();
                assertNotNull(revision);
                assertEquals(revisionApi.getRevision(revision.getRevisionID()).getRevisionText(),
                        revision.getRevisionText(), "Text of revision " + revision.getRevisionID());
                i++;
            }
            assertEquals(revisionIDs.size(), i);
        }
    }

    /**
     * @return the revision IDs of all revisions, ordered by their primary key
     */
    private static Map<Integer, Integer> readRevisionIDsByPrimaryKey()
        throws WikiApiException, SQLException
    {
        Map<Integer, Integer> revisionIDs = new LinkedHashMap<>();
        try (RevisionIterator iterator = new RevisionIterator(config, true)) {
            while (iterator.hasNext()) {
                Revision revision = iterator.next();
                revisionIDs.put(revision.getPrimaryKey(), revision.getRevisionID());
            }
        }
        assertEquals(GLOBAL_REVISION_COUNT, revisionIDs.size());
        return revisionIDs;
    }

    private static void assertStartsInsideDiffChain(int revisionID)
        throws WikiApiException, SQLException
    {
        try (RevisionApi revisionApi = new RevisionApi(config)) {
            Revision revision = revisionApi.getRevision(revisionID);
            revision.getRevisionText();
            Collection<DiffPart> parts = revision.getParts();
            assertFalse(parts.size() == 1 && parts.iterator().next()
                    .getAction() == DiffAction.FULL_REVISION_UNCOMPRESSED,
                    "Revision " + revisionID + " is expected to be a diff");
        }
    }

    /**
     * Iterates the remaining revisions and compares their texts with the ones reconstructed by
     * the {@link RevisionApi}.
     *
     * @param iterator
     *            the iterator under test
     * @param revisionIDs
     *            the revision IDs of all revisions, ordered by their primary key
     * @param startIndex
     *            index of the first revision returned by the iterator
     * @param switchIndex
     *            number of revisions after which the iterator switches to eager mode, {@code -1}
     *            to keep the mode
     */
    private static void assertTextsOfRemainingRevisions(RevisionIterator iterator,
            Map<Integer, Integer> revisionIDs, int startIndex, int switchIndex)
        throws WikiApiException, SQLException
    {
        List<Integer> expectedIDs = new ArrayList<>(revisionIDs.values())
                .subList(startIndex, revisionIDs.size());
        try (RevisionApi revisionApi = new RevisionApi(config)) {
            int i = 0;
            while (iterator.hasNext()) {
                if (i == switchIndex) {
                    iterator.setShouldLoadRevisionText(false);
                }
                Revision revision = iterator.next();
                assertNotNull(revision, "Revision " + expectedIDs.get(i) + " is missing");
                assertEquals(expectedIDs.get(i), revision.getRevisionID());
                assertEquals(revisionApi.getRevision(revision.getRevisionID()).getRevisionText(),
                        revision.getRevisionText(), "Text of revision " + revision.getRevisionID());
                i++;
            }
            assertEquals(expectedIDs.size(), i);
        }
    }

}
