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
/**
 * @(#)RevisionParser.java
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
	 * @author ivan.galkin
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
