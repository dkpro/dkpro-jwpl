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
package org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.calculation;

import java.io.UnsupportedEncodingException;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.DiffException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.TimeoutException;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.content.Diff;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * Calculates the Diff while collecting statistical data.
 */
public class TimedDiffCalculator
    extends DiffCalculator
{

    /**
     * Temporary variable - revision counter
     */
    private int revisionCounter;

    /**
     * Temporary variable - diff part counter
     */
    private int diffPartCounter;

    /**
     * Temporary variable - size of the diff
     */
    private long diffedSize;

    /**
     * Temporary variable - start time of the diff processing
     */
    private long startTime;

    /**
     * Temporary variable - time used for the diff processing
     */
    private long processingTimeDiff;

    /**
     * Temporary variable - number of ignored revisions
     */
    private int ignoredRevisionsCounter;

    /**
     * (Constructor) Creates a new DiffCalculator object.
     *
     * @param taskTransmitter
     *            Reference to the TaskTransmitter
     * @throws ConfigurationException
     *             if an error occurred while accessing the configuration
     */
    public TimedDiffCalculator(final TaskTransmitterInterface taskTransmitter)
        throws ConfigurationException
    {
        super(taskTransmitter);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Initializes the processing of a new RevisionTask.
     *
     * @param taskID
     *            Article ID
     */
    protected void initNewTask(final int taskID)
    {

        super.initNewTask(taskID);

        this.processingTimeDiff = 0;

        this.revisionCounter = 0;
        this.ignoredRevisionsCounter = 0;

        this.diffPartCounter = 0;
        this.diffedSize = 0;
    }

    /**
     * Transmits a partial DiffTask.
     *
     * @param result
     *            Reference to the DiffTask
     * @throws TimeoutException
     *             if a timeout occurred
     */
    protected void transmitPartialTask(final Task<Diff> result) throws TimeoutException
    {

        this.diffedSize += result.byteSize();
        this.processingTimeDiff += System.currentTimeMillis() - startTime;

        super.transmitPartialTask(result);

        startTime = System.currentTimeMillis();
    }

    /**
     * Transmits the DiffTask at the end of the RevisionTask processing.
     *
     * @param task
     *            Reference to the RevisionTask
     * @param result
     *            Reference to the DiffTask
     * @throws TimeoutException
     *             if a timeout occurred
     */
    protected void transmitAtEndOfTask(final Task<Revision> task, final Task<Diff> result)
        throws TimeoutException
    {

        this.processingTimeDiff += System.currentTimeMillis() - startTime;

        if (task.getTaskType() == TaskTypes.TASK_FULL
                || task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {

            diffedSize += result.byteSize();

            ArticleInformation info = result.getHeader();
            info.setRevisionCounter(revisionCounter);
            info.setIgnoredRevisionsCounter(ignoredRevisionsCounter);
            info.setDiffedSize(diffedSize);
            info.setDiffPartCounter(diffPartCounter);
            info.setProcessingTimeRead(task.getHeader().getProcessingTimeRead());
            info.setProcessingTimeDiff(processingTimeDiff);
        }

        super.transmitAtEndOfTask(task, result);
    }

    /**
     * Calculates the diff for the given revision.
     *
     * @param revision
     *            Reference to a revision
     * @return Diff
     * @throws UnsupportedEncodingException
     *             if the character encoding is unsupported
     */
    protected Diff processRevision(final Revision revision) throws UnsupportedEncodingException
    {

        Diff diff = super.processRevision(revision);
        if (diff == null) {
            this.ignoredRevisionsCounter++;
        }
        else {
            this.revisionCounter++;
            this.diffPartCounter += diff.size();
        }

        return diff;
    }

    /*--------------------------------------------------------------------------*/

    @Override
    public void process(final Task<Revision> task)
        throws DiffException, TimeoutException, UnsupportedEncodingException
    {

        this.startTime = System.currentTimeMillis();
        super.process(task);
    }
}
