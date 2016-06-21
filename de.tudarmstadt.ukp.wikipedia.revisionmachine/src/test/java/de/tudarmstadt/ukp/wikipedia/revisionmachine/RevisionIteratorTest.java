/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeNoException;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;

public class RevisionIteratorTest
{

	private static Wikipedia wiki = null;
	private static RevisionIterator revisionIterator = null;
	private static RevisionAPIConfiguration config = null;

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
		db.setHost("bender.ukp.informatik.tu-darmstadt.de");
		db.setDatabase("wikiapi_simple_20090119");
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

		config = new RevisionAPIConfiguration();
		config.setHost(db.getHost());
		config.setDatabase(db.getDatabase());
		config.setUser(db.getUser());
		config.setPassword(db.getPassword());
		config.setLanguage(db.getLanguage());
	}

	@Test
	public void iteratorTest()
	{

		int i = 0;
		try{
			revisionIterator = new RevisionIterator(config);
		}catch (WikiApiException e) {
			assertFalse("Error creating iterator", true);
		}
		while (revisionIterator.hasNext() && i < 500) {
			Revision revision = revisionIterator.next();
			revision.getArticleID();
			revision.getFullRevisionID();
			revision.getRevisionCounter();
			revision.getRevisionText();
			revision.getTimeStamp();
			i++;
		}
		assertEquals(500, i);

		//close iterator
		try {
			revisionIterator.close();
		}
		catch (SQLException e) {
			assertFalse("Error closing iterator", true);
		}
	}


	@Test
	public void lazyLoadingTest() {
		ArrayList<String> texts = new ArrayList<String>();
		int i = 0;
		//create new iterator without lazy loading
		try{
			revisionIterator = new RevisionIterator(config);
		}catch (WikiApiException e) {
			assertFalse("Error creating iterator", true);
		}

		while (revisionIterator.hasNext() && i < 500) {
			Revision revision = revisionIterator.next();
			texts.add(revision.getRevisionText());
			i++;
		}
		//closing iterator
		try {
			revisionIterator.close();
		}
		catch (SQLException e) {
			assertFalse("Error closing iterator", true);
		}

		ArrayList<String> lazyLoadedTexts = new ArrayList<String>();
		i = 0;

		//create new iterator with lazy loading
		try{
			revisionIterator = new RevisionIterator(config, true);
		}catch (WikiApiException e) {
			assertFalse("Error creating iterator", true);
		}

		while (revisionIterator.hasNext() && i < 1000) {
			Revision revision = revisionIterator.next();
			lazyLoadedTexts.add(revision.getRevisionText());
			i++;
		}

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
			assertFalse("Error closing iterator", true);
		}

	}

}
