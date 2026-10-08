/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class ErpInteractionPollerTest {

  @Test
  void pollerShouldPollSuccessfulInteractionOnFirstIterationWithDefaultPredicate() {
    val expectedInteraction = interaction(readyTask("Task/ready"), ErxTask.class);
    Supplier<ErpInteraction<ErxTask>> supplier = () -> expectedInteraction;

    val actualInteraction = getPollBuilder(3, 0).poll(supplier);

    assertSame(expectedInteraction, actualInteraction);
  }

  @Test
  void pollerShouldPollUntilTaskPredicateMatches() {
    val counter = new AtomicInteger();
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> {
          val task = readyTask("Task/" + counter.incrementAndGet());
          return interaction(task, ErxTask.class);
        };

    val actualInteraction =
        getPollBuilder(5, 0).poll(supplier, task -> "Task/3".equals(task.getId()));

    assertEquals("Task/3", actualInteraction.getExpectedResponse().getId());
    assertEquals(3, counter.get());
  }

  @Test
  void pollerShouldPollQueuedSupplierUntilAuditEventBundlePredicateMatches() {
    val firstBundle = new ErxAuditEventBundle();
    val secondBundle = new ErxAuditEventBundle();
    firstBundle.setId("Bundle/audit-events-empty");
    secondBundle.setId("Bundle/audit-events-found");
    val interactions =
        new ArrayDeque<>(
            List.of(
                interaction(firstBundle, ErxAuditEventBundle.class),
                interaction(secondBundle, ErxAuditEventBundle.class)));

    val actualInteraction =
        getPollBuilder(4, 0).poll(interactions::remove, bundle -> bundle.getId().endsWith("found"));

    assertSame(secondBundle, actualInteraction.getExpectedResponse());
    assertTrue(interactions.isEmpty());
  }

  @Test
  void pollerShouldPollSupplierMethodReference() {
    val supplier =
        new CountingTaskSupplier(List.of(draftTask("Task/draft"), readyTask("Task/ready")));

    val actualInteraction =
        getPollBuilder(3, 0).poll(supplier::get, task -> task.getStatus() == Task.TaskStatus.READY);

    assertEquals("Task/ready", actualInteraction.getExpectedResponse().getId());
    assertEquals(2, supplier.getCalls());
  }

  @Test
  void pollerShouldPollWithConfiguredWaitingTimeBetweenIterations() {
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> interaction(draftTask("Task/draft"), ErxTask.class);
    Predicate<ErxTask> neverMatchingPredicate = task -> false;

    val startedAt = Instant.now();
    val actualInteraction = getPollBuilder(2, 1).poll(supplier, neverMatchingPredicate);
    val elapsedMillis = Duration.between(startedAt, Instant.now()).toMillis();

    assertEquals("Task/draft", actualInteraction.getExpectedResponse().getId());
    assertTrue(elapsedMillis >= 2);
  }

  @Test
  void pollerShouldNotPollAfterPredicateMatched() {
    val calls = new AtomicInteger();
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> {
          calls.incrementAndGet();
          return interaction(readyTask("Task/ready"), ErxTask.class);
        };

    val actualInteraction =
        getPollBuilder(5, 0).poll(supplier, task -> task.getStatus() == Task.TaskStatus.READY);

    assertEquals("Task/ready", actualInteraction.getExpectedResponse().getId());
    assertEquals(1, calls.get());
  }

  @Test
  void pollerShouldNotReturnInteractionWhenPredicateNeverMatches() {
    val calls = new AtomicInteger();
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> {
          calls.incrementAndGet();
          return interaction(draftTask("Task/draft-" + calls.get()), ErxTask.class);
        };

    val actualInteraction =
        getPollBuilder(3, 0).poll(supplier, task -> task.getStatus() == Task.TaskStatus.READY);

    assertEquals("Task/draft-3", actualInteraction.getExpectedResponse().getId());
    assertEquals(3, calls.get());
  }

  @Test
  void pollerShouldNotReturnUnexpectedInteractionType() {
    val calls = new AtomicInteger();
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> {
          calls.incrementAndGet();
          return interaction(createOperationOutcome(), ErxTask.class);
        };

    val actualInteraction = getPollBuilder(3, 0).poll(supplier);

    assertEquals(3, calls.get());
    assertTrue(actualInteraction.getResponse().isOperationOutcome());
  }

  @Test
  void pollerShouldNotInvokePredicateForUnexpectedInteractionType() {
    val predicateCalls = new AtomicInteger();
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> interaction(createOperationOutcome(), ErxTask.class);

    getPollBuilder(2, 0)
        .poll(
            supplier,
            task -> {
              predicateCalls.incrementAndGet();
              return true;
            });

    assertEquals(0, predicateCalls.get());
  }

  @Test
  void pollerShouldThrowRuntimeExceptionWhenPollingIsInterrupted() {
    Supplier<ErpInteraction<ErxTask>> supplier =
        () -> interaction(draftTask("Task/draft"), ErxTask.class);

    Thread.currentThread().interrupt();
    val pollingBuilder = getPollBuilder(2, 10);
    try {
      val exception =
          assertThrows(
              RuntimeException.class,
              () ->
                  pollingBuilder.poll(supplier, task -> task.getStatus() == Task.TaskStatus.READY));

      assertEquals("Polling interrupted", exception.getMessage());
      assertInstanceOf(InterruptedException.class, exception.getCause());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  private static ErpInteractionPoller getPollBuilder(int maxRetries, int waitTimeInMilliSeconds) {
    return ErpInteractionPoller.builder()
        .maxRetries(maxRetries)
        .waitTimeInMilliSeconds(waitTimeInMilliSeconds)
        .build();
  }

  private static ErxTask readyTask(String id) {
    val task = new ErxTask();
    task.setId(id);
    task.setStatus(Task.TaskStatus.READY);
    return task;
  }

  private static ErxTask draftTask(String id) {
    val task = new ErxTask();
    task.setId(id);
    task.setStatus(Task.TaskStatus.DRAFT);
    return task;
  }

  private static <R extends Resource> ErpInteraction<R> interaction(
      Resource resource, Class<R> expectedType) {
    return new ErpInteraction<>(
        FhirBResponse.forPayload(expectedType, resource)
            .withStatusCode(200)
            .andValidationResult(createEmptyValidationResult()));
  }

  private static class CountingTaskSupplier {

    private final Queue<ErxTask> tasks;
    private int calls;

    private CountingTaskSupplier(List<ErxTask> tasks) {
      this.tasks = new ArrayDeque<>(tasks);
    }

    private ErpInteraction<ErxTask> get() {
      calls++;
      return interaction(tasks.remove(), ErxTask.class);
    }

    private int getCalls() {
      return calls;
    }
  }
}
