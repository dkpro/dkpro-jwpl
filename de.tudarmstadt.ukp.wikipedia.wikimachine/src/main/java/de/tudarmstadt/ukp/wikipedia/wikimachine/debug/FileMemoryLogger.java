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
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A logger implementation which directs its logging output to a file with a CSV format.
 *
 * The format - and it's semantics - is defined by the header: {@link FileMemoryLogger#FILEHEADER}.
 */
public class FileMemoryLogger extends AbstractLogger {

	private static final String FILEHEADER = "\"Date/Time\",\"Total Memory\",\"Free Memory\",\"Message\"";

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
			output = new PrintStream(new BufferedOutputStream(new FileOutputStream(FILENAME_FORMAT
					.format(new Date()).concat(".txt"))));
			output.println(FILEHEADER);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
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
