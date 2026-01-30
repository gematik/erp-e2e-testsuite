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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.containsAllPZNsForNewProfiles;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.verifyCountOfContainedMedication;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleHasLastMedicationDispenseDate;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.PrescriptionServiceVersion;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.*;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.erx.ErxMedicationDispenseManipulatorFactory;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("DispenseMedication bei zeitnaher Bereitstellung")
@Tag("MedicationDispenseTimelyManner")
class DispenseMedicationTimelyMannerIT extends ErpTest {

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
  void shouldDownloadMedicDispenseWithAllInformationAfterDispense() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    shouldDownloadMedicDispenseWithAllInformationAfterDispenseForNewProfiles(acceptation);
  }

  @TestcaseId("ERP_DISPENSE_MEDICATION_PROMPT_02")
  @Test
  @DisplayName(
      "Prüfe, dass ein Patient beim Abruf seiner MedicationDispense nach einer"
          + " Mehrfachdispensierung vor einem Close alle Informationen der Dispensierung vorliegen")
  void shouldDownloadMedicDispenseWithAllInformation() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    shouldDownloadMedicDispenseWithAllInformationForNewProfiles(acceptation);
  }

  @TestcaseId("ERP_DISPENSE_MEDICATION_PROMPT_03")
  @ParameterizedTest(name = "[{index}] -> Eine Apotheke stellt eine Dispensierung mit {0} ")
  @DisplayName(
      "Prüfe, dass der Fachdienst manipulierte MedicationDispense-Daten während der Dispensierung"
          + " ablehnt")
  @MethodSource("disppensationSystemManipulators")
  void shouldManipulateSystemsWhileDispensing(
      NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> manipulator) {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    shouldManipulateAndSendToFD(acceptation, manipulator);
  }

  @TestcaseId("ERP_DISPENSE_MEDICATION_PROMPT_04")
  @Test
  @DisplayName(
      "Prüfe, dass der Fachdienst bei einer Dispensierung für Release 1.21 keine"
          + " Rückgabeinformationen mehr liefert und mit StatusCode 204 antwortet")
  void shouldDispenseWithoutReturnInformations() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    val expectedMedicationDispenses =
        getErxMedicationDispensesForNewProfiles(acceptation.getTask());

    val dispensation =
        pharmacy.performs(getDispenseAction(acceptation, expectedMedicationDispenses));

    // derive version from CapabilityStatement
    val interaction = patient.asksFor(new ResponseOfGetCapabilityStatement());
    ErxCapabilityStatement cs = interaction.getResponse().getExpectedResource();

    PrescriptionServiceVersion currentVersion =
        PrescriptionServiceVersion.from(cs.getSoftwareVersion());

    if (currentVersion.isAtLeast(PrescriptionServiceVersion.V_1_21_0)) {
      pharmacy.attemptsTo(
          Verify.that(dispensation)
              .withExpectedType()
              .hasResponseWith(returnCode(204))
              .isCorrect());
    } else {
      pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());
    }
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
            DispensePrescriptionNew.withCredentials(
                    acceptation.getTaskId(), acceptation.getSecret())
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

  private List<Pair<ErxMedicationDispense, GemErpMedication>>
      getErxMedicationDispensesForNewProfiles(ErxTask task) {

    val medication1 =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(666)
            .withPzn(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();
    val medication2 =
        GemErpMedicationFaker.forPznMedication()
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

  private void shouldManipulateAndSendToFD(
      ErxAcceptBundle acceptation,
      NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> manipulator) {
    val task = acceptation.getTask();
    val manipulatedMedicationDispenses = getManipulatedDispense(task, manipulator);
    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();

    manipulatedMedicationDispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));

    val params = paramsBuilder.build();

    val dispensation =
        pharmacy.performs(
            DispensePrescriptionOld.withCredentials(
                    acceptation.getTaskId(), acceptation.getSecret())
                .withParameters(params));

    pharmacy.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_22927)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  private List<Pair<ErxMedicationDispense, GemErpMedication>> getManipulatedDispense(
      ErxTask task, NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> manipulator) {

    val medication1 =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(666)
            .withPzn(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();

    val medDisp1 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication1)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();

    manipulator.getParameter().accept(medDisp1);

    return List.of(Pair.of(medDisp1, medication1));
  }

  static Stream<Arguments> disppensationSystemManipulators() {
    return ArgumentComposer.composeWith(
            ErxMedicationDispenseManipulatorFactory.getSystemManipulator())
        .create();
  }

  private ErpAction<?> getDispenseAction(
      ErxAcceptBundle acceptation, List<Pair<ErxMedicationDispense, GemErpMedication>> dispenses) {

    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();
    dispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));
    val params = paramsBuilder.build();

    // derive version from CapabilityStatement
    val interaction = patient.asksFor(new ResponseOfGetCapabilityStatement());
    ErxCapabilityStatement cs = interaction.getResponse().getExpectedResource();

    PrescriptionServiceVersion currentVersion =
        PrescriptionServiceVersion.from(cs.getSoftwareVersion());

    if (currentVersion.isAtLeast(PrescriptionServiceVersion.V_1_21_0)) {
      return DispensePrescriptionNew.withCredentials(
              acceptation.getTaskId(), acceptation.getSecret())
          .withParameters(params);
    } else {
      return DispensePrescriptionOld.withCredentials(
              acceptation.getTaskId(), acceptation.getSecret())
          .withParameters(params);
    }
  }

  private void shouldDownloadMedicDispenseWithAllInformationAfterDispenseForNewProfiles(
      ErxAcceptBundle acceptation) {

    val task = acceptation.getTask();
    val expectedMedicationDispenses = getErxMedicationDispensesForNewProfiles(task);

    val dispensation =
        pharmacy.performs(getDispenseAction(acceptation, expectedMedicationDispenses));

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
}
