package de.tudarmstadt.ukp.wikipedia.util.templates.parser;

import java.util.List;

import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.CompilerException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.sweble.TemplateNameExtractor;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;

public class SwebleUtils
{
    public static final String SWEBLE_CONFIG = "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml";

	/**
	 * Extracts sections (without title) from Wikitext.
	 *
	 * @param text article text with wiki markup
	 * @param title article title
	 * @param revision the revision id
	 * @return list of Strings with the sections text
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	public static List<ExtractedSection> getSections(String text, String title, long revision) throws Exception{
		return (List<ExtractedSection>) parsePage(new SectionExtractor(), text, title, revision);
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
	public static List<String> getTemplateNames(String text, String title) throws Exception{
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
	private static Object parsePage(AstVisitor v, String text, String title, long revision) throws Exception{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage(text, title, revision).getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 * SimpleWikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws CompilerException if the wiki page could not be compiled by the parser
	 */
	private static CompiledPage getCompiledPage(String text, String title, long revision) throws Exception
	{
		SimpleWikiConfiguration config = new SimpleWikiConfiguration(SWEBLE_CONFIG);

		PageTitle pageTitle = PageTitle.make(config, title);
		PageId pageId = new PageId(pageTitle, revision);
		// Compile the retrieved page
		Compiler compiler = new Compiler(config);
		return compiler.postprocess(pageId, text, null);
	}
}

