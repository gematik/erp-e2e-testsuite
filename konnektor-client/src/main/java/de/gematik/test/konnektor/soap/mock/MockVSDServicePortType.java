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

package de.gematik.test.konnektor.soap.mock;

import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import de.gematik.test.smartcard.Egk;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import javax.xml.ws.Holder;
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
      Holder<byte[]> pruefungsnachweis)
      throws FaultMessage {

    val egk =
        (Egk) mockKonnektor.getSmartcardWrapperByCardHandle(ehcHandle).orElseThrow().getSmartcard();

    persoenlicheVersichertendaten.value = EMPTY.getBytes();
    allgemeineVersicherungsdaten.value = EMPTY.getBytes();
    geschuetzteVersichertendaten.value = EMPTY.getBytes();

    pruefungsnachweis.value =
        VsdmExamEvidence.asOnlineMode(vsdmService, egk)
            .generate(VsdmExamEvidenceResult.NO_UPDATES)
            .encode();

    vsdStatus.value = new VSDStatusType();
    vsdStatus.value.setStatus("0");
    vsdStatus.value.setVersion("5.2.0");
  }
}
