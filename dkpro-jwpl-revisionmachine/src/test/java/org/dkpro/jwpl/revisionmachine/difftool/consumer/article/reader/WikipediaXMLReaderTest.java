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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.article.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WikipediaXMLReaderTest
{

    private static final String SITEINFO = "<mediawiki><siteinfo><namespaces>"
            + "<namespace key=\"0\" case=\"first-letter\" />"
            + "<namespace key=\"1\" case=\"first-letter\">Talk</namespace>"
            + "<namespace key=\"4\" case=\"first-letter\">Wikipedia</namespace>"
            + "</namespaces></siteinfo>";

    @BeforeAll
    public static void setUpConfiguration()
    {
        ConfigSettings settings = new ConfigSettings();
        settings.defaultConfiguration();
        new ConfigurationManager(settings);
    }

    @Test
    public void testReadsNamespaceFromNsElement() throws Exception
    {
        String xml = SITEINFO + page("Main Page", "0", 1) + page("Talk:Main Page", "1", 2)
                + page("Wikipedia:About", "4", 3) + "</mediawiki>";

        assertEquals(List.of(0, 1, 4), readNamespaces(xml));
    }

    @Test
    public void testDerivesNamespaceFromTitlePrefixWithoutNsElement() throws Exception
    {
        // Older dumps have no <ns> element, so the prefix of the title has to do.
        String xml = SITEINFO + page("Main Page", null, 1) + page("Talk:Main Page", null, 2)
                + page("Wikipedia:About", null, 3) + "</mediawiki>";

        assertEquals(List.of(0, 1, 4), readNamespaces(xml));
    }

    /**
     * Creates a page with a single revision, laid out like in the dumps: the reader relies on the
     * whitespace between the elements.
     */
    private static String page(String title, String namespace, int id)
    {
        return "\n  <page>\n    <title>" + title + "</title>\n"
                + (namespace == null ? "" : "    <ns>" + namespace + "</ns>\n")
                + "    <id>" + id + "</id>\n"
                + "    <revision>\n      <id>" + (id * 10) + "</id>\n"
                + "      <timestamp>2009-03-16T01:13:23Z</timestamp>\n"
                + "      <contributor>\n        <username>Someone</username>\n"
                + "        <id>177</id>\n      </contributor>\n"
                + "      <text xml:space=\"preserve\">Some text</text>\n"
                + "    </revision>\n  </page>";
    }

    private static List<Integer> readNamespaces(String xml) throws Exception
    {
        WikipediaXMLReader reader = new WikipediaXMLReader(new StringReader(xml));
        List<Integer> namespaces = new ArrayList<>();
        while (reader.hasNext()) {
            Task<Revision> task = reader.next();
            namespaces.add(task.getHeader().getNamespace());
        }
        return namespaces;
    }
}
