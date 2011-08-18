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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.codec;

import java.io.UnsupportedEncodingException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.EncodingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * The RevisionApi Interface describes the link to the diff encoding unit.
 * 
 * 
 * 
 */
public interface RevisionEncoderInterface
{

	/**
	 * Returns the textual encoding of the given Diff.
	 * 
	 * @param codecData
	 *            CodecData used to encode the diff-data
	 * @param diff
	 *            diff-data
	 * @return base 64 encoded diff
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the CharacterSet defined in the configuration is not
	 *             supported by JAVA.
	 * 
	 * @throws EncodingException
	 *             if the encoding process fails
	 */
	public String encodeDiff(final RevisionCodecData codecData, final Diff diff)
		throws UnsupportedEncodingException, EncodingException;

	/**
	 * Returns the binary encoding of the given Diff.
	 * 
	 * @param codecData
	 *            CodecData used to encode the diff-data
	 * @param diff
	 *            diff-data
	 * @return binary encoded diff
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the CharacterSet defined in the configuration is not
	 *             supported by JAVA.
	 * 
	 * @throws EncodingException
	 *             if the encoding process fails
	 */
	public byte[] binaryDiff(final RevisionCodecData codecData, final Diff diff)
		throws UnsupportedEncodingException, EncodingException;

}
