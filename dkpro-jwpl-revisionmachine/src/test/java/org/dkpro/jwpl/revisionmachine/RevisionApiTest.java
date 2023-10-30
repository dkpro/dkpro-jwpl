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

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RevisionApiTest extends BaseJWPLTest {

	private static Wikipedia wiki = null;

	private Timestamp convertToUTC(Timestamp ts) {

		final LocalDateTime dt = LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
		return Timestamp.valueOf(dt);
	}

	// The object under test
	private RevisionApi revisionApi;

	/**
	 * Made this static so that following tests don't run if assumption fails.
	 * (With AT_Before, tests also would not be executed but marked as passed)
	 * This could be changed back as soon as JUnit ignored tests after failed
	 * assumptions
	 */
	@BeforeAll
	public static void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration(
				"wikiapi_simple_20090119_stripped", Language.simple_english);
		try {
			wiki = new Wikipedia(db);
		} catch (Exception e) {
			fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
		}
		assertNotNull(wiki);
	}

	@BeforeEach
	public void setupInstanceUnderTest() {
		try {
			revisionApi = new RevisionApi(wiki.getDatabaseConfiguration());
			assertNotNull(revisionApi);
		} catch (WikiApiException e) {
			fail("RevisionApi could not be initialized: " + e.getLocalizedMessage());
		}
	}

	@AfterEach
	public void cleanUpInstanceUnderTest() {
		if(revisionApi!=null) {
			try {
				revisionApi.close();
			} catch (SQLException e) {
				fail("RevisionApi could not be shut down correctly: " + e.getLocalizedMessage());
			}
		}
	}

	@Test
	public void getRevisionByTimestampTest() {
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
			fail(e.getMessage(), e);
		}
	}

	@Test
	public void getRevisionByRevisionId() {
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
			fail(e.getMessage(), e);
		}
	}

	@Test
	public void getRevisionByRevisionCounter() {
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
			fail(e.getMessage(), e);
		}
	}

	@Test
	public void articleIDTests() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		try {
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp firstDayOfAppearance = convertToUTC(revisionApi.getFirstDateOfAppearance(pageId));
			Timestamp lastDayOfAppearance = convertToUTC(revisionApi.getLastDateOfAppearance(pageId));
			int nrOfRevisions = revisionApi.getNumberOfRevisions(pageId);

			assertEquals("2004-04-07 00:31:34.0", firstDayOfAppearance.toString());
			assertEquals("2009-01-19 03:58:09.0", lastDayOfAppearance.toString());
			assertEquals(382, nrOfRevisions);
		}
		catch (WikiApiException e) {
			fail(e.getMessage(), e);
		}
	}

	@Test
	@Disabled
	public void lastRevisionTest() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		String pageName = "Car";
		try {
			int pageId = wiki.getPage(pageName).getPageId();

			Timestamp lastRevisionTimestamp = revisionApi.getLastDateOfAppearance(pageId);
			Revision revision = revisionApi.getRevision(pageId, lastRevisionTimestamp);
			// FIXME the comparison shall hold - Check: flattened in one line vs. multiple lines
			assertEquals(wiki.getPage(pageId).getText(), revision.getRevisionText());
		}
		catch (WikiApiException e) {
			fail(e.getMessage(), e);
		}
	}


	@Test
	public void lazyLoadingTest() {
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
			fail(e.getMessage(), e);
		}
	}
}
