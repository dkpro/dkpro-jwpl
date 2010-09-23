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
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamSpy extends InputStream {

	private InputStream iStream;
	private OutputStream oStream;
	
	public InputStreamSpy(InputStream iStream, OutputStream oStream){
		this.iStream = iStream;
		this.oStream = oStream;
	}
	
	@Override
	public int read() throws IOException {
		int result = iStream.read();
		oStream.write(result);
		return result;		
	}
	
	@Override
	public int available() throws IOException {
		return iStream.available();
	}

	@Override
	public void close() throws IOException {
		iStream.close();
		oStream.flush();
	}

	@Override
	public void mark(int readlimit) {
		iStream.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		iStream.reset();
	}

	@Override
	public boolean markSupported() {
		return iStream.markSupported();
	}


}
