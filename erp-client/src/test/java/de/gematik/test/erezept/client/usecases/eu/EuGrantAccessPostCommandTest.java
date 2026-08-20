/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.client.usecases.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.eu.EuAccessPermissionRequestBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCodeNCPeH;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuGrantAccessPostCommandTest {

  EuAccessCode testcode = EuAccessCode.from("123asd");

  private final EuAccessPermission euAccessPermission =
      EuAccessPermissionRequestBuilder.euAccessCode(testcode)
          .countryCode(IsoCountryCodeNCPeH.DE)
          .build();

  @Test
  void shouldCreateWithAccessPermissionResource() {

    val command = new EuGrantAccessPostCommand(euAccessPermission);
    val body = command.getRequestBody();
    assertInstanceOf(EuAccessPermission.class, body);
    assertTrue(GemErpEuStructDef.ACCESS_AUTHORIZATION_REQUEST.matches(body.getMeta()));
  }

  @Test
  void shouldUsePostMethodAndCorrectUrl() {
    EuGrantAccessPostCommand command =
        new EuGrantAccessPostCommand(testcode, IsoCountryCodeNCPeH.NL);

    assertEquals(de.gematik.bbriccs.rest.HttpRequestMethod.POST, command.getMethod());
    assertEquals("/$grant-eu-access-permission", command.getRequestLocator());
  }
}
