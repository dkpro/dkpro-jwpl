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
     * There is a language for each edition on the
     * <a href="https://meta.wikimedia.org/wiki/List_of_Wikipedias">List of Wikipedias</a>, and
     * each carries the code of its edition, see {@link #getWikiCode()}. Languages and codes are
     * taken from that list and are not looked up at runtime (see issue #53).
     */
    // Languages should be lowercase and match the corresponding snowball stemmer names.
    enum Language
    {
        abkhazian("ab"), acehnese("ace"), adyghe("ady"), afar("aa"), afrikaans("af"), akan("ak"),
        albanian("sq"), alemannic("als"), amharic("am"), amis("ami"), angika("anp"),
        anglo_saxon("ang"), arabic("ar"), aragonese("an"), arakanese("rki"), armenian("hy"),
        aromanian("roa-rup"), assamese("as"), assyrian_neo_aramaic("arc"), asturian("ast"),
        atayal("tay"), atikamekw("atj"), avar("av"), awadhi("awa"), aymara("ay"), azeri("az"),
        balinese("ban"), bambara("bm"), banjar("bjn"), banyumasan("map-bms"), bashkir("ba"),
        basque("eu"), batak_mandailing("btm"), batak_toba("bbc"), bavarian("bar"), belarusian("be"),
        belarusian_tarashkevitsa("be-tarask"), bengali("bn"), betawi("bew"), bihari("bh"),
        bishnupriya_manipuri("bpy"), bislama("bi"), bole("bol"), bosnian("bs"), breton("br"),
        buginese("bug"), bulgarian("bg"), burmese("my"), buryat_russia("bxr"), cantonese("zh-yue"),
        catalan("ca"), cebuano("ceb"), central_bicolano("bcl"), central_dusun("dtp"),
        central_kanuri("knc"), central_kurdish("ckb"), chamorro("ch"), chechen("ce"),
        cherokee("chr"), cheyenne("chy"), chichewa("ny"), chinese("zh"), choctaw("cho"),
        chuvash("cv"), classical_chinese("zh-classical"), cornish("kw"), corsican("co"), cree("cr"),
        crimean_tatar("crh"), croatian("hr"), czech("cs"), dagbani("dag"), danish("da"),
        dinka("din"), divehi("dv"), doteli("dty"), dutch("nl"), dutch_low_saxon("nds-nl"),
        dzongkha("dz"), eastern_mari("mhr"), egyptian_arabic("arz"), emilian_romagnol("eml"),
        english("en"), erzya("myv"), esperanto("eo"), estonian("et"), ewe("ee"),
        extremaduran("ext"), fanti("fat"), faroese("fo"), fiji_hindi("hif"), fijian("fj"),
        finnish("fi"), fon("fon"), frafra("gur"), franco_provencal_arpitan("frp"), french("fr"),
        friulian("fur"), fula("ff"), gagauz("gag"), galician("gl"), gan("gan"), georgian("ka"),
        german("de"), ghanaian_pidgin("gpe"), gilaki("glk"), goan_konkani("gom"), gorontalo("gor"),
        gothic("got"), greek("el"), greenlandic("kl"), guarani("gn"), guianan_creole("gcr"),
        gujarati("gu"), gun("guw"), haitian("ht"), hakka("hak"), hausa("ha"), hawaiian("haw"),
        hebrew("he"), herero("hz"), hindi("hi"), hiri_motu("ho"), hungarian("hu"), iban("iba"),
        icelandic("is"), ido("io"), igala("igl"), igbo("ig"), ilokano("ilo"), inari_sami("smn"),
        indonesian("id"), ingush("inh"), interlingua("ia"), interlingue("ie"), interslavic("isv"),
        inuktitut("iu"), inupiak("ik"), irish("ga"), italian("it"), jamaican_creole_english("jam"),
        japanese("ja"), javanese("jv"), jju("kaj"), kabardian("kbd"), kabiye("kbp"), kabyle("kab"),
        kalmyk("xal"), kannada("kn"), kanuri("kr"), kapampangan("pam"), kara_kalpak("kaa"),
        karachay_balkar("krc"), karekare("kai"), kashmiri("ks"), kashubian("csb"), kazakh("kk"),
        khmer("km"), kikuyu("ki"), kinyarwanda("rw"), kirghiz("ky"), kirundi("rn"), klingon("tlh"),
        komering("kge"), komi("kv"), komi_permyak("koi"), kongo("kg"), korean("ko"), kotava("avk"),
        kuanyama("kj"), kurdish("ku"), kusaal("kus"), ladin("lld"), ladino("lad"), lak("lbe"),
        lao("lo"), latgalian("ltg"), latin("la"), latvian("lv"), lezghian("lez"), ligurian("lij"),
        limburgian("li"), lingala("ln"), lingua_franca_nova("lfn"), lithuanian("lt"),
        livvi_karelian("olo"), lojban("jbo"), lombard("lmo"), low_saxon("nds"),
        lower_sorbian("dsb"), luganda("lg"), luxembourgish("lb"), macedonian("mk"), madurese("mad"),
        magahi("mag"), maithili("mai"), malagasy("mg"), malay("ms"), malayalam("ml"), maltese("mt"),
        manipuri("mni"), manx("gv"), maori("mi"), marathi("mr"), marshallese("mh"),
        mazandarani("mzn"), min_dong("cdo"), min_nan("zh-min-nan"), minangkabau("min"),
        mingrelian("xmf"), mirandese("mwl"), moksha("mdf"), moldovan("mo"), mon("mnw"),
        mongolian("mn"), moroccan_arabic("ary"), mossi("mos"), muscogee("mus"), nahuatl("nah"),
        nauruan("na"), navajo("nv"), nawat("ppl"), ndonga("ng"), neapolitan("nap"), nepali("ne"),
        newar_nepal_bhasa("new"), nias("nia"), nigerian_pidgin("pcm"), nko("nqo"), norfolk("pih"),
        norman("nrm"), northern_frisian("frr"), northern_luri("lrc"), northern_sami("se"),
        northern_sotho("nso"), norwegian_bokmal("no"), norwegian_nynorsk("nn"), novial("nov"),
        nupe("nup"), obolo("ann"), occitan("oc"), old_church_slavonic("cu"), oriya("or"),
        oromo("om"), ossetian("os"), pa_o("blk"), paiwan("pwn"), palatine_german("pfl"), pali("pi"),
        pangasinan("pag"), pannonian_rusyn("rsk"), papiamentu("pap"), pashto("ps"),
        pennsylvania_german("pdc"), persian("fa"), picard("pcd"), piedmontese("pms"), polish("pl"),
        pontic("pnt"), portuguese("pt"), punjabi("pa"), quechua("qu"), ripuarian("ksh"),
        romani("rmy"), romanian("ro"), romansh("rm"), russian("ru"), rusyn("rue"), sakizaya("szy"),
        samoan("sm"), samogitian("bat-smg"), sango("sg"), sanskrit("sa"), santali("sat"),
        saraiki("skr"), sardinian("sc"), saterland_frisian("stq"), scots("sco"),
        scottish_gaelic("gd"), serbian("sr"), serbo_croatian("sh"), sesotho("st"), shan("shn"),
        shona("sn"), sichuan_yi("ii"), sicilian("scn"), silesian("szl"), simple_english("simple"),
        sindhi("sd"), sinhalese("si"), slovak("sk"), slovenian("sl"), somali("so"),
        south_azerbaijani("azb"), south_ndebele("nr"), southern_altai("alt"),
        southern_dagaare("dga"), spanish("es"), sranan_tongo("srn"),
        standard_moroccan_tamazight("zgh"), sundanese("su"), swahili("sw"), swati("ss"),
        swedish("sv"), sylheti("syl"), tachelhit("shi"), tagalog("tl"), tahitian("ty"),
        tai_nuea("tdd"), tajik("tg"), talysh("tly"), tamil("ta"), tarantino("roa-tara"),
        taroko("trv"), tatar("tt"), telugu("te"), tetum("tet"), thai("th"), tibetan("bo"),
        tigre("tig"), tigrinya("ti"), tok_pisin("tpi"), tokipona("tok"), tongan("to"), tsonga("ts"),
        tswana("tn"), tulu("tcy"), tumbuka("tum"), turkish("tr"), turkmen("tk"), tuvinian("tyv"),
        twi("tw"), tyap("kcg"), udmurt("udm"), ukrainian("uk"), upper_sorbian("hsb"), urdu("ur"),
        uyghur("ug"), uzbek("uz"), venda("ve"), venetian("vec"), veps("vep"), vietnamese("vi"),
        volapuek("vo"), voro("fiu-vro"), walloon("wa"), waray_waray("war"), wayuu("guc"),
        welsh("cy"), west_coast_bajau("bdr"), west_flemish("vls"), west_frisian("fy"),
        western_armenian("hyw"), western_mari("mrj"), western_punjabi("pnb"), wolof("wo"),
        wu("wuu"), xhosa("xh"), yakut("sah"), yiddish("yi"), yoruba("yo"),
        zamboanga_chavacano("cbk-zam"), zazaki("diq"), zealandic("zea"), zhuang("za"), zulu("zu"),
        _test("test");

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
