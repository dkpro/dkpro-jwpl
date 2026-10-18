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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.dkpro.jwpl.datamachine.domain.ConcurrentStages.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class ConcurrentStagesTest {

  private static final long WAIT_SECONDS = 10;

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void testRejectsNonPositiveParallelism(int parallelism) {
    assertThrows(IllegalArgumentException.class,
            () -> ConcurrentStages.run(parallelism, List.of(() -> { })));
  }

  @Test
  void testSequentialRunsStagesInOrderOnCallingThread() throws IOException {
    List<String> calls = new CopyOnWriteArrayList<>();
    Thread caller = Thread.currentThread();
    ConcurrentStages.run(1, List.of(
            () -> calls.add("a:" + (Thread.currentThread() == caller)),
            () -> calls.add("b:" + (Thread.currentThread() == caller)),
            () -> calls.add("c:" + (Thread.currentThread() == caller))));
    assertEquals(List.of("a:true", "b:true", "c:true"), calls);
  }

  @Test
  void testSequentialStopsAtFirstFailure() {
    IOException failure = new IOException("boom");
    AtomicBoolean laterStageRan = new AtomicBoolean();
    IOException thrown = assertThrows(IOException.class, () -> ConcurrentStages.run(1, List.of(
            () -> { },
            () -> { throw failure; },
            () -> laterStageRan.set(true))));
    assertSame(failure, thrown);
    assertFalse(laterStageRan.get());
  }

  @Test
  void testParallelRunsAllStagesAtTheSameTimeOffTheCallingThread() throws IOException {
    // Every stage waits for the other two, so this only completes if all three run concurrently
    CyclicBarrier allRunning = new CyclicBarrier(3);
    Set<Thread> threads = ConcurrentHashMap.newKeySet();
    Stage stage = () -> {
      threads.add(Thread.currentThread());
      try {
        allRunning.await(WAIT_SECONDS, TimeUnit.SECONDS);
      } catch (Exception e) {
        throw new IOException(e);
      }
    };
    ConcurrentStages.run(4, List.of(stage, stage, stage));
    assertEquals(3, threads.size());
    assertFalse(threads.contains(Thread.currentThread()));
    for (Thread thread : threads) {
      assertTrue(thread.isDaemon());
      assertTrue(thread.getName().startsWith("jwpl-datamachine-stage-"));
    }
  }

  @Test
  void testParallelismCapsTheNumberOfConcurrentStages() throws IOException {
    AtomicInteger maxRunning = new AtomicInteger();
    AtomicInteger running = new AtomicInteger();
    Stage stage = () -> {
      int now = running.incrementAndGet();
      maxRunning.accumulateAndGet(now, Math::max);
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      running.decrementAndGet();
    };
    ConcurrentStages.run(2, List.of(stage, stage, stage, stage, stage));
    assertTrue(maxRunning.get() <= 2, "at most two stages may run at a time: " + maxRunning.get());
  }

  @Test
  void testParallelResultsAreVisibleAfterReturn() throws IOException {
    int[] results = new int[3];
    ConcurrentStages.run(3, List.of(() -> results[0] = 1, () -> results[1] = 2,
            () -> results[2] = 3));
    assertEquals(1, results[0]);
    assertEquals(2, results[1]);
    assertEquals(3, results[2]);
  }

  @Test
  void testParallelFailureCancelsSiblingsAndPropagatesOriginalException() throws Exception {
    IOException failure = new IOException("boom");
    CountDownLatch siblingStarted = new CountDownLatch(1);
    AtomicBoolean siblingInterrupted = new AtomicBoolean();
    AtomicBoolean siblingTerminated = new AtomicBoolean();
    Stage sibling = () -> {
      siblingStarted.countDown();
      try {
        // Would block for good if the failure did not cancel this stage
        new CountDownLatch(1).await();
      } catch (InterruptedException e) {
        siblingInterrupted.set(true);
      } finally {
        // Give the caller a chance to return too early, should it not wait for this stage
        sleepUninterruptibly(100);
        siblingTerminated.set(true);
      }
    };
    Stage failing = () -> {
      try {
        siblingStarted.await(WAIT_SECONDS, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      throw failure;
    };
    IOException thrown = assertThrows(IOException.class,
            () -> ConcurrentStages.run(2, List.of(sibling, failing)));
    assertSame(failure, thrown);
    assertTrue(siblingInterrupted.get(), "the running sibling must be interrupted");
    assertTrue(siblingTerminated.get(), "run() must not return before every stage terminated");
  }

  @Test
  void testParallelPropagatesRuntimeExceptionUnwrapped() {
    IllegalStateException failure = new IllegalStateException("boom");
    IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> ConcurrentStages.run(2, List.of(() -> { }, () -> { throw failure; })));
    assertSame(failure, thrown);
  }

  @Test
  void testParallelInterruptOfCallerCancelsStages() throws Exception {
    CountDownLatch stageStarted = new CountDownLatch(1);
    AtomicBoolean stageInterrupted = new AtomicBoolean();
    Stage blocking = () -> {
      stageStarted.countDown();
      try {
        new CountDownLatch(1).await();
      } catch (InterruptedException e) {
        stageInterrupted.set(true);
      }
    };
    AtomicReference<Throwable> outcome = new AtomicReference<>();
    AtomicBoolean callerStillInterrupted = new AtomicBoolean();
    Thread caller = new Thread(() -> {
      try {
        ConcurrentStages.run(2, List.of(blocking, blocking));
      } catch (Throwable t) {
        outcome.set(t);
      }
      callerStillInterrupted.set(Thread.currentThread().isInterrupted());
    });
    caller.start();
    assertTrue(stageStarted.await(WAIT_SECONDS, TimeUnit.SECONDS));
    caller.interrupt();
    caller.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
    assertFalse(caller.isAlive());
    assertTrue(outcome.get() instanceof InterruptedIOException, String.valueOf(outcome.get()));
    assertTrue(stageInterrupted.get());
    assertTrue(callerStillInterrupted.get(), "the interrupt status must be kept");
  }

  @Test
  void testSingleStageRunsOnCallingThreadEvenWithParallelism() throws IOException {
    AtomicReference<Thread> thread = new AtomicReference<>();
    ConcurrentStages.run(4, List.of(() -> thread.set(Thread.currentThread())));
    assertSame(Thread.currentThread(), thread.get());
  }

  private static void sleepUninterruptibly(long millis) {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
    long remaining;
    while ((remaining = deadline - System.nanoTime()) > 0) {
      try {
        TimeUnit.NANOSECONDS.sleep(remaining);
      } catch (InterruptedException e) {
        // keep sleeping
      }
    }
  }
}
