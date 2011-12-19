/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Project Website:
 * 	http://jwpl.googlecode.com
 *
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Calendar;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

public class RevisionApiTest
{

	private static Wikipedia wiki = null;

	/**
	 * Made this static so that following tests don't run if assumption fails.
	 * (With AT_Before, tests also would not be executed but marked as passed)
	 * This could be changed back as soon as JUnit ignores tests after failed
	 * assumptions
	 */
	@BeforeClass
	public static void setupWikipedia()
	{
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_simple_20090119");
		db.setHost("bender.ukp.informatik.tu-darmstadt.de");
		db.setUser("student");
		db.setPassword("student");
		db.setLanguage(Language.simple_english);
		try {
			wiki = new Wikipedia(db);
		}
		catch (Exception e) {
			assumeNoException(e);
		}
		Assume.assumeNotNull(wiki);
	}

	@Test
	public void getRevisionByTimestampTest()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 10, 10, 10, 10, 10);

		String pageName = "Car";
		try {
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage(pageName).getPageId();

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
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

			Revision revision1 = revisionApi.getRevision(1142935);
			Revision revision2 = revisionApi.getRevision(pageId, timestamp);

			assertEquals(1142935, revision1.getRevisionID());
			assertEquals(0, revision1.getFullRevisionID());
			assertEquals(349, revision1.getRevisionCounter());

			assertEquals(revision1.getRevisionID(), revision2.getRevisionID());
			assertEquals(revision1.getFullRevisionID(),
					revision2.getFullRevisionID());
			assertEquals(revision1.getRevisionCounter(),
					revision2.getRevisionCounter());
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
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

			Revision revision1 = revisionApi.getRevision(pageId, 349);
			Revision revision2 = revisionApi.getRevision(pageId, timestamp);

			assertEquals(1142935, revision1.getRevisionID());
			assertEquals(0, revision1.getFullRevisionID());
			assertEquals(349, revision1.getRevisionCounter());

			assertEquals(revision1.getRevisionID(), revision2.getRevisionID());
			assertEquals(revision1.getFullRevisionID(),
					revision2.getFullRevisionID());
			assertEquals(revision1.getRevisionCounter(),
					revision2.getRevisionCounter());
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
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp firstDayOfAppearance = revisionApi
					.getFirstDateOfAppearance(pageId);
			Timestamp lastDayOfAppearance = revisionApi
					.getLastDateOfAppearance(pageId);
			int nrOfRevisions = revisionApi.getNumberOfRevisions(pageId);

			assertEquals("2004-04-07 02:31:34.0",
					firstDayOfAppearance.toString());
			assertEquals("2009-01-19 04:58:09.0",
					lastDayOfAppearance.toString());
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
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage(pageName).getPageId();

			Timestamp lastRevisionTimestamp = revisionApi
					.getLastDateOfAppearance(pageId);
			Revision revision = revisionApi.getRevision(pageId,
					lastRevisionTimestamp);
			assertEquals(wiki.getPage(pageId).getText(),
					revision.getRevisionText());

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
			RevisionApi revisionApi = new RevisionApi(
					wiki.getDatabaseConfiguration());
			int pageId = wiki.getPage("Car").getPageId();

			Timestamp lastRevisionTimestamp = revisionApi
					.getLastDateOfAppearance(pageId);
			Revision revision = revisionApi.getRevision(pageId,
					lastRevisionTimestamp);

			Field privateStringField = Revision.class
					.getDeclaredField("revisionText");

			privateStringField.setAccessible(true);

			String fieldValue = (String) privateStringField.get(revision);
			if (fieldValue != null) {
				fail("Not lazy loaded!");
			}

			revision.getRevisionText();
			fieldValue = (String) privateStringField.get(revision);
			if (fieldValue == null) {
				fail("Not lazy loaded!");
			}

		}
		catch (WikiApiException e) {
			e.printStackTrace();
			fail(e.getMessage());

		}
		catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
