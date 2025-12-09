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

package de.gematik.test.erezept.actors;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class EuPharmacyActor extends ErpActor {

  public EuPharmacyActor(String name) {
    super(ActorType.PHARMACY, name);
  }

  public IsoCountryCode getCountryCode() {
    val baseData = SafeAbility.getAbility(this, ProvidePharmacyBaseData.class);
    return baseData.getCountryCode();
  }

  public String getCommonName() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return useSmcb.getSmcB().getOwnerData().getCommonName();
  }

  public TelematikID getTelematikId() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return TelematikID.from(useSmcb.getTelematikID());
  }
}
