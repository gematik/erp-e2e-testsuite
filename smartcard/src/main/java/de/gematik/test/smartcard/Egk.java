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
import java.util.List;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.encoders.Base64;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Egk extends Smartcard {

  private final String kvnr;

  public Egk(List<Supplier<SmartcardCertificate>> certificates, String iccsn, String kvnr) {
    super(certificates, iccsn, SmartcardType.EGK);
    this.kvnr = kvnr;
  }

  @SneakyThrows
  public String getPrivateKeyBase64() {
    val certificate = getAutCertificate();
    val pk = certificate.getPrivateKey();

    String pkBase64;
    try (val input = new ASN1InputStream(pk.getEncoded())) {
      val asn1Object = input.readObject();
      val asn1Sequence = ASN1Sequence.getInstance(asn1Object);
      val encapsulated = ASN1OctetString.getInstance(asn1Sequence.getObjectAt(2));
      val encapsulatedAsn1Sequence = ASN1Sequence.getInstance(encapsulated.getOctets());
      val encapsulatedPrivateKey =
          ASN1OctetString.getInstance(encapsulatedAsn1Sequence.getObjectAt(1));
      pkBase64 = Base64.toBase64String(encapsulatedPrivateKey.getOctets());
    }

    return pkBase64;
  }

  @Override
  public List<Oid> getAutOids() {
    return List.of(Oid.OID_EGK_AUT, Oid.OID_EGK_AUT_ALT);
  }
}
