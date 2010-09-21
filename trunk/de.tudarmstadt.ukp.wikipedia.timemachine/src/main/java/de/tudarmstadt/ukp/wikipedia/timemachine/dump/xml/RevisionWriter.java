/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.Page;
import org.mediawiki.importer.Revision;
import org.mediawiki.importer.Siteinfo;

public class RevisionWriter implements DumpWriter {

	private Page currentPage;
	private DataOutputStream stream;

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
