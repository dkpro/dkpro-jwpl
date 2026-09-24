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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DecodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.EncodingException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.SQLConsumerException;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * The SQLEncoderInterface provides the link to the SQLEncoder who will define the formatting of the
 * output.
 */
public interface SQLEncoderInterface
{

    /**
     * Returns the tables for textual output.
     * <p>
     * Each array entry will contain a single SQL command.
     *
     * @return SQL command to create the tables
     */
    String[] getTable();

    /**
     * Returns the tables for binary output.
     * <p>
     * Each array entry will contain a single SQL command.
     *
     * @return SQL command to create the tables
     */
    String[] getBinaryTable();

    /**
     * Binds the rows of the given DiffTask with their binary encoded diffs to the given statement
     * and sends them to the database in batches.
     * <p>
     * The statement has to be prepared from {@link SQLEncoder#INSERT_REVISION}, and the revisions
     * table has to be created with {@link #getBinaryTable()}. A batch is sent as soon as the next
     * row would exceed the maximum packet size of the server, and the remaining rows are sent
     * before this method returns.
     *
     * @param task
     *            DiffTask
     * @param statement
     *            statement prepared from {@link SQLEncoder#INSERT_REVISION}
     * @return estimated number of bytes sent for the rows of the task
     * @throws ConfigurationException
     *             if problems occurred while initializing the components
     * @throws UnsupportedEncodingException
     *             if the CharacterSet defined in the configuration is not supported by Java.
     * @throws DecodingException
     *             if the decoding process fails (during the verification process)
     * @throws EncodingException
     *             if the encoding process fails
     * @throws SQLConsumerException
     *             if the verification process fails
     * @throws SQLException
     *             if binding the rows or sending them to the database fails
     */
    long binaryTask(final Task<Diff> task, final PreparedStatement statement)
        throws ConfigurationException, UnsupportedEncodingException, DecodingException,
        EncodingException, SQLConsumerException, SQLException;

    /**
     * Returns the textual encoding of the given DiffTask.
     * <p>
     * Each Array entry will contain a single SQL command.
     *
     * @param task
     *            DiffTask
     * @return binary encoding of the task.
     * @throws ConfigurationException
     *             if problems occurred while initializing the components
     * @throws UnsupportedEncodingException
     *             if the CharacterSet defined in the configuration is not supported by JAVA.
     * @throws DecodingException
     *             if the decoding process fails (during the verification process)
     * @throws EncodingException
     *             if the encoding process fails
     * @throws SQLConsumerException
     *             if the verification process fails
     */
    SQLEncoding[] encodeTask(final Task<Diff> task)
        throws ConfigurationException, UnsupportedEncodingException, DecodingException,
        EncodingException, SQLConsumerException;

}
