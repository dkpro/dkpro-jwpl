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
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Writes the dumps of tables as txt files.
 *
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
