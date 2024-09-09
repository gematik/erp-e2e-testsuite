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

public class ResponseOfDispensePrescriptionAsBundle
    extends FhirResponseQuestion<ErxMedicationDispenseBundle> {

  private final Actor patient;
  private final DequeStrategy dequeue;

  public ResponseOfDispensePrescriptionAsBundle(Actor patient, DequeStrategy dequeue) {
    super("Task/$dispense");
    this.patient = patient;
    this.dequeue = dequeue;
  }

  @Override
  public ErpResponse<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val patientDispenseInformation = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);

    val acceptedPrescriptions = prescriptionManager.getAcceptedPrescriptions();
    val acceptBundle = dequeue.chooseFrom(acceptedPrescriptions);
    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();

    val kbvAsString = acceptBundle.getKbvBundleAsString();
    val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);
    val medicationDispenses = this.dispensePrescribedMedication(kbvBundle, smcb.getTelematikID());

    val client = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val resp =
        client.request(
            new DispensePrescriptionAsBundleCommand(taskId, secret, medicationDispenses));
    val time = resp.getExpectedResource().getTimestamp();
    prescriptionManager.getDispensedPrescriptions().append(resp.getExpectedResource());
    prescriptionManager.getDispenseTimestamps().append(time.toInstant());
    patientDispenseInformation.append(taskId.toPrescriptionId());

    return resp;
  }

  public static Builder fromStack(DequeStrategy dequeue) {
    return new Builder(dequeue);
  }

  public static Builder fromStack(String dequeue) {
    return fromStack(DequeStrategy.fromString(dequeue));
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
    private Actor patient;

    public Builder forPatient(Actor patient) {
      this.patient = patient;
      return this;
    }

    public ResponseOfDispensePrescriptionAsBundle build() {
      return new ResponseOfDispensePrescriptionAsBundle(patient, dequeue);
    }
  }
}
