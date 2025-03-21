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
 */

package de.gematik.test.erezept.cli.indexmap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.fhir.EncodingType;
import lombok.val;
import org.junit.jupiter.api.Test;

class ExampleEntryTest {

  @Test
  void shouldSerializeEntryWithoutErrors() {
    val entry = new ExampleEntry();
    entry.setFileName("custom_file.json");
    entry.setFileType(EncodingType.JSON);
    entry.setDescription("custom file");
    entry.setValidationSuccessful(true);

    val mapper = new ObjectMapper();
    assertDoesNotThrow(() -> mapper.writeValueAsString(entry));
  }

  @Test
  void shouldSerializeEntryWithErrors() {
    val entry = new ExampleEntry();
    entry.setFileName("custom_file.json");
    entry.setFileType(EncodingType.JSON);
    entry.setDescription("custom file");
    entry.setValidationSuccessful(false);
    entry.addResult(
        HapiValidationResult.from(
            ResultSeverityEnum.ERROR,
            "some error message",
            "Bundle.entry[0].resource.ofType(Composition).subject"));

    val mapper = new ObjectMapper();
    assertDoesNotThrow(() -> mapper.writeValueAsString(entry));
  }
}
