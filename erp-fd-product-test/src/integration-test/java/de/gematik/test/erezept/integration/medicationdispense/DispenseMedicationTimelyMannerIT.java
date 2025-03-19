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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.containsAllPZNsForNewProfiles;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.containsAllPZNsForOldProfiles;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.verifyCountOfContainedMedication;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleHasLastMedicationDispenseDate;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.List;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("DispenseMedication bei zeitnaher Bereitstellung")
@Tag("MedicationDispenseTimelyManner")
public class DispenseMedicationTimelyMannerIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @TestcaseId("ERP_DISPENSE_MEDICATION_PROMPT_01")
  @Test
  @DisplayName(
      "Prüfe, dass ein Patient beim Abruf seiner MedicationDispense nach einer"
          + " Mehrfachdispensierung und einem Close ohne Dispensierung alle informationen der"
          + " Dispensierung abrufen kann")
  public void shouldDownloadMedicDispenseWithAllInformationAfterDispense() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      shouldDownloadMedicDispenseWithAllInformationAfterDispenseForOldProfiles(acceptation);
    } else {
      shouldDownloadMedicDispenseWithAllInformationAfterDispenseForNewProfiles(acceptation);
    }
  }

  private void shouldDownloadMedicDispenseWithAllInformationAfterDispenseForOldProfiles(
      ErxAcceptBundle acceptation) {
    val task = acceptation.getTask();
    val expectedMedicationDispenses = getErxMedicationDispenses(task);
    val dispensation =
        pharmacy.performs(
            DispensePrescription.withCredentials(acceptation.getTaskId(), acceptation.getSecret())
                .withMedDsp(expectedMedicationDispenses));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptation.getSecret()));
    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    val medDisp =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .identifier(task.getPrescriptionId().asIdentifier())
                    .createParameter()));

    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyCountOfContainedMedication(2))
            .and(containsAllPZNsForOldProfiles(expectedMedicationDispenses))
            .isCorrect());
  }

  private void shouldDownloadMedicDispenseWithAllInformationAfterDispenseForNewProfiles(
      ErxAcceptBundle acceptation) {
    val task = acceptation.getTask();
    val expectedMedicationDispenses = getErxMedicationDispensesForNewProfiles(task);
    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();

    expectedMedicationDispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));

    val params = paramsBuilder.build();

    val dispensation =
        pharmacy.performs(
            DispensePrescription.withCredentials(acceptation.getTaskId(), acceptation.getSecret())
                .withParameters(params));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    val prescriptionBundle =
        patient.performs(GetPrescriptionById.withTaskId(task.getTaskId()).withoutAuthentication());

    patient.attemptsTo(
        Verify.that(prescriptionBundle)
            .withExpectedType()
            .has(bundleHasLastMedicationDispenseDate())
            .isCorrect());

    val medDisBeforeClose =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .identifier(task.getPrescriptionId().asIdentifier())
                    .createParameter()));

    patient.attemptsTo(
        Verify.that(medDisBeforeClose)
            .withExpectedType()
            .and(verifyCountOfContainedMedication(2))
            .and(
                containsAllPZNsForNewProfiles(
                    expectedMedicationDispenses.stream().map(Pair::getRight).toList()))
            .isCorrect());

    val closeResponse =
        pharmacy.performs(
            ClosePrescriptionWithoutDispensation.forTheTask(task, acceptation.getSecret()));

    pharmacy.attemptsTo(Verify.that(closeResponse).withExpectedType().isCorrect());

    val medDisAfterClose =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .identifier(task.getPrescriptionId().asIdentifier())
                    .createParameter()));

    patient.attemptsTo(
        Verify.that(medDisAfterClose)
            .withExpectedType()
            .and(verifyCountOfContainedMedication(2))
            .and(
                containsAllPZNsForNewProfiles(
                    expectedMedicationDispenses.stream().map(Pair::getRight).toList()))
            .isCorrect());
  }

  @TestcaseId("ERP_DISPENSE_MEDICATION_PROMPT_02")
  @Test
  @DisplayName(
      "Prüfe, dass ein Patient beim Abruf seiner MedicationDispense nach einer"
          + " Mehrfachdispensierung vor einem Close alle Informationen der Dispensierung vorliegen")
  public void shouldDownloadMedicDispenseWithAllInformation() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      shouldDownloadMedicDispenseWithAllInformationForOldProfiles(acceptation);
    } else {
      shouldDownloadMedicDispenseWithAllInformationForNewProfiles(acceptation);
    }
  }

  private void shouldDownloadMedicDispenseWithAllInformationForOldProfiles(
      ErxAcceptBundle acceptation) {
    val task = acceptation.getTask();
    val expectedMedicationDispenses = getErxMedicationDispenses(task);

    val dispensation =
        pharmacy.performs(
            DispensePrescription.withCredentials(acceptation.getTaskId(), acceptation.getSecret())
                .withMedDsp(expectedMedicationDispenses));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    val medDisBeforeClose =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .identifier(task.getPrescriptionId().asIdentifier())
                    .createParameter()));

    patient.attemptsTo(
        Verify.that(medDisBeforeClose)
            .withExpectedType()
            .and(verifyCountOfContainedMedication(2))
            .and(containsAllPZNsForOldProfiles(expectedMedicationDispenses))
            .isCorrect());

    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptation.getSecret()));
  }

  private void shouldDownloadMedicDispenseWithAllInformationForNewProfiles(
      ErxAcceptBundle acceptation) {
    val task = acceptation.getTask();
    val expectedMedicationDispenses = getErxMedicationDispensesForNewProfiles(task);
    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();
    expectedMedicationDispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));
    val params = paramsBuilder.build();

    val dispensation =
        pharmacy.performs(
            DispensePrescription.withCredentials(acceptation.getTaskId(), acceptation.getSecret())
                .withParameters(params));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    val medDisBeforeClose =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .identifier(task.getPrescriptionId().asIdentifier())
                    .createParameter()));

    patient.attemptsTo(
        Verify.that(medDisBeforeClose)
            .withExpectedType()
            .and(verifyCountOfContainedMedication(2))
            .and(
                containsAllPZNsForNewProfiles(
                    expectedMedicationDispenses.stream().map(Pair::getRight).toList()))
            .isCorrect());

    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptation.getSecret()));
  }

  @NotNull
  private List<ErxMedicationDispense> getErxMedicationDispenses(ErxTask task) {

    val medication1 =
        KbvErpMedicationPZNFaker.builder()
            .withAmount(666)
            .withPznMedication(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();
    val medication2 =
        KbvErpMedicationPZNFaker.builder()
            .withAmount(666)
            .withPznMedication(PZN.from("17377602"), "Spikevax von Moderna")
            .fake();

    val medDisp =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication1)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();
    val medDisp2 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication2)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();

    return List.of(medDisp, medDisp2);
  }

  private List<Pair<ErxMedicationDispense, GemErpMedication>>
      getErxMedicationDispensesForNewProfiles(ErxTask task) {

    val medication1 =
        GemErpMedicationFaker.builder()
            .withAmount(666)
            .withPzn(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();
    val medication2 =
        GemErpMedicationFaker.builder()
            .withAmount(666)
            .withPzn(PZN.from("17377602"), "Spikevax von Moderna")
            .fake();

    val medDisp1 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication1)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();
    val medDisp2 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication2)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();

    return List.of(Pair.of(medDisp1, medication1), Pair.of(medDisp2, medication2));
  }
}
