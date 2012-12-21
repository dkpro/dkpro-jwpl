package de.tudarmstadt.ukp.wikipedia.util.templates.parser;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;

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
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	public static List<ExtractedSection> getSections(String text, String title, long revision) throws LinkTargetException, CompilerException, FileNotFoundException, JAXBException{
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
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	public static List<ExtractedSection> getSections(String text, String title, long revision, List<String> templatesToMark) throws LinkTargetException, CompilerException, FileNotFoundException, JAXBException{
		return (List<ExtractedSection>) parsePage(new SectionExtractor(templatesToMark), text, title, revision);
	}

	/**
	 * Extracts template names from Wikitext by descending into every node and looking for templates.
	 * Results may contain duplicates if template appears multiple times in the article.
	 *
	 * @param text article text with wiki markup
	 * @param title article title
	 * @return list of template names
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	public static List<String> getTemplateNames(String text, String title) throws LinkTargetException, CompilerException, FileNotFoundException, JAXBException{
		return (List<String>) parsePage(new TemplateNameExtractor(), text, title, -1);
	}

	/**
	 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
	 * and the provided visitor.
	 *
	 * @return the parsed page. The actual return type depends on the provided
	 *         visitor. You have to cast the return type according to the return
	 *         type of the go() method of your visitor.
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	private static Object parsePage(AstVisitor v, String text, String title, long revision) throws LinkTargetException, CompilerException, FileNotFoundException, JAXBException{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage(text, title, revision).getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 * SimpleWikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws LinkTargetException
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	private static CompiledPage getCompiledPage(String text, String title, long revision) throws LinkTargetException, CompilerException, FileNotFoundException, JAXBException
	{
		SimpleWikiConfiguration config = new SimpleWikiConfiguration(SWEBLE_CONFIG);

		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, revision);
		// Compile the retrieved page
		Compiler compiler = new Compiler(config);
		return compiler.postprocess(pageId, text, null);
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
	 * @return the
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

