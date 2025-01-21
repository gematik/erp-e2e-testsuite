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
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.resources.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.datatable.DataTable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfDispenseMedicationAsBundle
    extends FhirResponseQuestion<ErxMedicationDispenseBundle> {

  private final Actor patient;
  private final DequeStrategy dequeue;
  private final List<Map<String, String>> medicationDspMapList;

  private final int dispenseIterations;

  private ResponseOfDispenseMedicationAsBundle(
      Actor patient,
      DequeStrategy dequeue,
      List<Map<String, String>> medications,
      int dispenseIterations) {
    super("Task/$dispense");
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
  public ErpResponse<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val patientDispenseInformation = SafeAbility.getAbility(patient, ReceiveDispensedDrugs.class);
    val acceptBundle = dequeue.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();

    ErpResponse<ErxMedicationDispenseBundle> resp = null;
    List<ErxMedicationDispense> medicationDispense = null;
    GemDispenseOperationParameters gemDispenseOperationParameters = null;

    for (int x = 1; x <= dispenseIterations; x++) {
      val client = SafeAbility.getAbility(actor, UseTheErpClient.class);
      if (medicationDspMapList == null || medicationDspMapList.isEmpty()) {
        val kbvAsString = acceptBundle.getKbvBundleAsString();
        val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);

        if (isOldProfile()) {
          medicationDispense =
              List.of(this.dispensePrescribedMedication(kbvBundle, smcb.getTelematikID()));

        } else {
          gemDispenseOperationParameters = convertKbvBundle(kbvBundle, smcb.getTelematikID());
        }
      } else {
        if (isOldProfile()) {

          medicationDispense =
              getErxMedDIsp(
                  medicationDspMapList,
                  PrescriptionId.from(taskId),
                  smcb.getTelematikID(),
                  patient);
        } else {
          gemDispenseOperationParameters =
              getAlternativeDispenseParams(
                  medicationDspMapList,
                  PrescriptionId.from(taskId),
                  smcb.getTelematikID(),
                  patient);
        }
      }
      resp =
          isOldProfile()
              ? client.request(
                  new DispensePrescriptionAsBundleCommand(taskId, secret, medicationDispense))
              : client.request(
                  new DispensePrescriptionAsBundleCommand(
                      taskId, secret, gemDispenseOperationParameters));

      if (resp.isOperationOutcome()) return resp;

      val dispensationTime = resp.getExpectedResource().getTimestamp().toInstant();
      prescriptionManager.getDispensedPrescriptions().append(resp.getExpectedResource());
      patientDispenseInformation.append(taskId.toPrescriptionId(), dispensationTime);
    }
    return resp;
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
          val isVaccine = Boolean.getBoolean(medMap.getOrDefault("Impfung", "false"));
          val darreichungsCode =
              medMap.getOrDefault(
                  "Darreichungsform", GemFaker.fakerValueSet(Darreichungsform.class).getCode());
          val sizeCode =
              medMap.getOrDefault(
                  "Normgröße", GemFaker.fakerValueSet(StandardSize.class).getCode());

          val medication =
              KbvErpMedicationPZNBuilder.builder()
                  .pzn(pzn, name)
                  .amount(amount, unit)
                  .category(MedicationCategory.fromCode(categoryCode))
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
        GemErpMedicationBuilder.from(kbvBundle.getMedication())
            .lotNumber(GemFaker.fakerLotNumber())
            .build();

    val medDisp =
        ErxMedicationDispenseBuilder.forKvnr(kbvBundle.getPatient().getKvnr())
            .prescriptionId(kbvBundle.getPrescriptionId())
            .performerId(telematikID)
            .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .medication(medication)
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
          val isVaccine = Boolean.getBoolean(medMap.getOrDefault("Impfung", "false"));
          val darreichungsCode =
              medMap.getOrDefault(
                  "Darreichungsform", GemFaker.fakerValueSet(Darreichungsform.class).getCode());
          val sizeCode =
              medMap.getOrDefault(
                  "Normgröße", GemFaker.fakerValueSet(StandardSize.class).getCode());

          val medication =
              GemErpMedicationBuilder.builder()
                  .pzn(pzn, name)
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

  /**
   * the Method compares the given ErpWorkflowVersion with ErpWorkflowVersion.V1_3_0 and returns
   * true if it is lower or equals Version 1.3.0
   *
   * @return true while oldProfiles in use
   */
  private boolean isOldProfile() {
    return (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0);
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
    private List<Map<String, String>> medications;

    public ResponseOfDispenseMedicationAsBundle build() {
      return new ResponseOfDispenseMedicationAsBundle(
          patient, dequeue, medications, dispenseIterations);
    }

    public Builder multiple(int dispenseIterations) {
      this.dispenseIterations = dispenseIterations;
      return this;
    }

    public Builder withMedicationDispense(DataTable medication) {

      this.medications = medication.asMaps();
      return this;
    }
  }
}
