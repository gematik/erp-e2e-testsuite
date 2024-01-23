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

import de.gematik.test.erezept.crypto.certificate.Oid;
import de.gematik.test.smartcard.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Hba extends InstituteSmartcard {

  public Hba(List<Supplier<SmartcardCertificate>> certificates, String iccsn) {
    super(certificates, iccsn, SmartcardType.HBA);
  }

  public SmartcardCertificate getQesCertificate(Algorithm algorithm) {
    return getKey(Oid.OID_HBA_QES, algorithm)
        .orElseThrow(() -> new SmartCardKeyNotFoundException(this, Oid.OID_HBA_QES, algorithm));
  }

  @Override
  public List<Oid> getAutOids() {
    return List.of(Oid.OID_HBA_AUT);
  }

  @Override
  protected Oid getEncOid() {
    return Oid.OID_HBA_ENC;
  }
}
