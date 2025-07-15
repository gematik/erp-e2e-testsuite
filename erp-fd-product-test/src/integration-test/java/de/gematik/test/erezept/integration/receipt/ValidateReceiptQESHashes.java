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

package de.gematik.test.erezept.integration.receipt;

import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.compareSignatureHashWith;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName(
    "QES_Hash der Signatur des Arztes muss dem in der Quittung hinterlegtem Hash entsprechen")
@Tag("QES")
public class ValidateReceiptQESHashes extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctorActor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patientActor;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacyActor;

  private static Stream<Arguments> testCaseComposer() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
                PrescriptionAssignmentKind.PHARMACY_ONLY)
            .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
            .multiply(1, PrescriptionAssignmentKind.class);
    return composer.create();
  }

  @SneakyThrows
  @TestcaseId("ERP_TASK_VALIDATE_HASHES")
  @ParameterizedTest(
      name =
          "[{index}] -> Einstellen eines {0} Rezeptes als {1} ein. Dabei muss der Hash werd der QES"
              + " Verordnung dem Binary Hash des Quittungsbundles entsprechen.")
  @DisplayName("Verifizierung der QES Hashes im Lebenszyklus eines E-Rezeptes")
  @MethodSource("testCaseComposer")
  void validateHashes(InsuranceTypeDe insuranceType, PrescriptionAssignmentKind assignmentKind) {

    val sigObserver = new ByteArrayOutputStream();
    patientActor.changePatientInsuranceType(insuranceType);
    val activation =
        doctorActor.performs(
            IssuePrescription.forPatient(patientActor)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle()
                .setSignatureObserver(sigObserver));
    val activationTask = activation.getExpectedResponse();
    val docSignedDocument = sigObserver.toByteArray();

    val acceptation = pharmacyActor.performs(AcceptPrescription.forTheTask(activationTask));
    val dispensation = pharmacyActor.performs(ClosePrescription.acceptedWith(acceptation));

    pharmacyActor.attemptsTo(
        Verify.that(dispensation)
            .withExpectedType(ErpAfos.A_19233)
            .and(compareSignatureHashWith(docSignedDocument, ErpAfos.A_19233))
            .isCorrect());
  }
}
