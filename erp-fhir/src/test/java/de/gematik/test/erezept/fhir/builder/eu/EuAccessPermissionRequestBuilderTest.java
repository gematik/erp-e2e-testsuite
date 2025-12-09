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
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.Instant;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.InstantType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EuAccessPermissionRequestBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldGetCountryCode() {
    val testCods = EuAccessCode.random();
    val euAP =
        EuAccessPermissionRequestBuilder.euAccessCode(testCods)
            .countryCode(IsoCountryCode.DE)
            .build();

    assertEquals(testCods, euAP.getAccessCode());

    val res = ValidatorUtil.encodeAndValidate(parser, euAP);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldThrowWileGettingCountryCode() {
    val testCods = EuAccessCode.random();
    val euAP =
        EuAccessPermissionRequestBuilder.euAccessCode(testCods)
            .countryCode(IsoCountryCode.DE)
            .build();
    euAP.getParameter().get(0).setName("wrongname");
    euAP.getParameter().get(1).setName("wrongname");

    assertThrows(MissingFieldException.class, euAP::getAccessCode);
    assertThrows(MissingFieldException.class, euAP::getIsoCountryCode);
  }

  @Test
  void shouldGetAccessCode() {
    val tescode = IsoCountryCode.NL;
    val euAP =
        EuAccessPermissionRequestBuilder.euAccessCode(EuAccessCode.random())
            .countryCode(tescode)
            .build();

    assertEquals(tescode, euAP.getIsoCountryCode());
    val res = ValidatorUtil.encodeAndValidate(parser, euAP);
    assertTrue(res.isSuccessful());
  }

  @ParameterizedTest
  @ValueSource(strings = {"toLongg", "short", "1", "12345", "1234567", "123 456", "**//§$"})
  void shouldThrowWilesSettingWrongAccessCode(String euAccessCode) {
    val testCode = EuAccessCode.from(euAccessCode);
    assertThrows(
        IllegalArgumentException.class,
        () -> EuAccessPermissionRequestBuilder.euAccessCode(testCode));
  }

  @Test
  void shouldBuildCorrect() {
    val euAP =
        EuAccessPermissionRequestBuilder.euAccessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.DE)
            .build();

    assertNotNull(euAP);
    val result = ValidatorUtil.encodeAndValidate(parser, euAP);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithoutAccessCode() {
    val euAP =
        EuAccessPermissionRequestBuilder.withRandomAccessCode()
            .countryCode(IsoCountryCode.DE)
            .build();

    assertNotNull(euAP);
    val result = ValidatorUtil.encodeAndValidate(parser, euAP);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithInvalidAccessCode() {
    val euAP =
        EuAccessPermissionRequestBuilder.withUncheckedAccessCode(EuAccessCode.from("123"))
            .countryCode(IsoCountryCode.DE)
            .build();

    assertNotNull(euAP);
    val result = ValidatorUtil.encodeAndValidate(parser, euAP);
    assertFalse(result.isSuccessful());
  }

  @Test
  void shouldGetValidUntilCorrect() {
    val now = Instant.now();
    val inOneOur = now.plusSeconds(3600);
    val euAP = getEuAccessPermissionResponse(inOneOur, now);

    assertTrue(now.isBefore(euAP.getValidUntil().get()));
  }

  @Test
  void shouldGetCreateAtCorrect() {
    val now = Instant.now();
    val inOneOur = now.plusSeconds(3600);
    val euAP = getEuAccessPermissionResponse(inOneOur, now);

    assertTrue(inOneOur.isAfter(euAP.getCreateAt().get()));
  }

  @Test
  void shouldGetOptionalEmptyWhileGettingValidUntilCorrect() {
    val now = Instant.now();
    val euAP = getEuAccessPermissionResponse(null, now);

    assertEquals(Optional.empty(), euAP.getValidUntil());
  }

  @Test
  void shoulddGetOptionalEmptyWhileGettingCreateAtCorrect() {

    val inOneOur = Instant.now().plusSeconds(3600);
    val euAP = getEuAccessPermissionResponse(inOneOur, null);

    assertEquals(Optional.empty(), euAP.getCreateAt());
  }

  private static EuAccessPermission getEuAccessPermissionResponse(Instant until, Instant created) {
    val euAP =
        EuAccessPermissionRequestBuilder.euAccessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.DE)
            .build();

    euAP.getMeta()
        .getProfile()
        .get(0)
        .setValue(euAP.getMeta().getProfile().get(0).getValue().replace("Request", "Response"));
    if (until != null)
      euAP.addParameter()
          .setName("validUntil")
          .setValue(new InstantType().setValue(java.util.Date.from(until)));
    if (created != null)
      euAP.addParameter()
          .setName("createdAt")
          .setValue(new InstantType().setValue(java.util.Date.from(created)));
    return euAP;
  }
}
