/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.isInProgressStatus;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleContainsNameInMedicationCompound;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleContainsPzn;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Valide PZN MedicationCompounding")
public class ActivateMedicationCompounding extends ErpTest {

  @Actor(name = "Leonie Hütter")
  private static PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private static DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private static PharmacyActor pharmacy;

  private static Stream<Arguments> medicationCompoundingComposer() {
    return ArgumentComposer.composeWith()
        .arguments("Salbe", "2 Komponenten Salbe")
        .arguments("Creme", "3 Komponenten Creme")
        .arguments("Pomade", "die 60er sind zurück")
        .arguments("Uran Spray", "leuchtet so schön")
        .multiply(0, List.of(VersicherungsArtDeBasis.PKV, VersicherungsArtDeBasis.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .create();
  }

  @TestcaseId("ERP_ACTIVATE_MEDICATION_COMPOUNDING_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe ob beim akzeptieren für {0} als {1} die MedicationCompounding mit {2} korrekt übermittelt wurde")
  @DisplayName(
      "Prüfe den Inhalt der MedicationCompounding als Patient und die Signatur als Apotheke")
  @MethodSource("medicationCompoundingComposer")
  void activateMedicationCompoundingAnCheckAsConsumer(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      String medicineName,
      String freetext) {

    val pzn = PZN.random();
    patient.changePatientInsuranceType(insuranceType);
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(pzn, medicineName, freetext)
            .fake();
    val assigner = AssignerOrganizationFaker.builder().fake();
    val insurance =
        KbvCoverageFaker.builder().withInsuranceType(patient.getCoverageInsuranceType()).fake();
    val medicationRequest =
        MedicationRequestFaker.builder()
            .withPatient(patient.getPatientData())
            .withMedication(medication)
            .withRequester(doc.getPractitioner())
            .withInsurance(insurance)
            .fake();

    val organisation = MedicalOrganizationFaker.builder().fake();
    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .patient(patient.getPatientData())
            .assigner(assigner)
            .practitioner(doc.getPractitioner())
            .custodian(organisation)
            .medicationRequest(medicationRequest)
            .insurance(insurance)
            .medication(medication);
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    doc.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .isCorrect());
    val getTaskByIdInteraction =
        patient.performs(
            GetPrescriptionById.withTaskId(activation.getExpectedResponse().getTaskId())
                .withoutAuthentication());

    patient.attemptsTo(
        Verify.that(getTaskByIdInteraction)
            .withExpectedType(ErpAfos.A_19113)
            .hasResponseWith(returnCode(200))
            .and(bundleContainsPzn(pzn, ErpAfos.A_24034))
            .and(bundleContainsNameInMedicationCompound(medicineName, ErpAfos.A_19113))
            .isCorrect());
    val acceptance =
        pharmacy.performs(AcceptPrescription.forTheTask(activation.getExpectedResponse()));

    pharmacy.attemptsTo(
        Verify.that(acceptance)
            .withExpectedType(ErpAfos.A_19113)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());
    val acceptBundle = acceptance.getExpectedResponse();

    val signedKbvBundle = acceptBundle.getSignedKbvBundle();
    pharmacy.attemptsTo(Ensure.that(IsValidSignature.forDocument(signedKbvBundle)).isTrue());
    // cleanup
    pharmacy.performs(ClosePrescription.acceptedWith(acceptance));
  }

}
