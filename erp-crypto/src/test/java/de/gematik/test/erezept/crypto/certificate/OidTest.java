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

package de.gematik.test.erezept.crypto.certificate;

import java.util.*;
import org.junit.jupiter.api.*;

class OidTest {
  @Test
  void walkThroughLiterals() {
    Arrays.stream(Oid.values()).forEach(x -> Assertions.assertNotNull(x.toString()));
  }

  @Test
  void shouldReturnCorrectOid() {
    Assertions.assertEquals(Oid.OID_EGK_AUT, Oid.getByOid("1.2.276.0.76.4.70").orElseThrow());
  }

  @Test
  void shouldReturnCertTypeLiteral() {
    Assertions.assertEquals("C.CH.AUT", Oid.OID_EGK_AUT.asCertType());
  }
}
