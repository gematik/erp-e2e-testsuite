/*
 * Copyright (c) 2023 gematik GmbH
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

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.dav.*;
import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.resources.dav.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import io.cucumber.datatable.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
public class DispenseMedication implements Task {

  private final ResponseOfDispenseMedicationOperation fhirResponseQuestion;

  private DispenseMedication(ResponseOfDispenseMedicationOperation fhirResponseQuestion) {
    this.fhirResponseQuestion = fhirResponseQuestion;
  }

  public static DispenseMedicationsBuilder withSecret(String dequeue, String wrongSecret) {
    return withSecret(DequeStrategy.fromString(dequeue), wrongSecret);
  }

  public static DispenseMedicationsBuilder withSecret(DequeStrategy dequeue, String wrongSecret) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.withSecret(dequeue, wrongSecret));
  }

  public static DispenseMedicationsBuilder toKvid(String dequeue, String kvid) {
    return toKvid(DequeStrategy.fromString(dequeue), kvid);
  }

  public static DispenseMedicationsBuilder toKvid(DequeStrategy dequeue, String kvid) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.toKvid(dequeue, kvid));
  }

  public static DispenseMedicationsBuilder toPatient(String dequeue, Actor patient) {
    return toPatient(DequeStrategy.fromString(dequeue), patient);
  }

  public static DispenseMedicationsBuilder toPatient(DequeStrategy dequeue, Actor patient) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.toPatient(dequeue, patient));
  }

  public static DispenseMedicationsBuilder fromStack(String dequeue) {
    return fromStack(DequeStrategy.fromString(dequeue));
  }

  public static DispenseMedicationsBuilder fromStack(DequeStrategy dequeStrategy) {
    return new DispenseMedicationsBuilder(
        ResponseOfDispenseMedicationOperation.fromStack(dequeStrategy));
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektorAbility = SafeAbility.getAbility(actor, UseTheKonnektor.class);
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
              strategy.getAccessCode(),
              strategy.getSecret(),
              receipt));

      /*if (kbvBundle.getInsuranceType() == VersicherungsArtDeBasis.PKV && strategy.hasConsent()) {
        this.createChargeItem(strategy, erpClientAbility, konnektorAbility, smcb.getTelematikID());
      }*/

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
      PrescriptionToDispenseStrategy strategy,
      UseTheErpClient client,
      UseTheKonnektor konnektor,
      String telematikId) {

    // use a random faked DavBundle for now
    val davBundle = DavAbgabedatenBuilder.faker(strategy.getPrescriptionId()).build();
    Function<DavAbgabedatenBundle, byte[]> signer =
        (b) -> {
          val encoded = client.encode(b, EncodingType.XML);
          return konnektor.signDocumentWithHba(encoded).getPayload();
        };

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(strategy.getPrescriptionId())
            .enterer(telematikId)
            .subject(strategy.getKvid(), GemFaker.insuranceName())
            .verordnung(strategy.getKbvBundleId())
            .abgabedatensatz(davBundle, signer)
            .build();
    val cmd = new ChargeItemPostCommand(chargeItem, strategy.getSecret());
    val response = client.request(cmd);
    return response.getResource(cmd.expectedResponseBody());
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
