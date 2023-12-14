/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.actors;

import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.dav.PharmacyOrganizationBuilder;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import de.gematik.test.erezept.primsys.data.actors.PharmacyDto;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import de.gematik.test.smartcard.SmartcardArchive;
import lombok.val;

public class Pharmacy extends BaseActor {

  private final Konnektor konnektor;
  private final CardInfo smcbHandle;
  private final PharmacyDto baseData;

  public Pharmacy(
      PharmacyConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, sca);

    baseData = new PharmacyDto();
    baseData.setId(this.getIdentifier());
    baseData.setSmcb(cfg.getSmcbIccsn());
    baseData.setType(ActorType.PHARMACY);
    baseData.setName(cfg.getName());

    this.konnektor = konnektor;
    this.smcbHandle =
        konnektor.execute(GetCardHandleCommand.forSmartcard(this.getSmcb())).getPayload();
  }

  public byte[] signDocument(String document) {
    val signCmd = new SignXMLDocumentCommand(smcbHandle, document, algorithm);
    return konnektor.execute(signCmd).getPayload();
  }

  public PharmacyOrganization createPharmacyOrganization() {
    return PharmacyOrganizationBuilder.faker().name(this.getName()).build();
  }
}
