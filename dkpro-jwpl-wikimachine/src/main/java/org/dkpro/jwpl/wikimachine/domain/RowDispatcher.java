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
package org.dkpro.jwpl.wikimachine.domain;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;

/**
 * Hands the rows of one table pass to all {@link IDumpVersion versions}.
 * <p>
 * Whatever the implementation, every version receives every row, in the order the rows were
 * read, and never on two threads at the same time. The output of a version therefore does not
 * depend on the implementation.
 *
 * @param <P> The type of the parser reading the table.
 */
interface RowDispatcher<P>
    extends AutoCloseable
{

    /**
     * Hands the row the parser is positioned on to all versions, possibly later and on other
     * threads. The parser may be advanced as soon as this method returns.
     *
     * @param parser A parser positioned on a row.
     * @throws IOException Thrown if a version failed to process this or an earlier row.
     */
    void accept(P parser) throws IOException;

    /**
     * Waits until all versions have processed all accepted rows.
     *
     * @throws IOException Thrown if a version failed to process a row.
     */
    void finish() throws IOException;

    /**
     * Releases the threads, if any. Does not wait for rows that are still being processed after a
     * failure to be completed.
     */
    @Override
    void close();

    /**
     * @param versions  The versions to hand the rows to.
     * @param threads   The number of worker threads; {@code 1} to call the versions on the
     *                  calling thread.
     * @param batchSize The number of rows handed to the worker threads at once.
     * @param batches   Creates an empty batch for the rows of the table.
     * @param handler   Hands a single row to a version.
     * @param <P>       The type of the parser reading the table.
     * @return A dispatcher calling the versions on the calling thread, if {@code threads} is
     *         {@code 1}, or else one using as many worker threads.
     */
    static <P> RowDispatcher<P> create(IDumpVersion[] versions, int threads, int batchSize,
            Supplier<RowBatch<P>> batches, RowHandler<P> handler)
    {
        if (threads <= 1) {
            return new Serial<>(versions, handler);
        }
        return new Parallel<>(versions, handler, batches, threads, batchSize);
    }

    /**
     * Calls every version in turn, on the calling thread.
     */
    final class Serial<P>
        implements RowDispatcher<P>
    {
        private final IDumpVersion[] versions;
        private final RowHandler<P> handler;

        Serial(IDumpVersion[] versions, RowHandler<P> handler)
        {
            this.versions = versions;
            this.handler = handler;
        }

        @Override
        public void accept(P parser) throws IOException
        {
            for (IDumpVersion version : versions) {
                handler.handle(version, parser);
            }
        }

        @Override
        public void finish()
        {
            // every row has been processed on arrival
        }

        @Override
        public void close()
        {
            // no resources
        }
    }

    /**
     * Copies the rows into batches and processes each batch on a pool of worker threads, one task
     * per version.
     * <p>
     * A batch is only handed to the versions once all of them have processed the previous one,
     * which keeps the order of the rows per version. While the workers process a batch, the
     * calling thread reads the dump into the next one, so at most two batches exist at a time.
     */
    final class Parallel<P>
        implements RowDispatcher<P>
    {
        private static final AtomicInteger POOL_COUNT = new AtomicInteger();

        private final IDumpVersion[] versions;
        private final RowHandler<P> handler;
        private final int batchSize;
        private final ExecutorService pool;
        private final List<Future<?>> pending;

        /** The batch the calling thread fills. */
        private RowBatch<P> filling;
        /** The batch the workers process, or processed last. */
        private RowBatch<P> inFlight;

        /**
         * @param versions  The versions to hand the rows to.
         * @param handler   Hands a single row to a version.
         * @param batches   Creates an empty batch; called twice.
         * @param threads   The number of worker threads, at least one.
         * @param batchSize The number of rows collected before a batch is handed to the versions,
         *                  at least one.
         */
        Parallel(IDumpVersion[] versions, RowHandler<P> handler, Supplier<RowBatch<P>> batches,
                int threads, int batchSize)
        {
            if (threads < 1) {
                throw new IllegalArgumentException("threads must be positive: " + threads);
            }
            if (batchSize < 1) {
                throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
            }
            this.versions = versions;
            this.handler = handler;
            this.batchSize = batchSize;
            this.filling = batches.get();
            this.inFlight = batches.get();
            this.pending = new ArrayList<>(versions.length);
            this.pool = Executors.newFixedThreadPool(threads, workerThreads());
        }

        private static ThreadFactory workerThreads()
        {
            final String prefix = "dump-version-" + POOL_COUNT.incrementAndGet() + "-worker-";
            final AtomicInteger count = new AtomicInteger();
            return runnable -> {
                final Thread thread = new Thread(runnable, prefix + count.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            };
        }

        @Override
        public void accept(P parser) throws IOException
        {
            filling.add(parser);
            if (filling.size() >= batchSize) {
                dispatch();
            }
        }

        @Override
        public void finish() throws IOException
        {
            if (filling.size() > 0) {
                dispatch();
            }
            awaitPending();
        }

        private void dispatch() throws IOException
        {
            // Barrier: the previous batch has been processed by all versions, so the next one
            // may be handed out and the previous one refilled.
            awaitPending();
            final RowBatch<P> batch = filling;
            for (IDumpVersion version : versions) {
                pending.add(pool.submit(() -> {
                    batch.replay(version, handler);
                    return null;
                }));
            }
            filling = inFlight;
            filling.clear();
            inFlight = batch;
        }

        private void awaitPending() throws IOException
        {
            try {
                for (Future<?> future : pending) {
                    future.get();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                final InterruptedIOException interrupted = new InterruptedIOException(
                        "Interrupted while waiting for the dump versions.");
                interrupted.initCause(e);
                throw interrupted;
            }
            catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException io) {
                    throw io;
                }
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw new IOException(cause);
            }
            finally {
                pending.clear();
            }
        }

        @Override
        public void close()
        {
            pool.shutdownNow();
            try {
                // Normally all tasks are done already. After a failure, the remaining tasks
                // are given the chance to end before the versions are touched again.
                pool.awaitTermination(1, TimeUnit.MINUTES);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
