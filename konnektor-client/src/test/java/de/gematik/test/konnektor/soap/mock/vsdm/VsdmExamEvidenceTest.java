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

package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.test.konnektor.exceptions.ParsingExamEvidenceException;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VsdmExamEvidenceTest {

  @Test
  void validExamEvidenceParsing() {
    val hannaKvnr = "X110499478";
    val checksum = VsdmChecksum.builder(hannaKvnr).build();
    for (VsdmExamEvidenceResult result : VsdmExamEvidenceResult.values()) {
      val evidence = VsdmExamEvidence.builder(result).checksum(checksum).build();
      val evidenceAsBase64 = evidence.encodeAsBase64();
      Assertions.assertDoesNotThrow(() -> VsdmExamEvidence.parse(evidenceAsBase64));
    }
  }

  @Test
  void invalidExamEvidenceParsing() {
    Assertions.assertThrows(
        ParsingExamEvidenceException.class, () -> VsdmExamEvidence.parse("abc"));
  }
}
