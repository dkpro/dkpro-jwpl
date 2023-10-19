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
package de.tudarmstadt.ukp.wikipedia.datamachine.dump.xml;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import de.tudarmstadt.ukp.wikipedia.datamachine.domain.DataMachineFiles;
import de.tudarmstadt.ukp.wikipedia.datamachine.file.DeleteFilesAtShutdown;
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

	public SimpleBinaryDumpWriter(DataMachineFiles files) throws IOException {
		this.files = files;
		if (this.files.isCompressGeneratedFiles()) {
			createCompressed();
		} else {
			createUncompressed();
		}
	}

	protected void createUncompressed() throws IOException {
		pageFile = openUTFDataOutputStream(files.getGeneratedPage(), false);
		revisionFile = openUTFDataOutputStream(files.getGeneratedRevision(), false);
		textFile = openUTFDataOutputStream(files.getGeneratedText(), false);
	}

	protected void createCompressed() throws IOException {
		pageFile = openUTFDataOutputStream(files.getGeneratedPage(), true);
		revisionFile = openUTFDataOutputStream(files.getGeneratedRevision(), true);
		textFile = openUTFDataOutputStream(files.getGeneratedText(), true);
	}

	private UTFDataOutputStream openUTFDataOutputStream(final String filePath, final boolean compressed) throws IOException {
		UTFDataOutputStream utfDataOutputStream;
		if(compressed) {
			utfDataOutputStream = new UTFDataOutputStream(new GZIPOutputStream(openFileStreamAndRegisterDeletion(filePath)));
		} else {
			utfDataOutputStream = new UTFDataOutputStream(openFileStreamAndRegisterDeletion(filePath));
		}
		return utfDataOutputStream;
	}

	private BufferedOutputStream openFileStreamAndRegisterDeletion(final String filePath) throws IOException {
		Path binaryOutputFilePath = Paths.get(filePath);
		// JavaDoc says:
		// "truncate and overwrite an existing file, or create the file if it doesn't initially exist"
		OutputStream fileOutputStream = Files.newOutputStream(binaryOutputFilePath);

		// Register a delete hook on JVM shutdown for this path
		DeleteFilesAtShutdown.register(binaryOutputFilePath);

		// Create a buffered version for this
		return new BufferedOutputStream(fileOutputStream);
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
		pageFile.writeUTFAsArray(SQLEscape.escape(SQLEscape.titleFormat(page.Title.Text)));
		// pageFile.writeBoolean(revision.isRedirect());
		pageFile.writeBoolean(Redirects.isRedirect(revision.Text));
	}
}
