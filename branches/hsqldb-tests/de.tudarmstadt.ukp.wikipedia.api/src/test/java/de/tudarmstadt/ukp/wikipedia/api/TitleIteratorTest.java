/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

public class TitleIteratorTest extends BaseJWPLTest{

	@Before
	public void setupWikipedia() {
		DatabaseConfiguration db = obtainHSDLDBConfiguration();
		try {
			wiki = new Wikipedia(db);
		} catch (WikiInitializationException e) {
			fail("Wikipedia could not be initialized.");
		}
	}
	

	@Test
	public void test_titleIteratorTest() {
		
        int nrOfTitles = 0;
		Iterator<Title> titleIter = wiki.getTitles().iterator();
		
		while (titleIter.hasNext()) {
			Title t = titleIter.next();
            System.out.println(t.getPlainTitle());
			nrOfTitles++;
		}
		assertEquals("Number of titles == 36", 36, nrOfTitles);
		
	}
}
