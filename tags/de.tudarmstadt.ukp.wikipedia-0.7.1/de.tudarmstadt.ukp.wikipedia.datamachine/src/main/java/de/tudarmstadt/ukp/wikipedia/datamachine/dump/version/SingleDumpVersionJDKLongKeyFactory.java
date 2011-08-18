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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.version;

import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersionFactory;
import de.tudarmstadt.ukp.wikipedia.wikimachine.hashing.StringHashCodeJBoss;

public class SingleDumpVersionJDKLongKeyFactory implements IDumpVersionFactory {

	@Override
	public IDumpVersion getDumpVersion() {
		IDumpVersion dumpVersion = null;
		try {
			dumpVersion = new SingleDumpVersionJDKGeneric<Long, StringHashCodeJBoss>(
					StringHashCodeJBoss.class);
		} catch (Exception e) {
			dumpVersion = null;
		}
		return dumpVersion;
	}

}
