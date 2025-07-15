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

package de.gematik.test.erezept.fhir.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionIdExtension;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class MultiplePrescriptionIdExtensionTest {

  @Test
  void shouldAddUrnPrefixWhenMissing() {
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    String system = "test-system";

    MultiplePrescriptionIdExtension extension = MultiplePrescriptionIdExtension.with(uuid, system);
    Extension fhirExtension = extension.asExtension();
    Identifier identifier = (Identifier) fhirExtension.getValue();

    assertEquals("urn:uuid:" + uuid, identifier.getValue());
    assertEquals(system, identifier.getSystem());
  }

  @Test
  void shouldKeepUrnPrefixWhenPresent() {
    String uuid = "urn:uuid:123e4567-e89b-12d3-a456-426614174000";
    String system = "test-system";

    MultiplePrescriptionIdExtension extension = MultiplePrescriptionIdExtension.with(uuid, system);
    Extension fhirExtension = extension.asExtension();
    Identifier identifier = (Identifier) fhirExtension.getValue();

    assertEquals(uuid, identifier.getValue());
    assertEquals(system, identifier.getSystem());
  }
}
