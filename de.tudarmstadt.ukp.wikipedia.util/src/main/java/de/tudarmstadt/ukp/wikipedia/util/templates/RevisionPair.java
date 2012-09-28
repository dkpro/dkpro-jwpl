package de.tudarmstadt.ukp.wikipedia.util.templates;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * Represents a pair of (adjacent) revisions. In the second pair part (=after) a
 * template has been added or removed (depending on the mode).
 * 
 * @author Oliver Ferschke
 * 
 */
public class RevisionPair {

	private Revision before;
	private Revision after;
	private String template;
	private RevisionPairType revPairType;
	
	public RevisionPair(Revision before, Revision after, String template, RevisionPairType revPairType){
		this.before=before;
		this.after=after;
		this.template=template;
		this.revPairType=revPairType;
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
	
	public enum RevisionPairType{
		deleteTemplate, addTemplate
	}
}
