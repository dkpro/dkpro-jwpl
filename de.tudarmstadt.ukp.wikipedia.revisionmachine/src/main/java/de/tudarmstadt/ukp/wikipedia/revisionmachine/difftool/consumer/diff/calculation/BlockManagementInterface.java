/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * Interface of the BlockManagement
 *
 *
 *
 */
public interface BlockManagementInterface
{

	/**
	 * Uses the substring blocks to create the diff operations.
	 *
	 * @param revA
	 *            revision A
	 * @param revB
	 *            revision B
	 * @param queueA
	 *            queue A
	 * @param queueB
	 *            queue B
	 * @return Diff
	 *
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	public Diff manage(final char[] revA, final char[] revB,
			final ArrayList<DiffBlock> queueA, final ArrayList<DiffBlock> queueB)
		throws UnsupportedEncodingException;

}
