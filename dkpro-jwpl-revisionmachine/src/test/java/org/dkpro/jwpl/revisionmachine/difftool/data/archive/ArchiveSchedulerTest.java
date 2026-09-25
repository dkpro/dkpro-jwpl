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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveScheduler.ArchiveJob;
import org.junit.jupiter.api.Test;

/**
 * Tests the archive-level scheduling of the DiffTool (see issue #548).
 */
class ArchiveSchedulerTest
{

    private static ArchiveDescription archive(final String path)
    {
        return new ArchiveDescription(InputType.BZIP2, path);
    }

    private static List<ArchiveDescription> archives(final int count)
    {
        List<ArchiveDescription> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(archive("dumps/enwiki-pages-meta-history" + i + ".xml.bz2"));
        }
        return list;
    }

    @Test
    void derivesOutputNamesFromTheArchiveFileNames()
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(List.of(
                archive("/data/enwiki-20260101-pages-meta-history1.xml-p1p812.bz2"),
                archive("C:\\dumps\\dewiki-20260101-pages-meta-history.xml.7z"),
                archive("./aawiki.xml.gz"),
                archive("plain.xml")), "output_");

        assertEquals(List.of("output_enwiki-20260101-pages-meta-history1.xml-p1p812",
                "output_dewiki-20260101-pages-meta-history", "output_aawiki", "output_plain"),
                jobs.stream().map(ArchiveJob::outputName).toList());
        for (int i = 0; i < jobs.size(); i++) {
            assertEquals(i, jobs.get(i).index());
        }
    }

    @Test
    void replacesUnsafeCharactersInOutputNames()
    {
        assertEquals("my_wiki__dump_", ArchiveScheduler.baseName("dir/my wiki (dump).xml.bz2"));
        assertEquals("archive", ArchiveScheduler.baseName("dir/"));
    }

    @Test
    void makesDuplicateOutputNamesUnique()
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(List.of(
                archive("a/wiki.xml.bz2"),
                archive("b/wiki.xml.bz2"),
                archive("c/WIKI.xml.7z"),
                archive("d/wiki-2.xml.bz2")), "output_");

        assertEquals(List.of("output_wiki", "output_wiki-2", "output_WIKI-3", "output_wiki-2-4"),
                jobs.stream().map(ArchiveJob::outputName).toList());
    }

    @Test
    void planIsDeterministic()
    {
        List<ArchiveDescription> list = archives(20);
        assertEquals(ArchiveScheduler.plan(list, "output_"), ArchiveScheduler.plan(list, "output_"));
    }

    @Test
    void runsEveryJobExactlyOnce() throws InterruptedException
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(50), "output_");
        Map<Integer, AtomicInteger> calls = new ConcurrentHashMap<>();

        Map<ArchiveJob, Throwable> failures = ArchiveScheduler.run(jobs, 4,
                job -> calls.computeIfAbsent(job.index(), k -> new AtomicInteger())
                        .incrementAndGet());

        assertTrue(failures.isEmpty());
        assertEquals(jobs.size(), calls.size());
        calls.values().forEach(count -> assertEquals(1, count.get()));
    }

    @Test
    void runsJobsInListOrderOnASingleThread() throws InterruptedException
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(10), "output_");
        List<Integer> order = new ArrayList<>();
        Set<String> threads = ConcurrentHashMap.newKeySet();

        ArchiveScheduler.run(jobs, 1, job -> {
            order.add(job.index());
            threads.add(Thread.currentThread().getName());
        });

        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), order);
        assertEquals(1, threads.size());
    }

    @Test
    void runsJobsConcurrentlyUpToTheThreadLimit() throws InterruptedException
    {
        int threads = 3;
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(9), "output_");
        CountDownLatch allWorkersBusy = new CountDownLatch(threads);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxRunning = new AtomicInteger();

        Map<ArchiveJob, Throwable> failures = ArchiveScheduler.run(jobs, threads, job -> {
            maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
            allWorkersBusy.countDown();
            // the first jobs only finish once every worker holds a job at the same time
            assertTrue(allWorkersBusy.await(10, TimeUnit.SECONDS));
            running.decrementAndGet();
        });

        assertTrue(failures.isEmpty(), failures::toString);
        assertEquals(threads, maxRunning.get());
    }

    @Test
    void doesNotStartMoreThreadsThanJobs() throws InterruptedException
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(2), "output_");
        Set<String> threads = ConcurrentHashMap.newKeySet();
        CountDownLatch bothStarted = new CountDownLatch(2);

        ArchiveScheduler.run(jobs, 16, job -> {
            threads.add(Thread.currentThread().getName());
            bothStarted.countDown();
            assertTrue(bothStarted.await(10, TimeUnit.SECONDS));
        });

        assertEquals(2, threads.size());
    }

    @Test
    void aFailingJobDoesNotStopTheOthers() throws InterruptedException
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(6), "output_");
        IllegalStateException boom = new IllegalStateException("broken archive");
        Set<Integer> done = ConcurrentHashMap.newKeySet();

        Map<ArchiveJob, Throwable> failures = ArchiveScheduler.run(jobs, 2, job -> {
            if (job.index() == 1 || job.index() == 4) {
                throw boom;
            }
            done.add(job.index());
        });

        assertEquals(Set.of(0, 2, 3, 5), done);
        assertEquals(List.of(jobs.get(1), jobs.get(4)), new ArrayList<>(failures.keySet()));
        failures.values().forEach(e -> assertSame(boom, e));
    }

    @Test
    void acceptsAnEmptyJobList() throws InterruptedException
    {
        assertTrue(ArchiveScheduler.run(List.of(), 4, job -> {
            throw new AssertionError("no job expected");
        }).isEmpty());
    }

    @Test
    void rejectsLessThanOneThread()
    {
        List<ArchiveJob> jobs = ArchiveScheduler.plan(archives(1), "output_");
        assertThrows(IllegalArgumentException.class, () -> ArchiveScheduler.run(jobs, 0, job -> {
        }));
    }
}
