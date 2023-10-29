package org.dkpro.jwpl.mwdumper.importer;
/*
 * MediaWiki import/export processing tools
 * Copyright 2005 by Brion Vibber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $Id: TitleTest.java 11268 2005-10-10 06:57:30Z vibber $
 */


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TitleTest {
	NamespaceSet namespaces;

	@BeforeEach
	protected void setUp() {
		namespaces = new NamespaceSet();
		namespaces.add(-2, "Media");
		namespaces.add(-1, "Special");
		namespaces.add(0, "");
		namespaces.add(1, "Talk");
		namespaces.add(2, "User");
		namespaces.add(3, "User talk");
		namespaces.add(4, "Project");
		namespaces.add(5, "Project talk");
		namespaces.add(6, "Image");
		namespaces.add(7, "Image talk");
		namespaces.add(8, "MediaWiki");
		namespaces.add(9, "MediaWiki talk");
		namespaces.add(10, "Template");
		namespaces.add(11, "Template talk");
		namespaces.add(12, "Help");
		namespaces.add(13, "Help talk");
		namespaces.add(14, "Category");
		namespaces.add(15, "Category talk");
	}

	@AfterEach
	protected void tearDown() {
		namespaces = null;
	}

	private class TestItem {
		public final int ns;
		public final String text;
		public final String prefixed;
		TestItem(int ns, String text, String prefixed) {
			this.ns = ns;
			this.text = text;
			this.prefixed = prefixed;
		}
		@Override
		public String toString() {
			return "(" + ns + ",\"" + text + "\") [[" + prefixed + "]]";
		}
	}

	final TestItem[] tests = {
		new TestItem(0, "Page title", "Page title"),
		new TestItem(1, "Page title", "Talk:Page title"),
		new TestItem(-1, "Recentchanges", "Special:Recentchanges"),
		new TestItem(13, "Logging in", "Help talk:Logging in"),
		new TestItem(0, "2001: A Space Odyssey", "2001: A Space Odyssey"),
		new TestItem(0, "2:2", "2:2")
	};

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.Title(int, String, NamespaceSet)'
	 */
	@Test
	public void testTitleIntStringNamespaceSet() {
		for (TestItem item : tests) {
			Title title = new Title(item.ns, item.text, namespaces);
			assertEquals(item.prefixed, title.toString(), item.toString());
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.Title(String, NamespaceSet)'
	 */
	@Test
	public void testTitleStringNamespaceSet() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			assertEquals(item.ns, title.Namespace.intValue(), item.toString());
			assertEquals(item.text, title.Text, item.toString());
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.toString()'
	 */
	@Test
	public void testToString() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			assertEquals(item.prefixed, title.toString(), item.toString());
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.isSpecial()'
	 */
	@Test
	public void testIsSpecial() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			if (item.ns < 0) {
				assertTrue(title.isSpecial(), item.toString());
			}
			else {
				assertFalse(title.isSpecial(), item.toString());
			}
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.isTalk()'
	 */
	@Test
	public void testIsTalk() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			if (title.isSpecial()) {
				assertFalse(title.isTalk(), item.toString());
			}
			else if (item.ns % 2 == 0) {
				assertFalse(title.isTalk(), item.toString());
			}
			else {
				assertTrue(title.isTalk(), item.toString());
			}
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.talkPage()'
	 */
	@Test
	public void testTalkPage() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			if (title.isTalk()) {
				assertEquals(title, title.talkPage(), item.toString());
			}
			else if (title.isSpecial()) {
				assertNull(title.talkPage(), item.toString());
			}
			else {
				assertNotEquals(title, title.talkPage(), item.toString());
			}
		}
	}

	/*
	 * Test method for 'org.dkpro.jwpl.mwdumper.importer.Title.subjectPage()'
	 */
	@Test
	public void testSubjectPage() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			if (title.isTalk()) {
				assertNotEquals(title, title.subjectPage(), item.toString());
			}
			else {
				assertEquals(title, title.subjectPage(), item.toString());
			}
		}
	}

	@Test
	public void testTalkSubjectPage() {
		for (TestItem item : tests) {
			Title title = new Title(item.prefixed, namespaces);
			if (title.isTalk()) {
				assertEquals( title, title.subjectPage().talkPage(), item.toString());
			}
			else if (title.isSpecial()) {
				assertNull(title.subjectPage().talkPage(), item.toString());
			}
			else {
				assertEquals(title, title.talkPage().subjectPage(), item.toString());
			}
		}
	}

}
