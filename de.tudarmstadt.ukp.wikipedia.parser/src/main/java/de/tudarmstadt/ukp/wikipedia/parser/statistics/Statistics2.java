/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser.statistics;

import java.util.Date;
import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Content.FormatType;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ShowTemplateNamesAndParameters;

/**
 * Creates a little Statistic about occurence of MediaWiki Elements...<br/> 
 * <br/>
 * Results for 15.05.2006 Database:<br/><pre>
 * SUBS: 279896 74.19%
 * NL: 255511 67.72%
 * DL: 1679 0.44%
 * TABLES: 64967 17.22%
 * TEMPLATES: 215022 56.99%
 * BOLD: 364484 96.61%
 * ITALIC: 231877 61.46%
 * MATH: 6499 1.72%
 * TAGS: 74236 19.67%
 * NOWIKI: 3058 0.81%</pre>
 * @author CJacobi
 *
 */
public class Statistics2 {

	// Variables
	static int nrOfPages;

	static int nrOfPagesWithNl;
	static int nrOfPagesWithDl;
	static int nrOfPagesWithBold;
	static int nrOfPagesWithItalic;
	static int nrOfPagesWithMath;
	static int nrOfPagesWithTag;
	static int nrOfPagesWithNoWiki;
	static int nrOfPagesWithTables;
	static int nrOfPagesWithSubSections;
	static int nrOfPagesWithTemplates;
	
	static int len_longestPage;
	static long len_allPages;
	
	static int nrOfAnalyzedPages;
			
	// Debug
	static final int skipPages = 0;
	static final long offsetTime = 0;  //1000 Sec/65536 Pages
	static final boolean debug = false;
	static final boolean savFiles = false;
	
	public static void main( String[] argv) throws Exception{
        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDatabase("wikiapi_de");
        dbConfig.setHost("bender.ukp.informatik.tu-darmstadt.de");
        dbConfig.setUser("student");
        dbConfig.setPassword("student");
        dbConfig.setLanguage(Language.german);

        Wikipedia wiki = new Wikipedia(dbConfig);
		
		MediaWikiParserFactory pf = new MediaWikiParserFactory( );
		
		pf.setTemplateParserClass( ShowTemplateNamesAndParameters.class );
		pf.setShowImageText( true );
		pf.setShowMathTagContent( true );
		pf.setDeleteTags( false );
		pf.getImageIdentifers().add("IMAGE");
		pf.setCalculateSrcSpans( false );
		
		MediaWikiParser parser = pf.createParser();
		
		System.out.println( parser.configurationInfo() );
		
		Iterator<Page> pageIt = wiki.getArticles().iterator();
		
		nrOfPages = 0;
		nrOfPagesWithNl = 0;
		nrOfPagesWithDl = 0;
		nrOfPagesWithBold = 0;
		nrOfPagesWithItalic = 0;
		nrOfPagesWithMath = 0;
		nrOfPagesWithTag = 0;
		nrOfPagesWithTables = 0;
		nrOfPagesWithSubSections = 0;
		nrOfPagesWithTemplates = 0;
		nrOfPagesWithNoWiki = 0;
		len_allPages = 0;
		len_longestPage = 0;
		
		nrOfAnalyzedPages = 0;
		
		long startTime = new Date().getTime();
		
		System.out.println("START OF ANALYSATION");
		while (pageIt.hasNext()) {
			Page currentPage = pageIt.next();
			nrOfAnalyzedPages++;
			
			//For Debugging purposes...
			if( nrOfAnalyzedPages < skipPages+1 ){
				System.out.println("Skipped: "+ currentPage.getPageId());
				continue;
			}	
			
			//Parsing
			String name = currentPage.getTitle().getPlainTitle();
			String src = currentPage.getText();
			
			if(debug) System.out.println( "     "+currentPage.getPageId()+" "+name );
			
			ParsedPage pp = parser.parse(src);
					
			if(pp==null){
				// this is an Error, wich occures when src=""
				continue;
			}
			
			pp.setName(name);

			//ANALYSIS
			
			int len_page = src.length();
			if( len_page > len_longestPage ) len_longestPage = len_page;
			len_allPages += len_page;
			
			if( pp.nrOfDefinitionLists() != 0 )nrOfPagesWithDl++;
			if( pp.nrOfNestedLists() != 0 )nrOfPagesWithNl++;
			if( pp.nrOfTables() != 0 ) nrOfPagesWithTables++;
			if( pp.getTemplates().size() != 0 ) nrOfPagesWithTemplates++;
			if( pp.getSections().size()>1 )nrOfPagesWithSubSections++;
			
			for( FormatType ft: pp.getFormats() ){
				if( ft ==  FormatType.BOLD ) nrOfPagesWithBold++;
				if( ft ==  FormatType.ITALIC ) nrOfPagesWithItalic++;
				if( ft ==  FormatType.NOWIKI ) nrOfPagesWithNoWiki++;
				if( ft ==  FormatType.MATH ) nrOfPagesWithMath++;
				if( ft ==  FormatType.TAG ) nrOfPagesWithTag++;
			}
			
			//Screen Info
			if( nrOfAnalyzedPages % 1024 == 0 ){
				long aktualTime = new Date().getTime();
				long runnedTime = aktualTime - startTime + offsetTime;
				long totalTime = (runnedTime * nrOfPages) / nrOfAnalyzedPages;
				
				System.out.println( 
						percentString(nrOfAnalyzedPages,nrOfPages)+
						" -> "+nrOfAnalyzedPages+" of "+nrOfPages+" pages in "+ runnedTime/1000+"sec"+
						" -> "+(totalTime-runnedTime)/60000+"min left" 
					);
				
				screenInfo();
				
				System.out.println();
			}	
			
			// if( nrOfAnalyzedPages == 1000 ) break;
  		}
		System.out.println("END OF ANALYSATION");
		screenInfo();
	
	}
		
	private static String percentString( long a, long nr){
		long temp = (a*10000)/nr;
		return temp/100+"."+(temp/10)%10+""+temp%10+"%";
	}
	
	private static String pi( String about, int what ){
		return " "+about+": "+what+" "+percentString(what,nrOfAnalyzedPages)+"\n";
	}
	
	private static void screenInfo(){
		System.out.print( 
				pi("SUBS",nrOfPagesWithSubSections) +
				pi("NL", nrOfPagesWithNl ) +
				pi("DL", nrOfPagesWithDl ) +
				pi("TABLES", nrOfPagesWithTables ) +
				pi("TEMPLATES", nrOfPagesWithTemplates ) +
				pi("BOLD", nrOfPagesWithBold ) +
				pi("ITALIC", nrOfPagesWithItalic ) +
				pi("MATH", nrOfPagesWithMath ) +
				pi("TAGS", nrOfPagesWithTag ) +
				pi("NOWIKI", nrOfPagesWithNoWiki )	
			);
		
		System.out.println("longes Page:"+len_longestPage);
		System.out.println("average length:"+len_allPages/nrOfAnalyzedPages );
	}
}
