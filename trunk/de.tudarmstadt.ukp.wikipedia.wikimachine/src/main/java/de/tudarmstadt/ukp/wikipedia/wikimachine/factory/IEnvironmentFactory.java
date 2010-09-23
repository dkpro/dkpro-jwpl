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
package de.tudarmstadt.ukp.wikipedia.wikimachine.factory;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.decompression.IDecompressor;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.DumpVersionProcessor;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.ISnapshotGenerator;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableInputStream;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;

public interface IEnvironmentFactory {
	public ILogger getLogger();

	public IDecompressor getDecompressor();

	public ISnapshotGenerator getSnapshotGenerator();

	public DumpVersionProcessor getDumpVersionProcessor();

	public IDumpVersion getDumpVersion();

	public DumpTableInputStream getDumpTableInputStream();

	public PageParser getPageParser();

	public RevisionParser getRevisionParser();

	public TextParser getTextParser();
}
