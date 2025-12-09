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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProdBuilder;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProduct;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.5.0")
class GemErpMedicationKombiPkgBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildMinimalCorrectKombiPckg() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .amount(5)
            .packaging("Box")
            .ingredientComponent(ingredComp)
            .build();

    assertNotNull(kombiPckg);
    assertTrue(parser.isValid(kombiPckg));
    assertEquals(5, kombiPckg.getAmount().getNumerator().getValue().intValue());
    assertEquals("Box", kombiPckg.getPackaging().orElseThrow());
  }

  @Test
  void shouldSetManufacturingInstructionCorrect() {
    val instr = "beides in eine Kiste packen";
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .codeText("CodeText")
            .manufacturingInstruction(instr)
            .build();

    assertNotNull(kombiPckg);
    assertTrue(parser.isValid(kombiPckg));
    assertEquals(instr, kombiPckg.getManufacturingInstruction().orElseThrow());
  }

  @Test
  void shouldSetAmountWithUnit() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .codeText("CodeText")
            .amount(10, "Stk")
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals(10, kombiPckg.getAmount().getNumerator().getValue().intValue());
    assertEquals("Stk", kombiPckg.getAmount().getNumerator().getUnit());
  }

  @Test
  void shouldSetPackagingSize() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .codeText("CodeText")
            .amount(3)
            .packagingSize("N1")
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals("N1", kombiPckg.getPackagingSize().orElseThrow());
  }

  @Test
  void shouldThrowWhenPackagingSizeTooLong() {
    val longSize = RandomStringUtils.random(91, true, true);
    val builder = GemErpMedicationKombiPkgBuilder.forKombiPckg().amount(3).packagingSize(longSize);
    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void shouldSetLotNumber() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .pzn(PZN.from("12345678"))
            .amount(1)
            .lotNumber("LOT123")
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals("LOT123", kombiPckg.getBatchLotNumber().orElseThrow());
  }

  @Test
  void shouldSetFormText() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .amount(1)
            .codeText("CodeText")
            .formText("Tablette")
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals("Tablette", kombiPckg.getForm().getText());
  }

  @Test
  void shouldSetAmountDenominator() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .codeText("CodeText")
            .amount(2)
            .amountDenominator(5)
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals(5, kombiPckg.getAmount().getDenominator().getValue().intValue());
  }

  @Test
  void shouldSetCodeText() {
    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg().amount(1).codeText("KombiCodeText").build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals("KombiCodeText", kombiPckg.getCode().getText());
  }

  @Test
  void shouldSetContainedMedications() {

    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .amount(1)
            .pzn(PZN.random())
            .containedMedications(contained1)
            .containedMedications(contained2)
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals(2, kombiPckg.getContained().size());
    assertEquals(contained1.getId(), kombiPckg.getContained().get(0).getId());
    assertEquals(contained2.getId(), kombiPckg.getContained().get(1).getId());
  }

  @Test
  void shouldSetContainedMedicationsAsList() {

    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .amount(1)
            .pzn(PZN.random())
            .containedMedications(List.of(contained1, contained2))
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals(2, kombiPckg.getContained().size());
    assertEquals(contained1.getId(), kombiPckg.getContained().get(0).getId());
    assertEquals(contained2.getId(), kombiPckg.getContained().get(1).getId());
  }

  @Test
  void shouldBuildWithAllFields() {

    val kombiPckg =
        GemErpMedicationKombiPkgBuilder.forKombiPckg()
            .amount(2, "Stk")
            .amountDenominator(4)
            .packaging("Box")
            .packagingSize("N2")
            .lotNumber("LOT999")
            .formText("Kapsel")
            .codeText("KombiText")
            .containedMedications(contained1)
            .build();

    assertTrue(parser.isValid(kombiPckg));
    assertEquals(2, kombiPckg.getAmount().getNumerator().getValue().intValue());
    assertEquals("Stk", kombiPckg.getAmount().getNumerator().getUnit());
    assertEquals(4, kombiPckg.getAmount().getDenominator().getValue().intValue());
    assertEquals("Box", kombiPckg.getPackaging().orElseThrow());
    assertEquals("N2", kombiPckg.getPackagingSize().orElseThrow());
    assertEquals("LOT999", kombiPckg.getBatchLotNumber().orElseThrow());
    assertEquals("Kapsel", kombiPckg.getForm().getText());
    assertEquals("KombiText", kombiPckg.getCode().getText());
    assertEquals(contained1.getId(), kombiPckg.getContained().get(0).getId());
  }

  Medication.MedicationIngredientComponent ingredComp =
      GemEpaIngredientComponentBuilder.builder().atc(ATC.from("A01AB")).build();
  EpaPharmaceuticalProduct contained1 =
      EpaPharmaceuticalProdBuilder.builder()
          .withoutVersion()
          .ingredientComponent(ingredComp)
          .atcCode(ATC.from("123456", "UnknownTestName1"))
          .build();

  EpaPharmaceuticalProduct contained2 =
      EpaPharmaceuticalProdBuilder.builder()
          .withoutVersion()
          .ingredientComponent(ingredComp)
          .atcCode(ATC.from("456789", "UnknownTestName2"))
          .build();
}
