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
import org.dkpro.jwpl.revisionmachine.api.ChronoRevisionIterator;
import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.api.RevisionAPIConfiguration;

/**
 * RevisionMachine Tutorial 5
 * <p>
 * Iterates over the revisions of a range of articles in chronological order with a
 * {@link ChronoRevisionIterator}. The order of the revisions in a dump - and thus the revision
 * counter - does not always follow their timestamps. For such articles, the IndexGenerator
 * stores a mapping in the {@code index_chronological} table, which this iterator uses to return
 * the revisions of each article ordered by timestamp. Articles are visited in ascending order of
 * their ids.
 */
public class T5_ChronoRevisionIterator
{

    public static void main(String[] args) throws WikiApiException, SQLException
    {
        // configure the connection to the revision database
        RevisionAPIConfiguration config = new RevisionAPIConfiguration();
        config.setHost("SERVER_URL");
        config.setDatabase("DATABASE");
        config.setUser("USER");
        config.setPassword("PASSWORD");

        // memory (in bytes) used to buffer revisions while reordering them
        config.setChronoStorageSpace(100 * 1024 * 1024);

        // iterate over the articles with the ids 1 to 100 (both inclusive);
        // new ChronoRevisionIterator(config) iterates over all articles
        ChronoRevisionIterator it = new ChronoRevisionIterator(config, 1, 100);
        try {
            while (it.hasNext()) {
                Revision revision = it.next();
                if (revision == null) {
                    continue;
                }
                System.out.println(revision.getArticleID() + "\t" + revision.getTimeStamp() + "\t"
                        + revision.getRevisionCounter() + "\t" + revision.getRevisionID() + "\t"
                        + revision.getRevisionText().length());
            }
        }
        finally {
            it.close();
        }
    }
}
