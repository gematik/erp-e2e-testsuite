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

package de.gematik.test.konnektor.commands;

import de.gematik.test.cardterminal.*;
import de.gematik.test.konnektor.*;
import de.gematik.test.konnektor.soap.*;
import de.gematik.ws.conn.cardservicecommon.v2.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import java.math.*;
import javax.xml.ws.*;
import lombok.*;

public class VerifyPinCommand extends AbstractKonnektorCommand<PinResponseType> {

  private final CardInfo cardInfo;
  private final PinType pinType;

  public VerifyPinCommand(CardInfo cardInfo, PinType pinType) {
    this.cardInfo = cardInfo;
    this.pinType = pinType;
  }

  @Override
  public PinResponseType execute(ContextType ctx, ServicePortProvider serviceProvider) {
    val servicePort = serviceProvider.getCardService();
    val response = new PinResponseType();
    val status = new Holder<>(new Status());
    val pinResult = new Holder<>(PinResultEnum.OK);
    val leftTries = new Holder<BigInteger>();
    this.executeAction(
        () ->
            servicePort.verifyPin(
                ctx, cardInfo.getHandle(), pinType.toString(), status, pinResult, leftTries));

    response.setLeftTries(leftTries.value);
    response.setPinResult(pinResult.value);
    response.setStatus(status.value);
    return response;
  }
}
