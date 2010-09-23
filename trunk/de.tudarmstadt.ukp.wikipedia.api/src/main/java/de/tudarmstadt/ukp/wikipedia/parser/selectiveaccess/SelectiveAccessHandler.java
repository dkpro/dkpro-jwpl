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
package de.tudarmstadt.ukp.wikipedia.parser.selectiveaccess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.DefinitionList;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.NestedList;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.Span;
import de.tudarmstadt.ukp.wikipedia.parser.Table;
import de.tudarmstadt.ukp.wikipedia.parser.Content.FormatType;

/**
 * Provides access to a ParsedPage at an abstract Level.
 * @author  CJacobi
 */
public class SelectiveAccessHandler {

	enum CIT{ TEXT, BOLD, ITALIC, LINK };

	enum SIT{ SUBS, TITLE, TABLE, DEFLIST, NESTLIST, PARA }; 
	
	protected enum SectionType{ DEFAULT_SECTION, SECTION_LEVEL, USER_SECTION };
	
	private EnumMap<CIT, Boolean> firstParagraphHandling;
	private EnumMap<CIT, Boolean> pageHandling;
	private Map<String, EnumMap<SIT, EnumMap<CIT, Boolean>> > sectionHandling;
	private int levelModifier = 0;
		
	/**
	 * Creates an SelectiveAccessHandler... ready to config...
	 */
	public SelectiveAccessHandler() {
		loadConfig();
	}
	
	/**
	 * Creates an SelectiveAccessHandler and loads the config from an XMLFile
	 */
	public SelectiveAccessHandler(String XMLFile ) {
		loadConfig( XMLFile );
	}
	
	public static EnumMap<CIT, Boolean> buildCITMap( boolean text, boolean bold, boolean italic, boolean link ){
		EnumMap<CIT, Boolean> result = new EnumMap<CIT, Boolean>( CIT.class );
		result.put( CIT.TEXT, text );
		result.put( CIT.BOLD, bold );
		result.put( CIT.ITALIC, italic );
		result.put( CIT.LINK, link );
		return result;
	}
	
	public static EnumMap<SIT, EnumMap<CIT, Boolean>> buildSITMap( EnumMap<CIT, Boolean> subs, EnumMap<CIT, Boolean> title, EnumMap<CIT, Boolean> table, EnumMap<CIT, Boolean> deflist, EnumMap<CIT, Boolean> nestedlist, EnumMap<CIT, Boolean> paragraph ){
		EnumMap<SIT, EnumMap<CIT, Boolean>> result = new EnumMap<SIT, EnumMap<CIT, Boolean>>( SIT.class );
		result.put( SIT.SUBS, subs );
		result.put( SIT.TITLE, title );
		result.put( SIT.TABLE, table );
		result.put( SIT.DEFLIST, deflist );
		result.put( SIT.NESTLIST, nestedlist );
		result.put( SIT.PARA, paragraph );
		return result;
	}

	/**
	 * if firstParagraphHandling is null, there will be no special handling for the FirstParagraph...
	 */
	public void setFirstParagraphHandling( EnumMap<CIT, Boolean> firstParagraphHandling ) {
		this.firstParagraphHandling = firstParagraphHandling;
	}

	/**
	 * if pageHandling is null, there will be no special handling for the WHOLE PAGE, this means, the handling will be sectionwhise...
	 */
	public void setPageHandling( EnumMap<CIT, Boolean> pageHandling ) {
		this.pageHandling = pageHandling;
	}

	/**
	 * @return the sectionHandling
	 */
	public Map<String, EnumMap<SIT, EnumMap<CIT, Boolean>>> getSectionHandling() {
		return sectionHandling;
	}
	
	/**
	 * Be sure to set the Default Section Handling to avoid errors...
	 */
	public void setSectionHandling(	Map<String, EnumMap<SIT, EnumMap<CIT, Boolean>>> sectionHandling ) {
		this.sectionHandling = sectionHandling;
	}
	
	/**
	 * adds section handling for a specified relative level...
	 */
	public void addSectionHandling( int level, EnumMap<SIT, EnumMap<CIT, Boolean>> sh ){
		sectionHandling.put( SectionType.SECTION_LEVEL.toString()+level, sh );
	}
	
	/**
	 * adds section handling for a specila section name...
	 */
	public void addSectionHandling( String name, EnumMap<SIT, EnumMap<CIT, Boolean>> sh ){
		sectionHandling.put( SectionType.USER_SECTION.toString() + name.toUpperCase(), sh);
	}
	
	/**
	 * sets the section handling for all sections which are not set by level or name...
	 */
	public void setDefaultSectionHandling( EnumMap<SIT, EnumMap<CIT, Boolean>> sh ){
		sectionHandling.put( SectionType.DEFAULT_SECTION.toString(), sh );
	}
	
	/**
	 * Returns information which infomations are selected by the actual configuration
	 */
	public String getSelectionInfo(){
		StringBuilder result = new StringBuilder();
		
		result.append( "SelectionInfo: "+this.getClass().toString() +"\n" );
		result.append( "Page:"+ CITInfo( pageHandling )+"\n" );
		result.append( "FirstParagraph:" +CITInfo( firstParagraphHandling )+"\n");
		for( String key: sectionHandling.keySet() ){
			final String uss = SectionType.USER_SECTION.toString();
			if( key.startsWith( uss ) )
				result.append(uss+"["+key.substring( uss.length() )+"]:\n");
			else
				result.append(key+":\n");
			
			result.append( SITInfo( sectionHandling.get(key))+"\n" );
		}
 		
		return result.toString();
	}
	
	/**
	 * Converts a CITMap into a human readable String
	 */
	public static String CITInfo( EnumMap<CIT, Boolean> hp ){
		StringBuilder result = new StringBuilder();
		result.append( "[");
		if( hp!= null ){
			for( CIT key: hp.keySet())
				result.append( key.toString()+":"+hp.get(key)+", ");
			result.delete( result.length()-2, result.length() );
		}
		result.append( "]" );
		return result.toString();
	}
	
	/**
	 * Converts a SITMap into a human readable String
	 */
	public static String SITInfo( EnumMap<SIT, EnumMap<CIT, Boolean>> shp ){
		StringBuilder result = new StringBuilder();
		for( SIT key: shp.keySet() ){
			result.append("\t"+key.toString()+":"+CITInfo( shp.get(key))+"\n");
		}
		return result.toString();
	}
	
	private void deleteParagraph( int nr, List<Section> sections ){
		int temp = nr;
		
		for( Section s: sections ){
			nr = temp;
			temp -= s.nrOfParagraphs();
			
			if( temp >= 0 ) continue;
			
			if( s.getClass() == SectionContainer.class )
				deleteParagraph( nr ,((SectionContainer)s).getSubSections() );
			else{
				SectionContent sc = (SectionContent)s;
				sc.removeParagraph( sc.getParagraph( nr ) );
			}
			
			break;
		}
	}
	
	/**
	 * Returns the Information of a ParsedPage which are selected by the actual configuration
	 */
	public String getSelectedText( ParsedPage pp ){
		if( pp == null ) return null;
		
		StringBuilder sb = new StringBuilder();
		
		levelModifier = pp.getSection(0).getLevel()-1;
		
		if( pageHandling == null ){
			if( firstParagraphHandling != null ){				
				handleContent( pp.getFirstParagraph(), firstParagraphHandling, sb );
				deleteParagraph( pp.getFirstParagraphNr(), pp.getSections() );
			}
			for( Section s: pp.getSections() )
				handleSection( s, sb );
		}
		else{
			if( pageHandling.get( CIT.TEXT ) ){
				sb.append( pp.getText() );
			}
			else{
				if( pageHandling.get( CIT.BOLD )){
					handleSpans( pp.getFormatSpans( FormatType.BOLD ), pp.getText(), sb );
				}
				if( pageHandling.get( CIT.ITALIC )){
					handleSpans( pp.getFormatSpans( FormatType.ITALIC ), pp.getText(), sb );
				}
			}
			
			if( pageHandling.get( CIT.LINK ))
				handleLinks( pp.getLinks(), !pageHandling.get( CIT.TEXT ), sb );
		}
		
		return sb.toString().trim();
	}
	
	private static void handleContent( Content c, EnumMap<CIT, Boolean> hp, StringBuilder sb ){
		if( hp != null ){
			if( hp.get( CIT.TEXT ))
				sb.append( c.getText()+" " );
			else{
				if( hp.get( CIT.BOLD ) )
					handleSpans( c.getFormatSpans( FormatType.BOLD), c.getText(), sb );
				if( hp.get( CIT.ITALIC ))
					handleSpans( c.getFormatSpans( FormatType.ITALIC), c.getText(), sb );
			}
			if( hp.get( CIT.LINK ))
				handleLinks( c.getLinks(), !hp.get( CIT.TEXT ), sb );
		}
	}
	
	private void handleSection( Section s, StringBuilder sb ){
		EnumMap<SIT, EnumMap<CIT, Boolean>> hp = null;
		
		if( s.getTitle()!= null ) hp = sectionHandling.get( SectionType.USER_SECTION.toString()+s.getTitle().toUpperCase() );
		if( hp == null ) hp = sectionHandling.get(SectionType.SECTION_LEVEL.toString()+(s.getLevel()-levelModifier));
		if( hp == null ) hp = sectionHandling.get(SectionType.DEFAULT_SECTION.toString());
		if( hp == null ){
			System.err.println( "Cannot get Handling Parameters for Section:\""+ s.getTitle()+"\" Level:"+s.getLevel() );
			return;
		}
	
		handleContent( s.getTitleElement(), hp.get( SIT.TITLE ), sb );
		
		if( s.getClass() == SectionContainer.class ){
			if( hp.get( SIT.SUBS )!= null ) 
				handleContent( s, hp.get( SIT.SUBS ), sb );
			else 
				for( Section ss: ((SectionContainer)s).getSubSections() )
					handleSection( ss, sb );
		}
		else{			
			EnumMap<CIT, Boolean> hpx;
			
			hpx = hp.get( SIT.TABLE );
			if( hpx != null )
				for( Table t: s.getTables() )
					handleContent( t, hpx, sb );
			
			hpx = hp.get( SIT.NESTLIST );
			if( hpx != null )
				for( NestedList nl: s.getNestedLists() )
					handleContent( nl, hpx, sb );
			
			hpx = hp.get( SIT.PARA );
			if( hpx != null )
				for( Paragraph p: s.getParagraphs() )
					handleContent( p, hpx, sb );
			
			hpx = hp.get( SIT.DEFLIST );
			if( hpx != null )
				for( DefinitionList dl: s.getDefinitionLists() )
					handleContent( dl, hpx, sb );
		}
	}
	
	private static void handleSpans( List<Span> spans, String text, StringBuilder sb ){
		for( Span s: spans )
			sb.append( text.substring( s.getStart(), s.getEnd() )+" ");
	}
	
	private static void handleLinks( List<Link> links, boolean linktext, StringBuilder sb ){
		for( Link l: links ){
			switch( l.getType() ){
			case INTERNAL:
				String lText = l.getText();
				String lTarget = l.getTarget();
				if( linktext ) sb.append( lText+" " );
				if( !lText.equals( lTarget )) sb.append( lTarget+" " );
				break;
			case EXTERNAL:
				sb.append( l.getText()+" " );
				break;
			case IMAGE:
			case AUDIO:
			case VIDEO:
				// do nothing !
				break;
			}
		}
	}
	
	/**
	 * Loads the Default Config... (shows nothing at all, but ready to config...)
	 */
	private void loadConfig(){
		firstParagraphHandling = null;
		pageHandling = null;
		sectionHandling = new HashMap<String, EnumMap<SIT, EnumMap<CIT, Boolean>> >();
		setDefaultSectionHandling( buildSITMap( buildCITMap( false, false, false, false ), null, null, null, null, null ) );
	}
	
	/**
	 * Loads a Configuration from an XMLFile...
	 */
	public void loadConfig( String XMLFile ){
		try{
			sectionHandling = new HashMap<String, EnumMap<SIT, EnumMap<CIT, Boolean>> >();
			SAXParserFactory factory = SAXParserFactory.newInstance();
		    factory.setNamespaceAware(true);
		    SAXParser sp = factory.newSAXParser();
		    DefaultHandler handler = new ConfigLoader( this );
		    sp.parse( XMLFile, handler );
		}
		catch( Exception e ){
			System.err.println( e );
			loadConfig();
		}
	}
	
	private static String XMLCIT( EnumMap<CIT, Boolean> em ){
		StringBuilder result = new StringBuilder();
		result.append( "<cit" );
		if( em != null )
			for( CIT key: em.keySet() )
				result.append( " "+ key.toString()+"=\""+em.get(key)+"\"" );
		result.append( "/>" );
		return result.toString();	
	}
	
	private static String XMLSIT( EnumMap<SIT, EnumMap<CIT, Boolean>> sem ){
		StringBuilder result = new StringBuilder();
		for( SIT key: sem.keySet() ){
			result.append( "<"+key.toString()+">");
			result.append( XMLCIT( sem.get( key ) ) );
			result.append( "</"+key.toString()+">\n");
		}
		return result.toString();
	}
	
	/**
	 * writes an XML configuration file...
	 */
	public void writeConfig( String XMLFile ){
		try{
			BufferedWriter bw = new BufferedWriter(	new FileWriter( XMLFile ) );
			
			bw.write( "<SelectiveAccessHandlerConfig>\n" );
			bw.write( "<page>"+XMLCIT( pageHandling )+"</page>\n" );
			bw.write( "<firstparagraph>"+XMLCIT( pageHandling )+"</firstparagraph>\n" );
			for( String key: sectionHandling.keySet() ){
				bw.write( "<section name=\""+key+"\">\n" );
				bw.write( XMLSIT( sectionHandling.get(key) ));
				bw.write( "</section>\n" );
			}
			bw.write( "<SelectiveAccessHandlerConfig>\n" );
			
			bw.close();
		}
		catch( IOException e ){
			System.err.println( e );
		}
	}
}
