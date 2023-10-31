/*
 * This file was derived from the TextConverter class which was published in the
 * Sweble example project provided on
 * http://http://sweble.org by the Open Source Research Group,
 * University of Erlangen-Nürnberg under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --- Modifications to the original file are licensed as state below ---
 *
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
package org.dkpro.jwpl.util.templates.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtDefinitionList;
import org.sweble.wikitext.parser.nodes.WtDefinitionListDef;
import org.sweble.wikitext.parser.nodes.WtDefinitionListTerm;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtLinkTarget;
import org.sweble.wikitext.parser.nodes.WtLinkTitle;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtPage;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlEmptyTag;
import org.sweble.wikitext.parser.nodes.WtXmlEndTag;
import org.sweble.wikitext.parser.nodes.WtXmlStartTag;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.AstText;

/**
 * A visitor that extracts sections from an article AST.
 */
public class SectionExtractor
    extends AstVisitor<WtNode>
{
    private final WikiConfig config;

    private List<ExtractedSection> sections;

    private StringBuilder bodyBuilder = new StringBuilder();
    private List<String> curTpls;
    private List<String> templatesToMark = null;
    private final String TEMPLATE_MARKER_PREFIX = "{{";
    private final String TEMPLATE_MARKER_SUFFIX = "}}";

    // =========================================================================

    /**
     * Creates a new visitor that extracts anchors of internal links from a parsed Wikipedia article
     * using the default Sweble config as defined in WikiConstants.SWEBLE_CONFIG.
     *
     * @param templatesToMark
     *            a list with templates names that are marked with placeholders in the section body
     */
    public SectionExtractor(List<String> templatesToMark)
    {
        this();
        this.templatesToMark = templatesToMark;
    }

    /**
     * Creates a new visitor that extracts anchors of internal links from a parsed Wikipedia article
     * using the default Sweble config as defined in WikiConstants.SWEBLE_CONFIG.
     */
    public SectionExtractor()
    {
        this.config = DefaultConfigEnWp.generate();
    }

    /**
     * Creates a new visitor that extracts anchors of internal links from a parsed Wikipedia
     * article.
     *
     * @param config
     *            the Sweble configuration
     */
    public SectionExtractor(WikiConfig config)
    {
        this.config = config;
    }

    @Override
    protected WtNode before(WtNode node)
    {
        // This method is called by go() before visitation starts
        sections = new ArrayList<>();
        curTpls = new ArrayList<>();
        return super.before(node);
    }

    @Override
    protected Object after(WtNode node, Object result)
    {
        return sections;
    }

    // =========================================================================

    public void visit(WtPage n)
    {
        iterate(n);
    }

    public void visit(WtWhitespace n)
    {
        bodyBuilder.append(" ");
    }

    public void visit(WtBold n)
    {
        iterate(n);
    }

    public void visit(WtItalics n)
    {
        iterate(n);
    }

    public void visit(WtExternalLink link)
    {
        iterate(link.getTitle());
    }

    public void visit(WtLinkTitle n)
    {
        iterate(n);
    }

    public void visit(WtLinkTarget n)
    {
        iterate(n);
    }

    public void visit(WtInternalLink link)
    {
        try {
            PageTitle page = PageTitle.make(config, link.getTarget().getAsString());
            if (page.getNamespace().equals(config.getNamespace("Category"))) {
                return;
            }
            else {
                String curLinkTitle = "";
                for (AstNode n : link.getTitle()) {
                    if (n instanceof AstText) {
                        curLinkTitle = ((AstText) n).getContent().trim();
                    }
                }
                if (curLinkTitle.isEmpty()) {
                    bodyBuilder.append(link.getTarget());
                }
                else {
                    bodyBuilder.append(curLinkTitle);
                }

            }
        }
        catch (LinkTargetException e) {
        }

    }

    public void visit(WtDefinitionList n)
    {
        iterate(n);
    }

    public void visit(WtDefinitionListTerm n)
    {
        iterate(n);
    }

    public void visit(WtDefinitionListDef n)
    {
        iterate(n);
    }

    public void visit(WtXmlStartTag n)
    {
    }

    public void visit(WtXmlEndTag n)
    {
    }

    public void visit(WtXmlEmptyTag n)
    {
    }

    public void visit(AstNode n)
    {
    }

    public void visit(WtNodeList n)
    {
        iterate(n);
    }

    public void visit(WtParagraph n)
    {
        iterate(n);
    }

    public void visit(WtTemplate tmpl) throws IOException
    {
        for (AstNode n : tmpl.getName()) {
            if (n instanceof AstText) {
                String s = ((AstText) n).getContent();
                s = s.replace("\n", "").replace("\r", "");
                if (!s.trim().isEmpty()) {
                    curTpls.add(s);
                    // if we set a list of templates we want to mark in the
                    // body, check the current one and mark it, if necessary
                    if (templatesToMark != null && containsIgnoreCase(templatesToMark, s)) {
                        bodyBuilder.append(TEMPLATE_MARKER_PREFIX);
                        bodyBuilder.append(s);
                        bodyBuilder.append(TEMPLATE_MARKER_SUFFIX);
                    }
                }
            }

        }
    }

    public void visit(AstText n)
    {
        bodyBuilder.append(n.getContent());
    }

    public void visit(WtSection sect) throws IOException
    {

        String title = null;

        for (AstNode n : sect.getBody()) {
            if (n instanceof AstText) {
                title = ((AstText) n).getContent();
            }
        }
        iterate(sect.getBody());

        sections.add(new ExtractedSection(title, bodyBuilder.toString().trim(), curTpls));
        bodyBuilder = new StringBuilder();
        curTpls = new ArrayList<>();
    }

    /**
     * Checks if a list of string contains a String while ignoring case
     *
     * @param stringlist
     *            a list of string
     * @param match
     *            the string to look for
     * @return true, if the list contains the string, false else
     */
    private boolean containsIgnoreCase(List<String> stringlist, String match)
    {
        for (String s : stringlist) {
            if (s.equalsIgnoreCase(match)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wraps title and body text of an extraction section
     */
    public static class ExtractedSection
    {
        private String title;
        private String body;
        private List<String> templates;

        public ExtractedSection(String title, String body, List<String> templates)
        {
            this.title = title;
            this.body = body;
            this.templates = templates;

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

        public List<String> getTemplates()
        {
            return templates;
        }

        public void setTemplates(List<String> templates)
        {
            this.templates = templates;
        }

        public void addTemplate(String tpl)
        {
            this.templates.add(tpl);
        }
    }
}
