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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.crypto.certificate.Oid;
import de.gematik.test.smartcard.cfg.LdapReader;
import de.gematik.test.smartcard.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@EqualsAndHashCode
@Slf4j
public abstract class Smartcard {

  private final List<Supplier<SmartcardCertificate>> certificates;
  @Getter private final String iccsn;
  @Getter private final SmartcardType type;

  protected Smartcard(
      List<Supplier<SmartcardCertificate>> certificates, String iccsn, SmartcardType type) {
    this.certificates = certificates;
    this.iccsn = iccsn;
    this.type = type;

    log.trace(format("Initialize smartcard {0} with iccsn={1}", type.name(), iccsn));
  }

  public SmartcardOwnerData getOwner() {
    val certificate = this.getAutCertificate();
    return LdapReader.getOwnerData(certificate.getX509Certificate().getSubjectDN());
  }

  public SmartcardCertificate getAutCertificate() {
    return this.getAutCertificate(Algorithm.ECC_256)
        .or(() -> getAutCertificate(Algorithm.RSA_2048))
        .or(() -> getAutCertificate(Algorithm.RSA_PSS_2048))
        .orElseThrow(() -> new SmartCardKeyNotFoundException(this));
  }

  public Optional<SmartcardCertificate> getAutCertificate(Algorithm algorithm) {
    val oids = getAutOids();
    return oids.stream()
        .map(it -> getKey(it, algorithm))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  public abstract List<Oid> getAutOids();

  public Optional<SmartcardCertificate> getKey(Oid oid, Algorithm algorithm) {
    log.debug(
        format("Look for smartcard certificate with oid={0} and algorithm={1}", oid, algorithm));
    return certificates.stream()
        .map(Supplier::get)
        .filter(it -> it.getAlgorithm() == algorithm)
        .filter(it -> it.getOid() == oid)
        .findFirst();
  }

  @Override
  public String toString() {
    return format("Smartcard {0} [iccsn={1}]", type, iccsn);
  }
}
