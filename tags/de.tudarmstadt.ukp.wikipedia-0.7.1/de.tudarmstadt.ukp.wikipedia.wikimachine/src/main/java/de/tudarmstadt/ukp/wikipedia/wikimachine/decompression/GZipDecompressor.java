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
import java.util.zip.GZIPInputStream;

/**
 * GZip Decompressor (based on Singleton Design Pattern). Uses getInputStream to
 * set up the archive path and returns the InputStream to read from
 * 
 * @author ivan.galkin
 * 
 */
public class GZipDecompressor implements IDecompressor {

	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		InputStream inputStream = null;

		inputStream = new GZIPInputStream(new FileInputStream(fileName));

		return inputStream;
	}

}
