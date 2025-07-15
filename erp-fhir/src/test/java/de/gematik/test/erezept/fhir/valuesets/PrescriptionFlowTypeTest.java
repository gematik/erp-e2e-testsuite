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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrescriptionFlowTypeTest {

  @Test
  void shouldCreateFromPrescriptionId() {
    val flowTypes =
        List.of(
            PrescriptionFlowType.FLOW_TYPE_160,
            PrescriptionFlowType.FLOW_TYPE_169,
            PrescriptionFlowType.FLOW_TYPE_200,
            PrescriptionFlowType.FLOW_TYPE_209);
    flowTypes.forEach(
        ft -> {
          val pid = PrescriptionId.random(ft);
          val actual = PrescriptionFlowType.fromPrescriptionId(pid);
          assertEquals(ft, actual);
          assertNotNull(ft.toString());
        });
  }

  @Test
  void shouldDetectDirectAssignments() {
    List.of(PrescriptionFlowType.FLOW_TYPE_169, PrescriptionFlowType.FLOW_TYPE_209)
        .forEach(ft -> assertTrue(ft.isDirectAssignment()));
  }

  @Test
  void shouldDetectGkvTypes() {
    List.of(PrescriptionFlowType.FLOW_TYPE_160, PrescriptionFlowType.FLOW_TYPE_169)
        .forEach(
            ft -> {
              assertTrue(ft.isGkvType());
              assertFalse(ft.isPkvType());
            });
  }

  @Test
  void shouldDetectPkvTypes() {
    List.of(PrescriptionFlowType.FLOW_TYPE_200, PrescriptionFlowType.FLOW_TYPE_209)
        .forEach(
            ft -> {
              assertTrue(ft.isPkvType());
              assertFalse(ft.isGkvType());
            });
  }

  @ParameterizedTest(name = "#{index} - WorkflowType from InsuranceKind {0} expected to be {1}")
  @MethodSource("defaultPrescriptionFlowTypes")
  void shouldGenerateFromInsuranceKind(
      InsuranceTypeDe insuranceKind, PrescriptionFlowType expectation) {
    assertEquals(expectation, PrescriptionFlowType.fromInsuranceKind(insuranceKind));
    assertEquals(expectation, PrescriptionFlowType.fromInsuranceKind(insuranceKind, false));
    assertFalse(PrescriptionFlowType.fromInsuranceKind(insuranceKind).isDirectAssignment());
  }

  @ParameterizedTest(name = "#{index} - WorkflowType from InsuranceKind {0} expected to be {1}")
  @MethodSource("directAssignmentPrescriptionFlowTypes")
  void shouldGenerateFromInsuranceKindWithDirectAssignment(
      InsuranceTypeDe insuranceKind, PrescriptionFlowType expectation) {
    assertEquals(expectation, PrescriptionFlowType.fromInsuranceKind(insuranceKind, true));
    assertTrue(PrescriptionFlowType.fromInsuranceKind(insuranceKind, true).isDirectAssignment());
  }

  static Stream<Arguments> defaultPrescriptionFlowTypes() {
    return Stream.of(
        arguments(InsuranceTypeDe.GKV, PrescriptionFlowType.FLOW_TYPE_160),
        arguments(InsuranceTypeDe.PKV, PrescriptionFlowType.FLOW_TYPE_200),
        arguments(InsuranceTypeDe.BG, PrescriptionFlowType.FLOW_TYPE_160));
  }

  static Stream<Arguments> directAssignmentPrescriptionFlowTypes() {
    return Stream.of(
        arguments(InsuranceTypeDe.GKV, PrescriptionFlowType.FLOW_TYPE_169),
        arguments(InsuranceTypeDe.PKV, PrescriptionFlowType.FLOW_TYPE_209),
        arguments(InsuranceTypeDe.BG, PrescriptionFlowType.FLOW_TYPE_169));
  }
}
