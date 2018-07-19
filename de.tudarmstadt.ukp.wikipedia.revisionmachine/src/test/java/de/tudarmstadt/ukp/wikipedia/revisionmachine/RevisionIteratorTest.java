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
package de.tudarmstadt.ukp.wikipedia.revisionmachine;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.*;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;

public class RevisionIteratorTest extends BaseJWPLTest {

    // Note: In the stripped HSQLDB data set only 382 revisions exist for the Page 'Car'
	private static final int GLOBAL_REVISION_COUNT = 382;

    private static Wikipedia wiki = null;
	private static RevisionAPIConfiguration config = null;

	// The object under test
	private RevisionIterator revisionIterator = null;

	/**
	 * Made this static so that following tests don't run if assumption fails.
	 * (With AT_Before, tests also would not be executed but marked as passed)
	 * This could be changed back as soon as JUnit ignored tests after failed
	 * assumptions
	 */
	@BeforeClass
	public static void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration(
				"wikiapi_simple_20090119_stripped", Language.simple_english);
		try {
			wiki = new Wikipedia(db);
			config = new RevisionAPIConfiguration(wiki.getDatabaseConfiguration());
		} catch (Exception e) {
			fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
		}
		assertNotNull(wiki);
		assertNotNull(config);

	}

	@Before
	public void setupInstanceUnderTest()
	{
		try {
			revisionIterator = new RevisionIterator(config);
			assertNotNull(revisionIterator);
		} catch (WikiApiException e) {
			fail("RevisionIterator could not be initialized: " + e.getLocalizedMessage());
		}
	}

	@After
	public void cleanUpInstanceUnderTest()
	{
		if(revisionIterator!=null) {
			try {
				revisionIterator.close();
			} catch (SQLException e) {
				fail("RevisionIterator could not be shut down correctly: " + e.getLocalizedMessage());
			}
		}
	}

	@Test
	public void iteratorTest() {

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
	public void lazyLoadingTest() {
		ArrayList<String> texts = new ArrayList<String>();
		int i = 0;

		while (revisionIterator.hasNext() && i < 500) {
			Revision revision = revisionIterator.next();
            assertNotNull(revision);
			texts.add(revision.getRevisionText());
			i++;
		}
        assertEquals(GLOBAL_REVISION_COUNT, i);

		ArrayList<String> lazyLoadedTexts = new ArrayList<String>();
		i = 0;

		//create new iterator with lazy loading
		try{
			revisionIterator = new RevisionIterator(config, true);
		}catch (WikiApiException e) {
            fail("RevisionIterator could not be initialized with lazy loading = 'true': " + e.getLocalizedMessage());
		}

		while (revisionIterator.hasNext() && i < 1000) {
			Revision revision = revisionIterator.next();
			lazyLoadedTexts.add(revision.getRevisionText());
			i++;
		}
        assertEquals(GLOBAL_REVISION_COUNT, i);

		for (int j = 0; j < texts.size(); j++) {
			if(!texts.get(j).equals(lazyLoadedTexts.get(j))){
				assertFalse(true);
			}
		}
		//close iterator
		try {
			revisionIterator.close();
		}
		catch (SQLException e) {
            fail("RevisionIterator could not be shut down correctly: " + e.getLocalizedMessage());
		}

	}

}
