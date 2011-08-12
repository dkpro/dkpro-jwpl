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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Page;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Revision;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Siteinfo;

public class RevisionWriter implements DumpWriter {

	private Page currentPage;
	private final DataOutputStream stream;

	public RevisionWriter(OutputStream output) throws IOException {
		this.stream = new DataOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void writeEndPage() throws IOException {
		currentPage = null;
	}

	@Override
	public void writeEndWiki() throws IOException {
		 stream.flush();
	}

	@Override
	public void writeRevision(Revision revision) throws IOException {
		stream.writeInt(currentPage.Id);
		stream.writeInt(revision.Id);
		stream.writeLong(revision.Timestamp.getTimeInMillis());
	}

	@Override
	public void writeSiteinfo(Siteinfo info) throws IOException {

	}

	@Override
	public void writeStartPage(Page page) throws IOException {
		currentPage = page;
	}

	@Override
	public void writeStartWiki() throws IOException {
	}
}
