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
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.EOFException;
import java.io.IOException;

import de.tudarmstadt.ukp.wikipedia.timemachine.domain.Revision;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;

public class TimeMachineRevisionParser extends RevisionParser {

	public boolean next() throws IOException {
		boolean hasNext = true;
		try {
			revPage = stream.readInt();
			revTextId = stream.readInt();
			revTimestamp = Revision.compressTime(stream.readLong());
		} catch (EOFException e) {
			hasNext = false;
		}

		return hasNext;

	}

}
