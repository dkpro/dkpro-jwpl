package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SectionExtractor.ExtractedSection;
import de.tudarmstadt.ukp.wikipedia.util.templates.parser.SwebleUtils;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;

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
	 * @return a pair of strings corresponding to the before-revision and
	 *         after-revision
	 */
	public List<TextPair> getInlineTextPairs() {
		List<TextPair> pairList = new ArrayList<TextPair>();

		try {
			List<ExtractedSection> beforeSections = SwebleUtils.getSections(before.getRevisionText(), before.getRevisionID() + "",before.getRevisionID());
			List<ExtractedSection> afterSections = SwebleUtils.getSections(after.getRevisionText(), after.getRevisionID() + "", after.getRevisionID());
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
								pairList.add(new TextPair(tplSect.getBody(),
										nonTplSect.getBody()));
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

	/**
	 * Represents a pair of Strings. Usually corresponding to a RevisionPair.
	 *
	 * @author Oliver Ferschke
	 *
	 */
	public class TextPair {
		private String beforeText;
		private String afterText;

		public TextPair(String before, String after) {
			this.beforeText = normalize(before);
			this.afterText = normalize(after);
		}

		public String getBeforeText() {
			return beforeText;
		}

		public void setBeforeText(String beforeText) {
			this.beforeText = normalize(beforeText);
		}

		public String getAfterText() {
			return afterText;
		}

		public void setAfterText(String afterText) {
			this.afterText = normalize(afterText);
		}

		/**
		 * Returns the patch object that contains all diffs between
		 * the beforeText and the afterText
		 *
		 * @return Patch object with all diffs
		 */
		public Patch getPatch() {
			return DiffUtils.diff(sentenceSplit(beforeText), sentenceSplit(afterText));
		}

		public List<DiffRow> getDiffRows(boolean markChangesInline){
			DiffRowGenerator generator = new DiffRowGenerator.Builder()
            	.showInlineDiffs(markChangesInline)
            	.columnWidth(Integer.MAX_VALUE) // do not wrap
            	.build();

			return generator.generateDiffRows(sentenceSplit(beforeText),sentenceSplit(afterText));
		}

		public String getInlineDiffString() {
			StringBuilder diffString = new StringBuilder();
			for(DiffRow row:getDiffRows(true)){
				diffString.append(row.toString());
				diffString.append(System.getProperty("line.separator"));
			}
			return diffString.toString();
		}


		/**
		 * Returns the deltas between beforeText and afterText as a line separated String
		 * using delta.toString()
		 * For more detailed diffs, use getPatch() or getUnifiedDiffStrings()
		 *
		 * @return diffs as line-separated String using delta.toString()
		 */
		public String getSimpleDiffString() {
			StringBuilder deltas = new StringBuilder();
			for(Delta delta:getPatch().getDeltas()){
				deltas.append(delta.toString());
				deltas.append(System.getProperty("line.separator"));
			}
			return deltas.toString();
		}

		/**
		 * Returns the deltas between beforeText and afterText as a line separated String
		 * using delta.toString()
		 * For more detailed diffs, use getPatch() or getUnifiedDiffStrings()
		 *
		 * @param difftype defines the type of diffs to include in the String
		 * @return diffs as line-separated String using delta.toString()
		 */
		public String getSimpleDiffString(TYPE difftype) {
			StringBuilder deltas = new StringBuilder();
			for(Delta delta:getPatch().getDeltas()){
				if(delta.getType()==difftype){
					deltas.append(delta.toString());
					deltas.append(System.getProperty("line.separator"));
				}
			}
			return deltas.toString();
		}

		/**
		 * Returns the deltas between beforeText and afterText as a line separated String.
		 * For more detailed diffs, use getPatch() or getUnifiedDiffStrings()
		 *
		 * @return diffs as line-separated String
		 */
		public String getLongDiffString() {
			StringBuilder deltas = new StringBuilder();
			for(Delta delta:getPatch().getDeltas()){
				deltas.append("DeltaType: "+delta.getType().toString());
				deltas.append(System.getProperty("line.separator"));
				deltas.append("Original (Non-Neutral):");
				deltas.append(System.getProperty("line.separator"));
				deltas.append(delta.getOriginal());
				deltas.append(System.getProperty("line.separator"));
				deltas.append(System.getProperty("line.separator"));
				deltas.append("Revised (Neutral):");
				deltas.append(System.getProperty("line.separator"));
				deltas.append(delta.getRevised());
				deltas.append(System.getProperty("line.separator"));
			}
			return deltas.toString();
		}

		/**
		 * Returns the deltas between beforeText and afterText as a line separated String.
		 * For more detailed diffs, use getPatch() or getUnifiedDiffStrings()
		 *
		 * @param difftype defines the type of diffs to include in the String
		 * @return diffs as line-separated String
		 */
		public String getLongDiffString(TYPE diffType) {
			StringBuilder deltas = new StringBuilder();
			for(Delta delta:getPatch().getDeltas()){
				if(delta.getType()==diffType){
					deltas.append("Original (Non-Neutral):");
					deltas.append(System.getProperty("line.separator"));
					deltas.append(delta.getOriginal());
					deltas.append(System.getProperty("line.separator"));
					deltas.append(System.getProperty("line.separator"));
					deltas.append("Revised (Neutral):");
					deltas.append(System.getProperty("line.separator"));
					deltas.append(delta.getRevised());
					deltas.append(System.getProperty("line.separator"));
					deltas.append("*********************************************");
					deltas.append(System.getProperty("line.separator"));
				}
			}
			return deltas.toString();
		}


		/**
		 * Returns the unified diff between "Before" and "After"
		 * containing one sentence per String.
		 * <code>contextSize</code> defines a window of lines/sentences around each change
		 * to display
		 *
		 * @param contextSize numer of lines/sentences around a change to display
		 * @return diffs as line-separated String
		 */
		public List<String> getUnifiedDiffStrings(int contextSize) {
			return DiffUtils.generateUnifiedDiff("Before", "After", sentenceSplit(beforeText), getPatch(), contextSize);
		}

		/**
		 * Returns the unified diff between "Before" and "After" as a single
		 * line-separated String
		 * @see getUnifiedDiffStrings()
		 *
		 * @param contextSize numer of characters around a change to display
		 * @return diffs as line-separated String
		 */
		public String getUnifiedDiffString(int contextSize) {
			return listToString(getUnifiedDiffStrings(contextSize));
		}


		/**
		 * Splits a String into sentences using the BreakIterator with
		 * US locale
		 *
		 * @param str a String with (multiple) sentences
		 * @return a list of Strings - one sentences per String
		 */
		private List<String> sentenceSplit(String str) {
			BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
			iterator.setText(str);
			int start = iterator.first();
			List<String> sentences = new ArrayList<String>();
			for (int end = iterator.next();
				 end != BreakIterator.DONE;
				 start = end, end = iterator.next()) {
				sentences.add(str.substring(start, end).trim());
			}
			return sentences;
		}

		/**
		 * Concatenates a list of Strings to one line-separated String
		 *
		 * @param stringList a list of Strings
		 * @return a single line-separated String containing all Strings from the list
		 */
		private String listToString(List<String> stringList){
			StringBuilder concat = new StringBuilder();
			for(String str:stringList){
				concat.append(str);
				concat.append(System.getProperty("line.separator"));
			}
			return concat.toString();
		}

		/**
		 * Normalizes the Strings in the TextPair.
		 * This mainly deals with whitespace-issues.
		 * Other normalizations can be included.
		 *
		 * @param str
		 * @return
		 */
		private String normalize(String str){
			str = StringUtils.trimToEmpty(str);
			str = StringUtils.normalizeSpace(str);

			// remove whitespace before punctuation. not using \p{Punct},
			// because it includes to many special characters.
			str = str.replaceAll("\\s+(?=[.!,\\?;:])", "");

			return str;
		}

	}
}
