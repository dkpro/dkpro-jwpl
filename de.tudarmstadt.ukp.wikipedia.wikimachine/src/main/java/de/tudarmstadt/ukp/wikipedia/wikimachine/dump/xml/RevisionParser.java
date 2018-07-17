/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class RevisionParser {

	protected int revPage;
	protected int revTextId;
	protected int revTimestamp;

	protected DataInputStream stream;

	/**
	 * Create a parser from an input stream
	 *
		 * @param inputStream
	 */
	public void setInputStream(InputStream inputStream){
		stream = new DataInputStream(inputStream);
	}

	public int getRevPage() {
		return revPage;
	}

	public int getRevTextId() {
		return revTextId;
	}

	public int getRevTimestamp() {
		return revTimestamp;
	}

	public void close() throws IOException {
		stream.close();
	}

	/**
	 * Returns true if the table has more rows.
	 *
	 * @return
	 * @throws IOException
	 */
	public abstract boolean next() throws IOException;

}
