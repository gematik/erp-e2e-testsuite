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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExamEvidenceTest {
  private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  @Test
  void generateValidBase64EncodedEvidences() {
    for (ExamEvidence examEvidence : ExamEvidence.values()) {
      val evidenceAsBase64 = examEvidence.encodeAsBase64().getBytes(StandardCharsets.UTF_8);
      Assertions.assertDoesNotThrow(() -> Base64.getDecoder().decode(evidenceAsBase64));
    }
  }

  @SneakyThrows
  @Test
  void generateExpiredEvidence() {
    val expected =
        new Date().toInstant().minus(29, ChronoUnit.MINUTES).minus(59, ChronoUnit.SECONDS);
    val timestamp =
        parseTimestamp(ExamEvidence.UPDATES_SUCCESSFUL.withExpiredEvidence().asXml())
            .minus(30, ChronoUnit.MINUTES);
    Assertions.assertTrue(timestamp.isBefore(expected));
  }

  @SneakyThrows
  @Test
  void generateNotYetValidEvidence() {
    val expected = new Date().toInstant().plus(29, ChronoUnit.MINUTES).plus(40, ChronoUnit.SECONDS);
    val timestamp =
        parseTimestamp(ExamEvidence.UPDATES_SUCCESSFUL.withNotYetValidEvidence().asXml())
            .plus(30, ChronoUnit.MINUTES);
    Assertions.assertTrue(
        timestamp.isAfter(expected),
        format("{0} is expected to be after {1}", timestamp, expected));
  }

  @SneakyThrows
  private Instant parseTimestamp(String evidenceAsXml) {
    val pattern = Pattern.compile("<TS>(.*?)</TS>");
    val matcher = pattern.matcher(evidenceAsXml);
    Assertions.assertTrue(matcher.find());
    return TIMESTAMP_FORMAT.parse(matcher.group(1)).toInstant();
  }
}
