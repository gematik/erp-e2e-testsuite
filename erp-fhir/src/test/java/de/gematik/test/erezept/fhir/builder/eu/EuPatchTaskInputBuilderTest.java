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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.Test;

class EuPatchTaskInputBuilderTest {

  @Test
  void shouldBuildParametersWithDefaultIsRedeemableTrue() {
    Parameters parameters = EuPatchTaskInputBuilder.builder().build();

    assertNotNull(parameters);
    assertEquals(1, parameters.getParameter().size());

    var param = parameters.getParameterFirstRep();
    assertEquals("eu-isRedeemableByPatientAuthorization", param.getName());
    assertTrue(param.getValue() instanceof BooleanType);
    assertTrue(((BooleanType) param.getValue()).booleanValue());
  }

  @Test
  void shouldBuildParametersWithIsRedeemableFalse() {
    Parameters parameters = EuPatchTaskInputBuilder.builder().withIsRedeemable(false).build();

    assertNotNull(parameters);
    assertEquals(1, parameters.getParameter().size());

    var param = parameters.getParameterFirstRep();
    assertEquals("eu-isRedeemableByPatientAuthorization", param.getName());
    assertTrue(param.getValue() instanceof BooleanType);
    assertFalse(((BooleanType) param.getValue()).booleanValue());
  }

  @Test
  void shouldAcceptCustomVersion() {
    Parameters parameters = EuPatchTaskInputBuilder.builder().withVersion(EuVersion.V1_0).build();

    assertNotNull(parameters);
  }
}
