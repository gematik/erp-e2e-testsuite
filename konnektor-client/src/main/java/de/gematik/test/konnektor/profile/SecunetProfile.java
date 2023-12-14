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

package de.gematik.test.konnektor.profile;

import lombok.Getter;

@Getter
@SuppressWarnings({"java:S1170", "java:S1075"})
public class SecunetProfile implements KonnektorProfile {

  private final ProfileType type = ProfileType.SECUNET;
  private final String signaturePath = "/ws/SignatureService";
  private final String authSignaturePath = "/ws/AuthSignatureService";
  private final String certificatePath = "/ws/CertificateService";
  private final String eventPath = "/ws/EventService";
  private final String cardPath = "/ws/CardService";
  private final String cardTerminalPath = "/ws/CardTerminalService";
  private final String vsdPath = "/ws/VSDService";
  private final String encryptionPath = "/service/encryptionservice";
}
