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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuMedicationBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldSetCorrectPzn() {
    val pzn = PZN.from("pzn-Code");
    val pznMedication = EuMedicationBuilder.builder().pzn(pzn).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(pzn.getValue(), pznMedication.getPzn().orElseThrow().getValue());
  }

  @Test
  void shouldSetCorrectPznAsString() {
    val pzn = "pzn-Code";
    val pznMedication = EuMedicationBuilder.builder().pzn(pzn).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(pzn, pznMedication.getPzn().orElseThrow().getValue());
  }

  @Test
  void shouldSetCorrectPznWithName() {
    val pzn = PZN.from("pzn-Code");
    val name = "Test Name";
    val pznMedication = EuMedicationBuilder.builder().pzn(pzn, name).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(pzn.getValue(), pznMedication.getPzn().orElseThrow().getValue());
    assertEquals(name, pznMedication.getCode().getCoding().get(0).getDisplay());
  }

  @Test
  void shouldSetCorrectSize() {
    val size = "100";
    val pznMedication =
        EuMedicationBuilder.builder().pzn("123").amount(3).packagingSize(size).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(
        size,
        pznMedication
            .getAmount()
            .getNumerator()
            .getExtension()
            .get(0)
            .getValueAsPrimitive()
            .getValueAsString());
  }

  @Test
  void shouldSetCorrectAmount() {
    val num = 100;
    val pznMedication = EuMedicationBuilder.builder().pzn("123").amount(num).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(num, pznMedication.getAmount().getNumerator().getValue().intValue());
  }

  @Test
  void shouldSetCorrectAmountWithUnit() {
    val num = 100;
    val unit = "ml";
    val pznMedication = EuMedicationBuilder.builder().pzn("123").amount(num, unit).build();

    assertTrue(parser.isValid(pznMedication));
    assertEquals(num, pznMedication.getAmount().getNumerator().getValue().intValue());
    assertEquals(unit, pznMedication.getAmount().getNumerator().getUnit());
  }

  @Test
  void shouldSetCorrectAmountDenominator() {
    val num = 100;
    val denom = 10;
    val pznMedication =
        EuMedicationBuilder.builder().pzn("123").amount(num).amountDenominator(denom).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(num, pznMedication.getAmount().getNumerator().getValue().intValue());
    assertEquals(denom, pznMedication.getAmount().getDenominator().getValue().intValue());
  }

  @Test
  void shouldSetCorrectDarreichungsform() {
    val form = Darreichungsform.PUE;
    val pznMedication = EuMedicationBuilder.builder().pzn("123").darreichungsform(form).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(form, pznMedication.getDarreichungsform().orElseThrow());
  }

  @Test
  void shouldSetCorrectCodeText() {
    val codeText = "Test Text in Code";
    val pznMedication = EuMedicationBuilder.builder().pzn("123").codeText(codeText).build();
    assertTrue(parser.isValid(pznMedication));
    assertEquals(codeText, pznMedication.getCode().getText());
  }

  @Test
  void shouldSetCorrectNormgroesse() {
    val normgroesse = StandardSize.N1;
    val pznMedication = EuMedicationBuilder.builder().pzn("123").normgroesse(normgroesse).build();

    assertTrue(parser.isValid(pznMedication));
    assertEquals(normgroesse, pznMedication.getStandardSize().orElseThrow());
  }

  @Test
  void shouldGetMedicationName() {
    val pzn = PZN.from("12345678");
    val name = "Test Medication Name";
    val medication = EuMedicationBuilder.builder().pzn(pzn, name).build();
    assertEquals(name, medication.getCode().getCoding().get(0).getDisplay());

    assertTrue(parser.isValid(medication));
  }

  @Test
  void shouldBuildMedicationWithAllPossibleValues() {
    val pzn = PZN.from("12345678");
    val name = "Test Medication Name";
    val darreichungsform = Darreichungsform.PUE;
    val normgroesse = StandardSize.N1;
    val amountNumerator = 100L;
    val amountDenominator = 10L;
    val amountUnit = "mg";
    val packagingSize = "50";
    val codeText = "Test Code Text";
    val lotNumber = "TestLot123";
    val totalQuantity = "totalQ";
    val packaging = "packag";
    val formText = "formText";
    val manufacturingInstruction = "manufacturingInstruction";
    val version = EuVersion.getDefaultVersion();
    val ask = ASK.from("ASK_12345678");
    val snomed = "snomed";
    val atc = ATC.from("ATC_12345678");
    val category = EpaDrugCategory.C_00;
    val isVaccine = true;

    val medication =
        EuMedicationBuilder.builder()
            .pzn(pzn, name)
            .darreichungsform(darreichungsform)
            .normgroesse(normgroesse)
            .amount(amountNumerator, amountUnit)
            .amountDenominator(amountDenominator)
            .packagingSize(packagingSize)
            .codeText(codeText)
            .lotNumber(lotNumber)
            .totalQuantity(totalQuantity)
            .packaging(packaging)
            .formText(formText)
            .manufacturingInstruction(manufacturingInstruction)
            .version(version)
            .ask(ask)
            .snomed(snomed)
            .atc(atc)
            .category(category)
            .isVaccine(isVaccine)
            .build();
    assertNotNull(medication);

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
    assertEquals(pzn.getValue(), medication.getPzn().orElseThrow().getValue());
    assertEquals(name, medication.getCode().getCoding().get(0).getDisplay());
    assertEquals(darreichungsform, medication.getDarreichungsform().orElseThrow());
    assertEquals(normgroesse, medication.getStandardSize().orElseThrow());
    assertEquals(amountNumerator, medication.getAmount().getNumerator().getValue().longValue());
    assertEquals(amountUnit, medication.getAmount().getNumerator().getUnit());
    assertEquals(amountDenominator, medication.getAmount().getDenominator().getValue().longValue());
    assertEquals(packagingSize, medication.getPackagingSize().orElseThrow());
    assertEquals(codeText, medication.getCode().getText());
    assertEquals(lotNumber, medication.getBatch().getLotNumber());
    assertEquals(totalQuantity, medication.getTotalQuantity().orElseThrow());
    assertEquals(packaging, medication.getPackaging().orElseThrow());
    assertEquals(formText, medication.getFormText().orElseThrow());
    assertEquals(manufacturingInstruction, medication.getManufacturingInstruction().orElseThrow());
    assertTrue(medication.getMeta().getProfile().get(0).getValue().endsWith("|1.1"));
    assertEquals(ask, medication.getAsk().orElseThrow());
    assertEquals(snomed, medication.getSnomed().orElseThrow());
    assertEquals(atc, medication.getAtc().orElseThrow());
    assertEquals(category.getCode(), medication.getCategory().get().getCode());
    assertTrue(medication.isVaccine());
  }
}
