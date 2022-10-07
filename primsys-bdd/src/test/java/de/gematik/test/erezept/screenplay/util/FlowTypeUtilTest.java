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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class FlowTypeUtilTest {

  @Test
  public void shouldTakeOverrideCode() {
    val codes = List.of("160", "169", "200");
    val expected =
        List.of(
            PrescriptionFlowType.FLOW_TYPE_160,
            PrescriptionFlowType.FLOW_TYPE_169,
            PrescriptionFlowType.FLOW_TYPE_200);

    assertEquals(expected.size(), codes.size());
    for (var i = 0; i < codes.size(); i++) {
      val exp = expected.get(i);
      val code = codes.get(i);
      val actual =
          FlowTypeUtil.getFlowType(
              code, VersicherungsArtDeBasis.BG, PrescriptionAssignmentKind.PHARMACY_ONLY);
      assertEquals(exp, actual);
    }
  }

  @Test
  public void shouldThrowOnInvalidOverrideCode() {
    val codes = List.of("161", "170", "201", "");
    codes.forEach(
        code ->
            assertThrows(
                InvalidValueSetException.class,
                () ->
                    FlowTypeUtil.getFlowType(
                        code,
                        VersicherungsArtDeBasis.BG,
                        PrescriptionAssignmentKind.PHARMACY_ONLY)));
  }

  @Test
  public void shouldReasonWorkflowType() {
    val insuranceKinds =
        List.of(
            VersicherungsArtDeBasis.GKV,
            VersicherungsArtDeBasis.GKV,
            VersicherungsArtDeBasis.PKV,
            VersicherungsArtDeBasis.PKV);
    val assignementKinds =
        List.of(
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    val expectedTypes =
        List.of(
            PrescriptionFlowType.FLOW_TYPE_160,
            PrescriptionFlowType.FLOW_TYPE_169,
            PrescriptionFlowType.FLOW_TYPE_200,
            PrescriptionFlowType.FLOW_TYPE_209);

    assertEquals(insuranceKinds.size(), assignementKinds.size());
    assertEquals(assignementKinds.size(), expectedTypes.size());

    for (var i = 0; i < expectedTypes.size(); i++) {
      val actual = FlowTypeUtil.getFlowType(null, insuranceKinds.get(i), assignementKinds.get(i));
      assertEquals(expectedTypes.get(i), actual);
    }
  }

  @Test
  public void should160OnUnsupportedInsuranceKind() {
    val unsupported =
        List.of(
            VersicherungsArtDeBasis.BG,
            VersicherungsArtDeBasis.BEI,
            VersicherungsArtDeBasis.GPV,
            VersicherungsArtDeBasis.PPV,
            VersicherungsArtDeBasis.SEL,
            VersicherungsArtDeBasis.SOZ);

    unsupported.forEach(
        ik ->
            assertEquals(
                PrescriptionFlowType.FLOW_TYPE_160,
                FlowTypeUtil.getFlowType(null, ik, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)));
  }
}
