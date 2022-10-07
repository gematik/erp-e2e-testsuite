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

import java.io.*;
import java.nio.charset.*;
import java.text.*;
import java.time.temporal.*;
import java.util.*;
import java.util.zip.*;
import lombok.*;

@Getter
public enum ExamEvidence {
  UPDATES_SUCCESSFUL(1),
  NO_UPDATES(2),
  ERROR_EGK(3),
  ERROR_AUTH_CERT_INVALID(4),
  ERROR_ONLINECHECK_NOT_POSSIBLE(5),
  ERROR_OFFLINE_PERIOD_EXCEEDED(6),
  INVALID(-1);

  private final SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
  private final String template =
      "H4sIAFMA8mIA/zWN0UvDMBDG/5WSd3ur9GFIklFMBB8unW4T9EWCibV1TYcp7Zq/freBD7/vuB/cd3xz7o/Z5P9iOwTBi"
          + "nzFMh++BteGRrDD/uluzbI42uDscQhesMVHtpF8azI6DFGwn3E8PQDMMW98b8f2N3cevi1M0fVwCjNMt9JHhZ9v"
          + "+nX3XJvbG3KS73eSwzU0TWL7IWtVLSa9l7XSM3aHwqhmhUknvO6q+uceE/kOE3bVQpSomjOml5KM4EBFFEZeAOI"
          + "YaMDfAAAA";
  private final int number;
  private final String xml;

  private Date timestamp = new Date();

  ExamEvidence(int number) {
    this.number = number;
    this.xml = new String(decompress(Base64.getDecoder().decode(template)), StandardCharsets.UTF_8);
  }

  public String asXml() {
    return xml.replace("<TS></TS>", String.format("<TS>%s</TS>", genTimestamp()))
        .replace("<E></E>", String.format("<E>%s</E>", getNumber()));
  }

  public byte[] encode() {
    return compress(asXml().getBytes(StandardCharsets.UTF_8));
  }

  public String encodeAsBase64() {
    return Base64.getEncoder().encodeToString(encode());
  }

  public ExamEvidence withExpiredEvidence() {
    val minus = timestamp.toInstant().minus(30, ChronoUnit.MINUTES);
    this.timestamp = Date.from(minus);
    return this;
  }

  public ExamEvidence withNotYetValidEvidence() {
    val minus = timestamp.toInstant().plus(30, ChronoUnit.MINUTES);
    this.timestamp = Date.from(minus);
    return this;
  }

  private String genTimestamp() {
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
}
