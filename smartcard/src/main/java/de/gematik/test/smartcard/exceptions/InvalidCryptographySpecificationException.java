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

package de.gematik.test.smartcard.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.Algorithm.CryptographySpecification;
import java.util.Arrays;

public class InvalidCryptographySpecificationException extends RuntimeException {

  public InvalidCryptographySpecificationException(String urn) {
    super(
        format(
            "Given specification URN {0} is not valid. Valid Urns are {1}",
            urn,
            Arrays.stream(CryptographySpecification.values())
                .map(CryptographySpecification::getUrn)
                .toList()));
  }
}
