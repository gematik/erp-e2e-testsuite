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
public class KonSimProfile implements KonnektorProfile {

  private final ProfileType type = ProfileType.KONSIM;
  private final String signaturePath = "/soap-api/SignatureService/7.5.5";
  private final String authSignaturePath = "/soap-api/AuthSignatureService/7.4.1";
  private final String certificatePath = "/soap-api/CertificateService/6.0.1";
  private final String eventPath = "/soap-api/EventService/7.2.0";
  private final String cardPath = "/soap-api/CardService/8.1.2";
  private final String cardTerminalPath = "/soap-api/CardTerminalService/1.1.0";
  private final String vsdPath = "/soap-api/fmvsdm";
  private final String encryptionPath = "/soap-api/encryptionservice";
}
