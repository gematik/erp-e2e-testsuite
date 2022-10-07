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

package de.gematik.test.konnektor.soap.mock;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeFactory;
import lombok.SneakyThrows;
import lombok.val;

public class MockKonnektor {

  private final SmartcardArchive smartcards;
  private final Map<String, SmartcardWrapper> cardsMap;

  private int jobNumber = 0;

  public MockKonnektor(SmartcardArchive smartcards) {
    this.smartcards = smartcards;
    this.cardsMap = new HashMap<>();
    initCardsMap();
  }

  public Cards getAllCards() {
    val cards = new Cards();
    val cardInfoTypes =
        cardsMap.values().stream().map(SmartcardWrapper::getInfoType).collect(Collectors.toList());
    cards.getCard().addAll(cardInfoTypes);
    return cards;
  }

  public Optional<SmartcardWrapper> getSmartcardWrapperByCardHandle(String handle) {
    val wrapper = cardsMap.get(handle);
    return Optional.ofNullable(wrapper);
  }

  private void initCardsMap() {
    val egks = smartcards.getEgkCards().stream().map(SmartcardWrapper::new);
    val hbas = smartcards.getHbaCards().stream().map(SmartcardWrapper::new);
    val smcbs = smartcards.getSmcbCards().stream().map(SmartcardWrapper::new);

    egks.forEach(egk -> cardsMap.put(egk.getCardHandle(), egk));
    hbas.forEach(hba -> cardsMap.put(hba.getCardHandle(), hba));
    smcbs.forEach(smcb -> cardsMap.put(smcb.getCardHandle(), smcb));
  }

  public byte[] signDocumentWith(String cardHandle, byte[] data) throws FaultMessage {
    val wrapper = cardsMap.get(cardHandle);
    if (wrapper == null) {
      throw new FaultMessage(
          format("No card found with CardHandle {0}", cardHandle), createError(cardHandle));
    }

    val smartcard = wrapper.getSmartcard();
    if (smartcard.getType() != SmartcardType.HBA) {
      throw new FaultMessage(
          format("Given CardHandle {0} does not belong to a HBA", cardHandle),
          createError(cardHandle));
    }

    val signer = new LocalSigner((Hba) smartcard);

    return signer.signQES(data);
  }

  public boolean verifyDocument(byte[] data) {
    val verifier = new LocalVerifier();
    return verifier.verify(data);
  }

  public String getJobNumber(ContextType context) { // NOSONAR: I will need this parameter later on
    return String.format("MOCK-KON-%03d", jobNumber++);
  }

  @SneakyThrows
  public Error createError(String messageId) {
    val error = new Error();
    error.setMessageID(messageId);
    error.setTimestamp(
        DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
    return error;
  }
}
