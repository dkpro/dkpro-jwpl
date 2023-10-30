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
package org.dkpro.jwpl.util.revisions;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.Wikipedia;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.api.exception.WikiPageNotFoundException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;

/**
 * Provides several revision-related utilities that should not be part of the RevisionMachine
 * package because of dependencies to the JWPL API. RevisionMachine should stay independent
 * from the RevisionMachine
 */
public class RevisionUtils {
  private RevisionApi revApi;
  private Wikipedia wiki;

  public RevisionUtils(DatabaseConfiguration conf) throws WikiApiException {
    wiki = new Wikipedia(conf);
    revApi = new RevisionApi(conf);
  }

  public RevisionUtils(Wikipedia wiki, RevisionApi revApi) throws WikiApiException {
    this.revApi = revApi;
    this.wiki = wiki;
  }

  /**
   * For a given article revision, the method returns the revision of the article discussion
   * page which was current at the time the revision was created.
   *
   * @param revisionId revision of the article for which the talk page revision should be retrieved
   * @return the revision of the talk page that was current at the creation time of the given article revision
   * @throws WikiApiException          if any error occurred accessing the Wiki db
   * @throws WikiPageNotFoundException if no discussion page was available at the time of the given article revision
   */
  public Revision getDiscussionRevisionForArticleRevision(int revisionId) throws WikiApiException, WikiPageNotFoundException {
    //get article revision
    Revision rev = revApi.getRevision(revisionId);
    Timestamp revTime = rev.getTimeStamp();

    //get corresponding discussion page
    Page discussion = wiki.getDiscussionPage(rev.getArticleID());

    /*
     * find correct revision of discussion page
     */
    List<Timestamp> discussionTs = revApi.getRevisionTimestamps(discussion.getPageId());

    // sort in reverse order - newest first
    discussionTs.sort(Comparator.reverseOrder());

    //find first timestamp equal to or before the article revision timestamp
    for (Timestamp curDiscTime : discussionTs) {
      if (curDiscTime == revTime || curDiscTime.before(revTime)) {
        return revApi.getRevision(discussion.getPageId(), curDiscTime);
      }
    }

    throw new WikiPageNotFoundException("Not discussion page was available at the time of the given article revision");
  }


  /**
   * For a given article revision, the method returns the revisions of the archived article discussion
   * pages which were available at the time of the article revision
   *
   * @param revisionId revision of the article for which the talk page archive revisions should be retrieved
   * @return the revisions of the talk page archives that were available at the time of the article revision
   */
  public List<Revision> getDiscussionArchiveRevisionsForArticleRevision(int revisionId) throws WikiApiException, WikiPageNotFoundException {
    List<Revision> result = new LinkedList<>();

    //get article revision
    Revision rev = revApi.getRevision(revisionId);
    Timestamp revTime = rev.getTimeStamp();

    //get corresponding discussion archives
    Iterable<Page> discArchives = wiki.getDiscussionArchives(rev.getArticleID());

    /*
     * for each discussion archive, find correct revision of discussion page
     */
    for (Page discArchive : discArchives) {
      //get revision timestamps for the current discussion archive
      List<Timestamp> discussionTs = revApi.getRevisionTimestamps(discArchive.getPageId());

      // sort in reverse order - newest first
      discussionTs.sort(Comparator.reverseOrder());

      //find first timestamp equal to or before the article revision timestamp
      for (Timestamp curDiscTime : discussionTs) {
        if (curDiscTime == revTime || curDiscTime.before(revTime)) {
          result.add(revApi.getRevision(discArchive.getPageId(), curDiscTime));
          break;
        }
      }
    }

    return result;
  }

}
