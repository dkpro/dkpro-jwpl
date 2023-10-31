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
package org.dkpro.jwpl.revisionmachine.index.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.index.indices.AbstractIndex;
import org.dkpro.jwpl.revisionmachine.index.indices.ArticleIndex;
import org.dkpro.jwpl.revisionmachine.index.indices.ChronoIndex;
import org.dkpro.jwpl.revisionmachine.index.indices.RevisionIndex;

/**
 * This class writes the output of the index generator to an SQL file.
 */
public class DataFileWriter implements IndexWriterInterface {

  /**
   * Reference to the Writer object
   */
  private final Writer chronoIdxWriter;
  private final Writer revisionIdxWriter;
  private final Writer articleIdxWriter;

  /**
   * Creates a new SQLFileWriter.
   *
   * @param config Reference to the configuration parameters
   * @throws IOException if an error occurred while writing the file
   */
  public DataFileWriter(final RevisionAPIConfiguration config) throws IOException {

    File path = new File(config.getOutputPath());
    chronoIdxWriter = new BufferedWriter(new FileWriter(new File(path, "chronoIndex.csv")));
    revisionIdxWriter = new BufferedWriter(new FileWriter(new File(path, "revisionIndex.csv")));
    articleIdxWriter = new BufferedWriter(new FileWriter(new File(path, "articleIndex.csv")));
  }

  /**
   * Writes the buffered finalized queries to the output.
   *
   * @param index Reference to an index
   * @throws IOException if an error occurred while writing the output
   */
  @Override
  public void write(final AbstractIndex index) throws IOException {

    StringBuilder cmd;

    while (index.size() > 0) {
      System.out.println("Transmit Index [" + index + "]");
      cmd = index.remove();
      if (index instanceof ArticleIndex) {
        articleIdxWriter.write(cmd.toString());
      } else if (index instanceof ChronoIndex) {
        chronoIdxWriter.write(cmd.toString());
      } else if (index instanceof RevisionIndex) {
        revisionIdxWriter.write(cmd.toString());
      }

    }

    if (index instanceof ArticleIndex) {
      articleIdxWriter.flush();
    } else if (index instanceof ChronoIndex) {
      chronoIdxWriter.flush();
    } else if (index instanceof RevisionIndex) {
      revisionIdxWriter.flush();
    }
  }

  /**
   * Closes the file or the database connection.
   *
   * @throws IOException if an error occurred while closing the file
   */
  @Override
  public void close() throws IOException {
    articleIdxWriter.close();
    chronoIdxWriter.close();
    revisionIdxWriter.close();
  }

  /**
   * Wraps up the index generation process and writes all remaining statements
   * e.g. concerning UNCOMPRESSED-Indexes on the created tables.
   *
   * @throws IOException if an error occurred while writing to the file
   */
  @Override
  public void finish() throws IOException {
    articleIdxWriter.flush();
    chronoIdxWriter.flush();
    revisionIdxWriter.flush();
  }
}
