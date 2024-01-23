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

package de.gematik.test.erezept.pspwsclient.dataobjects;

import lombok.Data;
import lombok.NonNull;

@Data
public class PspMessage {
  private DeliveryOption deliveryOption;
  private String clientId;
  private String transactionId;
  private byte[] blob;

  /** info note */
  private String note;

  public static PspMessage create(
      DeliveryOption deliveryOption,
      @NonNull String clientId,
      String transactionId,
      @NonNull byte[] blob) {
    return create(deliveryOption, clientId, transactionId, blob, null);
  }

  public static PspMessage create(
      DeliveryOption deliveryOption,
      @NonNull String telematikId,
      String transactionId,
      @NonNull byte[] blob,
      String message) {
    PspMessage pspMessage = new PspMessage();
    pspMessage.setDeliveryOption(deliveryOption);
    pspMessage.setClientId(telematikId);
    pspMessage.setTransactionId(transactionId);
    pspMessage.setBlob(blob);
    pspMessage.setNote(message);
    return pspMessage;
  }
}
