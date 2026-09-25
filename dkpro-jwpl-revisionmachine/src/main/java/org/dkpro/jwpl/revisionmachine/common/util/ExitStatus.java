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
package org.dkpro.jwpl.revisionmachine.common.util;

/**
 * The exit statuses of the command line tools.
 */
public final class ExitStatus
{

    /**
     * A tool completed successfully.
     */
    public static final int EXIT_SUCCESS = 0;

    /**
     * A tool failed, for instance because an input file is missing or corrupt.
     */
    public static final int EXIT_FAILURE = 1;

    /**
     * A tool was called with missing or invalid arguments.
     */
    public static final int EXIT_USAGE = 255;

    private ExitStatus()
    {
    }

    /**
     * Terminates the JVM with the given status unless it is {@link #EXIT_SUCCESS}.
     * <p>
     * A successful run returns normally from {@code main}, so the JVM exits once all non-daemon
     * threads have finished.
     *
     * @param status The exit status of a tool.
     */
    public static void exitOnFailure(int status)
    {
        if (status != EXIT_SUCCESS) {
            System.exit(status);
        }
    }
}
