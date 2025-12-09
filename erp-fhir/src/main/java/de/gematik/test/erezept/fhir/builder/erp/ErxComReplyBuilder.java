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
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

public class ErxComReplyBuilder extends ErxComPrescriptionBuilder<ErxComReplyBuilder> {

  protected ErxComReplyBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    val flowTypeDiGA =
        Optional.ofNullable(this.flowType)
            .map(ft -> ft.equals(PrescriptionFlowType.FLOW_TYPE_162))
            .orElse(false);
    val useDiGAProfile = flowTypeDiGA && erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4) > 0;
    val com =
        buildCommon(
            () ->
                useDiGAProfile
                    ? ErpWorkflowStructDef.COM_DIGA.asCanonicalType(erpWorkflowVersion)
                    : ErpWorkflowStructDef.COM_REPLY.asCanonicalType(erpWorkflowVersion));

    // set sender and receiver
    com.addRecipient(
        CommunicationType.REPLY.getRecipientReference(this.receiver, this.erpWorkflowVersion));
    Optional.ofNullable(this.sender)
        .ifPresent(
            s ->
                com.setSender(
                    CommunicationType.REPLY.getSenderReference(s, this.erpWorkflowVersion)));

    val payload = com.getPayloadFirstRep();

    payload.setContent(new StringType(message));

    if (!useDiGAProfile) {

      Optional.ofNullable(availabilityStatus)
          .ifPresent(
              as -> {
                val availabilityStatusStructDef = ErpWorkflowStructDef.AVAILABILITY_STATUS;
                val ext = availabilityStatusStructDef.asExtension(availabilityStatus.asCoding());
                payload.addExtension(ext);
              });

      payload.addExtension(supplyOptionsType.asExtension());
    }

    com.setBasedOn(List.of(new Reference(baseOnReference)));

    return com;
  }
}
