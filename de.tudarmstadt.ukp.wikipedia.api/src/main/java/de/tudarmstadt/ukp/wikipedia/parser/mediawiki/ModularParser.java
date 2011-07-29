/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Lesser
 * Public License v3 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 * 		Torsten Zesch - initial API and implementation
 *     	Samy Ateia - provided a patch via the JWPL mailing list
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser.mediawiki;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.parser.*;
import de.tudarmstadt.ukp.wikipedia.parser.Content.FormatType;


/**
 * This is a Parser for MediaWiki Source.<br/>
 * It exist a MediaWikiParserFactory, to get an Instance of this Parser.<br/>
 * But, if you want to, you can create a parser by yourself.
 *
 * @author CJacobi
 *
 */
public class ModularParser implements MediaWikiParser,
		MediaWikiContentElementParser
{

	private final Log logger = LogFactory.getLog(getClass());

	// Options, set by the ParserFactory
	private String lineSeparator;
	private List<String> categoryIdentifers;
	private List<String> languageIdentifers;
	private List<String> imageIdentifers;
	private MediaWikiTemplateParser templateParser;
	private boolean showImageText = false;
	private boolean deleteTags = true;
	private boolean showMathTagContent = true;
	private boolean calculateSrcSpans = true;

	/**
	 * Creates a unconfigurated Parser...
	 */
	public ModularParser()
	{
	}

	/**
	 * Creates a fully configurated parser...
	 */
	public ModularParser(String lineSeparator, List<String> languageIdentifers,
			List<String> categoryIdentifers, List<String> imageIdentifers,
			boolean showImageText, boolean deleteTags,
			boolean showMathTagContent, boolean calculateSrcSpans,
			MediaWikiTemplateParser templateParser)
	{

		setLineSeparator(lineSeparator);
		setLanguageIdentifers(languageIdentifers);
		setCategoryIdentifers(categoryIdentifers);
		setImageIdentifers(imageIdentifers);
		setShowImageText(showImageText);
		setDeleteTags(deleteTags);
		setShowMathTagContent(showMathTagContent);
		setCalculateSrcSpans(calculateSrcSpans);
		setTemplateParser(templateParser);
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	@Override
	public String getLineSeparator()
	{
		return lineSeparator;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setLineSeparator(String lineSeparator)
	{
		this.lineSeparator = lineSeparator;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public List<String> getLanguageIdentifers()
	{
		return languageIdentifers;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setLanguageIdentifers(List<String> languageIdentifers)
	{
		this.languageIdentifers = listToLowerCase(languageIdentifers);
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public List<String> getCategoryIdentifers()
	{
		return categoryIdentifers;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setCategoryIdentifers(List<String> categoryIdentifers)
	{
		this.categoryIdentifers = listToLowerCase(categoryIdentifers);
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public List<String> getImageIdentifers()
	{
		return imageIdentifers;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setImageIdentifers(List<String> imageIdentifers)
	{
		this.imageIdentifers = listToLowerCase(imageIdentifers);
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public MediaWikiTemplateParser getTemplateParser()
	{
		return templateParser;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setTemplateParser(MediaWikiTemplateParser templateParser)
	{
		this.templateParser = templateParser;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public boolean showImageText()
	{
		return showImageText;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setShowImageText(boolean showImageText)
	{
		this.showImageText = showImageText;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public boolean deleteTags()
	{
		return deleteTags;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setDeleteTags(boolean deleteTags)
	{
		this.deleteTags = deleteTags;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public boolean showMathTagContent()
	{
		return showMathTagContent;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setShowMathTagContent(boolean showMathTagContent)
	{
		this.showMathTagContent = showMathTagContent;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public boolean calculateSrcSpans()
	{
		return calculateSrcSpans;
	}

	/**
	 * Look at MediaWikiParserFactory for a description...
	 */
	public void setCalculateSrcSpans(boolean calculateSrcSpans)
	{
		this.calculateSrcSpans = calculateSrcSpans;
	}

	/**
	 * Converts a List of Strings to lower case Strings.
	 */
	private List<String> listToLowerCase(List<String> l)
	{
		List<String> result = new ArrayList<String>();
		for (String s : l)
		{
			result.add(s.toLowerCase());
		}
		return result;
	}

	/**
	 * Look at the MediaWikiParser interface for a description...
	 */
	@Override
	public String configurationInfo()
	{
		StringBuilder result = new StringBuilder();

		result.append("MediaWikiParser configuration:\n");
		result.append("ParserClass: " + this.getClass() + "\n");
		result.append("ShowImageText: " + showImageText + "\n");
		result.append("DeleteTags: " + deleteTags + "\n");
		result.append("ShowMathTagContent: " + showMathTagContent + "\n");
		result.append("CalculateSrcSpans: " + calculateSrcSpans + "\n");

		result.append("LanguageIdentifers: ");
		for (String s : languageIdentifers)
		{
			result.append(s + " ");
		}
		result.append("\n");

		result.append("CategoryIdentifers: ");
		for (String s : categoryIdentifers)
		{
			result.append(s + " ");
		}
		result.append("\n");

		result.append("ImageIdentifers: ");
		for (String s : imageIdentifers)
		{
			result.append(s + " ");
		}
		result.append("\n");

		result.append("TemplateParser: " + templateParser.getClass() + "\n");
		result.append(templateParser.configurationInfo());

		return result.toString();
	}

	/**
	 * Checks if the configuration is runnable.
	 */
	private boolean runConfig()
	{
		if (lineSeparator == null)
		{
			logger.error("Set lineSeparator");
			return false;
		}
		if (categoryIdentifers == null)
		{
			logger.error("Set categoryIdentifers");
			return false;
		}
		if (languageIdentifers == null)
		{
			logger.error("Set languageIdentifers");
			return false;
		}
		if (imageIdentifers == null)
		{
			logger.error("Set imageIdentifers");
			return false;
		}
		if (templateParser == null)
		{
			logger.error("Set templateParser");
			return false;
		}
		return true;
	}

	/**
	 * Look at the MediaWikiParser for a description...
	 */
	@Override
	public ParsedPage parse(String src)
	{
		// check if the configuration is runnable.
		if (!runConfig())
		{
			return null;
		}

		// check if the is something to parse. somtimes there is an empty string
		// due to an error of other clases...
		if (src == null || src.length() == 0)
		{
			return null;
		}

		// creates a new span manager with the given source, appending a newline
		// to avoid errors.
		SpanManager sm = new SpanManager(src.replace('\t', ' ') + lineSeparator);
		if (calculateSrcSpans)
		{
			sm.enableSrcPosCalculation();
		}

		// Creating a new ParsePage, which will be filled with information in
		// the parseing process.
		ParsedPage ppResult = new ParsedPage();

		// Creating a new Parameter Container
		ContentElementParsingParameters cepp = new ContentElementParsingParameters();

		// Deletes comments out of the Source
		deleteComments(sm);

		// Deletes any TOC Tags, these are not usesd in this parser.
		deleteTOCTag(sm);

		// Removing the Content which should not parsed but integrated later in
		// the resulting text
		sm.manageList(cepp.noWikiSpans);
		parseSpecifiedTag(sm, cepp.noWikiSpans, cepp.noWikiStrings, "PRE", " ");
		parseSpecifiedTag(sm, cepp.noWikiSpans, cepp.noWikiStrings, "NOWIKI");
		if (cepp.noWikiSpans.size() == 0)
		{
			sm.removeManagedList(cepp.noWikiSpans);
		}

		// Parseing the Math Tags...
		sm.manageList(cepp.mathSpans);
		parseSpecifiedTag(sm, cepp.mathSpans, cepp.mathStrings, "MATH");
		if (cepp.mathSpans.size() == 0)
		{
			sm.removeManagedList(cepp.mathSpans);
		}

		// Parseing the Templates (the Span List will be added to the managed
		// lists by the function)
		parseTemplates(sm, cepp.templateSpans, cepp.templates, ppResult);

		// Parsing all other Tags
		parseTags(sm, cepp.tagSpans);

		// Converting &lt;gallery>s to normal Images, this is not beautiful, but
		// a simple solution..
		convertGalleriesToImages(sm, cepp.tagSpans);

		// Parsing Links and Images.
		parseImagesAndInternalLinks(sm, cepp.linkSpans, cepp.links);

		// Creating a list of Line Spans to work with lines in the following
		// functions
		LinkedList<Span> lineSpans = new LinkedList<Span>();
		getLineSpans(sm, lineSpans);

		// Removing the Category Links from the Links list, and crating an
		// ContentElement for these links...
		ppResult.setCategoryElement(getSpecialLinks(sm, cepp.linkSpans,
				cepp.links, " - ", categoryIdentifers));

		// Removing the Language Links from the Links list, and crating an
		// ContentElement for these links...
		ppResult.setLanguagesElement(getSpecialLinks(sm, cepp.linkSpans,
				cepp.links, " - ", languageIdentifers));

		// Parsing and Setting the Sections... the main work is done in parse
		// sections!
		ppResult.setSections(EmptyStructureRemover
				.eliminateEmptyStructures(parseSections(sm, cepp, lineSpans)));

		// Finding and Setting the paragraph which is concidered as the "First"
		setFirstParagraph(ppResult);

		// check the calculated source positions, and reset them if necessary.
		if (calculateSrcSpans)
		{
			SrcPosRangeChecker.checkRange(ppResult);
		}

		// So it is done...
		return ppResult;
	}



	/**
	 * Deleting all comments out of the SpanManager...<br/>
	 * &lt!-- COMMENT -->
	 */
	private void deleteComments(SpanManager sm)
	{
		int start = 0;
		while ((start = sm.indexOf("<!--", start)) != -1)
		{
			int end = sm.indexOf("-->", start + 4) + 3;
			if (end == -1 + 3)
			{
				end = sm.length();
			}

			// Remove the one lineSeparator too, if the whole line is a comment!
			try
			{
				if (lineSeparator.equals(sm.substring(start
						- lineSeparator.length(), start))
						&& lineSeparator.equals(sm.substring(end, end
								+ lineSeparator.length())))
				{
					end += lineSeparator.length();
				}
			}
			catch (IndexOutOfBoundsException e)
			{}

			sm.delete(start, end);
		}
	}

	/**
	 * Deleteing ALL TOC Tags
	 */
	private void deleteTOCTag(SpanManager sm)
	{
		// delete all __TOC__ from SRC
		int temp = 0;
		while ((temp = sm.indexOf("__TOC__", temp)) != -1)
		{
			sm.delete(temp, temp + 2 + 3 + 2);
		}

		// delete all __NOTOC__ from SRC
		temp = 0;
		while ((temp = sm.indexOf("__NOTOC__", temp)) != -1)
		{
			sm.delete(temp, temp + 2 + 5 + 2);
		}
	}

	private ContentElement getSpecialLinks(SpanManager sm,
			List<Span> linkSpans, List<Link> links, String linkSpacer,
			List<String> identifers)
	{
		ContentElement result = new ContentElement();
		StringBuilder text = new StringBuilder();
		List<Link> localLinks = new ArrayList<Link>();

		for (int i = links.size() - 1; i >= 0; i--)
		{
			String identifer = getLinkNameSpace(links.get(i).getTarget());

			if (identifer != null && identifers.indexOf(identifer) != -1)
			{
				Link l = links.remove(i);
				Span s = linkSpans.remove(i);
				String linkText = sm.substring(s);
				sm.delete(s);
				l.setHomeElement(result);
				s.adjust(-s.getStart() + text.length());
				text.append(linkText + linkSpacer);
				localLinks.add(l);
				//TODO add type?
			}
		}

		int len = text.length();
		if (len != 0)
		{
			text.delete(len - linkSpacer.length(), len);
		}

		result.setText(text.toString());
		result.setLinks(localLinks);

		if (result.empty())
		{
			return null;
		}
		else
		{
			return result;
		}
	}

	private void getLineSpans(SpanManager sm, LinkedList<Span> lineSpans)
	{
		sm.manageList(lineSpans);

		int start = 0;
		int end;

		while ((end = sm.indexOf(lineSeparator, start)) != -1)
		{
			lineSpans.add(new Span(start, end).trimTrail(sm));
			start = end + lineSeparator.length();
		}
		lineSpans.add(new Span(start, sm.length()).trimTrail(sm));

		while (!lineSpans.isEmpty() && lineSpans.getFirst().length() == 0)
		{
			lineSpans.removeFirst();
		}
		while (!lineSpans.isEmpty() && lineSpans.getLast().length() == 0)
		{
			lineSpans.removeLast();
		}
	}

	private SectionContainer parseSections(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans)
	{

		List<SectionContent> contentSections = new ArrayList<SectionContent>();

		SectionContent sc = new SectionContent(1);

		if (calculateSrcSpans)
		{
			sc.setSrcSpan(new SrcSpan(sm.getSrcPos(lineSpans.getFirst()
					.getStart()), -1));
		}

		// Identify the Line Type and call the necessary Function for the
		// further handling...
		while (!lineSpans.isEmpty())
		{

			Span s = lineSpans.getFirst();

			lineType t = getLineType(sm, s);
			switch (t)
			{
			case SECTION:
				contentSections.add(sc);
				int level = getSectionLevel(sm, s);
				sc = new SectionContent(parseContentElement(sm, cepp, new Span(
						s.getStart() + level, s.getEnd() - level).trim(sm)),
						level);
				lineSpans.removeFirst();

				if (calculateSrcSpans)
				{
					sc.setSrcSpan(new SrcSpan(sm.getSrcPos(s.getStart()), -1));
				}

				break;

			case HR:
				// remove the HR (----) and handle the rest as a parapraph line
				removeHr(sm, s);
				t = lineType.PARAGRAPH;
			case PARAGRAPH:
			case PARAGRAPH_BOXED:
			case PARAGRAPH_INDENTED:
				sc.addParagraph(buildParagraph(sm, cepp, lineSpans, t));
				break;

			case NESTEDLIST:
			case NESTEDLIST_NR:
				sc.addNestedList(buildNestedList(sm, cepp, lineSpans, t));
				break;

			case DEFINITIONLIST:
				sc.addDefinitionList(buildDefinitionList(sm, cepp, lineSpans));
				break;

			case TABLE:
				sc.addTable(buildTable(sm, cepp, lineSpans));
				break;

			case EMPTYLINE:
				lineSpans.removeFirst();
				break;

			default:
				logger.error("unknown lineStart!: \"" + sm.substring(s) + "\"");
				lineSpans.removeFirst();
			}
		}

		// add the remaining Section to the list.
		contentSections.add(sc);

		return buildSectionStructure(contentSections);
	}

	private Span removeHr(SpanManager sm, Span s)
	{
		int start = s.getStart();
		final int end = s.getEnd();
		while (sm.charAt(start) == '-' && start < end)
		{
			start++;
		}
		return s.setStart(start).trim(sm);
	}

	/**
	 * The Line Types wich are possible...
	 */
	private enum lineType
	{
		SECTION, TABLE, NESTEDLIST, NESTEDLIST_NR, DEFINITIONLIST, HR, PARAGRAPH, PARAGRAPH_INDENTED, PARAGRAPH_BOXED, EMPTYLINE
	}

	/**
	 * Retunrns the Type of a line, this is mainly done by the First Char of the
	 * Line...
	 */
	private lineType getLineType(SpanManager sm, Span lineSpan)
	{

		switch (lineSpan.charAt(0, sm))
		{

		case '{':
			if (lineSpan.charAt(1, sm) == '|')
			{
				return lineType.TABLE;
			}
			else
			{
				return lineType.PARAGRAPH;
			}

		case '=':
			if (lineSpan.length() > 2
					&& sm.charAt(lineSpan.getEnd() - 1) == '=')
			{
				return lineType.SECTION;
			}
			else
			{
				return lineType.PARAGRAPH;
			}

		case '-':
			if (lineSpan.charAt(1, sm) == '-' && lineSpan.charAt(2, sm) == '-'
					&& lineSpan.charAt(3, sm) == '-')
			{
				return lineType.HR;
			}
			else
			{
				return lineType.PARAGRAPH;
			}

		case '*':
			return lineType.NESTEDLIST;

		case '#':
			return lineType.NESTEDLIST_NR;

		case ';':
			return lineType.DEFINITIONLIST;

		case ':':
			if (lineSpan.length() > 1)
			{
				if (lineSpan.length() > 2 && lineSpan.charAt(1, sm) == '{'
						&& lineSpan.charAt(2, sm) == '|')
				{
					return lineType.TABLE;
				}
				else
				{
					return lineType.PARAGRAPH_INDENTED;
				}
			}
			else
			{
				return lineType.PARAGRAPH;
			}

		case ' ':
			int nonWSPos = lineSpan.nonWSCharPos(sm);
			switch (lineSpan.charAt(nonWSPos, sm))
			{
			case Span.ERRORCHAR:
				return lineType.EMPTYLINE;
			case '{':
				if (lineSpan.charAt(nonWSPos + 1, sm) == '|')
				{
					return lineType.TABLE;
				}
			default:
				return lineType.PARAGRAPH_BOXED;
			}

		case Span.ERRORCHAR:
			return lineType.EMPTYLINE;

		default:
			return lineType.PARAGRAPH;
		}
	}

	/**
	 * Returns the number of Equality Chars which are used to specify the level
	 * of the Section.
	 */
	private int getSectionLevel(SpanManager sm, Span sectionNameSpan)
	{
		int begin = sectionNameSpan.getStart();
		int end = sectionNameSpan.getEnd();
		int level = 0;

		try
		{
			while ((sm.charAt(begin + level) == '=')
					&& (sm.charAt(end - 1 - level) == '='))
			{
				level++;
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			// there is no need to do anything!
			logger.debug("EXCEPTION IS OK: " + e);
		}

		if (begin + level == end)
		{
			level = (level - 1) / 2;
		}

		return level;
	}

	/**
	 * Takes a list of SectionContent and returns a SectionContainer with the
	 * given SectionContent s in the right structure.
	 */
	private SectionContainer buildSectionStructure(List<SectionContent> scl)
	{
		SectionContainer result = new SectionContainer(0);

		for (SectionContent sContent : scl)
		{
			int contentLevel = sContent.getLevel();
			SectionContainer sContainer = result;

			// get the right SectionContainer or create it
			for (int containerLevel = result.getLevel() + 1; containerLevel < contentLevel; containerLevel++)
			{
				int containerSubSections = sContainer.nrOfSubSections();
				if (containerSubSections != 0)
				{
					Section temp = sContainer
							.getSubSection(containerSubSections - 1);
					if (temp.getClass() == SectionContainer.class)
					{
						sContainer = (SectionContainer) temp;
					}
					else
					{
						SectionContainer sct = new SectionContainer(temp
								.getTitleElement(), containerLevel);
						sct.addSection(temp);
						if (calculateSrcSpans)
						{
							sct.setSrcSpan(temp.getSrcSpan());
						}
						temp.setTitleElement(null);
						temp.setLevel(containerLevel + 1);
						sContainer.removeSection(temp);
						sContainer.addSection(sct);
						sContainer = sct;
					}
				}
				else
				{
					sContainer = new SectionContainer(null, containerLevel);
				}
			}

			sContainer.addSection(sContent);
		}

		if (calculateSrcSpans)
		{
			result.setSrcSpan(new SrcSpan(0, -1));
		}

		return result;
	}

	private boolean startsWithIgnoreCase(String s1, String s2)
	{
		final int s2len = s2.length();
		if (s1.length() < s2len)
		{
			return false;
		}
		return s1.substring(0, s2len).equalsIgnoreCase(s2);
	}

	private Span getTag(SpanManager sm, int offset)
	{
		int start = sm.indexOf("<", offset);
		if (start == -1)
		{
			return null;
		}
		int end = sm.indexOf(">", start);
		if (end == -1)
		{
			return null;
		}

		Span s = new Span(start, end + 1);
		if (calculateSrcSpans)
		{
			s
					.setSrcSpan(new SrcSpan(sm.getSrcPos(start), sm
							.getSrcPos(end) + 1));
		}
		return s;
	}

	private String getTagText(SpanManager sm, Span tag)
	{
		return sm.substring(new Span(tag.getStart() + 1, tag.getEnd() - 1)
				.trim(sm));
	}

	private void parseSpecifiedTag(SpanManager sm, List<Span> spans,
			List<String> strings, String specifier)
	{
		parseSpecifiedTag(sm, spans, strings, specifier, "");
	}

	private void parseSpecifiedTag(SpanManager sm, List<Span> spans,
			List<String> strings, String specifier, String prefix)
	{
		int offset = 0;

		Span s;
		while ((s = getTag(sm, offset)) != null)
		{
			offset = s.getEnd();
			String tagText = getTagText(sm, s);
			if (startsWithIgnoreCase(tagText, specifier))
			{

				Span e;
				while ((e = getTag(sm, offset)) != null)
				{
					offset = e.getEnd();
					tagText = getTagText(sm, e);
					if (startsWithIgnoreCase(tagText, "/" + specifier))
					{
						break;
					}
				}

				if (e == null)
				{
					/*
					 * OF: Setting e to sm.length()results in ArrayIndexOutOfBoundsExeption if calculateSrcSpans=true
					 */
					//e = new Span(sm.length(), sm.length());
					e = new Span(Math.max(0,sm.length()-1), Math.max(0,sm.length()-1));
				}

				strings.add(sm.substring(s.getEnd(), e.getStart()));

				Span tSpan = new Span(s.getStart(), e.getEnd());
				if (calculateSrcSpans)
				{
					tSpan.setSrcSpan(new SrcSpan(sm.getSrcPos(s.getStart()), sm
							.getSrcPos(e.getEnd())));
				}

				spans.add(tSpan);
				sm.replace(tSpan, prefix + "(" + specifier + ")");
				tSpan.adjustStart(prefix.length());

				offset = tSpan.getEnd();
			}
		}
	}

	private void parseTags(SpanManager sm, List<Span> spans)
	{
		sm.manageList(spans);

		Span s = new Span(0, 0);
		while ((s = getTag(sm, s.getEnd())) != null)
		{
			spans.add(s);
		}

		if (spans.size() == 0)
		{
			sm.removeManagedList(spans);
		}
	}

	private void parseTemplates(SpanManager sm,
			List<Span> resolvedTemplateSpans,
			List<ResolvedTemplate> resolvedTemplates, ParsedPage pp)
	{

		sm.manageList(resolvedTemplateSpans);

		int pos = -2;
		Stack<Integer> templateOpenTags = new Stack<Integer>();
		while ((pos = sm.indexOf("{{", pos + 2)) != -1)
		{
			if (sm.length() > pos + 3 && sm.charAt(pos + 2) == '{'
					&& sm.charAt(pos + 3) != '{')
			{
				pos++;
			}
			templateOpenTags.push(pos);
		}

		while (!templateOpenTags.empty())
		{
			int templateOpenTag = templateOpenTags.pop();
			int templateCloseTag = sm.indexOf("}}", templateOpenTag);
			if (templateCloseTag == -1)
			{
				continue;
			}

			int templateOptionTag = sm.indexOf("|", templateOpenTag,
					templateCloseTag);
			int templateNameEnd;
			List<String> templateOptions;

			if (templateOptionTag != -1)
			{
				templateNameEnd = templateOptionTag;
				templateOptions = tokenize(sm, templateOptionTag + 1,
						templateCloseTag, "|");
			}
			else
			{
				templateNameEnd = templateCloseTag;
				templateOptions = new ArrayList<String>();
			}

			Span ts = new Span(templateOpenTag, templateCloseTag + 2);

			Template t = new Template(ts, encodeWikistyle(sm.substring(
					templateOpenTag + 2, templateNameEnd).trim()),
					templateOptions);

			if (calculateSrcSpans)
			{
				t.setSrcSpan(new SrcSpan(sm.getSrcPos(templateOpenTag), sm
						.getSrcPos(templateCloseTag + 2)));
			}

			t.setPos(ts);

			ResolvedTemplate rt = templateParser.parseTemplate(t, pp);

			resolvedTemplateSpans.add(ts);
			resolvedTemplates.add(rt);

			sm.replace(ts, rt.getPreParseReplacement());
		}

		if (resolvedTemplateSpans.isEmpty())
		{
			sm.removeManagedList(resolvedTemplateSpans);
		}
	}

	private void convertGalleriesToImages(SpanManager sm, List<Span> tagSpans)
	{
		// Quick Hack, not very efficent, should be improved, wont work with
		// calculateSrcSpans == true !

		for (int i = 0; i < tagSpans.size() - 1; i++)
		{
			String openText = getTagText(sm, tagSpans.get(i));
			if (startsWithIgnoreCase(openText, "GALLERY"))
			{

				if (startsWithIgnoreCase(getTagText(sm, tagSpans.get(i + 1)),
						"/GALLERY"))
				{

					// gallery range is tag(i).end() .. tag(i+1).start()
					Span startSpan = tagSpans.remove(i);
					Span endSpan = tagSpans.remove(i);
					i--;

					StringBuilder sb = new StringBuilder();

					// caption (any option will be treated as caption)
					int eqPos = openText.indexOf('=');
					if (eqPos != -1)
					{
						int captionStart = eqPos + 1;
						int captionEnd = openText.length();

						if (captionStart < captionEnd
								&& openText.charAt(captionStart) == '"'
								&& openText.charAt(captionEnd - 1) == '"')
						{
							captionStart++;
							captionEnd--;
						}

						if (captionStart < captionEnd)
						{
							sb.append(openText.substring(captionStart,
									captionEnd)
									+ lineSeparator);
						}
					}

					// images
					for (String s : tokenize(sm, startSpan.getEnd(), endSpan
							.getStart(), lineSeparator))
					{
						sb.append("[[" + s + "]]" + lineSeparator);
					}

					// replace the source and remove the tags
					sm.replace(startSpan.getStart(), endSpan.getEnd(), sb
							.toString());
				}
				else
				{
					continue;
				}
			}
		}
	}

	private Table buildTable(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans)
	{

		Table result = new Table();
		int col = -1;
		int row = 0;
		int subTables = 0;
		LinkedList<Span> tableDataSpans = new LinkedList<Span>();
		sm.manageList(tableDataSpans);

		if (calculateSrcSpans)
		{
			result.setSrcSpan(new SrcSpan(sm.getSrcPos(lineSpans.getFirst()
					.getStart()), -1));
		}

		lineSpans.removeFirst();

		while (!lineSpans.isEmpty())
		{
			Span s = lineSpans.removeFirst();

			int pos = s.nonWSCharPos(sm);
			char c0 = s.charAt(pos, sm);
			char c1 = s.charAt(pos + 1, sm);

			if (subTables == 0 && (c0 == '!' || c0 == '|'))
			{
				if (!tableDataSpans.isEmpty())
				{
					lineSpans.addFirst(s);

					SrcSpan ei = null;
					if (calculateSrcSpans)
					{
						ei = new SrcSpan(sm.getSrcPos(tableDataSpans.getFirst()
								.getStart() - 1) + 1, -1);
					}

					TableElement te = new TableElement(parseSections(sm, cepp,
							tableDataSpans), row, col);
					te.setSrcSpan(ei);
					result.addTableElement(te);
					lineSpans.removeFirst();
				}

				col++;
				if (c1 == '-')
				{
					row++;
					col = -1;
					continue;
				}
				else if (c0 == '|' && c1 == '}')
				{
					sm.removeManagedList(tableDataSpans);

					if (calculateSrcSpans)
					{
						result.getSrcSpan().setEnd(sm.getSrcPos(s.getEnd()));
					}

					return result;
				}
				else if (c0 == '|' && c1 == '+')
				{
					result.setTitleElement(parseContentElement(sm, cepp,
							new Span(s.getStart() + pos + 2, s.getEnd())
									.trim(sm)));
					continue;
				}
				else
				{
					int multipleCols;
					if ((multipleCols = sm.indexOf("||",
							s.getStart() + pos + 1, s.getEnd())) != -1)
					{
						lineSpans.addFirst(new Span(multipleCols + 1, s
								.getEnd()));
						s.setEnd(multipleCols);
					}

					int optionTagPos = sm.indexOf("|", s.getStart() + pos + 1,
							s.getEnd());

					if (optionTagPos != -1)
					{
						s.setStart(optionTagPos + 1).trim(sm);
					}
					else
					{
						s.adjustStart(pos + 1).trim(sm);
					}
				}
			}
			else if (c0 == '|' && c1 == '}')
			{
				subTables--;
			}
			else if (c0 == '{' && c1 == '|')
			{
				subTables++;
			}

			tableDataSpans.addLast(s);
		}

		if (tableDataSpans.size() != 0)
		{

			SrcSpan ei = null;
			if (calculateSrcSpans)
			{
				ei = new SrcSpan(sm.getSrcPos(tableDataSpans.getFirst()
						.getStart() - 1) + 1, -1);
			}

			TableElement te = new TableElement(parseSections(sm, cepp,
					tableDataSpans), row, col);
			te.setSrcSpan(ei);

			result.addTableElement(te);
		}

		sm.removeManagedList(tableDataSpans);

		if (calculateSrcSpans)
		{
			result.getSrcSpan().setEnd(-1);
		}

		return result;
	}

	private NestedListContainer buildNestedList(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans,
			lineType listType)
	{

		boolean numbered = listType == lineType.NESTEDLIST_NR;
		NestedListContainer result = new NestedListContainer(numbered);

		if (calculateSrcSpans)
		{
			result.setSrcSpan(new SrcSpan(sm.getSrcPos(lineSpans.getFirst()
					.getStart()), -1));
		}

		LinkedList<Span> nestedListSpans = new LinkedList<Span>();
		while (!lineSpans.isEmpty())
		{
			Span s = lineSpans.getFirst();
			if (listType != getLineType(sm, s))
			{
				break;
			}
			nestedListSpans
					.add(new Span(s.getStart() + 1, s.getEnd()).trim(sm));
			lineSpans.removeFirst();
		}
		sm.manageList(nestedListSpans);

		if (calculateSrcSpans)
		{
			result.getSrcSpan().setEnd(
					sm.getSrcPos(nestedListSpans.getLast().getEnd()));
		}

		while (!nestedListSpans.isEmpty())
		{
			Span s = nestedListSpans.getFirst();
			lineType t = getLineType(sm, s);
			if (t == lineType.NESTEDLIST || t == lineType.NESTEDLIST_NR)
			{
				result.add(buildNestedList(sm, cepp, nestedListSpans, t));
			}
			else
			{
				nestedListSpans.removeFirst();
				result.add((NestedListElement) parseContentElement(sm, cepp, s,
						new NestedListElement()));
			}
		}

		sm.removeManagedList(nestedListSpans);

		return result;
	}

	private DefinitionList buildDefinitionList(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans)
	{
		List<ContentElement> content = new ArrayList<ContentElement>();

		Span s = lineSpans.removeFirst();

		int temp = sm.indexOf(":", s);
		if (temp == -1)
		{
			content.add(parseContentElement(sm, cepp, new Span(
					s.getStart() + 1, s.getEnd())));
		}
		else
		{
			content.add(parseContentElement(sm, cepp, new Span(temp + 1, s
					.getEnd())));
			content.add(0, parseContentElement(sm, cepp, new Span(
					s.getStart() + 1, temp)));
		}

		while (!lineSpans.isEmpty())
		{
			Span ns = lineSpans.getFirst();
			if (sm.charAt(ns.getStart()) != ':')
			{
				break;
			}
			lineSpans.removeFirst();
			content.add(parseContentElement(sm, cepp, new Span(
					ns.getStart() + 1, ns.getEnd())));
		}

		DefinitionList result = new DefinitionList(content);

		if (calculateSrcSpans)
		{
			result.setSrcSpan(new SrcSpan(sm.getSrcPos(s.getStart()), content
					.get(content.size() - 1).getSrcSpan().getEnd()));
		}

		return result;
	}

	private Paragraph buildParagraph(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans,
			lineType paragraphType)
	{

		LinkedList<Span> paragraphSpans = new LinkedList<Span>();
		Paragraph result = new Paragraph();
		Span s = lineSpans.removeFirst();
		paragraphSpans.add(s);

		switch (paragraphType)
		{
		case PARAGRAPH:
			result.setType(Paragraph.type.NORMAL);
			while (!lineSpans.isEmpty())
			{
				if (paragraphType != getLineType(sm, lineSpans.getFirst()))
				{
					break;
				}
				paragraphSpans.add(lineSpans.removeFirst());
			}
			break;

		case PARAGRAPH_BOXED:
			result.setType(Paragraph.type.BOXED);
			while (!lineSpans.isEmpty())
			{
				lineType lt = getLineType(sm, lineSpans.getFirst());
				if (paragraphType != lt && lineType.EMPTYLINE != lt)
				{
					break;
				}
				paragraphSpans.add(lineSpans.removeFirst());
			}
			break;

		case PARAGRAPH_INDENTED:
			result.setType(Paragraph.type.INDENTED);
			s.trim(sm.setCharAt(s.getStart(), ' '));
			break;

		default:
			return null;
		}

		parseContentElement(sm, cepp, paragraphSpans, result);

		return result;
	}

	private List<String> tokenize(SpanManager sm, int start, int end,
			String delim)
	{
		List<String> result = new ArrayList<String>();

		if (start > end)
		{
			logger.debug("tokenize(" + start + ", " + end
					+ ") doesn't make sense");
			return result;
		}

		int s = start;
		int e;
		String token;
		// Span rs;
		while ((e = sm.indexOf(delim, s, end)) != -1)
		{
			// rs = new Span(s, e).trim( sm );
			// if( rs.length()>0 ) result.add( sm.substring( rs ) );
			token = sm.substring(s, e).trim();
			if (token.length() > 0)
			{
				result.add(token);
			}
			s = e + delim.length();
		}
		// rs = new Span(s, end).trim( sm );
		// if( rs.length()>0 ) result.add( sm.substring( rs ) );
		token = sm.substring(s, end).trim();
		if (token.length() > 0)
		{
			result.add(token);
		}

		return result;
	}

	private void parseExternalLinks(SpanManager sm, Span s, String protocol,
			List<Span> managedList, List<Link> links, Content home_cc)
	{
		int extLinkTargetStart;
		Span extLinkSpan = new Span(0, s.getStart());

		while ((extLinkTargetStart = sm.indexOf(protocol, extLinkSpan.getEnd(),
				s.getEnd())) != -1)
		{

			// Allowed char before the protocol identifer ?
			if (extLinkTargetStart > s.getStart()
					&& (" [").indexOf(sm.charAt(extLinkTargetStart - 1)) == -1)
			{
				extLinkSpan = new Span(0, extLinkTargetStart + 1);
				continue;
			}

			// Target
			int extLinkTargetEnd = extLinkTargetStart;
			while ((lineSeparator + " ]").indexOf(sm.charAt(extLinkTargetEnd)) == -1)
			{
				extLinkTargetEnd++;
			}

			// Open/Close Tags
			int extLinkOpenTag = extLinkTargetStart - 1;
			int extLinkCloseTag;
			int extLinkTextStart = extLinkTargetStart;
			int extLinkTextEnd = extLinkTargetEnd;

			while (extLinkOpenTag >= s.getStart()
					&& sm.charAt(extLinkOpenTag) == ' ')
			{
				extLinkOpenTag--;
			}

			if (extLinkOpenTag >= s.getStart()
					&& sm.charAt(extLinkOpenTag) == '[')
			{
				extLinkCloseTag = sm.indexOf("]", extLinkTargetEnd, s.getEnd());

				if (extLinkCloseTag != -1)
				{
					extLinkTextStart = extLinkTargetEnd;
					// nicht wie bei "normalen" links durhc | getrennt sondenr
					// durhc leerzeichen !!! schei�e !!!
					while (sm.charAt(extLinkTextStart) == ' ')
					{
						extLinkTextStart++;
					}
					extLinkTextEnd = extLinkCloseTag;
					extLinkCloseTag++;

					if (extLinkTextStart == extLinkTextEnd)
					{
						sm.insert(extLinkTextStart, "[ ]");
						extLinkTextEnd += 3;
						extLinkCloseTag += 3;
					}
				}
				else
				{
					extLinkOpenTag = extLinkTargetStart;
					extLinkCloseTag = extLinkTargetEnd;
				}
			}
			else
			{
				extLinkOpenTag = extLinkTargetStart;
				extLinkCloseTag = extLinkTargetEnd;
			}

			extLinkSpan = new Span(extLinkOpenTag, extLinkCloseTag);
			managedList.add(extLinkSpan);

			Link l = new Link(home_cc, extLinkSpan, sm.substring(
					extLinkTargetStart, extLinkTargetEnd), Link.type.EXTERNAL,
					null);
			links.add(l);

			if (calculateSrcSpans)
			{
				l.setSrcSpan(new SrcSpan(sm.getSrcPos(extLinkOpenTag), sm
						.getSrcPos(extLinkCloseTag - 1) + 1));
			}

			sm.delete(extLinkTextEnd, extLinkCloseTag);
			sm.delete(extLinkOpenTag, extLinkTextStart);
		}
	}

	/**
	 * Returns the LOWERCASE NameSpace of the link target
	 */
	private static String getLinkNameSpace(String target)
	{
		int pos = target.indexOf(':');
		if (pos == -1)
		{
			return null;
		}
		else
		{
			return target.substring(0, pos).replace('_', ' ').trim()
					.toLowerCase();
		}
	}

	/**
	 * There is not much differences between links an images, so they are parsed
	 * in a single step
	 */
	private void parseImagesAndInternalLinks(SpanManager sm,
			List<Span> linkSpans, List<Link> links)
	{

		sm.manageList(linkSpans);

		int pos = -1;
		Stack<Integer> linkOpenTags = new Stack<Integer>();
		while ((pos = sm.indexOf("[[", pos + 1)) != -1)
		{
			linkOpenTags.push(pos);
		}

		Span lastLinkSpan = new Span(sm.length() + 1, sm.length() + 1);
		Link.type linkType = Link.type.INTERNAL;

		while (!linkOpenTags.empty())
		{
			int linkStartTag = linkOpenTags.pop();
			int linkEndTag = sm.indexOf("]]", linkStartTag);
			if (linkEndTag == -1)
			{
				continue;
			}

			int linkOptionTag = sm.indexOf("|", linkStartTag, linkEndTag);

			int linkTextStart;
			String linkTarget;

			if (linkOptionTag != -1)
			{
				linkTextStart = linkOptionTag + 1;
				linkTarget = sm.substring(new Span(linkStartTag + 2,
						linkOptionTag).trim(sm));
			}
			else
			{
				linkTextStart = linkStartTag + 2;
				linkTarget = sm
						.substring(new Span(linkStartTag + 2, linkEndTag)
								.trim(sm));
			}

			// is is a regular link ?
			if (linkTarget.indexOf(lineSeparator) != -1)
			{
				continue;
			}
			linkTarget = encodeWikistyle(linkTarget);

			// so it is a Link or image!!!
			List<String> parameters;

			String namespace = getLinkNameSpace(linkTarget);
			if (namespace != null)
			{
				if (imageIdentifers.indexOf(namespace) != -1)
				{
					if (linkOptionTag != -1)
					{
						int temp;
						while ((temp = sm.indexOf("|", linkTextStart,
								linkEndTag)) != -1)
						{
							linkTextStart = temp + 1;
						}

						parameters = tokenize(sm, linkOptionTag + 1,
								linkEndTag, "|");

						// maybe there is an external link at the end of the
						// image description...
						if (sm.charAt(linkEndTag + 2) == ']'
								&& sm.indexOf("[", linkTextStart, linkEndTag) != -1)
						{
							linkEndTag++;
						}
					}
					else
					{
						parameters = null;
					}
					linkType = Link.type.IMAGE;
				}
				else
				{
					//Link has namespace but is not image
					linkType = Link.type.UNKNOWN;
					parameters = null;
				}
			}
			else
			{
				if (linkType == Link.type.INTERNAL
						&& lastLinkSpan.hits(new Span(linkStartTag,
								linkEndTag + 2)))
				{
					continue;
				}
				parameters = null;
				linkType = Link.type.INTERNAL;
			}

			Span posSpan = new Span(linkTextStart, linkEndTag).trim(sm);
			linkSpans.add(posSpan);

			Link l = new Link(null, posSpan, linkTarget, linkType, parameters);
			links.add(l);

			if (calculateSrcSpans)
			{
				l.setSrcSpan(new SrcSpan(sm.getSrcPos(linkStartTag), sm
						.getSrcPos(linkEndTag + 2)));
			}

			sm.delete(posSpan.getEnd(), linkEndTag + 2);
			sm.delete(linkStartTag, posSpan.getStart());

			// removing line separators in link text
			int lsinlink;
			while ((lsinlink = sm.indexOf(lineSeparator, posSpan)) != -1)
			{
				sm.replace(lsinlink, lsinlink + lineSeparator.length(), " ");
			}

			lastLinkSpan = posSpan;
		}
	}

	/**
	 * Searches the Range given by the Span s for the double occurence of
	 * "quotation" and puts the results in the List quotedSpans. The Quotation
	 * tags will be deleted.
	 *
	 * @param sm
	 *            , the Source in which will be searched
	 * @param s
	 *            , the range in which will be searched
	 * @param quotedSpans
	 *            , the List where the Spans will be placed, should be managed
	 *            by the SpanManager sm
	 * @param quotation
	 *            , the start and end tag as String
	 */
	private void parseQuotedSpans(SpanManager sm, Span s,
			List<Span> quotedSpans, String quotation)
	{

		final int qlen = quotation.length();

		// get the start position
		int start = sm.indexOf(quotation, s.getStart(), s.getEnd());

		while (start != -1)
		{

			// get the end position
			int end = sm.indexOf(quotation, start + qlen, s.getEnd());
			if (end == -1)
			{
				break;
			}

			// build a new span from start and end position.
			Span qs = new Span(start, end);
			quotedSpans.add(qs);

			// calculate the original src positions.
			if (calculateSrcSpans)
			{
				qs.setSrcSpan(new SrcSpan(sm.getSrcPos(start), sm.getSrcPos(end
						+ qlen - 1) + 1));
			}

			// delete the tags.
			sm.delete(end, end + qlen);
			sm.delete(start, start + qlen);

			// get the next start position
			start = sm.indexOf(quotation, qs.getEnd(), s.getEnd());
		}
	}

	/**
	 * Searches a line for Bold and Italic quotations, this has to be done
	 * linewhise.
	 */
	private void parseBoldAndItalicSpans(SpanManager sm, Span line,
			List<Span> boldSpans, List<Span> italicSpans)
	{
		// Das suchen nach BOLD und ITALIC muss in den Jeweiligen
		// Zeilen geschenhen, da ein LineSeparator immer BOLD und
		// Italic Tags schliesst.

		// Bold Spans
		parseQuotedSpans(sm, line, boldSpans, "'''");

		// Italic Spans
		parseQuotedSpans(sm, line, italicSpans, "''");

		// Maybe there is ONE SINGLE OPEN TAG left... handel these...
		int openTag = sm.indexOf("''", line);
		if (openTag != -1)
		{
			// build a Span from this Tag.
			Span qs = new Span(openTag, line.getEnd());

			// calculate the original src positions.
			if (calculateSrcSpans)
			{
				qs.setSrcSpan(new SrcSpan(sm.getSrcPos(openTag), sm
						.getSrcPos(line.getEnd())));
			}

			// is it a Bold or an Italic tag ?
			if (sm.indexOf("'''", openTag, openTag + 3) != -1)
			{
				// --> BOLD
				boldSpans.add(qs);
				sm.delete(openTag, openTag + 3);
			}
			else
			{
				// --> ITALIC
				italicSpans.add(qs);
				sm.delete(openTag, openTag + 2);
			}
		}
	}

	private static String encodeWikistyle(String str)
	{
		return str.replace(' ', '_');
	}

	/**
	 * Building a ContentElement from a String
	 */
	@Override
	public ContentElement parseContentElement(String src)
	{
		SpanManager sm = new SpanManager(src);
		ContentElementParsingParameters cepp = new ContentElementParsingParameters();

		parseImagesAndInternalLinks(sm, cepp.linkSpans, cepp.links);

		LinkedList<Span> lineSpans = new LinkedList<Span>();
		getLineSpans(sm, lineSpans);
		sm.removeManagedList(lineSpans);
		return (parseContentElement(sm, cepp, lineSpans, new ContentElement()));
	}

	/**
	 * Building a ContentElement from a single line.
	 */
	private ContentElement parseContentElement(SpanManager sm,
			ContentElementParsingParameters cepp, Span lineSpan)
	{
		LinkedList<Span> lineSpans = new LinkedList<Span>();
		lineSpans.add(lineSpan);
		return parseContentElement(sm, cepp, lineSpans, new ContentElement());
	}

	/**
	 * Building a ContentElement from a single line. But the result is given, so
	 * e.g. a NestedListElement can be filled with information...
	 */
	private ContentElement parseContentElement(SpanManager sm,
			ContentElementParsingParameters cepp, Span lineSpan,
			ContentElement result)
	{
		LinkedList<Span> lineSpans = new LinkedList<Span>();
		lineSpans.add(lineSpan);
		return parseContentElement(sm, cepp, lineSpans, result);
	}

	/**
	 * Building a ContentElement, this funciton is calles by all the other
	 * parseContentElement(..) functions
	 */
	private ContentElement parseContentElement(SpanManager sm,
			ContentElementParsingParameters cepp, LinkedList<Span> lineSpans,
			ContentElement result)
	{

		List<Link> localLinks = new ArrayList<Link>();
		List<Template> localTemplates = new ArrayList<Template>();

		List<Span> boldSpans = new ArrayList<Span>();
		List<Span> italicSpans = new ArrayList<Span>();
		sm.manageList(boldSpans);
		sm.manageList(italicSpans);

		List<Span> managedSpans = new ArrayList<Span>();
		sm.manageList(managedSpans);

		Span contentElementRange = new Span(lineSpans.getFirst().getStart(),
				lineSpans.getLast().getEnd()).trim(sm);
		managedSpans.add(contentElementRange);

		// set the SrcSpan
		if (calculateSrcSpans)
		{
			result.setSrcSpan(new SrcSpan(sm.getSrcPos(contentElementRange
					.getStart()), sm.getSrcPos(contentElementRange.getEnd())));
		}

		sm.manageList(lineSpans);
		while (!lineSpans.isEmpty())
		{
			Span line = lineSpans.getFirst();

			parseBoldAndItalicSpans(sm, line, boldSpans, italicSpans);

			// External links
			parseExternalLinks(sm, line, "http://", managedSpans, localLinks,
					result);
			parseExternalLinks(sm, line, "https://", managedSpans, localLinks,
					result);
			parseExternalLinks(sm, line, "ftp://", managedSpans, localLinks,
					result);
			parseExternalLinks(sm, line, "mailto:", managedSpans, localLinks,
					result);

			// end of linewhise opperations
			lineSpans.removeFirst();
		}
		sm.removeManagedList(lineSpans);

		// Links
		int i;
		i = 0;
		while (i < cepp.linkSpans.size())
		{
			if (contentElementRange.hits(cepp.linkSpans.get(i)))
			{
				Span linkSpan = cepp.linkSpans.remove(i);
				managedSpans.add(linkSpan);
				Link l = cepp.links.remove(i).setHomeElement(result);
				localLinks.add(l);
				if (!showImageText && l.getType() == Link.type.IMAGE)
				{
					// deletes the Image Text from the ContentElement Text.
					sm.delete(linkSpan);
				}
			}
			else
			{
				i++;
			}
		}

		// Templates
		i = 0;
		while (i < cepp.templateSpans.size())
		{
			Span ts = cepp.templateSpans.get(i);
			if (contentElementRange.hits(ts))
			{
				ResolvedTemplate rt = cepp.templates.remove(i);

				if (rt.getPostParseReplacement() != null)
				{
					sm.replace(ts, rt.getPostParseReplacement());
				}
				cepp.templateSpans.remove(i);

				Object parsedObject = rt.getParsedObject();
				if (parsedObject != null)
				{
					managedSpans.add(ts);

					Class parsedObjectClass = parsedObject.getClass();
					if (parsedObjectClass == Template.class)
					{
						localTemplates.add((Template) parsedObject);
					}
					else if (parsedObjectClass == Link.class)
					{
						localLinks.add(((Link) parsedObject)
								.setHomeElement(result));
					}
					else
					{
						localTemplates.add(rt.getTemplate());
					}
				}
			}
			else
			{
				i++;
			}
		}

		// HTML/XML Tags
		i = 0;
		List<Span> tags = new ArrayList<Span>();
		while (i < cepp.tagSpans.size())
		{
			Span s = cepp.tagSpans.get(i);
			if (contentElementRange.hits(s))
			{
				cepp.tagSpans.remove(i);
				if (deleteTags)
				{
					sm.delete(s);
				}
				else
				{
					tags.add(s);
					managedSpans.add(s);
				}
			}
			else
			{
				i++;
			}
		}

		// noWiki
		i = 0;
		List<Span> localNoWikiSpans = new ArrayList<Span>();
		while (i < cepp.noWikiSpans.size())
		{
			Span s = cepp.noWikiSpans.get(i);
			if (contentElementRange.hits(s))
			{
				cepp.noWikiSpans.remove(i);
				sm.replace(s, cepp.noWikiStrings.remove(i));
				localNoWikiSpans.add(s);
				managedSpans.add(s);
			}
			else
			{
				i++;
			}
		}

		// MATH Tags
		i = 0;
		List<Span> mathSpans = new ArrayList<Span>();
		while (i < cepp.mathSpans.size())
		{
			Span s = cepp.mathSpans.get(i);
			if (contentElementRange.hits(s))
			{
				cepp.mathSpans.remove(i);

				if (showMathTagContent)
				{
					mathSpans.add(s);
					managedSpans.add(s);
					sm.replace(s, cepp.mathStrings.remove(i));
				}
				else
				{
					sm.delete(s);
				}
			}
			else
			{
				i++;
			}
		}

		result.setText(sm.substring(contentElementRange));

		// managed spans must be removed here and not earlier, because every
		// change in the SpanManager affects the Spans!
		sm.removeManagedList(boldSpans);
		sm.removeManagedList(italicSpans);
		sm.removeManagedList(managedSpans);

		// contentElementRange ist auch noch in managedSpans !!! deswegen:
		final int adjust = -contentElementRange.getStart();
		for (Span s : boldSpans)
		{
			s.adjust(adjust);
		}
		for (Span s : italicSpans)
		{
			s.adjust(adjust);
		}
		for (Span s : managedSpans)
		{
			s.adjust(adjust);
		}

		result.setFormatSpans(FormatType.BOLD, boldSpans);
		result.setFormatSpans(FormatType.ITALIC, italicSpans);
		result.setFormatSpans(FormatType.TAG, tags);
		result.setFormatSpans(FormatType.MATH, mathSpans);
		result.setFormatSpans(FormatType.NOWIKI, localNoWikiSpans);

		result.setLinks(sortLinks(localLinks));
		result.setTemplates(sortTemplates(localTemplates));

		return result;
	}

	/**
	 * Sorts the Links...
	 */
	private static List<Link> sortLinks(List<Link> links)
	{
		List<Link> result = new ArrayList<Link>();
		for (Link l : links)
		{
			int pos = 0;
			while (pos < result.size()
					&& l.getPos().getStart() > result.get(pos).getPos()
							.getStart())
			{
				pos++;
			}
			result.add(pos, l);
		}
		return result;
	}

	/**
	 * Sorts the Templates...
	 */
	private static List<Template> sortTemplates(List<Template> templates)
	{
		List<Template> result = new ArrayList<Template>();
		for (Template t : templates)
		{
			int pos = 0;
			while (pos < result.size()
					&& t.getPos().getStart() > result.get(pos).getPos()
							.getStart())
			{
				pos++;
			}
			result.add(pos, t);
		}
		return result;
	}

	/**
	 * Algorithm to identify the first paragraph of a ParsedPage
	 */
	private void setFirstParagraph(ParsedPage pp)
	{
		int nr = pp.nrOfParagraphs();

		// the paragraph with the lowest number, must not be the first, maybe it
		// is only an Image...
		for (int i = 0; i < nr; i++)
		{
			Paragraph p = pp.getParagraph(i);

			// get the Text from the paragraph
			SpanManager ptext = new SpanManager(p.getText());
			List<Span> delete = new ArrayList<Span>();
			ptext.manageList(delete);

			// getting the spans to remove from the text, for templates
			List<Template> tl = p.getTemplates();
			for (int j = tl.size() - 1; j >= 0; j--)
			{
				delete.add(tl.get(j).getPos());
			}

			// getting the spans to remove from the text, for Tags
			List<Span> sl = p.getFormatSpans(FormatType.TAG);
			for (int j = sl.size() - 1; j >= 0; j--)
			{
				delete.add(sl.get(j));
			}

			// getting the spans to remove from the text, for image text
			if (showImageText)
			{
				List<Link> ll = p.getLinks(Link.type.IMAGE);
				for (int j = ll.size() - 1; j >= 0; j--)
				{
					delete.add(ll.get(j).getPos());
				}
			}

			// delete the spans in reverse order, the spans are managed, so
			// there is no need to sort them
			for (int j = delete.size() - 1; j >= 0; j--)
			{
				ptext.delete(delete.remove(j));
			}

			// removing line separators if exist, so the result can be trimmed
			// in the next step
			int pos = ptext.indexOf(lineSeparator);
			while (pos != -1)
			{
				ptext.delete(pos, pos + lineSeparator.length());
				pos = ptext.indexOf(lineSeparator);
			}

			// if the result is not an empty string, we got the number of the
			// first paragraph
			if (!ptext.toString().trim().equals(""))
			{
				pp.setFirstParagraphNr(i);
				return;
			}
		}
	}

	/**
	 * Container for all the Parameters needed in the parseing process
	 *
	 * @author CJacobi
	 *
	 */
	class ContentElementParsingParameters
	{
		List<Span> noWikiSpans;
		List<String> noWikiStrings;
		List<Span> linkSpans;
		List<Link> links;
		List<Span> templateSpans;
		List<ResolvedTemplate> templates;
		List<Span> tagSpans;
		List<Span> mathSpans;
		List<String> mathStrings;

		ContentElementParsingParameters()
		{
			noWikiSpans = new ArrayList<Span>();
			noWikiStrings = new ArrayList<String>();
			linkSpans = new ArrayList<Span>();
			links = new ArrayList<Link>();
			templateSpans = new ArrayList<Span>();
			templates = new ArrayList<ResolvedTemplate>();
			tagSpans = new ArrayList<Span>();
			mathSpans = new ArrayList<Span>();
			mathStrings = new ArrayList<String>();
		}
	}
}
