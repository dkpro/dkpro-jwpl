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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class FileMemoryLogger extends AbstractLogger {

	private static final String FILEHEADER = "\"Date/Time\",\"Total Memory\",\"Free Memory\",\"Message\"";

	private static final Logger log4j = Logger
			.getLogger(FileMemoryLogger.class);

	private static final SimpleDateFormat FILENAME_FORMAT = new SimpleDateFormat(
			"yyyyMMdd_HHmmss");
	private static final SimpleDateFormat DATEFIELD_FORMAT = new SimpleDateFormat(
			"yyyy.MM.dd HH:mm:ss");

	public static String now(SimpleDateFormat format) {
		return format.format(new Date());
	}

	private PrintStream output = null;

	public FileMemoryLogger() {

		try {
			output = new PrintStream(new FileOutputStream(FILENAME_FORMAT
					.format(new Date()).concat(".txt")));
			output.println(FILEHEADER);
		} catch (FileNotFoundException e) {
			log4j.error(e.getMessage());
			output = null;
		}

	}

	@Override
	public void logObject(Object message) {
		if (output != null) {
			output.println("\"" + DATEFIELD_FORMAT.format(new Date()) + "\",\""
					+ Runtime.getRuntime().totalMemory() + "\",\""
					+ Runtime.getRuntime().freeMemory() + "\",\"" + message
					+ "\"");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		output.close();
		super.finalize();
	}

}
