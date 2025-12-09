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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErxMedicationDispenseBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_4 = "fhir/valid/erp/1.4.0/medicationdispensebundle/";
  ErxMedicationDispenseBundle multipleBundle =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/eu/09_response_get_multiple_medication_dispense.json"));
  ErxMedicationDispenseBundle singleBundle =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/eu/08_response_get_single_medicationdispense.json"));

  static Stream<Arguments> shouldReadGemMedicationsFromBundle() {
    return Stream.of(
        arguments("Bundle-SimpleMedicationDispenseBundle.json", 1, 1),
        arguments("Bundle-KomplexMedicationDispenseBundle.json", 1, 1),
        arguments("Bundle-MultipleMedicationDispenseBundle.json", 2, 2),
        arguments("Bundle-SearchSetMultipleMedicationDispenseBundle.json", 2, 3));
  }

  static Stream<Arguments> shouldReadDispensationPairsByPrescriptionId() {
    return Stream.of(
        arguments(
            PrescriptionId.from("160.000.000.000.000.01"),
            "Bundle-SimpleMedicationDispenseBundle.json",
            1),
        arguments(
            PrescriptionId.from("160.000.000.000.000.01"),
            "Bundle-MultipleMedicationDispenseBundle.json",
            1),
        arguments(
            PrescriptionId.from("160.000.000.000.000.02"),
            "Bundle-MultipleMedicationDispenseBundle.json",
            1),
        arguments(
            PrescriptionId.from("160.000.000.000.000.03"),
            "Bundle-KomplexMedicationDispenseBundle.json",
            1),
        arguments(
            PrescriptionId.from("160.000.000.000.000.01"),
            "Bundle-SearchSetMultipleMedicationDispenseBundle.json",
            1),
        arguments(
            PrescriptionId.from("160.000.000.000.000.02"),
            "Bundle-SearchSetMultipleMedicationDispenseBundle.json",
            1));
  }

  static Stream<Arguments> shouldUnpackOldMedicationDispenses() {
    return Stream.of(
        arguments(
            PrescriptionId.from("160.000.000.000.000.04"),
            "Bundle-SearchSetMultipleMedicationDispenseBundle.json",
            0),
        arguments(
            PrescriptionId.from("160.000.000.000.000.05"),
            "Bundle-SearchSetMultipleMedicationDispenseBundle.json",
            1));
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadGemMedicationsFromBundle(
      String fileName, int expectedMedications, int expectedMedicationDispenses) {
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    assertEquals(expectedMedications, bundle.getMedications().size());
    assertEquals(expectedMedicationDispenses, bundle.getMedicationDispenses().size());
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadDispensationPairsByPrescriptionId(
      PrescriptionId prescriptionId, String fileName, int expectedMedications) {
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val pairs = assertDoesNotThrow(() -> bundle.getDispensePairBy(prescriptionId));
    assertEquals(expectedMedications, pairs.size());
  }

  @ParameterizedTest
  @MethodSource
  void shouldUnpackOldMedicationDispenses(
      PrescriptionId prescriptionId, String fileName, int expectedMedications) {
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val pairs = assertDoesNotThrow(() -> bundle.unpackDispensePairBy(prescriptionId));
    assertEquals(expectedMedications, pairs.size());
  }

  @Test
  void shouldThrowOnUnpackingNewMedicationDispensesByPrescriptionId() {
    val prescriptionId = PrescriptionId.from("160.000.000.000.000.01");
    val fileName = "Bundle-SimpleMedicationDispenseBundle.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    assertThrows(RuntimeException.class, () -> bundle.unpackDispensePairBy(prescriptionId));
  }

  // Tests for EuMedicatioDispense

  @Test
  void shouldSkipContainedKbvMedications() {
    // Note: these require an additional method for fetching contained medications in
    // ErxMedicationDispense
    val prescriptionId = PrescriptionId.from("160.000.000.000.000.04");
    val fileName = "Bundle-SearchSetMultipleMedicationDispenseBundle.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val pairs = assertDoesNotThrow(() -> bundle.getDispensePairBy(prescriptionId));
    assertTrue(pairs.isEmpty());
  }

  @Test
  void shouldReturnEmptyListOnMissingPrescriptionId() {
    val prescriptionId = PrescriptionId.from("160.000.000.000.000.10");
    val fileName = "Bundle-SearchSetMultipleMedicationDispenseBundle.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val l1 = assertDoesNotThrow(() -> bundle.getDispensePairBy(prescriptionId));
    assertTrue(l1.isEmpty());

    val l2 = assertDoesNotThrow(() -> bundle.unpackDispensePairBy(prescriptionId));
    assertTrue(l2.isEmpty());
  }

  @Test
  void shouldExtractEuMedDspCorrect() {
    val dispensations = multipleBundle.getEuMedicationDispenses();
    assertEquals(1, dispensations.size());
    assertEquals("160.000.000.000.000.01", dispensations.get(0).getPrescriptionId().getValue());
    assertEquals("160.000.000.000.000.01", dispensations.get(0).getId().split("/")[1]);
  }

  @Test
  void shouldExtractErxMedDispensesCorrect() {
    val dispensations = multipleBundle.getMedicationDispenses();
    assertEquals(1, dispensations.size());
    assertEquals("160.000.000.000.000.01", dispensations.get(0).getPrescriptionId().getValue());
    assertEquals("160.000.000.000.000.02", dispensations.get(0).getId().split("/")[1]);
  }

  @Test
  void shouldFindNoEuMedicationByPrescriptionIdWhileAbsent() {
    val euMedications =
        multipleBundle.getEuMedicationBy(PrescriptionId.from("160.000.000.000.000.01"));
    assertEquals(0, euMedications.size());
  }

  @Test
  void shouldFindEuMedicationByDspCorrect() {
    val md = singleBundle.getEuMedicationBy(PrescriptionId.from("160.000.000.000.000.01"));
    assertEquals(1, md.size());

    assertNotNull(md.get(0));
    assertEquals(
        "SumatripanMedication-EU", md.stream().findFirst().orElseThrow().getId().split("/")[1]);
  }

  @Test
  void shouldFinGemMedicationCorrect() {
    val medications = multipleBundle.getMedications();
    assertEquals(2, medications.size());
  }

  @Test
  void getEuPractitionerCorrect() {
    val pract = singleBundle.getEuPractitionerBy(PrescriptionId.from("160.000.000.000.000.01"));
    assertEquals("a7adde1a-af5c-4814-8fea-e46e7e63ed07", pract.get(0).getId().split("/")[1]);
  }

  @Test
  void shouldGetEuPractitionerCorrectByMedicationDispense() {
    val medDsp =
        singleBundle
            .getEuMedicationDispenseBy(PrescriptionId.from("160.000.000.000.000.01"))
            .stream()
            .findFirst()
            .orElseThrow();
    val pract = singleBundle.getEuPractitionerBy(medDsp);
    assertEquals("a7adde1a-af5c-4814-8fea-e46e7e63ed07", pract.getId().split("/")[1]);
  }

  @Test
  void shouldFailWhileGetEuPractitionerCorrectByMedicationDispense() {
    val medDsp =
        multipleBundle
            .getEuMedicationDispenseBy(PrescriptionId.from("160.000.000.000.000.01"))
            .stream()
            .findFirst()
            .orElseThrow();
    assertThrows(MissingFieldException.class, () -> singleBundle.getEuPractitionerBy(medDsp));
  }

  @Test
  void shouldGetPractitionerRoleCorrect() {
    val medDsp =
        multipleBundle
            .getEuMedicationDispenseBy(PrescriptionId.from("160.000.000.000.000.01"))
            .stream()
            .toList();
    val pracRole =
        multipleBundle.getEuPractitionerRoleTo(medDsp.stream().findFirst().orElseThrow());
    assertEquals(
        "ebe39d92-276b-436d-a9ea-9dd5e042637b", pracRole.orElseThrow().getId().split("/")[1]);
  }

  @Test
  void shouldFailWhileGettingPractitionerRole() {
    val medDsp =
        singleBundle.getEuMedicationDispenseBy(PrescriptionId.from("160.000.000.000.000.01"));
    val pracRole =
        multipleBundle.getEuPractitionerRoleTo(medDsp.stream().findFirst().orElseThrow());
    assertTrue(pracRole.isEmpty());
  }

  @Test
  void shouldGetOrganizationCorrect() {
    val organiz = multipleBundle.getEuOrganizations();
    assertEquals("6a3c8c57-0870-476e-90e3-25b7562799d3", organiz.get(0).getId().split("/")[1]);
  }

  @Test
  void getWhenHandedOverCorrect() {
    val whenHandedOver =
        multipleBundle.getEuWhenHandedOver(PrescriptionId.from("160.000.000.000.000.01"));
    assertEquals(LocalDate.of(2025, 10, 01), whenHandedOver.orElseThrow());
  }

  @Test
  void shouldGetPairBy() {
    val result = singleBundle.getEuDispensePairBy(PrescriptionId.from("160.000.000.000.000.01"));
    assertNotNull(result);
  }

  @Test
  void shouldThrowWhileMedicationReferenceIsNotCorrect() {
    val manipulatedBundle = singleBundle;
    manipulatedBundle.getMedications().get(0).setId("123Faill-Id");
    val prId = PrescriptionId.from("160.000.000.000.000.01");
    assertThrows(MissingFieldException.class, () -> manipulatedBundle.getEuDispensePairBy(prId));
  }
}
