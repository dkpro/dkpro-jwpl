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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.Table;
import de.tudarmstadt.ukp.wikipedia.parser.TableElement;
import de.tudarmstadt.ukp.wikipedia.parser.Template;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.ShowTemplateNamesAndParameters;

/**
 * This creates a Detailed Statistic file for Tables and Templates.
 * @author CJacobi
 *
 */
public class Statistics {
	
	// Constants
	public static final String path = "./data/parsedpage/statistics/"; 
	
	// Variables
	static long nrOfPages;
	static int nrOfTables;
	static int nrOfTemplates;
	static int nrOfAnalyzedPages;
	static List<Integer> templateNrOfOccurence;
	static List<String> templateNameOfFirstOccurence;
	static List<String> templateNames;
	static List<String> pagesWithTableSections;
		
	// Debug
	static final int skipPages = 0;
	static final long offsetTime = 0;  //1000 Sec/65536 Pages
	static final boolean debug = false;
	
	public static void main( String[] argv) throws Exception{
        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDatabase("wikiapi_en");
        dbConfig.setHost("bender.ukp.informatik.tu-darmstadt.de");
        dbConfig.setUser("student");
        dbConfig.setPassword("student");
        dbConfig.setLanguage(Language.english);

        Wikipedia wiki = new Wikipedia(dbConfig);
		
		MediaWikiParserFactory pf = new MediaWikiParserFactory( );
		pf.setTemplateParserClass( ShowTemplateNamesAndParameters.class );
		pf.setShowImageText( true );
		pf.setShowMathTagContent( true );
		pf.setDeleteTags( false );
		pf.getImageIdentifers().add("IMAGE");
		pf.setCalculateSrcSpans( false );
		
		MediaWikiParser parser = pf.createParser();
		
		Iterator<Page> pageIt = wiki.getArticles().iterator();
		
		nrOfPages = wiki.getMetaData().getNumberOfPages();
		nrOfTables = 0;
		nrOfTemplates = 0;
		templateNames = new ArrayList<String>();
		templateNameOfFirstOccurence = new ArrayList<String>();
		templateNrOfOccurence = new ArrayList<Integer>();
		pagesWithTableSections = new ArrayList<String>();

		long startTime = new Date().getTime();
		
		nrOfAnalyzedPages = 0;
		
		System.out.println("ANALYSING ...");
		while (pageIt.hasNext()) {
			Page currentPage = pageIt.next();
			nrOfAnalyzedPages++;
			
			//For Debugging purposes...
			if( nrOfAnalyzedPages < skipPages+1 ){
				System.out.println("Skipped: "+ currentPage.getPageId());
				continue;
			}	
			
			//Screen Info
			if( nrOfAnalyzedPages % 1024 == 0){
				long aktualTime = new Date().getTime();
				long runnedTime = aktualTime - startTime + offsetTime;
				long totalTime = (runnedTime * nrOfPages) / nrOfAnalyzedPages;
				
				System.out.println( 
						percentString(nrOfAnalyzedPages,nrOfPages)+
						" -> "+nrOfAnalyzedPages+" of "+nrOfPages+" pages in "+ runnedTime/1000+"sec"+
						" -> "+(totalTime-runnedTime)/60000+"min left" 
					);
			}			
			
			//Parsing
			String name = currentPage.getTitle().getPlainTitle();
			String src = currentPage.getText();
			
			if(debug) System.out.println( "     "+currentPage.getPageId()+" "+name );
			
			ParsedPage pp = parser.parse(src);
            if (pp==null) {
                // this is an Error, wich occures when src=""
                continue;
            }

            pp.setName(name);
			

			//Template Analysis
			for( Template t: pp.getTemplates()){
				nrOfTemplates++;
				String templateName = t.getName().toLowerCase();	
				if( templateName.startsWith("vorlage:") )templateName = templateName.substring(8);
				else if( templateName.startsWith("template:") )templateName = templateName.substring(9);
				
				int pos = templateNames.indexOf( templateName );
				if( pos != -1 ){
					templateNrOfOccurence.set( pos, templateNrOfOccurence.get( pos )+1 );
				}
				else{
					templateNrOfOccurence.add(1);
					templateNames.add( templateName );
					templateNameOfFirstOccurence.add( pp.getName() );
					List<String> temp = new ArrayList<String>();
					temp.add( pp.getName() );
				}
			}
			
			//Table Analysis
			if( pp.nrOfTables()!=0 ) nrOfTables++;
			boolean b = true;
			for( Table t: pp.getTables() ){	
				if( b )for( int i=0; i<t.nrOfTableElements(); i++ ){
					TableElement te = t.getTableElement(i);
					if( te.nrOfSections() > 1 || te.getSection(0).getClass()==SectionContainer.class ){
						pagesWithTableSections.add( pp.getName() );
						b = false;
						break;
					}
				}
			}
			
			// if( nrOfAnalyzedPages == 1000 ) break;
  		}
		System.out.println("Finished.");
		
		sortTemplates();
		writeFiles("statistics");
		
		restructureTemplateNames();
		sortTemplates();
		writeTemplates("statistics.restructured");
		
		System.out.println("check the Results ;-)\nnow...");	
	}

	private static void sortTemplates(){
		//sort templates
		System.out.println("Sort Template List");
		List<String> sTemplateNames = new ArrayList<String>();
		List<Integer> sOcc = new ArrayList<Integer>();
		List<String> sTemplateNameFirstOcc = new ArrayList<String>();
		
		for( int i=0; i<templateNrOfOccurence.size(); i++){
			int nr = templateNrOfOccurence.get(i);
			int pos = 0;
			while( pos<sOcc.size() && nr<sOcc.get(pos) )pos++;
			sOcc.add(pos, nr );
			sTemplateNames.add(pos, templateNames.get(i) );
			sTemplateNameFirstOcc.add( pos, templateNameOfFirstOccurence.get(i) );
		}
		
		templateNames = sTemplateNames;
		templateNrOfOccurence = sOcc;
		templateNameOfFirstOccurence = sTemplateNameFirstOcc;
	}
	
	private static void writeFiles(String fileName) throws IOException{
		System.out.print("writeFiles() "+fileName);
		writeTemplates( fileName );
		wirteTables( fileName );
	}
	
	private static void writeTemplates(String fileName )throws IOException{	
		//write templates to file
		BufferedWriter bw = new BufferedWriter( new FileWriter(path+fileName+".template"));
		bw.write("Analyzed Pages: "+(nrOfAnalyzedPages)+"\n\n");
		bw.write("Found "+nrOfTemplates+" Templates\n");
		bw.write("Found "+templateNames.size()+" different Templates\n\n");
		int sum = 0;
		for( int i=0; i<templateNames.size(); i++){
			int temp = templateNrOfOccurence.get(i);
			sum+=temp;			
			bw.write( temp +" x {{"+templateNames.get(i)+"}}");
			bw.write(" @"+templateNameOfFirstOccurence.get(i));
			bw.write(" sum="+sum);
			bw.write("\n");
		}
		bw.close();
	}
	
	private static void wirteTables(String fileName )throws IOException{
		//write tables
		BufferedWriter bw = new BufferedWriter( new FileWriter( path+fileName+".table") );
		
		int sections = pagesWithTableSections.size();
			
		bw.write(
				"Analyzed Pages: "+nrOfAnalyzedPages+"\n"+
				"\n"+
				"Found "+nrOfTables+" Tables\n" +
				"-> "+ percentString(nrOfTables,nrOfAnalyzedPages)+" @Pages\n"+
				"\n"+
				"Found "+sections+ " Sections in Tables\n"+
				"-> "+percentString(sections,nrOfTables)+" @Tables\n"+
				"-> "+percentString(sections,nrOfAnalyzedPages)+" @Pages\n"+
				"\n");

		bw.write("-=Pages with Tables and Sections---------------------------------------------------\n");
		for(String s: pagesWithTableSections ) bw.write(s+"\n");
		
		bw.close();
		
		System.out.println( " --> OK" );
	}
	
	private static void restructureTemplateNames(){
		System.out.println( "restructure Template Names" );
		
		List<String> newTemplateNames = new ArrayList<String>();
		List<Integer> newTemplateNrOfOccurence = new ArrayList<Integer>();
		List<String> newTemplateNameOfFirstOccurence = new ArrayList<String>();
			
		for( int i=0; i<templateNames.size(); i++){
			String tn = templateNames.get(i);
			
			//get the First Word of the Name
			int pos = tn.indexOf(' ');
			int pos2 = tn.indexOf('_');
			if( pos == -1 || ( pos2!=-1 && pos2 < pos ) ) pos = pos2;
			if( pos != -1 ) tn = tn.substring(0, pos);
			
			//check if exists
			int index = newTemplateNames.indexOf( tn );
			
			if( index!=-1 ){
				newTemplateNrOfOccurence.set(
						index, 
						newTemplateNrOfOccurence.get(index)+ 
						templateNrOfOccurence.get(i)
					);
			}
			else{
				newTemplateNames.add( tn );
				newTemplateNrOfOccurence.add( templateNrOfOccurence.get(i) );
				newTemplateNameOfFirstOccurence.add( templateNameOfFirstOccurence.get(i));
			}
		}
		
		templateNames = newTemplateNames;
		templateNrOfOccurence = newTemplateNrOfOccurence;
		templateNameOfFirstOccurence = newTemplateNameOfFirstOccurence;
	}
	
	private static String percentString( long a, long nr){
		long temp;
	    if (nr > 0) {
	        temp = (a*10000)/nr;
		}
		else {
		    temp = 0;
		}
		return temp/100+"."+(temp/10)%10+""+temp%10+"%";
	}
}
