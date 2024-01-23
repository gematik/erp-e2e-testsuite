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

import static de.gematik.test.smartcard.Algorithm.CryptographySpecification.*;
import static java.text.MessageFormat.format;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_RSASSA_PSS;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.sha256WithRSAEncryption;
import static org.bouncycastle.asn1.x9.X9ObjectIdentifiers.ecdsa_with_SHA256;

import de.gematik.test.smartcard.exceptions.InvalidCryptographySpecificationException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@Getter
public enum Algorithm {
  RSA_2048("RSA", SPEC_RSA, sha256WithRSAEncryption, 2048),
  RSA_PSS_2048("RSASSA-PSS", SPEC_RSA, id_RSASSA_PSS, 2048),
  ECC_256("ECC", SPEC_ECC, ecdsa_with_SHA256, 256);

  public static final Algorithm DEFAULT_ALGORITHM = ECC_256;

  private final String algorithmName;

  private final int keyLength;
  private final ASN1ObjectIdentifier oid;
  private final CryptographySpecification specification;

  Algorithm(
      String algo, CryptographySpecification standard, ASN1ObjectIdentifier oid, int keyLength) {
    this.algorithmName = algo;
    this.oid = oid;
    this.keyLength = keyLength;
    this.specification = standard;
  }

  public static Algorithm fromString(String value) {
    return switch (value.toUpperCase().replace("-", "_")) {
      case "RSA_2048", "R2048", "RSA" -> RSA_2048;
      case "RSASSA_PSS" -> RSA_PSS_2048;
      case "E256", "ECC_256", "ECC", "RSA_ECC" -> ECC_256;
      default -> throw new AssertionError(format("Given Algorithm {0} is not supported", value));
    };
  }

  public static Algorithm fromOid(ASN1ObjectIdentifier oid) {
    return Arrays.stream(Algorithm.values())
        .filter(it -> it.oid.equals(oid))
        .findFirst()
        .orElseThrow(() -> new AssertionError(format("Given Oid {0} is not supported", oid)));
  }

  public static Algorithm fromSpecificationUrn(String urn) {
    val standard = fromUrn(urn);
    return Arrays.stream(Algorithm.values())
        .filter(it -> it.getSpecification().equals(standard))
        .findFirst()
        .orElseThrow(() -> new InvalidCryptographySpecificationException(urn));
  }

  @Override
  public String toString() {
    return this.algorithmName + " " + this.keyLength;
  }

  @Getter
  @AllArgsConstructor
  public enum CryptographySpecification {
    SPEC_RSA("urn:ietf:rfc:3447"),
    SPEC_ECC("urn:bsi:tr:03111:ecdsa");
    final String urn;

    public static CryptographySpecification fromUrn(String urn) {
      return Arrays.stream(values())
          .filter(it -> it.getUrn().equals(urn))
          .findFirst()
          .orElseThrow(() -> new InvalidCryptographySpecificationException(urn));
    }
  }
}
