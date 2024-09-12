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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Date;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfDispenseMedicationAsBundle
    extends FhirResponseQuestion<ErxMedicationDispenseBundle> {

  private final Actor patient;
  private final DequeStrategy dequeue;
  private final ErxMedicationDispense medicationDsp;

  private final int dispenseIterations;

  private ResponseOfDispenseMedicationAsBundle(
      Actor patient,
      DequeStrategy dequeue,
      ErxMedicationDispense medicationDsp,
      int dispenseIterations) {
    super("Task/$dispense");
    this.patient = patient;
    this.dequeue = dequeue;
    this.medicationDsp = medicationDsp;
    this.dispenseIterations = dispenseIterations;
  }

  public static Builder fromStackForPatient(DequeStrategy dequeue, Actor patient) {
    return new Builder(dequeue, patient);
  }

  public static Builder fromStackForPatient(String dequeue, Actor patient) {
    return fromStackForPatient(DequeStrategy.fromString(dequeue), patient);
  }

  @Override
  public ErpResponse<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val patientDispenseInformation = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);

    val acceptBundle = dequeue.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();

    ErpResponse<ErxMedicationDispenseBundle> resp = null;
    ErxMedicationDispense medicationDispense = null;
    for (int x = 1; x <= dispenseIterations; x++) {
      if (medicationDsp == null || medicationDsp.isEmpty()) {
        val kbvAsString = acceptBundle.getKbvBundleAsString();
        val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);
        medicationDispense = this.dispensePrescribedMedication(kbvBundle, smcb.getTelematikID());
      } else {
        medicationDispense = medicationDsp;
      }
      val client = SafeAbility.getAbility(actor, UseTheErpClient.class);
      resp =
          client.request(
              new DispensePrescriptionAsBundleCommand(taskId, secret, medicationDispense));

      if (resp.isOperationOutcome()) return resp;

      val dispensationTime = resp.getExpectedResource().getTimestamp().toInstant();
      prescriptionManager.getDispensedPrescriptions().append(resp.getExpectedResource());
      patientDispenseInformation.append(taskId.toPrescriptionId(), dispensationTime);
    }
    return resp;
  }

  private ErxMedicationDispense dispensePrescribedMedication(
      KbvErpBundle bundle, String telematikId) {

    return ErxMedicationDispenseBuilder.forKvnr(bundle.getPatient().getKvnr())
        .prescriptionId(bundle.getPrescriptionId())
        .performerId(telematikId)
        .whenHandedOver(new Date())
        .medication(bundle.getMedication())
        .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
        .wasSubstituted(false)
        .build();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy dequeue;
    private final Actor patient;
    private int dispenseIterations = 1;
    private ErxMedicationDispense medicationDispense;

    public ResponseOfDispenseMedicationAsBundle build() {
      return new ResponseOfDispenseMedicationAsBundle(
          patient, dequeue, medicationDispense, dispenseIterations);
    }

    public Builder multiple(int dispenseIterations) {
      this.dispenseIterations = dispenseIterations;
      return this;
    }

    public Builder withMedicationDispense(ErxMedicationDispense medication) {
      this.medicationDispense = medication;
      return this;
    }
  }
}
