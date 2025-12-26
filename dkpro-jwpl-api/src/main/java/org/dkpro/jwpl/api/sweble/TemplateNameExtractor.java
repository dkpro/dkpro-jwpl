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
package org.dkpro.jwpl.api.sweble;

/*
 * Derived from the TextConverter class which was published in the
 * Sweble example project provided on
 * http://http://sweble.org by the Open Source Research Group,
 * University of Erlangen-Nürnberg under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0)
 */

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtTemplate;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.AstText;

/**
 * A visitor that extracts template names (no parameters) from an article AST.
 */
public class TemplateNameExtractor
    extends AstVisitor<WtNode>
{
    private final WikiConfig config;

    private List<String> templates;

    // =========================================================================

    /**
     * Creates a new visitor that extracts anchors of internal links from a parsed Wikipedia article
     * using the default Sweble config as defined in {@link org.dkpro.jwpl.api.WikiConstants#SWEBLE_CONFIG}.
     */
    public TemplateNameExtractor()
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
    public TemplateNameExtractor(WikiConfig config)
    {
        this.config = config;
    }

    @Override
    protected WtNode before(WtNode node)
    {
        // This method is called by go() before visitation starts
        templates = new LinkedList<>();
        return super.before(node);
    }

    @Override
    protected Object after(WtNode node, Object result)
    {
        return templates;
    }

    // =========================================================================

    /**
     * Called when a {@link WtNode node} instance is processed.
     * @param n The node which is visited.
     */
    public void visit(WtNode n)
    {
        iterate(n);
    }

    /**
     * Called when a {@link WtTemplate template} instance is processed.
     * @param tmpl The template which is visited.
     *
     * @throws IOException Thrown if IO errors occurred.
     */
    public void visit(WtTemplate tmpl) throws IOException
    {
        for (AstNode<?> n : tmpl.getName()) {
            if (n instanceof AstText) {
                add(((AstText<?>) n).getContent());
            }
        }
    }

    private void add(String s)
    {
        s = s.replace("\n", "").replace("\r", "");
        if (s.trim().isEmpty()) {
            return;
        }
        templates.add(s);
    }

}
