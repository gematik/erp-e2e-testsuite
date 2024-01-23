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
import lombok.val;

public abstract class InstituteSmartcard extends Smartcard {

  protected InstituteSmartcard(
      List<Supplier<SmartcardCertificate>> certificates, String iccsn, SmartcardType type) {
    super(certificates, iccsn, type);
  }

  public String getTelematikId() {
    return getAutCertificate()
        .getCertWrapper()
        .getProfessionId()
        .orElse(this.getOwner().getOrganization());
  }

  public SmartcardCertificate getEncCertificate(Algorithm algorithm) {
    val oid = getEncOid();
    return getKey(oid, algorithm)
        .orElseThrow(() -> new SmartCardKeyNotFoundException(this, oid, algorithm));
  }

  protected abstract Oid getEncOid();
}
