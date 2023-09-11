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

import de.gematik.test.cardterminal.*;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.primsys.rest.data.*;
import de.gematik.test.konnektor.*;
import de.gematik.test.konnektor.commands.*;
import de.gematik.test.smartcard.*;
import lombok.*;

public class Pharmacy extends BaseActor {

  private final Konnektor konnektor;
  private final CardInfo smcbHandle;
  private final PharmacyData baseData;

  public Pharmacy(
      PharmacyConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, sca);

    baseData = new PharmacyData();
    baseData.setId(this.getIdentifier());
    baseData.setSmcb(cfg.getSmcbIccsn());
    baseData.setType(ActorRole.PHARMACY.getReadable());
    baseData.setName(cfg.getName());

    this.konnektor = konnektor;
    this.smcbHandle =
        konnektor.execute(GetCardHandleCommand.forSmartcard(this.getSmcb())).getPayload();
  }

  public byte[] signDocument(String document) {
    val signCmd = new SignXMLDocumentCommand(smcbHandle, document);
    return konnektor.execute(signCmd).getPayload();
  }

  @Override
  public PharmacyData getBaseData() {
    return baseData;
  }
}
