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
