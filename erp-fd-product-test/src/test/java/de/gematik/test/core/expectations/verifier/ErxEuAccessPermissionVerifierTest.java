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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.junit.jupiter.api.Test;

class ErxEuAccessPermissionVerifierTest {

  private EuAccessPermission newPermission() {
    return new EuAccessPermission();
  }

  private EuAccessPermission withValidUntil(Instant validUntil) {
    val perm = newPermission();
    perm.addParameter().setName("validUntil").setValue(new InstantType(Date.from(validUntil)));
    return perm;
  }

  private EuAccessPermission withProfile(String profile) {
    val perm = newPermission();
    perm.getMeta().addProfile(profile);
    return perm;
  }

  private EuAccessPermission withCountry(IsoCountryCode country) {
    val perm = newPermission();
    perm.addParameter().setName("countryCode").setValue(country.asCoding());
    return perm;
  }

  private EuAccessPermission withAccessCode(EuAccessCode code) {
    val permission = newPermission();
    permission.addParameter().setName("accessCode").setValue(code.asIdentifier());
    return permission;
  }

  @Test
  void shouldVerifyValidityWithinOneHour() {
    val permission = withValidUntil(Instant.now().plusSeconds(1800));
    val step = ErxEuAccessPermissionVerifier.validUntilWithinOneHour();
    assertTrue(step.getPredicate().test(permission));
  }

  @Test
  void shouldFailValidityAfterOneHour() {
    val permission = withValidUntil(Instant.now().plus(2, ChronoUnit.HOURS));
    val step = ErxEuAccessPermissionVerifier.validUntilWithinOneHour();
    assertFalse(step.getPredicate().test(permission));
  }

  @Test
  void shouldVerifyCorrectProfile() {
    String expectedProfile =
        GemErpEuStructDef.ACCESS_AUTHORIZATION_RESPONSE.getCanonicalUrl()
            + "|"
            + VersionUtil.omitPatch(EuVersion.getDefaultVersion().getVersion());
    val permission = withProfile(expectedProfile);
    val step = ErxEuAccessPermissionVerifier.hasCorrectProfile();
    assertTrue(step.getPredicate().test(permission));
  }

  @Test
  void shouldFailWhenMissingProfile() {
    val permission = newPermission();
    val step = ErxEuAccessPermissionVerifier.hasCorrectProfile();
    assertFalse(step.getPredicate().test(permission));
  }

  @Test
  void shouldVerifyIsoCountryCorrectly() {
    val permission = withCountry(IsoCountryCode.LI);
    val step = ErxEuAccessPermissionVerifier.hasIsoCountry(IsoCountryCode.LI);
    assertTrue(step.getPredicate().test(permission));
  }

  @Test
  void shouldFailDifferentIsoCountry() {
    val permission = withCountry(IsoCountryCode.DE);
    val step = ErxEuAccessPermissionVerifier.hasIsoCountry(IsoCountryCode.LI);
    assertFalse(step.getPredicate().test(permission));
  }

  @Test
  void shouldFailWhenPermissionIsNull() {
    val step = ErxEuAccessPermissionVerifier.hasExactlyOnePermission();
    assertFalse(step.getPredicate().test(null));
  }

  @Test
  void shouldFailWhenMultiplePermissionsPresent() {
    EuAccessPermission perm = new EuAccessPermission();
    perm.addParameter().setName("accessCode").setValue(new Identifier().setValue("CODE1"));
    perm.addParameter().setName("accessCode").setValue(new Identifier().setValue("CODE2"));

    val step = ErxEuAccessPermissionVerifier.hasExactlyOnePermission();
    assertFalse(step.getPredicate().test(perm));
  }

  @Test
  void shouldVerifyPermissionWithCorrectAccessCode() {
    val expected = EuAccessCode.random();
    val permission = withAccessCode(EuAccessCode.from(expected.getValue()));
    val step = ErxEuAccessPermissionVerifier.hasPermissionWithAccessCode(expected);
    assertTrue(step.getPredicate().test(permission));
  }

  @Test
  void shouldFailPermissionWhenAccessCodeDifferent() {
    val expected = EuAccessCode.random();
    val permission = withAccessCode(EuAccessCode.from("WRONG"));
    val step = ErxEuAccessPermissionVerifier.hasPermissionWithAccessCode(expected);
    assertFalse(step.getPredicate().test(permission));
  }
}
