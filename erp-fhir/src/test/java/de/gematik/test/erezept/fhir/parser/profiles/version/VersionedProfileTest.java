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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import lombok.val;
import org.junit.jupiter.api.Test;

class VersionedProfileTest {

  @Test
  void shouldDetectEqualProfiles() {
    val vp1 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_0_2);
    val vp2 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_0_2);

    assertEquals(vp1, vp2);
  }

  @Test
  void shouldDetectUnEqualProfiles() {
    val vp1 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_0_2);
    val vp2 = new VersionedProfile<>(CustomProfiles.KBV_BASIS, KbvBasisVersion.V1_3_0);

    assertNotEquals(vp1, vp2);
  }

  @Test
  void shouldDetectUnEqualProfiles01() {
    val vp1 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_0_2);
    val vp2 = new VersionedProfile<>(CustomProfiles.KBV_BASIS, KbvItaErpVersion.V1_0_2);

    assertNotEquals(vp1, vp2);
  }

  @Test
  void shouldDetectUnEqualProfilesViaVersion() {
    val vp1 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_0_2);
    val vp2 = new VersionedProfile<>(CustomProfiles.KBV_ITA_ERP, KbvItaErpVersion.V1_1_0);

    assertNotEquals(vp1, vp2);
  }
}
