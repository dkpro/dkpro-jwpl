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
package de.tudarmstadt.ukp.wikipedia.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class TitleTest {

	@Test
	public void titleTest1() throws WikiTitleParsingException {

		Title t = new Title("Car");
		assertEquals("Car", t.getEntity());
		assertEquals("Car", t.getPlainTitle());
		assertEquals("Car", t.getRawTitleText());
		assertEquals(null,  t.getDisambiguationText());
		assertEquals(null,  t.getSectionText());
		assertEquals("Car", t.getWikiStyleTitle());
	}

	@Test
	public void titleTest2() throws WikiTitleParsingException {

		Title t = new Title("Car_(automobile)");
		assertEquals("Car", t.getEntity());
		assertEquals("Car (automobile)", t.getPlainTitle());
		assertEquals("Car_(automobile)", t.getRawTitleText());
		assertEquals("automobile", t.getDisambiguationText());
		assertEquals(null,  t.getSectionText());
		assertEquals("Car_(automobile)", t.getWikiStyleTitle());
	}

	@Test
	public void titleTest3() throws WikiTitleParsingException {

		Title t = new Title("Car (automobile)");
		assertEquals("Car", t.getEntity());
		assertEquals("Car (automobile)", t.getPlainTitle());
		assertEquals("Car (automobile)", t.getRawTitleText());
		assertEquals("automobile", t.getDisambiguationText());
		assertEquals(null,  t.getSectionText());
		assertEquals("Car_(automobile)", t.getWikiStyleTitle());
	}

	@Test
	public void titleTest4() throws WikiTitleParsingException {

		Title t = new Title("Car (automobile)#Introduction");
		assertEquals("Car", t.getEntity());
		assertEquals("Car (automobile)", t.getPlainTitle());
		assertEquals("Car (automobile)#Introduction", t.getRawTitleText());
		assertEquals("automobile", t.getDisambiguationText());
		assertEquals("Introduction",  t.getSectionText());
		assertEquals("Car_(automobile)", t.getWikiStyleTitle());
	}

	@Test
	public void titleTest5() throws WikiTitleParsingException {

		Title t = new Title("Car_(automobile)#Introduction");
		assertEquals("Car", t.getEntity());
		assertEquals("Car (automobile)", t.getPlainTitle());
		assertEquals("Car_(automobile)#Introduction", t.getRawTitleText());
		assertEquals("automobile", t.getDisambiguationText());
		assertEquals("Introduction",  t.getSectionText());
		assertEquals("Car_(automobile)", t.getWikiStyleTitle());
	}

    @Test
    public void titleTest6() throws WikiTitleParsingException {

        Title t = new Title("Car#Introduction");
        assertEquals("Car", t.getEntity());
        assertEquals("Car", t.getPlainTitle());
        assertEquals("Car#Introduction", t.getRawTitleText());
        assertEquals(null, t.getDisambiguationText());
        assertEquals("Introduction",  t.getSectionText());
        assertEquals("Car", t.getWikiStyleTitle());
    }

	@Test
	public void titleTest7() throws WikiTitleParsingException {

		Title t = new Title("401(k)");
		assertEquals("401(k)", t.getEntity());
		assertEquals("401(k)", t.getPlainTitle());
		assertEquals("401(k)", t.getRawTitleText());
		assertEquals(null, t.getDisambiguationText());
		assertEquals(null,  t.getSectionText());
		assertEquals("401(k)", t.getWikiStyleTitle());
	}

	@Test
	public void titleTest8() throws WikiTitleParsingException {

		Title t = new Title("Ytterbium(III)_chloride_(data_page)");
		assertEquals("Ytterbium(III) chloride", t.getEntity());
		assertEquals("Ytterbium(III) chloride (data page)", t.getPlainTitle());
		assertEquals("Ytterbium(III)_chloride_(data_page)", t.getRawTitleText());
		assertEquals("data page", t.getDisambiguationText());
		assertEquals(null,  t.getSectionText());
		assertEquals("Ytterbium(III)_chloride_(data_page)", t.getWikiStyleTitle());
	}

}
