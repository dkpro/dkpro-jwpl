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
package org.dkpro.jwpl.revisionmachine.difftool.data.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Schedules the processing of input archives on a fixed number of worker threads.
 * <p>
 * Each archive becomes one {@link ArchiveJob} with its own, deterministic output name that is
 * derived from the archive file name. The output of a job therefore does not depend on which
 * worker picks it up or in which order the jobs finish. The workers share no mutable state: each
 * job is expected to create its own diff calculator and output writer.
 * <p>
 * A failing job does not stop the others. All failures are collected and returned once every job
 * has finished.
 */
public final class ArchiveScheduler
{

    /**
     * Characters that are allowed in an output name; every other character is replaced.
     */
    private static final String ALLOWED_NAME_CHARS = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-.";

    /**
     * File name extensions that are stripped from an archive name, in this order.
     */
    private static final String[] STRIPPED_EXTENSIONS = { ".bz2", ".7z", ".gz", ".xml" };

    /**
     * One unit of work: an input archive together with the name of its output.
     *
     * @param index
     *            position of the archive in the configured archive list
     * @param archive
     *            the input archive
     * @param outputName
     *            prefix of the output files written for this archive
     */
    public record ArchiveJob(int index, ArchiveDescription archive, String outputName)
    {
    }

    /**
     * Processes a single archive job.
     */
    @FunctionalInterface
    public interface ArchiveJobProcessor
    {
        /**
         * Processes the given job.
         *
         * @param job
         *            the job to process
         * @throws Exception
         *             if the job failed
         */
        void process(ArchiveJob job) throws Exception;
    }

    private ArchiveScheduler()
    {
    }

    /**
     * Creates one job per archive. The output name of a job is the given prefix followed by the
     * archive file name without its directory and without the {@code .xml}, {@code .bz2},
     * {@code .7z} or {@code .gz} extensions. If two archives map to the same name, the later one
     * gets its (1-based) position in the archive list appended.
     *
     * @param archives
     *            the archives, in configuration order
     * @param prefix
     *            prefix of every output name
     * @return the jobs, in the order of the given archives
     */
    public static List<ArchiveJob> plan(final List<ArchiveDescription> archives,
            final String prefix)
    {
        Objects.requireNonNull(archives, "archives");
        Objects.requireNonNull(prefix, "prefix");

        List<ArchiveJob> jobs = new ArrayList<>(archives.size());
        Set<String> used = new HashSet<>();
        for (int i = 0; i < archives.size(); i++) {
            ArchiveDescription archive = archives.get(i);
            String name = prefix + baseName(archive.getPath());
            if (!used.add(name.toLowerCase(Locale.ROOT))) {
                String candidate = name + "-" + (i + 1);
                int n = 1;
                while (!used.add(candidate.toLowerCase(Locale.ROOT))) {
                    candidate = name + "-" + (i + 1) + "-" + n++;
                }
                name = candidate;
            }
            jobs.add(new ArchiveJob(i, archive, name));
        }
        return Collections.unmodifiableList(jobs);
    }

    /**
     * Runs the given jobs on {@code threads} worker threads and waits until all of them have
     * finished. Jobs are started in list order.
     *
     * @param jobs
     *            the jobs to run
     * @param threads
     *            maximum number of worker threads, at least 1
     * @param processor
     *            processes one job
     * @return the failed jobs with their exceptions, in job order; empty if all jobs succeeded
     * @throws InterruptedException
     *             if the calling thread was interrupted while waiting for the workers
     */
    public static Map<ArchiveJob, Throwable> run(final List<ArchiveJob> jobs, final int threads,
            final ArchiveJobProcessor processor)
        throws InterruptedException
    {
        Objects.requireNonNull(jobs, "jobs");
        Objects.requireNonNull(processor, "processor");
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be at least 1, was " + threads);
        }

        Map<ArchiveJob, Throwable> failures = new LinkedHashMap<>();
        if (jobs.isEmpty()) {
            return failures;
        }

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(threads, jobs.size()),
                new WorkerThreadFactory());
        try {
            List<Future<?>> futures = new ArrayList<>(jobs.size());
            for (ArchiveJob job : jobs) {
                futures.add(pool.submit(() -> {
                    processor.process(job);
                    return null;
                }));
            }
            for (int i = 0; i < jobs.size(); i++) {
                try {
                    futures.get(i).get();
                }
                catch (ExecutionException e) {
                    failures.put(jobs.get(i), e.getCause());
                }
            }
        }
        catch (InterruptedException e) {
            pool.shutdownNow();
            throw e;
        }
        finally {
            pool.shutdown();
        }
        return failures;
    }

    /**
     * Returns the file name of the given path without directories and known archive extensions,
     * restricted to characters that are safe in a file name.
     *
     * @param path
     *            path of an archive
     * @return base name of the archive
     */
    static String baseName(final String path)
    {
        String name = path;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        for (String extension : STRIPPED_EXTENSIONS) {
            if (name.length() > extension.length()
                    && name.toLowerCase(Locale.ROOT).endsWith(extension)) {
                name = name.substring(0, name.length() - extension.length());
            }
        }

        StringBuilder builder = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            builder.append(ALLOWED_NAME_CHARS.indexOf(c) >= 0 ? c : '_');
        }
        return builder.length() == 0 ? "archive" : builder.toString();
    }

    /**
     * Names the worker threads so that log output can be attributed to them.
     */
    private static final class WorkerThreadFactory
        implements ThreadFactory
    {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable r)
        {
            return new Thread(r, "DiffTool-archive-" + counter.incrementAndGet());
        }
    }
}
