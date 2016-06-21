/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
