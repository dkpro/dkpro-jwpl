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
package org.dkpro.jwpl.parser.mediawiki;

import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.parser.Content.FormatType;
import org.dkpro.jwpl.parser.ContentElement;
import org.dkpro.jwpl.parser.DefinitionList;
import org.dkpro.jwpl.parser.Link;
import org.dkpro.jwpl.parser.NestedList;
import org.dkpro.jwpl.parser.NestedListContainer;
import org.dkpro.jwpl.parser.Paragraph;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.parser.Section;
import org.dkpro.jwpl.parser.SectionContainer;
import org.dkpro.jwpl.parser.SectionContent;
import org.dkpro.jwpl.parser.Span;
import org.dkpro.jwpl.parser.SrcSpan;
import org.dkpro.jwpl.parser.Table;
import org.dkpro.jwpl.parser.TableElement;
import org.dkpro.jwpl.parser.Template;

/**
 * Checks the Range of the SrcSpans of a ParsedPage, so it isn't possible
 * that e.g. a ContentElement conatins a Link which isn't in the Range of
 * this ContentElement. This must be done because some positons will be
 * jammed by the parsing process, e.g. if a Link is the start of a Paragrah.
 *
 */
public class SrcPosRangeChecker {

	public static void checkRange( ParsedPage pp ){
		for( Section s: pp.getSections() ){
			if( s.getClass() == SectionContent.class )
				checkRange( (SectionContent)s );
			else
				checkRange( (SectionContainer)s );
		}
	}
	
	private static void checkRange( SectionContainer sc ){
		if( sc.getTitleElement()!= null )
			checkRange( sc.getTitleElement() );
		
		for( Section s: sc.getSubSections() ){
			if( s.getClass() == SectionContent.class )
				checkRange( (SectionContent)s );
			else
				checkRange( (SectionContainer)s );
		}
	}
	
	private static void checkRange( SectionContent s ){
		List<SrcSpan> eil = new ArrayList<SrcSpan>();
		
		if( s.getTitleElement()!= null ){
			checkRange( s.getTitleElement() );
			eil.add( s.getTitleElement().getSrcSpan() );
		}
				
		for( Paragraph p: s.getParagraphs() ){
			checkRange( p );
			eil.add( p.getSrcSpan() );
		}
		
		for( DefinitionList dl: s.getDefinitionLists() ){
			checkRange( dl );
			eil.add( dl.getSrcSpan() );
		}

		for( NestedListContainer nl: s.getNestedLists() ){
			checkRange( nl );
			eil.add( nl.getSrcSpan() );
		}

		for( Table t: s.getTables() ){
			checkRange( t );
			eil.add( t.getSrcSpan() );
		}
		
		s.setSrcSpan( getEvalInfo( s.getSrcSpan(), eil));
	}
	
	private static void checkRange( DefinitionList dl ){

	}
	
	private static void checkRange( NestedListContainer nlc ){
		for( NestedList nl: nlc.getNestedLists() ){
			if( nl.getClass() == NestedListContainer.class )
				checkRange( (NestedListContainer)nl );
			else
				checkRange( (ContentElement)nl );
		}
	}

	private static void checkRange( Table t ){
		List<SrcSpan> eil = new ArrayList<SrcSpan>();	
		
		for( int i=0; i<t.nrOfTableElements(); i++){
			TableElement te = t.getTableElement(i);
			checkRange( te );
			eil.add( te.getSrcSpan() );
		}
		
		t.setSrcSpan( getEvalInfo( t.getSrcSpan(), eil));
	}
	
	private static void checkRange( TableElement te ){
		List<SrcSpan> eil = new ArrayList<SrcSpan>();
		
		for( Section s: te.getSubSections() ){
			if( s.getClass() == SectionContent.class )
				checkRange( (SectionContent)s );
			else
				checkRange( (SectionContainer)s );
		}
		
		te.setSrcSpan( getEvalInfo( te.getSrcSpan(), eil ) );
	}
	
	private static void checkRange( ContentElement ce ){
		List<SrcSpan> eil = new ArrayList<SrcSpan>();	
		for( Span s: ce.getFormatSpans( FormatType.BOLD ) ) eil.add( s.getSrcSpan() );
		for( Span s: ce.getFormatSpans( FormatType.ITALIC ) ) eil.add( s.getSrcSpan() );
		for( Span s: ce.getFormatSpans( FormatType.MATH ) ) eil.add( s.getSrcSpan() );
		for( Span s: ce.getFormatSpans( FormatType.TAG ) ) eil.add( s.getSrcSpan() );
		for( Span s: ce.getFormatSpans( FormatType.NOWIKI ) ) eil.add( s.getSrcSpan() );
		for( Link l: ce.getLinks())	eil.add( l.getSrcSpan() );
		for( Template t: ce.getTemplates() ) eil.add( t.getSrcSpan() );

		ce.setSrcSpan( getEvalInfo( ce.getSrcSpan(), eil) );
	}
	
	private static SrcSpan getEvalInfo( SrcSpan e, List<SrcSpan> eil ){
		int start = e.getStart();
		int end = e.getEnd();
		
		for( SrcSpan ei: eil ){
			if( start==-1 ||( start > ei.getStart() && ei.getStart() != -1 ) )	start = ei.getStart();
			if( end < ei.getEnd()) end = ei.getEnd();
		}
		return new SrcSpan( start, end );
	}
}
