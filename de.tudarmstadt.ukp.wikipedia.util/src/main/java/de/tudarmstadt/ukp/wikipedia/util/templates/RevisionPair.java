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
package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.ParseUtils;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;

/**
 * Represents a pair of (adjacent) revisions. In the second pair part (=after) a
 * template has been added or removed (depending on the mode).
 *
 *
 */
public class RevisionPair implements Serializable{

	private static final long serialVersionUID = -428550315195347191L;

	private Revision before;
	private Revision after;
	private String template;
	private RevisionPairType revPairType;

	public RevisionPair(Revision before, Revision after, String template,
			RevisionPairType revPairType) {
		this.before = before;
		this.after = after;
		this.template = template;
		this.revPairType = revPairType;
	}

	/**
	 * @return revision before the template change
	 */
	public Revision getBeforeRevision() {
		return before;
	}

	/**
	 * @return revision after the template change
	 */
	public Revision getAfterRevision() {
		return after;
	}

	/**
	 * @return the template that has been added or removed
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @return the type of template change
	 */
	public RevisionPairType getType() {
		return revPairType;
	}

	/**
	 * Returns the text "around the given template" and returns the corresponding
	 * text in the other pair part of the RevisionPair.
	 *
	 * Currently, this is done section-based. On TextPairPart contains a section
	 * with a template and the other contains the corresponding section
	 * after the template has been deleted (in deleteTemplate mode) or before
	 * it has been added (in addTemplate mode).
	 *
	 * Note that this only makes sense for inline- or section-templates.
	 *
	 * The section-matching is currently done simply by matching section titles.
	 * If the title has changed, no match will be found.
	 *
	 * @param markTemplates sets whether to add an inline marker for the template
	 *
	 * @return a pair of strings corresponding to the before-revision and
	 *         after-revision
	 */
	public List<TextPair> getInlineTextPairs(boolean markTemplates) {
		List<TextPair> pairList = new ArrayList<TextPair>();

		try {
			//extract sections
			List<ExtractedSection> beforeSections=null;
			List<ExtractedSection> afterSections=null;
			if(markTemplates){
				//add inline marker for the template
				beforeSections = ParseUtils.getSections(before.getRevisionText(), before.getRevisionID() + "",before.getRevisionID(), Arrays.asList(new String[]{template}));
				afterSections = ParseUtils.getSections(after.getRevisionText(), after.getRevisionID() + "", after.getRevisionID(), Arrays.asList(new String[]{template}));
			}else{
				//no inline markers
				beforeSections = ParseUtils.getSections(before.getRevisionText(), before.getRevisionID() + "",before.getRevisionID());
				afterSections = ParseUtils.getSections(after.getRevisionText(), after.getRevisionID() + "", after.getRevisionID());
			}
			for (ExtractedSection tplSect : revPairType == RevisionPairType.deleteTemplate ? beforeSections : afterSections) {
				// in DELETE-mode, the "before" revision contain the templates
				// in ADD-mode, the "after" revision contains the templates
				if (containsIgnoreCase(tplSect.getTemplates(), template)) {
					// the current sect contains the template we're looking for
					// now find the corresponding tpl in the other revisions
					for (ExtractedSection nonTplSect : revPairType == RevisionPairType.deleteTemplate ? afterSections: beforeSections) {
						// TODO how do we match the sections?
						// currently only by title - we could do fuzzy matching
						// of the section body
						if (tplSect.getTitle()!=null&&nonTplSect.getTitle()!=null&&tplSect.getTitle().equalsIgnoreCase(nonTplSect.getTitle())) {
							if (revPairType == RevisionPairType.deleteTemplate) {
								pairList.add(new TextPair(tplSect.getBody(), nonTplSect.getBody()));
							} else {
								pairList.add(new TextPair(nonTplSect.getBody(), tplSect.getBody()));
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			//This happends if a (SWEBLE-)compiler exception occurs.S
			//Sometimes, malformed xml items seem to cause class cast exceptions
			//in the parser, which is not wrapped in a Compiler exception.
			//Therefore, we should catch all exceptions here and return the
			//TextPairs identified so far (if any)
			System.err.println(ex.getMessage());
			//TODO use logger!!
		}
		return pairList;
	}

	/**
	 * Checks if a list of string contains a String while ignoring case
	 *
	 * @param stringlist a list of string
	 * @param match the string to look for
	 * @return true, if the list contains the string, false else
	 */
	private boolean containsIgnoreCase(List<String> stringlist, String match) {
		for (String s : stringlist) {
			if (s.equalsIgnoreCase(match)) {
				return true;
			}
		}
		return false;
	}

	public enum RevisionPairType {
		deleteTemplate, addTemplate
	}


    @Override
	public boolean equals(Object anObject) {
    	if(!(anObject instanceof RevisionPair)){
    		return false;
    	}else{
    		RevisionPair otherPair = (RevisionPair)anObject;
			if (this.getBeforeRevision().getRevisionID() ==
					otherPair.getBeforeRevision().getRevisionID()
					&& this.getAfterRevision().getRevisionID() ==
					otherPair.getAfterRevision().getRevisionID()
					&& this.getTemplate().equals(otherPair.getTemplate())&&
					this.getType()==otherPair.getType()) {
    			return true;
    		}else{
    			return false;
    		}
    	}
    }
}
