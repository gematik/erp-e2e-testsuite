/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.smartcard;

import static java.text.MessageFormat.format;

import lombok.Getter;

@Getter
public enum Crypto {
  RSA_2048("RSA", 2048),
  ECC_256("ECC", 256);

  private final String algorithm;
  private final int keyLength;

  Crypto(String algo, int keyLength) {
    this.algorithm = algo;
    this.keyLength = keyLength;
  }

  public static Crypto fromString(String value) {
    return switch (value.toUpperCase()) {
      case "RSA_2048", "R2048" -> RSA_2048;
      case "E256", "ECC_256" -> ECC_256;
      default -> throw new AssertionError(format("Given Algorithm {0} is not supported", value));
    };
  }

  @Override
  public String toString() {
    return this.algorithm + " " + this.keyLength;
  }
}
