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
 */

package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.test.konnektor.exceptions.ParsingExamEvidenceException;
import de.gematik.test.konnektor.soap.mock.utils.CdmVersion;
import de.gematik.test.konnektor.soap.mock.utils.XmlEncoder;
import de.gematik.ws.fa.vsds.PN;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor
@Slf4j
public class VsdmExamEvidence {

  private final PN pn;

  public Optional<String> getCheckDigit() {
    return pn.getPZ() != null
        ? Optional.of(Base64.getEncoder().encodeToString(pn.getPZ()))
        : Optional.empty();
  }

  public static VsdmExamEvidenceBuilder asOnlineMode(VsdmService service, Egk egk) {
    return new VsdmExamEvidenceBuilder(service, egk);
  }

  public static VsdmExamEvidenceBuilder asOfflineMode() {
    return new VsdmExamEvidenceBuilder();
  }

  public static VsdmExamEvidence parse(String base64) {
    try {
      val pn = XmlEncoder.parse(PN.class, base64);
      log.debug("VsdmExamEvidence: {}", XmlEncoder.asXml(pn).replace("\n", ""));
      if (pn.getPZ() != null && pn.getPZ().length > 0) {
        val version = VsdmCheckDigitVersion.fromData(pn.getPZ());
        log.debug("Checkdigit Version: {}", version);
      }
      return new VsdmExamEvidence(pn);
    } catch (JAXBException | IOException e) {
      throw new ParsingExamEvidenceException(base64, e);
    }
  }

  public String encode() {
    return XmlEncoder.encode(pn);
  }

  public String asXml() {
    return XmlEncoder.asXml(pn);
  }

  public static class VsdmExamEvidenceBuilder {

    private final VsdmService vsdmService;
    private final Egk egk;

    private final DateTimeFormatter timestampFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));

    private Instant iatTimestamp = Instant.now();
    private VsdmService.CheckDigitConfiguration checkDigitCfg =
        VsdmService.CheckDigitConfiguration.DEFAULT;
    private VsdmCheckDigitVersion checkDigitVersion = VsdmCheckDigitVersion.V2;

    protected VsdmExamEvidenceBuilder(VsdmService service, Egk egk) {
      this.vsdmService = service;
      this.egk = egk;
    }

    protected VsdmExamEvidenceBuilder() {
      this(null, null);
    }

    public VsdmExamEvidenceBuilder withExpiredIatTimestamp() {
      this.iatTimestamp =
          this.iatTimestamp.minus(30, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS);
      return this;
    }

    public VsdmExamEvidenceBuilder withInvalidIatTimestamp() {
      this.iatTimestamp = this.iatTimestamp.plus(31, ChronoUnit.MINUTES);
      return this;
    }

    public VsdmExamEvidenceBuilder with(VsdmService.CheckDigitConfiguration cfg) {
      this.checkDigitCfg = cfg;
      return this;
    }

    public VsdmExamEvidenceBuilder with(VsdmCheckDigitVersion version) {
      this.checkDigitVersion = version;
      return this;
    }

    private boolean isOnlineMode() {
      return vsdmService != null;
    }

    public VsdmExamEvidence build(VsdmExamEvidenceResult result) {
      val pn = new PN();
      pn.setE(BigInteger.valueOf(result.getResult()));
      pn.setTS(timestampFormatter.format(iatTimestamp));
      pn.setCDMVERSION(CdmVersion.V1.getVersion());
      pn.setPZ(
          isOnlineMode()
              ? Base64.getDecoder()
                  .decode(
                      vsdmService.requestCheckDigitFor(
                          checkDigitCfg, egk, checkDigitVersion, iatTimestamp))
              : null);
      return new VsdmExamEvidence(pn);
    }
  }
}
