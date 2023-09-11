/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.client.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.testutil.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ValidationResultHelperTest {

  @Test
  void coverPrivateConstructor() {
    assertTrue(
        PrivateConstructorsUtil.throwsInvocationTargetException(ValidationResultHelper.class));
  }

  @Test
  void shouldThrowOnBaseInvalidResult() {
    val vr = FhirTestResourceUtil.createFailingValidationResult();
    assertThrows(
        FhirValidationException.class,
        () -> ValidationResultHelper.throwOnInvalidValidationResult(vr));
  }

  @Test
  void shouldNotThrowOnBaseValidResult() {
    val vr = FhirTestResourceUtil.createEmptyValidationResult();
    assertDoesNotThrow(() -> ValidationResultHelper.throwOnInvalidValidationResult(vr));
  }

  @Test
  void shouldThrowOnConcreteInvalidResult() {
    val vr = FhirTestResourceUtil.createFailingValidationResult();
    assertThrows(
        FhirValidationException.class,
        () -> ValidationResultHelper.throwOnInvalidValidationResult(KbvErpBundle.class, vr));
  }

  @Test
  void shouldNotThrowOnConcreteValidResult() {
    val vr = FhirTestResourceUtil.createEmptyValidationResult();
    assertDoesNotThrow(
        () -> ValidationResultHelper.throwOnInvalidValidationResult(KbvErpBundle.class, vr));
  }
}
