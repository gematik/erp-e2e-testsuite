/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.konnektor.commands;

import de.gematik.test.konnektor.CardHandle;
import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.math.BigInteger;
import javax.xml.ws.Holder;
import lombok.val;

public class VerifyPinCommand extends AbstractKonnektorCommand<PinResponseType> {

  private final CardHandle cardHandle;
  private final PinType pinType;

  public VerifyPinCommand(CardHandle cardHandle, PinType pinType) {
    this.cardHandle = cardHandle;
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
                ctx, cardHandle.getHandle(), pinType.toString(), status, pinResult, leftTries));

    response.setLeftTries(leftTries.value);
    response.setPinResult(pinResult.value);
    response.setStatus(status.value);
    return response;
  }
}
