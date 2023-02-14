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

package de.gematik.test.konnektor.commands.options;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyDocumentOptions {

  private String tvMode;
  private SignatureType signatureType;
  private boolean includeVerifier;
  private boolean includeCertificateValue;
  private boolean includeRevocationValue;
  private boolean includeRevocationInfo;
  private boolean expandBinaryValues;

  public static VerifyDocumentOptions getDefaultOptions() {
    return new VerifyDocumentOptionsBuilder()
        .tvMode("NONE")
        .signatureType(SignatureType.RFC_5652)
        .includeVerifier(true)
        .includeCertificateValue(true)
        .includeRevocationValue(true)
        .includeRevocationInfo(false)
        .expandBinaryValues(false)
        .build();
  }
}
