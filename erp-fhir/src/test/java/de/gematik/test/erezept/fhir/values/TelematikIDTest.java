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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import java.util.InputMismatchException;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TelematikIDTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://gematik.de/fhir/sid/telematik-id",
        "https://gematik.de/fhir/NamingSystem/TelematikID"
      })
  void shouldGenerateFormIdentifier(String system) {
    val value = "123.546.789";
    Identifier identifier = new Identifier();
    identifier.setSystem(system);
    identifier.setValue(value);
    val telematikId = TelematikID.from(identifier);
    assertEquals(value, telematikId.getValue());
    assertEquals(system, telematikId.getSystem().getCanonicalUrl());
  }

  @Test
  void shouldThrowFormIdentifierWithWrongNamingSystem() {
    val system = "https://gematik.de/fhir/NamingSystem/Secret";
    val value = "123.546.789";
    Identifier identifier = new Identifier();
    identifier.setSystem(system);
    identifier.setValue(value);
    assertThrows(InputMismatchException.class, () -> TelematikID.from(identifier));
  }

  @Test
  void shouldBuildFromString() {
    val telemId = TelematikID.from("123");
    assertEquals("123", telemId.getValue());
  }
}
