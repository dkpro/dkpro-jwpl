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

import java.io.InputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.AbstractXmlDumpReader;

/**
 * This class is a specified variant of
 * {@link org.mediawiki.importer.XmlDumpReader}. Please see its source for more
 * information about a functionality and a license.<br>
 *
 * @author ivan.galkin
 *
 */
public class TextReader extends AbstractXmlDumpReader {

	public TextReader(InputStream inputStream, DumpWriter writer) {
		super(inputStream, writer);
	}

	@Override
	protected void setupStartElements() {
		startElements.put(REVISION, REVISION);
		startElements.put(CONTRIBUTOR, CONTRIBUTOR);
		startElements.put(PAGE, PAGE);
		startElements.put(SITEINFO, SITEINFO);
		startElements.put(NAMESPACES, NAMESPACES);
		startElements.put(NAMESPACE, NAMESPACE);
	}

	@Override
	protected void setupEndElements() {
		endElements.put(REVISION, REVISION);
		endElements.put(TEXT, TEXT);
		endElements.put(CONTRIBUTOR, CONTRIBUTOR);
		endElements.put(ID, ID);
		endElements.put(PAGE, PAGE);
		endElements.put(TITLE, TITLE);
		endElements.put(SITEINFO, SITEINFO);
		endElements.put(NAMESPACES, NAMESPACES);
		endElements.put(NAMESPACE, NAMESPACE);
	}
}
