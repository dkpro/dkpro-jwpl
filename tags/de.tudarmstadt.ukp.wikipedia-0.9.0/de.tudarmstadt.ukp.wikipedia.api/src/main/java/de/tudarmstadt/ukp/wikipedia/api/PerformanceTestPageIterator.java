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

import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;


/**
 * This is a test class for the version of PageIterator, that buffers a
 * certain number of pages in order to gain efficiency.
 * We get the same number of pages from a Wikipedia using 
 * different buffer sizes and return the performance.
 *
 * For an unbuffered iterator set bufferSize to 1.
 * 
 * @author Anouar
 * 
 */
public class PerformanceTestPageIterator {

	public static void test(int numberOfPages, int bufferSize, Wikipedia wiki) {
		long from = System.currentTimeMillis();
		Iterator<Page> pages = wiki.getPages(bufferSize).iterator();
		int counter = 0;
		while (counter < numberOfPages && pages.hasNext()) {
			pages.next();
			counter++;
		}
		long to = System.currentTimeMillis();
		System.out.println("RetrievedPages  : " + counter);
		System.out.println("Used Buffer Size: " + bufferSize);
		System.out.println("Time            : " + (to - from) + "ms");
		System.out.println("------------------------------");
	}

	public static void main(String[] args) throws Exception {
		DatabaseConfiguration db = new DatabaseConfiguration();
		db.setDatabase("wikiapi_uk");
		db.setHost("bender.ukp.informatik.tu-darmstadt.de");
		db.setUser("student");
		db.setPassword("student");
		db.setLanguage(Language.ukrainian);
		Wikipedia wiki = new Wikipedia(db);
		System.out.println("Test: retrieve 4000 pages ...");
		test(4000, 1, wiki);
		test(4000, 10, wiki);
		test(4000, 50, wiki);
		test(4000, 100, wiki);
		test(4000, 200, wiki);
		test(4000, 500, wiki);
	}

}
