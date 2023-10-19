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
package org.dkpro.jwpl.wikimachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.dkpro.jwpl.wikimachine.util.UTFDataInputStream;

public class PageParser {
	/**
	 * Needed fields from the table page.<br>
	 * These fields are updated each time the method next() returns true.
	 */
	protected int pageId;
	protected int pageNamespace;
	protected String pageTitle;
	protected boolean pageIsRedirect;

	protected UTFDataInputStream stream;

	/**
	 * Create a parser from an input stream
	 *
	 * @param inputStream
		 */
	public void setInputStream(InputStream inputStream) {
		stream = new UTFDataInputStream(inputStream);
	}

	/**
	 * @return Returns the page_id.
	 */
	public int getPageId() {
		return pageId;
	}

	/**
	 * @return Returns the page_is_redirect.
	 */
	public boolean getPageIsRedirect() {
		return pageIsRedirect;
	}

	/**
	 * @return Returns the page_namespace.
	 */
	public int getPageNamespace() {
		return pageNamespace;
	}

	/**
	 * @return Returns the page_title.
	 */
	public String getPageTitle() {
		return pageTitle;
	}

	/**
	 * Returns true if the table has more rows.
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean next() throws IOException {
		boolean hasNext = true;
		try {
			pageId = stream.readInt();
			pageNamespace = stream.readInt();
			pageTitle = stream.readUTFAsArray();
			pageIsRedirect = stream.readBoolean();
		} catch (EOFException e) {
			hasNext = false;
		}
		return hasNext;
	}

	public void close() throws IOException {
		stream.close();
	}
}