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
 */

package de.gematik.test.konnektor.profile;

import lombok.Getter;

@Getter
@SuppressWarnings({"java:S1170", "java:S1075"})
public class CgmProfile implements KonnektorProfile {

  private final ProfileType type = ProfileType.CGM;
  private final String signaturePath = "/service/v75/signservice";
  private final String authSignaturePath = "/service/authsignatureservice";
  private final String certificatePath = "/service/certificateservice";
  private final String eventPath = "/service/systeminformationservice";
  private final String cardPath = "/service/cardservice";
  private final String cardTerminalPath = "/service/cardterminalservice";
  private final String vsdPath = "/service/fmvsdm";
  private final String encryptionPath = "/service/encryptionservice";
}
