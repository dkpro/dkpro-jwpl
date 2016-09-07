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
