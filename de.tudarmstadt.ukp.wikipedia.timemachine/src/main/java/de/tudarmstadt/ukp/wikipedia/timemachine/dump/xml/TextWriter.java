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

import java.io.IOException;
import java.io.OutputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Page;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Revision;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Siteinfo;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.SQLEscape;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.UTFDataOutputStream;

public class TextWriter implements DumpWriter {

	private final UTFDataOutputStream stream;

	public TextWriter(OutputStream output) throws IOException {
		this.stream = new UTFDataOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void writeEndPage() throws IOException {
	}

	@Override
	public void writeEndWiki() throws IOException {
		stream.flush();
	}

	@Override
	public void writeRevision(Revision revision) throws IOException {
		stream.writeInt(revision.Id);
		stream.writeUTFAsArray(SQLEscape.escape(revision.Text));
	}

	@Override
	public void writeSiteinfo(Siteinfo info) throws IOException {
	}

	@Override
	public void writeStartPage(Page page) throws IOException {
	}

	@Override
	public void writeStartWiki() throws IOException {
	}
}
