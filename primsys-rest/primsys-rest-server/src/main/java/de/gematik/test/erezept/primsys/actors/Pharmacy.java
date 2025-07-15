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

package de.gematik.test.erezept.primsys.actors;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.dav.PharmacyOrganizationFaker;
import de.gematik.test.erezept.fhir.r4.dav.PharmacyOrganization;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.ReadVsdCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import java.util.Base64;
import lombok.val;

public class Pharmacy extends BaseActor {

  public Pharmacy(
      PharmacyConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, konnektor, sca);
  }

  public byte[] signDocument(String document) {
    val signCmd = new SignXMLDocumentCommand(smcbHandle, document, algorithm);
    return konnektor.execute(signCmd).getPayload();
  }

  public PharmacyOrganization createPharmacyOrganization() {
    return PharmacyOrganizationFaker.builder().withName(this.getName()).fake();
  }

  public String requestEvidenceForEgk(Egk egk) {
    val egkCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(egk)).getPayload();
    val readVsdCommand = new ReadVsdCommand(egkCardHandle, smcbHandle, true, true);
    val pruefnachweis = konnektor.execute(readVsdCommand).getPayload().getPruefungsnachweis();
    return Base64.getEncoder().encodeToString(pruefnachweis);
  }
}
