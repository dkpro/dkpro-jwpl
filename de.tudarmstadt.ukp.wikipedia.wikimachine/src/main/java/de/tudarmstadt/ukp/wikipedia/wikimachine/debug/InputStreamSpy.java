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
