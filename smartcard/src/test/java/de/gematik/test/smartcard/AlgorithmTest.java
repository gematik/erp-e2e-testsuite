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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.smartcard.Algorithm.CryptographySpecification;
import de.gematik.test.smartcard.exceptions.InvalidCryptographySpecificationException;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class AlgorithmTest {

  @Test
  void shouldGetRsaFromString() {
    val rsa = List.of("R2048", "RSA_2048", "r2048", "rsa_2048");
    rsa.forEach(input -> assertEquals(Algorithm.RSA_2048, Algorithm.fromString(input)));
  }

  @Test
  void shouldGetEccFromString() {
    val rsa = List.of("E256", "ECC_256", "e256", "ecc_256");
    rsa.forEach(input -> assertEquals(Algorithm.ECC_256, Algorithm.fromString(input)));
  }

  @Test
  void shouldGetRsaPssFromString() {
    val rsa = List.of("RSASSA_PSS", "RSASSA-PSS");
    rsa.forEach(input -> assertEquals(Algorithm.RSA_PSS_2048, Algorithm.fromString(input)));
  }

  @Test
  void shouldThrowOnInvalidString() {
    val rsa = List.of("Ecc256", "ECC_512", "r256", "rsa_256");
    rsa.forEach(input -> assertThrows(AssertionError.class, () -> Algorithm.fromString(input)));
  }

  @Test
  void fromSpecificationUrn() {
    Arrays.stream(CryptographySpecification.values())
        .forEach(it -> assertDoesNotThrow(() -> Algorithm.fromSpecificationUrn(it.getUrn())));
  }

  @Test
  void shouldThrowInvalidCryptographySpecificationException() {
    assertThrows(
        InvalidCryptographySpecificationException.class,
        () -> Algorithm.fromSpecificationUrn("urn:unknown"));
  }
}
