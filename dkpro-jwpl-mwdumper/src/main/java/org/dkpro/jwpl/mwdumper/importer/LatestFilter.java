/*
 * MediaWiki import/export processing tools
 * Copyright 2005 by Brion Vibber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $Id: LatestFilter.java 11127 2005-09-27 05:46:20Z vibber $
 */

package org.dkpro.jwpl.mwdumper.importer;

import java.io.IOException;

public class LatestFilter implements DumpWriter {
	final DumpWriter sink;
	Revision lastRevision;
	
	public LatestFilter(DumpWriter sink) {
		this.sink = sink;
	}
	
	public void close() throws IOException {
		sink.close();
	}
	
	public void writeStartWiki() throws IOException {
		sink.writeStartWiki();
	}
	
	public void writeEndWiki() throws IOException {
		sink.writeEndWiki();
	}
	
	public void writeSiteinfo(Siteinfo info) throws IOException {
		sink.writeSiteinfo(info);
	}
	
	public void writeStartPage(Page page) throws IOException {
		sink.writeStartPage(page);
	}
	
	public void writeEndPage() throws IOException {
		if (lastRevision != null) {
			sink.writeRevision(lastRevision);
			lastRevision = null;
		}
		sink.writeEndPage();
	}
	
	public void writeRevision(Revision revision) {
		lastRevision = revision;
	}
}
