package de.tudarmstadt.ukp.wikipedia.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

public class LinkAnchorExtractor
{

	private MediaWikiParser parser;

	public LinkAnchorExtractor(){
        MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
        parser = pf.createParser();
	}

	public LinkAnchorExtractor(Language lang){
        MediaWikiParserFactory pf = new MediaWikiParserFactory(lang);
        parser = pf.createParser();
	}

	public LinkAnchorExtractor(MediaWikiParser parser){
		this.parser=parser;
	}

	/**
	 * Note that this method only returns the anchors that are not equal to the page's title.
	 * Anchors might contain references to sections in an article in the form of "Page#Section".
	 * If you need the plain title, e.g. for checking whether the page exists in Wikipedia, the Title object can be used.
	 *
	 * @return A set of strings used as anchor texts in links pointing to that page.
	 * @throws WikiTitleParsingException
	 */
	public Set<String> getInlinkAnchors(Page page)
		throws WikiTitleParsingException
	{
		Set<String> inAnchors = new HashSet<String>();
		for (Page p : page.getInlinks()) {
			ParsedPage pp = parser.parse(p.getText());
			if (pp == null) {
				return inAnchors;
			}
			for (Link l : pp.getLinks()) {
				String pageTitle = page.getTitle().getPlainTitle();

				String anchorText = l.getText();
				if (l.getTarget().equals(pageTitle) && !anchorText.equals(pageTitle)) {
					inAnchors.add(anchorText);
				}
			}
		}
		return inAnchors;
	}

	/**
	 * Note that this method only returns the anchors that are not equal to the title of the page
	 * they are pointing to.
	 * Anchors might contain references to sections in an article in the form of "Page#Section".
	 * If you need the plain title, e.g. for checking whether the page exists in Wikipedia, the Title object can be used.
	 *
	 * @return A mapping from the page titles of links in that page to the anchor texts used in the
	 *         links.
	 * @throws WikiTitleParsingException
	 */
	public Map<String, Set<String>> getOutlinkAnchors(Page page)
		throws WikiTitleParsingException
	{
		Map<String, Set<String>> outAnchors = new HashMap<String, Set<String>>();
		ParsedPage pp = parser.parse(page.getText());
		if (pp == null) {
			return outAnchors;
		}
		for (Link l : pp.getLinks()) {
			if (l.getTarget().length() == 0) {
				continue;
			}

			String targetTitle = new Title(l.getTarget()).getPlainTitle();
			if (!l.getType().equals(Link.type.EXTERNAL) && !l.getType().equals(Link.type.IMAGE)
					&& !l.getType().equals(Link.type.AUDIO) && !l.getType().equals(Link.type.VIDEO)
					&& !targetTitle.contains(":")) // Wikipedia titles only contain colons if they
													// are categories or other meta data
			{
				String anchorText = l.getText();
				if (!anchorText.equals(targetTitle)) {
					Set<String> anchors;
					if (outAnchors.containsKey(targetTitle)) {
						anchors = outAnchors.get(targetTitle);
					}
					else {
						anchors = new HashSet<String>();
					}
					anchors.add(anchorText);
					outAnchors.put(targetTitle, anchors);
				}
			}
		}
		return outAnchors;
	}
}
