/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.fdv.task;

import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.questions.DownloadTaskById;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HasAllAuditEvents implements Question<Boolean> {

  private final DequeStrategy order;

  public static Builder forPrescription(DequeStrategy order) {
    return new Builder(order);
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  @Override
  @Step("{0} fragt beim Fachdienst, nach Protokolleinträgen zu einer EVDGA Verordnung")
  public Boolean answeredBy(Actor actor) {
    val ability = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val managedStack = ability.chooseStack(DmcStack.ACTIVE);
    val taskId = order.chooseFrom(managedStack).getTaskId();

    // receive audit events
    val auditEvents =
        actor
            .asksFor(GetAuditEventBundle.forPrescription(PrescriptionId.from(taskId)))
            .getExpectedResource()
            .getAuditEvents();

    // get Task to evaluate correct contained audit events
    val task = actor.asksFor(DownloadTaskById.withId(taskId)).getTask();

    val searchedAuditEvents = createExpectedAuditEventMessagesFor(task);

    val res =
        searchedAuditEvents.stream()
            .map(
                searchedElem ->
                    auditEvents.stream().anyMatch(aE -> aE.getFirstText().contains(searchedElem)))
            .toList();
    return res.stream().noneMatch(entry -> entry.equals(false));
  }

  private List<String> createExpectedAuditEventMessagesFor(ErxTask task) {
    val searchedAuditEvents = new ArrayList<String>();
    val prescriptionId = task.getPrescriptionId().getValue();
    switch (task.getStatus()) {
      case READY -> {
        searchedAuditEvents.add("activated a prescription " + prescriptionId);
      }
      case INPROGRESS -> {
        searchedAuditEvents.addAll(
            List.of(
                "activated a prescription " + prescriptionId,
                "accepted a prescription " + prescriptionId));
      }
      case COMPLETED -> {
        searchedAuditEvents.addAll(
            List.of(
                "activated a prescription " + prescriptionId,
                "accepted a prescription " + prescriptionId,
                "closed a prescription " + prescriptionId));
      }
        // in case of multiple possibilities for the moment the prescription became cancelled here
        // is only one case checked
      case CANCELLED -> {
        searchedAuditEvents.add("cancelled a prescription " + prescriptionId);
      }
      default -> log.info(
          "Possible Task.Status are READY, INPROGRESS, COMPLETED, CANCELLED, but was: "
              + task.getStatus());
    }
    return searchedAuditEvents;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy order;

    public HasAllAuditEvents build() {
      return new HasAllAuditEvents(order);
    }
  }
}
