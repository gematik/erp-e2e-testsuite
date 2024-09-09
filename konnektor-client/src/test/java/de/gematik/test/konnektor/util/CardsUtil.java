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

package de.gematik.test.konnektor.util;

import com.github.javafaker.Faker;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.val;

@Builder
public class CardsUtil {

  private static final Faker faker = new Faker();

  private CardsUtil() {
    throw new AssertionError("util class");
  }

  public static CardInfoTypeBuilder builder() {
    return new CardInfoTypeBuilder();
  }

  public static class CardInfoTypeBuilder {
    private CardInfoTypeBuilder() {}

    private CardTypeType type;
    private int slot = Integer.parseInt(faker.regexify("[1-4]{1}"));

    private String iccsn = faker.regexify("[0-9]{20}");

    private String ctId = "CT1";

    private String cardHandle = faker.regexify("[A-Za-z0-9]{15}");

    private String cardHolderName = faker.name().fullName();

    private GregorianCalendar insertTime = new GregorianCalendar();

    public CardInfoTypeBuilder type(CardTypeType type) {
      this.type = type;
      return this;
    }

    public CardInfoTypeBuilder slot(int slot) {
      this.slot = slot;
      return this;
    }

    public CardInfoTypeBuilder iccsn(String iccsn) {
      this.iccsn = iccsn;
      return this;
    }

    public CardInfoTypeBuilder ctId(String ctId) {
      this.ctId = ctId;
      return this;
    }

    public CardInfoTypeBuilder cardHandle(String cardHandle) {
      this.cardHandle = cardHandle;
      return this;
    }

    public CardInfoTypeBuilder cardHolderName(String cardHolderName) {
      this.cardHolderName = cardHolderName;
      return this;
    }

    public CardInfoTypeBuilder insertTime(GregorianCalendar insertTime) {
      this.insertTime = insertTime;
      return this;
    }

    @SneakyThrows
    public CardInfoType build() {
      val cit = new CardInfoType();
      cit.setIccsn(iccsn);
      cit.setCardType(type);
      cit.setCardHandle(cardHandle);
      cit.setCardHolderName(cardHolderName);
      cit.setInsertTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(insertTime));
      cit.setCtId(ctId);
      cit.setSlotId(BigInteger.valueOf(slot));
      return cit;
    }
  }

  public static GetCardsResponse createGetCardsResponse(Status status, int numCards) {
    val cards = new ArrayList<CardInfoType>();
    for (int i = 0; i < numCards; i++) {
      cards.add(CardsUtil.builder().type(CardTypeType.EGK).slot(i).build());
    }
    return createGetCardsResponse(status, cards.toArray(new CardInfoType[cards.size()]));
  }

  public static GetCardsResponse createGetCardsResponse(Status status, CardInfoType... card) {
    val gcr = new GetCardsResponse();
    gcr.setStatus(status);

    val cards = new Cards();
    cards.getCard().addAll(Arrays.stream(card).toList());

    gcr.setCards(cards);
    return gcr;
  }
}
