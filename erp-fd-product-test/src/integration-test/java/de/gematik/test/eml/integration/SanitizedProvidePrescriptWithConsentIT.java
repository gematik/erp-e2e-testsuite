/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.emlMedRequestDosageHasText;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionWithTask;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Provide Prescription with Consent Test")
@Tag("ProvideEmlPrescription")
@Tag("EpaEml")
@Tag("EpaEmlSanitized")
public class SanitizedProvidePrescriptWithConsentIT extends ErpTest {
  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @ParameterizedTest(name = "{0} → {1}")
  @TestcaseId("EML_PROVIDE_PRESCRIPTION_DOSAGE_INSTRUCTION_04")
  @DisplayName(
      "Es soll geprüft werden, dass der FD die Zeichen der DosageInstruction beim Export korrekt"
          + " transformiert")
  @MethodSource("dosageInstructionTestData")
  void shouldExportDosageInstructionWithCorrectEscaping(
      String dosageInstructionText, String expectedDosageInstructionText) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(InsuranceTypeDe.GKV);

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageInstruction(
                            dosageInstructionText) // The parameter represents only the text value
                        // of the DosageInstruction, not the entire
                        // element.
                        .toBuilder()));

    val task = activation.getExpectedResponse();

    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    val kbvBundle = prescr.getExpectedResponse().getKbvBundle().orElseThrow();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            kbvBundle,
            doc.getSmcbTelematikId(),
            doc.getHbaTelematikId(),
            emlMedRequestDosageHasText(expectedDosageInstructionText)));
  }

  private static Stream<Arguments> dosageInstructionTestData() {
    return Stream.of(
        /* Arguments.of(
            "<",
            "<"), // TODO: The "<" -> "<" case is expected to fail once the FD feature is deployed,
        // as "<" will then be correctly escaped to "\\u003C"*/
        Arguments.of("<script", "\\u003Cscript"),
        Arguments.of("<Script", "\\u003CScript"),
        Arguments.of("</Script", "\\u003C/Script"),
        Arguments.of("</script", "\\u003C/script"),
        Arguments.of("<!--", "\\u003C-->"),
        Arguments.of("]]>", "]]\\u003e"),
        Arguments.of("-->", "--\\u003e"),
        Arguments.of("\u2028", "\\u2028"),
        Arguments.of("\u2029", "\\u2029"),
        Arguments.of("U+2028", "\\u2028"),
        Arguments.of("U+2029", "\\u2029"));
  }
}
