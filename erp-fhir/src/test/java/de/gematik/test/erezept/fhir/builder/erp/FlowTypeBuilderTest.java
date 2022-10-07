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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.Before;
import org.junit.Test;

public class FlowTypeBuilderTest {

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void buildParametersFlowType160() {
    val params = FlowTypeBuilder.build(PrescriptionFlowType.FLOW_TYPE_160);

    assertNotNull(params);
    val result = encodeAndValidate(params);
    assertTrue(result.isSuccessful());
  }

  @Test
  public void buildParametersForAllFlowTypes() {
    Arrays.stream(PrescriptionFlowType.values())
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
