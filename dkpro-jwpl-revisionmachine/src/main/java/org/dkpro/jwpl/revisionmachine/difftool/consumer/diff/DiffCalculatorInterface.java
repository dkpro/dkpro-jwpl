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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DiffException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.TimeoutException;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;

/**
 * The DiffCalculatorInterface represents the interface to the diff processing unit.
 * <p>
 * Please notice that there is no default method to return the generated diff. The current
 * implementation uses the TaskTransmitterInterface (given as parameter of the constructor) to send
 * the diffed data to the DiffProducer.
 */
public interface DiffCalculatorInterface
{

    /**
     * This method process the given task to generate the diff.
     *
     * @param task
     *            RevisionTask
     * @throws DiffException
     *             if the diff process fails
     * @throws TimeoutException
     *             if the TaskTransmitter times out during the transmission of the task to the
     *             DiffProducer.
     * @throws UnsupportedEncodingException
     *             if the CharacterSet defined in the configuration is not supported by JAVA.
     */
    void process(final Task<Revision> task)
        throws DiffException, TimeoutException, UnsupportedEncodingException;

    /**
     * This method is used to delete all information concerning the partial task processing.
     * <p>
     * This method has to be called if the process method throws an exception.
     */
    void reset();

    /**
     * Close Stream of Transmitter
     *
     * @throws IOException
     * @throws SQLException
     */
    void closeTransmitter() throws IOException, SQLException;

}
