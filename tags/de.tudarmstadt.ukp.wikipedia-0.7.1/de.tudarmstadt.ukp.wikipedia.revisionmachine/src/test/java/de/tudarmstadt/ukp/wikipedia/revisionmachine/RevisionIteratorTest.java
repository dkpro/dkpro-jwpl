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
import static org.junit.Assume.assumeNoException;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;

public class RevisionIteratorTest
{

	private static Wikipedia wiki = null;
	private static RevisionIterator revisionIterator = null;

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
		db.setHost("bender.tk.informatik.tu-darmstadt.de");
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

		RevisionAPIConfiguration config = new RevisionAPIConfiguration();
		config.setHost(db.getHost());
		config.setDatabase(db.getDatabase());
		config.setUser(db.getUser());
		config.setPassword(db.getPassword());
		config.setLanguage(db.getLanguage());

		try {
			revisionIterator = new RevisionIterator(config);
		}
		catch (Exception e) {
			Assume.assumeNoException(e);
		}
		Assume.assumeNotNull(revisionIterator);
	}

	@Test
	public void iteratorTest()
	{
		int i = 0;
		while (revisionIterator.hasNext() && i < 1000) {
			Revision revision = revisionIterator.next();
			revision.getArticleID();
			revision.getFullRevisionID();
			revision.getRevisionCounter();
			revision.getRevisionText();
			revision.getTimeStamp();
			i++;
		}
		assertEquals(1000, i);
	}
}
