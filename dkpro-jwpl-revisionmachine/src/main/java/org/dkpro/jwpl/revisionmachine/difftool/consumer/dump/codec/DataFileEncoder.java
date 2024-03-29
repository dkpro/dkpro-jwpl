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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.dump.codec;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.EncodingException;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionEncoder;
import org.dkpro.jwpl.revisionmachine.difftool.data.codec.RevisionEncoderInterface;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * Alternative to the SQLEncoder - writes data files instead of UNCOMPRESSED dumps
 */
public class DataFileEncoder
{

    /**
     * Reference to the RevisionApi
     */
    private final RevisionEncoderInterface encoder;

    /**
     * Last used ID of a full revision
     */
    private int lastFullRevID = -1;

    /**
     * (Constructor) Creates a new SQLEncoder object.
     *
     * @throws ConfigurationException
     *             if an error occurred while accessing the configuration
     */
    public DataFileEncoder() throws ConfigurationException
    {
        this.encoder = new RevisionEncoder();
    }

    /**
     * Encodes the diff.
     *
     * @param task
     *            Reference to the DiffTask
     * @param diff
     *            Diff to encode
     * @return Base 64 encoded Diff
     * @throws UnsupportedEncodingException
     *             if the character encoding is unsupported
     * @throws EncodingException
     *             if the encoding failed
     */
    protected String encodeDiff(final Task<Diff> task, final Diff diff)
        throws UnsupportedEncodingException, EncodingException
    {

        return encoder.encodeDiff(diff.getCodecData(), diff);
    }

    public List<String> encodeTask(final Task<Diff> task)
        throws UnsupportedEncodingException, DecodingException, EncodingException
    {

        // this.task = task;
        if (task.getTaskType() == TaskTypes.TASK_FULL
                || task.getTaskType() == TaskTypes.TASK_PARTIAL_FIRST) {

            this.lastFullRevID = -1;
        }

        int articleId = task.getHeader().getArticleId();
        Diff diff;

        ArrayList<String> list = new ArrayList<>();

        String tempData;

        int size = task.size();
        for (int i = 0; i < size; i++) {

            diff = task.get(i);

            if (diff.isFullRevision()) {
                this.lastFullRevID = diff.getRevisionID();
            }

            /*
             * prepare values that might be null because we don't want quotes if they are null
             *
             * Furthermore, escape quote-characters. Quotes are used as the "ENCLOSED BY" character
             * in MySQL to mark begin and end of Strings
             */

            // prepare values that might be null
            // because we don't want quotes if they are null
            String comm = diff.getComment();
            String comment = comm == null ? "\\N" : "\"" + escape(comm) + "\"";

            Integer cId = diff.getContributorId();
            String contributorId = cId == null ? "\\N" : cId.toString();

            String cName = diff.getContributorName();
            String contributorName = cName == null ? "\\N" : "\"" + escape(cName) + "\"";

            // Prepare the actual data item
            tempData = "\\N," + this.lastFullRevID + "," + diff.getRevisionCounter() + ","
                    + diff.getRevisionID() + "," + articleId + "," + diff.getTimeStamp().getTime()
                    + ",\"" + encodeDiff(task, diff) + "\"," + comment + ","
                    + (diff.isMinor() ? "1" : "0") + "," + contributorName + "," + contributorId
                    + "," + (diff.getContributorIsRegistered() ? "1" : "0");

            // add item to the list
            list.add(tempData);
        }

        return list;
    }

    private String escape(String str)
    {
        return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }

}
