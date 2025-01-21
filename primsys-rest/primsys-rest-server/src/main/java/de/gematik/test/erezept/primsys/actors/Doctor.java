/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.resources.kbv.KbvBaseBundle;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.primsys.data.actors.DoctorDto;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import lombok.Getter;
import lombok.val;

public class Doctor extends BaseActor {

  private final Konnektor konnektor;

  @Getter private final Hba hba;
  private final CardInfo hbaHandle;

  public Doctor(
      DoctorConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, sca);

    this.konnektor = konnektor;
    this.hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    this.hbaHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(hba)).getPayload();
  }

  public byte[] signDocument(String document) {
    val signCmd = new SignXMLDocumentCommand(hbaHandle, document, algorithm);
    return konnektor.execute(signCmd).getPayload();
  }

  public DoctorDto getDoctorInformation(KbvBaseBundle kbvBundle) {
    val org = kbvBundle.getMedicalOrganization();
    val doc = kbvBundle.getPractitioner();
    val d = new DoctorDto();
    d.setName(doc.getFullName());
    d.setType(this.getType());
    d.setId(this.getIdentifier());

    d.setOfficeName(org.getName());
    d.setHba(this.getHba().getIccsn());
    d.setSmcb(this.getSmcb().getIccsn());
    d.setDocNumberType(doc.getANRType().name());
    d.setDocNumber(doc.getANR().getValue());
    d.setDocQualificationType(QualificationType.DOCTOR.getDisplay());
    org.getBsnrOptional().ifPresent(bsnr -> d.setBsnr(bsnr.getValue()));
    d.setPhone(org.getPhone());
    d.setEmail(org.getMail());
    d.setCity(org.getCity());
    d.setPostal(org.getPostalCode());
    d.setStreet(org.getStreet());

    return d;
  }
}
