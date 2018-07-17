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
package de.tudarmstadt.ukp.wikipedia.parser.html;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Content.FormatType;
import de.tudarmstadt.ukp.wikipedia.parser.ContentElement;
import de.tudarmstadt.ukp.wikipedia.parser.DefinitionList;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.NestedList;
import de.tudarmstadt.ukp.wikipedia.parser.NestedListContainer;
import de.tudarmstadt.ukp.wikipedia.parser.NestedListElement;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContent;
import de.tudarmstadt.ukp.wikipedia.parser.SectionContainer;
import de.tudarmstadt.ukp.wikipedia.parser.Span;
import de.tudarmstadt.ukp.wikipedia.parser.Table;
import de.tudarmstadt.ukp.wikipedia.parser.TableElement;
import de.tudarmstadt.ukp.wikipedia.parser.Template;

/**
 * Renders a ParsedPage in HTML...<br>
 * <br>
 * There is a ParsedPage.css for formatting the HTML Tags.<br>
 * Look at the {@code T7_HtmlFileDemo.java} in the 'tutorial' module for a better introduction.
 *
 */
public class HtmlWriter {

	private final static Log logger = LogFactory.getLog(HtmlWriter.class);

    /**
	 * Generates HTML Output for a {@link ParsedPage}.
	 *
	 * @param pp The page that shall be parsed.
	 * @return A string containing the HTML rendering of the {@link ParsedPage}.
	 */
	public static String parsedPageToHtml( ParsedPage pp ){
		StringBuilder result = new StringBuilder();
		result.append(getHtmlHeader());

		if( pp != null ) {
    		//Title
    		result.append(
    			"<table class=\"ParsedPage\">\n"+
    			"<tr><th class=\"ParsedPage\">ParsedPage: \n" +
    			pp.getName()+
    			"</th></tr>\n");

//    		if( pp.aboutArticle()!=null ){
//    			result.append("<tr><td class=\"ParsedPage\">\n");
//    			result.append("About Article:" + contentElementToHtml( pp.aboutArticle() ));
//    			result.append("</td></tr>\n");
//    		}

    		//Sections
    		result.append(
    			"<tr><td class=\"ParsedPage\">\n"		);
    		for( Section s: pp.getSections() ) {
				result.append( sectionToHtml( s ));
			}
    		result.append(
    			"</td></tr>\n");

    		//Categories
    		if( pp.getCategoryElement()!= null ){
    			result.append("<tr><td class=\"ParsedPage\">\n");
    			result.append("Categories:\n"  + contentElementToHtml( pp.getCategoryElement() ));
    			result.append("</td></tr>\n");
    		}

    		//Languages
    		if( pp.getLanguagesElement()!= null ){
    			result.append("<tr><td class=\"ParsedPage\">\n");
    			result.append("Languages:\n"  + contentElementToHtml( pp.getLanguagesElement() ));
    			result.append("</td></tr>\n");
    		}

    		//Finalize
    		result.append("</table>\n");
        }

		result.append(getHtmlFooter());

		return result.toString();
	}

	/**
	 * @return Creates and returns the header of the HTML page
	 */
	private static String getHtmlHeader() {
		StringBuilder header = new StringBuilder();
		header.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		header.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"     http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		header.append("<html>");
		header.append("<head>");
		header.append(getCSS());
//		header.append("	 <link href=\""+cssFileName+"\" type=\"text/css\" rel=\"stylesheet\"/>");
		header.append("</head>");
		header.append("<body>");

		return header.toString();
	}

	/**
	 * @return Creates and returns the footer of the HTML page
	 */
	private static String getHtmlFooter() {
		StringBuilder footer = new StringBuilder();
		footer.append("</body>");
		footer.append("</html>");

		return footer.toString();
	}

	/**
	 * @return Creates and returns the CSS definitions of the HTML page
	 */
	private static String getCSS() {
		StringBuilder css = new StringBuilder();
		css.append("<style>");
		css.append(ParsedPageCSS.getFileText());
		css.append("</style>");

		return css.toString();
	}

	/**
	 * Generates HTML Output for a {@link SectionContainer} or {@link SectionContent}.
	 */
	private static String sectionToHtml( Section s ){

		return	"<table class=\"Section\">\n"+
				"<tr><th class=\"Section\">\n" +

				"<table class=\"SectionTh\"><tr>\n" +
				"<th class=\"SectionTh\">\n" +
				(s.getClass() == SectionContainer.class?"SectionStructure":"SectionContent")+":<br>\n"+
				"Level: "+s.getLevel()+"\n"+
				"</th><th class=\"SectionTh\">\n" +
				(s.getTitleElement()!=null?contentElementToHtml( s.getTitleElement() ):"")+
				"</th>\n" +
				"</tr></table>\n"+

				"</th></tr>\n"	+
				"<tr><td class=\"Section\">\n"+
				sectionCCLToHtml( s )+
				"</td></tr>\n"+
				"</table>\n";
	}

	private static String sectionCCLToHtml( Section s ){
		StringBuilder result = new StringBuilder();

		if( s.getClass() == SectionContainer.class ){
			for( Section ss: ((SectionContainer)s).getSubSections() ) {
				result.append( sectionToHtml( ss ));
			}
		}
		else{
			List<Content> ccl = s.getContentList();
			for( int i=(s.getTitleElement()!=null?1:0); i<ccl.size(); i++  ){
				Content c = ccl.get(i);
				Class cc = c.getClass();
				if( cc == Paragraph.class ) {
					result.append( paragraphToHtml( (Paragraph)c ) );
				}
				else if( cc == DefinitionList.class ) {
					result.append( definitionListToHtml( (DefinitionList)c ) );
				}
				else if( cc == NestedListContainer.class ) {
					result.append( nestedListToHtml( (NestedList)c ) );
				}
				else if( cc == Table.class ) {
					result.append( tableToHtml( (Table)c ) );
				}
				else {
					result.append("\n<pre>UNKNOWN CLASS: "+cc+"\n"+ convertTags( c.toString() )+"</pre>\n");
				}
			}
		}

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link Paragraph}.
	 */
	private static String paragraphToHtml( Paragraph p ){
		return contentElementToHtml( p, "Paragraph", "Paragraph: "+p.getType() );
	}

	/**
	 * Generates HTML Output for a {@link ContentElement}.
	 */
	private static String contentElementToHtml( ContentElement ce ){
		return contentElementToHtml( ce, "ContentElement", "ContentElement" );
	}

	private static String contentElementToHtml( ContentElement ce, String cssClass, String headline ){

		StringBuilder result = new StringBuilder();

		result.append(
				"<table class=\""+cssClass+"\">\n" +
				"<tr><th class=\""+cssClass+"\">" +headline+"</th></tr>\n"+
				"<tr><td class=\""+cssClass+"\">\n" +
				"\"" +	convertTags( ce.getText() )+ "\"\n" +
				"</td></tr>\n" );

		String BoldWords = ce.getText( ce.getFormatSpans( FormatType.BOLD ));
		if( BoldWords.length() > 0 ) {
			result.append("<tr><td class=\""+cssClass+"\">BoldWords: "+convertTags(BoldWords)+"</td></tr>\n");
		}

		String ItalicWords = ce.getText( ce.getFormatSpans( FormatType.ITALIC ));
		if( ItalicWords.length() > 0 ) {
			result.append("<tr><td class=\""+cssClass+"\">italicWords: "+convertTags(ItalicWords)+"</td></tr>\n");
		}

		if( ce.getFormatSpans( FormatType.MATH ).size() != 0 ){
			result.append("<tr><td class=\""+cssClass+"\">MathTags\n");
			for( Span s: ce.getFormatSpans( FormatType.MATH ) ) {
				result.append( s.toString() +"\n");
			}
			result.append("</td></tr>\n");
		}

		if( ce.getFormatSpans( FormatType.TAG ).size()!=0 ){
			result.append("<tr><td class=\""+cssClass+"\">Tags:\n");
			for( Span s: ce.getFormatSpans( FormatType.TAG ) ) {
				result.append( s.toString() +"\n");
			}
			result.append("</td></tr>\n");
		}

		if( ce.getLinks().size()!=0 ){
			result.append("<tr><td class=\""+cssClass+"\">\n");
			for( Link l: ce.getLinks() ) {
				result.append( linkToHtml( l ) );
			}
			result.append("</td></tr>\n");
		}

		if( ce.getTemplates().size()!=0 ){
			result.append("<tr><td class=\""+cssClass+"\">\n");
			for( Template t: ce.getTemplates() ) {
				result.append( templateToHtml( t ));
			}
			result.append("</td></tr>\n");
		}

		result.append( "</table>\n" );

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link DefinitionList}.
	 */
	private static String definitionListToHtml( DefinitionList dl){
		if( dl == null ) {
			return "null";
		}

		StringBuilder result = new StringBuilder();

		result.append("<table class=\"DefinitionList\">\n" +
				"<tr><th class=\"DefinitionList\">DefinitionList</th></tr>\n"+
				"<tr><td class=\"DefinitionList\">" );

		if( dl.getDefinedTerm() != null ) {
			result.append( contentElementToHtml( dl.getDefinedTerm() )+ "\n");
		}

		result.append("<ul>");
		for( ContentElement ce: dl.getDefinitions() ) {
			result.append("<li>"+contentElementToHtml(ce)+"</li>" );
		}

		result.append("</ul>\n");
		result.append("</td></tr>\n" );
		result.append("</table>\n");

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link NestedList}.
	 */
	private static String nestedListToHtml( NestedList nl ){
		if( nl == null ) {
			return "null";
		}

		StringBuilder result = new StringBuilder();

		if( nl.getClass()==NestedListElement.class ){
			result.append( "<li>\n"+ contentElementToHtml( (NestedListElement)nl ) +"</li>\n" );
		}
		else{
			result.append("<table class=\"NestedList\">\n" +
					"<tr><th class=\"NestedList\">NestedList</th></tr>\n"+
					"<tr><td class=\"NestedList\">" );

			result.append((((NestedListContainer)nl).isNumbered()?"<ol>":"<ul>")+"\n" );
			for( NestedList nl2 : ((NestedListContainer)nl).getNestedLists() ) {
				result.append( nestedListToHtml( nl2 ) );
			}
			result.append((((NestedListContainer)nl).isNumbered()?"</ol>":"</ul>")+"\n");

			result.append("</td></tr>\n" );
			result.append("</table>\n");
		}

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link Table}.
	 */
	private static String tableToHtml( Table t ){

		if( t == null ) {
			return "null";
		}

		StringBuilder result = new StringBuilder();

		int colspan;
		try{
			colspan = t.getTableElement( t.nrOfTableElements()-1 ).getCol()+1;
		}catch( Exception e){
			colspan = 1;
		}

		result.append("<table class=\"Table\">\n<tr><th colspan="+colspan+" class=\"Table\">Table");

		if( t.getTitleElement()!=null ) {
			result.append( contentElementToHtml( t.getTitleElement() ) );
		}

		result.append("</th></tr>\n<tr>\n");

		int row = 0;
		for( int i=0; i<t.nrOfTableElements(); i++ ){
			TableElement td = t.getTableElement(i);
			if( td.getRow() > row ){
				result.append( "</tr><tr>\n");
				row = td.getRow();
			}

			result.append( "<td class=\"Table\">\n" + tableElementToHtml( td ) +"</td>\n" );
		}

		result.append("</tr>\n</table>\n");
		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link TableElement}.
	 */
	private static String tableElementToHtml( TableElement td ){
		StringBuilder result = new StringBuilder();

		result.append("Row: "+td.getRow()+" Col: "+td.getCol()+"\n");

		if( td.nrOfSections()==1 && td.getSection(0).getTitleElement()==null) {
			result.append( sectionCCLToHtml( td.getSection(0) ));
		}
		else {
			for( int i=0; i<td.nrOfSections(); i++) {
				result.append( sectionToHtml(td.getSection(i) ));
			}
		}

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link Link}.
	 */
	private static String linkToHtml( Link l ){
		if( l == null ) {
			return "null";
		}

		StringBuilder result = new StringBuilder();

		result.append("<div class=\"Link\"><b class=\"Link\">Link:</b>" +
				l.getType() + ": \"" +
				convertTags( l.getText() )+ "\" -> \"" + convertTags( l.getTarget() ) +"\"");

		if( l.getParameters().size() != 0 ){
			for( String parameter: l.getParameters() ) {
				result.append("<br>\nPARAMETER: \""+convertTags( parameter )+"\"");
			}
		}

		result.append("</div>\n");

		return result.toString();
	}

	/**
	 * Generates HTML Output for a {@link Template}.
	 */
	private static String templateToHtml( Template t){
		if( t == null ) {
			return "null";
		}

		StringBuilder result = new StringBuilder();

		result.append(
			"<table class=\"Template\">\n" +
			"<tr><th class=\"Template\">Template</th></tr>\n"+
			"<tr><td class=\"Template\">" +
			"Name: \""+convertTags( t.getName() )+"\"<br>"+
			"</td></tr>\n");

		if( t.getParameters().size() != 0 ){
			result.append("<tr><td class=\"Template\">");
			for( String parameter: t.getParameters() ) {
				result.append("Parameter: \""+convertTags( parameter )+"\"<br>");
			}
			result.append("</td></tr>\n");
		}

		result.append("</table>" );

		return result.toString();
	}

	private static String convertTags( String s ){
		if( s==null ) {
			return null;
		}

		StringBuilder result = new StringBuilder( s );

		int temp;

		temp = 0;
		while( (temp=result.indexOf("<", temp))!=-1 ) {
			result.replace(temp, temp+1, "&lt;");
		}

		temp = 0;
		while( (temp=result.indexOf(">", temp))!=-1 ) {
			result.replace(temp, temp+1, "&gt;");
		}

		return result.toString();
	}

    public static void writeFile(String filename, String encoding, String text) {

        File outFile = new File(filename);
        try (Writer destFile = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), encoding))) {
            destFile.write(text);
        } catch (UnsupportedEncodingException e1) {
            logger.error("Unsupported encoding exception while opening file " + outFile.getAbsolutePath());
        } catch (FileNotFoundException e1) {
            logger.error("File " + outFile.getAbsolutePath() + " not found.",  e1);
        } catch (IOException e) {
            logger.error("IO exception while writing file " + outFile.getAbsolutePath(), e);
        }
    }

}
