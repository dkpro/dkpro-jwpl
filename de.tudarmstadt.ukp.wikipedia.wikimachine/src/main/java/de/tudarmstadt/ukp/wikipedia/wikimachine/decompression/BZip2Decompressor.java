/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.decompression;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * BZip2 Decompressor (based on Singleton Design Pattern). Uses getInputStream
 * to set up the archive path and returns the InputStream to read from
 * 
 * @author ivan.galkin
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
