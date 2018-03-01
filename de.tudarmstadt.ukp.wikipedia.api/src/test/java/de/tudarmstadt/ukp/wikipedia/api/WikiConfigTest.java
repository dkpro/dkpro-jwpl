package de.tudarmstadt.ukp.wikipedia.api;

import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfig;

import static org.junit.Assert.assertTrue;

public class WikiConfigTest {

    @Test
    public void testGetWikiConf() {
        WikiConfig portugueseConf = Wikipedia.getWikiconfig(WikiConstants.Language.portuguese);
        WikiConfig englishConf = Wikipedia.getWikiconfig(WikiConstants.Language.english);
        WikiConfig testConf = Wikipedia.getWikiconfig(WikiConstants.Language._test);
        WikiConfig frenchConf = Wikipedia.getWikiconfig(WikiConstants.Language.french);
        // assertion block
        assertTrue(portugueseConf.getContentLanguage() == "pt");
        assertTrue(englishConf.getContentLanguage() == "en");
        assertTrue(testConf.getContentLanguage() == "en");
        assertTrue(frenchConf.getContentLanguage() == "fr");
    }
}
