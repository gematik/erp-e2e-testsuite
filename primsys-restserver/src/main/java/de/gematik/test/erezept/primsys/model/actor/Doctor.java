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

package de.gematik.test.erezept.primsys.model.actor;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.lei.cfg.DoctorConfiguration;
import de.gematik.test.erezept.lei.cfg.EnvironmentConfiguration;
import de.gematik.test.erezept.primsys.rest.data.DoctorData;
import de.gematik.test.erezept.primsys.rest.data.TelematikData;
import de.gematik.test.konnektor.CardHandle;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardArchive;
import lombok.val;

public class Doctor extends BaseActor {

  private final DoctorConfiguration cfg;
  private final Konnektor konnektor;
  private final Hba hba;
  private final CardHandle hbaHandle;

  private final DoctorData baseData;

  public Doctor(
      DoctorConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    super(cfg, env, sca);
    this.cfg = cfg;

    this.konnektor = konnektor;
    this.hba = sca.getHbaByICCSN(cfg.getHbaIccsn(), cfg.getCryptoAlgorithm());
    this.hbaHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(hba));

    baseData = new DoctorData();
    baseData.setId(this.getIdentifier());
    baseData.setHba(cfg.getHbaIccsn());
    baseData.setSmcb(cfg.getSmcbIccsn());
    baseData.setType(ActorRole.DOCTOR.getReadable());
    baseData.setName(cfg.getName());

    val qualificationType = cfg.getQualificationType();
    val docNumber = BaseANR.randomFromQualification(qualificationType);
    baseData.setDocQualificationType(qualificationType.getDisplay());
    baseData.setOfficeName(format("{0}raxis {1}", qualificationType.getDisplay(), cfg.getName()));
    baseData.setDocNumber(docNumber.getValue());
    baseData.setDocNumberType(docNumber.getType().name());

    baseData.setBsnr(GemFaker.fakerBsnr());
    baseData.setPhone(GemFaker.fakerPhone());
    baseData.setEmail(GemFaker.eMail(cfg.getName()));
    baseData.setCity(GemFaker.fakerCity());
    baseData.setPostal(GemFaker.fakerZipCode());
    baseData.setStreet(GemFaker.fullStreetName());

    val ti = new TelematikData();
    ti.setFachdienst(env.getTi().getFdBaseUrl());
    ti.setDiscoveryDocument(env.getTi().getDiscoveryDocumentUrl());
    ti.setTsl(env.getTslBaseUrl());
    baseData.setTi(ti);
  }

  public byte[] signDocument(String document) {
    val signCmd = new SignXMLDocumentCommand(hbaHandle, document);
    return konnektor.execute(signCmd);
  }

  @Override
  public DoctorData getBaseData() {
    return baseData;
  }
}
