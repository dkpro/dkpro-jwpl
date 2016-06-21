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
package de.tudarmstadt.ukp.wikipedia.mwdumper.importer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SqlFileStream implements SqlStream {
	protected PrintStream stream;
	
	public SqlFileStream(OutputStream output) throws IOException {
		this.stream = new PrintStream(output, false, "UTF-8");
	}
	
	public void writeComment(CharSequence sql) {
		stream.println(sql.toString());
	}
	
	public void writeStatement(CharSequence sql) {
		stream.print(sql.toString());
		stream.println(';');
	}
	
	public void close() {
		stream.flush();
		stream.close();
	}
}
