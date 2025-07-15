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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErxMedicationDispenseBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_4 = "fhir/valid/erp/1.4.0/medicationdispensebundle/";

  static Stream<Arguments> shouldReadGemMedicationsFromBundle() {
    return Stream.of(
        arguments("Bundle-SimpleMedicationDispenseBundle.json", 1, 1),
        arguments("Bundle-KomplexMedicationDispenseBundle.json", 1, 1),
        arguments("Bundle-MultipleMedicationDispenseBundle.json", 2, 2),
        arguments("Bundle-SearchSetMultipleMedicationDispenseBundle.json", 2, 3));
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

  @ParameterizedTest
  @MethodSource
  void shouldReadDispensationPairsByPrescriptionId(
      PrescriptionId prescriptionId, String fileName, int expectedMedications) {
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val pairs = assertDoesNotThrow(() -> bundle.getDispensePairBy(prescriptionId));
    assertEquals(expectedMedications, pairs.size());
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
}
