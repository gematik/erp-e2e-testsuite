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

package de.gematik.test.konnektor.profile;

import static org.junit.Assert.assertTrue;

import java.util.List;
import lombok.val;
import org.junit.Test;

public class KonnektorProfileTest {

  @Test
  public void shouldContainKeywordInPaths() {
    // Well, does not test much but should raise the coverage slightly
    // different Konnektor-Profiles use quite different endpoints for SOAP services
    val inputs = List.of("KonSim", "SECUNET", "RISE", "CGM");
    inputs.stream()
        .map(ProfileType::fromString)
        .map(ProfileType::createProfile)
        .forEach(
            profile -> {
              assertTrue(profile.getSignaturePath().toLowerCase().matches(".*sign.*service.*"));
              assertTrue(
                  profile.getAuthSignaturePath().toLowerCase().contains("authsignatureservice"));
              assertTrue(
                  profile.getAuthSignaturePath().toLowerCase().contains("authsignatureservice"));
              assertTrue(profile.getCertificatePath().toLowerCase().contains("certificateservice"));
              assertTrue(
                  profile
                      .getEventPath()
                      .toLowerCase()
                      .matches(".*(eventservice|systeminformationservice).*"));
              assertTrue(
                  profile.getCardTerminalPath().toLowerCase().contains("cardterminalservice"));
            });
  }
}
