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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.v7.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GetCardsCommand extends AbstractKonnektorCommand<GetCardsResponse> {

  private final boolean mandantWide;

  public GetCardsCommand() {
    this(false);
  }

  public GetCardsCommand(boolean mandantWide) {
    this.mandantWide = mandantWide;
  }

  @Override
  public GetCardsResponse execute(ContextType ctx, ServicePortProvider serviceProvider) {
    val factory = new ObjectFactory();
    val servicePort = serviceProvider.getEventService();
    val payload = factory.createGetCards();
    payload.setMandantWide(mandantWide);
    payload.setContext(ctx);

    log.trace(format("Get cards mandantWide={0}", mandantWide));
    return this.executeSupplier(() -> servicePort.getCards(payload));
  }
}
