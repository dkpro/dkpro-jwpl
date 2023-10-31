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
package org.dkpro.jwpl.tutorial.parser;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.parser.mediawiki.FlushTemplates;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParser;
import org.dkpro.jwpl.parser.mediawiki.MediaWikiParserFactory;

/**
 * Shows how to clean an article text from "TEMPLATE" and "Image" elements
 */

public class T5_CleaningTemplateImage
{

    public static void main(String[] args) throws WikiApiException
    {

        // db connection settings
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDatabase("DATABASE");
        dbConfig.setHost("HOST");
        dbConfig.setUser("USER");
        dbConfig.setPassword("PASSWORD");
        dbConfig.setLanguage(Language.english);

        // initialize a wiki
        Wikipedia wiki = new Wikipedia(dbConfig);

        // get the page 'Dog'
        Page p = wiki.getPage("Dog");

        // get a ParsedPage object
        MediaWikiParserFactory pf = new MediaWikiParserFactory();
        pf.setTemplateParserClass(FlushTemplates.class); // Filtering TEMPLATE-Elements

        String IMAGE = "Image"; // Replace it with the image template name in your Wiki language
                                // edition,
        // e.g. "Image" in English

        // filtering Image-Elements
        pf.getImageIdentifers().add(IMAGE);

        // parse page text
        MediaWikiParser parser = pf.createParser();
        ParsedPage pp = parser.parse(p.getText());

        System.out.println(pp.getText());
    }
}
