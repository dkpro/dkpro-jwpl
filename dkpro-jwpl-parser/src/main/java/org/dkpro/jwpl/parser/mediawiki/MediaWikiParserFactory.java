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
package org.dkpro.jwpl.parser.mediawiki;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.WikiConstants.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for easy creation of a configured {@link MediaWikiParser}.
 */
public class MediaWikiParserFactory
{

    private static final Logger logger = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private Class<?> parserClass;
    private Class<?> templateParserClass;
    private String lineSeparator;
    private List<String> deleteTemplates;
    private List<String> parseTemplates;
    private List<String> categoryIdentifiers;
    private List<String> languageIdentifiers;
    private List<String> imageIdentifiers;
    private boolean showImageText;
    private boolean deleteTags;
    private boolean showMathTagContent;
    private boolean calculateSrcSpans;

    /**
     * Creates a new un-configured {@link MediaWikiParserFactory}.
     */
    public MediaWikiParserFactory()
    {
        initVariables();
        initLanguages();
    }

    /**
     * Creates a fully configured {@link MediaWikiParserFactory} for the specified
     * {@link Language}.<br>
     * Next step is {@link MediaWikiParserFactory#createParser()}.
     */
    public MediaWikiParserFactory(Language language)
    {
        initVariables();
        initLanguages();
        if (language.equals(Language.german)) {
            initGermanVariables();
        }
        else if (language.equals(Language.english)) {
            initEnglishVariables();
        }
        else {
            logger.warn("No language specific parser for '{}' available. Using default values.",
                    language);
        }
    }

    private void initVariables()
    {
        lineSeparator = "LF";
        parserClass = ModularParser.class;
        imageIdentifiers = new ArrayList<>();
        categoryIdentifiers = new ArrayList<>();
        languageIdentifiers = new ArrayList<>();
        deleteTemplates = new ArrayList<>();
        parseTemplates = new ArrayList<>();
        showImageText = false;
        deleteTags = true;
        showMathTagContent = true;
        calculateSrcSpans = false;
        templateParserClass = ShowTemplateNamesAndParameters.class;
    }

    private void initLanguages()
    {
        // Init the Languages...
        languageIdentifiers.addAll(List.of("aa","ab","af","am","an","ar","as",
                "av","ay","az","ba","be","bg","bh","bi","bm","bn","bo","br","bs","ca",
                "ce","ch","co","cr","cs","cv","cy","da","de","dk","dv","dz","ee","el",
                "en","eo","es","et","eu","fa","ff","fi","fj","fo","fr","fy","ga","gd",
                "gl","gn","gu","gv","ha","he","hi","hr","ht","hu","hy","ia","id","ie",
                "ig","ii","ik","io","is","it","iu","ja","jv","ka","kg","ki","kk","kl",
                "km","kn","ko","ks","ku","kv","kw","ky","la","lb","li","ln","lo","lt",
                "lv","mg","mh","mi","mk","ml","mn","mo","mr","ms","mt","my","na","nb",
                "ne","ng","nl","nn","no","nv","ny","oc","os","pa","pl","ps","pt","qu",
                "rm","rn","ro","ru","rw","sa","sc","sd","se","sg","sh","si","sk","sl",
                "sm","sn","so","sq","sr","ss","st","su","sv","sw","ta","te","tg","th",
                "ti","tk","tl","tn","to","tr","ts","tt","tw","ty","ug","uk","ur","uz",
                "ve","vi","vo","wa","wo","xh","yi","yo","za","zh","zu","als","ang",
                "arc","ast","bug","ceb","chr","chy","csb","frp","fur","got","haw",
                "ilo","jbo","ksh","lad","lmo","nah","nap","nds","nrm","pam","pap",
                "pdc","pih","pms","rmy","scn","sco","tet","tpi","tum","udm","vec",
                "vls","war","xal","simple"));
    }

    private void initGermanVariables()
    {
        templateParserClass = FlushTemplates.class;
        // deleteTemplates.add( "Prettytable" );
        // parseTemplates.add( "Dieser Artikel" );
        // parseTemplates.add( "Audio" );
        // parseTemplates.add( "Video" );
        imageIdentifiers.add("Bild");
        imageIdentifiers.add("Image");
        imageIdentifiers.add("Datei");
        categoryIdentifiers.add("Kategorie");
        languageIdentifiers.remove("de");
    }

    private void initEnglishVariables()
    {
        templateParserClass = FlushTemplates.class;

        imageIdentifiers.add("Image");
        imageIdentifiers.add("File");
        imageIdentifiers.add("media");
        categoryIdentifiers.add("Category");
        languageIdentifiers.remove("en");
    }

    private String resolveLineSeparator()
    {
        if (lineSeparator.equals("CRLF")) {
            return "\r\n";
        }
        if (lineSeparator.equals("LF")) {
            return "\n";
        }

        logger.error("LineSeparator is UNKNOWN: \"" + lineSeparator + "\"\n"
                + "Set LineSeparator to \"LF\" or \"CRLF\" for a Error free configuration");

        return lineSeparator;
    }

    /**
     * Creates a MediaWikiParser with the configurations which has been set.
     */
    public MediaWikiParser createParser()
    {
        logger.debug("Selected Parser: {}", parserClass);

        if (parserClass == ModularParser.class) {
            ModularParser mwgp = new ModularParser(
                    // resolveLineSeparator(),
                    "\n", languageIdentifiers, categoryIdentifiers, imageIdentifiers, showImageText,
                    deleteTags, showMathTagContent, calculateSrcSpans, null);

            StringBuilder sb = new StringBuilder();
            sb.append(lineSeparator).append("languageIdentifiers: ");
            for (String s : languageIdentifiers) {
                sb.append(s).append(" ");
            }
            sb.append(lineSeparator).append("categoryIdentifiers: ");
            for (String s : categoryIdentifiers) {
                sb.append(s).append(" ");
            }
            sb.append(lineSeparator).append("imageIdentifiers: ");
            for (String s : imageIdentifiers) {
                sb.append(s).append(" ");
            }
            logger.debug(sb.toString());

            MediaWikiTemplateParser mwtp;

            logger.debug("Selected TemplateParser: {}", templateParserClass);
            if (templateParserClass == GermanTemplateParser.class) {
                for (String s : deleteTemplates) {
                    logger.debug("DeleteTemplate: '{}'", s);
                }
                for (String s : parseTemplates) {
                    logger.debug("ParseTemplate: '{}'", s);
                }
                mwtp = new GermanTemplateParser(mwgp, deleteTemplates, parseTemplates);
            }
            else if (templateParserClass == FlushTemplates.class) {
                mwtp = new FlushTemplates();
            }
            else if (templateParserClass == ShowTemplateNamesAndParameters.class) {
                mwtp = new ShowTemplateNamesAndParameters();
            }
            else {
                logger.error("TemplateParser Class Not Found!");
                return null;
            }

            mwgp.setTemplateParser(mwtp);

            return mwgp;
        }
        else {
            logger.error("Parser Class Not Found!");
            return null;
        }
    }

    /**
     * Adds a Template which should be deleted while the parsing process.
     */
    public void addDeleteTemplate(String deleteTemplate)
    {
        deleteTemplates.add(deleteTemplate);
    }

    /**
     * Adds a Template which should be "parsed" while the parsing process.
     */
    public void addParseTemplate(String parseTemplate)
    {
        parseTemplates.add(parseTemplate);
    }

    /**
     * Retuns the Class of the selected Parser.
     */
    public Class<?> getParserClass()
    {
        return parserClass;
    }

    /**
     * Set the Parser which should be configured and returned by createParser().
     */
    public void setParserClass(Class<?> parserClass)
    {
        this.parserClass = parserClass;
    }

    /**
     * Returns the Class of the selected TemplateParser.
     */
    public Class<?> getTemplateParserClass()
    {
        return templateParserClass;
    }

    /**
     * Set the Parser which should be used for Template parsing.
     */
    public void setTemplateParserClass(Class<?> templateParserClass)
    {
        this.templateParserClass = templateParserClass;
    }

    /**
     * Returns the List of templates which should be deleted in the parsing process.
     */
    public List<String> getDeleteTemplates()
    {
        return deleteTemplates;
    }

    /**
     * Set the List of templates which should be deleted in the parsing process.
     */
    public void setDeleteTemplates(List<String> deleteTemplates)
    {
        this.deleteTemplates = deleteTemplates;
    }

    /**
     * Returns the CharSequence/String which should be used as line separator.
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }

    /**
     * Sets the CharSequence/String which should be used as line separator.
     */
    public void setLineSeparator(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns the List of templates which should be "parsed" in the parsing process.
     */
    public List<String> getParseTemplates()
    {
        return parseTemplates;
    }

    /**
     * Sets the List of templates which should be "parsed" in the parsing process.
     */
    public void setParseTemplates(List<String> parseTemplates)
    {
        this.parseTemplates = parseTemplates;
    }

    /**
     * Returns the List of Strings which are used to specify that a link is a link to a wikipedia i
     * another language.
     */
    public List<String> getLanguageIdentifiers()
    {
        return languageIdentifiers;
    }

    /**
     * Sets the list of language identifiers.
     */
    public void setLanguageIdentifiers(List<String> languageIdentifiers)
    {
        this.languageIdentifiers = languageIdentifiers;
    }

    /**
     * Returns the List of Strings which are used to specify that a link is a link to a category.
     * E.g. in german "Kategorie" is used. But it could be useful to use more than one identifier,
     * mainly the english identifier "category" should be used too.
     */
    public List<String> getCategoryIdentifiers()
    {
        return categoryIdentifiers;
    }

    /**
     * Set the list of category identifiers.
     */
    public void setCategoryIdentifiers(List<String> categoryIdentifiers)
    {
        this.categoryIdentifiers = categoryIdentifiers;
    }

    /**
     * Returns the List of Strings which are used to specify that a link is an Image.
     */
    public List<String> getImageIdentifiers()
    {
        return imageIdentifiers;
    }

    /**
     * Sets the image identifier list.
     */
    public void setImageIdentifiers(List<String> imageIdentifiers)
    {
        this.imageIdentifiers = imageIdentifiers;
    }

    /**
     * Returns if the Parser should show the Text of an Image, or delete it. If the Text is deleted,
     * it will be added as a Parameter to the Link.
     *
     * @return {@code true}, if the Text should be shown.
     */
    public boolean getShowImageText()
    {
        return showImageText;
    }

    /**
     * Sets if the Parser should show the Text of an Image, or delete it.
     */
    public void setShowImageText(boolean showImageText)
    {
        this.showImageText = showImageText;
    }

    /**
     * Returns if &lt; * &gt; tags should be deleted or annotated.
     *
     * @return {@code true}, if the tags should be deleted.
     */
    public boolean getDeleteTags()
    {
        return deleteTags;
    }

    /**
     * Sets if &lt; * &gt; tags should be deleted or annotated.
     */
    public void setDeleteTags(boolean deleteTags)
    {
        this.deleteTags = deleteTags;
    }

    /**
     * Retruns if the Content of math tags (&lt;math&gt;&lt;CONTENT/math&gt;) should be deleted or
     * annotated.
     *
     * @return {@code true}, if the tag content should be annotated.
     */
    public boolean getShowMathTagContent()
    {
        return showMathTagContent;
    }

    /**
     * Set if the Content of math tags should be deleted or annotated.
     */
    public void setShowMathTagContent(boolean showMathTagContent)
    {
        this.showMathTagContent = showMathTagContent;
    }

    /**
     * Returns if the Parser should calculate the positions in the original source of the elements
     * which are parsed.
     *
     * @return {@code true}, if the positions should be calculated.
     */
    public boolean getCalculateSrcSpans()
    {
        return calculateSrcSpans;
    }

    /**
     * Sets if the Parser should calculate the positions in the original source of the elements
     * which are parsed.
     */
    public void setCalculateSrcSpans(boolean calculateSrcSpans)
    {
        this.calculateSrcSpans = calculateSrcSpans;
    }
}
