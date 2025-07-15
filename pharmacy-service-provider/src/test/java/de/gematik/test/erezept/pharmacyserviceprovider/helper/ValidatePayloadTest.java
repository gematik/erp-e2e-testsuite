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

package de.gematik.test.erezept.pharmacyserviceprovider.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidatePayloadTest {

  @Test
  void stringIsNotNull() {
    assertFalse(ValidatePayload.stringIsNull("test"));
  }

  @org.junit.jupiter.api.Test
  void stringIsNull() {
    assertTrue(ValidatePayload.stringIsNull(null));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "test body from App",
        "t",
        "senseless long String without an idea what to write inside because it does not make sense"
            + " to write something senseless, but a good idea is to try to push all keys at the"
            + " same time like that: asudzigkughjevfuilgqwe678tdguzb079rhoifweksdcvn"
            + " 89wquoejedjgfbhasweupq OCERIO/=()q2zwe OPFfgWERZOZOIQUZE IROUZFTw"
            + " eIWUEHFRUZq3wuzepi IUEWZSGTuzwe iWEGRUZFqw3teoI Eiuzwer  "
      })
  void bodyHasContent(String args) {
    byte[] testByteArray = args.getBytes();
    assertTrue(ValidatePayload.bodyHasContent(testByteArray));
  }

  @org.junit.jupiter.api.Test
  void bodyIsNotNullButEmpty() {
    byte[] testByteArray = "".getBytes();
    assertFalse(ValidatePayload.bodyHasContent(testByteArray));
  }

  @org.junit.jupiter.api.Test
  void bodyIsNullExpectedfalse() {
    assertFalse(ValidatePayload.bodyHasContent(null));
  }
}
