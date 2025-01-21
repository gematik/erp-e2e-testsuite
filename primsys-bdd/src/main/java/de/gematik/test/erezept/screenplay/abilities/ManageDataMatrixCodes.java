/*
 * Copyright 2024 gematik GmbH
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
 */

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.HasTeardown;
import net.serenitybdd.screenplay.RefersToActor;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * This ability is intended to be used by the Patient-Actor for managing his
 * (Low-Detail)-Prescriptions which he receives from the doctor
 */
@Slf4j
public class ManageDataMatrixCodes implements Ability, HasTeardown, RefersToActor {

  @Getter private final ManagedList<DmcPrescription> dmcs;
  @Getter private final ManagedList<DmcPrescription> deletedDmcs;
  private Actor actor;

  private ManageDataMatrixCodes() {
    this.dmcs = new ManagedList<>(() -> "No Prescriptions via DataMatrixCode received so far");
    this.deletedDmcs = new ManagedList<>(() -> "No DataMatrixCode were deleted so far");
  }

  public static ManageDataMatrixCodes heGetsPrescribed() {
    return new ManageDataMatrixCodes();
  }

  public static ManageDataMatrixCodes sheGetsPrescribed() {
    // gender-sensitive Java :)
    return heGetsPrescribed();
  }

  public ManagedList<DmcPrescription> chooseStack(DmcStack stack) {
    return switch (stack) {
      case ACTIVE -> dmcs;
      case DELETED -> deletedDmcs;
    };
  }

  public void appendDmc(DmcPrescription dmc) {
    log.info(
        "Patient has received a 'paper-based' Prescription on a Data Matrix Code for Task {}",
        dmc.getTaskId());
    this.dmcs.append(dmc);
  }

  public void moveToDeleted(DmcPrescription dmc) {
    log.info(
        "Patient has deleted a 'paper-based' Prescription on a Data Matrix Code for Task {}",
        dmc.getTaskId());
    this.dmcs.getRawList().remove(dmc);
    this.deletedDmcs.getRawList().remove(dmc); // make sure the DMC does not appear twice
    this.deletedDmcs.append(dmc);
  }

  public List<DmcPrescription> getAggregatedDmcs() {
    val aggregatedDmcs = new HashSet<>(this.dmcs.getRawList());
    aggregatedDmcs.addAll(this.deletedDmcs.getRawList());
    return aggregatedDmcs.stream()
        .sorted(Comparator.comparing(DmcPrescription::getCreated))
        .toList();
  }

  public List<DmcPrescription> getDmcList() {
    return this.dmcs.getRawList();
  }

  @Override
  public String toString() {
    return "DataMatrixCode Manager für die Verwaltung von verschriebenen E-Rezepten auf Papier";
  }

  @Override
  public void tearDown() {
    log.info("TearDown ManageDataMatrixCodes Ability for {}", this.actor.getName());
    val erpClientAbility = this.actor.abilityTo(UseTheErpClient.class);

    // null-check required because having this ability does not necessarily mean that ErpClient is
    // also available
    if (erpClientAbility != null) {
      // use the erpClient directly to avoid reporting of the teardowns
      val erpClient = erpClientAbility.getClient();
      this.getDmcList()
          .forEach(
              dmc -> {
                val response = erpClient.request(new TaskAbortCommand(dmc.getTaskId()));
                response
                    .getResourceOptional(OperationOutcome.class)
                    .map(OperationOutcomeWrapper::extractFrom)
                    .ifPresent(
                        errorMessage ->
                            log.info(
                                "Received OperationOutcome with HTTP Statuscode {} while trying to"
                                    + " abort Prescription {}: \n"
                                    + "{}",
                                response.getStatusCode(),
                                dmc.getTaskId(),
                                errorMessage));
              });
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Ability> T asActor(Actor actor) {
    this.actor = actor;
    return (T) this;
  }
}
