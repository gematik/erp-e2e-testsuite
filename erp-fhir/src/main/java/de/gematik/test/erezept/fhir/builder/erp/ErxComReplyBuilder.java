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
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

public class ErxComReplyBuilder extends ErxComPrescriptionBuilder<ErxComReplyBuilder> {

  protected ErxComReplyBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    val type = CommunicationType.REPLY;
    ErxCommunication com;
    ErpWorkflowStructDef availabilityStatusStructDef;
    ErpWorkflowCodeSystem availabilityCodeSystem;
    if (this.erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = buildCommon(type, () -> type.getType().asCanonicalType());
      availabilityStatusStructDef = ErpWorkflowStructDef.AVAILABILITY_STATUS;
      availabilityCodeSystem = ErpWorkflowCodeSystem.AVAILABILITY_STATUS;
    } else {
      com =
          buildCommon(
              type, () -> ErpWorkflowStructDef.COM_REPLY_12.asCanonicalType(erpWorkflowVersion));
      availabilityStatusStructDef = ErpWorkflowStructDef.AVAILABILITY_STATUS_12;
      availabilityCodeSystem = ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12;
    }

    val payload = com.getPayloadFirstRep();

    Optional.ofNullable(availabilityStatus)
        .ifPresent(
            as -> {
              val ext =
                  availabilityStatusStructDef.asExtension(
                      availabilityStatus.asCoding(availabilityCodeSystem));
              payload.addExtension(ext);
            });

    payload.addExtension(supplyOptionsType.asExtension(erpWorkflowVersion));
    com.setBasedOn(List.of(new Reference(baseOnReference)));

    return com;
  }
}
