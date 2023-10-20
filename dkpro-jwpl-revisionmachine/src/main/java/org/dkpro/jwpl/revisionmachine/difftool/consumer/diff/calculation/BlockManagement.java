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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationManager;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionCodecData;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffAction;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.DiffPart;

/**
 * The BlockManagement class is used to calculate the diff operations using the
 * blocks of the longest common substring search.
 *
 *
 *
 */
public class BlockManagement
	implements BlockManagementInterface
{

	/** Configuration parameter - Charset name of the input data */
	private static String WIKIPEDIA_ENCODING;

	/** Temporary variable - Just in Time revision */
	private StringBuilder version;

	/** Temporary variable - Diff */
	private Diff diff;

	/** Temporary variable - Storage for intermediate blocks */
	private Map<Integer, String> bufferMap;

	/** Reference to the codec */
	private RevisionCodecData codecData;

	/**
	 * (Constructor) Creates a BlockManagement object.
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public BlockManagement()
		throws ConfigurationException
	{

		ConfigurationManager config = ConfigurationManager.getInstance();
		WIKIPEDIA_ENCODING = (String) config
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.tudarmstadt.ukp.kulessa.delta.consumers.diff.calculation.
	 * BlockManagementInterface#manage(char[], char[], java.util.ArrayList,
	 * java.util.ArrayList)
	 */
	public Diff manage(char[] revA, char[] revB, ArrayList<DiffBlock> queueA,
			ArrayList<DiffBlock> queueB)
		throws UnsupportedEncodingException
	{

		this.diff = new Diff();
		this.codecData = new RevisionCodecData();

		this.bufferMap = new HashMap<Integer, String>();
		this.version = new StringBuilder();

		DiffBlock curA = null, curB = null;
		while (!queueA.isEmpty() || !queueB.isEmpty() || curB != null) {

			if (!queueA.isEmpty() && curA == null) {
				curA = queueA.remove(0);
			}
			if (!queueB.isEmpty() && curB == null) {
				curB = queueB.remove(0);
			}

			if (curA != null && curB != null) {

				if (curA.getId() == curB.getId()) {

					if (curA.getId() == -1) {
						replace(revA, revB, curA, curB);
					}
					else {
						version.append(copy(revA, curA.getRevAStart(),
								curA.getRevAEnd()));
					}

					curA = null;
					curB = null;

				}
				else if (curA.getId() == -1) {

					delete(curA);
					curA = null;

				}
				else if (curB.getId() == -1) {

					insert(revB, curB);
					curB = null;

				}
				else {

					// Difference :(
					if (bufferMap.containsKey(curB.getId())) {

						paste(curB);
						curB = null;

					}
					else {

						cut(revA, curA);
						curA = null;

						// System.out.println("@TO CUT: " + curA.getId() + "\t<"
						// + text + ">");
					}
				}

			}
			else if (curA != null) {

				delete(curA);
				curA = null;

			}
			else if (curB != null) {

				// Difference :(
				if (bufferMap.containsKey(curB.getId())) {

					paste(curB);
					curB = null;

				}
				else {

					insert(revB, curB);
					curB = null;
				}

			}
			else {
				System.err.println("INVALID CASE");
				System.exit(-1);
			}
		}

		diff.setCodecData(codecData);
		return diff;
	}

	/*-PRIVATE-METHODS----------------------------------------------------------*/

	/**
	 * Copies the specified interval of characters for the array.
	 *
	 * @return specified interval
	 */
	private String copy(final char[] array, final int start, final int end)
	{
		StringBuilder text = new StringBuilder();
		for (int j = start; j < end; j++) {
			text.append(array[j]);
		}

		return text.toString();
	}

	/**
	 * Creates an insert operation.
	 *
	 * @param revB
	 *            revision B
	 * @param curB
	 *            Reference to the block B
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void insert(final char[] revB, final DiffBlock curB)
		throws UnsupportedEncodingException
	{

		String text = copy(revB, curB.getRevBStart(), curB.getRevBEnd());

		// Insert (C S L T)
		DiffPart action = new DiffPart(DiffAction.INSERT);

		// S
		action.setStart(version.length());
		codecData.checkBlocksizeS(version.length());

		// L T
		action.setText(text);
		codecData.checkBlocksizeL(text.getBytes(WIKIPEDIA_ENCODING).length);

		diff.add(action);

		version.append(text);
	}

	/**
	 * Creates a delete operation.
	 *
	 * @param curA
	 *            Reference to the block A
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void delete(final DiffBlock curA)
	{

		// Delete (C S E)
		DiffPart action = new DiffPart(DiffAction.DELETE);

		// S
		action.setStart(version.length());
		codecData.checkBlocksizeS(version.length());

		// E
		action.setLength(curA.getRevAEnd() - curA.getRevAStart());
		codecData.checkBlocksizeE(action.getLength());

		diff.add(action);
	}

	/**
	 * Creates a replace operation.
	 *
	 * @param revA
	 *            Reference to revision A
	 * @param revB
	 *            Reference to revision B
	 * @param curA
	 *            Reference to current block A
	 * @param curB
	 *            Reference to current block B
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void replace(final char[] revA, final char[] revB,
			final DiffBlock curA, final DiffBlock curB)
		throws UnsupportedEncodingException
	{

		// Replace (C S E L T)
		String text = copy(revB, curB.getRevBStart(), curB.getRevBEnd());

		DiffPart action = new DiffPart(DiffAction.REPLACE);

		// S
		action.setStart(version.length());
		codecData.checkBlocksizeS(version.length());

		// E
		action.setLength(curA.getRevAEnd() - curA.getRevAStart());
		codecData.checkBlocksizeE(action.getLength());

		// L T
		action.setText(text);
		codecData.checkBlocksizeL(text.getBytes(WIKIPEDIA_ENCODING).length);

		diff.add(action);

		version.append(text);
	}

	/**
	 * Creates a cut operation.
	 *
	 * @param revA
	 *            Reference to revision A
	 * @param curA
	 *            Reference to current block A
	 */
	private void cut(final char[] revA, final DiffBlock curA)
	{

		String text = copy(revA, curA.getRevAStart(), curA.getRevAEnd());

		// Cut (C S E B)
		DiffPart action = new DiffPart(DiffAction.CUT);

		// S
		action.setStart(version.length());
		codecData.checkBlocksizeS(version.length());

		// E
		action.setLength(curA.getRevAEnd() - curA.getRevAStart());
		codecData.checkBlocksizeE(action.getLength());

		// B
		action.setText(Integer.toString(curA.getId()));
		codecData.checkBlocksizeB(curA.getId());

		diff.add(action);

		bufferMap.put(curA.getId(), text);
	}

	/**
	 * Creates a paste operation.
	 *
	 * @param curB
	 *            Reference to current block B
	 */
	private void paste(final DiffBlock curB)
	{

		String text = bufferMap.remove(curB.getId());

		// Paste (C S B)
		DiffPart action = new DiffPart(DiffAction.PASTE);

		// S
		action.setStart(version.length());
		codecData.checkBlocksizeS(version.length());

		// B
		action.setText(Integer.toString(curB.getId()));
		codecData.checkBlocksizeB(curB.getId());

		diff.add(action);

		version.append(text);
	}
}
