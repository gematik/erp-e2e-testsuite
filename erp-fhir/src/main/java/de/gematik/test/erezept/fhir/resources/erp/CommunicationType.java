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

package de.gematik.test.erezept.fhir.resources.erp;

import de.gematik.test.erezept.fhir.parser.profiles.INamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommunicationType implements ICommunicationType<ErpWorkflowVersion> {
  INFO_REQ(ErpWorkflowStructDef.COM_INFO_REQ),
  DISP_REQ(ErpWorkflowStructDef.COM_DISP_REQ),
  REPLY(ErpWorkflowStructDef.COM_REPLY),
  REPRESENTATIVE(ErpWorkflowStructDef.COM_REPRESENTATIVE),
  ;

  // which communication types are received by KVIDs
  private static final List<CommunicationType> PATIENT_RECEIVING = List.of(REPLY, REPRESENTATIVE);

  // which communication types are sent by pharmacies
  private static final List<CommunicationType> PHARMACY_SENDING = List.of(REPLY);

  @Getter private final ErpWorkflowStructDef type;

  public INamingSystem getRecipientNamingSystem() {
    INamingSystem ns;
    if (PATIENT_RECEIVING.contains(this)) {
      ns = DeBasisNamingSystem.KVID; // TODO: not always true, Patient might also be PKV!
    } else {
      ns = ErpWorkflowNamingSystem.TELEMATIK_ID;
    }
    return ns;
  }

  public INamingSystem getSenderNamingSystem() {
    INamingSystem ns;
    if (PHARMACY_SENDING.contains(this)) {
      ns = ErpWorkflowNamingSystem.TELEMATIK_ID;
    } else {
      ns = DeBasisNamingSystem.KVID; // TODO: not always true, Patient might also be PKV!
    }
    return ns;
  }
}
