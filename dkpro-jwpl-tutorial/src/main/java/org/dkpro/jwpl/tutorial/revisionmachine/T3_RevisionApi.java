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
package org.dkpro.jwpl.tutorial.revisionmachine;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionApi;

/**
 * RevisionMachine Tutorial 3
 * <p>
 * Accesses the revisions of a single article with the {@link RevisionApi}. Articles are
 * identified by their page id, revisions either by their (Wikipedia) revision id, by their
 * position in the history of an article (the revision counter, starting at 1) or by a timestamp.
 */
public class T3_RevisionApi
{

    public static void main(String[] args) throws WikiApiException, SQLException
    {
        // configure the connection to the revision database
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("SERVER_URL");
        config.setDatabase("DATABASE");
        config.setUser("USER");
        config.setPassword("PASSWORD");

        // the page id of the article
        int articleId = 1;

        // RevisionApi holds a database connection - close it when done
        try (RevisionApi revisionApi = new RevisionApi(config)) {

            int numberOfRevisions = revisionApi.getNumberOfRevisions(articleId);
            System.out.println("Number of revisions  : " + numberOfRevisions);
            System.out.println("First revision       : "
                    + revisionApi.getFirstDateOfAppearance(articleId));
            System.out.println("Latest revision      : "
                    + revisionApi.getLastDateOfAppearance(articleId));
            System.out.println("Unique contributors  : "
                    + revisionApi.getNumberOfUniqueContributors(articleId));

            // the first and the latest revision by revision counter
            Revision first = revisionApi.getRevision(articleId, 1);
            Revision latest = revisionApi.getRevision(articleId, numberOfRevisions);
            print("First revision", first);
            print("Latest revision", latest);

            // the same revision by its Wikipedia revision id
            Revision byId = revisionApi.getRevision(latest.getRevisionID());
            System.out.println("Same text            : "
                    + latest.getRevisionText().equals(byId.getRevisionText()));

            // the revision that was the current one at a given point in time
            List<Timestamp> timestamps = revisionApi.getRevisionTimestamps(articleId).stream()
                    .sorted().toList();
            Timestamp middle = timestamps.get(timestamps.size() / 2);
            print("Revision at " + middle, revisionApi.getRevision(articleId, middle));
        }
    }

    private static void print(String label, Revision revision)
    {
        System.out.println(label);
        System.out.println("  Revision id        : " + revision.getRevisionID());
        System.out.println("  Revision counter   : " + revision.getRevisionCounter());
        System.out.println("  Timestamp          : " + revision.getTimeStamp());
        System.out.println("  Contributor        : " + revision.getContributorName()
                + (revision.contributorIsRegistered() ? "" : " (not registered)"));
        System.out.println("  Minor edit         : " + revision.isMinor());
        System.out.println("  Comment            : " + revision.getComment());
        System.out.println("  Text length        : " + revision.getRevisionText().length());
    }
}
