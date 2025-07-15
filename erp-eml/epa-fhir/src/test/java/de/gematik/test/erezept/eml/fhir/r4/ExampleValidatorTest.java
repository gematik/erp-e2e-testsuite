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

package de.gematik.test.erezept.eml.fhir.r4;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
class ExampleValidatorTest extends EpaFhirParsingTest {

  @Test
  void validateAllExamples() {
    val medList = ResourceLoader.readFilesFromDirectory("fhir/valid/medication", true);
    medList.forEach(
        med -> {
          val vr = epaFhir.validate(med);
          this.printValidationResult(vr);
        });
  }

  @Test
  void validateSingleExample() {
    val medicationAsString =
        ResourceLoader.readFileFromResource(
            "fhir/valid/medication/" + "Medication-TabletSprayKombipackung.json");
    val result = epaFhir.validate(medicationAsString);
    this.printValidationResult(result);
  }
}
