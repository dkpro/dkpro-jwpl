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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.junit.*;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

public class RevisionApiTest extends BaseJWPLTest {

	private static Wikipedia wiki = null;

	// The object under test
	private RevisionApi revisionApi;

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
		} catch (Exception e) {
			fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
		}
		Assert.assertNotNull(wiki);
	}

	@Before
	public void setupInstanceUnderTest()
	{
		try {
			revisionApi = new RevisionApi(wiki.getDatabaseConfiguration());
			assertNotNull(revisionApi);
		} catch (WikiApiException e) {
			fail("RevisionApi could not be initialized: " + e.getLocalizedMessage());
		}
	}

	@After
	public void cleanUpInstanceUnderTest()
	{
		if(revisionApi!=null) {
			try {
				revisionApi.close();
			} catch (SQLException e) {
				fail("RevisionApi could not be shut down correctly: " + e.getLocalizedMessage());
			}
		}
	}

	@Test
	public void getRevisionByTimestampTest()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {

			int pageId = wiki.getPage("Car").getPageId();

			Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
			Revision revision = revisionApi.getRevision(pageId, timestamp);

			assertEquals(1142935, revision.getRevisionID());
			assertEquals(0, revision.getFullRevisionID());
			assertEquals(349, revision.getRevisionCounter());
			assertEquals(pageId, revision.getArticleID());
		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void getRevisionByRevisionId()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

			Revision revision1 = revisionApi.getRevision(1142935);
			Revision revision2 = revisionApi.getRevision(pageId, timestamp);

			assertEquals(1142935, revision1.getRevisionID());
			assertEquals(0, revision1.getFullRevisionID());
			assertEquals(349, revision1.getRevisionCounter());

			assertEquals(revision1.getRevisionID(), revision2.getRevisionID());
			assertEquals(revision1.getFullRevisionID(), revision2.getFullRevisionID());
			assertEquals(revision1.getRevisionCounter(), revision2.getRevisionCounter());
			assertEquals(revision1.getArticleID(), revision2.getArticleID());

		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void getRevisionByRevisionCounter()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

			Revision revision1 = revisionApi.getRevision(pageId, 349);
			Revision revision2 = revisionApi.getRevision(pageId, timestamp);

			assertEquals(1142935, revision1.getRevisionID());
			assertEquals(0, revision1.getFullRevisionID());
			assertEquals(349, revision1.getRevisionCounter());

			assertEquals(revision1.getRevisionID(), revision2.getRevisionID());
			assertEquals(revision1.getFullRevisionID(), revision2.getFullRevisionID());
			assertEquals(revision1.getRevisionCounter(), revision2.getRevisionCounter());
			assertEquals(revision1.getArticleID(), revision2.getArticleID());

		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void articleIDTests()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp firstDayOfAppearance = revisionApi.getFirstDateOfAppearance(pageId);
			Timestamp lastDayOfAppearance = revisionApi.getLastDateOfAppearance(pageId);
			int nrOfRevisions = revisionApi.getNumberOfRevisions(pageId);

			assertEquals("2004-04-07 02:31:34.0", firstDayOfAppearance.toString());
			assertEquals("2009-01-19 04:58:09.0", lastDayOfAppearance.toString());
			assertEquals(382, nrOfRevisions);
		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void lastRevisionTest()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		String pageName = "Car";
		try {
			int pageId = wiki.getPage(pageName).getPageId();

			Timestamp lastRevisionTimestamp = revisionApi.getLastDateOfAppearance(pageId);
			Revision revision = revisionApi.getRevision(pageId, lastRevisionTimestamp);
			assertEquals(wiki.getPage(pageId).getText(), revision.getRevisionText());
		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


	@Test
	public void lazyLoadingTest()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp lastRevisionTimestamp = revisionApi.getLastDateOfAppearance(pageId);
			Revision revision = revisionApi.getRevision(pageId, lastRevisionTimestamp);

			Field privateStringField = Revision.class.getDeclaredField("revisionText");
			privateStringField.setAccessible(true);

			String fieldValue = (String) privateStringField.get(revision);
			if (fieldValue != null) {
				fail("Not lazy loaded!");
			}

			// trigger the load of the data
			revision.getRevisionText();

			fieldValue = (String) privateStringField.get(revision);
			if (fieldValue == null) {
				fail("Not lazy loaded!");
			}

		} catch (WikiApiException | SecurityException | NoSuchFieldException |
				 IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
