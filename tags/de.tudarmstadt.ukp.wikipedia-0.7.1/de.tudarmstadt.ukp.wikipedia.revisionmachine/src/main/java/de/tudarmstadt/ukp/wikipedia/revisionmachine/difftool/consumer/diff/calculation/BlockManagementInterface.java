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
