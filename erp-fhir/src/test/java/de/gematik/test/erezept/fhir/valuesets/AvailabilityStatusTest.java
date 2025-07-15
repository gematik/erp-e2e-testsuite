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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class AvailabilityStatusTest {

  @Test
  void shouldBuildCodingWithCustomSystem() {
    val coding = AvailabilityStatus.AS_10.asCoding(ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12);
    assertEquals(
        ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12.getCanonicalUrl(), coding.getSystem());
  }

  @Test
  void shouldBuildCodingWithCustomSystemAndDisplay() {
    val coding =
        AvailabilityStatus.AS_20.asCoding(ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12, true);
    assertEquals(
        ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12.getCanonicalUrl(), coding.getSystem());
    assertNotNull(coding.getDisplay());
    assertFalse(coding.getDisplay().isEmpty());
  }

  @Test
  void shouldParseFromString() {
    val values = Arrays.asList(AvailabilityStatus.values());
    values.forEach(v -> assertEquals(v, AvailabilityStatus.fromCode(v.getCode())));
  }

  @Test
  void shouldThrowOnInvalidString() {
    assertThrows(InvalidValueSetException.class, () -> AvailabilityStatus.fromCode("91"));
  }
}
