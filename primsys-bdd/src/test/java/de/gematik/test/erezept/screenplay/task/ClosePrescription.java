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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import io.cucumber.datatable.*;
import java.util.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

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
                log.info(
                    format(
                        "Handout dispensed medication for prescription {0} to {1}",
                        prescriptionId, patient.getName()));
                val drugReceive = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);
                drugReceive.append(prescriptionId);
              });

      // only if the dispensation was successful teardown the strategy
      strategy.teardown();
    } catch (UnexpectedResponseResourceError urre) {
      val strategy = fhirResponseQuestion.getExecutedStrategy();
      log.warn(
          format(
              "Dispensing Prescription {0} to {1} failed",
              strategy.getPrescriptionId(), strategy.getKvnr()));
      // re-throw for potential decorators
      throw urre;
    }
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
