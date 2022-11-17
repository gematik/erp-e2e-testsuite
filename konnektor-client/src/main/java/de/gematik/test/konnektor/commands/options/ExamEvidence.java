/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.konnektor.commands.options;

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.SneakyThrows;
import lombok.val;

public enum ExamEvidence {
  UPDATES_SUCCESSFUL(1),
  NO_UPDATES(2),
  ERROR_EGK(3),
  ERROR_AUTH_CERT_INVALID(4),
  ERROR_ONLINECHECK_NOT_POSSIBLE(5),
  ERROR_OFFLINE_PERIOD_EXCEEDED(6),
  INVALID_EVIDENCE_NUMBER(-1),
  NO_UPDATE_WITH_EXPIRED_TIMESTAMP(2),
  NO_WELL_FORMED_XML(-1),
  ;

  private final DateTimeFormatter timestampFormatter =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));
  private final String template =
      "H4sIAFMA8mIA/zWN0UvDMBDG/5WSd3ur9GFIklFMBB8unW4T9EWCibV1TYcp7Zq/freBD7/vuB/cd3xz7o/Z5P9iOwTBi"
          + "nzFMh++BteGRrDD/uluzbI42uDscQhesMVHtpF8azI6DFGwn3E8PQDMMW98b8f2N3cevi1M0fVwCjNMt9JHhZ9v"
          + "+nX3XJvbG3KS73eSwzU0TWL7IWtVLSa9l7XSM3aHwqhmhUknvO6q+uceE/kOE3bVQpSomjOml5KM4EBFFEZeAOI"
          + "YaMDfAAAA";
  private final int number;
  private final String xml;

  private final Instant timestamp = Instant.now();

  ExamEvidence(int number) {
    this.number = number;
    this.xml = new String(decompress(Base64.getDecoder().decode(template)), StandardCharsets.UTF_8);
  }

  public String asXml() {
    if (this == NO_WELL_FORMED_XML) {
      return "<foo></bar>";
    }
    return replaceChecksum(xml)
        .replace("<TS></TS>", String.format("<TS>%s</TS>", genTimestamp()))
        .replace("<E></E>", String.format("<E>%s</E>", number));
  }

  public Optional<String> getChecksum() {
    val pattern = Pattern.compile("<PZ>(.*?)</PZ>");
    val matcher = pattern.matcher(this.asXml());
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  private String replaceChecksum(String xml) {
    if (number > 2) {
      return xml.replaceFirst("<PZ>.*?</PZ>", "");
    }
    return xml;
  }

  public byte[] encode() {
    return compress(asXml().getBytes(StandardCharsets.UTF_8));
  }

  public String encodeAsBase64() {
    return Base64.getEncoder().encodeToString(encode());
  }

  private String genTimestamp() {
    if (this == NO_UPDATE_WITH_EXPIRED_TIMESTAMP) {
      val minus = this.timestamp.minus(30, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS);
      return timestampFormatter.format(minus);
    }
    // example 20220808134833
    return timestampFormatter.format(timestamp);
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

  @SneakyThrows
  private byte[] decompress(byte[] bytes) {
    val output = new ByteArrayOutputStream();
    try (output;
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      output.write(gis.readAllBytes());
    }
    return output.toByteArray();
  }

  @Override
  public String toString() {
    return format("Name: {0}, TS: {1}", this.name(), genTimestamp());
  }
}
