/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.test.konnektor.exceptions.ParsingExamEvidenceException;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"ts", "e", "pz"})
@XmlRootElement(name = "PN")
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Slf4j
public class VsdmExamEvidence {
  // <?xml version="1.0" encoding="UTF-8" standalone="yes"?><PN
  // xmlns="http://ws.gematik.de/fa/vsdm/pnw/v1.0"
  // CDM_VERSION="1.0.0"><TS></TS><E></E><PZ>ODAyNzY4ODEwMjU1NDg0MzEzMDEwMDAwMDAwMDA2Mzg0MjMzMjAyMjA4MDgxMzQ4MzM=</PZ></PN>

  @XmlElement(name = "TS", required = true)
  private String ts;

  @XmlElement(name = "E", required = true)
  private BigInteger e;

  @XmlElement(name = "PZ")
  private String pz;

  @XmlAttribute(name = "CDM_VERSION")
  private String cdmVersion;

  public static VsdmExamEvidence parse(String examEvidenceAsBase64)
      throws ParsingExamEvidenceException {
    byte[] decode = Base64.getDecoder().decode(examEvidenceAsBase64.getBytes());
    try {
      val decompress = decompress(decode);
      val jaxbContext = JAXBContext.newInstance(VsdmExamEvidence.class);
      val jaxbUnMarshaller = jaxbContext.createUnmarshaller();
      return (VsdmExamEvidence) jaxbUnMarshaller.unmarshal(new StringReader(decompress));
    } catch (IOException | JAXBException e) {
      throw new ParsingExamEvidenceException(examEvidenceAsBase64);
    }
  }

  @SneakyThrows
  public String asXml() {
    val ret = new StringWriter();
    val jaxbContext = JAXBContext.newInstance(VsdmExamEvidence.class);
    val jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    jaxbMarshaller.marshal(this, ret);
    return ret.toString();
  }

  public Optional<String> getChecksum() {
    return pz != null ? Optional.of(pz) : Optional.empty();
  }

  public byte[] encode() {
    return compress(asXml().getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  private byte[] compress(byte[] data) {
    val baos = new ByteArrayOutputStream();
    try (baos;
        GZIPOutputStream out = new GZIPOutputStream(baos)) {
      out.write(data);
    }
    return baos.toByteArray();
  }

  private static String decompress(byte[] data) throws IOException {
    val ret = new StringBuilder();
    try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data))) {
      ret.append(new String(in.readAllBytes()));
    }
    return ret.toString();
  }

  public String encodeAsBase64() {
    return Base64.getEncoder().encodeToString(encode());
  }

  public static VsdmExamEvidenceBuilder builder(VsdmExamEvidenceResult result) {
    return new VsdmExamEvidenceBuilder(result);
  }

  @Override
  public String toString() {
    return "VsdmExamEvidence{"
        + "ts='"
        + ts
        + '\''
        + ", e="
        + e
        + ", pz='"
        + pz
        + '\''
        + ", cdmVersion='"
        + cdmVersion
        + '\''
        + '}';
  }

  @RequiredArgsConstructor
  public static class VsdmExamEvidenceBuilder {

    private final DateTimeFormatter timestampFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));

    private final VsdmExamEvidenceResult result;
    private VsdmChecksum checksum;
    private Instant timestamp = Instant.now();

    public VsdmExamEvidenceBuilder checksum(VsdmChecksum checksum) {
      this.checksum = checksum;
      return this;
    }

    public VsdmExamEvidenceBuilder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public VsdmExamEvidenceBuilder withExpiredTimestamp() {
      this.timestamp = this.timestamp.minus(30, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS);
      return this;
    }

    public VsdmExamEvidence build() {
      return new VsdmExamEvidence(
          timestampFormatter.format(timestamp),
          BigInteger.valueOf(result.getResult()),
          checksum != null ? checksum.generate() : null,
          "1.0.0");
    }
  }
}
