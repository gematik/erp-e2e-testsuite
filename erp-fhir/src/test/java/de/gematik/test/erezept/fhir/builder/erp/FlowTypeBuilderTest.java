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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FlowTypeBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildParametersFlowType160(ErpWorkflowVersion version) {
    val params =
        FlowTypeBuilder.builder(PrescriptionFlowType.FLOW_TYPE_160).version(version).build();

    assertNotNull(params);
    val result = ValidatorUtil.encodeAndValidate(parser, params);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildParametersForAllFlowTypes(ErpWorkflowVersion version) {
    val isOldProfile = ErpWorkflowVersion.V1_1_1.compareTo(version) == 0;
    Arrays.stream(PrescriptionFlowType.values())
        .filter(flowType -> !isOldProfile || !flowType.equals(PrescriptionFlowType.FLOW_TYPE_209))
        .forEach(
            flowType -> {
              val params = FlowTypeBuilder.builder(flowType).version(version).build();

              assertNotNull(params);
              val result = ValidatorUtil.encodeAndValidate(parser, params);
              assertTrue(result.isSuccessful());
            });
  }
}
