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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableEnum;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableInputStream;

public class BinaryDumpTableInputStream extends DumpTableInputStream {

	protected InputStream inputStream = null;

	@Override
	public void initialize(InputStream inputStream, DumpTableEnum table)
			throws IOException {
		// just read from the stream without any data manipulations
		this.inputStream = inputStream;
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

}
