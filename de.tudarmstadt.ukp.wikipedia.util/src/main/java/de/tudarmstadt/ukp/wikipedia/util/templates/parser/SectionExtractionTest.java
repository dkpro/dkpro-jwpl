package de.tudarmstadt.ukp.wikipedia.util.templates.parser;

import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;

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

			List<ExtractedSection> sects = SwebleUtils.getSections(p.getText(),p.getTitle().toString(),  -1);
			for(ExtractedSection sect:sects){
				System.out.println(sect.getBody());
			}

		}
		catch (Exception e) {
			// TODO handle exception
			e.printStackTrace();
		}





	}

}
