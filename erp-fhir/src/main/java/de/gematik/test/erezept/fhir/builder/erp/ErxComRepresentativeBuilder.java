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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

/** Builder for ErxCommunication of type REPRESENTATIVE */
public class ErxComRepresentativeBuilder
    extends ErxComPrescriptionBuilder<ErxComRepresentativeBuilder> {

  protected ErxComRepresentativeBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    checkRequired();

    val com =
        buildCommon(
            CommunicationType.REPRESENTATIVE,
            () -> ErpWorkflowStructDef.COM_REPRESENTATIVE.asCanonicalType(this.erpWorkflowVersion));
    val substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED;
    val flowTypeExtension = flowType.asExtension();

    val payload = com.getPayloadFirstRep();
    payload.addExtension(substitutionAllowedExt.asBooleanExtension(substitutionAllowed));
    payload.addExtension(flowTypeExtension);
    com.setBasedOn(List.of(new Reference(this.baseOnReference)));
    return com;
  }

  private void checkRequired() {
    this.checkRequired(
        flowType, "A Representative Communication requires a Flow-Type of the Prescription");
  }
}
