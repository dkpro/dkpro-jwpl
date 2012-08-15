package de.tudarmstadt.ukp.wikipedia.api.sweble;

/**
 * Derived from the TextConverter class which was published in the
 * Sweble example project provided on
 * http://http://sweble.org by the Open Source Research Group,
 * University of Erlangen-Nürnberg under the Apache License, Version 2.0
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
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.ImageLink;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.Paragraph;
import org.sweble.wikitext.lazy.parser.Section;

import de.fau.cs.osr.ptk.common.Visitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;

/**
 * A visitor that extracts anchors of Wikipedia-internal links from an article AST.
 * To better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 * <ul>
 * <li>{@link http://en.wikipedia.org/wiki/Visitor_pattern} (classic pattern)</li>
 * <li>{@link http://www.javaworld.com/javaworld/javatips/jw-javatip98.html}
 * (the version we use here)</li>
 * </ul>
 *
 * The methods needed to descend into an AST and visit the children of a given
 * node <code>n</code> are
 * <ul>
 * <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node
 * <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code>
 * and gather the return values of the <code>visit()</code> calls in a list,</li>
 * <li><code>mapInPlace(n)</code> - visit the <b>children</b> of node
 * <code>n</code> and replace each child node <code>c</code> with the return
 * value of the call to <code>visit(c)</code>.</li>
 * </ul>
 *
 * @author Oliver Ferschke
 */
public class InternalLinkAnchorExtractor extends Visitor
{
	private final SimpleWikiConfiguration config;

	private List<String> anchors;

	// =========================================================================


	/**
	 * Creates a new visitor that extracts anchors of internal links from a
	 * parsed Wikipedia article using the default Sweble config as defined
	 * in WikiConstants.SWEBLE_CONFIG.
	 */
	public InternalLinkAnchorExtractor()
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
	public InternalLinkAnchorExtractor(SimpleWikiConfiguration config)
	{
		this.config = config;
	}

	@Override
	protected boolean before(AstNode node)
	{
		// This method is called by go() before visitation starts
		anchors = new LinkedList<String>();
		return super.before(node);
	}

	@Override
	protected Object after(AstNode node, Object result)
	{
		return anchors;
	}

	// =========================================================================

	public void visit(AstNode n)
	{
		iterate(n);
	}

	public void visit(NodeList n)
	{
		iterate(n);
	}

	public void visit(Page p)
	{
		iterate(p.getContent());
	}

	public void visit(ExternalLink link)
	{
		//	We want only internal links
	}

	public void visit(ImageLink n)
	{
		//	We want only internal links
	}


	//FIXME does not work yet
	public void visit(InternalLink link)
	{
		try
		{
			PageTitle page = PageTitle.make(config, link.getTarget());
			if (page.getNamespace().equals(config.getNamespace("Category"))) {
				return;
			}
		}
		catch (LinkTargetException e)
		{
		}

		if (link.getTitle().getContent() == null
		        || link.getTitle().getContent().isEmpty())
		{
			String anchor = link.getTarget();
			if(!anchor.contains(":")){
				add(link.getTarget());
			}
		}
		else
		{
			iterate(link.getTitle());
		}
	}

	public void visit(Section s)
	{
		iterate(s.getBody());
	}

	public void visit(Paragraph p)
	{
		iterate(p.getContent());
	}


	private void add(String s)
	{
		if (s.isEmpty()) {
			return;
		}
		anchors.add(s);
	}

}
