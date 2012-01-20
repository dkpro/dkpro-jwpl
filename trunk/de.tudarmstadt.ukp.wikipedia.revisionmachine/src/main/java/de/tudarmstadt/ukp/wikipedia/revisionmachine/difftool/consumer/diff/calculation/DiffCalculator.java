/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Project Website:
 * 	http://jwpl.googlecode.com
 *
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DiffException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Surrogates;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.WikipediaXMLWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.DiffCalculatorInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.SurrogateModes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec.RevisionCodecData;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffAction;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * Calculates the Diff.
 *
 *
 *
 */
public class DiffCalculator
	implements DiffCalculatorInterface
{

	/**
	 * Configuration parameter - Flag, which indicates whether debug output is
	 * enabled or not
	 */
	private final boolean MODE_DEBUG_OUTPUT_ACTIVATED;

	/** Configuration parameter - Path for the DiffTool logger */
	private final String LOGGING_PATH_DIFFTOOL;

	/** Configuration parameter - Path for the debug logger */
	private final String LOGGING_PATH_DEBUG;

	/** Configuration parameter - Each x-th version is a full revision */
	private final int COUNTER_FULL_REVISION;

	/** Configuration parameter - Maximum size of a diff statement */
	private final long LIMIT_TASK_SIZE_DIFFS;

	/** Configuration parameter - Charset name of the input data */
	private final String WIKIPEDIA_ENCODING;

	/**
	 * Configuration parameter - Flag, which indicates whether the verification
	 * of the diff is enabled or not
	 */
	private final boolean VERIFICATION_DIFF;

	/** Configuration parameter - Value of the minimum legal substring */
	private final int VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING;

	/** Configuration parameter - Surrogate Mode */
	private final SurrogateModes MODE_SURROGATES;

	/** Reference to the TransTransmitter */
	private final TaskTransmitterInterface taskTransmitter;

	/** Reference to the BlockManager */
	private final BlockManagementInterface blocks;

	@Override
	public void closeTransmitter() throws IOException, SQLException {
		this.taskTransmitter.close();
	}

	/**
	 * (Constructor) Creates a new DiffCalculator object.
	 *
	 * @param taskTransmitter
	 *            Reference to the TaskTransmitter
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public DiffCalculator(final TaskTransmitterInterface taskTransmitter)
		throws ConfigurationException
	{
		this.taskTransmitter = taskTransmitter;
		this.blocks = new BlockManagement();

		this.articleID = -1;
		this.partCounter = 0;

		// Load config parameters
		ConfigurationManager config = ConfigurationManager.getInstance();

		MODE_DEBUG_OUTPUT_ACTIVATED = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_DEBUG_OUTPUT);

		LOGGING_PATH_DIFFTOOL = (String) config
				.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL);

		LOGGING_PATH_DEBUG = (String) config
				.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DEBUG);

		COUNTER_FULL_REVISION = (Integer) config
				.getConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION);

		LIMIT_TASK_SIZE_DIFFS = (Long) config
				.getConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_DIFFS);

		WIKIPEDIA_ENCODING = (String) config
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);

		VERIFICATION_DIFF = (Boolean) config
				.getConfigParameter(ConfigurationKeys.VERIFICATION_DIFF);

		VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING = (Integer) config
				.getConfigParameter(ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING);

		MODE_SURROGATES = (SurrogateModes) config
				.getConfigParameter(ConfigurationKeys.MODE_SURROGATES);
	}

	/*--------------------------------------------------------------------------*/

	/** Temporary variable - ID of the currently processed article */
	private int articleID;

	/** Temporary variable - Storage for the diffs */
	private Task<Diff> result;

	/** Temporary variable - Revision Counter */
	private int revisionCounter;

	/** Temporary variable - Part Counter */
	private int partCounter;

	/** Temporary variable - Diff Part */
	private DiffPart part;

	/** Temporary variable - content */
	private String text;

	/** Temporary variable - previous revision */
	private char[] revPrevious;

	/** Temporary variable - current revision */
	private char[] revCurrent;

	/** Temporary variable - temporary revision */
	private char[] revTemp;

	/** Temporary variable - Block Counter */
	private int blockCount;

	/**
	 * Temporary variable - Used to mark used characters of the previous
	 * revision
	 */
	private boolean[] revABlocked;

	/**
	 * Temporary variable - Used to mark used characters of the current revision
	 */
	private boolean[] revBBlocked;

	/**
	 * Temporary variable - Mapping of characters and their positions in the
	 * previous revision
	 */
	private HashMap<Character, ArrayList<Integer>> positions;

	/** Temporary variable - Queue for blocks of the previous revision */
	private ArrayList<DiffBlock> queueA;

	/** Temporary variable - Queue for blocks of the current revision */
	private ArrayList<DiffBlock> queueB;

	/** Temporary variable - size of the longest matching substring */
	private int longestMatch_size;

	/** Temporary variable - start position of the longest matching substring */
	private int longestMatch_start;

	/*--------------------------------------------------------------------------*/

	/**
	 * Initializes the processing of a RevisionTask using a new DiffTask.
	 *
	 * @param task
	 *            Reference to the DiffTask
	 */
	private void init(final Task<Revision> task)
	{
		this.partCounter++;
		this.result = new Task<Diff>(task.getHeader(), partCounter);
	}

	/**
	 * Initializes the processing of a new RevisionTask.
	 *
	 * @param taskID
	 *            Article ID
	 */
	protected void initNewTask(final int taskID)
	{

		this.articleID = taskID;

		this.partCounter = 0;
		this.revisionCounter = 0;

		this.revPrevious = null;
		this.revCurrent = null;
	}

	/**
	 * Generates a FullRevision.
	 *
	 * @param revision
	 *            Reference to the revision
	 * @return Diff, containing a FullRevision
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private Diff generateFullRevision(final Revision revision)
		throws UnsupportedEncodingException
	{

		Diff diff = new Diff();
		RevisionCodecData codecData = new RevisionCodecData();

		// FullRevisionUncompressed (C L T)
		part = new DiffPart(DiffAction.FULL_REVISION_UNCOMPRESSED);

		// L T
		text = revision.getRevisionText();
		revCurrent = text.toCharArray();

		part.setText(text);
		codecData.checkBlocksizeL(text.getBytes(WIKIPEDIA_ENCODING).length);

		diff.add(part);

		diff.setCodecData(codecData);
		return diff;
	}

	/**
	 * Transmits a partial DiffTask.
	 *
	 * @param result
	 *            Reference to the DiffTask
	 *
	 * @throws TimeoutException
	 *             if a timeout occurred
	 */
	protected void transmitPartialTask(final Task<Diff> result)
		throws TimeoutException
	{

		if (this.partCounter == 1) {

			this.result.setTaskType(TaskTypes.TASK_PARTIAL_FIRST);
			this.taskTransmitter.transmitDiff(result);

		}
		else {

			this.result.setTaskType(TaskTypes.TASK_PARTIAL);
			this.taskTransmitter.transmitPartialDiff(result);
		}
	}

	/**
	 * Transmits the DiffTask at the end of the RevisionTask processing.
	 *
	 * @param task
	 *            Reference to the RevisionTask
	 * @param result
	 *            Reference to the DiffTask
	 *
	 * @throws TimeoutException
	 *             if a timeout occurred
	 */
	protected void transmitAtEndOfTask(final Task<Revision> task,
			final Task<Diff> result)
		throws TimeoutException
	{

		if (task.getTaskType() == TaskTypes.TASK_FULL
				|| task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {

			if (this.partCounter > 1) {
				this.result.setTaskType(TaskTypes.TASK_PARTIAL_LAST);
				this.taskTransmitter.transmitPartialDiff(result);
			}
			else {
				this.result.setTaskType(TaskTypes.TASK_FULL);
				this.taskTransmitter.transmitDiff(result);
			}

			this.result = null;
		}
	}

	/**
	 * Calculates the diff for the given revision.
	 *
	 * @param revision
	 *            Reference to a revision
	 * @return Diff
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	protected Diff processRevision(final Revision revision)
		throws UnsupportedEncodingException
	{

		// ----------------------------------------------------//
		// ** HERE IS THE POINT TO INCLUDE ADDITIONAL FILTERS //
		// TO REMOVE FAULTY REVISIONS FROM FURTHER PROCESSING //
		// ----------------------------------------------------//

		try{
			if(revision.getRevisionText()==null){
				return null;
			}
		}catch(NullPointerException e){
			return null;
		}

		revTemp = revision.getRevisionText().toCharArray();

		if (MODE_SURROGATES == SurrogateModes.DISCARD_REVISION) {

			// Ignore Revision with surrogate characters
			if (Surrogates.scan(revTemp)) {
				return null;
			}
		}

		Diff diff;

		// Full revision
		if (revisionCounter % COUNTER_FULL_REVISION == 0) {

			diff = generateFullRevision(revision);

			// Diffed revision
		}
		else {

			diff = generateDiff(revPrevious, revTemp);

			// if the current revision is identical to the last valid revision
			if (diff.size() == 0) {
				return null;
			}
		}

		return diff;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.tud.ukp.kulessa.delta.consumers.diff.calculation.DiffCalculatorInterface
	 * #process(de.tud.ukp.kulessa.delta.data.Task)
	 */
	public void process(final Task<Revision> task)
		throws DiffException, TimeoutException, UnsupportedEncodingException
	{
		// this.startTime = System.currentTimeMillis();
		Revision revision;

		// check if a new task was received
		if (articleID != task.getHeader().getArticleId()) {

			// init settings
			initNewTask(task.getHeader().getArticleId());
			init(task);

			// check if old task was complete
		}
		else if (result == null) {

			init(task);
		}

		Diff diff;

		// TODO: Chronological order hotfix -
		// does not work for articles that are split across multiple tasks
		ArrayList<Revision> list = task.getContainer();
		Collections.sort(list);

		int i, rSize = list.size();

		for (i = 0; i < rSize; i++) {

			if (result.byteSize() > LIMIT_TASK_SIZE_DIFFS) {

				transmitPartialTask(result);
				init(task);
			}

			// Store previous revision
			revPrevious = revCurrent;

			// Process next revision
			revision = list.get(i);

			diff = processRevision(revision);

			if (diff != null) {

				revCurrent = revTemp;

				// Add to result
				revisionCounter++;

				diff.setRevisionCoutner(revisionCounter);
				diff.setRevisionID(revision.getRevisionID());
				diff.setTimeStamp(revision.getTimeStamp());
				diff.setComment(revision.getComment());
				diff.setContributorName(revision.getContributorName());
				diff.setContributorId(revision.getContributorId());
				diff.setContributorIsRegistered(revision.contributorIsRegistered());
				diff.setMinor(revision.isMinor());

				result.add(diff);

				// Verification
				if (VERIFICATION_DIFF) {
					String revC, revP;
					try {
						revC = String.valueOf(revCurrent);
						revP = diff.buildRevision(revPrevious);

						/*
						 * WRONG LOCATION if (notEqual && MODE_SURROGATES ==
						 * SurrogateModes.REPLACE) {
						 *
						 * // TODO: TEST: if (Surrogates.scan(revCurrent)) {
						 *
						 * char[] repCurrent = Surrogates.replace(revCurrent);
						 * char[] repPrevious = Surrogates.replace(revPrevious);
						 *
						 * revC = String.valueOf(repCurrent); revP =
						 * diff.buildRevision(repPrevious);
						 *
						 * notEqual = !revC.equals(revP); } }
						 */

						if (!revC.equals(revP)) {

							if (MODE_DEBUG_OUTPUT_ACTIVATED) {
								WikipediaXMLWriter writer = new WikipediaXMLWriter(
										LOGGING_PATH_DIFFTOOL
												+ LOGGING_PATH_DEBUG
												+ task.getHeader()
														.getArticleName()
												+ ".dbg");

								writer.writeRevision(task);
								writer.close();
							}

							throw ErrorFactory
									.createDiffException(
											ErrorKeys.DIFFTOOL_DIFFCONSUMER_DIFF_VERIFICATION_FAILED,
											"Reconstruction of "
													+ task.toString()
													+ " failed at revision "
													+ revisionCounter + ".");
						}

						// Throw again
					}
					catch (DiffException e) {
						throw e;

						// Catch unexpected exceptions
					}
					catch (Exception e) {
						throw ErrorFactory
								.createDiffException(
										ErrorKeys.DIFFTOOL_DIFFCONSUMER_DIFF_VERIFICATION_FAILED,
										"Reconstruction of " + task.toString()
												+ " failed at revision "
												+ revisionCounter + ".", e);
					}
				}
			}
		}

		transmitAtEndOfTask(task, result);
	}

	/**
	 * Generates a Diff by using the CommonLongestSubstring search.
	 *
	 * @param revA
	 *            previous revision
	 * @param revB
	 *            current revision
	 * @return Diff
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private Diff generateDiff(final char[] revA, final char[] revB)
		throws UnsupportedEncodingException
	{

		blockCount = 0;
		queueA = new ArrayList<DiffBlock>();
		queueB = new ArrayList<DiffBlock>();

		revABlocked = new boolean[revA.length];
		revBBlocked = new boolean[revB.length];

		int revAStartIndex = 0, revAEndIndex = revA.length - 1;
		int revBStartIndex = 0, revBEndIndex = revB.length - 1;

		while (revAStartIndex <= revAEndIndex && revBStartIndex <= revBEndIndex
				&& revA[revAStartIndex] == revB[revBStartIndex]) {

			revABlocked[revAStartIndex] = true;
			revBBlocked[revBStartIndex] = true;
			revAStartIndex++;
			revBStartIndex++;
		}

		// First Block
		if (revAStartIndex != 0) {
			queueA.add(new DiffBlock(this.blockCount, 0, revAStartIndex, 0,
					revBStartIndex, true));
			queueB.add(new DiffBlock(this.blockCount, 0, revAStartIndex, 0,
					revBStartIndex, false));
			this.blockCount++;
		}

		while (revAStartIndex < revAEndIndex && revBStartIndex < revBEndIndex
				&& revA[revAEndIndex] == revB[revBEndIndex]) {

			revABlocked[revAEndIndex] = true;
			revBBlocked[revBEndIndex] = true;
			revAEndIndex--;
			revBEndIndex--;
		}

		// Last Block
		if (revAEndIndex + 1 != revA.length) {
			queueA.add(new DiffBlock(this.blockCount, revAEndIndex + 1,
					revA.length, revBEndIndex + 1, revB.length, true));
			queueB.add(new DiffBlock(this.blockCount, revAEndIndex + 1,
					revA.length, revBEndIndex + 1, revB.length, false));
			this.blockCount++;
		}

		scan(revA, revAStartIndex, revAEndIndex);

		ArrayList<Integer> list;
		char c;

		int i = revBStartIndex;
		while (i < revBEndIndex) {

			c = revB[i];
			list = positions.get(c);

			if (list != null && findLongestMatch(revA, list, revB, i)) {

				i += longestMatch_size;
			}
			else {
				i++;
			}
		}

		int j;
		for (i = revAStartIndex; i <= revAEndIndex; i++) {
			if (!revABlocked[i]) {
				j = i;
				while (i + 1 <= revAEndIndex && !revABlocked[++i]) {
					;
				}

				if (i + 1 > revAEndIndex) {
					i++;
				}

				queueA.add(new DiffBlock(-1, j, i, -1, -1, true));
			}
		}

		for (i = revBStartIndex; i <= revBEndIndex; i++) {
			if (!revBBlocked[i]) {
				j = i;
				while (i + 1 <= revBEndIndex && !revBBlocked[++i]) {
					;
				}

				if (i + 1 > revBEndIndex) {
					i++;
				}

				queueB.add(new DiffBlock(-1, -1, -1, j, i, false));
			}
		}

		Collections.sort(queueA);
		Collections.sort(queueB);

		return blocks.manage(revA, revB, queueA, queueB);
	}

	/**
	 * Scans the input and creates the character -> position mapping.
	 *
	 * @param input
	 *            character array
	 * @param start
	 *            start position
	 * @param end
	 *            end position
	 */
	private void scan(final char[] input, final int start, final int end)
	{

		this.positions = new HashMap<Character, ArrayList<Integer>>();
		ArrayList<Integer> list;

		char c;
		for (int i = start; i < end; i++) {
			c = input[i];

			list = positions.get(c);
			if (list == null) {
				list = new ArrayList<Integer>();
				positions.put(c, list);
			}

			list.add(i);
		}
	}

	/**
	 * Searches the longest common substring
	 *
	 * @param revA
	 *            current revision
	 * @param list
	 *            list of start positions for this substring search
	 * @param revB
	 *            previous revision
	 * @param index
	 *            start index previous revision
	 *
	 * @return TRUE if a legal substring was found FALSE otherwise
	 */
	private boolean findLongestMatch(final char[] revA,
			final ArrayList<Integer> list, final char[] revB, final int index)
	{

		int match;
		longestMatch_size = -1;

		int size = list.size();
		int revAsize = revA.length;
		int revBsize = revB.length;

		int start, end, count;
		for (int i = 0; i < size; i++) {

			start = list.get(i);
			if (!revABlocked[start] && !revBBlocked[index + 1]) {

				count = index + 1;
				end = start + 1;

				while (end < revAsize && count < revBsize
						&& revA[end] == revB[count] && !revABlocked[end]
						&& !revBBlocked[count]) {
					end++;
					count++;
				}

				match = end - start;
				if (match > longestMatch_size) {
					longestMatch_size = match;
					longestMatch_start = start;
				}
			}
		}

		if (longestMatch_size <= VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING) {
			return false;
		}

		queueA.add(new DiffBlock(this.blockCount, longestMatch_start,
				longestMatch_start + longestMatch_size, index, index
						+ longestMatch_size, true));
		queueB.add(new DiffBlock(this.blockCount, longestMatch_start,
				longestMatch_start + longestMatch_size, index, index
						+ longestMatch_size, false));

		blockCount++;

		for (int i = 0, j = longestMatch_start, k = index; i < longestMatch_size; i++, j++, k++) {
			revABlocked[j] = true;
			revBBlocked[k] = true;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.tud.ukp.kulessa.delta.consumers.diff.calculation.DiffCalculatorInterface
	 * #reset()
	 */
	public void reset()
	{
		this.result = null;
	}
}
