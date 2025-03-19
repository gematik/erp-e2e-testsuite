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

package de.gematik.test.konnektor.soap.mock.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class BNetzAVLCaTest {

  @Test
  void certificateShouldNotNull() {
    for (BNetzAVLCa bNetzAVLCa : BNetzAVLCa.values()) {
      val certificate = bNetzAVLCa.getCertificate();
      assertNotNull(certificate);
    }
  }

  @Test
  void shouldThrowExceptionWhenSubjectCANotFound() {
    assertEquals(Optional.empty(), BNetzAVLCa.getBy("GEM.HBA-qCA86 TEST-ONLY"));
  }

  @Test
  void shouldReturnCorrectValueWhenSubjectCAExists() {
    val bNetzAVLCa = BNetzAVLCa.getBy("GEM.HBA-qCA6 TEST-ONLY");
    assertTrue(bNetzAVLCa.isPresent());
    assertEquals(BNetzAVLCa.GEM_HBA_QCA6_TEST_ONLY, bNetzAVLCa.get());
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldReturnCertificateWhenEECertificatesIssuerExists(CryptoSystem algorithm) {
    // excluded, as these SmartCards do NOT have an RSA-QES certificate
    val excludeList = List.of("80276001011699901343", "80276001011699901344");

    SmartcardArchive.fromResources().getHbaCards().stream()
        .filter(hba -> !excludeList.contains(hba.getIccsn()))
        .forEach(
            hba -> {
              val eeCert = hba.getQesCertificate(algorithm);
              assertNotNull(BNetzAVLCa.getBy(eeCert.getX509Certificate()));
            });
  }
}
