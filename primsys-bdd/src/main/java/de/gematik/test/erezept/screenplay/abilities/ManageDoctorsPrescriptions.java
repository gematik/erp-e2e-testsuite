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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;
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
    log.info(format("TearDown ManageDoctorsPrescriptions Ability for {0}", this.actor.getName()));
    val erpClient = this.actor.abilityTo(UseTheErpClient.class);

    // null-check required because in App-Testsuite an actor does not necessarily have an ErpClient
    if (erpClient != null) {
      this.prescriptions
          .getRawList()
          .forEach(
              task -> {
                val response =
                    erpClient.request(new TaskAbortCommand(task.getTaskId(), task.getAccessCode()));
                response
                    .getResourceOptional(OperationOutcome.class)
                    .map(OperationOutcomeWrapper::extractFrom)
                    .ifPresent(
                        errorMessage ->
                            log.warn(
                                format(
                                    "Received OperationOutcome with HTTP Statuscode {0} while"
                                        + " trying to abort Prescription {1}: \n"
                                        + "{2}",
                                    response.getStatusCode(), task.getTaskId(), errorMessage)));
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
