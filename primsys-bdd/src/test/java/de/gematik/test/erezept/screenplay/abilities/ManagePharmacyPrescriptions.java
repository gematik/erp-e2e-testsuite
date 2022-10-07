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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;

/**
 * This ability is intended to be used by the Pharmacy-Actor for managing it's assigned, accepted
 * and dispensed Prescriptions
 */
@Slf4j
public class ManagePharmacyPrescriptions implements Ability {

  /**
   * Stores Prescriptions (ErxTasks) which has been physically assigned and have not yet been
   * accepted
   */
  @Getter private final ManagedList<DmcPrescription> assignedPrescriptions;

  /** Stores Prescriptions (ErxAcceptBundles) which has been digitally accepted at the FD */
  @Getter private final ManagedList<ErxAcceptBundle> acceptedPrescriptions;

  /** Stores the Prescriptions (Quittungen/ErxReceipt) which has been dispensed at the FD */
  @Getter private final ManagedList<DispenseReceipt> dispensedPrescriptions;

  private ManagePharmacyPrescriptions() {
    assignedPrescriptions =
        new ManagedList<>(new MissingPreconditionError("No Prescriptions were assigned so far"));
    acceptedPrescriptions =
        new ManagedList<>(new MissingPreconditionError("No Prescriptions were accepted so far"));
    dispensedPrescriptions =
        new ManagedList<>(new MissingPreconditionError("No Prescriptions were dispensed so far"));
  }

  public List<DmcPrescription> getAssignedList() {
    return assignedPrescriptions.getRawList();
  }

  public List<ErxAcceptBundle> getAcceptedList() {
    return acceptedPrescriptions.getRawList();
  }

  public List<DispenseReceipt> getReceiptsList() {
    return dispensedPrescriptions.getRawList();
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
    dispensedPrescriptions.append(receipt);
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

  /**
   * LIFO consumption of accepted prescriptions
   *
   * @return the youngest accepted prescription
   * @throws MissingPreconditionError if no Prescriptions have been accepted so far
   */
  public ErxAcceptBundle consumeLastAcceptedPrescription() {
    return acceptedPrescriptions.consumeLast();
  }

  public ErxAcceptBundle getLastAcceptedPrescription() {
    return acceptedPrescriptions.getLast();
  }

  /**
   * FIFO consumption of accepted prescriptions
   *
   * @return the oldest accepted prescription
   * @throws MissingPreconditionError if no Prescriptions have been accepted so far
   */
  public ErxAcceptBundle consumeOldestAcceptedPrescription() {
    return acceptedPrescriptions.consumeFirst();
  }

  public ErxAcceptBundle getOldestAcceptedPrescription() {
    return acceptedPrescriptions.getFirst();
  }

  /**
   * LIFO consumption of dispensed prescriptions
   *
   * @return the youngest dispensed prescription
   * @throws MissingPreconditionError if no Prescriptions have been dispensed so far
   */
  public DispenseReceipt consumeLastDispensedPrescription() {
    return dispensedPrescriptions.consumeLast();
  }

  /**
   * FIFO consumption of dispensed prescriptions
   *
   * @return the oldest dispensed prescription
   * @throws MissingPreconditionError if no Prescriptions have been dispensed so far
   */
  public DispenseReceipt consumeOldestDispensedPrescription() {
    return dispensedPrescriptions.consumeFirst();
  }

  /**
   * Check if at least one Prescription has already been dispensed
   *
   * @return true if at least one Receipt is available and false otherwise
   */
  public boolean hasDispensedPrescriptions() {
    return !dispensedPrescriptions.isEmpty();
  }

  public boolean hasAssignedPrescriptions() {
    return !assignedPrescriptions.isEmpty();
  }

  public boolean hasAcceptedPrescriptions() {
    return !acceptedPrescriptions.isEmpty();
  }

  /**
   * Just a handy Instatiator for better wording within the Gluecode
   *
   * @return ManagePharmacyPrescriptions
   */
  public static ManagePharmacyPrescriptions itWorksWith() {
    return new ManagePharmacyPrescriptions();
  }

  @Override
  public String toString() {
    return "E-Rezepte Manager für die Verwaltung von zugewiesenen, akzeptierten und dispensierten E-Rezepten";
  }
}
