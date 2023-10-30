/*******************************************************************************
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
 * $Id$
 *******************************************************************************/
package org.dkpro.jwpl.datamachine.dump.xml;

import java.io.InputStream;

import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.wikimachine.dump.xml.AbstractXmlDumpReader;

/**
 * This class is a specified variant of XmlDumpReader. Please see its source for more
 * information about a functionality and a license.<br>
 */
public class SimpleXmlDumpReader extends AbstractXmlDumpReader {

  public SimpleXmlDumpReader(InputStream inputStream, DumpWriter writer) {
    super(inputStream, writer);

  }

  @Override
  protected void setupStartElements() {
    startElements.put(REVISION, REVISION);
    startElements.put(CONTRIBUTOR, CONTRIBUTOR);
    startElements.put(PAGE, PAGE);
    startElements.put(SITEINFO, SITEINFO);
    startElements.put(NAMESPACES, NAMESPACES);
    startElements.put(NAMESPACE, NAMESPACE);
  }

  @Override
  protected void setupEndElements() {
    endElements.put(REVISION, REVISION);
    endElements.put(TEXT, TEXT);
    endElements.put(CONTRIBUTOR, CONTRIBUTOR);
    endElements.put(ID, ID);
    endElements.put(PAGE, PAGE);
    endElements.put(TITLE, TITLE);
    endElements.put(SITEINFO, SITEINFO);
    endElements.put(NAMESPACES, NAMESPACES);
    endElements.put(NAMESPACE, NAMESPACE);

  }
}
