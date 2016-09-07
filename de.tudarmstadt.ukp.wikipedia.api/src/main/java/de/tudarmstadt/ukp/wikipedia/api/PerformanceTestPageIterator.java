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
