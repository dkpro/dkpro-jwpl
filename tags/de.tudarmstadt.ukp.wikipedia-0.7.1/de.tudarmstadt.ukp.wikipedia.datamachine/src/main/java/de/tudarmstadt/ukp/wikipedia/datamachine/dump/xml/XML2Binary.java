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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.datamachine.domain.DataMachineFiles;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceFilter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.XmlDumpReader;

/**
 *
 * Use org.mediawiki.importer engine to parse the XML-Dump (only useful fields)
 * and store it to binary file. Compression of the output files is possible.
 *
 * @see de.tudarmstadt.ukp.wikipedia.wikimachine.decompression.Files
 * @see de.tudarmstadt.ukp.wikipedia.wikimachine.decompression.Files#setCompressGeneratedFiles(boolean)
 * @see org.mediawiki.importer.NamespaceFilter
 *
 * @author ivan.galkin
 *
 */
public class XML2Binary {
	/**
	 * Enable the main and category pages as well as discussions
	 */
	private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

	private static boolean USE_MODIFED_PARSER = true;

	public XML2Binary(InputStream iStream, DataMachineFiles files)
			throws IOException {
		if (USE_MODIFED_PARSER) {
			// modified parser, skips faulty tags
			new SimpleXmlDumpReader(iStream, new NamespaceFilter(
					new SimpleBinaryDumpWriter(files), ENABLED_NAMESPACES))
					.readDump();
		} else {
			// original MWDumper parser, very sensible to not closed tags
			new XmlDumpReader(iStream, new NamespaceFilter(
					new SimpleBinaryDumpWriter(files), ENABLED_NAMESPACES))
					.readDump();
		}
	}

}
