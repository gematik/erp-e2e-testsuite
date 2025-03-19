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

package de.gematik.test.konnektor.soap.mock.vsdm;

import static de.gematik.test.konnektor.soap.mock.vsdm.VsdmService.CheckDigitConfiguration.REVOKED_EGK;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.vsdm.*;
import de.gematik.bbriccs.vsdm.types.VsdmKey;
import de.gematik.bbriccs.vsdm.types.VsdmKeyVersion;
import de.gematik.bbriccs.vsdm.types.VsdmKvnr;
import de.gematik.bbriccs.vsdm.types.VsdmPatient;
import de.gematik.test.erezept.config.dto.konnektor.VsdmServiceConfiguration;
import java.time.Instant;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VsdmService {

  public enum CheckDigitConfiguration {
    DEFAULT,
    INVALID_MANUFACTURER,
    INVALID_KEY,
    INVALID_KEY_VERSION,
    INVALID_KVNR,
    REVOKED_EGK
  }

  private final byte[] hmacKey;
  private final char operator;
  private final char keyVersion;

  public static VsdmService createFrom(byte[] key, char operator, char version) {
    return new VsdmService(key, operator, version);
  }

  public static VsdmService createFrom(VsdmServiceConfiguration config) {
    return createFrom(
        Base64.getDecoder().decode(config.getHMacKey()),
        config.getOperator().charAt(0),
        config.getVersion().charAt(0));
  }

  public static VsdmService instantiateWithTestKey() {
    return createFrom(VsdmServiceConfiguration.createDefault());
  }

  public String requestCheckDigitFor(
      CheckDigitConfiguration cfg, Egk egk, VsdmCheckDigitVersion version, Instant iatTimestamp) {
    val vsdmKey = new VsdmKey(key(cfg), new VsdmKeyVersion(keyVersion(cfg), version));
    return switch (version) {
      case V1 -> requestCheckDigitForV1(cfg, egk, vsdmKey, iatTimestamp);
      case V2 -> requestCheckDigitForV2(cfg, egk, vsdmKey, iatTimestamp);
    };
  }

  private String requestCheckDigitForV2(
      CheckDigitConfiguration cfg, Egk egk, VsdmKey vsdmKey, Instant iatTimestamp) {
    val street = egk.getOwnerData().getStreet() == null ? "" : egk.getOwnerData().getStreet();
    val patient =
        new VsdmPatient(
            VsdmKvnr.from(kvnr(cfg, egk)), cfg == REVOKED_EGK, egk.getInsuranceStartDate(), street);
    val checkDigit = VsdmCheckDigitFactory.createV2(patient, operator(cfg));
    if (iatTimestamp != null) {
      checkDigit.setIatTimestamp(iatTimestamp);
    }
    return checkDigit.encrypt(vsdmKey);
  }

  private String requestCheckDigitForV1(
      CheckDigitConfiguration cfg, Egk egk, VsdmKey vsdmKey, Instant iatTimestamp) {
    val checkDigit = VsdmCheckDigitFactory.createV1(kvnr(cfg, egk), operator(cfg));
    if (iatTimestamp != null) {
      checkDigit.setIatTimestamp(iatTimestamp);
    }
    return checkDigit.sign(vsdmKey);
  }

  private String kvnr(CheckDigitConfiguration cfg, Egk egk) {
    return cfg == CheckDigitConfiguration.INVALID_KVNR ? "ABC" : egk.getKvnr();
  }

  private char keyVersion(CheckDigitConfiguration cfg) {
    return cfg == CheckDigitConfiguration.INVALID_KEY_VERSION ? '3' : keyVersion;
  }

  private char operator(CheckDigitConfiguration cfg) {
    return cfg == CheckDigitConfiguration.INVALID_MANUFACTURER ? 'Z' : operator;
  }

  private byte[] key(CheckDigitConfiguration cfg) {
    return cfg == CheckDigitConfiguration.INVALID_KEY ? new byte[32] : hmacKey;
  }
}
