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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.wikimachine.util.UTFDataInputStream;

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
	 * @author ivan.galkin
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
