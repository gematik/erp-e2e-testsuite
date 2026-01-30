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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionCommandNew;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBundleBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationPZNBuilderORIGINAL_BUILDER;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfDispenseMedicationNew extends FhirResponseQuestion<EmptyResource> {

  private final Actor patient;
  private final DequeStrategy dequeue;
  private final List<Map<String, String>> medicationDspMapList;
  private final int dispenseIterations;

  private ResponseOfDispenseMedicationNew(
      Actor patient,
      DequeStrategy dequeue,
      List<Map<String, String>> medications,
      int dispenseIterations) {
    this.patient = patient;
    this.dequeue = dequeue;
    this.medicationDspMapList = medications;
    this.dispenseIterations = dispenseIterations;
  }

  public static Builder fromStackForPatient(DequeStrategy dequeue, Actor patient) {
    return new Builder(dequeue, patient);
  }

  public static Builder fromStackForPatient(String dequeue, Actor patient) {
    return fromStackForPatient(DequeStrategy.fromString(dequeue), patient);
  }

  @Override
  public ErpResponse<EmptyResource> answeredBy(Actor actor) {

    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val patientDispenseInformation = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);

    val acceptBundle = dequeue.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();

    ErpResponse<EmptyResource> response = null;

    for (int i = 1; i <= dispenseIterations; i++) {

      GemDispenseOperationParameters dispenseParameters = null;

      if (medicationDspMapList.isEmpty()) {
        val kbvAsString = LocalVerifier.parse(acceptBundle.getSignedKbvBundle()).getDocument();
        val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);
        dispenseParameters = convertKbvBundle(kbvBundle, smcb.getTelematikID());
      } else {
        dispenseParameters =
            getAlternativeDispenseParams(
                medicationDspMapList, PrescriptionId.from(taskId), smcb.getTelematikID(), patient);
      }

      response =
          erpClient.request(new DispensePrescriptionCommandNew(taskId, secret, dispenseParameters));

      if (response.isOperationOutcome()) {
        return response;
      }

      // EmptyRessource response does not contain a timestamp
      val dispTime = Instant.now();
      patientDispenseInformation.append(taskId.toPrescriptionId(), dispTime);
      // Extract medication dispense from dispense parameter
      val md =
          dispenseParameters.getParameter(EuPartNaming.RX_DISPENSATION.getCode()).getPart().stream()
              .filter(p -> p.getName().equals("medicationDispense"))
              .map(p -> (ErxMedicationDispense) p.getResource())
              .findFirst()
              .orElseThrow();
      // Add medication dispense to bundle
      val mdBundle = ErxMedicationDispenseBundleBuilder.of(List.of(md)).build();
      // ErxMedicationDispenseBundle generate HL7 bundle; we need medication bundle
      val erxMdBundle = new ErxMedicationDispenseBundle();
      mdBundle.copyValues(erxMdBundle);
      prescriptionManager.getDispensedPrescriptions().append(erxMdBundle);
    }
    return response;
  }

  private List<ErxMedicationDispense> getErxMedDIsp(
      List<Map<String, String>> medicationDspMapList,
      PrescriptionId taskId,
      String performerId,
      Actor patient) {

    val egk = SafeAbility.getAbility(patient, ProvideEGK.class);
    val medicationDispenses = new ArrayList<ErxMedicationDispense>();

    medicationDspMapList.forEach(
        medMap -> {
          val pzn = medMap.getOrDefault("PZN", PZN.random().getValue());
          val name = medMap.getOrDefault("Name", GemFaker.fakerDrugName());
          val amount =
              Long.valueOf(medMap.getOrDefault("Menge", String.valueOf(GemFaker.fakerAmount())));
          val unit = medMap.getOrDefault("Einheit", "Stk");
          val categoryCode =
              medMap.getOrDefault(
                  "Kategorie", GemFaker.fakerValueSet(MedicationCategory.class).getCode());
          val isVaccine = Boolean.parseBoolean(medMap.getOrDefault("Impfung", "false"));
          val darreichungsCode =
              medMap.getOrDefault(
                  "Darreichungsform", GemFaker.fakerValueSet(Darreichungsform.class).getCode());
          val sizeCode = medMap.getOrDefault("Normgröße", StandardSize.random().getCode());

          val medication =
              GemErpMedicationBuilder.forPZN()
                  .pzn(PZN.from(pzn), name)
                  .amount(amount, unit)
                  .category(EpaDrugCategory.fromCode(categoryCode))
                  .isVaccine(isVaccine)
                  .darreichungsform(Darreichungsform.fromCode(darreichungsCode))
                  .normgroesse(StandardSize.fromCode(sizeCode))
                  .build();

          val medicationDispense =
              ErxMedicationDispenseBuilder.forKvnr(egk.getKvnr())
                  .prescriptionId(taskId)
                  .performerId(performerId)
                  .medication(medication)
                  .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                  .wasSubstituted(true)
                  .build();

          medicationDispenses.add(medicationDispense);
        });

    return medicationDispenses;
  }

  private GemDispenseOperationParameters convertKbvBundle(
      KbvErpBundle kbvBundle, String telematikID) {

    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();

    val medication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvBundle.getMedication())
            .lotNumber(GemFaker.fakerLotNumber())
            .build();

    val medDisp =
        ErxMedicationDispenseBuilder.forKvnr(kbvBundle.getPatient().getKvnr())
            .prescriptionId(kbvBundle.getPrescriptionId())
            .performerId(telematikID)
            .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .wasSubstituted(false)
            .medication(medication)
            .build();

    return paramsBuilder.with(medDisp, medication).build();
  }

  private GemDispenseOperationParameters getAlternativeDispenseParams(
      List<Map<String, String>> medicationDspMapList,
      PrescriptionId taskId,
      String performerId,
      Actor patient) {

    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();

    medicationDspMapList.forEach(
        medMap -> {
          val pzn = medMap.getOrDefault("PZN", PZN.random().getValue());
          val name = medMap.getOrDefault("Name", GemFaker.fakerDrugName());
          val amount =
              Long.valueOf(medMap.getOrDefault("Menge", String.valueOf(GemFaker.fakerAmount())));
          val unit = medMap.getOrDefault("Einheit", "Stk");
          val categoryCode =
              medMap.getOrDefault(
                  "Kategorie", GemFaker.fakerValueSet(MedicationCategory.class).getCode());
          val isVaccine = Boolean.parseBoolean(medMap.getOrDefault("Impfung", "false"));
          val darreichungsCode =
              medMap.getOrDefault(
                  "Darreichungsform", GemFaker.fakerValueSet(Darreichungsform.class).getCode());
          val sizeCode = medMap.getOrDefault("Normgröße", StandardSize.random().getCode());

          val medication =
              GemErpMedicationBuilder.forPZN()
                  .pzn(PZN.from(pzn), name)
                  .amount(amount, unit)
                  .category(EpaDrugCategory.fromCode(categoryCode))
                  .isVaccine(isVaccine)
                  .darreichungsform(Darreichungsform.fromCode(darreichungsCode))
                  .normgroesse(StandardSize.fromCode(sizeCode))
                  .build();

          val egk = SafeAbility.getAbility(patient, ProvideEGK.class);

          val medicationDisp =
              ErxMedicationDispenseBuilder.forKvnr(egk.getKvnr())
                  .prescriptionId(taskId)
                  .performerId(performerId)
                  .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                  .whenPrepared(new Date())
                  .whenHandedOver(new Date())
                  .wasSubstituted(true)
                  .medication(medication)
                  .build();

          paramsBuilder.with(medicationDisp, medication);
        });

    return paramsBuilder.build();
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
    private List<Map<String, String>> medications = List.of();

    public ResponseOfDispenseMedicationNew build() {
      return new ResponseOfDispenseMedicationNew(patient, dequeue, medications, dispenseIterations);
    }
  }
}
