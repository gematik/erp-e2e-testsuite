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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

/** Builder for ErxCommunication of type DISP_REQ */
public class ErxComDispReqBuilder extends ErxComPrescriptionBuilder<ErxComDispReqBuilder> {

  protected ErxComDispReqBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    checkRequired();
    val type = CommunicationType.DISP_REQ;

    ErxCommunication com;
    if (this.erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = buildCommon(type, () -> type.getType().asCanonicalType());
    } else {
      com =
          buildCommon(
              type, () -> ErpWorkflowStructDef.COM_DISP_REQ_12.asCanonicalType(erpWorkflowVersion));
    }

    com.setBasedOn(List.of(new Reference(baseOnReference)));

    if (this.erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) >= 0) {
      com.addExtension(flowType.asExtension());
    }
    return com;
  }

  private void checkRequired() {
    if (this.erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) >= 0) {
      this.checkRequired(
          flowType, "A Dispense Request Communication requires a Flow-Type of the Prescription");
    }
  }
}
