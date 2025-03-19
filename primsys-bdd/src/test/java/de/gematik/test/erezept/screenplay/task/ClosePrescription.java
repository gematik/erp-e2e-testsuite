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
 */

package de.gematik.test.erezept.screenplay.task;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.questions.GetMedicationDispense;
import de.gematik.test.erezept.screenplay.questions.ResponseOfClosePrescriptionOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class ClosePrescription implements Task {

  private final ResponseOfClosePrescriptionOperation fhirResponseQuestion;

  private ClosePrescription(ResponseOfClosePrescriptionOperation fhirResponseQuestion) {
    this.fhirResponseQuestion = fhirResponseQuestion;
  }

  public static DispenseMedicationsBuilder withSecret(String dequeue, String wrongSecret) {
    return withSecret(DequeStrategy.fromString(dequeue), wrongSecret);
  }

  public static DispenseMedicationsBuilder withSecret(DequeStrategy dequeue, String wrongSecret) {
    return new DispenseMedicationsBuilder(
        ResponseOfClosePrescriptionOperation.withSecret(dequeue, wrongSecret));
  }

  public static DispenseMedicationsBuilder toKvnr(String dequeue, String kvnr) {
    return toKvnr(DequeStrategy.fromString(dequeue), KVNR.from(kvnr));
  }

  public static DispenseMedicationsBuilder toKvnr(DequeStrategy dequeue, KVNR kvnr) {
    return new DispenseMedicationsBuilder(
        ResponseOfClosePrescriptionOperation.toKvnr(dequeue, kvnr));
  }

  public static DispenseMedicationsBuilder toPatient(String dequeue, Actor patient) {
    return toPatient(DequeStrategy.fromString(dequeue), patient);
  }

  public static DispenseMedicationsBuilder toPatient(DequeStrategy dequeue, Actor patient) {
    return new DispenseMedicationsBuilder(
        ResponseOfClosePrescriptionOperation.toPatient(dequeue, patient));
  }

  public static DispenseMedicationsBuilder fromStack(String dequeue) {
    return fromStack(DequeStrategy.fromString(dequeue));
  }

  public static DispenseMedicationsBuilder fromStack(DequeStrategy dequeStrategy) {
    return new DispenseMedicationsBuilder(
        ResponseOfClosePrescriptionOperation.fromStack(dequeStrategy));
  }

  private static boolean storeAcceptationInformation = false;
  private static Actor patient;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    try {
      val response = actor.asksFor(fhirResponseQuestion);
      val receipt = response.getExpectedResource();
      val strategy = fhirResponseQuestion.getExecutedStrategy();

      prescriptionManager.appendDispensedPrescriptions(
          new DispenseReceipt(
              strategy.getKvnr(),
              strategy.getTaskId(),
              strategy.getPrescriptionId(),
              strategy.getAccessCode(),
              strategy.getSecret(),
              receipt));

      strategy
          .getPatient()
          .ifPresent(
              patient -> {
                val prescriptionId = receipt.getPrescriptionId();
                val dispensationDate = receipt.getTimestamp().toInstant();
                log.info(
                    "Handout dispensed medication for prescription {} to {}",
                    prescriptionId,
                    patient.getName());
                val drugReceive = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);
                drugReceive.append(prescriptionId, dispensationDate);
              });

      if (patient != null) {
        val medDsp =
            patient.asksFor(GetMedicationDispense.asPatient().forPrescription(DequeStrategy.LIFO));
        prescriptionManager.getDispensedPrescriptions().append(medDsp);
      }
      if (!storeAcceptationInformation) {
        // only if the dispensation was successful teardown the strategy
        strategy.teardown();
      }
    } catch (UnexpectedResponseResourceError urre) {
      val strategy = fhirResponseQuestion.getExecutedStrategy();
      log.warn(
          "Dispensing Prescription {} to {} failed",
          strategy.getPrescriptionId(),
          strategy.getKvnr());
      // re-throw for potential decorators
      throw urre;
    }
  }

  public ClosePrescription andStoreAcceptInformationForLaterStep() {
    storeAcceptationInformation = true;
    return this;
  }

  public ClosePrescription forThePatient(Actor thePatient) {
    patient = thePatient;
    return this;
  }

  public static class DispenseMedicationsBuilder {
    @Delegate
    private final ResponseOfClosePrescriptionOperation.ResponseOfDispenseMedicationOperationBuilder
        builder;

    private DispenseMedicationsBuilder(
        ResponseOfClosePrescriptionOperation.ResponseOfDispenseMedicationOperationBuilder builder) {
      this.builder = builder;
    }

    public ClosePrescription withPrescribedMedications() {
      val question = builder.forPrescribedMedications();
      return new ClosePrescription(question);
    }

    public ClosePrescription withoutMedicationDispense() {
      val question = builder.forAlreadyDispensedMedication();
      return new ClosePrescription(question);
    }

    public ClosePrescription withAlternativeMedications(DataTable medications) {
      return withAlternativeMedications(medications.asMaps());
    }

    public ClosePrescription withAlternativeMedications(List<Map<String, String>> medications) {
      val question = builder.forAlternativeMedications(medications);
      return new ClosePrescription(question);
    }
  }
}
