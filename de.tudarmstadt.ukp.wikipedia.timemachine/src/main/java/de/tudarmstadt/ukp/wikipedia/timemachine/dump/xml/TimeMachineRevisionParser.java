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
