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

import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;

public class FlowTypeBuilder {

  private ErpWorkflowVersion version = ErpWorkflowVersion.getDefaultVersion();
  private PrescriptionFlowType flowType;

  public static FlowTypeBuilder builder(PrescriptionFlowType flowType) {
    val builder = new FlowTypeBuilder();
    builder.flowType = flowType;

    return builder;
  }

  public static Parameters build(PrescriptionFlowType flowType) {
    return builder(flowType).build();
  }

  public FlowTypeBuilder version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public Parameters build() {
    Coding flowTypeCoding;

    if (version.compareTo(ErpWorkflowVersion.V1_2_0) < 0) {
      flowTypeCoding = flowType.asCoding();
    } else {
      flowTypeCoding = flowType.asCoding(ErpWorkflowCodeSystem.FLOW_TYPE_12);
    }

    return new Parameters().addParameter("workflowType", flowTypeCoding);
  }
}
