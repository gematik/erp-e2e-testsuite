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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import lombok.val;
import org.junit.jupiter.api.Test;

class GemErpMedicationBuilderTest extends ParsingTest {

  @Test
  void shouldBuildGemErpMedicationWithFixedValues() {
    // https://github.com/gematik/eRezept-Examples/blob/main/Standalone-Examples/E-Rezept-Workflow_gematik/1.4.3/Medication-SumatripanMedication.xml
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .category(EpaDrugCategory.C_00)
            .pzn("06313728", "Sumatriptan-1a Pharma 100 mg Tabletten")
            .isVaccine(false)
            .darreichungsform(Darreichungsform.TAB)
            .normgroesse(StandardSize.N1)
            .amount(20) // will use St as default unit
            .lotNumber("1234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());

    assertFalse(medication.isVaccine());

    assertTrue(medication.getPzn().isPresent());
    assertEquals("06313728", medication.getPzn().get().getValue());
    assertTrue(medication.getName().isPresent());
    assertEquals("Sumatriptan-1a Pharma 100 mg Tabletten", medication.getName().get());

    assertTrue(medication.getCategory().isPresent());
    assertEquals(EpaDrugCategory.C_00, medication.getCategory().get());

    assertTrue(medication.getDarreichungsform().isPresent());
    assertEquals(Darreichungsform.TAB, medication.getDarreichungsform().get());

    assertTrue(medication.getStandardSize().isPresent());
    assertEquals(StandardSize.N1, medication.getStandardSize().get());

    assertTrue(medication.getBatchLotNumber().isPresent());
    assertEquals("1234567890", medication.getBatchLotNumber().get());
    // this one is not null-safe!!
    assertEquals("1234567890", medication.getBatch().getLotNumber());

    assertTrue(medication.getAmountNumeratorUnit().isPresent());
    assertEquals("Stk", medication.getAmountNumeratorUnit().get());
    assertTrue(medication.getAmountNumerator().isPresent());
    assertEquals(20, medication.getAmountNumerator().get());
  }

  @Test
  void shouldBuildSimpleMedication() {
    // https://github.com/gematik/eRezept-Examples/blob/main/Standalone-Examples/E-Rezept-Workflow_gematik/1.4.3/Medication-SimpleMedication.xml
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .pzn("06313728")
            .lotNumber("1234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildSimplestMedication() {
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .pzn("06313728")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
    assertTrue(medication.getBatchLotNumber().isEmpty());
  }
}
