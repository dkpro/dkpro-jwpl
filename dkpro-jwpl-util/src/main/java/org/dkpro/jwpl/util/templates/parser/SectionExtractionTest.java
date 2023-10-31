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
package org.dkpro.jwpl.util.templates.parser;

import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.WikiConstants;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.util.templates.parser.SectionExtractor.ExtractedSection;

public class SectionExtractionTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DatabaseConfiguration dbconf = new DatabaseConfiguration();
        dbconf.setDatabase("wiki_en_20120104_rev");
        dbconf.setUser("root");
        dbconf.setPassword("");
        dbconf.setHost("127.0.0.1:3307");
        dbconf.setLanguage(WikiConstants.Language.english);
        try {
            Wikipedia wiki = new Wikipedia(dbconf);
            Page p = wiki.getPage("Ari Sitas");

            List<ExtractedSection> sects = ParseUtils.getSections(p.getText(),
                    p.getTitle().toString(), -1);
            for (ExtractedSection sect : sects) {
                System.out.println(sect.getBody());
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
