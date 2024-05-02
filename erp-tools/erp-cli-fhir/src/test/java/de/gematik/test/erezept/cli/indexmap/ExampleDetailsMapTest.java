/*
 * Copyright 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import java.nio.file.Path;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ExampleDetailsMapTest {

  @Test
  void shouldSerializeDetails() {
    val edm = ExampleDetailsMap.forAuthor("Gematik");

    val entry = new ExampleEntry();
    entry.setFileName("custom_file.json");
    entry.setFileType(EncodingType.JSON);
    entry.setDescription("custom file");
    entry.setValidationSuccessful(false);

    val vm = new SingleValidationMessage();
    vm.setMessage("Validation Message");
    vm.setLocationString("Error in line 123");
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);
    when(vr.getMessages()).thenReturn(List.of(vm));

    entry.setValidationResults(vr);
    entry.addResult(
        HapiValidationResult.from(
            ResultSeverityEnum.WARNING,
            "some error message",
            "Bundle.entry[0].resource.ofType(Composition).subject"));

    assertEquals(2, entry.getValidationResults().size());

    edm.addEntry(entry);

    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "idxmap");
    edm.write(emptyTargetDir);
  }

  @Test
  void shouldSerializeDetailsMapToJson() {
    val edm = ExampleDetailsMap.forAuthor("Gematik");

    val entry = new ExampleEntry();
    entry.setFileName("custom_file.json");
    entry.setFileType(EncodingType.JSON);
    entry.setDescription("custom file");
    entry.setValidationSuccessful(false);

    val vm = new SingleValidationMessage();
    vm.setMessage("Validation Message");
    vm.setLocationString("Error in line 123");
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);
    when(vr.getMessages()).thenReturn(List.of(vm));

    entry.setValidationResults(vr);
    entry.addResult(
        HapiValidationResult.from(
            ResultSeverityEnum.FATAL,
            "some error message",
            "Bundle.entry[0].resource.ofType(Composition).subject"));

    assertEquals(2, entry.getValidationResults().size());

    edm.addEntry(entry);
    val mapper = new ObjectMapper();

    assertDoesNotThrow(() -> mapper.writeValueAsString(edm));
  }

  @Test
  void shouldCreateForCurrentUser() {
    val edm = ExampleDetailsMap.forCurrentUser();
    assertEquals(System.getProperty("user.name"), edm.getMeta().getAuthor());
  }
}
