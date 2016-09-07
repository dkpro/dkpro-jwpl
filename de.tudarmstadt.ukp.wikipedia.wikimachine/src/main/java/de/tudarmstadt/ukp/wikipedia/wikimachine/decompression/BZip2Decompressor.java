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
package de.tudarmstadt.ukp.wikipedia.wikimachine.decompression;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * BZip2 Decompressor (based on Singleton Design Pattern). Uses getInputStream
 * to set up the archive path and returns the InputStream to read from
 *
 *
 */
public class BZip2Decompressor implements IDecompressor {

	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		FileInputStream inputStream;
		InputStream outputStream = null;

		inputStream = new FileInputStream(fileName);
		/**
		 * skip 2 first bytes (see the documentation of CBZip2InputStream) e.g.
		 * here http://lucene.apache.org/tika/xref/org/apache/tika/parser
		 * /pkg/bzip2 /CBZip2InputStream.html
		 */
		inputStream.skip(2);
		outputStream = new CBZip2InputStream(inputStream);

		return outputStream;

	}

}
