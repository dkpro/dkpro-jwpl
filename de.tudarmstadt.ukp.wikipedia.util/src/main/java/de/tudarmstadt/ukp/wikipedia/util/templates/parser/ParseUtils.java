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
package de.tudarmstadt.ukp.wikipedia.util.templates.parser;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.sweble.TemplateNameExtractor;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;

public class ParseUtils
{
    public static final String SWEBLE_CONFIG = "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml";

	/**
	 * Extracts sections (without title) from Wikitext.
	 *
	 * @param text article text with wiki markup
	 * @param title article title
	 * @param revision the revision id
	 * @return list of ExtractedSections
	 */
	public static List<ExtractedSection> getSections(String text, String title, long revision) throws LinkTargetException, EngineException, FileNotFoundException, JAXBException{
		return (List<ExtractedSection>) parsePage(new SectionExtractor(), text, title, revision);
	}

	/**
	 * Extracts sections (without title) from Wikitext.
	 *
	 * @param text article text with wiki markup
	 * @param title article title
	 * @param revision the revision id
	 * @param templatesToMark a list of template names that should be annotated in the text
	 * @return list of ExtractedSections
	 * @throws EngineException if the wiki page could not be compiled by the parser
	 */
	public static List<ExtractedSection> getSections(String text, String title, long revision, List<String> templatesToMark) throws LinkTargetException, EngineException, FileNotFoundException, JAXBException{
		return (List<ExtractedSection>) parsePage(new SectionExtractor(templatesToMark), text, title, revision);
	}

	/**
	 * Extracts template names from Wikitext by descending into every node and looking for templates.
	 * Results may contain duplicates if template appears multiple times in the article.
	 *
	 * @param text article text with wiki markup
	 * @param title article title
	 * @return list of template names
	 * @throws EngineException if the wiki page could not be compiled by the parser
	 */
	public static List<String> getTemplateNames(String text, String title) throws LinkTargetException, EngineException, FileNotFoundException, JAXBException{
		return (List<String>) parsePage(new TemplateNameExtractor(), text, title, -1);
	}

	/**
	 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
	 * and the provided visitor.
	 *
	 * @return the parsed page. The actual return type depends on the provided
	 *         visitor. You have to cast the return type according to the return
	 *         type of the go() method of your visitor.
	 * @throws EngineException if the wiki page could not be compiled by the parser
	 */
	private static Object parsePage(AstVisitor v, String text, String title, long revision) throws LinkTargetException, EngineException, FileNotFoundException, JAXBException{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage(text, title, revision).getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 * SimpleWikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws LinkTargetException
	 * @throws EngineException if the wiki page could not be compiled by the parser
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	private static EngProcessedPage getCompiledPage(String text, String title, long revision) throws LinkTargetException, EngineException, FileNotFoundException, JAXBException
	{
		WikiConfig config = DefaultConfigEnWp.generate();

		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, revision);
		// Compile the retrieved page
		WtEngineImpl engine = new WtEngineImpl(config);
		// Compile the retrieved page
		return engine.postprocess(pageId, text, null);
	}

	/**
	 * Removes template markers from a String
	 * Assumes the standard prefix and suffix
	 *
	 * @param str A string with template markers
	 * @return the same string without template markers
	 */
	private static String removeTemplateMarker(String str){
		return str.replaceAll("\\{\\{(.*?)\\}\\}", "");
	}

	/**
	 * Returns the template marker of the sentence.
	 *
	 * Assumes hat a sentence has only ONE marker.
	 * If the sentence contains more than one template, an IllegalStateException
	 * is thrown.
	 *
	 * If the String does not contain a marker, null is returned
	 *
	 *
	 * @param str A string with template markers
	 * @return the template marker
	 */
	private static String getTemplateMarker(String str) throws IllegalStateException{
		Pattern p = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
		Matcher matcher = p.matcher(str);
		String tpl = null;
		while(matcher.find())
		{
		    if(tpl!=null){
		    	throw new IllegalStateException("More than one template in the sentence.");
		    }
			tpl=matcher.group(1);
		}
		return tpl;

	}

}

