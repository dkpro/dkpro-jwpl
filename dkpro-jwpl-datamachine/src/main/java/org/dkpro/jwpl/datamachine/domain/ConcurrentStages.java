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
package org.dkpro.jwpl.datamachine.domain;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs a group of mutually independent processing stages, either one after another on the calling
 * thread or concurrently on a bounded pool of worker threads.
 * <p>
 * The stages of one group must not depend on each other: each one has to read only state that was
 * published before {@link #run(int, List)} was called, and has to write state and output files no
 * other stage of the group touches. Under that contract the result does not depend on the order in
 * which the stages execute. Everything a stage wrote is visible to the calling thread once
 * {@link #run(int, List)} returns.
 * <p>
 * If a stage fails, the remaining ones are cancelled: stages that have not started yet are skipped
 * and running ones are interrupted. {@link #run(int, List)} returns only after every stage has
 * terminated, so no stage can still be writing when the failure reaches the caller. The failure of
 * the stage that failed first is rethrown; stages that failed as well before they were cancelled
 * are attached to it as suppressed exceptions.
 */
final class ConcurrentStages
{

    private static final String THREAD_NAME_PREFIX = "jwpl-datamachine-stage-";

    /**
     * A single processing stage.
     */
    @FunctionalInterface
    interface Stage
    {
        /**
         * Executes this stage.
         *
         * @throws IOException Thrown if IO errors occurred during processing.
         */
        void run() throws IOException;
    }

    private ConcurrentStages()
    {
        // static-only
    }

    /**
     * Runs the given {@code stages}.
     *
     * @param parallelism The maximum number of stages to run at the same time. With {@code 1} the
     *                    stages run one after another, in list order, on the calling thread, and
     *                    the first failure is propagated right away.
     * @param stages      The mutually independent stages to run. Must not be {@code null}.
     * @throws IOException              Thrown if a stage failed with an {@link IOException}, or if
     *                                  the calling thread was interrupted while waiting.
     * @throws IllegalArgumentException Thrown if {@code parallelism} is less than {@code 1}.
     */
    static void run(int parallelism, List<Stage> stages) throws IOException
    {
        if (parallelism < 1) {
            throw new IllegalArgumentException(
                    "The parallelism must be at least 1, got " + parallelism + ".");
        }
        if (parallelism == 1 || stages.size() < 2) {
            for (Stage stage : stages) {
                stage.run();
            }
            return;
        }
        runConcurrently(Math.min(parallelism, stages.size()), stages);
    }

    private static void runConcurrently(int threads, List<Stage> stages) throws IOException
    {
        final ExecutorService pool = Executors.newFixedThreadPool(threads, new StageThreads());
        final CompletionService<Void> completion = new ExecutorCompletionService<>(pool);
        final List<Future<Void>> futures = new ArrayList<>(stages.size());
        Throwable failure = null;
        try {
            for (Stage stage : stages) {
                futures.add(completion.submit(() -> {
                    stage.run();
                    return null;
                }));
            }
            // Stages are awaited in completion order, so the first failure is seen as soon as it
            // happens and not only once the stages submitted before it have finished.
            for (int i = 0; i < futures.size(); i++) {
                final Throwable cause = causeOf(completion.take());
                if (cause != null && failure == null) {
                    failure = cause;
                    futures.forEach(f -> f.cancel(true));
                }
                else if (cause != null) {
                    failure.addSuppressed(cause);
                }
            }
        }
        catch (InterruptedException e) {
            futures.forEach(f -> f.cancel(true));
            Thread.currentThread().interrupt();
            final InterruptedIOException interrupted = new InterruptedIOException(
                    "Interrupted while waiting for the processing stages to finish.");
            interrupted.initCause(e);
            throw interrupted;
        }
        finally {
            // A cancelled future completes right away, while its stage may still be running
            awaitTermination(pool);
        }
        if (failure != null) {
            rethrow(failure);
        }
    }

    /**
     * @param future A future that has completed.
     * @return The failure of the stage behind {@code future}, or {@code null} if it succeeded or
     *         was cancelled before it could fail.
     */
    private static Throwable causeOf(Future<Void> future) throws InterruptedException
    {
        if (future.isCancelled()) {
            return null;
        }
        try {
            future.get();
            return null;
        }
        catch (ExecutionException e) {
            return e.getCause();
        }
    }

    /**
     * Shuts the {@code pool} down and waits for every stage to terminate, even if the calling
     * thread is interrupted in the meantime; the interrupt status is kept.
     */
    private static void awaitTermination(ExecutorService pool)
    {
        pool.shutdownNow();
        boolean interrupted = Thread.interrupted();
        while (true) {
            try {
                if (pool.awaitTermination(1, TimeUnit.MINUTES)) {
                    break;
                }
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static void rethrow(Throwable failure) throws IOException
    {
        if (failure instanceof IOException io) {
            throw io;
        }
        if (failure instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (failure instanceof Error error) {
            throw error;
        }
        // Stage.run() only declares IOException, this is unreachable in practice
        throw new IOException(failure);
    }

    /**
     * Creates named daemon threads, so that a stage stuck in an uninterruptible read cannot keep
     * the JVM alive on its own.
     */
    private static final class StageThreads
        implements ThreadFactory
    {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable)
        {
            final Thread thread = new Thread(runnable,
                    THREAD_NAME_PREFIX + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
