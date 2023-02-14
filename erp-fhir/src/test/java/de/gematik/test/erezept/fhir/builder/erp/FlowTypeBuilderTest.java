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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.validation.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class FlowTypeBuilderTest extends ParsingTest {

  @Test
  void buildParametersFlowType160() {
    val params = FlowTypeBuilder.build(PrescriptionFlowType.FLOW_TYPE_160);

    assertNotNull(params);
    val result = encodeAndValidate(params);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildParametersForAllFlowTypes() {
    Arrays.stream(PrescriptionFlowType.values())
        .filter(
            flowType ->
                !flowType.equals(PrescriptionFlowType.FLOW_TYPE_209)) // 209 not yet supported!
        .forEach(
            flowType -> {
              val params = FlowTypeBuilder.build(flowType);

              assertNotNull(params);
              val result = encodeAndValidate(params);
              assertTrue(result.isSuccessful());
            });
  }

  private ValidationResult encodeAndValidate(Parameters parameters) {
    // encode and check if encoded content is valid
    val xmlParameters = parser.encode(parameters, EncodingType.XML);
    val result = parser.validate(xmlParameters);
    ValidatorUtil.printValidationResult(result);

    return result;
  }
}
