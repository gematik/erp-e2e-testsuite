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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.SetSystemProperty;

class ParserConfigurationsTest {

  @Test
  void shouldReadProfileConfiguration() {
    assertDoesNotThrow(ParserConfigurations::getInstance);
  }

  @Test
  void shouldThrowOnInvalidProfileSettingsVersion() {
    val c = ParserConfigurations.getInstance();
    assertThrows(FhirValidatorException.class, () -> c.getValidatorConfiguration("abc"));
  }

  @Test
  void shouldThrowOnMissingAppropriateProfileInSet() {
    val c = ParserConfigurations.getInstance();
    assertThrows(
        FhirValidatorException.class,
        () -> c.getAppropriateVersion(PatientenrechnungVersion.class, ErpWorkflowVersion.V1_1_1));
  }

  @Test
  void shouldThrowOnUnknownAppropriateProfile() {
    val c = ParserConfigurations.getInstance();
    val unknownCustomProfile = mock(CustomProfiles.class);
    val unknownVersion = mock(ErpWorkflowVersion.class);
    when(unknownVersion.getCustomProfile()).thenReturn(unknownCustomProfile);
    when(unknownCustomProfile.getName()).thenReturn("hello.world.profile");
    assertThrows(
        FhirValidatorException.class,
        () -> c.getAppropriateVersion(PatientenrechnungVersion.class, unknownVersion));
  }

  @Test
  void shouldThrowOnFetchingInvalidProfile() {
    val c = ParserConfigurations.getInstance();
    val profileSetting = c.getProfileSettings().get(0);
    assertThrows(
        FhirValidatorException.class, () -> profileSetting.getVersionedProfile("hello.world"));
  }

  @Test
  @SetSystemProperty(key = ParserConfigurations.SYS_PROP_TOGGLE, value = " ")
  void shouldNotHaveDefaultOnBlankValue() {
    val pc = ParserConfigurations.getInstance();
    val psc = pc.getDefaultConfiguration();
    assertTrue(psc.isEmpty());
  }
  @Test
  @SetSystemProperty(key = ParserConfigurations.SYS_PROP_TOGGLE, value = "")
  void shouldNotHaveDefaultOnEmptyValue() {
    val pc = ParserConfigurations.getInstance();
    val psc = pc.getDefaultConfiguration();
    assertTrue(psc.isEmpty());
  }

}
