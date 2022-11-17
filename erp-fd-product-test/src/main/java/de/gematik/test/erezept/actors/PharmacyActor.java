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

package de.gematik.test.erezept.actors;

import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
@EqualsAndHashCode
public class PharmacyActor extends ErpActor {

  public PharmacyActor(String name) {
    super(ActorType.PHARMACY, name);
  }

  public String getCommonName() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return useSmcb.getSmcB().getOwner().getCommonName();
  }

  public TelematikID getTelematikId() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return TelematikID.from(useSmcb.getTelematikID());
  }
}