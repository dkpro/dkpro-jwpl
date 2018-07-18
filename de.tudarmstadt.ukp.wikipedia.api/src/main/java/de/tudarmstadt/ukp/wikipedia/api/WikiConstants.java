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
package de.tudarmstadt.ukp.wikipedia.api;

import com.neovisionaries.i18n.LanguageCode;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface WikiConstants {
    /**
     * Shortcut for System.getProperty("line.separator").
     */
    static final String LF = System.getProperty("line.separator");

    /**
     * The prefix that is added to page titles of discussion pages
     * Has to be the same as in wikipedia.datamachine:SingleDumpVersionJDKGeneric
     */
    static final String DISCUSSION_PREFIX = "Discussion:";

    /**
     * Configuration file for the Sweble parser
     */
    static final String SWEBLE_CONFIG = "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml";

    /**
     * Enumerates the languages for which Wikipedia APIs are available.
     * A Wikipedia object can be created using one of these languages.
     */
    // Languages should be lowercase and match the corresponding snowball stemmer names.
    public enum Language {
        abkhazian,
        afar,
        afrikaans,
        akan,
        albanian,
        alemannic,
        amharic,
        anglo_saxon,
        arabic,
        aragonese,
        armenian,
        aromanian,
        assamese,
        assyrian_neo_aramaic,
        asturian,
        avar,
        aymara,
        azeri,
        bambara,
        banyumasan,
        bashkir,
        basque,
        bavarian,
        belarusian,
        belarusian_tarashkevitsa,
        bengali,
        bihari,
        bishnupriya_manipuri,
        bislama,
        bosnian,
        breton,
        buginese,
        bulgarian,
        burmese,
        buryat_russia,
        cantonese,
        catalan,
        cebuano,
        central_bicolano,
        chamorro,
        chechen,
        cherokee,
        cheyenne,
        chichewa,
        chinese,
        choctaw,
        chuvash,
        classical_chinese,
        cornish,
        corsican,
        cree,
        crimean_tatar,
        croatian,
        czech,
        danish,
        divehi,
        dutch,
        dutch_low_saxon,
        dzongkha,
        emilian_romagnol,
        english,
        esperanto,
        estonian,
        ewe,
        faroese,
        fijian,
        finnish,
        franco_provencal_arpitan,
        french,
        friulian,
        fula,
        galician,
        georgian,
        german,
        gilaki,
        gothic,
        greek,
        greenlandic,
        guarani,
        gujarati,
        haitian,
        hakka,
        hausa,
        hawaiian,
        hebrew,
        herero,
        hindi,
        hiri_motu,
        hungarian,
        icelandic,
        ido,
        igbo,
        ilokano,
        indonesian,
        interlingua,
        interlingue,
        inuktitut,
        inupiak,
        irish,
        italian,
        japanese,
        javanese,
        kabyle,
        kalmyk,
        kannada,
        kanuri,
        kapampangan,
        kashmiri,
        kashubian,
        kazakh,
        khmer,
        kikuyu,
        kinyarwanda,
        kirghiz,
        kirundi,
        klingon,
        komi,
        kongo,
        korean,
        kuanyama,
        kurdish,
        ladino,
        lak,
        lao,
        latin,
        latvian,
        ligurian,
        limburgian,
        lingala,
        lithuanian,
        lojban,
        lombard,
        low_saxon,
        lower_sorbian,
        luganda,
        luxembourgish,
        macedonian,
        malagasy,
        malay,
        malayalam,
        maltese,
        manx,
        maori,
        marathi,
        marshallese,
        mazandarani,
        min_dong,
        min_nan,
        moldovan,
        mongolian,
        muscogee,
        nahuatl,
        nauruan,
        navajo,
        ndonga,
        neapolitan,
        nepali,
        newar_nepal_bhasa,
        norfolk,
        norman,
        northern_sami,
        norwegian_bokmal,
        norwegian_nynorsk,
        novial,
        occitan,
        old_church_slavonic,
        oriya,
        oromo,
        ossetian,
        pali,
        pangasinan,
        papiamentu,
        pashto,
        pennsylvania_german,
        persian,
        piedmontese,
        polish,
        portuguese,
        punjabi,
        quechua,
        ripuarian,
        romani,
        romanian,
        romansh,
        russian,
        samoan,
        samogitian,
        sango,
        sanskrit,
        sardinian,
        saterland_frisian,
        scots,
        scottish_gaelic,
        serbian,
        serbo_croatian,
        sesotho,
        shona,
        sichuan_yi,
        sicilian,
        simple_english,
        sindhi,
        sinhalese,
        slovak,
        slovenian,
        somali,
        spanish,
        sundanese,
        swahili,
        swati,
        swedish,
        tagalog,
        tahitian,
        tajik,
        tamil,
        tarantino,
        tatar,
        telugu,
        tetum,
        thai,
        tibetan,
        tigrinya,
        tok_pisin,
        tokipona,
        tongan,
        tsonga,
        tswana,
        tumbuka,
        turkish,
        turkmen,
        twi,
        udmurt,
        ukrainian,
        upper_sorbian,
        urdu,
        uyghur,
        uzbek,
        venda,
        venetian,
        vietnamese,
        volapuek,
        voro,
        walloon,
        waray_waray,
        welsh,
        west_flemish,
        west_frisian,
        wolof,
        wu,
        xhosa,
        yiddish,
        yoruba,
        zamboanga_chavacano,
        zazaki,
        zealandic,
        zhuang,
        zulu,
        _test;

        /**
         * Configures a language specific configuration for parsing wikipedia pages.
         * @return WikiConfig
         */
        public WikiConfig getWikiconfig(Language this) {
            WikiConfig config = DefaultConfigEnWp.generate();
            if (this != Language._test) {
                // We need to capitalize the language name otherwise the locale lib cannot find it.
                String langName = this.name().substring(0, 1).toUpperCase() + this.name().substring(1);
                try {
                    List<LanguageCode> langCodes = LanguageCode.findByName(langName);
                    if (!langCodes.isEmpty()) {
                        String langCode = langCodes.get(0).name();
                        return LanguageConfigGenerator.generateWikiConfig(langCode);
                    }
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    System.out.println(
                            String.format("Failed to create WikiConfig for language for %s, using default instead",
                                    langName)
                    );
                }
            }
            return config;
        }
    }
}
