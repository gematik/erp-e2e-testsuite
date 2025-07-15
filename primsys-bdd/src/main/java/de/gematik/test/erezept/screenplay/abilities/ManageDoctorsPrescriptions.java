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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.bbriccs.fhir.codec.OperationOutcomeExtractor;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.HasTeardown;
import net.serenitybdd.screenplay.RefersToActor;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * This ability is intended to be used by the Doctor-Actor for managing it's issued Prescriptions
 */
@Slf4j
public class ManageDoctorsPrescriptions implements Ability, HasTeardown, RefersToActor {

  @Getter @Delegate private final ManagedList<ErxTask> prescriptions;
  private Actor actor;

  private ManageDoctorsPrescriptions() {
    this.prescriptions = new ManagedList<>(() -> "No Prescriptions were issued so far");
  }

  public static ManageDoctorsPrescriptions sheIssued() {
    return heIssued();
  }

  public static ManageDoctorsPrescriptions heIssued() {
    return new ManageDoctorsPrescriptions();
  }

  @Override
  public String toString() {
    return "E-Rezepte Manager für die Verwaltung von ausgestellten E-Rezepten";
  }

  @Override
  public void tearDown() {
    log.info("TearDown ManageDoctorsPrescriptions Ability for {}", this.actor.getName());
    val erpClientAbility = this.actor.abilityTo(UseTheErpClient.class);

    // null-check required because having this ability does not necessarily mean that ErpClient is
    // also available
    if (erpClientAbility != null) {
      // use the erpClient directly to avoid reporting of the teardowns
      val erpClient = erpClientAbility.getClient();
      this.prescriptions
          .getRawList()
          .forEach(
              task -> {
                val response =
                    erpClient.request(new TaskAbortCommand(task.getTaskId(), task.getAccessCode()));
                response
                    .getResourceOptional(OperationOutcome.class)
                    .map(OperationOutcomeExtractor::extractFrom)
                    .ifPresent(
                        errorMessage ->
                            log.info(
                                "Received OperationOutcome with HTTP Statuscode {} while trying to"
                                    + " abort Prescription {}: \n"
                                    + "{}",
                                response.getStatusCode(),
                                task.getTaskId(),
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
