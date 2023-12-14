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

import static java.text.MessageFormat.*;

import java.util.*;
import lombok.*;

@AllArgsConstructor
@Getter
public enum Oid {
  OID_EGK_AUT("CH.AUT", "1.2.276.0.76.4.70"),
  OID_EGK_AUT_ALT("CH.AUT", "1.2.276.0.76.4.212"),
  OID_HBA_QES("HP.QES", "1.2.276.0.76.4.72"),
  OID_HBA_ENC("HP.ENC", "1.2.276.0.76.4.74"),
  OID_HBA_AUT("HP.AUT", "1.2.276.0.76.4.75"),
  OID_SMC_B_ENC("HCI.ENC", "1.2.276.0.76.4.76"),
  OID_SMC_B_AUT("HCI.AUT", "1.2.276.0.76.4.77"),
  OID_SMC_B_OSIG("HCI.OSIG", "1.2.276.0.76.4.78"),
  ;
  private final String type;
  private final String id;

  public static Optional<Oid> getByOid(String oid) {
    return Arrays.stream(Oid.values()).filter(o -> o.getId().equals(oid)).findFirst();
  }

  public String asCertType() {
    return format("C.{0}", type);
  }

  @Override
  public String toString() {
    return format("Name: {0} Type: {1} Oid: {2}", name(), type, id);
  }
}
