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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class DocumentTypeTest {

  @Test
  void shouldParseValidDocumentTypesFromCode() {
    val codes = List.of("1", "2", "3");
    val expectedTypes =
        List.of(DocumentType.PRESCRIPTION, DocumentType.CONFIRMATION, DocumentType.RECEIPT);

    for (var i = 0; i < codes.size(); i++) {
      val actual = DocumentType.fromCode(codes.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidDocumentTypeCodes() {
    val codes = List.of("eins", "zwei", "0", "4", "12", "33");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> DocumentType.fromCode(code)));
  }
}
