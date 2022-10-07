/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.screenplay.abilities.ManagePatientPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.DownloadAllTasks;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * @deprecated no need to download all Prescriptions and store as fulldetail prescriptions on a
 *     Stack. Rather use the Question DownloadPrescriptions on demand where the ErxTaskBundle is
 *     required
 */
@Slf4j
@Deprecated(forRemoval = true)
public class RetrievePrescriptionFromServer implements Task {
  private final DequeStrategy strategy;

  private RetrievePrescriptionFromServer(DequeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val erpClientAbility = actor.abilityTo(UseTheErpClient.class);
    val managePatientPrescriptions =
        SafeAbility.getAbility(actor, ManagePatientPrescriptions.class);

    val taskBundle = actor.asksFor(DownloadAllTasks.descending());
    val lastPrescription =
        strategy.choose(taskBundle.getTasks(), Comparator.comparing(ErxTask::getAuthoredOn));

    val cmdGetTaskById = new TaskGetByIdCommand(lastPrescription.getUnqualifiedId());
    val fullDetailPrescription =
        erpClientAbility.request(cmdGetTaskById).getResource(ErxPrescriptionBundle.class);

    managePatientPrescriptions.appendFullDetailedPrescription(fullDetailPrescription);

    log.info(
        format(
            "Patient {0} has downloaded Prescription {1}",
            actor.getName(), fullDetailPrescription.getTask().getPrescriptionId()));
  }

  public static RetrievePrescriptionFromServer andChooseWith(String order) {
    return andChooseWith(DequeStrategyEnum.fromString(order));
  }

  public static RetrievePrescriptionFromServer andChooseWith(DequeStrategyEnum dequeue) {
    return new RetrievePrescriptionFromServer(new DequeStrategy(dequeue));
  }
}
