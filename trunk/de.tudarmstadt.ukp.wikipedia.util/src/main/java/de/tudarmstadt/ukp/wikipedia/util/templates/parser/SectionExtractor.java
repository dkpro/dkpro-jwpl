package de.tudarmstadt.ukp.wikipedia.util.templates.parser;

/**
 * Derived from the TextConverter class which was published in the
 * Sweble example project provided on
 * http://http://sweble.org by the Open Source Research Group,
 * University of Erlangen-NÃ¼rnberg under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0)
 */

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.sweble.wikitext.engine.Page;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.LinkTargetException;
import org.sweble.wikitext.lazy.parser.*;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;

/**
 * A visitor that extracts sections from an article AST.
 *
 * @author Oliver Ferschke
 */
public class SectionExtractor extends AstVisitor
{
	private final SimpleWikiConfiguration config;

	private List<ExtractedSection> sections;

	private StringBuilder bodyBuilder = new StringBuilder();

	// =========================================================================


	/**
	 * Creates a new visitor that extracts anchors of internal links from a
	 * parsed Wikipedia article using the default Sweble config as defined
	 * in WikiConstants.SWEBLE_CONFIG.
	 */
	public SectionExtractor()
	{
		SimpleWikiConfiguration config=null;
		try{
			config = new SimpleWikiConfiguration(WikiConstants.SWEBLE_CONFIG);
		}catch(IOException e){
			//TODO logger
			e.printStackTrace();
		}catch(JAXBException e){
			//TODO logger
			e.printStackTrace();
		}
		this.config=config;
	}

	/**
	 * Creates a new visitor that extracts anchors of internal links from a
	 * parsed Wikipedia article.
	 *
	 * @param config the Sweble configuration
	 */
	public SectionExtractor(SimpleWikiConfiguration config)
	{
		this.config = config;
	}

	@Override
	protected boolean before(AstNode node)
	{
		// This method is called by go() before visitation starts
		sections = new LinkedList<ExtractedSection>();
		return super.before(node);
	}

	@Override
	protected Object after(AstNode node, Object result)
	{
		return sections;
	}

	// =========================================================================

	public void visit(Page n)
	{
		iterate(n);
	}

	public void visit(Whitespace n)
	{
		bodyBuilder.append(" ");
	}

	public void visit(Bold n)
	{
		iterate(n);
	}

	public void visit(Italics n)
	{
		iterate(n);
	}

	public void visit(ExternalLink link)
	{
		iterate(link.getTitle());
	}

	public void visit(LinkTitle n)
	{
		iterate(n);
	}

	public void visit(LinkTarget n)
	{
		iterate(n);
	}

	public void visit(InternalLink link)
	{
		try
		{
			PageTitle page = PageTitle.make(config, link.getTarget());
			if (page.getNamespace().equals(config.getNamespace("Category"))) {
				return;
			}else{
				String curLinkTitle="";
				for(AstNode n:link.getTitle().getContent()){
					if(n instanceof Text){
						curLinkTitle = ((Text)n).getContent().trim();
					}
				}
				if(curLinkTitle.isEmpty()){
					bodyBuilder.append(link.getTarget());
				}else{
					bodyBuilder.append(curLinkTitle);
				}

			}
		}
		catch (LinkTargetException e)
		{
		}

	}
	public void visit(DefinitionList n){
		iterate(n);
	}

	public void visit(DefinitionTerm n){
		iterate(n);
	}

	public void visit(DefinitionDefinition n){
		iterate(n);
	}

	public void visit(AstNode n)
	{
	}

	public void visit(NodeList n)
	{
		iterate(n);
	}

	public void visit(Paragraph n)
	{
		iterate(n);
	}

	public void visit(Text n)
	{
		bodyBuilder.append(n.getContent());
	}

	public void visit(Section sect) throws IOException
	{

		String title = null;
		String body = null;

		for(AstNode n:sect.getTitle()){
			if(n instanceof Text){
				title = ((Text)n).getContent();
			}
		}
		iterate(sect.getBody());

		sections.add(new ExtractedSection(title,bodyBuilder.toString().trim()));
		bodyBuilder=new StringBuilder();
	}


	/**
	 * Wraps title and body text of an extraction section
	 *
	 * @author Oliver Ferschke
	 *
	 */
	public class ExtractedSection
	{
		private String title;
		private String body;

		public ExtractedSection(String title, String body){
			this.title=title;
			this.body=body;
		}

		public String getTitle()
		{
			return title;
		}
		public void setTitle(String aTitle)
		{
			title = aTitle;
		}
		public String getBody()
		{
			return body;
		}
		public void setBody(String aBody)
		{
			body = aBody;
		}
	}
}
