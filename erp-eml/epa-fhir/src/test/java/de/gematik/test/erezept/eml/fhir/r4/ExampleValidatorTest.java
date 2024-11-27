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

package de.gematik.test.erezept.eml.fhir.r4;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.SingleValidationMessage;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

@Slf4j
class ExampleValidatorTest {
  private static final boolean SHOULD_PRINT = false;

  @Test
  void validateAllExamples() {
    val fhir = EpaFhirFactory.create();
    val medList = ResourceLoader.readFilesFromDirectory("fhir/valid/medication", true);
    val resultList = new HashMap<String, List<SingleValidationMessage>>();
    var counter = 0;
    if (SHOULD_PRINT) {
      for (val med : medList) {
        counter++;
        val vr = fhir.validate(med);
        if (!vr.isSuccessful()) {
          resultList.put(
              "\n no: " + counter + "  " + med.substring(0, 100) + "   :  \n", vr.getMessages());
          log.info("\nMedication:\n" + med + "Message:\n" + vr.getMessages());
        }
      }
    }
  }

  @Ignore
  void validateSingleExample() {
    val medicationAsString =
        ResourceLoader.readFileFromResource(
            "fhir/valid/medication/" + "Medication-TabletSprayKombipackung.json");
    val fhir = EpaFhirFactory.create();
    val result = fhir.validate(medicationAsString);
    if (SHOULD_PRINT) {
      for (val message : result.getMessages()) {
        log.info(
            format(
                "\n"
                    + "ValidationResult: \n"
                    + "{0} \n"
                    + "of Medication with Message:\n"
                    + "{1} \n"
                    + " Count of Messages: {2}",
                message.getSeverity(), message.getMessage(), result.getMessages().size()));
      }
    }
  }
}
