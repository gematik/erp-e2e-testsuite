/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * This ability is intended to be used by the Patient-Actor for managing his
 * (Low-Detail)-Prescriptions which he receives from the doctor
 */
@Slf4j
public class ManageDataMatrixCodes implements Ability, HasTeardown, RefersToActor {

  private Actor actor;

  @Getter private final ManagedList<DmcPrescription> dmcs;
  @Getter private final ManagedList<DmcPrescription> deletedDmcs;

  private ManageDataMatrixCodes() {
    this.dmcs = new ManagedList<>(() -> "No Prescriptions via DataMatrixCode received so far");
    this.deletedDmcs = new ManagedList<>(() -> "No DataMatrixCode were deleted so far");
  }

  public ManagedList<DmcPrescription> chooseStack(DmcStack stack) {
    return switch (stack) {
      case ACTIVE -> dmcs;
      case DELETED -> deletedDmcs;
    };
  }

  public void appendDmc(DmcPrescription dmc) {
    log.info(
        format(
            "Patient has received a 'paper-based' Prescription on a Data Matrix Code for Task {0}",
            dmc.getTaskId()));
    this.dmcs.append(dmc);
  }

  public DmcPrescription getLastDmc() {
    return this.dmcs.getLast();
  }

  public DmcPrescription consumeLastDmc() {
    return this.dmcs.consumeLast();
  }

  public DmcPrescription getFirstDmc() {
    return this.dmcs.getFirst();
  }

  public DmcPrescription consumeFirstDmc() {
    return this.dmcs.consumeFirst();
  }

  public List<DmcPrescription> getDmcList() {
    return this.dmcs.getRawList();
  }

  @Override
  public String toString() {
    return "DataMatrixCode Manager für die Verwaltung von verschriebenen E-Rezepten auf Papier";
  }

  public static ManageDataMatrixCodes heGetsPrescribed() {
    return new ManageDataMatrixCodes();
  }

  public static ManageDataMatrixCodes sheGetsPrescribed() {
    // gender-sensitive Java :)
    return heGetsPrescribed();
  }

  @Override
  public void tearDown() {
    log.info(format("TearDown ManageDataMatrixCodes Ability for {0}", this.actor.getName()));
    val erpClient = this.actor.abilityTo(UseTheErpClient.class);

    // null-check required because in App-Testsuite an actor does not necessarily have an ErpClient
    if (erpClient != null) {
      this.getDmcList()
          .forEach(
              dmc -> {
                val response = erpClient.request(new TaskAbortCommand(dmc.getTaskId()));
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
                                    response.getStatusCode(), dmc.getTaskId(), errorMessage)));
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
