package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

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
 * $Id: XmlDumpReader.java 59325 2009-11-22 01:21:03Z rainman $
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Contributor;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.DumpWriter;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.NamespaceSet;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Page;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Revision;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Siteinfo;
import de.tudarmstadt.ukp.wikipedia.mwdumper.importer.Title;

/**
 * Parser of WikiMedia XML dumps. Modification of
 * {@link org.mediawiki.importer.XmlDumpReader} with some enhanced error
 * resistance and adaptation mechanisms. Please see copyright and comments of
 * original code. Modification was done by Ivan Galkin <br>
 * <br>
 * See SVN at
 * <a>http://svn.wikimedia.org/svnroot/mediawiki/trunk/mwdumper/src/org
 * /mediawiki/importer/XmlDumpReader.java</a>
 *
 * @see org.mediawiki.importer.XmlDumpReader
 *
 */
public abstract class AbstractXmlDumpReader extends DefaultHandler {
	protected static final String SITENAME = "sitename";
	protected static final String GENERATOR = "generator";
	protected static final String CASE = "case";
	protected static final String BASE = "base";
	protected static final String NAMESPACE = "namespace";
	protected static final String NAMESPACES = "namespaces";
	protected static final String SITEINFO = "siteinfo";
	protected static final String MEDIAWIKI = "mediawiki";
	protected static final String USERNAME = "username";
	protected static final String TITLE = "title";
	protected static final String TIMESTAMP = "timestamp";
	protected static final String TEXT = "text";
	protected static final String RESTRICTIONS = "restrictions";
	protected static final String PAGE = "page";
	protected static final String MINOR = "minor";
	protected static final String IP = "ip";
	protected static final String ID = "id";
	protected static final String COMMENT = "comment";
	protected static final String THREAD_TYPE = "ThreadType";
	protected static final String THREAD_EDIT_STATUS = "ThreadEditStatus";
	protected static final String THREAD_AUTHOR = "ThreadAuthor";
	protected static final String THREAD_SUMMARY_PAGE = "ThreadSummaryPage";
	protected static final String THREAD_ID = "ThreadID";
	protected static final String THREAD_PAGE = "ThreadPage";
	protected static final String THREAD_ANCESTOR = "ThreadAncestor";
	protected static final String THREAD_PARENT = "ThreadParent";
	protected static final String THREAD_SUBJECT = "ThreadSubject";
	protected static final String CONTRIBUTOR = "contributor";
	protected static final String REVISION = "revision";

	InputStream input;
	DumpWriter writer;

	private char[] buffer;
	private int len;
	private boolean hasContent = false;
	private boolean deleted = false;

	Siteinfo siteinfo;
	Page page;
	boolean pageSent;
	Contributor contrib;
	Revision rev;
	int nskey;

	boolean abortFlag;
	boolean errorState = false;

	protected Map<String, String> startElements = new HashMap<String, String>(
			64);
	protected Map<String, String> endElements = new HashMap<String, String>(64);
	protected Map<String, String> forbiddenIdStartElements = new HashMap<String, String>(
			64);

	protected Map<String, String> forbiddenIdEndElements = new HashMap<String, String>(
			64);

	/**
	 * Fill {@link #forbiddenIdStartElements}
	 *
	 * @see #notAllowedStart(String)
	 */
	protected void setupForbiddenStartElements() {
		forbiddenIdStartElements.put(REVISION, REVISION);
		forbiddenIdStartElements.put(CONTRIBUTOR, CONTRIBUTOR);
	}

	/**
	 * Fill {@link #forbiddenIdEndElements}
	 *
	 * @see #notAllowedEnd(String)
	 */
	protected void setupForbiddenEndElements() {
		forbiddenIdEndElements.put(THREAD_SUBJECT, THREAD_SUBJECT);
		forbiddenIdEndElements.put(THREAD_PARENT, THREAD_PARENT);
		forbiddenIdEndElements.put(THREAD_ANCESTOR, THREAD_ANCESTOR);
		forbiddenIdEndElements.put(THREAD_PAGE, THREAD_PAGE);
		forbiddenIdEndElements.put(THREAD_ID, THREAD_ID);
		forbiddenIdEndElements.put(THREAD_SUMMARY_PAGE, THREAD_SUMMARY_PAGE);
		forbiddenIdEndElements.put(THREAD_AUTHOR, THREAD_AUTHOR);
		forbiddenIdEndElements.put(THREAD_EDIT_STATUS, THREAD_EDIT_STATUS);
		forbiddenIdEndElements.put(THREAD_TYPE, THREAD_TYPE);
		forbiddenIdEndElements.put(COMMENT, COMMENT);
		forbiddenIdEndElements.put(CONTRIBUTOR, CONTRIBUTOR);
		forbiddenIdEndElements.put(ID, ID);
		forbiddenIdEndElements.put(IP, IP);
		forbiddenIdEndElements.put(MINOR, MINOR);
		forbiddenIdEndElements.put(PAGE, PAGE);
		forbiddenIdEndElements.put(RESTRICTIONS, RESTRICTIONS);
		forbiddenIdEndElements.put(REVISION, REVISION);
		forbiddenIdEndElements.put(TEXT, TEXT);
		forbiddenIdEndElements.put(TIMESTAMP, TIMESTAMP);
		forbiddenIdEndElements.put(TITLE, TITLE);
		forbiddenIdEndElements.put(USERNAME, USERNAME);
	}

	/**
	 * Setup start tags, which will be processed. Fill {@link #startElements}
	 */
	protected abstract void setupStartElements();

	/**
	 * Setup end tags, which will be processed. Fill {@link #endElements}
	 */
	protected abstract void setupEndElements();

	/**
	 * Initialize a processor for a MediaWiki XML dump stream. Events are sent
	 * to a single DumpWriter output sink, but you can chain multiple output
	 * processors with a MultiWriter.
	 *
	 * @param inputStream
	 *            Stream to read XML from.
	 * @param writer
	 *            Output sink to send processed events to.
	 */
	public AbstractXmlDumpReader(InputStream inputStream, DumpWriter writer) {
		input = inputStream;
		this.writer = writer;
		buffer = new char[4096];
		len = 0;
		hasContent = false;

		setupStartElements();
		setupEndElements();
		setupForbiddenStartElements();
		setupForbiddenEndElements();
	}

	/**
	 * Reads through the entire XML dump on the input stream, sending events to
	 * the DumpWriter as it goes. May throw exceptions on invalid input or due
	 * to problems with the output.
	 *
	 * @throws IOException
	 */
	public void readDump() throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			parser.parse(input, this);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		writer.close();
	}

	/**
	 * Request that the dump processing be aborted. At the next element, an
	 * exception will be thrown to stop the XML parser.
	 *
	 * @fixme Is setting a bool thread-safe? It should be atomic...
	 */
	public void abort() {
		abortFlag = true;
	}

	// --------------------------
	// SAX handler interface methods:

	/**
	 * Add by Ivan Galkin.<br>
	 * If error with wrong id tag occurs, the errorState flag will be set. In
	 * this case some start tags have to be ignored.
	 *
	 * @param startTag
	 * @return true if startTag is not allowed and will be ignored
	 * @see #setupForbiddenStartElements()
	 */
	private boolean notAllowedStart(String startTag) {
		errorState = errorState && startElements.containsKey(startTag)
				&& forbiddenIdStartElements.containsKey(startTag);
		return errorState;
	}

	/**
	 * Add by Ivan Galkin.<br>
	 * If error with wrong id tag occurs, the errorState flag will be set. In
	 * this case some end tags have to be ignored.
	 *
	 * @param startTag
	 * @return true if startTag is not allowed and will be ignored
	 * @see #setupForbiddenEndElements()
	 */
	private boolean notAllowedEnd(String endTag) {
		errorState = errorState && endElements.containsKey(endTag)
				&& forbiddenIdEndElements.containsKey(endTag);

		return errorState;
	}

	@Override
    public void startElement(String uri, String localname, String qName,
			Attributes attributes) throws SAXException {
		// Clear the buffer for character data; we'll initialize it
		// if and when character data arrives -- at that point we
		// have a length.
		len = 0;
		hasContent = false;

		if (abortFlag) {
			throw new SAXException("XmlDumpReader set abort flag.");
		}

		// check for deleted="deleted", and set deleted flag for the current
		// element.
		String d = attributes.getValue("deleted");
		deleted = (d != null && d.equals("deleted"));

		try {
			qName = startElements.get(qName);
			if (qName == null || notAllowedStart(qName)) {
				return;
			}

			// frequent tags:
			if (qName == REVISION) {
				openRevision();
			}
			else if (qName == CONTRIBUTOR) {
				openContributor();
			}
			else if (qName == PAGE) {
				openPage();
			}
			else if (qName == MEDIAWIKI) {
				openMediaWiki();
			}
			else if (qName == SITEINFO) {
				openSiteinfo();
			}
			else if (qName == NAMESPACES) {
				openNamespaces();
			}
			else if (qName == NAMESPACE) {
				openNamespace(attributes);
			}
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
    public void characters(char[] ch, int start, int length) {
		if (buffer.length < len + length) {
			int maxlen = buffer.length * 2;
			if (maxlen < len + length) {
				maxlen = len + length;
			}
			char[] tmp = new char[maxlen];
			System.arraycopy(buffer, 0, tmp, 0, len);
			buffer = tmp;
		}
		System.arraycopy(ch, start, buffer, len, length);
		len += length;
		hasContent = true;
	}

	@Override
    public void endElement(String uri, String localname, String qName)
			throws SAXException {
		try {
			qName = endElements.get(qName);
			if (qName == null || notAllowedEnd(qName)) {
				return;
			}
			// frequent tags:
			if (qName == ID) {
				readId();
			}
			else if (qName == REVISION) {
				closeRevision();
			}
			else if (qName == TIMESTAMP) {
				readTimestamp();
			}
			else if (qName == TEXT) {
				readText();
			}
			else if (qName == CONTRIBUTOR) {
				closeContributor();
			}
			else if (qName == USERNAME) {
				readUsername();
			}
			else if (qName == IP) {
				readIp();
			}
			else if (qName == COMMENT) {
				readComment();
			}
			else if (qName == MINOR) {
				readMinor();
			}
			else if (qName == PAGE) {
				closePage();
			}
			else if (qName == TITLE) {
				readTitle();
			}
			else if (qName == RESTRICTIONS) {
				readRestrictions();
			}
			else if (qName.startsWith("Thread")) {
				threadAttribute(qName);
			}
			else if (qName == MEDIAWIKI) {
				closeMediaWiki();
			}
			else if (qName == SITEINFO) {
				closeSiteinfo();
			}
			else if (qName == SITENAME) {
				readSitename();
			}
			else if (qName == BASE) {
				readBase();
			}
			else if (qName == GENERATOR) {
				readGenerator();
			}
			else if (qName == CASE) {
				readCase();
			}
			else if (qName == NAMESPACES) {
				closeNamespaces();
			}
			else if (qName == NAMESPACE)
			 {
				closeNamespace();
			// else throw(SAXException)new
			// SAXException("Unrecognised "+qName+"(substring "+qName.length()+qName.substring(0,6)+")");
			}
		} catch (IOException e) {
			throw (SAXException) new SAXException(e.getMessage()).initCause(e);
		}
	}

	// ----------

	@SuppressWarnings("unchecked")
	void threadAttribute(String attrib) throws IOException {
		if (attrib.equals("ThreadPage")) {
			page.DiscussionThreadingInfo.put(attrib, new Title(
					bufferContents(), siteinfo.Namespaces));
		}
		else {
			page.DiscussionThreadingInfo.put(attrib, bufferContents());
		}
	}

	void openMediaWiki() throws IOException {
		siteinfo = null;
		writer.writeStartWiki();
	}

	void closeMediaWiki() throws IOException {
		writer.writeEndWiki();
		siteinfo = null;
	}

	// ------------------

	void openSiteinfo() {
		siteinfo = new Siteinfo();
	}

	void closeSiteinfo() throws IOException {
		writer.writeSiteinfo(siteinfo);
	}

	private String bufferContentsOrNull() {
		if (!hasContent) {
			return null;
		}
		else {
			return bufferContents();
		}
	}

	private String bufferContents() {
		// escape backslashes
	    if(len == 0) {
			return "";
		}
		else{
			String result = new String(buffer, 0 , len);
			result = result.replace("\\","\\\\");
			return result;
		}
	}

	void readSitename() {
		siteinfo.Sitename = bufferContents();
	}

	void readBase() {
		siteinfo.Base = bufferContents();
	}

	void readGenerator() {
		siteinfo.Generator = bufferContents();
	}

	void readCase() {
		siteinfo.Case = bufferContents();
	}

	void openNamespaces() {
		siteinfo.Namespaces = new NamespaceSet();
	}

	void openNamespace(Attributes attribs) {
		nskey = Integer.parseInt(attribs.getValue("key"));
	}

	void closeNamespace() {
		siteinfo.Namespaces.add(nskey, bufferContents());
	}

	void closeNamespaces() {
		// NOP
	}

	// -----------

	void openPage() {
		page = new Page();
		pageSent = false;
	}

	void closePage() throws IOException {
		if (pageSent) {
			writer.writeEndPage();
		}
		page = null;
	}

	void readTitle() {
		page.Title = new Title(bufferContents(), siteinfo.Namespaces);
	}

	void readId() {
		int id = Integer.parseInt(bufferContents());
		if (contrib != null) {
			contrib.Id = id;
		}
		else if (rev != null) {
			rev.Id = id;
		}
		else if (page != null) {
			page.Id = id;
		}
		else {
			Logger
					.getLogger(AbstractXmlDumpReader.class.getName())
					.debug(
							"Unexpected <id> outside a <page>, <revision>, or <contributor>");
			errorState = true;
			contrib = null;
			rev = null;
			page = null;
		}
	}

	void readRestrictions() {
		page.Restrictions = bufferContents();
	}

	// ------

	void openRevision() throws IOException {
		if (!pageSent) {
			writer.writeStartPage(page);
			pageSent = true;
		}

		rev = new Revision();
	}

	void closeRevision() throws IOException {
		writer.writeRevision(rev);
		rev = null;
	}

	void readTimestamp() {
		rev.Timestamp = parseUTCTimestamp(bufferContents());
	}

	void readComment() {
		rev.Comment = bufferContentsOrNull();
		if (rev.Comment == null && !deleted)
		 {
			rev.Comment = ""; // NOTE: null means deleted/supressed
		}
	}

	void readMinor() {
		rev.Minor = true;
	}

	void readText() {
		rev.Text = bufferContentsOrNull();
		if (rev.Text == null && !deleted)
		 {
			rev.Text = ""; // NOTE: null means deleted/supressed
		}
	}

	// -----------
	void openContributor() {
		// XXX: record deleted flag?! as it is, any empty <contributor> tag
		// counts as "deleted"
		contrib = new Contributor();
	}

	void closeContributor() {
		// NOTE: if the contributor was supressed, nither username nor id have
		// been set in the Contributor object
		rev.Contributor = contrib;
		contrib = null;
	}

	void readUsername() {
		contrib.Username = bufferContentsOrNull();
	}

	void readIp() {
		contrib.Username = bufferContents();
		contrib.isIP = true;
	}

	private static final TimeZone utc = TimeZone.getTimeZone("UTC");

	private static Calendar parseUTCTimestamp(String text) {
		// 2003-10-26T04:50:47Z
		// We're doing this manually for now, though DateFormatter might work...
		String trimmed = text.trim();
		GregorianCalendar ts = new GregorianCalendar(utc);
		ts.set(Integer.parseInt(trimmed.substring(0, 0 + 4)), // year
				Integer.parseInt(trimmed.substring(5, 5 + 2)) - 1, // month is
				// 0-based!
				Integer.parseInt(trimmed.substring(8, 8 + 2)), // day
				Integer.parseInt(trimmed.substring(11, 11 + 2)), // hour
				Integer.parseInt(trimmed.substring(14, 14 + 2)), // minute
				Integer.parseInt(trimmed.substring(17, 17 + 2))); // second
		return ts;
	}
}
