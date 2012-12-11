package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.util.List;

import org.sweble.wikitext.engine.CompilerException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SwebleUtils;

/**
 * Represents a pair of (adjacent) revisions. In the second pair part (=after) a
 * template has been added or removed (depending on the mode).
 *
 * @author Oliver Ferschke
 *
 */
public class RevisionPair
{

	private Revision before;
	private Revision after;
	private String template;
	private RevisionPairType revPairType;

	public RevisionPair(Revision before, Revision after, String template,
			RevisionPairType revPairType)
	{
		this.before = before;
		this.after = after;
		this.template = template;
		this.revPairType = revPairType;
	}

	/**
	 * @return revision before the template change
	 */
	public Revision getBeforeRevision()
	{
		return before;
	}

	/**
	 * @return revision after the template change
	 */
	public Revision getAfterRevision()
	{
		return after;
	}

	/**
	 * @return the template that has been added or removed
	 */
	public String getTemplate()
	{
		return template;
	}

	/**
	 * @return the type of template change
	 */
	public RevisionPairType getType()
	{
		return revPairType;
	}

	/**
	 * If the base template of this RevisionPair is an inline template, this
	 * method uses the text around the template to create the diff.
	 *
	 * @return the diff of getInlineTextPair();
	 */
	public String getInlineDiff()
	{
		// TODO not yet implemented
		return "";
	}

	/**
	 * Returns the text around the given template and returns the corresponding
	 * text in the other pair part of the RevisionPair
	 *
	 * @return a pair of strings corresponding to the before-revision and
	 *         after-revision
	 */
	public TextPair getInlineTextPair()
	{

		String beforeString = null;
		String afterString = null;

		try{
			if (revPairType == RevisionPairType.deleteTemplate) {
				//"before" revision contains the template
				List<ExtractedSection> beforeSections = SwebleUtils.getSections(before.getRevisionText(), before.getRevisionID()+"", before.getRevisionID());
				List<ExtractedSection> afterSections = SwebleUtils.getSections(after.getRevisionText(), after.getRevisionID()+"", after.getRevisionID());

				for(ExtractedSection sect:beforeSections){
					//TODO find sect with template
					//TODO sync before-after: using title? fuzzy matching? location in article?
				}
			}
			else if (revPairType == RevisionPairType.addTemplate) {
				//"after" revision contains the template
				List<ExtractedSection> beforeSections = SwebleUtils.getSections(before.getRevisionText(), before.getRevisionID()+"", before.getRevisionID());
				List<ExtractedSection> afterSections = SwebleUtils.getSections(after.getRevisionText(), after.getRevisionID()+"", after.getRevisionID());

				for(ExtractedSection sect:afterSections){
					//TODO find sect with template
					//TODO sync before-after: using title? fuzzy matching? location in article?
				}
			}
		}catch(CompilerException cEx){
			//TODO handle properly
			cEx.printStackTrace();
		}
		return new TextPair(beforeString, afterString);
	}

	public enum RevisionPairType
	{
		deleteTemplate, addTemplate
	}

	/**
	 * Represents a pair of Strings. Usually corresponding to a RevisionPair.
	 *
	 * @author Oliver Ferschke
	 *
	 */
	public class TextPair
	{
		private String beforeText;
		private String afterText;

		public TextPair(String before, String after)
		{
			this.beforeText = before;
			this.afterText = after;
		}

		public String getBeforeText()
		{
			return beforeText;
		}

		public void setBeforeText(String beforeText)
		{
			this.beforeText = beforeText;
		}

		public String getAfterText()
		{
			return afterText;
		}

		public void setAfterText(String afterText)
		{
			this.afterText = afterText;
		}

	}
}
