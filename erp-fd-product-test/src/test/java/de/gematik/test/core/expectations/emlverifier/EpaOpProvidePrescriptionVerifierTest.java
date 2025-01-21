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

package de.gematik.test.core.expectations.emlverifier;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class EpaOpProvidePrescriptionVerifierTest {

  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));
  private static EpaOpProvidePrescription epaOpProvidePrescription;
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithPzn;
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithIngredient;
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithCompounding;
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithFreeText;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("not needed");
    val fhir = EpaFhirFactory.create();
    epaOpProvidePrescription =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/medication/Parameters-example-epa-op-provide-prescription-erp-input-parameters-1.json"));
    epaOpProvidePrescriptionWithPzn =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/medication/Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json"));
    epaOpProvidePrescriptionWithFreeText =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/unknown/providePrescrFromEpaMockAsMedFreeText.json"));
    epaOpProvidePrescriptionWithCompounding =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/unknown/providePrescrFromEpaMockAsMedCompounding.json"));
    epaOpProvidePrescriptionWithIngredient =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/unknown/providePrescrFromEpaMockAsMedIngredient.json"));
  }

  private static Stream<Arguments>
      shouldThrowWhileValidateEpaMedicationWithMedicationFreeTextWithWrongValues() {
    return Stream.of(
        Arguments.of(
            MedicationCategory.C_01,
            "Zäpfchen, viel Spaß",
            true,
            "'Hier ist der mentale Meilenstein des LE"),
        Arguments.of(
            MedicationCategory.C_00,
            "FailDosage",
            true,
            "'Hier ist der mentale Meilenstein des LE"),
        Arguments.of(
            MedicationCategory.C_00,
            "Zäpfchen, viel Spaß",
            false,
            "'Hier ist der mentale Meilenstein des LE"),
        Arguments.of(MedicationCategory.C_00, "Zäpfchen, viel Spaß", true, "fail FreeText"));
  }

  private static Stream<Arguments>
      shouldThrowWhileValidateEpaMedicationWithMedicationCompoundingWithWrongValues() {
    return Stream.of(
        Arguments.of(
            MedicationCategory.C_01,
            "13374",
            "Vertigoheel® 20 mg",
            5,
            1,
            "Stk",
            "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00,
            "FailPzn",
            "Vertigoheel® 20 mg",
            5,
            1,
            "Stk",
            "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00, "13374", "FailName", 5, 1, "Stk", "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00,
            "13374",
            "Vertigoheel® 20 mg",
            10,
            1,
            "Stk",
            "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00,
            "13374",
            "Vertigoheel® 20 mg",
            5,
            10,
            "Stk",
            "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00,
            "13374",
            "Vertigoheel® 20 mg",
            5,
            1,
            "Fail Unit",
            "Zäpfchen, viel Spaß"),
        Arguments.of(
            MedicationCategory.C_00, "13374", "Vertigoheel® 20 mg", 5, 1, "Stk", "fail Form"));
  }

  private static Stream<Arguments>
      shouldThrowWhileValidateEpaMedicationWithMedicationIngredientWithWrongValues() {
    return Stream.of(
        Arguments.of(
            MedicationCategory.C_01,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "2",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            2,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "Fail_Unit",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Fail_Name",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N2,
            2,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            3,
            1,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            4,
            "wölkchen",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "Fail_Unit",
            "0-0-1-0-3-2"),
        Arguments.of(
            MedicationCategory.C_00,
            "1",
            1,
            "halt sowas",
            "Grippostad C® Fruchtgummi",
            StandardSize.N1,
            2,
            1,
            "wölkchen",
            "Fail_Dosage"));
  }

  @Test
  void shouldValidatePrescriptionIdCorrect() {
    val step = emlPrescriptionIdIsEqualTo(PrescriptionId.from("160.153.303.257.459"));
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidatePrescriptionId() {
    val step = emlPrescriptionIdIsEqualTo(PrescriptionId.from("Test-ID"));
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidateAuthoredOnIsCorrect() {

    val step = emlAuthoredOnIsEqualTo(testDate_22_01_2025);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateAuthoredOn() {
    val testDate = DateConverter.getInstance().localDateToDate(LocalDate.of(2020, Month.JULY, 31));
    val step = emlAuthoredOnIsEqualTo(testDate);

    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidatePznCorrect() {
    val pznMedication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication("10019621", "IBU-ratiopharm 400mg akut Schmerztabletten")
            .withAmount(50, "Tablet")
            .withStandardSize(StandardSize.N3)
            .withCategory(MedicationCategory.C_00)
            .fake();
    val step = emlMedicationMapsTo(pznMedication);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @Test
  void shouldValidateEpaMedicationWithMedicationIngredientCorrect() {
    val ingredientMed =
        KbvErpMedicationIngredientFaker.builder()
            .withCategory(MedicationCategory.C_00) // Mapped to 'Medication.extension:drugCategory'
            .withAmount("1", 1, "halt sowas")
            .withDrugName("Grippostad C® Fruchtgummi")
            .withStandardSize(StandardSize.N1)
            .withIngredientComponent(2, 1, "wölkchen")
            .fake();
    ingredientMed.setForm(new CodeableConcept(new Coding()).setText("0-0-1-0-3-2"));
    val step = emlMedicationMapsTo(ingredientMed);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescriptionWithIngredient));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowWhileValidateEpaMedicationWithMedicationIngredientWithWrongValues(
      MedicationCategory category,
      String amountNum,
      int amountDenom,
      String amountUnit,
      String drugName,
      StandardSize standardSize,
      int ingredientNum,
      int ingredientDenom,
      String ingredientUnit,
      String ingredientText) {
    val ingredientMed =
        KbvErpMedicationIngredientFaker.builder()
            .withCategory(category)
            .withAmount(amountNum, amountDenom, amountUnit)
            .withDrugName(drugName)
            .withStandardSize(standardSize)
            .withIngredientComponent(ingredientNum, ingredientDenom, ingredientUnit)
            .fake();
    ingredientMed.setForm(new CodeableConcept(new Coding()).setText(ingredientText));
    val step = emlMedicationMapsTo(ingredientMed);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithIngredient));
  }

  @Test
  void shouldValidateEpaMedicationAsATCAndPZNCorrect() {
    val medication =
        new KbvErpMedicationPZNBuilder()
            .isVaccine(true)
            .pzn(PZN.from("10019621"), "IBU-ratiopharm 400mg akut Schmerztabletten")
            .amount(4, "iss Soo")
            .build();

    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @Test
  void shouldThrowsWhileValidateEpaMedicationAsASK() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            new Coding(
                DeBasisProfilCodeSystem.ASK.getCanonicalUrl(),
                "10019621",
                "IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @Test
  void shouldThrowWhileValidateEpaMedicationAsATCAndIncorrectSystem() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            new Coding(
                DeBasisProfilCodeSystem.ATC.getCanonicalUrl(),
                "10019621",
                "IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ASK.getCanonicalUrl(), "M01AE21", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @ParameterizedTest
  @CsvSource({
    "'Invalid' ,'M01AE01'",
    "'10019621',  'invalid'",
    "'invalid',  'invalid'",
  })
  void shouldThrowWhileValidateEpaMedicationAsATCAndIncorrectPZNCode(
      String pznCode, String atcCode) {
    val medication =
        new KbvErpMedicationPZNBuilder()
            .isVaccine(true)
            .pzn(PZN.from(pznCode), "IBU-ratiopharm 400mg akut Schmerztabletten")
            .amount(4, "iss Soo")
            .build();

    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), atcCode, "Ibuprofen"));

    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @Test
  void shouldThrowWileValidateEpaMedication() {
    val step = emlMedicationMapsTo(new KbvErpMedication());
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidateEpaMedicationRequestCorrect() {
    val dispRequest =
        new MedicationRequest.MedicationRequestDispenseRequestComponent()
            .setQuantity(new Quantity().setValue(1).setSystem("http://unitsofmeasure.org"));
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medRequ =
        MedicationRequestFaker.builder()
            .withAuthorDate(new Date())
            .withMedication(medication)
            .fake();
    medRequ
        .setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
        .setAuthoredOn(testDate_22_01_2025)
        .setDispenseRequest(dispRequest);
    val step = emlMedicationRequestMapsTo(medRequ);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaMedicationRequestDate() {
    val testDate =
        DateConverter.getInstance().localDateToDate(LocalDate.of(2000, Month.JANUARY, 22));
    val dispRequest =
        new MedicationRequest.MedicationRequestDispenseRequestComponent()
            .setQuantity(new Quantity().setValue(1).setSystem("http://unitsofmeasure.org"));
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medRequ =
        MedicationRequestFaker.builder()
            .withAuthorDate(new Date())
            .withMedication(medication)
            .fake();
    medRequ
        .setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
        .setAuthoredOn(testDate)
        .setDispenseRequest(dispRequest);

    val step = emlMedicationRequestMapsTo(medRequ);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaMedicationRequestDispRequestQuantity() {
    val dispRequest =
        new MedicationRequest.MedicationRequestDispenseRequestComponent()
            .setQuantity(new Quantity().setValue(2).setSystem("http://unitsofmeasure.org"));
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medRequ =
        MedicationRequestFaker.builder()
            .withAuthorDate(new Date())
            .withMedication(medication)
            .fake();
    medRequ
        .setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
        .setAuthoredOn(testDate_22_01_2025)
        .setDispenseRequest(dispRequest);

    val step = emlMedicationRequestMapsTo(medRequ);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaMedicationRequestDispRequestSystem() {
    val dispRequest =
        new MedicationRequest.MedicationRequestDispenseRequestComponent()
            .setQuantity(new Quantity().setValue(1).setSystem("http://fakeSystem.eu"));
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medRequ =
        MedicationRequestFaker.builder()
            .withAuthorDate(new Date())
            .withMedication(medication)
            .fake();
    medRequ
        .setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
        .setAuthoredOn(testDate_22_01_2025)
        .setDispenseRequest(dispRequest);

    val step = emlMedicationRequestMapsTo(medRequ);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaMedicationRequestStatus() {
    val dispRequest =
        new MedicationRequest.MedicationRequestDispenseRequestComponent()
            .setQuantity(new Quantity().setValue(1).setSystem("http://unitsofmeasure.org"));
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medRequ =
        MedicationRequestFaker.builder()
            .withAuthorDate(new Date())
            .withMedication(medication)
            .fake();
    medRequ
        .setStatus(MedicationRequest.MedicationRequestStatus.CANCELLED)
        .setAuthoredOn(testDate_22_01_2025)
        .setDispenseRequest(dispRequest);

    val step = emlMedicationRequestMapsTo(medRequ);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidateEpaOrganisationCorrect() {
    val step = emlOrganisationHasSmcbTelematikId(TelematikID.from("9-2.58.00000040"));
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaOrganisation() {
    val step = emlOrganisationHasSmcbTelematikId(TelematikID.from("TestValue"));
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidateEpaPractitionerIdCorrect() {
    val step = emlPractitionerHasHbaTelematikId(TelematikID.from("1-1.58.00000040"));
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldThrowWileValidateEpaPractitionerId() {
    val step = emlPractitionerHasHbaTelematikId(TelematikID.from("Test-Id"));
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescription));
  }

  @Test
  void shouldValidateEpaMedicationWithMedicationCompoundingCorrect() {
    val compoundingMed =
        KbvErpMedicationCompoundingFaker.builder()
            .withCategory(MedicationCategory.C_00) // Mapped to 'Medication.extension:drugCategory'
            .withMedicationIngredient("13374", "Vertigoheel® 20 mg", "freitextPzn")
            .withAmount(5, 1, "Stk")
            .withDosageForm("Zäpfchen, viel Spaß")
            .fake();
    compoundingMed.getIngredientFirstRep().getStrength().setDenominator(new Quantity(1));
    val step = emlMedicationMapsTo(compoundingMed);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescriptionWithCompounding));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowWhileValidateEpaMedicationWithMedicationCompoundingWithWrongValues(
      MedicationCategory category,
      String ingredientPznCode,
      String ingredientPznName,
      int amountNUm,
      int amountDenom,
      String amountUnit,
      String dosage) {
    val compoundingMed =
        KbvErpMedicationCompoundingFaker.builder()
            .withCategory(category)
            .withMedicationIngredient(ingredientPznCode, ingredientPznName, "freitextPzn")
            .withAmount(amountNUm, amountDenom, amountUnit)
            .withDosageForm(dosage)
            .fake();
    compoundingMed
        .getIngredient()
        .get(0)
        .getStrength()
        .setDenominator(new Quantity(1))
        .setNumerator(new Quantity(2));
    val step = emlMedicationMapsTo(compoundingMed);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithCompounding));
  }

  @Test
  void shouldValidateEpaMedicationWithMedicationFreeTextCorrect() {
    val compoundingMed =
        KbvErpMedicationFreeTextFaker.builder()
            .withCategory(MedicationCategory.C_00) // Mapped to 'Medication.extension:drugCategory'
            .withDosageForm("Zäpfchen, viel Spaß")
            .withVaccine(false)
            .withFreeText("Hier ist der mentale Meilenstein des LE")
            .fake();
    val step = emlMedicationMapsTo(compoundingMed);
    assertDoesNotThrow(() -> step.apply(epaOpProvidePrescriptionWithFreeText));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowWhileValidateEpaMedicationWithMedicationFreeTextWithWrongValues(
      MedicationCategory category, String dosage, boolean isvVaccine, String freeText) {
    val compoundingMed =
        KbvErpMedicationFreeTextFaker.builder()
            .withCategory(category) // Mapped to 'Medication.extension:drugCategory'
            .withDosageForm(dosage)
            .withVaccine(isvVaccine)
            .withFreeText(freeText)
            .fake();
    val step = emlMedicationMapsTo(compoundingMed);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithFreeText));
  }
}
