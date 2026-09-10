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
package org.dkpro.jwpl.api;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

import com.neovisionaries.i18n.LanguageCode;

public interface WikiConstants
{
    /**
     * Shortcut for System.getProperty("line.separator").
     */
    String LF = System.getProperty("line.separator");

    /**
     * The prefix that is added to page titles of discussion pages Has to be the same as in
     * wikipedia.datamachine:SingleDumpVersionJDKGeneric
     */
    String DISCUSSION_PREFIX = "Discussion:";

    /**
     * Configuration file for the Sweble parser
     */
    String SWEBLE_CONFIG = "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml";

    /**
     * Enumerates the languages for which Wikipedia APIs are available. A Wikipedia object can be
     * created using one of these languages.
     * <p>
     * Each language carries the code of its Wikipedia edition, see {@link #getWikiCode()}. The
     * codes are taken from the
     * <a href="https://meta.wikimedia.org/wiki/List_of_Wikipedias">List of Wikipedias</a> and
     * are not looked up at runtime (see issue #53).
     */
    // Languages should be lowercase and match the corresponding snowball stemmer names.
    enum Language
    {
        abkhazian("ab"), afar("aa"), afrikaans("af"), akan("ak"), albanian("sq"), alemannic("als"),
        amharic("am"), anglo_saxon("ang"), arabic("ar"), aragonese("an"), armenian("hy"),
        aromanian("roa-rup"), assamese("as"), assyrian_neo_aramaic("arc"), asturian("ast"),
        avar("av"), aymara("ay"), azeri("az"), bambara("bm"), banyumasan("map-bms"), bashkir("ba"),
        basque("eu"), bavarian("bar"), belarusian("be"), belarusian_tarashkevitsa("be-tarask"),
        bengali("bn"), bihari("bh"), bishnupriya_manipuri("bpy"), bislama("bi"), bosnian("bs"),
        breton("br"), buginese("bug"), bulgarian("bg"), burmese("my"), buryat_russia("bxr"),
        cantonese("zh-yue"), catalan("ca"), cebuano("ceb"), central_bicolano("bcl"), chamorro("ch"),
        chechen("ce"), cherokee("chr"), cheyenne("chy"), chichewa("ny"), chinese("zh"),
        choctaw("cho"), chuvash("cv"), classical_chinese("zh-classical"), cornish("kw"),
        corsican("co"), cree("cr"), crimean_tatar("crh"), croatian("hr"), czech("cs"), danish("da"),
        divehi("dv"), dutch("nl"), dutch_low_saxon("nds-nl"), dzongkha("dz"),
        emilian_romagnol("eml"), english("en"), esperanto("eo"), estonian("et"), ewe("ee"),
        faroese("fo"), fijian("fj"), finnish("fi"), franco_provencal_arpitan("frp"), french("fr"),
        friulian("fur"), fula("ff"), galician("gl"), georgian("ka"), german("de"), gilaki("glk"),
        gothic("got"), greek("el"), greenlandic("kl"), guarani("gn"), gujarati("gu"), haitian("ht"),
        hakka("hak"), hausa("ha"), hawaiian("haw"), hebrew("he"), herero("hz"), hindi("hi"),
        hiri_motu("ho"), hungarian("hu"), icelandic("is"), ido("io"), igbo("ig"), ilokano("ilo"),
        indonesian("id"), interlingua("ia"), interlingue("ie"), inuktitut("iu"), inupiak("ik"),
        irish("ga"), italian("it"), japanese("ja"), javanese("jv"), kabyle("kab"), kalmyk("xal"),
        kannada("kn"), kanuri("kr"), kapampangan("pam"), kashmiri("ks"), kashubian("csb"),
        kazakh("kk"), khmer("km"), kikuyu("ki"), kinyarwanda("rw"), kirghiz("ky"), kirundi("rn"),
        klingon("tlh"), komi("kv"), kongo("kg"), korean("ko"), kuanyama("kj"), kurdish("ku"),
        ladino("lad"), lak("lbe"), lao("lo"), latin("la"), latvian("lv"), ligurian("lij"),
        limburgian("li"), lingala("ln"), lithuanian("lt"), lojban("jbo"), lombard("lmo"),
        low_saxon("nds"), lower_sorbian("dsb"), luganda("lg"), luxembourgish("lb"),
        macedonian("mk"), malagasy("mg"), malay("ms"), malayalam("ml"), maltese("mt"), manx("gv"),
        maori("mi"), marathi("mr"), marshallese("mh"), mazandarani("mzn"), min_dong("cdo"),
        min_nan("zh-min-nan"), moldovan("mo"), mongolian("mn"), muscogee("mus"), nahuatl("nah"),
        nauruan("na"), navajo("nv"), ndonga("ng"), neapolitan("nap"), nepali("ne"),
        newar_nepal_bhasa("new"), norfolk("pih"), norman("nrm"), northern_sami("se"),
        norwegian_bokmal("no"), norwegian_nynorsk("nn"), novial("nov"), occitan("oc"),
        old_church_slavonic("cu"), oriya("or"), oromo("om"), ossetian("os"), pali("pi"),
        pangasinan("pag"), papiamentu("pap"), pashto("ps"), pennsylvania_german("pdc"),
        persian("fa"), piedmontese("pms"), polish("pl"), portuguese("pt"), punjabi("pa"),
        quechua("qu"), ripuarian("ksh"), romani("rmy"), romanian("ro"), romansh("rm"),
        russian("ru"), samoan("sm"), samogitian("bat-smg"), sango("sg"), sanskrit("sa"),
        sardinian("sc"), saterland_frisian("stq"), scots("sco"), scottish_gaelic("gd"),
        serbian("sr"), serbo_croatian("sh"), sesotho("st"), shona("sn"), sichuan_yi("ii"),
        sicilian("scn"), simple_english("simple"), sindhi("sd"), sinhalese("si"), slovak("sk"),
        slovenian("sl"), somali("so"), spanish("es"), sundanese("su"), swahili("sw"), swati("ss"),
        swedish("sv"), tagalog("tl"), tahitian("ty"), tajik("tg"), tamil("ta"),
        tarantino("roa-tara"), tatar("tt"), telugu("te"), tetum("tet"), thai("th"), tibetan("bo"),
        tigrinya("ti"), tok_pisin("tpi"), tokipona("tok"), tongan("to"), tsonga("ts"), tswana("tn"),
        tumbuka("tum"), turkish("tr"), turkmen("tk"), twi("tw"), udmurt("udm"), ukrainian("uk"),
        upper_sorbian("hsb"), urdu("ur"), uyghur("ug"), uzbek("uz"), venda("ve"), venetian("vec"),
        vietnamese("vi"), volapuek("vo"), voro("fiu-vro"), walloon("wa"), waray_waray("war"),
        welsh("cy"), west_flemish("vls"), west_frisian("fy"), wolof("wo"), wu("wuu"), xhosa("xh"),
        yiddish("yi"), yoruba("yo"), zamboanga_chavacano("cbk-zam"), zazaki("diq"),
        zealandic("zea"), zhuang("za"), zulu("zu"), _test("test");

        private static final Logger logger = LoggerFactory
                .getLogger(MethodHandles.lookup().lookupClass());

        private static final Map<String, Language> BY_WIKI_CODE = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(Language::getWikiCode, Function.identity()));

        private final String wikiCode;

        Language(String wikiCode)
        {
            this.wikiCode = wikiCode;
        }

        /**
         * Returns the code of the Wikipedia edition in this language, which is the sub domain the
         * edition is served from, as {@code en} in {@code en.wikipedia.org}. The dumps of an
         * edition are named after the code as well, with hyphens replaced by underscores, as
         * {@code zh_min_nanwiki} for {@code zh-min-nan}.
         * <p>
         * An edition that was closed, as {@code kl}, or deleted, as {@code mo} and {@code tlh},
         * keeps the code it was served from. {@link #_test} is mapped to the test wiki,
         * {@code test.wikipedia.org}.
         *
         * @return The code of the Wikipedia edition in this language, never {@code null}.
         */
        public String getWikiCode()
        {
            return wikiCode;
        }

        /**
         * Looks up the language of a Wikipedia edition by the code of the edition.
         *
         * @param wikiCode
         *            The code of a Wikipedia edition, as {@code en} or {@code zh-min-nan}. Case and
         *            surrounding blanks are ignored. May be {@code null}.
         * @return The language of the edition, or {@code null} if no language has that code.
         * @see #getWikiCode()
         */
        public static Language fromWikiCode(String wikiCode)
        {
            if (wikiCode == null) {
                return null;
            }
            return BY_WIKI_CODE.get(wikiCode.trim().toLowerCase(Locale.ROOT));
        }

        /**
         * Configures a language specific configuration for parsing wikipedia pages.
         *
         * @return WikiConfig
         */
        public WikiConfig getWikiconfig(Language this)
        {
            WikiConfig config = DefaultConfigEnWp.generate();
            if (this != Language._test) {
                // We need to capitalize the language name otherwise the locale lib cannot find it.
                String langName = this.name().substring(0, 1).toUpperCase()
                        + this.name().substring(1);
                try {
                    List<LanguageCode> langCodes = LanguageCode.findByName(langName);
                    if (!langCodes.isEmpty()) {
                        String langCode = langCodes.get(0).name();
                        return LanguageConfigGenerator.generateWikiConfig(langCode);
                    }
                }
                catch (IOException | ParserConfigurationException | SAXException e) {
                    logger.warn("Failed to create WikiConfig for language for {}, "
                            + "using default instead", langName, e);
                }
            }
            return config;
        }
    }
}
