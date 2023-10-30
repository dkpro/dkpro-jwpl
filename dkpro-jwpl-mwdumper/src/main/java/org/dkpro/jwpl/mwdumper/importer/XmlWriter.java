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
package org.dkpro.jwpl.mwdumper.importer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Quickie little class for sending properly encoded, prettily
 * indented XML output to a stream. There is no namespace support,
 * so prefixes and xmlns attributes must be managed manually.
 */
public class XmlWriter {
  private final String encoding;
  private final List<String> stack;
  private final BufferedWriter writer;

  public XmlWriter(OutputStream stream) {
    encoding = "utf-8";
    stack = new ArrayList<>();
    writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
  }

  /**
   * @throws IOException Thrown if IO errors occurred.
   */
  public void close() throws IOException {
    writer.flush();
    writer.close();
  }

  /**
   * Write the &lt;?xml?&gt; header.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  public void openXml() throws IOException {
    writeRaw("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n");
  }

  /**
   * In theory, we might close out open elements or such.
   */
  public void closeXml() {
  }


  /**
   * Write an empty element, such as &lt;el/&gt;, on a standalone line.
   * Takes an optional dictionary of attributes.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  public void emptyElement(String element) throws IOException {
    emptyElement(element, null);
  }

  public void emptyElement(String element, String[][] attributes) throws IOException {
    startElement(element, attributes, "/>\n");
    deIndent();
  }

  /**
   * Write an element open tag, such as &lt;el/&gt;, on a standalone line.
   * Takes an optional dictionary of attributes.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  public void openElement(String element) throws IOException {
    openElement(element, null);
  }

  public void openElement(String element, String[][] attributes) throws IOException {
    startElement(element, attributes, ">\n");
  }

  /**
   * Write an element close tag, such as &lt;el/&gt;, on a standalone line.
   * If indent=False is passed, indentation will not be added.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  public void closeElement() throws IOException {
    closeElement(true);
  }

  public void closeElement(boolean indent) throws IOException {
    String[] bits = deIndent();
    String element = bits[0];
    String space = bits[1];
    if (indent)
      writeRaw(space + "</" + element + ">\n");
    else
      writeRaw("</" + element + ">\n");
  }

  /**
   * Write an element with a text node included, such as &lt;el/&gt;foo&lt;el/&gt;,
   * on a standalone line. If the text is empty, an empty element will
   * be output as &lt;el/&gt;. Takes an optional list of tuples with attribute
   * names and values.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  public void textElement(String element, String text) throws IOException {
    textElement(element, text, null);
  }

  public void textElement(String element, String text, String[][] attributes) throws IOException {
    if (text == null || text.length() == 0) {
      emptyElement(element, attributes);
    } else {
      startElement(element, attributes, ">");
      writeEscaped(text);
      closeElement(false);
    }
  }

  void startElement(String element, String[][] attributes, String terminator) throws IOException {
    writeRaw(indent(element));
    writeRaw('<');
    writeRaw(element);
    if (attributes != null) {
      for (int i = 0; i < attributes.length; i++) {
        writeRaw(' ');
        writeRaw(attributes[i][0]);
        writeRaw("=\"");
        writeEscaped(attributes[i][1]);
        writeRaw('"');
      }
    }
    writeRaw(terminator);
  }

  /**
   * Send an encoded Unicode string to the output stream.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  void writeRaw(String data) throws IOException {
    writer.write(data);
  }

  void writeRaw(char c) throws IOException {
    writer.write(c);
  }

  void writeEscaped(String data) throws IOException {
    int end = data.length();
    for (int i = 0; i < end; i++) {
      char c = data.charAt(i);
      switch (c) {
        case '&':
          writer.write("&amp;");
          break;
        case '<':
          writer.write("&lt;");
          break;
        case '>':
          writer.write("&gt;");
          break;
        case '"':
          writer.write("&quot;");
          break;
        default:
          writer.write(c);
      }
    }
  }

  private String indent(String element) {
    int level = stack.size();
    stack.add(element);
    return spaces(level);
  }

  private String[] deIndent() {
    String element = stack.remove(stack.size() - 1);
    String space = spaces(stack.size());
    return new String[]{element, space};
  }

  private String spaces(int level) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(" ".repeat(Math.max(0, level * 2)));
    return buffer.toString();
  }
}
