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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class EpaPractitionerTest {

  @SneakyThrows
  @Test
  void shouldResponseTelematikIdFromPractitioner() {
    val fhir = EpaFhirFactory.create();
    val path = "fhir/valid/directory/Practitioner-TIPractitionerExample001.json";
    val medicationAsString = ResourceLoader.readFileFromResource(path);
    val epaMedic = fhir.decode(EpaPractitioner.class, medicationAsString);

    assertEquals(TelematikID.from("1-1.58.00000040"), epaMedic.getTelematikId());
  }
}
