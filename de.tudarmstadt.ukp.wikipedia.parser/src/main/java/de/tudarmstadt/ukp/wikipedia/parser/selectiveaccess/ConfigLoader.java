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
package de.tudarmstadt.ukp.wikipedia.parser.selectiveaccess;

import java.util.EnumMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.wikipedia.parser.selectiveaccess.SelectiveAccessHandler.CIT;
import de.tudarmstadt.ukp.wikipedia.parser.selectiveaccess.SelectiveAccessHandler.SIT;

class ConfigLoader extends DefaultHandler{
	SelectiveAccessHandler sah;
	
	private EnumMap<CIT, Boolean> citm;
	private EnumMap<SIT, EnumMap<CIT, Boolean>> sitm;
	private Attributes secatt;
	
	private Map<String, EnumMap<SIT, EnumMap<CIT, Boolean>> > sectionHandling;
	
	public ConfigLoader( SelectiveAccessHandler sah ){
		this.sah = sah;
	}

	public void startElement(String uri, String localName, String qName, Attributes att){
		if( localName.equalsIgnoreCase( "cit" )){
			citm = SelectiveAccessHandler.buildCITMap(
				"true".equalsIgnoreCase( att.getValue( "text" ) ),
				"true".equalsIgnoreCase( att.getValue( "bold" ) ),
				"true".equalsIgnoreCase( att.getValue( "italic" ) ),
				"true".equalsIgnoreCase( att.getValue( "link" ) )
			);
		}
		else if( localName.equalsIgnoreCase("section") ){
			sitm = new EnumMap<SIT, EnumMap<CIT, Boolean>>( SIT.class );
			secatt = att;
		}
		else if( localName.equalsIgnoreCase( SIT.SUBS.toString() ) ){
			citm = null;		
		}
		else if( localName.equalsIgnoreCase( SIT.TITLE.toString() ) ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase( SIT.DEFLIST.toString() ) ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase( SIT.TABLE.toString() ) ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase( SIT.NESTLIST.toString() ) ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase( SIT.PARA.toString() ) ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase("page") ){
			citm = null;
		}
		else if( localName.equalsIgnoreCase("firstParagraph")){
			citm = null;
		}
		else if( localName.equalsIgnoreCase("SelectiveAccessHandlerConfig")){
			sah.setPageHandling( null );
			sah.setFirstParagraphHandling( null );
			sectionHandling = sah.getSectionHandling();
			sectionHandling.clear();
		}
		else{
			System.err.println("UnhandledElement: "+localName);
		}
	}
	
	public void endElement(String uri, String localName, String qName){
		if( localName.equalsIgnoreCase( "cit" )){
			// do nothing...
		}
		else if( localName.equalsIgnoreCase("section") ){
			String name = secatt.getValue("name");
			
			if( name != null )
				if( 	name.startsWith( SelectiveAccessHandler.SectionType.DEFAULT_SECTION.toString()) ||
						name.startsWith( SelectiveAccessHandler.SectionType.SECTION_LEVEL.toString()) ||
						name.startsWith( SelectiveAccessHandler.SectionType.USER_SECTION.toString()) )
					sectionHandling.put( name, sitm );
				else
					sectionHandling.put( SelectiveAccessHandler.SectionType.USER_SECTION.toString()+name, sitm );
			else
				sah.setDefaultSectionHandling( sitm );
			
		}
		else if( localName.equalsIgnoreCase( SIT.SUBS.toString() ) ){
			sitm.put( SIT.SUBS, citm );
		}
		else if( localName.equalsIgnoreCase( SIT.TITLE.toString() ) ){
			sitm.put( SIT.TITLE, citm );
		}
		else if( localName.equalsIgnoreCase( SIT.TABLE.toString() ) ){
			sitm.put( SIT.TABLE, citm );
		}
		else if( localName.equalsIgnoreCase( SIT.DEFLIST.toString() ) ){
			sitm.put( SIT.DEFLIST, citm );
		}
		else if( localName.equalsIgnoreCase( SIT.NESTLIST.toString() ) ){
			sitm.put( SIT.NESTLIST, citm );
		}
		else if( localName.equalsIgnoreCase( SIT.PARA.toString() ) ){
			sitm.put( SIT.PARA, citm );
		}
		else if( localName.equalsIgnoreCase("page") ){
			sah.setPageHandling( citm );
		}
		else if( localName.equalsIgnoreCase("firstParagraph")){
			sah.setFirstParagraphHandling( citm );
		}
		else if( localName.equalsIgnoreCase("SelectiveAccessHandlerConfig")){
			
		}
		else{
			System.err.println("UnhandledElement: "+localName);
		}
	}
}
