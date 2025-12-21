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
package org.dkpro.jwpl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the Simple implementation of the Content Interface, and is used for nearly all content
 * containing classes...
 * <p>
 * Be aware, that all returned Spans refer to the String returned by getText()<br>
 */
public class ContentElement
    extends ParsedPageObject
    implements Content
{

    private String text;
    private List<Span> boldSpans;
    private List<Span> italicSpans;
    private List<Link> links;
    private List<Template> templates;
    private List<Span> tags;
    private List<Span> mathSpans;
    private List<Span> noWikiSpans;

    public ContentElement()
    {
        text = "";
        links = new ArrayList<>();
        templates = new ArrayList<>();
        boldSpans = new ArrayList<>();
        italicSpans = new ArrayList<>();
        tags = new ArrayList<>();
        mathSpans = new ArrayList<>();
        noWikiSpans = new ArrayList<>();
    }

    /**
     * See {@link #getText()} for details.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * @return The Text on which all elements of this ContentElement are bases on.
     */
    @Override
    public String getText()
    {
        return text;
    }

    /**
     * Returns the Text defined with the Spans in the List divided by a WS
     */
    public String getText(List<Span> sl)
    {
        StringBuilder result = new StringBuilder();

        for (Span s : sl) {
            result.append(s.getText(text) + ' ');
        }
        int delChar = result.length() - 1;
        if (delChar > 0)
            result.deleteCharAt(delChar);

        return result.toString();
    }

    /**
     * @return the length of the Text. Alternatively, you can use getText().length()
     */
    @Override
    public int length()
    {
        return text.length();
    }

    /**
     * @return {@code true} if there is no Content in this ContentElement.
     */
    @Override
    public boolean empty()
    {
        return text.length() == 0 && links.size() == 0 && templates.size() == 0 && tags.size() == 0
                && mathSpans.size() == 0;
    }

    /**
     * Look at getFormatSpans for Details...
     */
    public void setFormatSpans(FormatType t, List<Span> spans)
    {
        switch (t) {
        case BOLD:
            boldSpans = spans;
            break;

        case ITALIC:
            italicSpans = spans;
            break;

        case TAG:
            tags = spans;
            break;

        case MATH:
            mathSpans = spans;
            break;

        case NOWIKI:
            noWikiSpans = spans;
            break;
        }
    }

    /**
     * @return all the Spans of the Format type t.
     */
    @Override
    public List<Span> getFormatSpans(FormatType t)
    {
        return switch (t) {
            case BOLD -> boldSpans;
            case ITALIC -> italicSpans;
            case TAG -> tags;
            case MATH -> mathSpans;
            case NOWIKI -> noWikiSpans;
            default -> null;
        };
    }

    /**
     * @return all the Spans of the Format type t in the Range of start to end
     */
    @Override
    public List<Span> getFormatSpans(FormatType t, int start, int end)
    {
        return getFormatSpans(t, new Span(start, end));
    }

    /**
     * @return all the Spans of the Format type t in the Range of the Span s
     */
    @Override
    public List<Span> getFormatSpans(FormatType t, Span s)
    {
        List<Span> result = new ArrayList<>();
        for (Span s2 : getFormatSpans(t))
            if (s2.hits(s))
                result.add(s2);
        return result;
    }

    /**
     * @return the Formats which are used in this ContentElement in a List.
     */
    @Override
    public List<FormatType> getFormats()
    {
        List<FormatType> ftl = new ArrayList<>();
        if (boldSpans.size() != 0)
            ftl.add(FormatType.BOLD);
        if (italicSpans.size() != 0)
            ftl.add(FormatType.ITALIC);
        if (tags.size() != 0)
            ftl.add(FormatType.TAG);
        if (mathSpans.size() != 0)
            ftl.add(FormatType.MATH);
        if (noWikiSpans.size() != 0)
            ftl.add(FormatType.NOWIKI);
        return ftl;
    }

    /**
     * @return The Formats which are used in this ContentElement, in the Range from start to end, in
     * a List.
     */
    @Override
    public List<FormatType> getFormats(int start, int end)
    {
        return getFormats(new Span(start, end));
    }

    /**
     * @return The Formats which are used in this ContentElement, in the Range of the Span s, in a
     * List.
     */
    @Override
    public List<FormatType> getFormats(Span s)
    {
        List<FormatType> result = new ArrayList<>();
        for (Span s2 : boldSpans)
            if (s.hits(s2)) {
                result.add(FormatType.BOLD);
                break;
            }

        for (Span s2 : italicSpans)
            if (s.hits(s2)) {
                result.add(FormatType.ITALIC);
                break;
            }

        return result;
    }

    /**
     * See {@link #getLinks()} for details.
     */
    public void setLinks(List<Link> links)
    {
        this.links = links;
    }

    /**
     * @return A List of the links of this ContentElement
     */
    @Override
    public List<Link> getLinks()
    {
        return links;
    }

    /**
     * @return A List of links of this ContentElement for the specified {@link Link.type t}
     */
    @Override
    public List<Link> getLinks(Link.type t)
    {
        List<Link> result = new ArrayList<>();
        for (Link l : links)
            if (l.getType() == t)
                result.add(l);
        return result;
    }

    /**
     * @return A List of links of this ContentElement for the specified {@link Link.type t}
     * in the range of {@code s}.
     */
    @Override
    public List<Link> getLinks(Link.type t, Span s)
    {
        List<Link> result = new ArrayList<>();
        for (Link l : links)
            if (l.getType() == t && l.getPos().hits(s))
                result.add(l);
        return result;
    }

    /**
     * @return A list of the links of this ContentElement of the specified {@link Link.type t}
     * in the range of {@code begin} to {@code end}.
     */
    @Override
    public List<Link> getLinks(Link.type t, int begin, int end)
    {
        return getLinks(t, new Span(begin, end));
    }

    /**
     * Look at getTemplates for Details...
     */
    public void setTemplates(List<Template> templates)
    {
        this.templates = templates;
    }

    /**
     * @return A list of {@link Template templates} of this ContentElement.
     */
    @Override
    public List<Template> getTemplates()
    {
        return templates;
    }

    /**
     * @return A list of {@link Template templates} of this ContentElement in the Range from start to end
     */
    @Override
    public List<Template> getTemplates(int start, int end)
    {
        return getTemplates(new Span(start, end));
    }

    /**
     * @return A list of {@link Template templates} of this ContentElement in the Range of s
     */
    @Override
    public List<Template> getTemplates(Span s)
    {
        List<Template> result = new ArrayList<>();
        for (Template t : templates)
            if (t.getPos().hits(s))
                result.add(t);
        return result;
    }

    /**
     * Try and find out ;-)
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("CE_TEXT: \"" + text + "\"");

        result.append("\nCE_BOLD_SPANS: ");
        if (boldSpans != null) {
            result.append(boldSpans.size());
            for (Span s : boldSpans)
                result.append("\n\t" + s + " : \"" + s.getText(text) + "\"");
        }
        else
            result.append("ERROR: boldSpans == null");

        result.append("\nCE_ITALIC_SPANS: ");
        if (italicSpans != null) {
            result.append(italicSpans.size());
            for (Span s : italicSpans)
                result.append("\n\t" + s + " : \"" + s.getText(text) + "\"");
        }
        else
            result.append("ERROR: italicSpans == null");

        result.append("\nCE_LINKS: ");
        if (links != null) {
            result.append(links.size());
            for (Link l : links)
                result.append("\n" + l);
        }
        else
            result.append("ERROR: links == null");

        result.append("\nCE_TEMPLATES: ");
        if (templates != null) {
            result.append(templates.size());
            for (Template t : templates)
                result.append("\n" + t);
        }
        else
            result.append("ERROR: templates == null");

        result.append("\nCE_TAGS: ");
        if (templates != null) {
            result.append(tags.size());
            for (Span s : tags)
                result.append("\n" + s);
        }
        else
            result.append("ERROR: templates == null");

        return result.toString();
    }
}
