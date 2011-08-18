package de.tudarmstadt.ukp.wikipedia.mwdumper.importer;
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

import junit.framework.TestCase;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceSet;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title;

public class TitleTest extends TestCase {
	NamespaceSet namespaces;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TitleTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
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

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		namespaces = null;
	}

	private class TestItem {
		public int ns;
		public String text;
		public String prefixed;
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

	TestItem[] tests = {
		new TestItem(0, "Page title", "Page title"),
		new TestItem(1, "Page title", "Talk:Page title"),
		new TestItem(-1, "Recentchanges", "Special:Recentchanges"),
		new TestItem(13, "Logging in", "Help talk:Logging in"),
		new TestItem(0, "2001: A Space Odyssey", "2001: A Space Odyssey"),
		new TestItem(0, "2:2", "2:2")
	};

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.Title(int, String, NamespaceSet)'
	 */
	public void testTitleIntStringNamespaceSet() {
		for (TestItem test : tests) {
			Title title = new Title(new Integer(test.ns), test.text, namespaces);
			assertEquals(test.toString(), test.prefixed, title.toString());
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.Title(String, NamespaceSet)'
	 */
	public void testTitleStringNamespaceSet() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			assertEquals(test.toString(), test.ns, title.Namespace.intValue());
			assertEquals(test.toString(), test.text, title.Text);
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.ValidateTitleChars(String)'
	 */
	/*public void testValidateTitleChars() {
	 // FIXME
	}*/

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.toString()'
	 */
	public void testToString() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			assertEquals(test.toString(), test.prefixed, title.toString());
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.isSpecial()'
	 */
	public void testIsSpecial() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			if (test.ns < 0) {
				assertTrue(test.toString(), title.isSpecial());
			}
			else {
				assertFalse(test.toString(), title.isSpecial());
			}
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.isTalk()'
	 */
	public void testIsTalk() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			if (title.isSpecial()) {
				assertFalse(test.toString(), title.isTalk());
			}
			else if (test.ns % 2 == 0) {
				assertFalse(test.toString(), title.isTalk());
			}
			else {
				assertTrue(test.toString(), title.isTalk());
			}
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.talkPage()'
	 */
	public void testTalkPage() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			if (title.isTalk()) {
				assertEquals(test.toString(), title, title.talkPage());
			}
			else if (title.isSpecial()) {
				assertNull(test.toString(), title.talkPage());
			}
			else {
				assertFalse(test.toString(), title.equals(title.talkPage()));
			}
		}
	}

	/*
	 * Test method for 'de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title.subjectPage()'
	 */
	public void testSubjectPage() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			if (title.isTalk()) {
				assertNotSame(test.toString(), title, title.subjectPage());
			}
			else {
				assertSame(test.toString(), title, title.subjectPage());
			}
		}
	}

	public void testTalkSubjectPage() {
		for (TestItem test : tests) {
			Title title = new Title(test.prefixed, namespaces);
			if (title.isTalk()) {
				assertEquals(test.toString(), title, title.subjectPage().talkPage());
			}
			else if (title.isSpecial()) {
				assertNull(test.toString(), title.subjectPage().talkPage());
			}
			else {
				assertEquals(test.toString(), title, title.talkPage().subjectPage());
			}
		}
	}

}
