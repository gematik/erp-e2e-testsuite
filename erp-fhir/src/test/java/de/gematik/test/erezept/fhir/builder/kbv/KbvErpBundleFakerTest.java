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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.QuantityBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;

class KbvErpBundleFakerTest extends ParsingTest {
  @Test
  void buildFakeKbvErpBundleWithPrescriptionId() {
    val bundle = KbvErpBundleFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    val bundle2 =
        KbvErpBundleFaker.builder().withPrescriptionId(PrescriptionId.random().getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithStatusKennzeichen() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withStatusKennzeichen(fakerValueSet(StatusKennzeichen.class).getCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvEprBundleWithKvnr() {
    val kvnr = KVNR.random();
    val bundle = KbvErpBundleFaker.builder().withKvnr(kvnr).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(kvnr, bundle.getPatient().getKvnr());
  }

  @Test
  void buildFakeKbvEprBundleWithPatient() {
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .fake();
    val bundle = KbvErpBundleFaker.builder().withPatient(patient).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertEquals(patient.hashCode(), bundle.getPatient().hashCode());
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithDosageInstruction() {
    val bundle = KbvErpBundleFaker.builder().withDosageInstruction("di").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("di", bundle.getMedicationRequest().getDosageInstructionFirstRep().getText());
  }

  @Test
  void buildFakeKbvErpBundleWithBvg() {
    val bundle = KbvErpBundleFaker.builder().withBvg(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMedicationRequest().isBvg());
  }

  @Test
  void buildFakeKbvErpBundleWithEmergencyServiceFee() {
    val bundle = KbvErpBundleFaker.builder().withEmergencyServiceFee(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMedicationRequest().hasEmergencyServiceFee());
  }

  @Test
  void buildFakeKbvErpBundleWithAccident() {
    val bundle = KbvErpBundleFaker.builder().withAccident(AccidentExtension.accident()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithMedicationRequestVersion() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withMedicationRequestVersion(KbvItaErpVersion.getDefaultVersion())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithQuantity() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withQuantity(new MedicationRequest.MedicationRequestDispenseRequestComponent())
            .fake();
    val bundle2 =
        KbvErpBundleFaker.builder()
            .withQuantity(QuantityBuilder.asUcumPackage().withValue(fakerAmount()))
            .fake();
    val bundle3 = KbvErpBundleFaker.builder().withQuantityPackages(fakerAmount()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    val result3 = ValidatorUtil.encodeAndValidate(parser, bundle3);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
    assertTrue(result3.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithCoPaymentStatus() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withCoPaymentStatus(fakerValueSet(StatusCoPayment.class))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithNote() {
    val bundle = KbvErpBundleFaker.builder().withNote("note").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("note", bundle.getMedicationRequest().getNoteText().orElse(""));
  }

  @Test
  void buildFakeKbvErpBundleWithIntent() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withIntent(MedicationRequest.MedicationRequestIntent.ORDER.toCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("order", bundle.getMedicationRequest().getIntent().toCode());
  }

  @Test
  void buildFakeKbvErpBundleWithSubstitution() {
    val bundle = KbvErpBundleFaker.builder().withSubstitution(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMedicationRequest().allowSubstitution());
  }

  @Test
  void buildFakeKbvErpBundleWithAttester() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withPractitioner(
                PractitionerFaker.builder()
                    .withQualificationType(QualificationType.DOCTOR_IN_TRAINING)
                    .fake())
            .withAttester(PractitionerFaker.builder().fake())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithStatus() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
            .fake();
    val bundle2 = KbvErpBundleFaker.builder().withStatus("active").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithMvo() {
    val bundle = KbvErpBundleFaker.builder().withMvo(mvo()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }
}
