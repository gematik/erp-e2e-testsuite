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

package de.gematik.test.erezept.fhir.r4.erp;

import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import java.util.List;

public enum CommunicationType implements ICommunicationType<ErpWorkflowVersion> {
  INFO_REQ(ErpWorkflowStructDef.COM_INFO_REQ),
  DISP_REQ(ErpWorkflowStructDef.COM_DISP_REQ),
  REPLY(ErpWorkflowStructDef.COM_REPLY),
  REPRESENTATIVE(ErpWorkflowStructDef.COM_REPRESENTATIVE);

  // which communication types are received by KVIDs
  private static final List<CommunicationType> PATIENT_RECEIVING = List.of(REPLY, REPRESENTATIVE);

  // which communication types are sent by pharmacies
  private static final List<CommunicationType> PHARMACY_SENDING = List.of(REPLY);

  private final List<ErpWorkflowStructDef> types;

  CommunicationType(ErpWorkflowStructDef... type) {
    this(List.of(type));
  }

  CommunicationType(List<ErpWorkflowStructDef> types) {
    this.types = types;
  }

  @Override
  public WithStructureDefinition<ErpWorkflowVersion> getType() {
    return types.get(0);
  }

  @Override
  public WithNamingSystem getRecipientNamingSystem() {
    if (PATIENT_RECEIVING.contains(this)) {
      // TODO: not always true, Patient might also be PKV!
      return DeBasisProfilNamingSystem.KVID_GKV_SID;
    } else {
      return DeBasisProfilNamingSystem.TELEMATIK_ID_SID;
    }
  }

  @Override
  public WithNamingSystem getSenderNamingSystem() {
    if (PHARMACY_SENDING.contains(this)) {
      return DeBasisProfilNamingSystem.TELEMATIK_ID_SID;
    } else {
      // TODO: not always true, Patient might also be PKV!
      return DeBasisProfilNamingSystem.KVID_GKV_SID;
    }
  }

  @Override
  public boolean doesMatch(String url) {
    return this.types.stream().anyMatch(type -> type.matches(url));
  }
}
