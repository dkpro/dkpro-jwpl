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
