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
package de.tudarmstadt.ukp.wikipedia.timemachine.dump.xml;

import java.io.IOException;
import java.io.OutputStream;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Page;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Revision;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Siteinfo;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.sql.SQLEscape;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.Redirects;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.UTFDataOutputStream;

public class PageWriter implements DumpWriter {

	private Page currentPage;
	private Revision lastRevision;
	private final UTFDataOutputStream stream;

	public PageWriter(OutputStream output) throws IOException {
		this.stream = new UTFDataOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		stream.close();
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
		stream.flush();
	}

	@Override
	public void writeRevision(Revision revision) throws IOException {

		lastRevision = revision;

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
		stream.writeInt(page.Id);
		stream.writeInt(page.Title.Namespace);
		String wellformedTitle = SQLEscape.titleFormat(page.Title.Text);
		stream.writeUTFAsArray(SQLEscape.escape(wellformedTitle));
		// stream.writeBoolean(revision.isRedirect());
		stream.writeBoolean(Redirects.isRedirect(revision.Text));
	}
}
