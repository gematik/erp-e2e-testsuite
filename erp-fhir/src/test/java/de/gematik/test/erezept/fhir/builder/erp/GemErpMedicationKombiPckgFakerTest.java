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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;
import static de.gematik.test.erezept.fhir.builder.GemFaker.getFaker;
import static de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProdBuilder;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProduct;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.Mockito;

@SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.5.0")
class GemErpMedicationKombiPckgFakerTest extends ErpFhirParsingTest {

  @RepeatedTest(5)
  void shouldRandomlyFake() {
    val medication = GemErpMedicationFaker.forKombiPkg().fake();
    assertEquals(2, medication.getContained().size());
    assertTrue(parser.isValid(medication));
  }

  @RepeatedTest(5)
  void shouldFakeWithFixedValues() {
    val medication = GemErpMedicationFaker.forKombiPkg();

    if (fakerBool()) medication.withSnomed(fakerLotNumber());
    if (fakerBool())
      medication.withAmount(getFaker().random().nextLong(7), getFaker().starTrek().character());
    if (fakerBool()) medication.withDrugCategory(EpaDrugCategory.C_00);
    if (fakerBool()) medication.withLotNumber(fakerLotNumber());
    if (fakerBool()) medication.withVaccine(true);
    if (fakerBool()) medication.withAsk(ASK.from(getFaker().superhero().name()));
    if (fakerBool())
      medication.withIngredientWithContainedAtc(
          getFaker().random().nextInt(1000),
          getFaker().random().nextInt(500),
          ATC.from(GemFaker.getFaker().regexify("[0-9]{1,4}")));

    assertTrue(parser.isValid(medication.fake()));
  }

  @RepeatedTest(5)
  void shouldFakeEverythingWithAllFakerBools() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(true);
      val medication = GemErpMedicationFaker.forKombiPkg().fake();
      assertTrue(parser.isValid(medication));
    }
  }

  @Test
  void shouldFakeEverythingWithoutFakerBool() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val medication = GemErpMedicationFaker.forKombiPkg().fake();
      assertTrue(parser.isValid(medication));
    }
  }

  @RepeatedTest(5)
  void shouldFakeWithMultipleAsks() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val medication =
          GemErpMedicationFaker.forKombiPkg()
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withSnomed("1234567890")
              .withSnomed("123456789123456")
              .withSnomed("1234567891234567890")
              .withAtc(ATC.from(getFaker().superhero().name()))
              .withAtc(ATC.from(getFaker().superhero().name()))
              .withPzn(PZN.from("123456789"))
              .fake();

      assertTrue(ValidatorUtil.encodeAndValidate(parser, medication).isSuccessful());
      assertEquals(10, medication.getCode().getCoding().size());
    }
  }

  @Test
  void shouldBuildMinimal() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val medication = GemErpMedicationFaker.forKombiPkg().fake();
      val res = ValidatorUtil.encodeAndValidate(parser, medication);
      assertTrue(res.isSuccessful());
    }
  }

  @Test
  void shouldSetSnomedPositively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withSnomed("1234567890");
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(
        medication.getCode().getCoding().stream().anyMatch(c -> "1234567890".equals(c.getCode())));
  }

  @Test
  void shouldSetSnomedCorrectly() {
    val faker = GemErpMedicationFaker.forKombiPkg().withSnomed("-123");
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getCode().getCoding().stream().anyMatch(c -> "-123".equals(c.getCode())));
  }

  @Test
  void shouldSetAmountPositively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withAmount(10, "mg");
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertEquals("mg", medication.getAmount().getNumerator().getUnit());
    assertEquals(10, medication.getAmount().getNumerator().getValue().intValue());
  }

  @Test
  void shouldSetDrugCategoryPositively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withDrugCategory(EpaDrugCategory.C_00);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getCategory().isPresent());
    assertEquals(EpaDrugCategory.C_00, medication.getCategory().get());
  }

  @Test
  void shouldSetDrugCategoryNegatively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withDrugCategory(null);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertFalse(medication.getCategory().isPresent());
  }

  @Test
  void shouldSetLotNumberPositively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withLotNumber("LOT123");
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getBatchLotNumber().isPresent());
    assertEquals("LOT123", medication.getBatchLotNumber().get());
  }

  @Test
  void shouldSetLotNumberNegatively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withLotNumber("");
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertFalse(medication.getBatchLotNumber().isPresent());
  }

  @Test
  void shouldSetContainedMedicationsDirectly() {
    val faker = GemErpMedicationFaker.forKombiPkg().withContained(contained1, contained2);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertEquals(2, medication.getContained().size());
    assertEquals(contained1.getId(), medication.getContained().get(0).getId());
    assertEquals(contained2.getId(), medication.getContained().get(1).getId());
  }

  @Test
  void shouldSetContainedMedicationsAsAtc() {
    val atc1 = ATC.from("123456", "UnknownTestName1");
    val atc2 = ATC.from("789123", "UnknownTestName2");
    val faker =
        GemErpMedicationFaker.forKombiPkg().withContainedAtcMedications(List.of(atc1, atc2));
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertEquals(2, medication.getContained().size());
    val containedEpaMed1 = (EpaPharmaceuticalProduct) medication.getContained().get(0);
    val containedEpaMed2 = (EpaPharmaceuticalProduct) medication.getContained().get(1);
    assertEquals(atc1.getValue(), containedEpaMed1.getAtcCode().orElseThrow().getValue());
    assertEquals(atc2.getValue(), containedEpaMed2.getAtcCode().orElseThrow().getValue());
    assertEquals(atc1.getSystem(), containedEpaMed1.getAtcCode().orElseThrow().getSystem());
    assertEquals(atc2.getSystem(), containedEpaMed2.getAtcCode().orElseThrow().getSystem());
  }

  @Test
  void shouldSeContainedMedicationsAsList() {
    val faker =
        GemErpMedicationFaker.forKombiPkg().withContained(contained1, contained2, contained1);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertEquals(3, medication.getContained().size());
    assertEquals(contained1.getId(), medication.getContained().get(0).getId());
    assertEquals(contained2.getId(), medication.getContained().get(1).getId());
  }

  Medication.MedicationIngredientComponent ingredComp =
      GemEpaIngredientComponentBuilder.builder().atc(ATC.from("A01AB")).build();
  EpaPharmaceuticalProduct contained1 =
      EpaPharmaceuticalProdBuilder.builder()
          .ingredientComponent(ingredComp)
          .atcCode(ATC.from("123456", "UnknownTestName1"))
          .build();

  EpaPharmaceuticalProduct contained2 =
      EpaPharmaceuticalProdBuilder.builder()
          .ingredientComponent(ingredComp)
          .atcCode(ATC.from("456789", "UnknownTestName2"))
          .build();

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldSetVaccineCorrectly(boolean isVaccine) {
    val faker = GemErpMedicationFaker.forKombiPkg().withVaccine(isVaccine);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    if (isVaccine) {
      assertTrue(medication.isVaccine());
    } else {
      assertFalse(medication.isVaccine());
    }
  }

  @Test
  void shouldSetAskPositively() {
    val ask = ASK.from("ASK123");
    val faker = GemErpMedicationFaker.forKombiPkg().withAsk(ask);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getAsk().isPresent());
    assertEquals(ask, medication.getAsk().get());
  }

  @Test
  void shouldSetVersionPositively() {
    val faker = GemErpMedicationFaker.forKombiPkg().withVersion(ErpWorkflowVersion.V1_5);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getMeta().getProfile().get(0).getValue().endsWith("1.5"));
  }

  @Test
  void shouldSetVersionNegatively() {
    val faker =
        GemErpMedicationFaker.forKombiPkg().withVersion(ErpWorkflowVersion.getDefaultVersion());
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
  }

  @Test()
  void shouldSetIngredientWithContainedAtcPositively() {
    val atc = ATC.from("A01");
    val faker =
        GemErpMedicationFaker.forKombiPkg().withAtc(atc).withIngredientWithContainedAtc(1, 2, atc);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getAtc().isPresent());
  }

  @Test
  void shouldSetPznPositively() {
    val pzn = PZN.from("123456789");
    val faker = GemErpMedicationFaker.forKombiPkg().withPzn(pzn);
    val medication = faker.fake();
    assertTrue(parser.isValid(medication));
    assertTrue(medication.getPzn().isPresent());
    assertEquals(pzn, medication.getPzn().get());
  }

  @Test
  void shouldSetAllSettersAndValidate() {
    val ask = ASK.from("ASK123");
    val atc = ATC.from("A01");
    val pzn = PZN.from("123456789");
    val faker =
        GemErpMedicationFaker.forKombiPkg()
            .withSnomed("SNOMED123")
            .withAmount(5, "ml")
            .withDrugCategory(EpaDrugCategory.C_00)
            .withLotNumber("LOT999")
            .withVaccine(true)
            .withAsk(ask)
            .withAtc(atc)
            .withVersion(ErpWorkflowVersion.getDefaultVersion())
            .withIngredientWithContainedAtc(1, 2, atc)
            .withPzn(pzn);
    val medication = faker.fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
    assertTrue(medication.getAsk().isPresent());
    assertTrue(medication.getAtc().isPresent());
    assertTrue(medication.getPzn().isPresent());
    assertTrue(medication.isVaccine());
    assertTrue(medication.getBatchLotNumber().isPresent());
    assertEquals("LOT999", medication.getBatchLotNumber().get());
    assertEquals("ml", medication.getAmount().getNumerator().getUnit());
    assertEquals(5, medication.getAmount().getNumerator().getValue().intValue());
  }
}
