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
package org.dkpro.jwpl.revisionmachine.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.index.indices.ArticleIndex;
import org.dkpro.jwpl.revisionmachine.index.indices.ArticleIndexData;
import org.dkpro.jwpl.revisionmachine.index.indices.ChronoIndex;
import org.dkpro.jwpl.revisionmachine.index.indices.RevisionIndex;
import org.dkpro.jwpl.revisionmachine.index.writer.DataFileWriter;
import org.dkpro.jwpl.revisionmachine.index.writer.DatabaseWriter;
import org.dkpro.jwpl.revisionmachine.index.writer.IndexWriterInterface;
import org.dkpro.jwpl.revisionmachine.index.writer.SQLFileWriter;

/**
 * Forwards the necessary information to the AbstractIndex classes and controls
 * the writing to the output if one of the index has reached the maximum size.
 */
public class Indexer {

  /**
   * Currently used article
   */
  private int currentArticleID;

  /**
   * First appearance of the current article
   */
  private long startTime;

  /**
   * Last appearance of the current article
   */
  private long endTime;

  /**
   * Currently used full revision
   */
  private int currentFullRevisionID;

  /**
   * Previous revision
   */
  private Revision lastRev;

  /**
   * Reference to the revision index
   */
  private RevisionIndex revisionIndex = null;

  /**
   * Reference to the currently used article index information
   */
  private ArticleIndexData info;

  /**
   * List of article index information related to the currently used article
   */
  private final List<ArticleIndexData> infoList;

  /**
   * Reference to the article index
   */
  private ArticleIndex articleIndex = null;

  /**
   * Reference to the chronological order index
   */
  private ChronoIndex chronoIndex = null;

  /**
   * Reference to the output writer
   */
  private IndexWriterInterface indexWriter;

  /**
   * Reference to the database connection
   */
  private final Connection connection = null;

  /**
   * (Constructor) Creates a Index object.
   *
   * @param config Reference to the configuration
   * @throws ClassNotFoundException if the jdbc classes could not be located
   * @throws SQLException           if an error occurred while accessing the database
   * @throws IOException            if an error occurred while writing the output
   */
  public Indexer(final RevisionAPIConfiguration config)
          throws ClassNotFoundException, SQLException, IOException {

    this.currentArticleID = -1;

    switch (config.getOutputType()) {
      case DATABASE:
      case SQL:
        //Indices with SQL statements
        this.revisionIndex = new RevisionIndex(config.getMaxAllowedPacket());
        this.articleIndex = new ArticleIndex(config.getMaxAllowedPacket());
        this.chronoIndex = new ChronoIndex(config.getMaxAllowedPacket());
        break;
      case DATAFILE:
        //Indices without SQL statements
        this.revisionIndex = new RevisionIndex();
        this.articleIndex = new ArticleIndex();
        this.chronoIndex = new ChronoIndex();
        break;
    }

    this.infoList = new ArrayList<>();

    switch (config.getOutputType()) {
      case DATABASE:
        this.indexWriter = new DatabaseWriter(config);
        break;
      case SQL:
        this.indexWriter = new SQLFileWriter(config);
        break;
      case DATAFILE:
        this.indexWriter = new DataFileWriter(config);
        break;
    }
  }

  /**
   * Checks whether the AbstractIndex classes have output available and
   * forward them to the output writer.
   *
   * @throws IOException  if an error occurred while writing the output
   * @throws SQLException if an error occurred while accessing the database
   */
  private void send()
          throws IOException, SQLException {

    this.indexWriter.write(articleIndex);
    this.indexWriter.write(revisionIndex);
    this.indexWriter.write(chronoIndex);
  }

  /**
   * Processes the given revision.
   *
   * @param rev Reference to a revision
   * @throws WikiApiException if an error occurs
   */
  public void index(final Revision rev) throws WikiApiException {

    int articleID = rev.getArticleID();
    int fullRevisionID = rev.getFullRevisionID();
    int revisionCounter = rev.getRevisionCounter();

    if (articleID != currentArticleID) {

      if (lastRev != null) {
        info.setEndRevisionCount(lastRev.getRevisionCounter());
        this.infoList.add(info);

        try {
          this.articleIndex.add(currentArticleID, startTime, endTime,
                  infoList);
          send();
        } catch (SQLException | IOException sql) {
          sql.printStackTrace();
          throw new WikiApiException(sql);
        }
      }

      if (revisionCounter != 1) {
        System.err.println("WARNING : ArticleID (" + articleID
                + ") RevisionCounter 1 expected - " + revisionCounter
                + " read");
      }

      startTime = Long.MAX_VALUE;
      endTime = Long.MIN_VALUE;

      currentArticleID = articleID;
      currentFullRevisionID = fullRevisionID;

      info = new ArticleIndexData();

      info.setFullRevisionPrimaryKey(rev.getPrimaryKey());
      info.setStartRevisionCount(rev.getRevisionCounter());

    } else if (fullRevisionID != currentFullRevisionID) {

      if (lastRev.getRevisionCounter() + 1 != revisionCounter) {
        System.err.println("WARNING : ArticleID (" + articleID + ")"
                + " RevisionCounter "
                + (lastRev.getRevisionCounter() + 1) + " expected - "
                + revisionCounter + " read");
      }

      info.setEndRevisionCount(lastRev.getRevisionCounter());
      this.infoList.add(info);

      currentFullRevisionID = fullRevisionID;
      info = new ArticleIndexData();

      info.setFullRevisionPrimaryKey(rev.getPrimaryKey());
      info.setStartRevisionCount(rev.getRevisionCounter());

    } else if (lastRev.getRevisionCounter() + 1 != revisionCounter) {

      System.err.println("WARNING : ArticleID (" + articleID + ")"
              + " RevisionCounter " + (lastRev.getRevisionCounter() + 1)
              + " expected - " + revisionCounter + " read");
    }

    this.startTime = Math.min(rev.getTimeStamp().getTime(), startTime);
    this.endTime = Math.max(rev.getTimeStamp().getTime(), endTime);

    revisionIndex.add(rev.getRevisionID(), rev.getPrimaryKey(),
            info.getFullRevisionPrimaryKey());
    chronoIndex.add(articleID, rev.getRevisionCounter(), rev.getTimeStamp()
            .getTime());
    lastRev = rev;
  }

  /**
   * Finalizes the indices and sends the rest of the data to the output.
   * Afterwards the database connection will be closed.
   *
   * @throws WikiApiException if an error occurs
   */
  public void close() throws WikiApiException {

    try {
      this.revisionIndex.finalizeIndex();
      this.chronoIndex.finalizeIndex();

      info.setEndRevisionCount(lastRev.getRevisionCounter());
      this.infoList.add(info);

      this.articleIndex.add(currentArticleID, startTime, endTime,
              infoList);
      this.articleIndex.finalizeIndex();

      send();

      this.indexWriter.finish();

      if (connection != null) {
        this.connection.close();
      }

    } catch (SQLException | IOException sql) {
      sql.printStackTrace();
      throw new WikiApiException(sql);
    }
  }
}
