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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * This class defines common utilities for the classes CategorylinksParser <br>
 * and PagelinksParser.
 *
 * @version 0.2 <br>
 * <code>SQLFileParser</code> don't create a BufferedReader by himself
 * but entrust it to <code>BufferedReaderFactory</code>. Thereby
 * BufferedReaders are created according to archive type and try to
 * uncompress the file on the fly. (Ivan Galkin 15.01.2009)
 */
abstract class SQLFileParser {

  private static final String ENCODING = "UTF-8";
  protected InputStream stream;
  protected StreamTokenizer st;
  protected boolean EOF_reached;

  /**
   * Init the SQLFileParser with an input stream
   *
   * @param inputStream
   * @throws IOException Thrown if IO errors occurred.
   */
  protected void init(InputStream inputStream) throws IOException {
    stream = inputStream;
    st = new StreamTokenizer(new BufferedReader(new InputStreamReader(
            stream, ENCODING)));

    EOF_reached = false;
    skipStatements();

  }

  /**
   * Skip the sql statements for table creation and the prefix <br>
   * INSERT INTO TABLE .... VALUES for values insertion.<br>
   * Read tokens until the word 'VALUES' is reached or the EOF.
   *
   * @throws IOException Thrown if IO errors occurred.
   */
  protected void skipStatements() throws IOException {
    while (true) {
      st.nextToken();
      if (null != st.sval && st.sval.equalsIgnoreCase("VALUES")) {
        // the next token is the begin of a value
        break;
      }
      if (st.ttype == StreamTokenizer.TT_EOF) {
        // the end of the file is reached
        EOF_reached = true;
        break;
      }
    }
  }

  public void close() throws IOException {
    stream.close();
  }

  /**
   * This method must be implemented by the PagelinksParser and the
   * CategorylinksParser<br> classes.
   *
   * @return Returns true if a new value is now available und false otherwise.
   * @throws IOException Thrown if IO errors occurred.
   */
  abstract boolean next() throws IOException;
}
