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

import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;

public class FlowTypeBuilder {

  private PrescriptionFlowType flowType;

  public static FlowTypeBuilder builder(PrescriptionFlowType flowType) {
    val builder = new FlowTypeBuilder();
    builder.flowType = flowType;

    return builder;
  }

  public static Parameters build(PrescriptionFlowType flowType) {
    return builder(flowType).build();
  }

  public Parameters build() {
    return new Parameters().addParameter("workflowType", flowType.asCoding());
  }
}
