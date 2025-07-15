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

package de.gematik.test.erezept.actions.rawhttpactions.pki;

import static org.reflections.Reflections.log;

import de.gematik.test.erezept.actions.rawhttpactions.pki.dto.OcspExtensionInfo;
import de.gematik.test.erezept.actions.rawhttpactions.pki.dto.OcspResponseInfo;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;

public class OCSPBodyWrapper {
  private final byte[] ocspResponseBody;
  private OCSPResp ocspResp;

  public OCSPBodyWrapper(byte[] byteBody) {
    this.ocspResponseBody = byteBody;
  }

  public String bodyAsString() {
    return new String(ocspResponseBody);
  }

  public Optional<OCSPResp> getOcspResp() {
    if (ocspResp == null) {
      try {
        ocspResp = new OCSPResp(Base64.getDecoder().decode(this.ocspResponseBody));
      } catch (Exception e) {
        log.info("Invalid OCSP Content");
        return Optional.empty();
      }
    }
    return Optional.of(ocspResp);
  }

  @SneakyThrows
  public Optional<OcspResponseInfo> decodeOcspResponse() {
    getOcspResp();
    if (this.ocspResp != null) {
      OcspResponseInfo respInfo = null;
      if (ocspResp.getStatus() < 1) {
        val basicResp = (BasicOCSPResp) ocspResp.getResponseObject();
        val singleResp = basicResp.getResponses()[0];
        val oids = (List<ASN1ObjectIdentifier>) singleResp.getExtensionOIDs();
        val ext =
            oids.stream()
                .map(
                    number -> {
                      Extension extension = singleResp.getExtension(number);
                      return new OcspExtensionInfo(
                          number.toString(),
                          extension.isCritical(),
                          extension.getExtnValue().toString());
                    })
                .toList();
        respInfo =
            new OcspResponseInfo(
                ext,
                singleResp.getCertStatus() != null ? singleResp.getCertStatus().toString() : "GOOD",
                singleResp.getThisUpdate(),
                singleResp.getNextUpdate());
      }
      return Optional.of(respInfo);
    } else return Optional.empty();
  }

  /**
   * Null is in RFC 6960 href: https://datatracker.ietf.org/doc/html/rfc6960#section-4.2.1 definiert
   * CertStatus ::= CHOICE { good [0] IMPLICIT NULL, revoked [1] IMPLICIT RevokedInfo, unknown [2]
   * IMPLICIT UnknownInfo }
   *
   * @return GOOD (if Status is null)
   */
  public OCSPResponseStatus getStatus() {
    return this.getOcspResp()
        .map(o -> o.toASN1Structure().getResponseStatus())
        .orElse(new OCSPResponseStatus(OCSPResponseStatus.SUCCESSFUL));
  }
}
