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
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChargeItemCommunicationType implements ICommunicationType<PatientenrechnungVersion> {
  CHANGE_REQ(PatientenrechnungStructDef.COM_CHARGE_CHANGE_REQ),
  CHANGE_REPLY(PatientenrechnungStructDef.COM_CHARGE_CHANGE_REPLY);

  private static final List<ChargeItemCommunicationType> PATIENT_RECEIVING = List.of(CHANGE_REPLY);

  // which communication types are sent by pharmacies
  private static final List<ChargeItemCommunicationType> PHARMACY_SENDING = List.of(CHANGE_REPLY);

  private final PatientenrechnungStructDef type;

  @Override
  public WithNamingSystem getRecipientNamingSystem(PatientenrechnungVersion version) {
    if (PATIENT_RECEIVING.contains(this)) {
      return kvidFor(version);
    } else {
      return DeBasisProfilNamingSystem.TELEMATIK_ID_SID;
    }
  }

  @Override
  public WithNamingSystem getSenderNamingSystem(PatientenrechnungVersion version) {
    WithNamingSystem ns;
    if (PHARMACY_SENDING.contains(this)) {
      ns = DeBasisProfilNamingSystem.TELEMATIK_ID_SID;
    } else {
      ns = kvidFor(version);
    }
    return ns;
  }

  private WithNamingSystem kvidFor(PatientenrechnungVersion version) {
    if (version.isSmallerThanOrEqualTo(PatientenrechnungVersion.V1_0_0)) {
      return DeBasisProfilNamingSystem.KVID_PKV_SID;
    }
    return DeBasisProfilNamingSystem.KVID_GKV_SID;
  }

  @Override
  public boolean doesMatch(String url) {
    return this.type.matches(url);
  }
}
