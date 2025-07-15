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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.r4.kbv.KbvBaseBundle;
import de.gematik.test.erezept.primsys.data.actors.DoctorDto;
import de.gematik.test.erezept.primsys.data.actors.DoctorNumber;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import lombok.Getter;
import lombok.val;

public class Doctor extends BaseActor {

  @Getter private final Hba hba;
  private final CardInfo hbaHandle;

  public Doctor(
      DoctorConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, konnektor, sca);

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
    d.setQualificationType(doc.getQualificationType().getDisplay());
    d.addQualifications(doc.getAdditionalQualifications());

    val anr = doc.getANR();
    anr.ifPresent(it -> d.setAnr(DoctorNumber.from(it.getType().name(), it.getValue())));

    // usually, if a doc has no LANR/ZANR it will likely have an ASV Fachgruppennummer
    val asv = doc.getAsvFachgruppennummer();
    asv.ifPresent(
        it -> {
          val qualificationType = format("ASV-Fachgruppennummer: {0}", it.getValue());
          d.addQualifications(qualificationType);
        });

    val bsnr = org.getBsnr();
    bsnr.ifPresent(it -> d.setBsnr(it.getValue()));

    d.setPhone(org.getPhone());
    d.setEmail(org.getMail());
    d.setCity(org.getCity());
    d.setPostal(org.getPostalCode());
    d.setStreet(org.getStreet());

    return d;
  }
}
