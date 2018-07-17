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
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.version;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersionFactory;
import de.tudarmstadt.ukp.wikipedia.wikimachine.hashing.StringHashCodeJBoss;

public class DumpVersionJDKLongKeyFactory implements IDumpVersionFactory {

	@Override
	public IDumpVersion getDumpVersion() {
		IDumpVersion dumpVersion = null;
		try {
			dumpVersion = new DumpVersionJDKGeneric<Long, StringHashCodeJBoss>(
					StringHashCodeJBoss.class);
		} catch (Exception e) {
			dumpVersion = null;
		}
		return dumpVersion;
	}

}
