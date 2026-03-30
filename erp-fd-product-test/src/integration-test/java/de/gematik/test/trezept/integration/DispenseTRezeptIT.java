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

package de.gematik.test.trezept.integration;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.trezept.RetrieveCarbonCopy;
import de.gematik.test.erezept.actions.trezept.VerifyTRegisterCarbonCopy;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Dispense T-Rezept")
@Tag("TRezept")
class DispenseTRezeptIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  private GemaTestActor tRegisterChecker;

  @BeforeEach
  void setup() {
    tRegisterChecker = new GemaTestActor("TRegisterChecker");
    this.config.equipWithTPrescriptionMockClient(tRegisterChecker);
  }

  @Test
  @TestcaseId("ERP_DISPENSE_TREZEPT_01")
  @DisplayName("Die Abgabe eines T-Rezepts erzeugt Digitalen Durchschlag im T-Register")
  void shouldCreateDigitalCarbonCopyAfterSuccessfulClose() {

    val kbvErpBundleFaker =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor
            .performs(
                IssuePrescription.forPatient(sina).asTPrescription(kbvErpBundleFaker.toBuilder()))
            .getExpectedResponse();

    val accept = flughafen.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    val closeInteraction = flughafen.performs(ClosePrescription.acceptedWith(accept));

    flughafen.attemptsTo(
        Verify.that(closeInteraction)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());

    val medDisp =
        sina.performs(GetMedicationDispense.fromPerformer(flughafen.getTelematikId()))
            .getExpectedResponse();
    val logs = tRegisterChecker.asksFor(RetrieveCarbonCopy.forTask(task));

    tRegisterChecker.attemptsTo(
        VerifyTRegisterCarbonCopy.from(logs, task.getPrescriptionId(), medDisp));
  }

  @Test
  @TestcaseId("ERP_DISPENSE_TREZEPT_02")
  @DisplayName("Abgabe eines E-T-Rezepts ohne MedicationReference in Dispense Resource")
  void shouldRejectCloseWithoutMedicationReferenceInDispense() {
    val kbvErpBundleFaker =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor
            .performs(
                IssuePrescription.forPatient(sina).asTPrescription(kbvErpBundleFaker.toBuilder()))
            .getExpectedResponse();

    val accept = flughafen.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> removeMedicationReferenceInDispense =
        NamedEnvelope.of(
            "Remove MedicationReference", medDispense -> medDispense.setMedication(null));

    val closeResponse =
        flughafen.performs(
            ClosePrescription.alternative()
                .withResourceManipulator(removeMedicationReferenceInDispense)
                .acceptedWith(accept));

    flughafen.attemptsTo(
        Verify.that(closeResponse)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "This element is not expected. Expected is one of ("
                        + " {http://hl7.org/fhir}statusReasonCodeableConcept,"
                        + " {http://hl7.org/fhir}statusReasonReference,"
                        + " {http://hl7.org/fhir}category,"
                        + " {http://hl7.org/fhir}medicationCodeableConcept,"
                        + " {http://hl7.org/fhir}medicationReference ).",
                    // hapi validator sagt:
                    // "SingleValidationMessage[col=20,row=10,locationString=Parameters.parameter[0].part[0].resource/*MedicationDispense/67c31982-3851-4565-ab11-999c66f1a4e6*/,message=MedicationDispense.medication[x]: minimum required = 1, but only found 0 (from https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.6.1),Validation_VAL_Profile_Minimum,severity=error]"
                    ErpAfos.A_26002_02))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_DISPENSE_TREZEPT_03")
  @DisplayName(
      "Abgabe  eines E-T-Rezepts ohne MedicationDispense Ressource im Close Objekt"
          + " (GemCloseOperationParameters)")
  void shouldRejectCloseWithoutMedicationDispense() {
    val kbvErpBundleFaker =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor
            .performs(
                IssuePrescription.forPatient(sina).asTPrescription(kbvErpBundleFaker.toBuilder()))
            .getExpectedResponse();

    val accept = flughafen.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    NamedEnvelope<FuzzingMutator<GemCloseOperationParameters>> removeDispensesation =
        NamedEnvelope.of(
            "Remove Dispensation",
            closeOperationParameters ->
                closeOperationParameters.getParameter().stream()
                    .findFirst()
                    .ifPresent(
                        param -> param.getPart().get(0).setPart(List.of()).setResource(null)));

    val closeResponse =
        flughafen.performs(
            ClosePrescription.alternative()
                .withCloseResourceManipulator(removeDispensesation)
                .acceptedWith(accept));

    flughafen.attemptsTo(
        Verify.that(closeResponse)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Resource not found in parameter part",
                    // "Unzulässige Abgabeinformationen: Für diesen Workflow sind nur"
                    //        + " Abgabeinformationen für Arzneimittel zulässig.",
                    // details text: "parsing / validation error"
                    // diagnostics: "Resource not found in parameter part"
                    ErpAfos.A_26002_02))
            .isCorrect());
  }
}
