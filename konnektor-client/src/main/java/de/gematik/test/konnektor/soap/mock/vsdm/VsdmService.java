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

package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.test.erezept.config.dto.konnektor.VsdmServiceConfiguration;
import de.gematik.test.smartcard.Egk;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Base64;

@RequiredArgsConstructor()
@Getter
public class VsdmService {

  private final byte[] hMacKey;
  private final char operator;
  private final char version;

  public VsdmChecksum requestFor(@NonNull Egk egk, VsdmUpdateReason updateReason) {
    return VsdmChecksum.builder(egk.getKvnr())
        .identifier(operator)
        .version(version)
        .updateReason(updateReason)
        .key(hMacKey)
        .build();
  }

  public static VsdmService createFrom(VsdmServiceConfiguration config) {
    return new VsdmService(
            Base64.getDecoder().decode(config.getHMacKey()), config.getOperator().charAt(0), config.getVersion().charAt(0));
  }
}
