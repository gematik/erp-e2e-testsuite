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
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestFaker;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EpaOpProvidePrescriptionVerifierTest {

  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));
  private static EpaOpProvidePrescription epaOpProvidePrescription;
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithPzn;

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
  void shouldThrowWhileOneCodingIsMissing() {
    val pznMedication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication("10019621", "IBU-ratiopharm 400mg akut Schmerztabletten")
            .fake();
    val step = emlMedicationMapsTo(pznMedication);
    assertThrows(AssertionError.class, () -> step.apply(epaOpProvidePrescriptionWithPzn));
  }

  @Test
  void shouldValidateEpaMedicationAsATCAndPZNCorrect() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
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
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            new Coding(
                DeBasisProfilCodeSystem.PZN.getCanonicalUrl(),
                pznCode,
                "IBU-ratiopharm 400mg akut Schmerztabletten"));
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
}
