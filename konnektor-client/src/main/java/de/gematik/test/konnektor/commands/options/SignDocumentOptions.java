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

package de.gematik.test.konnektor.commands.options;

import de.gematik.test.smartcard.Algorithm;
import lombok.Builder;
import lombok.Data;
import lombok.val;

@Data
@Builder
public class SignDocumentOptions {

  private String mimeType;
  private String tvMode;
  private boolean includeEContent;
  private SignatureType signatureType;
  private SigningCryptType cryptoType;

  public static SignDocumentOptions getDefaultOptions() {
    return getDefaultOptions(SigningCryptType.RSA_ECC);
  }

  public static SignDocumentOptions getDefaultOptions(SigningCryptType crypto) {
    // these are coming from erp-api
    // https://build.top.local/source/git/spezifikation/erp/api-erp/-/blob/master/docs/erp_bereitstellen.adoc
    return new SignDocumentOptionsBuilder()
        .includeEContent(true)
        .mimeType("text/plain; charset=utf-8")
        .tvMode("NONE")
        .signatureType(SignatureType.RFC_5652)
        .cryptoType(crypto)
        .build();
  }

  public static SignDocumentOptions withAlgorithm(Algorithm algorithm) {
    val cryptType = switch (algorithm) {
      case RSA_2048, RSA_PSS_2048 -> SigningCryptType.RSA;
      case ECC_256 -> SigningCryptType.ECC;
    };
    return getDefaultOptions(cryptType);
  }
}
