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

import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;
import org.dkpro.jwpl.revisionmachine.api.RevisionIterator;

/**
 * RevisionMachine Tutorial 4
 * <p>
 * Iterates over all revisions in the database with a {@link RevisionIterator}. The revisions are
 * returned in the order in which they are stored: article by article and, within an article, by
 * revision counter. The iterator reconstructs the text of each revision from the previous one, so
 * this is the most efficient way to process the complete history.
 */
public class T4_RevisionIterator
{

    public static void main(String[] args) throws WikiApiException, SQLException
    {
        // configure the connection to the revision database
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("SERVER_URL");
        config.setDatabase("DATABASE");
        config.setUser("USER");
        config.setPassword("PASSWORD");

        // number of revisions fetched from the database per query
        config.setBufferSize(10000);

        int articles = 0;
        long revisions = 0;
        long characters = 0;

        // pass 'true' as second argument to skip the text reconstruction; the text of a
        // revision is then loaded on demand when getRevisionText() is called
        try (RevisionIterator it = new RevisionIterator(config)) {
            int lastArticleId = -1;
            while (it.hasNext()) {
                Revision revision = it.next();
                // null is returned for a revision that could not be reconstructed
                if (revision == null) {
                    continue;
                }
                if (revision.getArticleID() != lastArticleId) {
                    lastArticleId = revision.getArticleID();
                    articles++;
                }
                revisions++;
                characters += revision.getRevisionText().length();
            }
        }

        System.out.println("Articles             : " + articles);
        System.out.println("Revisions            : " + revisions);
        System.out.println("Characters (total)   : " + characters);
    }
}
