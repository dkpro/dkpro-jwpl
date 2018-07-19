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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * Represents a pair of Strings. Usually corresponding to a RevisionPair.
 *
 *
 */
public class TextPair {
	private String beforeText;
	private String afterText;
	/**
	 * Holds arbitrary String-MetaData
	 */
	private Map<String, String> metaData;

	public TextPair(String before, String after) {
		this.beforeText = normalize(before);
		this.afterText = normalize(after);
		setMetaData(new HashMap<String, String>());
	}

	public String getBeforeText() {
		return beforeText;
	}

	public List<String> getBeforeLines() {
		return sentenceSplit(beforeText);
	}

	public void setBeforeText(String beforeText) {
		this.beforeText = normalize(beforeText);
	}

	public String getAfterText() {
		return afterText;
	}

	public List<String> getAfterLines() {
		return sentenceSplit(afterText);
	}

	public void setAfterText(String afterText) {
		this.afterText = normalize(afterText);
	}

	public Map<String, String> getMetaData()
	{
		return metaData;
	}

	public void setMetaData(Map<String, String> metaData)
	{
		this.metaData = metaData;
	}

	public void addMetaData(String key, String value)
	{
		metaData.put(key, value);
	}

	public String getMetaDataValue(String key)
	{
		return metaData.get(key);
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
	 * @param diffType defines the type of diffs to include in the String
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

    @Override
	public boolean equals(Object anObject) {
    	if(!(anObject instanceof TextPair)){
    		return false;
    	}else{
    		TextPair otherPair = (TextPair)anObject;
			if (this.getBeforeText().equals(otherPair.getBeforeText())&&this.getAfterText().equals(otherPair.getAfterText())) {
    			return true;
    		}else{
    			return false;
    		}
    	}
    }

}