/*
 * Copyright 2024 gematik GmbH
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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.konnektor.exceptions.ParsingExamEvidenceException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.regex.Pattern;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VsdmExamEvidenceTest {

  private static Egk egk;

  @BeforeAll
  public static void setup() {
    val archive = SmartcardArchive.fromResources();
    egk = archive.getEgk(0);
  }

  private final DateTimeFormatter timestampFormatter =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));

  @Test
  void generateValidBase64EncodedEvidences() {
    for (val result : VsdmExamEvidenceResult.values()) {
      val examEvidence = VsdmExamEvidence.asOfflineMode().generate(result);
      assertNotNull(examEvidence.asXml());
      assertNotNull(examEvidence.encode());
      val evidenceAsBase64 = examEvidence.encodeAsBase64().getBytes(StandardCharsets.UTF_8);
      assertDoesNotThrow(() -> Base64.getDecoder().decode(evidenceAsBase64));
    }
  }

  @Test
  void withExpiredTimestamp() {
    val expected = Instant.now().minus(30, ChronoUnit.MINUTES);
    var examEvidence =
        VsdmExamEvidence.asOnlineTestMode(egk)
            .withExpiredTimestamp()
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    var expiredTimestamp = parseTimestamp(examEvidence.asXml());

    assertTrue(
        expiredTimestamp.isBefore(expected),
        format("{0} is expected to be before {1}", expiredTimestamp, expected));

    examEvidence =
        VsdmExamEvidence.asOfflineMode()
            .withExpiredTimestamp()
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    expiredTimestamp = parseTimestamp(examEvidence.asXml());
    assertTrue(
        expiredTimestamp.isBefore(expected),
        format("{0} is expected to be before {1}", expiredTimestamp, expected));
  }

  @Test
  void withInvalidTimestamp() {
    val expected = Instant.now().plus(31, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS);

    var expiredTimestamp =
        parseTimestamp(
            VsdmExamEvidence.asOnlineTestMode(egk)
                .withInvalidTimestamp()
                .generate(VsdmExamEvidenceResult.NO_UPDATES)
                .asXml());

    assertTrue(
        expiredTimestamp.isAfter(expected),
        format("{0} is expected to be after {1}", expiredTimestamp, expected));

    expiredTimestamp =
        parseTimestamp(
            VsdmExamEvidence.asOfflineMode()
                .withInvalidTimestamp()
                .generate(VsdmExamEvidenceResult.NO_UPDATES)
                .asXml());
    assertTrue(
        expiredTimestamp.isAfter(expected),
        format("{0} is expected to be after {1}", expiredTimestamp, expected));
  }

  @Test
  void evidenceWithoutChecksum() {
    val examEvidence = VsdmExamEvidence.asOfflineMode().generate(VsdmExamEvidenceResult.ERROR_EGK);
    assertFalse(examEvidence.asXml().contains("<PZ>"));
  }

  private Instant parseTimestamp(String evidenceAsXml) {
    val pattern = Pattern.compile("<TS>(.*?)</TS>");
    val matcher = pattern.matcher(evidenceAsXml);
    assertTrue(matcher.find());
    return Instant.from(timestampFormatter.parse(matcher.group(1)));
  }

  @ParameterizedTest
  @EnumSource(VsdmExamEvidenceResult.class)
  void validExamEvidenceParsing(VsdmExamEvidenceResult result) {
    val evidence = VsdmExamEvidence.asOfflineMode().generate(result);
    val evidenceAsBase64 = evidence.encodeAsBase64();
    assertDoesNotThrow(() -> VsdmExamEvidence.parse(evidenceAsBase64));
  }

  @Test
  void invalidExamEvidenceParsing() {
    assertThrows(ParsingExamEvidenceException.class, () -> VsdmExamEvidence.parse("abc"));
  }

  @Test
  void checksumWithInvalidManufacturer() {
    val evidence =
        VsdmExamEvidence.asOnlineTestMode(egk)
            .checksumWithInvalidManufacturer()
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    // the manufacturer identifier defined for gematik for the HMacKey is 's'
    assertEquals('y', VsdmChecksum.parse(evidence.getChecksum().orElseThrow()).getIdentifier());
    assertDoesNotThrow(
        () ->
            VsdmExamEvidence.asOfflineMode()
                .checksumWithInvalidManufacturer()
                .generate(VsdmExamEvidenceResult.NO_UPDATES));
  }

  @Test
  void checksumWithInvalidVersion() {
    val evidence =
        VsdmExamEvidence.asOnlineTestMode(egk)
            .checksumWithInvalidVersion()
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    assertEquals('0', VsdmChecksum.parse(evidence.getChecksum().orElseThrow()).getVersion());
    assertDoesNotThrow(
        () ->
            VsdmExamEvidence.asOfflineMode()
                .checksumWithInvalidVersion()
                .generate(VsdmExamEvidenceResult.NO_UPDATES));
  }

  @ParameterizedTest
  @EnumSource(value = VsdmUpdateReason.class, names = "INVALID", mode = EnumSource.Mode.EXCLUDE)
  void checksumWithUpdateReason(VsdmUpdateReason reason) {
    val evidence =
        VsdmExamEvidence.asOnlineTestMode(egk)
            .checksumWithUpdateReason(reason)
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    assertEquals(
        reason, VsdmChecksum.parse(evidence.getChecksum().orElseThrow()).getUpdateReason());
    assertDoesNotThrow(
        () ->
            VsdmExamEvidence.asOfflineMode()
                .checksumWithUpdateReason(reason)
                .generate(VsdmExamEvidenceResult.NO_UPDATES));
  }

  @Test
  void checksumWithInvalidKvnr() {
    assertDoesNotThrow(
        () ->
            VsdmExamEvidence.asOfflineMode()
                .checksumWithInvalidKvnr()
                .generate(VsdmExamEvidenceResult.NO_UPDATES));
    assertDoesNotThrow(
        () ->
            VsdmExamEvidence.asOnlineTestMode(egk)
                .checksumWithInvalidKvnr()
                .generate(VsdmExamEvidenceResult.NO_UPDATES));
  }
}
