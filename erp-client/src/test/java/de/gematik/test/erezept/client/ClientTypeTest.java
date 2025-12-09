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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.client.exceptions.InvalidClientTypeException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClientTypeTest {

  @Test
  void shouldDetectPsFromString() {
    val inputs = List.of("Primärsystem", "primärsystem", "PS", "ps");
    inputs.forEach(input -> assertEquals(ClientType.PS, ClientType.fromString(input)));
  }

  @Test
  void shouldDetectFdvFromString() {
    val inputs = List.of("App", "Fdv", "Adv", "FDV", "ADV");
    inputs.forEach(input -> assertEquals(ClientType.FDV, ClientType.fromString(input)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ncpeh", "eu", "EU", "NCPEH", "NCPeH"})
  void shouldDetectNcpehFromString(String input) {
    assertEquals(ClientType.NCPEH, ClientType.fromString(input));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "ktr",
        "kostenträger",
        "kostentraeger",
        "krankenkasse",
        "KTR",
        "Kostenträger",
        "Krankenkasse",
        "Kostentraeger",
      })
  void shouldDetectKtrFromString(String input) {
    assertEquals(ClientType.KTR, ClientType.fromString(input));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Apps",
        "Edv",
        "Xdv",
        "Primersystem",
        "prim-sys",
        "NCPEH-Client",
        "KTR-System",
        "Kostenträger-KK",
        "Diga-App",
        "Evdga-App"
      })
  void shouldThrowOnInvalidTypes(String input) {
    assertThrows(InvalidClientTypeException.class, () -> ClientType.fromString(input));
  }

  @Test
  void shouldToStringReadableType() {
    // well, actually only for coverage
    assertEquals("Primärsystem", ClientType.PS.toString());
    assertEquals("Benutzer-App", ClientType.FDV.toString());
    assertEquals("NCPeH", ClientType.NCPEH.toString());
    assertEquals("Kostenträger", ClientType.KTR.toString());
  }
}
