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
 */

package de.gematik.test.konnektor.soap.mock;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.konnektor.soap.mock.utils.CdmVersion;
import de.gematik.test.konnektor.soap.mock.utils.XmlEncoder;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.fa.vsds.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsds.UCPersoenlicheVersichertendatenXML;
import jakarta.xml.ws.Holder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import lombok.val;

public class MockVSDServicePortType extends AbstractMockService implements VSDServicePortType {
  private static final String EMPTY = "empty";

  private final VsdmService vsdmService;

  public MockVSDServicePortType(MockKonnektor mockKonnektor, VsdmService vsdmService) {
    super(mockKonnektor);
    this.vsdmService = vsdmService;
  }

  @Override
  public void readVSD(
      String ehcHandle,
      String hpcHandle,
      boolean performOnlineCheck,
      boolean readOnlineReceipt,
      ContextType context,
      Holder<byte[]> persoenlicheVersichertendaten,
      Holder<byte[]> allgemeineVersicherungsdaten,
      Holder<byte[]> geschuetzteVersichertendaten,
      Holder<VSDStatusType> vsdStatus,
      Holder<byte[]> pruefungsnachweis) {

    val egk =
        (Egk) mockKonnektor.getSmartcardWrapperByCardHandle(ehcHandle).orElseThrow().getSmartcard();

    persoenlicheVersichertendaten.value = generatePersonalInsuranceData(egk).getBytes();
    allgemeineVersicherungsdaten.value = generateCommonInsuranceData(egk).getBytes();
    geschuetzteVersichertendaten.value = EMPTY.getBytes();

    val evidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, egk).build(VsdmExamEvidenceResult.NO_UPDATES);
    pruefungsnachweis.value = evidence.encode().getBytes(StandardCharsets.UTF_8);

    vsdStatus.value = new VSDStatusType();
    vsdStatus.value.setStatus("0");
    vsdStatus.value.setVersion("5.2.0");
  }

  private String generateCommonInsuranceData(Egk egk) {
    val person = new UCAllgemeineVersicherungsdatenXML.Versicherter.Versicherungsschutz();
    person.setBeginn(egk.getInsuranceStartDate().format(DateTimeFormatter.BASIC_ISO_DATE));

    val patient = new UCAllgemeineVersicherungsdatenXML.Versicherter();
    patient.setVersicherungsschutz(person);

    val commonInsurance = new UCAllgemeineVersicherungsdatenXML();
    commonInsurance.setCDMVERSION(CdmVersion.V1.getVersion());
    commonInsurance.setVersicherter(patient);

    return XmlEncoder.encode(commonInsurance);
  }

  private String generatePersonalInsuranceData(Egk egk) {
    val strassenAddress =
        new UCPersoenlicheVersichertendatenXML.Versicherter.Person.StrassenAdresse();
    strassenAddress.setStrasse(egk.getOwnerData().getStreet());
    strassenAddress.setPostleitzahl(egk.getOwnerData().getPostalCode());

    val person = new UCPersoenlicheVersichertendatenXML.Versicherter.Person();
    person.setStrassenAdresse(strassenAddress);
    person.setNachname(egk.getOwnerData().getSurname());
    person.setVorname(egk.getOwnerData().getGivenName());

    val patient = new UCPersoenlicheVersichertendatenXML.Versicherter();
    patient.setVersichertenID(egk.getKvnr());
    patient.setPerson(person);

    val personalInsurance = new UCPersoenlicheVersichertendatenXML();
    personalInsurance.setCDMVERSION(CdmVersion.V1.getVersion());
    personalInsurance.setVersicherter(patient);

    return XmlEncoder.encode(personalInsurance);
  }
}
