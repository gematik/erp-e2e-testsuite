/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProfileDtoTest {

  @Test
  void shouldGetVersionedProfile() {
    val dto = new ProfileDto();
    dto.setName("kbv.ita.erp");
    dto.setVersion("1.1.0");

    val versionedProfile = dto.getVersionedProfile();
    assertEquals(CustomProfiles.KBV_ITA_ERP, versionedProfile.getProfile());
    assertEquals(KbvItaErpVersion.V1_1_0, versionedProfile.getProfileVersion());
  }
}