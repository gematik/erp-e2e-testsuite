/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.app.parsers;

import static de.gematik.test.erezept.app.parsers.MedicationParser.compareMedicationNames;
import static de.gematik.test.erezept.app.parsers.MedicationParser.getMedicationName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
class MedicationParserTest extends ErpFhirParsingTest {
  @ParameterizedTest
  @CsvSource(
      value = {
        "Januvia® 50 mg 28 Filmtabletten N1:fhir/valid/kbv/1.4.0/Beispiel_1.xml",
        "Olanzapin Heumann 20mg 70 Schmelztbl. N3:fhir/valid/kbv/1.4.0/Beispiel_2.xml",
        "Beloc-Zok® mite 47,5 mg, 30 Retardtabletten N1:fhir/valid/kbv/1.4.0/Beispiel_3.xml",
        "Fucidine® 20mg/g Creme 15g N1:fhir/valid/kbv/1.4.0/Beispiel_4.xml",
      },
      delimiter = ':')
  void shouldHaveTheSameMedicationNames(String expectedName, String path) {
    val pznKbvBundle = getDecodedFromPath(KbvErpBundle.class, path);
    val medication = pznKbvBundle.getMedication();

    val medicationName = getMedicationName(medication);

    log.info("Expected medication name: {}", expectedName);
    log.info("Actual medication name: {}", medicationName);
    assertTrue(compareMedicationNames(expectedName, medicationName));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "Beloc-Zok® mite 47,5 mg, 30 Retardtabletten N1:fhir/valid/kbv/1.4.0/Beispiel_4.xml",
      },
      delimiter = ':')
  void shouldNotHaveTheSameMedicationNames(String name, String path) {
    val pznKbvBundle = getDecodedFromPath(KbvErpBundle.class, path);
    val medication = pznKbvBundle.getMedication();

    val medicationName = getMedicationName(medication);

    assertFalse(compareMedicationNames(name, medicationName));
  }
}
