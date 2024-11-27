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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.*;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.PZN;
import java.util.List;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("DispenseMedication bei zeitnaher Bereitstellung")
@Tag("MedicationDispenseTimelyManner")
public class DispenseMedicationTimelyManner extends ErpTest {

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
  public void shouldDownloadMedicDispenseWithAllInformationAfterClose() {

    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    final List<ErxMedicationDispense> medDisList = getErxMedicationDispenses(task);

    val dispensation =
        pharmacy.performs(
            DispensePrescription.forPrescription(acceptation.getTaskId(), acceptation.getSecret())
                .withMedDsp(medDisList));

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
            .and(verifyContainedMedicationDispensePZNs(medDisList))
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

    val medDisList = getErxMedicationDispenses(task);

    val dispensation =
        pharmacy.performs(
            DispensePrescription.forPrescription(acceptation.getTaskId(), acceptation.getSecret())
                .withMedDsp(medDisList));

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
            .and(verifyContainedMedicationDispensePZNs(medDisList))
            .isCorrect());
    //
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
}
