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
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Writes the dumps of tables as txt files.
 * 
 * @author Anouar
 * 
 */

public class TxtFileWriter extends PrintStream {

	private static final String ENCODING = "UTF-8";
	private static final boolean AUTOFLUSH = false;

	/**
	 * Instantiates a new TxtFileWriter object.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public TxtFileWriter(String filename) throws IOException {
		super(new FileOutputStream(filename), AUTOFLUSH, ENCODING);
	}

	/**
	 * Add a row to the dump of the table.
	 * 
	 * @param row
	 * @throws IOException
	 */
	public void addRow(Object... row) throws IOException {
		super.print(Strings.join(row, "\t") + "\n");
	}

	public void export() {
		super.flush();
		super.close();
	}

}
