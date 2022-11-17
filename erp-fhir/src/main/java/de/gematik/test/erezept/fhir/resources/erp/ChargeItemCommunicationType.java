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
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatienterechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChargeItemCommunicationType implements ICommunicationType<PatientenrechnungVersion> {
  CHANGE_REQ(PatienterechnungStructDef.COM_CHARGE_CHANGE_REQ),
  CHANGE_REPLY(PatienterechnungStructDef.COM_CHARGE_CHANGE_REPLY);

  private static final List<ChargeItemCommunicationType> PATIENT_RECEIVING = List.of(CHANGE_REPLY);

  // which communication types are sent by pharmacies
  private static final List<ChargeItemCommunicationType> PHARMACY_SENDING = List.of(CHANGE_REPLY);

  @Getter private final PatienterechnungStructDef type;

  public INamingSystem getRecipientNamingSystem() {
    INamingSystem ns;
    if (PATIENT_RECEIVING.contains(this)) {
      ns = DeBasisNamingSystem.KVID_PKV; // TODO: not always true, Patient might also be PKV!
    } else {
      ns = ErpWorkflowNamingSystem.TELEMATIK_ID_SID;
    }
    return ns;
  }

  public INamingSystem getSenderNamingSystem() {
    INamingSystem ns;
    if (PHARMACY_SENDING.contains(this)) {
      ns = ErpWorkflowNamingSystem.TELEMATIK_ID_SID;
    } else {
      ns = DeBasisNamingSystem.KVID_PKV;
    }
    return ns;
  }
}
