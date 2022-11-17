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

import static java.text.MessageFormat.*;

import java.nio.charset.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ExamEvidenceTest {
  private final DateTimeFormatter timestampFormatter =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));

  @Test
  void generateValidBase64EncodedEvidences() {
    for (ExamEvidence examEvidence : ExamEvidence.values()) {
      Assertions.assertNotNull(examEvidence.toString());
      val evidenceAsBase64 = examEvidence.encodeAsBase64().getBytes(StandardCharsets.UTF_8);
      Assertions.assertDoesNotThrow(() -> Base64.getDecoder().decode(evidenceAsBase64));
      Assertions.assertNotNull(examEvidence.getChecksum());
    }
  }

  @SneakyThrows
  @Test
  void generateExpiredEvidence() {
    val expected = Instant.now().minus(30, ChronoUnit.MINUTES);
    val expiredTimestamp = parseTimestamp(ExamEvidence.NO_UPDATE_WITH_EXPIRED_TIMESTAMP.asXml());
    Assertions.assertTrue(
        expiredTimestamp.isBefore(expected),
        format("{0} is expected to be before {1}", expiredTimestamp, expected));
  }

  @SneakyThrows
  @Test
  void evidenceWithoutChecksum() {
    val evidences =
        List.of(
            ExamEvidence.ERROR_EGK,
            ExamEvidence.ERROR_AUTH_CERT_INVALID,
            ExamEvidence.ERROR_ONLINECHECK_NOT_POSSIBLE,
            ExamEvidence.ERROR_OFFLINE_PERIOD_EXCEEDED);
    evidences.forEach(e -> Assertions.assertFalse(e.asXml().contains("<PZ>")));
  }

  @SneakyThrows
  private Instant parseTimestamp(String evidenceAsXml) {
    val pattern = Pattern.compile("<TS>(.*?)</TS>");
    val matcher = pattern.matcher(evidenceAsXml);
    Assertions.assertTrue(matcher.find());
    return Instant.from(timestampFormatter.parse(matcher.group(1)));
  }
}