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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

@ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
class ErxMedicationDispenseFakerTest extends ErpFhirParsingTest {

  @Test
  void buildFakeMedicationDispenseWithPrescriptionId() {
    val medDispense =
        ErxMedicationDispenseFaker.builder()
            .withPrescriptionId(PrescriptionId.random().getValue())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispense() {
    val medDispense = ErxMedicationDispenseFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithStatus() {
    val medDispense =
        ErxMedicationDispenseFaker.builder()
            .withStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .fake();
    val medDispense2 =
        ErxMedicationDispenseFaker.builder()
            .withStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED.toCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medDispense2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  @SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.3.0")
  void shouldFakeWithGivenKbvMedication() {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medDispense = ErxMedicationDispenseFaker.builder().withMedication(medication).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  @SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.4.0")
  void shouldFakeWithGivenGemErpMedication() {
    val medication = GemErpMedicationFaker.builder().fake();
    val medDispense = ErxMedicationDispenseFaker.builder().withMedication(medication).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithHandedOverDate() {
    val medDispense = ErxMedicationDispenseFaker.builder().withHandedOverDate(new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithPreparedDate() {
    val medDispense = ErxMedicationDispenseFaker.builder().withPreparedDate(new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithBatch() {
    val batch = new Medication.MedicationBatchComponent();
    batch.setLotNumber("123");
    val medDispense = ErxMedicationDispenseFaker.builder().withBatch(batch).fake();
    val medDispense2 = ErxMedicationDispenseFaker.builder().withBatch("123", new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medDispense2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }
}
