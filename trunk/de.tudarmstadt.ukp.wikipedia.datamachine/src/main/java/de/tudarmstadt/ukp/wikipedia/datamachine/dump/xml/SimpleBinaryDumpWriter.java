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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import de.tudarmstadt.ukp.wikipedia.datamachine.domain.DataMachineFiles;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Page;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Revision;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Siteinfo;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.SQLEscape;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.Redirects;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.UTFDataOutputStream;

public class SimpleBinaryDumpWriter implements DumpWriter {

	private UTFDataOutputStream pageFile;
	private UTFDataOutputStream revisionFile;
	private UTFDataOutputStream textFile;
	private final DataMachineFiles files;

	private Page currentPage;
	private Revision lastRevision;

	protected void createUncompressed() throws IOException {
		pageFile = new UTFDataOutputStream(new FileOutputStream(files
				.getGeneratedPage()));
		revisionFile = new UTFDataOutputStream(new FileOutputStream(files
				.getGeneratedRevision()));
		textFile = new UTFDataOutputStream(new FileOutputStream(files
				.getGeneratedText()));
	}

	protected void createCompressed() throws IOException {

		pageFile = new UTFDataOutputStream(new GZIPOutputStream(
				new FileOutputStream(files.getGeneratedPage())));
		revisionFile = new UTFDataOutputStream(new GZIPOutputStream(
				new FileOutputStream(files.getGeneratedRevision())));
		textFile = new UTFDataOutputStream(new GZIPOutputStream(
				new FileOutputStream(files.getGeneratedText())));

	}

	public SimpleBinaryDumpWriter(DataMachineFiles files) throws IOException {
		this.files = files;
		if (this.files.isCompressGeneratedFiles()) {
			createCompressed();
		} else {
			createUncompressed();
		}
	}

	@Override
	public void close() throws IOException {
		pageFile.close();
		revisionFile.close();
		textFile.close();
	}

	@Override
	public void writeEndPage() throws IOException {
		if (lastRevision != null) {
			updatePage(currentPage, lastRevision);
		}
		currentPage = null;
		lastRevision = null;
	}

	@Override
	public void writeEndWiki() throws IOException {
		pageFile.flush();
		revisionFile.flush();
		textFile.flush();
	}

	@Override
	public void writeRevision(Revision revision) throws IOException {
		lastRevision = revision;

		revisionFile.writeInt(currentPage.Id);
		revisionFile.writeInt(revision.Id);

		textFile.writeInt(revision.Id);
		textFile.writeUTFAsArray(SQLEscape.escape(revision.Text));
	}

	@Override
	public void writeSiteinfo(Siteinfo info) throws IOException {
	}

	@Override
	public void writeStartPage(Page page) throws IOException {
		currentPage = page;
		lastRevision = null;
	}

	@Override
	public void writeStartWiki() throws IOException {
	}

	private void updatePage(Page page, Revision revision) throws IOException {
		pageFile.writeInt(page.Id);
		pageFile.writeInt(page.Title.Namespace);
		pageFile.writeUTFAsArray(SQLEscape.escape(SQLEscape
				.titleFormat(page.Title.Text)));
		// pageFile.writeBoolean(revision.isRedirect());
		pageFile.writeBoolean(Redirects.isRedirect(revision.Text));
	}
}
