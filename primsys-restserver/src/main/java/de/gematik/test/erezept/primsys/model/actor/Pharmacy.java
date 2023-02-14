/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.model.actor;

import de.gematik.test.erezept.lei.cfg.EnvironmentConfiguration;
import de.gematik.test.erezept.lei.cfg.PharmacyConfiguration;
import de.gematik.test.erezept.primsys.rest.data.PharmacyData;
import de.gematik.test.erezept.primsys.rest.data.TelematikData;
import de.gematik.test.smartcard.SmartcardArchive;
import lombok.val;

public class Pharmacy extends BaseActor {

  private final PharmacyConfiguration cfg;
  private final PharmacyData baseData;

  public Pharmacy(PharmacyConfiguration cfg, EnvironmentConfiguration env, SmartcardArchive sca) {
    super(cfg, env, sca);
    this.cfg = cfg;

    baseData = new PharmacyData();
    baseData.setId(this.getIdentifier());
    baseData.setSmcb(cfg.getSmcbIccsn());
    baseData.setType(ActorRole.PHARMACY.getReadable());
    baseData.setName(cfg.getName());

    val ti = new TelematikData();
    ti.setFachdienst(env.getTi().getFdBaseUrl());
    ti.setDiscoveryDocument(env.getTi().getDiscoveryDocumentUrl());
    ti.setTsl(env.getTslBaseUrl());
    baseData.setTi(ti);
  }

  @Override
  public PharmacyData getBaseData() {
    return baseData;
  }
}
