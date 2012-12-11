package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SwebleUtils;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		DatabaseConfiguration config = new DatabaseConfiguration();

		config.setHost("127.0.0.1:3307");
		config.setDatabase("wiki_en_20120104_rev");
		config.setUser("root");
		config.setPassword("");
		config.setLanguage(Language.english);

		Wikipedia wiki = new Wikipedia(config);
		Page p = wiki.getPage("Germany");
		List<ExtractedSection> sects = SwebleUtils.getSections(p.getText(), p.getTitle().toString(),-1);

		for(ExtractedSection sect:sects){
			System.out.println(sect.getBody());
		}

	}

}
