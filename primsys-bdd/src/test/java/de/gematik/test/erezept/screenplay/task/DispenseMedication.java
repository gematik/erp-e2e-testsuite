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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.ResponseOfDispenseMedicationOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.strategy.PrescriptionToDispenseStrategy;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class DispenseMedication implements Task {

  private final ResponseOfDispenseMedicationOperation fhirResponseQuestion;

  private DispenseMedication(ResponseOfDispenseMedicationOperation fhirResponseQuestion) {
    this.fhirResponseQuestion = fhirResponseQuestion;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    try {
      val response = actor.asksFor(fhirResponseQuestion);
      val receipt = response.getResource(fhirResponseQuestion.expectedResponseBody());

      val strategy = fhirResponseQuestion.getExecutedStrategy();

      val kbvAsString = strategy.getKbvBundleAsString();
      val kbvBundle = erpClientAbility.decode(KbvErpBundle.class, kbvAsString);

      prescriptionManager.appendDispensedPrescriptions(
          new DispenseReceipt(
              strategy.getKvid(),
              strategy.getTaskId(),
              strategy.getPrescriptionId(),
              strategy.getSecret(),
              strategy.getKvid(),
              receipt));

      if (kbvBundle.getInsuranceType() == VersicherungsArtDeBasis.PKV && strategy.hasConsent()) {
        this.createChargeItem(strategy, erpClientAbility, smcb.getTelematikID());
      }

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
              strategy.getPrescriptionId(), strategy.getKvid()));
      // re-throw for potential decorators
      throw urre;
    }
  }

  private ErxChargeItem createChargeItem(
      PrescriptionToDispenseStrategy strategy, UseTheErpClient client, String telematikId) {
    val chargeItem =
        ErxChargeItemBuilder.forPrescription(strategy.getPrescriptionId())
            .enterer(telematikId)
            .subject(strategy.getKvid())
            .verordnung(strategy.getKbvBundleId())
            .abgabedatensatz(UUID.randomUUID().toString()) // create a random ID for the ChargeItem
            .build();
    val cmd = new ChargeItemPostCommand(chargeItem);
    val response = client.request(cmd);
    return response.getResource(cmd.expectedResponseBody());
  }

  public static DispenseMedicationsBuilder withSecret(String dequeue, String wrongSecret) {
    return withSecret(DequeStrategyEnum.fromString(dequeue), wrongSecret);
  }

  public static DispenseMedicationsBuilder withSecret(
      DequeStrategyEnum dequeue, String wrongSecret) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.withSecret(dequeue, wrongSecret));
  }

  public static DispenseMedicationsBuilder toKvid(String dequeue, String kvid) {
    return toKvid(DequeStrategyEnum.fromString(dequeue), kvid);
  }

  public static DispenseMedicationsBuilder toKvid(DequeStrategyEnum dequeue, String kvid) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.toKvid(dequeue, kvid));
  }

  public static DispenseMedicationsBuilder toPatient(String dequeue, Actor patient) {
    return toPatient(DequeStrategyEnum.fromString(dequeue), patient);
  }

  public static DispenseMedicationsBuilder toPatient(DequeStrategyEnum dequeue, Actor patient) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.toPatient(dequeue, patient));
  }

  public static DispenseMedicationsBuilder fromStack(String dequeue) {
    return fromStack(DequeStrategyEnum.fromString(dequeue));
  }

  public static DispenseMedicationsBuilder fromStack(DequeStrategyEnum dequeStrategy) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.fromStack(dequeStrategy));
  }

  public static class DispenseMedicationsBuilder {
    @Delegate
    private final ResponseOfDispenseMedicationOperation.ResponseOfDispenseMedicationOperationBuilder
        builder;

    private DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.ResponseOfDispenseMedicationOperationBuilder
            builder) {
      this.builder = builder;
    }

    public DispenseMedication withPrescribedMedications() {
      val question = builder.forPrescribedMedications();
      return new DispenseMedication(question);
    }

    public DispenseMedication withAlternativeMedications(DataTable medications) {
      return withAlternativeMedications(medications.asMaps());
    }

    public DispenseMedication withAlternativeMedications(List<Map<String, String>> medications) {
      val question = builder.forAlternativeMedications(medications);
      return new DispenseMedication(question);
    }
  }
}
