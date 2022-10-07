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

package de.gematik.test.konnektor.util;

import com.github.javafaker.Faker;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;

public class CardsUtil {

  private static Faker faker = new Faker();

  private CardsUtil() {
    throw new AssertionError("util class");
  }

  private static String randomIccsn() {
    return faker.regexify("[0-9]{20}");
  }

  public static CardInfoType createHba() {
    return createHba(randomIccsn());
  }

  public static CardInfoType createHba(String iccsn) {
    return create(CardTypeType.HBA, iccsn);
  }

  public static CardInfoType createSmcb() {
    return createSmcb(randomIccsn());
  }

  public static CardInfoType createSmcb(String iccsn) {
    return create(CardTypeType.SMC_B, iccsn);
  }

  public static CardInfoType createEgk() {
    return createEgk(randomIccsn());
  }

  public static CardInfoType createEgk(String icssn) {
    return create(CardTypeType.EGK, icssn);
  }

  public static List<CardInfoType> createRandomList(int numCards) {
    return IntStream.range(0, numCards).mapToObj(i -> createRandom()).collect(Collectors.toList());
  }

  public static Cards createRandomCards(int numCards) {
    val cards = new Cards();
    cards.getCard().addAll(createRandomList(numCards));
    return cards;
  }

  public static CardInfoType createRandom() {
    val types = List.of(CardTypeType.EGK, CardTypeType.HBA, CardTypeType.SMC_B);
    val idx = faker.random().nextInt(0, types.size() - 1);
    val type = types.get(idx);
    return create(type, randomIccsn());
  }

  public static CardInfoType create(CardTypeType type, String iccsn) {
    val cit = new CardInfoType();
    cit.setIccsn(iccsn);
    cit.setCardType(type);
    cit.setCardHandle(faker.regexify("[A-Za-z0-9]{15}"));
    cit.setCardHolderName(faker.name().fullName());
    return cit;
  }

  public static GetCardsResponse createGetCardsResponse(Status status, int numCards) {
    val gcr = new GetCardsResponse();
    gcr.setStatus(status);
    gcr.setCards(createRandomCards(numCards));
    return gcr;
  }
}
