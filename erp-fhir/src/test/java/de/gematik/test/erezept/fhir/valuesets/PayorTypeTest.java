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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class PayorTypeTest {

  @Test
  void shouldParseValidPayorTypesFromCode() {
    val codes = List.of("SKT", "UK");
    val expectedTypes = List.of(PayorType.SKT, PayorType.UK);

    for (var i = 0; i < codes.size(); i++) {
      val actual = PayorType.fromCode(codes.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidPayorTypeCodes() {
    val codes = List.of("KST", "UKK");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> PayorType.fromCode(code)));
  }

  @Test
  void shouldParseValidPayorTypesFromDisplay() {
    val displayValues = List.of("Sonstige", "Unfallkassen");
    val expectedTypes = List.of(PayorType.SKT, PayorType.UK);

    for (var i = 0; i < displayValues.size(); i++) {
      val actual = PayorType.fromDisplay(displayValues.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidPayorTypeDisplayValues() {
    val codes = List.of("Bürdenträger", "Krankenkassen");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> PayorType.fromCode(code)));
  }

  @Test
  void shouldEncodePayorTypeCoding() {
    val payorTypes = List.of(PayorType.values());
    payorTypes.forEach(
        pt -> {
          val coding = pt.asCoding();
          assertEquals(pt.getCode(), coding.getCode());
          assertEquals(pt.getCodeSystem().getCanonicalUrl(), coding.getSystem());
        });
  }

  @Test
  void shouldEncodePayorTypeCodingWithCustomCodeSystem() {
    val pt = PayorType.UK;
    val coding = pt.asCoding(ErpWorkflowCodeSystem.FLOW_TYPE);
    assertEquals(pt.getCode(), coding.getCode());
    assertEquals(ErpWorkflowCodeSystem.FLOW_TYPE.getCanonicalUrl(), coding.getSystem());
  }

  @Test
  void shouldEncodePayorTypeCodeableConcept() {
    val payorTypes = List.of(PayorType.values());
    payorTypes.forEach(
        pt -> {
          val codeable = pt.asCodeableConcept();
          assertEquals(pt.getCode(), codeable.getCodingFirstRep().getCode());
          assertEquals(
              pt.getCodeSystem().getCanonicalUrl(), codeable.getCodingFirstRep().getSystem());
        });
  }
}
