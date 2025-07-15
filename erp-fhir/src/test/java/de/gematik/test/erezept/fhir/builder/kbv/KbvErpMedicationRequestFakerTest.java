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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

class KbvErpMedicationRequestFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakerMedicationRequestWithVersion() {
    val medication =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(KbvItaErpVersion.getDefaultVersion())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithMedication() {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withMedication(KbvErpMedication.fromMedication(medication))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithRequester() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withRequester(KbvPractitionerFaker.builder().fake())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithInsurance() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withInsurance(KbvCoverageFaker.builder().fake())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithAccident() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withAccident(AccidentExtension.faker()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithStatus() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
            .fake();
    val medicationRequest2 = KbvErpMedicationRequestFaker.builder().withStatus("active").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medicationRequest2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithIntent() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withIntent(MedicationRequest.MedicationRequestIntent.ORDER)
            .fake();
    val medicationRequest2 = KbvErpMedicationRequestFaker.builder().withIntent("order").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medicationRequest2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithSubstitution() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withSubstitution(new MedicationRequest.MedicationRequestSubstitutionComponent())
            .fake();
    val medicationRequest2 =
        KbvErpMedicationRequestFaker.builder().withSubstitution(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medicationRequest2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithQuantity() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withQuantity(new Quantity()).fake();
    val medicationRequest2 =
        KbvErpMedicationRequestFaker.builder()
            .withQuantity(new MedicationRequest.MedicationRequestDispenseRequestComponent())
            .fake();
    val medicationRequest3 =
        KbvErpMedicationRequestFaker.builder().withQuantityPackages(fakerAmount()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medicationRequest2);
    val result3 = ValidatorUtil.encodeAndValidate(parser, medicationRequest3);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
    assertTrue(result3.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithDosageInstruction() {
    val dosageText = "Dosage Instruction";
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withDosageInstruction(dosageText).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertEquals(dosageText, medicationRequest.getDosageInstruction().get(0).getText());
  }

  @Test
  void buildFakerMedicationRequestWithCoPaymentStatus() {
    val status = StatusCoPayment.STATUS_0;
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withCoPaymentStatus(status).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertEquals(Optional.of(status), medicationRequest.getCoPaymentStatus());
  }

  @Test
  void buildFakerMedicationRequestWithBvg() {
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withBvg(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithEmergencyServiceFee() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withEmergencyServiceFee(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithMvo() {
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withMvo(mvo()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithAuthorDate() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withAuthorDate(new Date()).fake();
    val medicationRequest2 =
        KbvErpMedicationRequestFaker.builder()
            .withAuthorDate(new Date(), TemporalPrecisionEnum.DAY)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medicationRequest2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerMedicationRequestWithNote() {
    val noteText = "This is note";
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withNote(noteText).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertEquals(Optional.of(noteText), medicationRequest.getNoteText());
  }

  @Test
  void buildFakerMedicationRequestWithPatient() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withPatient(KbvPatientFaker.builder().fake()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }
}
