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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.konnektor.exceptions.SmartcardMissmatchException;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GetCardHandleCommand extends AbstractKonnektorCommand<CardInfo> {

  private final String iccsn;

  private GetCardHandleCommand(String iccsn) {
    this.iccsn = iccsn;
  }

  @Override
  public CardInfo execute(ContextType ctx, ServicePortProvider serviceProvider) {
    log.trace(format("Get CardHandle for ICCSN {0}", iccsn));
    val cmd = new GetCardsCommand();
    val cardsResponse = cmd.execute(ctx, serviceProvider);

    return cardsResponse.getCards().getCard().stream()
        .filter(cit -> cit.getIccsn().equals(this.iccsn))
        .map(CardInfo::fromCardInfoType)
        .findFirst()
        .orElseThrow(
            () -> new SmartcardMissmatchException(this.getClass(), iccsn, serviceProvider));
  }

  public static GetCardHandleCommand forIccsn(@NonNull final String iccsn) {
    return new GetCardHandleCommand(iccsn);
  }

  public static GetCardHandleCommand forSmartcard(@NonNull final Smartcard smartcard) {
    return forIccsn(smartcard.getIccsn());
  }
}
