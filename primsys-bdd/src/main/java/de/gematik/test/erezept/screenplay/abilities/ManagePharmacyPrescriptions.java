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
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
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
 * This ability is intended to be used by the Pharmacy-Actor for managing it's assigned, accepted
 * and dispensed Prescriptions
 */
@Slf4j
public class ManagePharmacyPrescriptions implements Ability, HasTeardown, RefersToActor {

  private Actor actor;

  /**
   * Stores Prescriptions (ErxTasks) which has been physically assigned and have not yet been
   * accepted
   */
  @Getter private final ManagedList<DmcPrescription> assignedPrescriptions;

  /** Stores Prescriptions (ErxAcceptBundles) which has been digitally accepted at the FD */
  @Getter private final ManagedList<ErxAcceptBundle> acceptedPrescriptions;

  /**
   * Stores the Prescriptions (Quittungen/ErxReceipt) which has been dispensed at the FD and closed
   */
  @Getter private final ManagedList<DispenseReceipt> closedPrescriptions;

  /** Stores dispensed Prescriptions (Quittungen/ErxReceipt) which has been dispensed at the FD */
  @Getter private final ManagedList<ErxMedicationDispenseBundle> dispensedPrescriptions;

  /**
   * Stores the AccessCodes/PrescriptionIDs for ChargeItems which the pharmacy was authorized to
   * change
   */
  @Getter private final ManagedList<ChargeItemChangeAuthorization> chargeItemChangeAuthorizations;

  private ManagePharmacyPrescriptions() {
    assignedPrescriptions = new ManagedList<>(() -> "No Prescriptions were assigned so far");
    acceptedPrescriptions = new ManagedList<>(() -> "No Prescriptions were accepted so far");
    closedPrescriptions = new ManagedList<>(() -> "No Prescriptions were closed so far");
    dispensedPrescriptions = new ManagedList<>(() -> "No Prescriptions were dispensed so far");
    chargeItemChangeAuthorizations =
        new ManagedList<>(() -> "No Authorizations for changing ChargeItems were given so far");
  }

  /**
   * Just a handy Instatiator for better wording within the Gluecode
   *
   * @return ManagePharmacyPrescriptions
   */
  public static ManagePharmacyPrescriptions itWorksWith() {
    return new ManagePharmacyPrescriptions();
  }

  public List<DmcPrescription> getAssignedList() {
    return assignedPrescriptions.getRawList();
  }

  public List<ErxAcceptBundle> getAcceptedList() {
    return acceptedPrescriptions.getRawList();
  }

  public List<DispenseReceipt> getReceiptsList() {
    return closedPrescriptions.getRawList();
  }

  /**
   * Append a newly accepted prescription
   *
   * @param accepted is the bundle returned from FD which will also contain the secret for later
   *     access
   */
  public void appendAcceptedPrescription(ErxAcceptBundle accepted) {
    acceptedPrescriptions.append(accepted);
  }

  /**
   * Append a newly assigned prescription
   *
   * @param dmc which is the prescription which was physically assigned via Data Matrix Code
   */
  public void appendAssignedPrescription(DmcPrescription dmc) {
    assignedPrescriptions.append(dmc);
  }

  /**
   * Append a newly dispensed prescription
   *
   * @param receipt which acknowledges a dispensation of a medication
   */
  public void appendDispensedPrescriptions(DispenseReceipt receipt) {
    closedPrescriptions.append(receipt);
  }

  /**
   * LIFO consumption of assigned prescriptions
   *
   * @return the youngest assigned prescription
   * @throws MissingPreconditionError if no Prescriptions have been assigned so far
   */
  public DmcPrescription consumeLastAssignedPrescription() {
    return assignedPrescriptions.consumeLast();
  }

  public DmcPrescription getLastAssignedPrescription() {
    return assignedPrescriptions.getLast();
  }

  /**
   * FIFO consumption of assigned prescriptions
   *
   * @return the oldest assigned prescription
   * @throws MissingPreconditionError if no Prescriptions have been assigned so far
   */
  public DmcPrescription consumeOldestAssignedPrescription() {
    return assignedPrescriptions.consumeFirst();
  }

  public DmcPrescription getOldestAssignedPrescription() {
    return assignedPrescriptions.getFirst();
  }

  public ErxAcceptBundle getLastAcceptedPrescription() {
    return acceptedPrescriptions.getLast();
  }

  @Override
  public String toString() {
    return "E-Rezepte Manager für die Verwaltung von zugewiesenen, akzeptierten und dispensierten"
        + " E-Rezepten";
  }

  @Override
  public void tearDown() {
    log.info(format("TearDown ManagePharmacyPrescription Ability for {0}", this.actor.getName()));
    val erpClient = SafeAbility.getAbility(this.actor, UseTheErpClient.class);
    this.acceptedPrescriptions
        .getRawList()
        .forEach(
            accepted -> {
              log.info(
                  format(
                      "Abort accepted Prescription {0} {1} {2}",
                      accepted.getTaskId(),
                      accepted.getTask().getAccessCode(),
                      accepted.getSecret()));
              val cmd =
                  new TaskAbortCommand(
                      accepted.getTaskId(),
                      accepted.getTask().getAccessCode(),
                      accepted.getSecret());
              val response = erpClient.request(cmd);

              response
                  .getResourceOptional(OperationOutcome.class)
                  .map(OperationOutcomeWrapper::extractFrom)
                  .ifPresent(
                      errorMessage ->
                          log.warn(
                              format(
                                  "Received OperationOutcome with HTTP Statuscode {0} while trying"
                                      + " to abort accepted Prescription {1}: \n"
                                      + "{2}",
                                  response.getStatusCode(), accepted.getTaskId(), errorMessage)));
            });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Ability> T asActor(Actor actor) {
    this.actor = actor;
    return (T) this;
  }
}
